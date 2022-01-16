package ota.com.schedulesms;

import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

public class PhoneUtils {


    public static boolean endCall(@NonNull Context context, boolean offHook) {

        if (offHook) {
            // According to docs, it should work with TelecomManager,
            // but it doesn't (the ongoing call is ended instead).
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                TelecomManager telecomManager = requireNonNull(
                        (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE));

                if (telecomManagerEndCall(telecomManager)) {
//                    LOG.info("endCall() ended call using TelecomManager");
                    MainActivity.displayStatus("Blocking Result of PhoneUtils: Block Success", false);
                } else {
                    MainActivity.displayStatus("Blocking Result of PhoneUtils: Block Failed", false);
//                    LOG.warn("endCall() TelecomManager returned false");
                }

                return true;
            } catch (Exception e) {
                MainActivity.displayStatus(String.format("Blocking of PhoneUtils:Error:%s",
                        e.getMessage()), false);
            }
        } else {
            try {
                TelephonyManager tm = requireNonNull(
                        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

                Method m = tm.getClass().getDeclaredMethod("getITelephony");
                m.setAccessible(true);
                ITelephony telephony = requireNonNull((ITelephony) m.invoke(tm));

                if (telephony.endCall()) {
                    MainActivity.displayStatus("Blocking Result of PhoneUtils: Block Success", false);
                } else {
                    MainActivity.displayStatus("Blocking Result of PhoneUtils: Block Failed", false);
                }
                return true;
            } catch (Exception e) {
                MainActivity.displayStatus(String.format("Blocking of PhoneUtils:Error:%s",
                        e.getMessage()), false);
            }
        }

        return false;
    }

    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // no choice
    @SuppressLint("MissingPermission") // maybe shouldn't
    @RequiresApi(Build.VERSION_CODES.P)
    private static boolean telecomManagerEndCall(TelecomManager telecomManager) {
        return telecomManager.endCall();
    }
}
