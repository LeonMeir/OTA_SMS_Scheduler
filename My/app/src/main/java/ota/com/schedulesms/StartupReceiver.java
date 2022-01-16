package ota.com.schedulesms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class StartupReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && "android.intent.action.BOOT_COMPLETED".equalsIgnoreCase(intent.getAction())) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            if (sharedPreferences.getBoolean(Constant.SH_KEY_AUTO_START_WITH_PHONE, true) &&
                    sharedPreferences.getBoolean(Constant.SH_KEY_SERVICE_STATUS, false)) {
                Intent serviceintent = new Intent(context, MessagingService.class);
                context.startService(serviceintent);
            }
        }
    }

}
