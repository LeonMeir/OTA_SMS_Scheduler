package ota.com.schedulesms;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class SmsReceiver extends BroadcastReceiver {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static int m_iSimIndex = 0;
    public ArrayList<Integer> mArrSimNumbersInUse = new ArrayList<>();
    private final Handler mHandler = new Handler();

    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            MainActivity.displayStatus(String.format("SMS Received-Action:%s", intent.getAction()), false);
            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(),
                    Context.MODE_PRIVATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (sharedPreferences.getBoolean(Constant.SH_KEY_SIM_AUTO_REPLY_SMS_STATUS, false)) {
                    if (SMS_RECEIVED.equalsIgnoreCase(intent.getAction())) {
                        Bundle bundle = intent.getExtras();
                        if (bundle != null) {
                            SmsMessage[] smsss = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                            //                        Object[] myPDU = (Object[]) bundle.get("pdus");
                            //                        final SmsMessage[] smss = new SmsMessage[myPDU.length];
                            SubscriptionManager localSubscriptionManager = SubscriptionManager.from(context);
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                                MainActivity.displayStatus("SMS Received-Read Phone State Denied", false);
                                return;
                            }
                            if (localSubscriptionManager != null &&
                                    localSubscriptionManager.getActiveSubscriptionInfoCount() >= 1) {
                                List<SubscriptionInfo> localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                                int nSimCnt = getSimCount(context);
                                MainActivity.displayStatus("SMS Received-Sim count:" + nSimCnt, false);
                                if (nSimCnt == 1) {
                                    MainActivity.displayStatus("Sms received in the case with one sim of dual", false);

                                    SmsManager sm = SmsManager.getDefault();
                                    String strReplySms = sharedPreferences.getString(Constant.SH_KEY_SIM_AUTO_REPLY_SMS, Constant.DEFAULT_AUTOMSG_REPLY_SMS);
                                    for (int i = 0; i < smsss.length; i++) {
                                        String strPhoneNo = smsss[i].getOriginatingAddress();
                                        MainActivity.displayStatus("Auto Reply to " + strPhoneNo, false);
                                        sm.sendTextMessage(strPhoneNo,
                                                null,
                                                strReplySms,
                                                null,
                                                null);
                                    }
                                    //                                if (localList != null && localList.size() > 0) {
                                    //                                    String strReplySms = sharedPreferences.getString(Constant.SH_KEY_SIM_AUTO_REPLY_SMS, "");
                                    //
                                    //                                    SubscriptionInfo simInfo1 = localList.get(0);
                                    //
                                    //                                    for (int i = 0; i < smsss.length; i++) {
                                    //                                        String strPhoneNo = smsss[i].getOriginatingAddress();
                                    //                                        SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).
                                    //                                                sendTextMessage(strPhoneNo,
                                    //                                                        null, strReplySms,
                                    //                                                        null, null);
                                    //                                    }
                                    //                                }
                                } else if (nSimCnt == 2) {
                                    MainActivity.displayStatus("Sms received in the case with dual sim of dual", false);
                                    if (localList != null && localList.size() > m_iSimIndex && m_iSimIndex != -1) {
                                        String strReplySms = "";
                                        if (m_iSimIndex == 0) {
                                            strReplySms = sharedPreferences.getString(Constant.SH_KEY_SIM1_AUTO_REPLY_SMS, Constant.DEFAULT_AUTOMSG_REPLY_SMS);
                                        } else {
                                            strReplySms = sharedPreferences.getString(Constant.SH_KEY_SIM2_AUTO_REPLY_SMS, Constant.DEFAULT_AUTOMSG_REPLY_SMS);
                                        }
                                        SubscriptionInfo simInfo1 = localList.get(m_iSimIndex);

                                        for (int i = 0; i < smsss.length; i++) {
                                            //                                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                                            //                                        String strFormat = bundle.getString("format");
                                            //                                        smss[i] = SmsMessage.createFromPdu((byte[]) myPDU[i], strFormat);
                                            //                                    } else {
                                            //                                        smss[i] = SmsMessage.createFromPdu((byte[]) myPDU[i]);
                                            //                                    }
                                            String strPhoneNo = smsss[i].getOriginatingAddress();
                                            MainActivity.displayStatus("Auto Reply to " + strPhoneNo, false);

                                            SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).
                                                    sendTextMessage(strPhoneNo,
                                                            null, strReplySms,
                                                            null, null);
                                        }
                                        m_iSimIndex++;
                                        getCurrentUsingSims(context);
                                        if (mArrSimNumbersInUse.size() > 0) {
                                            m_iSimIndex = m_iSimIndex % mArrSimNumbersInUse.size();
                                        } else {
                                            m_iSimIndex = 0;
                                        }
                                    }
                                }
                            } else {
                                MainActivity.displayStatus("SMS Received-Subscription is null or size 0", false);
                            }
                        }
                    }
                }

            } else {
                MainActivity.displayStatus("Sms received in the case with one sim", false);

                if (sharedPreferences.getBoolean(Constant.SH_KEY_SIM_AUTO_REPLY_SMS_STATUS, false)) {
                    if (SMS_RECEIVED.equalsIgnoreCase(intent.getAction())) {
                        Bundle bundle = intent.getExtras();
                        if (bundle != null) {
                            SmsMessage[] smsss = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                            SmsManager sm = SmsManager.getDefault();
                            String strReplySms = sharedPreferences.getString(Constant.SH_KEY_SIM_AUTO_REPLY_SMS, Constant.DEFAULT_AUTOMSG_REPLY_SMS);
                            for (int i = 0; i < smsss.length; i++) {
                                String strPhoneNo = smsss[i].getOriginatingAddress();
                                MainActivity.displayStatus("Auto Reply to " + strPhoneNo, false);
                                sm.sendTextMessage(strPhoneNo,
                                        null,
                                        strReplySms,
                                        null,
                                        null);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            MainActivity.displayStatus(e.getMessage(), false);
        }
    }

    public void getCurrentUsingSims(Context ctx) {
        mArrSimNumbersInUse = new ArrayList<>();
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(Constant.SH_KEY_SIM1_AUTO_REPLY_SMS_STATUS, false)) {
            mArrSimNumbersInUse.add(0);
        }
        if (sharedPreferences.getBoolean(Constant.SH_KEY_SIM2_AUTO_REPLY_SMS_STATUS, false)) {
            mArrSimNumbersInUse.add(1);
        }
    }

    public int getSimCount(Context ctx) {
//        return 1;
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        int i = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = (SubscriptionManager) ctx.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
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