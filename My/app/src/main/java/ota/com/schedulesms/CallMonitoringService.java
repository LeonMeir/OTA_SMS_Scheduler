package ota.com.schedulesms;

import static java.util.Objects.requireNonNull;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class CallMonitoringService extends Service {

    private static final String ACTION_START = "YACB_ACTION_START";
    private static final String ACTION_STOP = "YACB_ACTION_STOP";
    public static CallMonitoringService instance = null;
    private final MyPhoneStateListener phoneStateListenerForSingleSim = new MyPhoneStateListener();
    private final MyPhoneStateListener phoneStateListenerForSim1 = new MyPhoneStateListener(1);
    private final MyPhoneStateListener phoneStateListenerForSim2 = new MyPhoneStateListener(2);
    private final PhoneStateBroadcastReceiver phoneStateBroadcastReceiver
            = new PhoneStateBroadcastReceiver(PhoneStateHandler.Source.PHONE_STATE_BROADCAST_RECEIVER_MONITORING);
//    private final PhoneStateBroadcastReceiver phoneStateBroadcastReceiverForSim1
//            = new PhoneStateBroadcastReceiver(PhoneStateHandler.Source.PHONE_STATE_BROADCAST_RECEIVER_MONITORING, 1);
//    private final PhoneStateBroadcastReceiver phoneStateBroadcastReceiverForSim2
//            = new PhoneStateBroadcastReceiver(PhoneStateHandler.Source.PHONE_STATE_BROADCAST_RECEIVER_MONITORING, 2);

    private boolean m_bBroadcastReceiverStatus = false;
    private boolean m_bMonitoringStatusForAll;
    private boolean m_bMonitoringStartedSingleSim;
    private boolean m_bMonitoringStartedSim1ForDualSim;
    private boolean m_bMonitoringStartedSim2ForDualSim;

    public static void start(Context context) {
        ContextCompat.startForegroundService(context, getIntent(context, ACTION_START));
    }

    public static void start(Context context, int nSubId) {
        if (instance == null) {
            ContextCompat.startForegroundService(context, getIntent(context, ACTION_START, nSubId));
        } else {
            if (!instance.m_bMonitoringStatusForAll) {
                ContextCompat.startForegroundService(context, getIntent(context, ACTION_START, nSubId));
            } else {
                instance.startMonitoring(nSubId);
            }
        }
    }

    public static void stop(Context context) {
        context.stopService(getIntent(context, ACTION_STOP));
    }

    public static void stop(Context context, int nSubId) {
        if (instance != null) {
            instance.stopMonitoring(nSubId);
            if (!instance.m_bMonitoringStatusForAll) {
                instance.stopForeground();
                instance.stopSelf();
                instance = null;
            }
        }else {
            context.stopService(getIntent(context, ACTION_STOP, nSubId));
        }
    }

    private static Intent getIntent(Context context, String action) {
        Intent intent = new Intent(context, CallMonitoringService.class);
        intent.setAction(action);
        return intent;
    }

    private static Intent getIntent(Context context, String action, int nSubId) {
        Intent intent = new Intent(context, CallMonitoringService.class);
        intent.setAction(action);
        intent.putExtra("SubId", nSubId);
        return intent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            int nSubId = 0;
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                nSubId = bundle.getInt("SubId", 0);
            }
            stopMonitoring(nSubId);
            if (!m_bMonitoringStatusForAll) {
                stopForeground();
                stopSelf();
                instance = null;
            }
        } else {
            int nSubId = 0;
            if (intent != null) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    nSubId = bundle.getInt("SubId", 0);
                }
            }
            if (!m_bMonitoringStatusForAll) {
                startForeground();
            }
            startMonitoring(nSubId);
            instance = this;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onDestroy() {
        MainActivity.displayStatus("Stop Call Status Monitoring", false);
        stopMonitoring();
        instance = null;
    }

    private void startForeground() {
        startForeground(NotificationHelper.NOTIFICATION_ID_MONITORING_SERVICE,
                NotificationHelper.createMonitoringServiceNotification(this));
    }

    private void stopForeground() {
        stopForeground(true);
    }

    private void startMonitoring() {
        if (m_bMonitoringStartedSingleSim) return;
        m_bMonitoringStartedSingleSim = true;

        try {
            getTelephonyManager().listen(phoneStateListenerForSingleSim, PhoneStateListener.LISTEN_CALL_STATE);

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(TelephonyManager.EXTRA_STATE_RINGING); // TODO: check
            intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            registerReceiver(phoneStateBroadcastReceiver, intentFilter);
            m_bBroadcastReceiverStatus = true;
        } catch (Exception e) {
        }
    }

    private void startMonitoring(int nSubId) {
        if (nSubId == 0) {
            if (m_bMonitoringStartedSingleSim) return;
            m_bMonitoringStartedSingleSim = true;
            try {
                getTelephonyManager().listen(phoneStateListenerForSingleSim, PhoneStateListener.LISTEN_CALL_STATE);
                phoneStateBroadcastReceiver.enableForSubId(0);
                if (!m_bBroadcastReceiverStatus) {
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(TelephonyManager.EXTRA_STATE_RINGING); // TODO: check
                    intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                    registerReceiver(phoneStateBroadcastReceiver, intentFilter);
                    m_bBroadcastReceiverStatus = true;
                }
            } catch (Exception e) {
            }
        } else if (nSubId == 1) {
            if (m_bMonitoringStartedSim1ForDualSim) return;
            m_bMonitoringStartedSim1ForDualSim = true;
            try {
                getTelephonyManager(0).listen(phoneStateListenerForSim1, PhoneStateListener.LISTEN_CALL_STATE);
                phoneStateBroadcastReceiver.enableForSubId(1);
                if (!m_bBroadcastReceiverStatus) {
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(TelephonyManager.EXTRA_STATE_RINGING); // TODO: check
                    intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                    registerReceiver(phoneStateBroadcastReceiver, intentFilter);
                    m_bBroadcastReceiverStatus = true;
                }
            } catch (Exception e) {
            }
        } else if (nSubId == 2) {
            if (m_bMonitoringStartedSim2ForDualSim) return;
            m_bMonitoringStartedSim2ForDualSim = true;
            try {
                getTelephonyManager(1).listen(phoneStateListenerForSim2, PhoneStateListener.LISTEN_CALL_STATE);
                phoneStateBroadcastReceiver.enableForSubId(2);
                if (!m_bBroadcastReceiverStatus) {
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(TelephonyManager.EXTRA_STATE_RINGING); // TODO: check
                    intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                    registerReceiver(phoneStateBroadcastReceiver, intentFilter);
                    m_bBroadcastReceiverStatus = true;
                }
            } catch (Exception e) {
            }
        }
        m_bMonitoringStatusForAll = m_bMonitoringStartedSingleSim ||
                                    m_bMonitoringStartedSim1ForDualSim ||
                                    m_bMonitoringStartedSim2ForDualSim;
    }

    public void handleMonitoring(boolean bSupportDualSim, int iSimIndex, boolean bMonitoring) {

    }

    private void stopMonitoring() {
        if (!m_bMonitoringStartedSingleSim) return;

        try {
            getTelephonyManager().listen(phoneStateListenerForSingleSim, PhoneStateListener.LISTEN_NONE);
            unregisterReceiver(phoneStateBroadcastReceiver);
            m_bBroadcastReceiverStatus = false;
        } catch (Exception e) {
        }

        m_bMonitoringStartedSingleSim = false;
    }

    private void stopMonitoring(int nSubId) {
        if (nSubId == 0 && !m_bMonitoringStartedSingleSim) return;
        if (nSubId == 1 && !m_bMonitoringStartedSim1ForDualSim) return;
        if (nSubId == 2 && !m_bMonitoringStartedSim2ForDualSim) return;
        try {
            if (nSubId == 0){
                getTelephonyManager().listen(phoneStateListenerForSingleSim, PhoneStateListener.LISTEN_NONE);
                phoneStateBroadcastReceiver.disableForSubId(0);
                if (!m_bMonitoringStartedSim1ForDualSim && !m_bMonitoringStartedSim2ForDualSim){
                    unregisterReceiver(phoneStateBroadcastReceiver);
                }
                m_bMonitoringStartedSingleSim = false;
            }
            if (nSubId == 1){
                getTelephonyManager(0).listen(phoneStateListenerForSim1, PhoneStateListener.LISTEN_NONE);
                phoneStateBroadcastReceiver.disableForSubId(1);
                if (!m_bMonitoringStartedSingleSim && !m_bMonitoringStartedSim2ForDualSim){
                    unregisterReceiver(phoneStateBroadcastReceiver);
                }
                m_bMonitoringStartedSim1ForDualSim = false;
            }
            if (nSubId == 2){
                getTelephonyManager(1).listen(phoneStateListenerForSim2, PhoneStateListener.LISTEN_NONE);
                phoneStateBroadcastReceiver.disableForSubId(2);
                if (!m_bMonitoringStartedSingleSim && !m_bMonitoringStartedSim1ForDualSim){
                    unregisterReceiver(phoneStateBroadcastReceiver);
                }
                m_bMonitoringStartedSim2ForDualSim = false;
            }
        } catch (Exception e) {
        }
        m_bMonitoringStatusForAll = m_bMonitoringStartedSingleSim ||
                                    m_bMonitoringStartedSim1ForDualSim ||
                                    m_bMonitoringStartedSim2ForDualSim;
    }

    private TelephonyManager getTelephonyManager() {
        return requireNonNull(
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
    }

    private TelephonyManager getTelephonyManager(int nSubId) {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (nSubId == 0) {
            return tm;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return tm.createForSubscriptionId(nSubId - 1);
            } else {
                return tm;
            }
        }
    }

    private static class MyPhoneStateListener extends PhoneStateListener {

        public int m_nSubId;

        public MyPhoneStateListener() {
            super();
        }


        public MyPhoneStateListener(int nSubId) {
            super();
            m_nSubId = nSubId;
        }

        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            /*
             * According to docs, an empty string may be passed if the app lacks permissions.
             * The app deals with permissions in PhoneStateHandler.
             */
            if (TextUtils.isEmpty(phoneNumber)) {
                phoneNumber = null;
            }

            PhoneStateHandler phoneStateHandler = YacbHolder.getPhoneStateHandler();
            PhoneStateHandler.Source source = PhoneStateHandler.Source.PHONE_STATE_LISTENER;

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    phoneStateHandler.onIdle(source, phoneNumber);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    MainActivity.displayStatus(String.format("onCallStateChanged(Ringing) of CallMonitoringService:%s",
                            phoneNumber == null ? "NULL" : phoneNumber), false);
                    phoneStateHandler.onRinging(source, phoneNumber, m_nSubId);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    phoneStateHandler.onOffHook(source, phoneNumber);
                    break;
            }
        }
    }

    public int getSimCount() {
//        return 2;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        int i = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
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
}
