package ota.com.schedulesms;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import androidx.core.app.ActivityCompat;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class MultiSimListener extends PhoneStateListener {

    private Field subscriptionField;
    private int simSlot = -1;
    private Context mCtx;

    public MultiSimListener (int simSlot) {
        super();
        try {
            // Get the protected field mSubscription of PhoneStateListener and set it
            subscriptionField = Objects.requireNonNull(this.getClass().getSuperclass()).getDeclaredField("mSubscription");
            subscriptionField.setAccessible(true);
            subscriptionField.set(this, simSlot);
            this.simSlot = simSlot;
        } catch (NoSuchFieldException e) {

        } catch (IllegalAccessException e) {

        } catch (IllegalArgumentException e) {

        }
    }

    public MultiSimListener (Context ctx, int simSlot) {
        super();
        mCtx = ctx;
        try {
            // Get the protected field mSubscription of PhoneStateListener and set it

            subscriptionField = Objects.requireNonNull(this.getClass().getSuperclass()).getDeclaredField("mSubscription");
            subscriptionField.setAccessible(true);
            subscriptionField.set(this, simSlot);
            this.simSlot = simSlot;

        } catch (NoSuchFieldException e) {

        } catch (IllegalAccessException e) {

        } catch (IllegalArgumentException e) {

        }
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(mCtx.getPackageName(), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(Constant.SH_KEY_SIM2_REJECT_CALL_STATUS, false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager localSubscriptionManager = SubscriptionManager.from(mCtx);
                if (Constant.m_bReadPhoneState) {
                    if (ActivityCompat.checkSelfPermission(mCtx, Manifest.permission.READ_PHONE_STATE)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    if (localSubscriptionManager != null &&
                            localSubscriptionManager.getActiveSubscriptionInfoCount() >= 1) {
                        List<SubscriptionInfo> localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                        if (localList != null && localList.size() > simSlot) {
                            SubscriptionInfo simInfo1 = localList.get(simSlot);
                            String strKey = Constant.SH_KEY_SIM1_REJECT_CALL_SMS;
                            if (simSlot == 1){
                                strKey = Constant.SH_KEY_SIM2_REJECT_CALL_SMS;
                            }
                            SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(incomingNumber,
                                    null,
                                    sharedPreferences.getString(strKey, ""),
                                    null, null);
                        }
                    }
                }
            }else{
                SmsManager.getDefault().sendTextMessage(incomingNumber,
                        null,
                        sharedPreferences.getString(Constant.SH_KEY_SIM_REJECT_CALL_SMS, ""),
                        null,
                        null);
            }
        }
    }
}