package ota.com.schedulesms;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.Connection;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.N)
public class CallScreeningServiceImpl extends CallScreeningService {

    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {
        boolean shouldBlock = false;

        try {
            boolean ignore = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (callDetails.getCallDirection() != Call.Details.DIRECTION_INCOMING) {
                    ignore = true;
                }
            }
            SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            boolean bIsBlockingEnabled = sharedPreferences.getBoolean(Constant.SH_KEY_AUTO_REJECT_ALL_CALLS_SIM, false);
            if (!ignore && !bIsBlockingEnabled) {
                ignore = true;
            }

            String number = null;

            if (!ignore) {
                Uri handle = callDetails.getHandle();

                if (handle != null && PhoneAccount.SCHEME_TEL.equals(handle.getScheme())) {
                    number = handle.getSchemeSpecificPart();
                }

                if (number == null) {
                    Bundle intentExtras = callDetails.getIntentExtras();
                    if (intentExtras != null) {
                        Object o = intentExtras.get(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS);

                        if (o instanceof Uri) {
                            Uri uri = (Uri) o;
                            if (PhoneAccount.SCHEME_TEL.equals(uri.getScheme())) {
                                number = uri.getSchemeSpecificPart();
                            }
                        }

                        if (number == null && intentExtras.containsKey(
                                "com.google.android.apps.hangouts.telephony.hangout_info_bundle")) {
                            // NB: SIA doesn't block (based on number) hangouts if there's no number in intentExtras
                            number = "YACB_hangouts_stub";
                        }
                    }

                    if (number == null && callDetails.getExtras() != null) {
                        // NB: this part is broken in SIA
                        number = callDetails.getExtras().getString(Connection.EXTRA_CHILD_ADDRESS);
                    }
                }

                if (TextUtils.isEmpty(number)) {
                    ignore = true;
                }
            }

            if (!ignore) {
                shouldBlock = true;
            }
        } finally {

            CallResponse.Builder responseBuilder = new CallResponse.Builder();

            if (shouldBlock) {
                responseBuilder
                        .setDisallowCall(true)
                        .setRejectCall(true)
                        .setSkipNotification(true);
            }

            boolean blocked = shouldBlock;
            try {
                respondToCall(callDetails, responseBuilder.build());
            } catch (Exception e) {
                blocked = false;
            }

//            if (blocked) {
//
//                NotificationHelper.showBlockedCallNotification(this, numberInfo);
//
//                numberInfoService.blockedCall(numberInfo);
//
//                postEvent(new CallEndedEvent());
//            }
        }

    }

}
