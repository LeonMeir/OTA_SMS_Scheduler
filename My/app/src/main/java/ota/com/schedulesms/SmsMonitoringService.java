package ota.com.schedulesms;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class SmsMonitoringService extends Service {

    private static final String ACTION_START = "YACB_ACTION_START";
    private static final String ACTION_STOP = "YACB_ACTION_STOP";

    private final SmsReceiver mReceiverSms = new SmsReceiver();

    private boolean monitoringStarted;

    public static void start(Context context) {
        ContextCompat.startForegroundService(context, getIntent(context, ACTION_START));
    }

    public static void stop(Context context) {
        context.stopService(getIntent(context, ACTION_STOP));
    }

    private static Intent getIntent(Context context, String action) {
        Intent intent = new Intent(context, SmsMonitoringService.class);
        intent.setAction(action);
        return intent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopMonitoring();
            stopForeground();
            stopSelf();
        } else {
            startForeground();
            startMonitoring();
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
        MainActivity.displayStatus("Stop Sms Status Monitoring", false);
        stopMonitoring();
    }

    private void startForeground() {
        startForeground(NotificationHelper.NOTIFICATION_ID_MONITORING_SERVICE_SMS,
                NotificationHelper.createSmsMonitoringServiceNotification(this));
    }

    private void stopForeground() {
        stopForeground(true);
    }

    private void startMonitoring() {
        if (monitoringStarted) return;
        monitoringStarted = true;

        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SmsReceiver.SMS_RECEIVED);
            registerReceiver(mReceiverSms, intentFilter);
        } catch (Exception e) {
        }
    }

    private void stopMonitoring() {
        if (!monitoringStarted) return;

        try {
            unregisterReceiver(mReceiverSms);
        } catch (Exception e) {
        }

        monitoringStarted = false;
    }
}
