package ota.com.schedulesms;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

public class CallReceiver extends PhonecallReceiver {
    private static final int REQ_CODE_READ_PHONE_STATE = 5;
    private Handler mHandler = new Handler();
    @Override
    protected void onIncomingCallStarted(final Context ctx, final String number, Date start) {
//        Toast.makeText(ctx, "Incoming call:" + number, Toast.LENGTH_LONG).show();
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                Toast.makeText(ctx, "Incoming call:" + number, Toast.LENGTH_LONG).show();
            }
        });
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(Constant.SH_KEY_SIM_REJECT_CALL_STATUS, false)) {
            rejectCall(ctx);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager localSubscriptionManager = SubscriptionManager.from(ctx.getApplicationContext());
                if (Constant.m_bReadPhoneState) {
                    if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    if (localSubscriptionManager != null &&
                            localSubscriptionManager.getActiveSubscriptionInfoCount() >= 1) {
                        List<SubscriptionInfo> localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                        if (localList != null && localList.size() > 0) {
                            SubscriptionInfo simInfo1 = localList.get(0);

                            SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(
                                    number,
                                    null,
                                    sharedPreferences.getString(Constant.SH_KEY_SIM_REJECT_CALL_SMS, ""),
                                    null, null);
                        }
                    }
                }
            } else {
                SmsManager.getDefault().sendTextMessage(number,
                        null,
                        sharedPreferences.getString(Constant.SH_KEY_SIM_REJECT_CALL_SMS, ""),
                        null,
                        null);

            }
        }
    }

    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start, int nReceiverSimNo) {
//        Toast.makeText(ctx, "Incoming call:" + number + "--"+ nReceiverSimNo, Toast.LENGTH_LONG).show();
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                Toast.makeText(ctx, "Incoming call:" + number, Toast.LENGTH_LONG).show();
            }
        });

        if (nReceiverSimNo == -1) return;
        if (nReceiverSimNo == 0) {
//            try {
            SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
            if (sharedPreferences.getBoolean(Constant.SH_KEY_SIM1_REJECT_CALL_STATUS, false)) {
                rejectCall(ctx);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    SubscriptionManager localSubscriptionManager = SubscriptionManager.from(ctx);
                    if (Constant.m_bReadPhoneState) {
                        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
                                != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        if (localSubscriptionManager != null &&
                                localSubscriptionManager.getActiveSubscriptionInfoCount() >= 1) {
                            List<SubscriptionInfo> localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                            if (localList != null && localList.size() > nReceiverSimNo) {
                                SubscriptionInfo simInfo1 = localList.get(nReceiverSimNo);

                                SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(number,
                                        null,
                                        sharedPreferences.getString(Constant.SH_KEY_SIM1_REJECT_CALL_SMS, ""),
                                        null, null);
                            }
                        }
                    }
                }
            }
//            }catch (Exception e){
//                e.printStackTrace();
//            }
        } else if (nReceiverSimNo == 1) {
//            try {
            SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
            if (sharedPreferences.getBoolean(Constant.SH_KEY_SIM2_REJECT_CALL_STATUS, false)) {
                rejectCall(ctx);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    SubscriptionManager localSubscriptionManager = SubscriptionManager.from(ctx);
                    if (Constant.m_bReadPhoneState) {
                        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
                                != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        if (localSubscriptionManager != null &&
                                localSubscriptionManager.getActiveSubscriptionInfoCount() >= 1) {
                            List<SubscriptionInfo> localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                            if (localList != null && localList.size() > nReceiverSimNo) {
                                SubscriptionInfo simInfo1 = localList.get(nReceiverSimNo);

                                SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(number,
                                        null,
                                        sharedPreferences.getString(Constant.SH_KEY_SIM2_REJECT_CALL_SMS, ""),
                                        null, null);
                            }
                        }
                    }
                }
            }
//            }catch (Exception e){
//                e.printStackTrace();
//            }
        }
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
    }

    public boolean rejectCall(Context context) {
        try {
            // Get the boring old TelephonyManager
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // Get the getITelephony() method
            Class classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");

            // Ignore that the method is supposed to be private
            methodGetITelephony.setAccessible(true);

            // Invoke getITelephony() to get the ITelephony interface
            Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);

            // Get the endCall method from ITelephony
            Class telephonyInterfaceClass =
                    Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");

            // Invoke endCall()
            methodEndCall.invoke(telephonyInterface);
//            TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
//            Class clazz = Class.forName(telephonyManager.getClass().getName());
//            Method method = clazz.getDeclaredMethod("getITelephony");
//            method.setAccessible(true);
//            ITelephony telephonyService = (ITelephony) method.invoke(telephonyManager);
//            telephonyService.endCall();
        } catch (Exception ex) { // Many things can go wrong with reflection calls
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}