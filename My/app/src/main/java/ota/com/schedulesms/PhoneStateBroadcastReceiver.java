package ota.com.schedulesms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;


public class PhoneStateBroadcastReceiver extends BroadcastReceiver {

    private final PhoneStateHandler.Source source;
    private int m_nStatusForIds;

    public void enableForSubId(int nID){
        m_nStatusForIds = m_nStatusForIds | (0x01 << nID);
    }

    public void disableForSubId(int nID){
        if (nID == 0){
            m_nStatusForIds = m_nStatusForIds & 0x0e;
        }else if(nID == 1){
            m_nStatusForIds = m_nStatusForIds & 0x0d;
        }else if(nID == 2){
            m_nStatusForIds = m_nStatusForIds & 0x0b;
        }
    }

    public boolean getStatusForId(int nSubId){
        return !((m_nStatusForIds & (0x01 << nSubId)) == 0);
    }

    public int getStatusForIds(){
        return m_nStatusForIds;
    }

    @SuppressWarnings({"unused", "RedundantSuppression"}) // required for BroadcastReceivers
    public PhoneStateBroadcastReceiver() {
        this(PhoneStateHandler.Source.PHONE_STATE_BROADCAST_RECEIVER);
        m_nStatusForIds = 0;
    }

    public PhoneStateBroadcastReceiver(PhoneStateHandler.Source source) {
        this.source = source;
        m_nStatusForIds = 0;
    }

    public PhoneStateBroadcastReceiver(PhoneStateHandler.Source source, int nSubId) {
        this.source = source;
        m_nStatusForIds = nSubId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())
                && !TelephonyManager.EXTRA_STATE_RINGING.equals(intent.getAction())) { // TODO: check
            return;
        }

        @SuppressWarnings({"deprecation", "RedundantSuppression"}) // no choice
        String extraIncomingNumber = TelephonyManager.EXTRA_INCOMING_NUMBER;

        String telephonyExtraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String incomingNumber = intent.getStringExtra(extraIncomingNumber);
        boolean hasNumberExtra = intent.hasExtra(extraIncomingNumber);

        extraLogging(intent); // TODO: make optional or remove

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(intent.getAction())) {
            return;
        }

        if (incomingNumber == null && hasNumberExtra) {
            incomingNumber = "";
        }

        PhoneStateHandler phoneStateHandler = YacbHolder.getPhoneStateHandler();
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(telephonyExtraState)) {
            if (getStatusForId(0)) {
                phoneStateHandler.onRinging(source, incomingNumber, 0);
            }
            if (getStatusForId(1)) {
                phoneStateHandler.onRinging(source, incomingNumber, 1);
            }
            if (getStatusForId(2)) {
                phoneStateHandler.onRinging(source, incomingNumber, 2);
            }

        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(telephonyExtraState)) {
            phoneStateHandler.onOffHook(source, incomingNumber);
        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(telephonyExtraState)) {
            phoneStateHandler.onIdle(source, incomingNumber);
        }
    }

    private void extraLogging(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Object subscription = extras.get("subscription"); // PhoneConstants.SUBSCRIPTION_KEY
            if (subscription != null) {
                if (subscription instanceof Number) {
                    long subId = ((Number) subscription).longValue();
//                    LOG.trace("extraLogging() subId={}, check={}",
//                            subId, subId < Integer.MAX_VALUE);
                }
            }
        }
    }

}
