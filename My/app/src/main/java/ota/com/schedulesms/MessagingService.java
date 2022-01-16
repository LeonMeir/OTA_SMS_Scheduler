package ota.com.schedulesms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ota.com.schedulesms.http.ClientRequests;
import ota.com.schedulesms.http.data.Load;
import ota.com.schedulesms.http.data.LoadData;
import ota.com.schedulesms.http.data.LoadDataSingle;
import ota.com.schedulesms.model.MessageData;

public class MessagingService extends Service {
    protected final Gson mGsonParser = new Gson();
    protected boolean started = false;
    protected int m_nDelay = 5;
//    protected int m_nDelayBetweenMessages = 60;
//    public int m_iLoadDataIndex = -1;
//    public ArrayList<LoadDataSingle> mArrData;
//    public boolean m_bIsRunningSendSms = false;
//    public static boolean hasStarted = false;
//    int m_iLoadIndex = 0;
    public static int m_iCurrentLoadSmsSimNumber = 0;
    public int m_iCurrentLoadSmsSimIndexInArr = -1;
    public HashMap<Integer, String> mMapSimTokens = new HashMap<>();
    public HashMap<Integer, Integer> mMapLoadDataIndexForSim = new HashMap<>();
    public HashMap<Integer, Integer> mMapDelayBetweenMessagesForSim = new HashMap<>();
    public HashMap<Integer, Boolean> mMapSendingSmsRunningStatus = new HashMap<>();
    public HashMap<Integer, Boolean> mMapStartedStatus = new HashMap<>();
    public HashMap<Integer, ArrayList<LoadDataSingle>> mMapSmsToSendViaSim = new HashMap<>();
    public ArrayList<Integer> mArrSimNumbersInUse = new ArrayList<>();
//    public String strTemp = "{\"id\":\"8\", \"text\":\"Test 8\", \"phone\":\"19804256367\"}";
//    public LoadDataSingle ldsTemp = jsondata.fromJson(strTemp, LoadDataSingle.class);
//    public String strTemp1 = "{\"id\":\"9\", \"text\":\"Test 9\", \"phone\":\"19804256367\"}";
//    public LoadDataSingle ldsTemp1 = jsondata.fromJson(strTemp1, LoadDataSingle.class);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final Handler handler = new Handler();

    public void initTokenSim(){
        int nCnt = getSimCount();
    }

    public void getCurrentUsingSims(){
        mArrSimNumbersInUse = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(Constant.SH_KEY_USE_STATUS_SIM1, false)){
            mArrSimNumbersInUse.add(0);
        }
        if (sharedPreferences.getBoolean(Constant.SH_KEY_USE_STATUS_SIM2, false)){
            mArrSimNumbersInUse.add(1);
        }
    }

    private void validateCurrentLoadSmsSimIndex(){
        getCurrentUsingSims();
        m_iCurrentLoadSmsSimIndexInArr++;
        if (m_iCurrentLoadSmsSimIndexInArr >= mArrSimNumbersInUse.size()){
            if (mArrSimNumbersInUse.size() > 0){
                m_iCurrentLoadSmsSimIndexInArr = m_iCurrentLoadSmsSimIndexInArr % mArrSimNumbersInUse.size();
            }else{
                m_iCurrentLoadSmsSimIndexInArr = -1;
            }
        }else if (mArrSimNumbersInUse.size() == 0){
            m_iCurrentLoadSmsSimIndexInArr = -1;
        }
        if (m_iCurrentLoadSmsSimIndexInArr >= 0 && m_iCurrentLoadSmsSimIndexInArr < mArrSimNumbersInUse.size()){
            m_iCurrentLoadSmsSimNumber = mArrSimNumbersInUse.get(m_iCurrentLoadSmsSimIndexInArr);
        }
    }

    private final Runnable mRunnableLoadDataFromServer = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {

            validateCurrentLoadSmsSimIndex();
            String token = mMapSimTokens.get(m_iCurrentLoadSmsSimNumber);
            if (token == null) token = "";
            try {
                String apiURL = Constant.SERVER_URL_LOAD;
                ClientRequests clientRequest = new ClientRequests();
                String data = clientRequest.post(apiURL + "?token=" + token, "{" +
                        "token:" + token + "}");
                if (data != null) {
                    Load json = mGsonParser.fromJson(data, Load.class);
                    if (json == null || json.data == null || json.data.system == null) {
                        m_nDelay = 5;
                        mMapDelayBetweenMessagesForSim.put(m_iCurrentLoadSmsSimNumber, 60);
                        sendErrorReportToServer(token, "Invalid Data!");
                    } else {
                        try {
                            m_nDelay = Integer.parseInt(json.data.system.delay);
                            mMapDelayBetweenMessagesForSim.put(
                                    m_iCurrentLoadSmsSimNumber, Integer.parseInt(json.data.system.delayBetweenMessages));
                        } catch (Exception e) {
                            m_nDelay = 5;
                            mMapDelayBetweenMessagesForSim.put(m_iCurrentLoadSmsSimNumber, 60);
                            sendErrorReportToServer(token, "Invalid Delay and DelayBetweenMessages!");
                        }
                    }
                    LoadData main_data = null;
                    if (json != null) {
                        main_data = json.data;
                    }else{
                        sendErrorReportToServer(token, "Invalid Data!");
                    }
                    if (main_data != null && main_data.data != null) {
                        addSmsToSendIntoArray(main_data.data, m_iCurrentLoadSmsSimNumber);
                    }else{
                        sendErrorReportToServer(token, "Invalid Data!");
                    }
                    Boolean bIsRunningSendSms = mMapSendingSmsRunningStatus.get(m_iCurrentLoadSmsSimNumber);
                    if (bIsRunningSendSms == null) bIsRunningSendSms = false;
                    Boolean bIsStarted = mMapStartedStatus.get(m_iCurrentLoadSmsSimNumber);
                    if (bIsStarted == null) bIsStarted = false;
                    if (bIsStarted && !bIsRunningSendSms) {
                        handler.post(new SendSmsRunnable(m_iCurrentLoadSmsSimNumber));
                    } else {
                        mMapStartedStatus.put(m_iCurrentLoadSmsSimNumber, true);
                    }
                    handler.postDelayed(this, m_nDelay * 1000L);
                } else {
                    handler.postDelayed(this, m_nDelay * 1000L);
                    sendErrorReportToServer(token, "Invalid Data!");
                }
            } catch (IOException e) {
                handler.postDelayed(this, m_nDelay * 1000L);
                sendErrorReportToServer(token, "Communication Error");
                e.printStackTrace();
            }catch(Exception e){
                handler.postDelayed(this, m_nDelay * 1000L);
                sendErrorReportToServer(token,e.getMessage());
                e.printStackTrace();
            }
        }
    };

    private void refreshCurrentSimForLoadData(){
        m_iCurrentLoadSmsSimNumber++;
        int nSimCnt = getSimCount();
        if (nSimCnt > 0){
            m_iCurrentLoadSmsSimNumber %= nSimCnt;
        }else{
            m_iCurrentLoadSmsSimNumber = -1;
        }
    }

    public void sendErrorReportToServer(final String token,final  String strError) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    ClientRequests clientRequest = new ClientRequests();
                    String strApiUrl = Constant.SERVER_URL_ERROR;
                    String data = clientRequest.post(strApiUrl + "?token=" + token + "&error=" + strError, "{" +
                            "token:" + token + ",error:" + strError + "}");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

//    public void addSmsToSendIntoArray(ArrayList<LoadDataSingle> arrData) {
//        if (mArrData == null) mArrData = new ArrayList<>();
//        int nCnt = 0;
//        if (arrData != null && arrData.size() > 0) {
//            int j;
//            DBHelper dbHelper = null;
//            try {
//                dbHelper = new DBHelper(getApplicationContext());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            for (int i = 0; i < arrData.size(); i++) {
//                LoadDataSingle dataToSave = arrData.get(i);
//                for (j = 0; j < mArrData.size(); j++) {
//                    LoadDataSingle dataSaved = mArrData.get(j);
//                    if (dataSaved.id != null && dataToSave.id != null &&
//                            dataSaved.id.equals(dataToSave.id)) {
//                        break;
//                    }
//                }
//                if (j < mArrData.size()) continue;
//                try {
//                    MessageData messageDataInDB = dbHelper != null ?
//                            dbHelper.getMessageData(
//                                    Integer.parseInt(dataToSave.id != null ? dataToSave.id : "-1")) : null;
//                    if (messageDataInDB != null) continue;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                mArrData.add(dataToSave);
//                nCnt++;
//            }
//            try {
//                if (dbHelper != null) dbHelper.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
////        MainActivity.instance.mTvNewSms.setText(String.format("Newly added Sms count:%d", nCnt));
//    }

    public void addSmsToSendIntoArray(ArrayList<LoadDataSingle> arrData, int iSimIndex) {
        ArrayList<LoadDataSingle> arrDataFromMap = mMapSmsToSendViaSim.get(iSimIndex);
        if (arrDataFromMap == null) arrDataFromMap = new ArrayList<>();
        int nCnt = 0;
        if (arrData != null && arrData.size() > 0) {
            int j;
            DBHelper dbHelper = null;
            try {
                dbHelper = new DBHelper(getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < arrData.size(); i++) {
                LoadDataSingle dataToSave = arrData.get(i);
                for (j = 0; j < arrDataFromMap.size(); j++) {
                    LoadDataSingle dataSaved = arrDataFromMap.get(j);
                    if (dataSaved.id != null && dataToSave.id != null &&
                            dataSaved.id.equals(dataToSave.id)) {
                        break;
                    }
                }
                if (j < arrDataFromMap.size()) continue;
                try {
                    MessageData messageDataInDB = dbHelper != null ?
                            dbHelper.getMessageData(
                                    Integer.parseInt(dataToSave.id != null ? dataToSave.id : "-1")) : null;
                    if (messageDataInDB != null) continue;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                arrDataFromMap.add(dataToSave);
                nCnt++;
            }
            try {
                if (dbHelper != null) dbHelper.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMapSmsToSendViaSim.put(iSimIndex, arrDataFromMap);
        }
//        MainActivity.instance.mTvNewSms.setText(String.format("Newly added Sms count:%d", nCnt));
    }

    public void initData(){
//        mArrData = new ArrayList<>();
        mMapSimTokens = new HashMap<>();
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        int nSimCnt = getSimCount();
        if (nSimCnt == 1){
            mMapSimTokens.put(0, sharedPreferences.getString(Constant.SH_KEY_SIM1_TOKEN, ""));
        }else if(nSimCnt == 2){
            mMapSimTokens.put(0, sharedPreferences.getString(Constant.SH_KEY_SIM1_TOKEN, ""));
            mMapSimTokens.put(1, sharedPreferences.getString(Constant.SH_KEY_SIM2_TOKEN, ""));
        }
        mMapLoadDataIndexForSim = new HashMap<>();
        for (int i = 0; i < nSimCnt; i++){
            mMapLoadDataIndexForSim.put(i, 0);
            mMapSendingSmsRunningStatus.put(i, false);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        initData();
        initListeningIncomingCallForDualSims();
        handler.post(mRunnableLoadDataFromServer);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            //The Main unremovable notification
            NotificationChannel main_channel = new NotificationChannel(NotificationChannels.MAIN_NOTIFICATION_CHANNEL, NotificationChannels.MAIN_NOTIFICATION_CHANNEL_TITLE, importance);
            main_channel.setDescription(NotificationChannels.MAIN_NOTIFICATION_CHANNEL_DESCRIPTION);
            //For giving network notification update
            NotificationChannel network_channel = new NotificationChannel(NotificationChannels.NETWORK_NOTIFICATION_CHANNEL, NotificationChannels.NETWORK_NOTIFICATION_CHANNEL_TITLE, importance);
            main_channel.setDescription(NotificationChannels.NETWORK_NOTIFICATION_CHANNEL_DESCRIPTION);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(main_channel);
            notificationManager.createNotificationChannel(network_channel);
        }
        Intent notifIntent = new Intent(this, MainActivity.class);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder main_notif = new NotificationCompat.Builder(this, NotificationChannels.MAIN_NOTIFICATION_CHANNEL);
        Notification main_notification = main_notif.setContentTitle(NotificationChannels.MAIN_NOTIFICATION_CHANNEL_TITLE)
                .setContentText(NotificationChannels.MAIN_NOTIFICATION_CHANNEL_DESCRIPTION)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setLocalOnly(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(NotificationCompat.FLAG_FOREGROUND_SERVICE, main_notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(mRunnableLoadDataFromServer);
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("status", false).apply();
    }

    private boolean isAppOnForeground(Context context) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) return false;
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

//    public class SendSmsRunnable implements Runnable {
//
//        public int m_iSimIndex = -1;
//
//        public SendSmsRunnable() {
//        }
//
//        public SendSmsRunnable(int iSimIndex) {
//            m_iSimIndex = iSimIndex;
//        }
//
//        @SuppressLint("MissingPermission")
//        @Override
//        public void run() {
////            try {
//            m_bIsRunningSendSms = true;
//            ArrayList<LoadDataSingle> arrData = mMapSmsToSendViaSim.get(m_iSimIndex);
//            if (arrData == null) arrData = new ArrayList<>();
////            MainActivity.instance.mTvStatus.setText(String.format("%d of %d", m_iLoadDataIndex, mArrData.size()));
//            if (m_iLoadDataIndex >= 0 && m_iLoadDataIndex < mArrData.size()) {
//                DBHelper dbHelper = new DBHelper(getApplicationContext());
//                LoadDataSingle lds = mArrData.get(m_iLoadDataIndex);
//                long millitime = System.currentTimeMillis();
//                MessageData messageData = null;
//                try {
//                    messageData = dbHelper.getMessageData(Integer.parseInt(lds.id));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                if (messageData == null) {
//                    messageData = new MessageData(Integer.parseInt(lds.id),
//                            lds.text, lds.phone, millitime, millitime,
//                            "nums", Constant.STATUS_INIT, Constant.STATUS_STARTED, Constant.STATUS_INIT);
//                    dbHelper.insertOrUpdateMessageData(messageData);
//
//                    Intent intentSent = new Intent(getApplicationContext(), MessageStatusReceiver.class);
//
//                    Bundle bundleSent = new Bundle();
//                    bundleSent.putString(Constant.BUNDLE_KEY_ID, lds.id);
//                    bundleSent.putString(Constant.BUNDLE_KEY_TEXT, lds.text);
//                    bundleSent.putString(Constant.BUNDLE_KEY_PHONE, lds.phone);
//                    bundleSent.putString(Constant.BUNDLE_KEY_NANOTIME, "" + millitime);
//                    bundleSent.putString(Constant.BUNDLE_KEY_STATUS, Constant.STATUS_SENT);
//
//                    Intent intentDelivery = new Intent(getApplicationContext(), MessageStatusReceiver.class);
//                    Bundle bundleDelivered = new Bundle();
//                    bundleDelivered.putString(Constant.BUNDLE_KEY_ID, lds.id);
//                    bundleDelivered.putString(Constant.BUNDLE_KEY_TEXT, lds.text);
//                    bundleDelivered.putString(Constant.BUNDLE_KEY_PHONE, lds.phone);
//                    bundleDelivered.putString(Constant.BUNDLE_KEY_NANOTIME, "" + millitime);
//                    bundleDelivered.putString(Constant.BUNDLE_KEY_STATUS, Constant.STATUS_RECEIVED);
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//                        SubscriptionManager localSubscriptionManager = SubscriptionManager.from(getApplicationContext());
//                        if (Constant.m_bReadPhoneState) {
//                            if (localSubscriptionManager != null &&
//                                    localSubscriptionManager.getActiveSubscriptionInfoCount() >= 1) {
//                                List<SubscriptionInfo> localList = localSubscriptionManager.getActiveSubscriptionInfoList();
//                                if (localList != null && localList.size() > 0) {
//                                    SubscriptionInfo simInfo1 = localList.get(0);
//
//                                    bundleSent.putString(Constant.BUNDLE_KEY_SUBSCRIPTION_ID, "" + simInfo1.getSubscriptionId());
//
//                                    bundleDelivered.putString(Constant.BUNDLE_KEY_SUBSCRIPTION_ID, "" + simInfo1.getSubscriptionId());
//
//                                    intentSent.putExtras(bundleSent);
//                                    @SuppressLint("UnspecifiedImmutableFlag")
//                                    PendingIntent sentintent = PendingIntent.getBroadcast(getApplicationContext(),
//                                            Integer.parseInt(lds.id),
//                                            intentSent,
//                                            PendingIntent.FLAG_ONE_SHOT);
//
//                                    intentDelivery.putExtras(bundleDelivered);
//                                    @SuppressLint("UnspecifiedImmutableFlag")
//                                    PendingIntent deliveryintent = PendingIntent.getBroadcast(getApplicationContext(),
//                                            Integer.parseInt(lds.id),
//                                            intentDelivery,
//                                            PendingIntent.FLAG_UPDATE_CURRENT);
//
//                                    SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(lds.phone,
//                                            null, lds.text, sentintent, deliveryintent);
//                                }
//                            }
//                        }
//                    } else {
//                        intentSent.putExtras(bundleSent);
//                        @SuppressLint("UnspecifiedImmutableFlag")
//                        PendingIntent sentintent = PendingIntent.getBroadcast(getApplicationContext(),
//                                Integer.parseInt(lds.id),
//                                intentSent,
//                                PendingIntent.FLAG_ONE_SHOT);
//
//                        intentDelivery.putExtras(bundleDelivered);
//                        @SuppressLint("UnspecifiedImmutableFlag")
//                        PendingIntent deliveryintent = PendingIntent.getBroadcast(getApplicationContext(),
//                                Integer.parseInt(lds.id),
//                                intentDelivery,
//                                PendingIntent.FLAG_UPDATE_CURRENT);
//
//                        SmsManager.getDefault().sendTextMessage(lds.phone, null, lds.text, sentintent, deliveryintent);
//
//                    }
//
////                    final String strID = lds.id;
////                    handler.postDelayed(new Runnable() {
////                        @Override
////                        public void run() {
////                            Intent int1 = new Intent(getApplicationContext(), MessageStatusReceiver.class);
////                            Bundle bun1 = new Bundle();
////                            bun1.putString(Constant.DB_FIELDNAME_SMS_ID, strID);
////                            bun1.putString(Constant.BUNDLE_KEY_STATUS, Constant.STATUS_SENT);
////                            bun1.putString(Constant.DB_FIELDNAME_SMS_PHONE, strID);
////                            int1.putExtras(bun1);
////                            sendBroadcast(int1);
////                        }
////                    }, 2000);
////
////                    handler.postDelayed(new Runnable() {
////                        @Override
////                        public void run() {
////                            Intent int1 = new Intent(getApplicationContext(), MessageStatusReceiver.class);
////                            Bundle bun1 = new Bundle();
////                            bun1.putString(Constant.DB_FIELDNAME_SMS_ID, strID);
////                            bun1.putString(Constant.BUNDLE_KEY_STATUS, Constant.STATUS_RECEIVED);
////                            bun1.putString(Constant.DB_FIELDNAME_SMS_PHONE, strID);
////                            int1.putExtras(bun1);
////                            sendBroadcast(int1);
////                        }
////                    }, 6000);
//
//                    mArrData.remove(m_iLoadDataIndex);
//                    if (m_iLoadDataIndex < mArrData.size()) {
//                        handler.postDelayed(new SendSmsRunnable(), m_nDelayBetweenMessages * 1000L);
//                    } else {
//                        m_bIsRunningSendSms = false;
//                    }
//                } else {
//                    try {
//                        mArrData.remove(m_iLoadDataIndex);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    if (m_iLoadDataIndex < mArrData.size()) {
//                        handler.post(new SendSmsRunnable());
//                    } else {
//                        m_bIsRunningSendSms = false;
//                    }
//                }
//                dbHelper.close();
//            } else {
//                m_bIsRunningSendSms = false;
//            }
//        }
//    }

    public class SendSmsRunnable implements Runnable {

        public int m_iSimIndex = -1;

        public SendSmsRunnable() {
        }

        public SendSmsRunnable(int iSimIndex) {
            m_iSimIndex = iSimIndex;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void run() {
//            try {
            mMapSendingSmsRunningStatus.put(m_iSimIndex, true);
            ArrayList<LoadDataSingle> arrData = mMapSmsToSendViaSim.get(m_iSimIndex);
            if (arrData == null) arrData = new ArrayList<>();
            Integer iLoadDataIndexForSim = mMapLoadDataIndexForSim.get(m_iSimIndex);
            if (iLoadDataIndexForSim == null) iLoadDataIndexForSim = 0;
            if (iLoadDataIndexForSim >= 0 && iLoadDataIndexForSim < arrData.size()) {
                DBHelper dbHelper = new DBHelper(getApplicationContext());
                LoadDataSingle lds = arrData.get(iLoadDataIndexForSim);
                long millitime = System.currentTimeMillis();
                MessageData messageData = null;
                try {
                    messageData = dbHelper.getMessageData(Integer.parseInt(lds.id));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (messageData == null) {
                    messageData = new MessageData(Integer.parseInt(lds.id),
                            lds.text, lds.phone, millitime, millitime,
                            "nums", Constant.STATUS_INIT, Constant.STATUS_STARTED, Constant.STATUS_INIT);
                    dbHelper.insertOrUpdateMessageData(messageData);

                    Intent intentSent = new Intent(getApplicationContext(), MessageStatusReceiver.class);

                    Bundle bundleSent = new Bundle();
                    bundleSent.putString(Constant.BUNDLE_KEY_ID, lds.id);
                    bundleSent.putString(Constant.BUNDLE_KEY_TEXT, lds.text);
                    bundleSent.putString(Constant.BUNDLE_KEY_PHONE, lds.phone);
                    bundleSent.putString(Constant.BUNDLE_KEY_NANOTIME, "" + millitime);
                    bundleSent.putString(Constant.BUNDLE_KEY_STATUS, Constant.STATUS_SENT);

                    Intent intentDelivery = new Intent(getApplicationContext(), MessageStatusReceiver.class);
                    Bundle bundleDelivered = new Bundle();
                    bundleDelivered.putString(Constant.BUNDLE_KEY_ID, lds.id);
                    bundleDelivered.putString(Constant.BUNDLE_KEY_TEXT, lds.text);
                    bundleDelivered.putString(Constant.BUNDLE_KEY_PHONE, lds.phone);
                    bundleDelivered.putString(Constant.BUNDLE_KEY_NANOTIME, "" + millitime);
                    bundleDelivered.putString(Constant.BUNDLE_KEY_STATUS, Constant.STATUS_RECEIVED);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        SubscriptionManager localSubscriptionManager = SubscriptionManager.from(getApplicationContext());
                        if (Constant.m_bReadPhoneState) {
                            if (localSubscriptionManager != null &&
                                    localSubscriptionManager.getActiveSubscriptionInfoCount() >= 1) {
                                List<SubscriptionInfo> localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                                if (localList != null && localList.size() > m_iSimIndex && m_iSimIndex != -1) {
                                    SubscriptionInfo simInfo1 = localList.get(m_iSimIndex);

                                    bundleSent.putString(Constant.BUNDLE_KEY_SUBSCRIPTION_ID, "" + simInfo1.getSubscriptionId());

                                    bundleDelivered.putString(Constant.BUNDLE_KEY_SUBSCRIPTION_ID, "" + simInfo1.getSubscriptionId());

                                    intentSent.putExtras(bundleSent);
                                    @SuppressLint("UnspecifiedImmutableFlag")
                                    PendingIntent sentintent = PendingIntent.getBroadcast(getApplicationContext(),
                                            Integer.parseInt(lds.id),
                                            intentSent,
                                            PendingIntent.FLAG_ONE_SHOT);

                                    intentDelivery.putExtras(bundleDelivered);
                                    @SuppressLint("UnspecifiedImmutableFlag")
                                    PendingIntent deliveryintent = PendingIntent.getBroadcast(getApplicationContext(),
                                            Integer.parseInt(lds.id),
                                            intentDelivery,
                                            PendingIntent.FLAG_UPDATE_CURRENT);

                                    SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(lds.phone,
                                            null, lds.text, sentintent, deliveryintent);
                                }
                            }
                        }
                    } else {
                        intentSent.putExtras(bundleSent);
                        @SuppressLint("UnspecifiedImmutableFlag")
                        PendingIntent sentintent = PendingIntent.getBroadcast(getApplicationContext(),
                                Integer.parseInt(lds.id),
                                intentSent,
                                PendingIntent.FLAG_ONE_SHOT);

                        intentDelivery.putExtras(bundleDelivered);
                        @SuppressLint("UnspecifiedImmutableFlag")
                        PendingIntent deliveryintent = PendingIntent.getBroadcast(getApplicationContext(),
                                Integer.parseInt(lds.id),
                                intentDelivery,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        SmsManager.getDefault().sendTextMessage(lds.phone, null, lds.text, sentintent,
                                deliveryintent);

                    }

//                    final String strID = lds.id;
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent int1 = new Intent(getApplicationContext(), MessageStatusReceiver.class);
//                            Bundle bun1 = new Bundle();
//                            bun1.putString(Constant.DB_FIELDNAME_SMS_ID, strID);
//                            bun1.putString(Constant.BUNDLE_KEY_STATUS, Constant.STATUS_SENT);
//                            bun1.putString(Constant.DB_FIELDNAME_SMS_PHONE, strID);
//                            int1.putExtras(bun1);
//                            sendBroadcast(int1);
//                        }
//                    }, 2000);
//
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent int1 = new Intent(getApplicationContext(), MessageStatusReceiver.class);
//                            Bundle bun1 = new Bundle();
//                            bun1.putString(Constant.DB_FIELDNAME_SMS_ID, strID);
//                            bun1.putString(Constant.BUNDLE_KEY_STATUS, Constant.STATUS_RECEIVED);
//                            bun1.putString(Constant.DB_FIELDNAME_SMS_PHONE, strID);
//                            int1.putExtras(bun1);
//                            sendBroadcast(int1);
//                        }
//                    }, 6000);

                    arrData.remove(iLoadDataIndexForSim.intValue());
                    mMapSmsToSendViaSim.put(m_iSimIndex, arrData);
                    if (iLoadDataIndexForSim < arrData.size()) {
                        Integer nDelayBetweenMessages = mMapDelayBetweenMessagesForSim.get(m_iSimIndex);
                        if (nDelayBetweenMessages == null) nDelayBetweenMessages = 60;
                        handler.postDelayed(new SendSmsRunnable(this.m_iSimIndex), nDelayBetweenMessages * 1000L);
                    } else {
                        mMapSendingSmsRunningStatus.put(m_iSimIndex, false);
                    }
                } else {
                    try {
                        arrData.remove(iLoadDataIndexForSim.intValue());
                        mMapSmsToSendViaSim.put(m_iSimIndex, arrData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (iLoadDataIndexForSim < arrData.size()) {
                        handler.post(new SendSmsRunnable(this.m_iSimIndex));
                    } else {
                        mMapSendingSmsRunningStatus.put(m_iSimIndex, false);
                    }
                }
                dbHelper.close();
            } else {
                mMapSendingSmsRunningStatus.put(m_iSimIndex, false);
            }
        }
    }

    public int getSimCount(){
//        return 2;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        int i = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = (SubscriptionManager)getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (Constant.m_bReadPhoneState) {
                if (subscriptionManager != null) {
                    List<SubscriptionInfo> localList = subscriptionManager.getActiveSubscriptionInfoList();
                    if (localList != null) {
                        return localList.size();
                    }
                }
            }
        }
        return 1;
    }
    //New By LongYi End

    public void initListeningIncomingCallForDualSims(){
        try {
            final Class<?> tmClass = Class.forName(getPackageName() + ".MultiSimTelephonyManager");
            // MultiSimTelephonyManager Class found
            // getDefault() gets the manager instances for specific slots
            Method methodDefault = tmClass.getDeclaredMethod("getDefault", int.class);
            methodDefault.setAccessible(true);
            try {
                for (int slot = 0; slot < 2; slot++) {
                    MultiSimTelephonyManager telephonyManagerMultiSim = (MultiSimTelephonyManager)methodDefault.invoke(null, slot);
                    if (telephonyManagerMultiSim != null) {
                        telephonyManagerMultiSim.listen(new MultiSimListener(slot), PhoneStateListener.LISTEN_CALL_STATE);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // (Not tested) the getDefault method might cause the exception if there is only 1 slot
            }
        } catch (ClassNotFoundException e) {
            //
        } catch (NoSuchMethodException e) {
            //
        } catch (IllegalAccessException e) {
            //
        } catch (InvocationTargetException e) {
            //
        } catch (ClassCastException e) {
            //
        }
    }
}
