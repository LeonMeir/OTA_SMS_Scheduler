package ota.com.schedulesms;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.util.Predicate;


import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
public class PhoneStateHandler {

    public enum Source {
        PHONE_STATE_LISTENER,
        PHONE_STATE_BROADCAST_RECEIVER_MONITORING,
        PHONE_STATE_BROADCAST_RECEIVER
    }

    private final Context context;
    private final NotificationService notificationService;

    private boolean isOffHook;

    private List<CallEvent> lastEvents = new ArrayList<>();
    private long lastEventTimestamp;

    public PhoneStateHandler(Context context,
                             NotificationService notificationService) {
        this.context = context;
        this.notificationService = notificationService;
    }

    public void onRinging(Source source, String phoneNumber) {

        boolean ignore = false;

        if (phoneNumber == null) {

            if (source == Source.PHONE_STATE_LISTENER) {
                phoneNumber = "";
            } else if ((source == Source.PHONE_STATE_BROADCAST_RECEIVER_MONITORING
                    || source == Source.PHONE_STATE_BROADCAST_RECEIVER)
                    && isEventPresent(sameSourceAndNumber(source, null))
                    && !isEventPresent(nonEmptyNumber())) {
                phoneNumber = "";
            }

            if (phoneNumber == null) {
                ignore = true;
            }
        }

        if (!ignore && isEventPresent(sameNumber(phoneNumber))) {
            ignore = true;
        }

        MainActivity.displayStatus(String.format("onRinging of PhoneStateHandler:Ignore Value:%s",
                ignore ? "True" : "False"), false);

        recordEvent(source, phoneNumber);

        if (ignore) return;

//        boolean blockingEnabled = settings.getCallBlockingEnabled();
//        boolean showNotifications = settings.getIncomingCallNotifications();
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        boolean blockingEnabled = sharedPreferences.getBoolean(Constant.SH_KEY_AUTO_REJECT_ALL_CALLS_SIM, false);
        MainActivity.displayStatus(String.format("Blocking Enabled of PhoneStateHandler:Status:%s",
                blockingEnabled ? "Enabled" : "Disabled"), false);
//        boolean showNotifications = settings.getIncomingCallNotifications();

        if (!blockingEnabled) {
            return;
        }

        boolean blocked = false;
        MainActivity.displayStatus(String.format("Blocking Result of PhoneStateHandler:isOffHook:%s",
                isOffHook ? "True" : "False"), false);
        blocked = PhoneUtils.endCall(context, isOffHook);
        MainActivity.displayStatus(String.format("Blocking Result of PhoneStateHandler:Status:%s",
                blocked ? "Blocked" : "Passed"), false);
        if (sharedPreferences.getBoolean(Constant.SH_KEY_SIM_REJECT_CALL_STATUS, false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager localSubscriptionManager = SubscriptionManager.from(context.getApplicationContext());
                if (Constant.m_bReadPhoneState) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    if (localSubscriptionManager != null &&
                            localSubscriptionManager.getActiveSubscriptionInfoCount() >= 1) {
                        List<SubscriptionInfo> localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                        if (localList != null && localList.size() > 0) {
                            SubscriptionInfo simInfo1 = localList.get(0);

                            SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(
                                    phoneNumber,
                                    null,
                                    sharedPreferences.getString(Constant.SH_KEY_SIM_REJECT_CALL_SMS,
                                            Constant.DEFAULT_AUTOMSG_REJECT_CALL),
                                    null, null);
                        }
                    }
                }
            } else {
                SmsManager.getDefault().sendTextMessage(phoneNumber,
                        null,
                        sharedPreferences.getString(Constant.SH_KEY_SIM_REJECT_CALL_SMS,
                                Constant.DEFAULT_AUTOMSG_REJECT_CALL),
                        null,
                        null);
            }
        }
    }

    public void onRinging(Source source, String phoneNumber, int nSubId) {

        boolean ignore = false;

        if (phoneNumber == null) {

            if (source == Source.PHONE_STATE_LISTENER) {
                phoneNumber = "";
            } else if ((source == Source.PHONE_STATE_BROADCAST_RECEIVER_MONITORING
                    || source == Source.PHONE_STATE_BROADCAST_RECEIVER)
                    && isEventPresent(sameSourceAndNumber(source, null))
                    && !isEventPresent(nonEmptyNumber())) {
                phoneNumber = "";
            }

            if (phoneNumber == null) {
                ignore = true;
            }
        }

        if (!ignore && isEventPresent(sameNumber(phoneNumber))) {
            ignore = true;
        }

        MainActivity.displayStatus(String.format("onRinging of PhoneStateHandler:Ignore Value:%s",
                ignore ? "True" : "False"), false);

        recordEvent(source, phoneNumber);

        if (ignore) return;

//        boolean blockingEnabled = settings.getCallBlockingEnabled();
//        boolean showNotifications = settings.getIncomingCallNotifications();
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        boolean blockingEnabled = false;
        if (nSubId == 0){//For Single Sim
            blockingEnabled = sharedPreferences.getBoolean(Constant.SH_KEY_AUTO_REJECT_ALL_CALLS_SIM, false);
        }else if(nSubId == 1){
            blockingEnabled = sharedPreferences.getBoolean(Constant.SH_KEY_AUTO_REJECT_ALL_CALLS_SIM1, false);
        }else if(nSubId == 2){
            blockingEnabled = sharedPreferences.getBoolean(Constant.SH_KEY_AUTO_REJECT_ALL_CALLS_SIM2, false);
        }
        MainActivity.displayStatus(String.format("Blocking Enabled of PhoneStateHandler:Status:%s",
                blockingEnabled ? "Enabled" : "Disabled"), false);

        if (!blockingEnabled) {
            return;
        }

        boolean blocked = false;
        MainActivity.displayStatus(String.format("Blocking Result of PhoneStateHandler:isOffHook:%s",
                isOffHook ? "True" : "False"), false);
        blocked = PhoneUtils.endCall(context, isOffHook);
        MainActivity.displayStatus(String.format("Blocking Result of PhoneStateHandler:Status:%s",
                blocked ? "Blocked" : "Passed"), false);

        if (nSubId == 0){
            if (sharedPreferences.getBoolean(Constant.SH_KEY_SIM_REJECT_CALL_STATUS, false)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    SubscriptionManager localSubscriptionManager = SubscriptionManager.from(context.getApplicationContext());
                    if (Constant.m_bReadPhoneState) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        if (localSubscriptionManager != null &&
                                localSubscriptionManager.getActiveSubscriptionInfoCount() >= 1) {
                            List<SubscriptionInfo> localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                            if (localList != null && localList.size() > 0) {
                                SubscriptionInfo simInfo1 = localList.get(0);

                                SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(
                                        phoneNumber,
                                        null,
                                        sharedPreferences.getString(Constant.SH_KEY_SIM_REJECT_CALL_SMS,
                                                Constant.DEFAULT_AUTOMSG_REJECT_CALL),
                                        null, null);
                            }
                        }
                    }
                } else {
                    SmsManager.getDefault().sendTextMessage(phoneNumber,
                            null,
                            sharedPreferences.getString(Constant.SH_KEY_SIM_REJECT_CALL_SMS,
                                    Constant.DEFAULT_AUTOMSG_REJECT_CALL),
                            null,
                            null);
                }
            }
        }else if (nSubId == 1){
            if (sharedPreferences.getBoolean(Constant.SH_KEY_SIM1_REJECT_CALL_STATUS, false)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    SubscriptionManager localSubscriptionManager = SubscriptionManager.from(context.getApplicationContext());
                    if (Constant.m_bReadPhoneState) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        if (localSubscriptionManager != null &&
                                localSubscriptionManager.getActiveSubscriptionInfoCount() >= 1) {
                            List<SubscriptionInfo> localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                            if (localList != null && localList.size() > 0) {
                                SubscriptionInfo simInfo1 = localList.get(0);

                                SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(
                                        phoneNumber,
                                        null,
                                        sharedPreferences.getString(Constant.SH_KEY_SIM1_REJECT_CALL_SMS,
                                                Constant.DEFAULT_AUTOMSG_REJECT_CALL),
                                        null, null);
                            }
                        }
                    }
                } else {
                    SmsManager.getDefault().sendTextMessage(phoneNumber,
                            null,
                            sharedPreferences.getString(Constant.SH_KEY_SIM1_REJECT_CALL_SMS,
                                    Constant.DEFAULT_AUTOMSG_REJECT_CALL),
                            null,
                            null);
                }
            }
        }else if (nSubId == 2){
            if (sharedPreferences.getBoolean(Constant.SH_KEY_SIM2_REJECT_CALL_STATUS, false)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    SubscriptionManager localSubscriptionManager = SubscriptionManager.from(context.getApplicationContext());
                    if (Constant.m_bReadPhoneState) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        if (localSubscriptionManager != null){
                            if (localSubscriptionManager.getActiveSubscriptionInfoCount() >= 2) {
                                List<SubscriptionInfo> localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                                if (localList != null && localList.size() > 1) {
                                    SubscriptionInfo simInfo1 = localList.get(1);

                                    SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(
                                            phoneNumber,
                                            null,
                                            sharedPreferences.getString(Constant.SH_KEY_SIM2_REJECT_CALL_SMS,
                                                    Constant.DEFAULT_AUTOMSG_REJECT_CALL),
                                            null, null);
                                }else if (localList != null && localList.size() > 0) {
                                    SubscriptionInfo simInfo1 = localList.get(0);
                                    SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(
                                            phoneNumber,
                                            null,
                                            sharedPreferences.getString(Constant.SH_KEY_SIM2_REJECT_CALL_SMS,
                                                    Constant.DEFAULT_AUTOMSG_REJECT_CALL),
                                            null, null);
                                }
                            }else if(localSubscriptionManager.getActiveSubscriptionInfoCount() >= 1){
                                List<SubscriptionInfo> localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                                if (localList != null && localList.size() > 0) {
                                    SubscriptionInfo simInfo1 = localList.get(0);

                                    SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(
                                            phoneNumber,
                                            null,
                                            sharedPreferences.getString(Constant.SH_KEY_SIM2_REJECT_CALL_SMS,
                                                    Constant.DEFAULT_AUTOMSG_REJECT_CALL),
                                            null, null);
                                }
                            }
                        }
                    }
                } else {
                    SmsManager.getDefault().sendTextMessage(phoneNumber,
                            null,
                            sharedPreferences.getString(Constant.SH_KEY_SIM2_REJECT_CALL_SMS,
                                    Constant.DEFAULT_AUTOMSG_REJECT_CALL),
                            null,
                            null);
                }
            }
        }
    }

    public void onOffHook(Source source, String phoneNumber) {

        isOffHook = true;
    }

    public void onIdle(Source source, String phoneNumber) {


        isOffHook = false;

        notificationService.stopAllCallsIndication();

//        postEvent(new CallEndedEvent());
    }

    private static Predicate<CallEvent> sameNumber(String number) {
        return event -> TextUtils.equals(event.number, number);
    }

    private static Predicate<CallEvent> sameSourceAndNumber(Source source, String number) {
        return event -> event.source == source && TextUtils.equals(event.number, number);
    }

    private static Predicate<CallEvent> nonEmptyNumber() {
        return event -> !TextUtils.isEmpty(event.number);
    }

    private boolean isEventPresent(Predicate<CallEvent> predicate) {
        return findEvent(predicate) != null;
    }

    private CallEvent findEvent(Predicate<CallEvent> predicate) {
        // using 1 second ago as the cutoff point - consider everything older as unrelated events
        long cutoff = System.nanoTime() - TimeUnit.SECONDS.toNanos(1);

        if (lastEventTimestamp - cutoff < 0) { // no events in the last second
            lastEvents.clear();
            return null;
        }

        for (ListIterator<CallEvent> it = lastEvents.listIterator(); it.hasNext(); ) {
            CallEvent event = it.next();
            if (event.timestamp - cutoff < 0) { // event is older than the cutoff point
                it.remove();
            } else if (predicate.test(event)) {
                return event;
            }
        }

        return null;
    }

    private void recordEvent(Source source, String phoneNumber) {
        long currentTimestamp = System.nanoTime();
        lastEvents.add(new CallEvent(source, phoneNumber, currentTimestamp));
        lastEventTimestamp = currentTimestamp;
    }

    private static class CallEvent {
        final Source source;
        final String number;
        final long timestamp;

        public CallEvent(Source source, String number, long timestamp) {
            this.source = source;
            this.number = number;
            this.timestamp = timestamp;
        }
    }

}
