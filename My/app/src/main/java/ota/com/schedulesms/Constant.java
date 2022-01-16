package ota.com.schedulesms;

public class Constant {
    public static boolean m_bReadPhoneState = false;

    //URLS
    public static final String SERVER_URL_UPLOAD = "http://fxstudio.no-ip.org/sms-test/update.php";
    public static final String SERVER_URL_LOAD   = "http://fxstudio.no-ip.org/sms-test/load.php";
    public static final String SERVER_URL_LOAD1  = "http://fxstudio.no-ip.org/sms-test/load2.php";
    public static final String SERVER_URL_LOAD2  = "http://fxstudio.no-ip.org/sms-test/load3.php";
    public static final String SERVER_URL_ERROR  = "http://fxstudio.no-ip.org/sms-test/log.php";

    //Keys in JSON object on the SMS to be sent to the server
    public static final String JSON_KEY_UPLOAD_SMS              = "sms";
    public static final String JSON_KEY_UPLOAD_SMS_ID           = "id";
    public static final String JSON_KEY_UPLOAD_SMS_PHONE        = "phone";
    public static final String JSON_KEY_UPLOAD_SMS_UPDATE       = "update";
    public static final String JSON_KEY_UPLOAD_SMS_STATUS       = "status";

    //Keys in SharedPreferences
    public static final String SH_KEY_SERVICE_STATUS            = "status";
    public static final String SH_KEY_USE_STATUS_SIM1           = "use_sim1_status";
    public static final String SH_KEY_USE_STATUS_SIM2           = "use_sim2_status";
    public static final String SH_KEY_SIM1_TOKEN                = "sim1_token";
    public static final String SH_KEY_SIM2_TOKEN                = "sim2_token";
    public static final String SH_KEY_SIM_REJECT_CALL_SMS      = "sim_reject_call_sms";
    public static final String SH_KEY_SIM_REJECT_CALL_STATUS   = "sim_reject_call_status";
    public static final String SH_KEY_SIM_AUTO_REPLY_SMS       = "sim_auto_reply_sms";
    public static final String SH_KEY_SIM_AUTO_REPLY_SMS_STATUS= "sim_auto_reply_sms_status";
    public static final String SH_KEY_SIM1_REJECT_CALL_SMS      = "sim1_reject_call_sms";
    public static final String SH_KEY_SIM1_REJECT_CALL_STATUS   = "sim1_reject_call_status";
    public static final String SH_KEY_SIM1_AUTO_REPLY_SMS       = "sim1_auto_reply_sms";
    public static final String SH_KEY_SIM1_AUTO_REPLY_SMS_STATUS= "sim1_auto_reply_sms_status";
    public static final String SH_KEY_SIM2_REJECT_CALL_SMS      = "sim2_reject_call_sms";
    public static final String SH_KEY_SIM2_REJECT_CALL_STATUS   = "sim2_reject_call_status";
    public static final String SH_KEY_SIM2_AUTO_REPLY_SMS       = "sim2_auto_reply_sms";
    public static final String SH_KEY_SIM2_AUTO_REPLY_SMS_STATUS= "sim2_auto_reply_sms_status";
    public static final String SH_KEY_AUTO_START_WITH_PHONE     = "auto_start_with_phone";
    public static final String SH_KEY_AUTO_REJECT_ALL_CALLS_SIM = "auto_reject_all_calls_sim";
    public static final String SH_KEY_AUTO_REJECT_ALL_CALLS_SIM1= "auto_reject_all_calls_sim1";
    public static final String SH_KEY_AUTO_REJECT_ALL_CALLS_SIM2= "auto_reject_all_calls_sim2";
    public static final String SH_KEY_AUTO_REPLY_SMS_LISTENER_STATUS    = "auto_reply_sms_listener_status";


    //Table Names for DB
    public static final String TABLE_NAME_SMS               = "messageQueue";
    public static final String TABLE_NAME_TOKEN             = "tokens";

    //Field Names for the TOKEN in DB
    public static final String DB_FIELDNAME_TOKEN_TOKEN     = "token";

    //Key Names for Bundle
    public static final String BUNDLE_KEY_ID                = "id";
    public static final String BUNDLE_KEY_TEXT              = "text";
    public static final String BUNDLE_KEY_PHONE             = "phone";
    public static final String BUNDLE_KEY_NANOTIME          = "nanotime";
    public static final String BUNDLE_KEY_STATUS            = "status";
    public static final String BUNDLE_KEY_SUBSCRIPTION_ID   = "subscription_id";

    //Field Names for the SMS in DB
    public static final String DB_FIELDNAME_SMS_ID           = "id";
    public static final String DB_FIELDNAME_SMS_TEXT         = "text";
    public static final String DB_FIELDNAME_SMS_PHONE        = "phone";
    public static final String DB_FIELDNAME_SMS_TIME         = "time";
    public static final String DB_FIELDNAME_SMS_TIMESTAMP    = "timestamp";
    public static final String DB_FIELDNAME_SMS_TYPE        = "type";
    public static final String DB_FIELDNAME_SMS_DBSTATUS     = "dbstatus";
    public static final String DB_FIELDNAME_SMS_PROCESSING   = "processing";
    public static final String DB_FIELDNAME_SMS_PREVDBSTATUS = "prevdbstatus";

    //Status Constants
    public static final String STATUS_INIT                  = "init";
    public static final String STATUS_STARTED               = "started";
    public static final String STATUS_RECEIVED              = "Received";
    public static final String STATUS_SENT                  = "Sent";
    public static final String STATUS_DONE                  = "done";
    public static final String STATUS_FAILED                = "Failed";

    public static final String DEFAULT_AUTOMSG_REJECT_CALL  = "We will come back to you as soon as possible.";
    public static final String DEFAULT_AUTOMSG_REPLY_SMS    = "We will come back to you as soon as possible.";
}
