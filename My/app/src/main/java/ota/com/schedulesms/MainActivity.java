package ota.com.schedulesms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ota.com.schedulesms.model.SimData;

public class MainActivity extends AppCompatActivity {
    public Button mBtnControl;
    protected Context context;
    protected Intent mSvcMessagingService;
    private static final int PERMISSION_ALL = 111;
    private static final int REQ_CODE_SEND_SMS = 1;
    private static final int REQ_CODE_ANSWER_PHONE_CALL = 3;
    private static final int REQ_CODE_READ_PHONE_STATE = 2;
    private static final int REQ_CODE_READ_CALL_LOG = 4;
    private static final int REQ_CODE_RECEIVE_SMS = 6;
    public static MainActivity instance = null;

    public View mLlMainOneSimRoot;
    public TextView mTvSimIccid;
    public EditText mEdSimToken;

    public View mLlMainTwoSimRoot;

    public View mLlSim1stRoot;
    public TextView mTvSim1stStatus;
    public EditText mEdSim1stToken;
    public TextView mTvSim1stIccid;
    public SwitchCompat mSwSim1st;

    public View mLlSim2ndRoot;
    public TextView mTvSim2ndStatus;
    public EditText mEdSim2ndToken;
    public TextView mTvSim2ndIccid;
    public SwitchCompat mSwSim2nd;

    public ArrayList<String> mArrSimIccIds;
    public ArrayList<Boolean> mArrSimUsageStatus;
    public ArrayList<String> mArrTokens;
    public ArrayList<String> mArrSimIds;
    public boolean m_bSupportDualSims = false;

    public HashMap<Integer, SimData> mMapSimData;
    public final String[] PERMISSIONS = {
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.READ_PHONE_STATE,
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        context = this.getApplicationContext();

        mSvcMessagingService = new Intent(context, MessagingService.class);
//        checkPermissions();
        if (!hasPermissions(context, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.SEND_SMS
                }, REQ_CODE_SEND_SMS);
            }
            displayStatus("Send SMS Permission Denied.", false);
        }else{
            displayStatus("Send SMS Permission Granted.", false);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestPermissions(new String[]{
                        Manifest.permission.ANSWER_PHONE_CALLS
                }, REQ_CODE_ANSWER_PHONE_CALL);
            }
            displayStatus("Answer Phone Calls Permission Denied.", false);
        }else{
            displayStatus("Answer Phone Calls Permission Granted.", false);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_PHONE_STATE
                }, REQ_CODE_READ_PHONE_STATE);
            }
            displayStatus("Read Phone State Permission Denied.", false);
        }else{
            displayStatus("Read Phone State Permission Granted.", false);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_CALL_LOG
                }, REQ_CODE_READ_CALL_LOG);
            }
            displayStatus("Read Call Log Permission Denied.", false);
        }else{
            displayStatus("Read Call Log Permission Granted.", false);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.RECEIVE_SMS
                }, REQ_CODE_RECEIVE_SMS);
            }
            displayStatus("Read Call Log Permission Denied.", false);
        }else{
            displayStatus("Read Call Log Permission Granted.", false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        initControls();
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        boolean bStartedService = sharedPreferences.getBoolean(Constant.SH_KEY_SERVICE_STATUS, false);
        if (bStartedService) {
            mBtnControl.setText("Stop!");
        } else {
            mBtnControl.setText("Start!");
        }
        sharedPreferences.edit().putBoolean(Constant.SH_KEY_USE_STATUS_SIM1, mSwSim1st.isChecked())
                .putBoolean(Constant.SH_KEY_USE_STATUS_SIM2, mSwSim2nd.isChecked()).apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Send SMS Permission Granted", Toast.LENGTH_SHORT).show();
                displayStatus("Send SMS Permission Granted.", false);
            } else {
                Toast.makeText(context, "Send SMS Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
                displayStatus("Send SMS Permission Denied.", false);
            }
        } else if (requestCode == REQ_CODE_READ_PHONE_STATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Read Phone State Permission Granted.", Toast.LENGTH_LONG).show();
                Constant.m_bReadPhoneState = true;
                initSimData();
                updateViewStatusForSims();
                displayStatus("Read Phone State Permission Granted.", false);
            } else {
                Constant.m_bReadPhoneState = false;
                Toast.makeText(context, "Read Phone State Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
                displayStatus("Read Phone State Permission Denied.", false);
            }
        }else if(requestCode == REQ_CODE_ANSWER_PHONE_CALL){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Answer Phone Call Permission Granted.", Toast.LENGTH_LONG).show();
                displayStatus("Answer Phone Call Permission Granted.", false);
            } else {
                Toast.makeText(context, "Answer Phone Call Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
                displayStatus("Answer Phone Call Permission Denied.", false);
            }
        }
    }

    private void initControls() {
        mLlMainOneSimRoot = findViewById(R.id.ll_main_onesim_root);
        mTvSimIccid = (TextView) findViewById(R.id.tv_onesim_id);
        mBtnControl = findViewById(R.id.btn_control);
        mEdSimToken = findViewById(R.id.ed_token);
        mEdSimToken.requestFocus();

        mLlMainTwoSimRoot = findViewById(R.id.ll_main_twosim_root);

        mLlSim1stRoot = findViewById(R.id.ll_main_sim1_root);
        mTvSim1stStatus = (TextView) findViewById(R.id.tv_main_sim1_status);
        mSwSim1st = (SwitchCompat) findViewById(R.id.sw_use_sim1);
        mEdSim1stToken = (EditText) findViewById(R.id.ed_token_sim1);
        mTvSim1stIccid = (TextView) findViewById(R.id.tv_iccid_sim1);

        mLlSim2ndRoot = findViewById(R.id.ll_main_sim2_root);
        mTvSim2ndStatus = (TextView) findViewById(R.id.tv_main_sim2_status);
        mSwSim2nd = (SwitchCompat) findViewById(R.id.sw_use_sim2);
        mEdSim2ndToken = (EditText) findViewById(R.id.ed_token_sim2);
        mTvSim2ndIccid = (TextView) findViewById(R.id.tv_iccid_sim2);

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        mEdSimToken.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                DBHelper dbHelper = new DBHelper(MainActivity.this);
                dbHelper.updateToken(mEdSimToken.getText().toString());
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putString(Constant.SH_KEY_SIM1_TOKEN, mEdSimToken.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mEdSim1stToken.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                DBHelper dbHelper = new DBHelper(MainActivity.this);
                dbHelper.updateToken(mEdSim1stToken.getText().toString());
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putString(Constant.SH_KEY_SIM1_TOKEN, mEdSim1stToken.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEdSim2ndToken.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putString(Constant.SH_KEY_SIM2_TOKEN, mEdSim2ndToken.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBtnControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    toggleService(mSvcMessagingService);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{
                                Manifest.permission.SEND_SMS
                        }, REQ_CODE_SEND_SMS);
                    }
                }
            }
        });

        findViewById(R.id.rootView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

        findViewById(R.id.tv_main_twosim_sim1_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.tv_main_twosim_sim1_title).setBackgroundColor(getResources().getColor(R.color.mainfg_1));
                findViewById(R.id.tv_main_twosim_sim2_title).setBackgroundColor(getResources().getColor(R.color.mainfg_2));
                mLlSim1stRoot.setVisibility(View.VISIBLE);
                mLlSim2ndRoot.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.tv_main_twosim_sim2_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.tv_main_twosim_sim1_title).setBackgroundColor(getResources().getColor(R.color.mainfg_2));
                findViewById(R.id.tv_main_twosim_sim2_title).setBackgroundColor(getResources().getColor(R.color.mainfg_1));
                mLlSim1stRoot.setVisibility(View.GONE);
                mLlSim2ndRoot.setVisibility(View.VISIBLE);
            }
        });

        mSwSim1st.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(Constant.SH_KEY_USE_STATUS_SIM1, isChecked).apply();
                SimData simData = mMapSimData.get(0);
                if (simData == null) return;
                simData.m_bEnabled = isChecked;
                mMapSimData.put(0, simData);
            }
        });

        mSwSim2nd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(Constant.SH_KEY_USE_STATUS_SIM2, isChecked).apply();
                SimData simData = mMapSimData.get(1);
                if (simData == null) return;
                simData.m_bEnabled = isChecked;
                mMapSimData.put(1, simData);
            }
        });

        updateViewStatusForSims();

        findViewById(R.id.tv_settings_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        mMapSimData = new HashMap<>();
        initSimData();

        mArrTokens = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        if (mArrSimIccIds.size() > 1) {
            mArrTokens.add(sharedPreferences.getString(Constant.SH_KEY_SIM1_TOKEN, ""));
            mArrTokens.add(sharedPreferences.getString(Constant.SH_KEY_SIM2_TOKEN, ""));
        } else {
            mArrTokens.add(sharedPreferences.getString(Constant.SH_KEY_SIM1_TOKEN, ""));
        }
    }

    @SuppressLint("MissingPermission")
    private void initSimData() {
        mArrSimIccIds = new ArrayList<>();
        mArrSimIds = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    REQ_CODE_READ_PHONE_STATE);
            Constant.m_bReadPhoneState = false;
        } else {
            Constant.m_bReadPhoneState = true;
        }
        int i = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
            if (Constant.m_bReadPhoneState) {
                if (subscriptionManager != null) {
                    List<SubscriptionInfo> localList = subscriptionManager.getActiveSubscriptionInfoList();
                    if (localList != null && localList.size() > 0) {
                        for (i = 0; i < localList.size(); i++) {
                            mArrSimIccIds.add(String.valueOf(localList.get(i).getSubscriptionId()));
                            mArrSimIds.add(String.valueOf(localList.get(i).getSubscriptionId()));
                        }
                    }
                }
            }
        } else {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            mArrSimIccIds.add(String.valueOf(tm.getSimSerialNumber()));
            mArrSimIds.add(tm.getSubscriberId());
        }
    }

    private void updateViewStatusForSims() {
//        mArrSimIccIds = new ArrayList<>();
        try {
            if (getSimCount() == 1) {
                mLlMainOneSimRoot.setVisibility(View.VISIBLE);
                mLlMainTwoSimRoot.setVisibility(View.GONE);
                DBHelper dbHelper = new DBHelper(this);
                mEdSimToken.setText(dbHelper.getAppToken());
                dbHelper.close();
                mTvSimIccid.setText(String.format("%s %s", getString(R.string.sim_id_title), mArrSimIccIds.get(0)));
            } else if (getSimCount() == 2) {
                mLlMainOneSimRoot.setVisibility(View.GONE);
                mLlMainTwoSimRoot.setVisibility(View.VISIBLE);
                mTvSim1stIccid.setText(String.format("%s %s", getString(R.string.sim_id_title), mArrSimIccIds.get(0)));
                mTvSim2ndIccid.setText(String.format("%s %s", getString(R.string.sim_id_title), mArrSimIccIds.get(1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        if (mArrSimIccIds.size() == 1) {
//            mLlMainOneSimRoot.setVisibility(View.VISIBLE);
//            mLlMainTwoSimRoot.setVisibility(View.GONE);
//            DBHelper dbHelper = new DBHelper(this);
//            mEdSimToken.setText(dbHelper.getAppToken());
//            dbHelper.close();
//            mTvSimIccid.setText(String.format("%s %s", getString(R.string.sim_id_title), mArrSimIccIds.get(0)));
//        } else if (mArrSimIccIds.size() == 2) {
//            mLlMainOneSimRoot.setVisibility(View.GONE);
//            mLlMainTwoSimRoot.setVisibility(View.VISIBLE);
//            mTvSim1stIccid.setText(String.format("%s %s", getString(R.string.sim_id_title), mArrSimIccIds.get(0)));
//            mTvSim2ndIccid.setText(String.format("%s %s", getString(R.string.sim_id_title), mArrSimIccIds.get(1)));
//        }
    }

    public int getSimCount() {
//        return 2;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        int i = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (Constant.m_bReadPhoneState) {
                if (subscriptionManager != null) {
                    List<SubscriptionInfo> localList = subscriptionManager.getActiveSubscriptionInfoList();
                    if (localList != null) {
                        return localList.size();
                    }
                }
            }
        }
        return 1;
    }

    @SuppressLint("SetTextI18n")
    public void toggleService(Intent txtdata) {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        boolean bStartedService = sharedPreferences.getBoolean(Constant.SH_KEY_SERVICE_STATUS, false);
        if (!bStartedService) {
            context.startService(txtdata);
            mBtnControl.setText("Stop!");
            sharedPreferences.edit().putBoolean("status", true).apply();
        } else {
            context.stopService(txtdata);
            mBtnControl.setText("Start!");
            sharedPreferences.edit().putBoolean("status", false).apply();
        }
    }

    @SuppressWarnings("unused")
    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }

        for (ActivityManager.RunningServiceInfo aserv : activityManager.getRunningServices(3000))
            if (aserv.service.getClassName().equals(getPackageName() + "MessagingService"))
                return true;
        return false;
    }

    private void hideKeyboard() {
        if (mEdSimToken != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEdSimToken.getWindowToken(), 0);
            mEdSimToken.clearFocus();
            mBtnControl.requestFocus();
        }
    }

    public boolean checkIfHasDualSim() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    REQ_CODE_READ_PHONE_STATE);
            Constant.m_bReadPhoneState = false;
        } else {
            Constant.m_bReadPhoneState = true;
        }
        int i = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (Constant.m_bReadPhoneState) {
                if (subscriptionManager != null) {
                    List<SubscriptionInfo> localList = subscriptionManager.getActiveSubscriptionInfoList();
                    if (localList != null && localList.size() > 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void displayStatus(final String strStatus, final boolean bIfNew){
        if (instance == null) return;
        instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvStatus = instance.findViewById(R.id.tv_main_status);
                if (tvStatus != null){
                    if (bIfNew){
                        tvStatus.setText(strStatus);
                    }else{
                        String strText = tvStatus.getText().toString();
                        tvStatus.setText(String.format("%s\n%s", strText, strStatus));
                    }
                }
            }
        });
    }
}