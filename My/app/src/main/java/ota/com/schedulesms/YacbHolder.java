package ota.com.schedulesms;

import android.annotation.SuppressLint;


public class YacbHolder {
    @SuppressLint("StaticFieldLeak")
    private static NotificationService notificationService;

    @SuppressLint("StaticFieldLeak")
    private static PhoneStateHandler phoneStateHandler;

    static void setNotificationService(NotificationService notificationService) {
        YacbHolder.notificationService = notificationService;
    }

    static void setPhoneStateHandler(PhoneStateHandler phoneStateHandler) {
        YacbHolder.phoneStateHandler = phoneStateHandler;
    }

    public static NotificationService getNotificationService() {
        return notificationService;
    }

    public static PhoneStateHandler getPhoneStateHandler() {
        return phoneStateHandler;
    }


}
