package ota.com.schedulesms;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ota.com.schedulesms.Constant;
import ota.com.schedulesms.DBHelper;
import ota.com.schedulesms.http.ClientRequests;
import ota.com.schedulesms.model.MessageData;

public class MessageStatusReceiver extends BroadcastReceiver {

    public Handler mHandler = new Handler();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        DBHelper dbHelper = new DBHelper(context);
        if (getResultCode() == Activity.RESULT_OK) {
            try {
                MessageData messageData = dbHelper.getMessageData(
                        Integer.parseInt(bundle.getString(Constant.DB_FIELDNAME_SMS_ID)));
                if (messageData != null) {
                    messageData.mStrPrevDbStatus = messageData.mStrDbStatus;
                    messageData.mStrDbStatus = bundle.getString(Constant.BUNDLE_KEY_STATUS);
                    messageData.m_lTimestamp = System.currentTimeMillis();
                    dbHelper.insertOrUpdateMessageData(messageData);
                    uploadSmsStatusToServer(context, messageData);
                }
                dbHelper.close();
                dbHelper = null;
            } catch (Exception e) {
                e.printStackTrace();
                dbHelper.close();
            }
            if (isAppOnForeground((context))) {
                if (bundle.getString(Constant.BUNDLE_KEY_STATUS).equals(Constant.STATUS_SENT)) {
                    Toast.makeText(context, "Message Sent to " +
                            bundle.getString(Constant.DB_FIELDNAME_SMS_PHONE), Toast.LENGTH_SHORT).show();
                }
                if (bundle.getString(Constant.BUNDLE_KEY_STATUS).equals(Constant.STATUS_RECEIVED)) {
                    Toast.makeText(context, "Message Delivered to " +
                            bundle.getString(Constant.DB_FIELDNAME_SMS_PHONE), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (getResultCode() == SmsManager.RESULT_ERROR_GENERIC_FAILURE ||
                getResultCode() == SmsManager.RESULT_ERROR_NO_SERVICE ||
                getResultCode() == SmsManager.RESULT_ERROR_RADIO_OFF) {
            try {
                MessageData messageData = dbHelper.getMessageData(
                        Integer.parseInt(bundle.getString(Constant.JSON_KEY_UPLOAD_SMS_ID)));
                if (messageData != null) {
                    messageData.mStrPrevDbStatus = messageData.mStrDbStatus;
                    messageData.mStrDbStatus = Constant.STATUS_FAILED;
                    messageData.m_lTimestamp = System.currentTimeMillis();
                    dbHelper.insertOrUpdateMessageData(messageData);
                    uploadSmsStatusToServer(context, messageData);
//                    mHandler.post(new RunnableUploadSmsStatusToServer(context, messageData));
                }
                dbHelper.close();
                dbHelper = null;
                if (isAppOnForeground((context))) {
                    Toast.makeText(context, "Message Generic Failure to " +
                            bundle.getString(Constant.DB_FIELDNAME_SMS_PHONE), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                dbHelper.close();
            }
        } else {
            try {
                MessageData messageData = dbHelper.getMessageData(
                        Integer.parseInt(bundle.getString(Constant.JSON_KEY_UPLOAD_SMS_ID)));
                if (messageData != null) {
                    messageData.mStrPrevDbStatus = messageData.mStrDbStatus;
                    messageData.mStrDbStatus = Constant.STATUS_FAILED;
                    messageData.m_lTimestamp = System.currentTimeMillis();
                    dbHelper.insertOrUpdateMessageData(messageData);
                }
                dbHelper.close();
                dbHelper = null;
                uploadSmsStatusToServer(context, messageData);
                if (isAppOnForeground((context))) {
                    Toast.makeText(context, "Message Failure to " +
                            bundle.getString(Constant.DB_FIELDNAME_SMS_PHONE), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (dbHelper != null) {
                    dbHelper.close();
                }
            }
        }
    }

    private boolean isAppOnForeground(Context context) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
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

//    public class RunnableUploadSmsStatusToServer implements Runnable {
//
//        private final Context mCtx;
//        private MessageData mMessageData;
//
//        public RunnableUploadSmsStatusToServer(Context ctx, MessageData messageData) {
//            mCtx = ctx;
//            mMessageData = messageData;
//        }
//
//        @SuppressLint("DefaultLocale")
//        @Override
//        public void run() {
//            DBHelper dbHelper = null;
//            try {
//                dbHelper = new DBHelper(mCtx);
//                ArrayList<MessageData> arrMessageData = dbHelper.getAllMessageDataForType("nums");
//                String token = dbHelper.getAppToken();
//
//                ArrayList<String> ids = new ArrayList<>();
//                JSONObject jsonSmsData = new JSONObject();
//                JSONArray jsonSmsArrayData = new JSONArray();
//                for (MessageData messageData : arrMessageData) {
//                    long time = messageData.m_lTime;
//                    if (time == 0) continue;
//                    long difftime = System.currentTimeMillis() - time;
//
//                    //Document doc = database.getDocument(dk.getString("id"));
//                    //Parsed into JSON in this format, considering your dev might be coming from a PHP background.
//                    if (
//                            (difftime / 1000) >= 9
//                                    &&
//                                    Constant.STATUS_STARTED.equals(messageData.mStrProcessing)
//                                    &&
//                                    !Constant.STATUS_INIT.equals(messageData.mStrDbStatus)
//                                    &&
//                                    !messageData.mStrPrevDbStatus.equals(messageData.mStrDbStatus)
//                    ) {
//                        String id = String.valueOf(messageData.m_nID);
//                        String phone = messageData.mStrPhone;
//                        long timestamp = messageData.m_lTimestamp;
//                        String dbstatus = messageData.mStrDbStatus;
//
//                        ids.add(id);
//
//                        JSONObject jsonItem = new JSONObject();
//                        jsonItem.put(Constant.JSON_KEY_UPLOAD_SMS_ID, id)
//                                .put(Constant.JSON_KEY_UPLOAD_SMS_PHONE, phone)
//                                .put(Constant.JSON_KEY_UPLOAD_SMS_UPDATE, timestamp)
//                                .put(Constant.JSON_KEY_UPLOAD_SMS_STATUS, dbstatus);
//                        jsonSmsArrayData.put(jsonItem);
//                    }
//                }
//
//                jsonSmsData.put(Constant.JSON_KEY_UPLOAD_SMS, jsonSmsArrayData);
//                if (ids.size() > 0) {
//                    ClientRequests clientRequest = new ClientRequests();
//                    if (isAppOnForeground(mCtx)) {
//                        Toast.makeText(mCtx, String.format("%d sms status uploading to the server!", ids.size()), Toast.LENGTH_SHORT).show();
//                    }
//                    try {
//                        String apiURL2 = Constant.SERVER_URL_UPLOAD;
//                        String data = clientRequest.post(apiURL2 + "?token=" + token +
//                                "&payload=" + URLEncoder.encode(jsonSmsData.toString(), StandardCharsets.UTF_8.name()), "");
//
//                        if (data.contains("\"status\": \"ok\"") || data.contains("\"status\":\"ok\""))
//                            for (String id : ids) {
//                                MessageData messageData = null;
//                                try {
//                                    messageData = dbHelper.getMessageData(Integer.parseInt(id));
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                if (messageData == null) continue;
//                                if (Constant.STATUS_RECEIVED.equals(messageData.mStrDbStatus)
//                                        || Constant.STATUS_FAILED.equals(messageData.mStrDbStatus)) {
//                                    messageData.mStrProcessing = Constant.STATUS_DONE;
//                                } else if (Constant.STATUS_SENT.equals(messageData.mStrDbStatus)) {
//                                    messageData.mStrPrevDbStatus = Constant.STATUS_SENT;
//                                }
//                                dbHelper.insertOrUpdateMessageData(messageData);
//                            }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            } catch (Exception e) {
//                if (dbHelper != null) {
//                    dbHelper.close();
//                    dbHelper = null;
//                }
//                e.printStackTrace();
//            }
//            if (dbHelper != null) dbHelper.close();
//        }
//    }

    public void uploadSmsStatusToServer(Context ctx, MessageData messageDAta) {
        DBHelper dbHelper = null;
        try {
            dbHelper = new DBHelper(ctx);
//                ArrayList<MessageData> arrMessageData = dbHelper.getAllMessageDataForType("nums");
            String token = dbHelper.getAppToken();

            ArrayList<String> ids = new ArrayList<>();
            JSONObject jsonSmsData = new JSONObject();
            JSONArray jsonSmsArrayData = new JSONArray();
//                for (MessageData messageData : arrMessageData) {
            long time = messageDAta.m_lTime;
            if (time == 0) return;
            long difftime = System.currentTimeMillis() - time;

            //Document doc = database.getDocument(dk.getString("id"));
            //Parsed into JSON in this format, considering your dev might be coming from a PHP background.
//                    if (
//                            (difftime / 1000) >= 9
//                                    &&
//                                    Constant.STATUS_STARTED.equals(messageData.mStrProcessing)
//                                    &&
//                                    !Constant.STATUS_INIT.equals(messageData.mStrDbStatus)
//                                    &&
//                                    !messageData.mStrPrevDbStatus.equals(messageData.mStrDbStatus)
//                    ) {
            String id = String.valueOf(messageDAta.m_nID);
            String phone = messageDAta.mStrPhone;
            long timestamp = messageDAta.m_lTimestamp;
            String dbstatus = messageDAta.mStrDbStatus;

            ids.add(id);

            JSONObject jsonItem = new JSONObject();
            jsonItem.put(Constant.JSON_KEY_UPLOAD_SMS_ID, id)
                    .put(Constant.JSON_KEY_UPLOAD_SMS_PHONE, phone)
                    .put(Constant.JSON_KEY_UPLOAD_SMS_UPDATE, timestamp)
                    .put(Constant.JSON_KEY_UPLOAD_SMS_STATUS, dbstatus);
            jsonSmsArrayData.put(jsonItem);
//                    }
//                }

            jsonSmsData.put(Constant.JSON_KEY_UPLOAD_SMS, jsonSmsArrayData);
            if (ids.size() > 0) {
                ClientRequests clientRequest = new ClientRequests();
                try {
                    String apiURL2 = Constant.SERVER_URL_UPLOAD;
                    String data = clientRequest.post(apiURL2 + "?token=" + token +
                            "&payload=" + URLEncoder.encode(jsonSmsData.toString(), StandardCharsets.UTF_8.name()), "");

                    if (data.contains("\"status\": \"ok\"") || data.contains("\"status\":\"ok\"")) {
                        for (String id1 : ids) {
                            MessageData messageData = null;
                            try {
                                messageData = dbHelper.getMessageData(Integer.parseInt(id1));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (messageData == null) continue;
                            if (Constant.STATUS_RECEIVED.equals(messageData.mStrDbStatus)
                                    || Constant.STATUS_FAILED.equals(messageData.mStrDbStatus)) {
                                messageData.mStrProcessing = Constant.STATUS_DONE;
                            } else if (Constant.STATUS_SENT.equals(messageData.mStrDbStatus)) {
                                messageData.mStrPrevDbStatus = Constant.STATUS_SENT;
                            }
                            dbHelper.insertOrUpdateMessageData(messageData);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            if (dbHelper != null) {
                dbHelper.close();
                dbHelper = null;
            }
            e.printStackTrace();
        }
        if (dbHelper != null) dbHelper.close();
    }

    public class RunnableUploadSmsStatusToServer implements Runnable {

        private Context mCtx;
        private MessageData mMessageData;

        public RunnableUploadSmsStatusToServer(Context ctx, MessageData messageData) {
            mCtx = ctx;
            mMessageData = messageData;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            DBHelper dbHelper = null;
            try {
                dbHelper = new DBHelper(mCtx);
//                ArrayList<MessageData> arrMessageData = dbHelper.getAllMessageDataForType("nums");
                String token = dbHelper.getAppToken();

                ArrayList<String> ids = new ArrayList<>();
                JSONObject jsonSmsData = new JSONObject();
                JSONArray jsonSmsArrayData = new JSONArray();
//                for (MessageData messageData : arrMessageData) {
                long time = mMessageData.m_lTime;
                if (time == 0) return;
                long difftime = System.currentTimeMillis() - time;

                //Document doc = database.getDocument(dk.getString("id"));
                //Parsed into JSON in this format, considering your dev might be coming from a PHP background.
//                    if (
//                            (difftime / 1000) >= 9
//                                    &&
//                                    Constant.STATUS_STARTED.equals(messageData.mStrProcessing)
//                                    &&
//                                    !Constant.STATUS_INIT.equals(messageData.mStrDbStatus)
//                                    &&
//                                    !messageData.mStrPrevDbStatus.equals(messageData.mStrDbStatus)
//                    ) {
                String id = String.valueOf(mMessageData.m_nID);
                String phone = mMessageData.mStrPhone;
                long timestamp = mMessageData.m_lTimestamp;
                String dbstatus = mMessageData.mStrDbStatus;

                ids.add(id);

                JSONObject jsonItem = new JSONObject();
                jsonItem.put(Constant.JSON_KEY_UPLOAD_SMS_ID, id)
                        .put(Constant.JSON_KEY_UPLOAD_SMS_PHONE, phone)
                        .put(Constant.JSON_KEY_UPLOAD_SMS_UPDATE, timestamp)
                        .put(Constant.JSON_KEY_UPLOAD_SMS_STATUS, dbstatus);
                jsonSmsArrayData.put(jsonItem);
//                    }
//                }

                jsonSmsData.put(Constant.JSON_KEY_UPLOAD_SMS, jsonSmsArrayData);
                if (ids.size() > 0) {
                    ClientRequests clientRequest = new ClientRequests();
                    try {
                        String apiURL2 = Constant.SERVER_URL_UPLOAD;
                        String data = clientRequest.post(apiURL2 + "?token=" + token +
                                "&payload=" + URLEncoder.encode(jsonSmsData.toString(), StandardCharsets.UTF_8.name()), "");

                        if (data.contains("\"status\": \"ok\"") || data.contains("\"status\":\"ok\""))
                            for (String id1 : ids) {
                                MessageData messageData = null;
                                try {
                                    messageData = dbHelper.getMessageData(Integer.parseInt(id1));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (messageData == null) continue;
                                if (Constant.STATUS_RECEIVED.equals(messageData.mStrDbStatus)
                                        || Constant.STATUS_FAILED.equals(messageData.mStrDbStatus)) {
                                    messageData.mStrProcessing = Constant.STATUS_DONE;
                                } else if (Constant.STATUS_SENT.equals(messageData.mStrDbStatus)) {
                                    messageData.mStrPrevDbStatus = Constant.STATUS_SENT;
                                }
                                dbHelper.insertOrUpdateMessageData(messageData);
                            }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                if (dbHelper != null) {
                    dbHelper.close();
                    dbHelper = null;
                }
                e.printStackTrace();
            }
            if (dbHelper != null) dbHelper.close();
        }
    }
}
