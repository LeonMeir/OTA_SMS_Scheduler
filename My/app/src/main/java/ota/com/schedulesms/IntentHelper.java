package ota.com.schedulesms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

public class IntentHelper {


    public static Uri getUriForPhoneNumber(String number) {
        return Uri.parse("tel:" + (!TextUtils.isEmpty(number) ? number : "private"));
    }

    public static PendingIntent pendingActivity(Context context, Intent intent) {
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(context, 0, intent, flags);
    }

    public static Intent clearTop(Intent intent) {
        return intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    public static boolean startActivity(Context context, Intent intent) {
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

}
