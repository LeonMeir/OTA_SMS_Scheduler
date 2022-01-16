package ota.com.schedulesms;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
        formKey = "",
        mailTo = "leonmeier9097@gmail.com")

public class ScheduleSMSApplication extends Application {

    public static StartupReceiver mStartupReceiver;

    @Override
    public void onCreate(){
        super.onCreate();
        ACRA.init(this);
        NotificationService notificationService = new NotificationService(this);
        YacbHolder.setNotificationService(notificationService);
        YacbHolder.setPhoneStateHandler(
                new PhoneStateHandler(this, notificationService));

        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(Constant.SH_KEY_AUTO_REJECT_ALL_CALLS_SIM, false)) {
            CallMonitoringService.start(this);
        }
        mStartupReceiver = new StartupReceiver();
    }
}