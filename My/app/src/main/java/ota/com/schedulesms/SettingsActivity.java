package ota.com.schedulesms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQ_CODE_ANSWER_PHONE_CALL_SIM = 1;
    private static final int REQ_CODE_READ_CALL_LOG_SIM = 2;
    private static final int REQ_CODE_READ_PHONE_STATE_SIM = 3;
    private static final int REQ_CODE_SEND_SMS_SIM_CALL = 4;
    private static final int REQ_CODE_SEND_SMS_SIM_SMS = 5;
    private static final int REQ_CODE_RECEIVE_SMS_SIM = 6;
    private static final int REQ_CODE_ANSWER_PHONE_CALL_SIM1 = 11;
    private static final int REQ_CODE_READ_CALL_LOG_SIM1 = 12;
    private static final int REQ_CODE_READ_PHONE_STATE_SIM1 = 13;
    private static final int REQ_CODE_SEND_SMS_SIM1_CALL = 14;
    private static final int REQ_CODE_SEND_SMS_SIM1_SMS = 15;
    private static final int REQ_CODE_RECEIVE_SMS_SIM1 = 16;
    private static final int REQ_CODE_ANSWER_PHONE_CALL_SIM2 = 21;
    private static final int REQ_CODE_READ_CALL_LOG_SIM2 = 22;
    private static final int REQ_CODE_READ_PHONE_STATE_SIM2 = 23;
    private static final int REQ_CODE_SEND_SMS_SIM2_CALL = 24;
    private static final int REQ_CODE_SEND_SMS_SIM2_SMS = 25;
    private static final int REQ_CODE_RECEIVE_SMS_SIM2 = 26;
    private static final int REQ_CODE_RECEIVE_BOOT_COMPLETE = 31;


    private SwitchCompat mSwAutoStart;

    private View mLlSettingOneSimRoot;
    private SwitchCompat mSwRejectAllCallsSim;
    private SwitchCompat mSwRejectWithSmsSim;
    private EditText mEdRejectWithSmsSim;
    private TextView mTvRejectWithSmsLenSim;
    private SwitchCompat mSwAutoReplySmsSim;
    private EditText mEdAutoReplySmsSim;
    private TextView mTvAutoReplySmsLenSim;

    private View mLlSettingTwoSimRoot;
    private View mTvTabSim1;
    private View mTvTabSim2;

    private View mLlSettingSimRoot1;
    private SwitchCompat mSwRejectAllCallsSim1;
    private SwitchCompat mSwRejectWithSmsSim1;
    private EditText mEdRejectWithSmsSim1;
    private TextView mTvRejectWithSmsLenSim1;
    private SwitchCompat mSwAutoReplySmsSim1;
    private EditText mEdAutoReplySmsSim1;
    private TextView mTvAutoReplySmsLenSim1;

    private View mLlSettingSimRoot2;
    private SwitchCompat mSwRejectAllCallsSim2;
    private SwitchCompat mSwRejectWithSmsSim2;
    private EditText mEdRejectWithSmsSim2;
    private TextView mTvRejectWithSmsLenSim2;
    private SwitchCompat mSwAutoReplySmsSim2;
    private EditText mEdAutoReplySmsSim2;
    private TextView mTvAutoReplySmsLenSim2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initControls();
    }

    @SuppressLint("DefaultLocale")
    private void initControls() {
        mSwAutoStart = findViewById(R.id.sw_setting_auto_start);

        mLlSettingOneSimRoot = findViewById(R.id.ll_setting_sim_root);
        mSwRejectAllCallsSim = findViewById(R.id.sw_setting_reject_all_calls);
        mSwRejectWithSmsSim = findViewById(R.id.sw_setting_reject_withsms);
        mEdRejectWithSmsSim = findViewById(R.id.ed_setting_reject_sms);
        mTvRejectWithSmsLenSim = findViewById(R.id.tv_setting_reject_sms_len);
        mSwAutoReplySmsSim = findViewById(R.id.sw_setting_reply_withsms);
        mEdAutoReplySmsSim = findViewById(R.id.ed_setting_reply_sms);
        mTvAutoReplySmsLenSim = findViewById(R.id.tv_setting_reply_sms_len);

        mLlSettingTwoSimRoot = findViewById(R.id.ll_main_twosim_root);
        mTvTabSim1 = findViewById(R.id.tv_main_twosim_sim1_title);
        mTvTabSim2 = findViewById(R.id.tv_main_twosim_sim2_title);

        mLlSettingSimRoot1 = findViewById(R.id.ll_setting_sim1_root);
        mSwRejectAllCallsSim1 = findViewById(R.id.sw_setting_reject_all_calls_sim1);
        mSwRejectWithSmsSim1 = findViewById(R.id.sw_setting_reject_withsms_1);
        mEdRejectWithSmsSim1 = findViewById(R.id.ed_setting_reject_sms_1);
        mTvRejectWithSmsLenSim1 = findViewById(R.id.tv_setting_reject_sms_len_1);
        mSwAutoReplySmsSim1 = findViewById(R.id.sw_setting_reply_withsms_1);
        mEdAutoReplySmsSim1 = findViewById(R.id.ed_setting_reply_sms_1);
        mTvAutoReplySmsLenSim1 = findViewById(R.id.tv_setting_reply_sms_len_1);

        mLlSettingSimRoot2 = findViewById(R.id.ll_setting_sim2_root);
        mSwRejectAllCallsSim2 = findViewById(R.id.sw_setting_reject_all_calls_sim2);
        mSwRejectWithSmsSim2 = findViewById(R.id.sw_setting_reject_withsms_2);
        mEdRejectWithSmsSim2 = findViewById(R.id.ed_setting_reject_sms_2);
        mTvRejectWithSmsLenSim2 = findViewById(R.id.tv_setting_reject_sms_len_2);
        mSwAutoReplySmsSim2 = findViewById(R.id.sw_setting_reply_withsms_2);
        mEdAutoReplySmsSim2 = findViewById(R.id.ed_setting_reply_sms_2);
        mTvAutoReplySmsLenSim2 = findViewById(R.id.tv_setting_reply_sms_len_2);

        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        mSwAutoStart.setChecked(sharedPreferences.getBoolean(Constant.SH_KEY_AUTO_START_WITH_PHONE, true));
        mSwAutoStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
                        mSwAutoStart.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Boot Complete Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED},
                                    REQ_CODE_RECEIVE_BOOT_COMPLETE);
                        }
                        return;
                    }
                }
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(Constant.SH_KEY_AUTO_START_WITH_PHONE, isChecked).apply();
                PackageManagerUtils.setComponentEnabledOrDefault(
                        getApplicationContext(), StartupReceiver.class, isChecked);
                if (isChecked){
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED); // TODO: check
                    registerReceiver(ScheduleSMSApplication.mStartupReceiver, intentFilter);
                }else{
                    unregisterReceiver(ScheduleSMSApplication.mStartupReceiver);
                }
            }
        });

        mSwRejectAllCallsSim.setChecked(sharedPreferences.getBoolean(Constant.SH_KEY_AUTO_REJECT_ALL_CALLS_SIM, false));
        mSwRejectAllCallsSim.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                        mSwRejectAllCallsSim.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Answer Phone Call Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.ANSWER_PHONE_CALLS},
                                    REQ_CODE_ANSWER_PHONE_CALL_SIM);
                        }
                        return;
                    }
                }
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(Constant.SH_KEY_AUTO_REJECT_ALL_CALLS_SIM, isChecked).apply();

                mSwRejectWithSmsSim.setEnabled(isChecked);
                mEdRejectWithSmsSim.setEnabled(isChecked);

                if (isChecked) {
                    CallMonitoringService.start(getApplicationContext(), 0);
                } else {
                    CallMonitoringService.stop(getApplicationContext(), 0);
                }
                PackageManagerUtils.setComponentEnabledOrDefault(
                        getApplicationContext(), CallMonitoringService.class, isChecked);
            }
        });

        mSwRejectWithSmsSim.setChecked(sharedPreferences.getBoolean(Constant.SH_KEY_SIM_REJECT_CALL_STATUS, false));
        mSwRejectWithSmsSim.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                        mSwRejectWithSmsSim.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Read Call Log Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_CALL_LOG},
                                    REQ_CODE_READ_CALL_LOG_SIM);
                        }
                        return;
                    }
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        mSwRejectWithSmsSim.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Send SMS Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.SEND_SMS},
                                    REQ_CODE_SEND_SMS_SIM_CALL);
                        }
                        return;
                    }
                }
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(Constant.SH_KEY_SIM_REJECT_CALL_STATUS, isChecked).apply();
                mEdRejectWithSmsSim.setEnabled(isChecked);
            }
        });

        mEdRejectWithSmsSim.setEnabled(mSwRejectWithSmsSim.isChecked());
        mEdRejectWithSmsSim.setText(sharedPreferences.getString(Constant.SH_KEY_SIM_REJECT_CALL_SMS, getString(R.string.initial_reply_text)));
        mTvRejectWithSmsLenSim.setText(String.format("%d/160", mEdRejectWithSmsSim.getText().toString().length()));
        mEdRejectWithSmsSim.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTvRejectWithSmsLenSim.setText(String.format("%d/160", mEdRejectWithSmsSim.getText().toString().length()));
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putString(Constant.SH_KEY_SIM_REJECT_CALL_SMS, mEdRejectWithSmsSim.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSwAutoReplySmsSim.setChecked(sharedPreferences.getBoolean(Constant.SH_KEY_SIM_AUTO_REPLY_SMS_STATUS, false));
        mSwAutoReplySmsSim.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                        mSwAutoReplySmsSim.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Receive SMS Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, 
                                    new String[]{Manifest.permission.RECEIVE_SMS},
                                    REQ_CODE_RECEIVE_SMS_SIM);
                        }
                        return;
                    }
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        mSwAutoReplySmsSim.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Send SMS Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.SEND_SMS},
                                    REQ_CODE_SEND_SMS_SIM_SMS);
                        }
                        return;
                    }
                }
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(Constant.SH_KEY_SIM_AUTO_REPLY_SMS_STATUS, isChecked).apply();
                mEdAutoReplySmsSim.setEnabled(isChecked);
                handleToggleAutoReplySmsSwitch();
            }
        });

        mEdAutoReplySmsSim.setText(sharedPreferences.getString(Constant.SH_KEY_SIM_AUTO_REPLY_SMS, getString(R.string.initial_reply_text)));
        mTvAutoReplySmsLenSim.setText(String.format("%d/160", mEdAutoReplySmsSim.getText().toString().length()));
        mEdAutoReplySmsSim.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTvAutoReplySmsLenSim.setText(String.format("%d/160", mEdAutoReplySmsSim.getText().toString().length()));
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putString(Constant.SH_KEY_SIM_AUTO_REPLY_SMS, mEdAutoReplySmsSim.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        findViewById(R.id.tv_settings_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTvTabSim1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlSettingSimRoot1.setVisibility(View.VISIBLE);
                mLlSettingSimRoot2.setVisibility(View.GONE);
                mTvTabSim1.setBackgroundColor(getResources().getColor(R.color.mainfg_1));
                mTvTabSim2.setBackgroundColor(getResources().getColor(R.color.mainfg_2));
            }
        });

        mTvTabSim2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlSettingSimRoot1.setVisibility(View.GONE);
                mLlSettingSimRoot2.setVisibility(View.VISIBLE);
                mTvTabSim1.setBackgroundColor(getResources().getColor(R.color.mainfg_2));
                mTvTabSim2.setBackgroundColor(getResources().getColor(R.color.mainfg_1));
            }
        });

        mSwRejectAllCallsSim1.setChecked(sharedPreferences.getBoolean(Constant.SH_KEY_AUTO_REJECT_ALL_CALLS_SIM1, false));
        mSwRejectAllCallsSim1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                        mSwRejectAllCallsSim1.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Answer Phone Call Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.ANSWER_PHONE_CALLS},
                                    REQ_CODE_ANSWER_PHONE_CALL_SIM1);
                        }
                        return;
                    }
                }
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(Constant.SH_KEY_AUTO_REJECT_ALL_CALLS_SIM1, isChecked).apply();

                mSwRejectWithSmsSim1.setEnabled(isChecked);
                mEdRejectWithSmsSim1.setEnabled(isChecked);

                if (isChecked) {
                    CallMonitoringService.start(getApplicationContext(), 1);
                } else {
                    CallMonitoringService.stop(getApplicationContext(), 1);
                }
                PackageManagerUtils.setComponentEnabledOrDefault(
                        getApplicationContext(), CallMonitoringService.class, isChecked);
            }
        });

        mSwRejectWithSmsSim1.setChecked(sharedPreferences.getBoolean(Constant.SH_KEY_SIM1_REJECT_CALL_STATUS, false));
        mSwRejectWithSmsSim1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                        mSwRejectWithSmsSim1.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Read Call Log Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_CALL_LOG},
                                    REQ_CODE_READ_CALL_LOG_SIM1);
                        }
                        return;
                    }
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        mSwRejectWithSmsSim1.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Send SMS Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.SEND_SMS},
                                    REQ_CODE_SEND_SMS_SIM1_CALL);
                        }
                        return;
                    }
                }
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(Constant.SH_KEY_SIM1_REJECT_CALL_STATUS, isChecked).apply();
                mEdRejectWithSmsSim1.setEnabled(isChecked);
            }
        });

        mEdRejectWithSmsSim1.setEnabled(mSwRejectWithSmsSim1.isChecked());
        mEdRejectWithSmsSim1.setText(sharedPreferences.getString(Constant.SH_KEY_SIM1_REJECT_CALL_SMS, getString(R.string.initial_reply_text)));
        mTvRejectWithSmsLenSim1.setText(String.format("%d/160", mEdRejectWithSmsSim1.getText().toString().length()));
        mEdRejectWithSmsSim1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTvRejectWithSmsLenSim1.setText(String.format("%d/160", mEdRejectWithSmsSim1.getText().toString().length()));
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putString(Constant.SH_KEY_SIM1_REJECT_CALL_SMS, mEdRejectWithSmsSim1.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSwAutoReplySmsSim1.setChecked(sharedPreferences.getBoolean(Constant.SH_KEY_SIM1_AUTO_REPLY_SMS_STATUS, false));
        mSwAutoReplySmsSim1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                        mSwAutoReplySmsSim1.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Receive SMS Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.RECEIVE_SMS},
                                    REQ_CODE_RECEIVE_SMS_SIM1);
                        }
                        return;
                    }
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        mSwAutoReplySmsSim1.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Send SMS Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.SEND_SMS},
                                    REQ_CODE_SEND_SMS_SIM1_SMS);
                        }
                        return;
                    }
                }
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(Constant.SH_KEY_SIM1_AUTO_REPLY_SMS_STATUS, isChecked).apply();
                mEdAutoReplySmsSim1.setEnabled(isChecked);
                handleToggleAutoReplySmsSwitch();
            }
        });

        mEdAutoReplySmsSim1.setText(sharedPreferences.getString(Constant.SH_KEY_SIM1_AUTO_REPLY_SMS, getString(R.string.initial_reply_text)));
        mTvAutoReplySmsLenSim1.setText(String.format("%d/160", mEdAutoReplySmsSim1.getText().toString().length()));
        mEdAutoReplySmsSim1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTvAutoReplySmsLenSim1.setText(String.format("%d/160", mEdAutoReplySmsSim1.getText().toString().length()));
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putString(Constant.SH_KEY_SIM1_AUTO_REPLY_SMS, mEdAutoReplySmsSim1.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSwRejectAllCallsSim2.setChecked(sharedPreferences.getBoolean(Constant.SH_KEY_AUTO_REJECT_ALL_CALLS_SIM2, false));
        mSwRejectAllCallsSim2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                        mSwRejectAllCallsSim2.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Answer Phone Call Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.ANSWER_PHONE_CALLS},
                                    REQ_CODE_ANSWER_PHONE_CALL_SIM2);
                        }
                        return;
                    }
                }
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(Constant.SH_KEY_AUTO_REJECT_ALL_CALLS_SIM2, isChecked).apply();

                mSwRejectWithSmsSim2.setEnabled(isChecked);
                mEdRejectWithSmsSim2.setEnabled(isChecked);

                if (isChecked) {
                    CallMonitoringService.start(getApplicationContext(), 2);
                } else {
                    CallMonitoringService.stop(getApplicationContext(), 2);
                }
                PackageManagerUtils.setComponentEnabledOrDefault(
                        getApplicationContext(), CallMonitoringService.class, isChecked);
            }
        });

        mSwRejectWithSmsSim2.setChecked(sharedPreferences.getBoolean(Constant.SH_KEY_SIM2_REJECT_CALL_STATUS, false));
        mSwRejectWithSmsSim2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                        mSwRejectWithSmsSim2.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Read Call Log Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_CALL_LOG},
                                    REQ_CODE_READ_CALL_LOG_SIM2);
                        }
                        return;
                    }
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        mSwRejectWithSmsSim2.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Send SMS Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.SEND_SMS},
                                    REQ_CODE_SEND_SMS_SIM2_CALL);
                        }
                        return;
                    }
                }
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(Constant.SH_KEY_SIM2_REJECT_CALL_STATUS, isChecked).apply();
                mEdRejectWithSmsSim2.setEnabled(isChecked);
            }
        });
        mEdRejectWithSmsSim2.setEnabled(mSwRejectWithSmsSim2.isChecked());
        mEdRejectWithSmsSim2.setText(sharedPreferences.getString(Constant.SH_KEY_SIM2_REJECT_CALL_SMS, getString(R.string.initial_reply_text)));
        mTvRejectWithSmsLenSim2.setText(String.format("%d/160", mEdRejectWithSmsSim2.getText().toString().length()));
        mEdRejectWithSmsSim2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTvRejectWithSmsLenSim2.setText(String.format("%d/160", mEdRejectWithSmsSim2.getText().toString().length()));
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putString(Constant.SH_KEY_SIM2_REJECT_CALL_SMS, mEdRejectWithSmsSim2.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSwAutoReplySmsSim2.setChecked(sharedPreferences.getBoolean(Constant.SH_KEY_SIM2_AUTO_REPLY_SMS_STATUS, false));
        mSwAutoReplySmsSim2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                        mSwAutoReplySmsSim2.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Receive SMS Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.RECEIVE_SMS},
                                    REQ_CODE_RECEIVE_SMS_SIM2);
                        }
                        return;
                    }
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        mSwAutoReplySmsSim2.setChecked(false);
                        Toast.makeText(SettingsActivity.this, "You should grant Send SMS Permission!",
                                Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.SEND_SMS},
                                    REQ_CODE_SEND_SMS_SIM2_SMS);
                        }
                        return;
                    }
                }
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(Constant.SH_KEY_SIM2_AUTO_REPLY_SMS_STATUS, isChecked).apply();
                mEdAutoReplySmsSim2.setEnabled(isChecked);
                handleToggleAutoReplySmsSwitch();
            }
        });

        mEdAutoReplySmsSim2.setText(sharedPreferences.getString(Constant.SH_KEY_SIM2_AUTO_REPLY_SMS, getString(R.string.initial_reply_text)));
        mTvAutoReplySmsLenSim2.setText(String.format("%d/160", mEdAutoReplySmsSim2.getText().toString().length()));
        mEdAutoReplySmsSim2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTvAutoReplySmsLenSim2.setText(String.format("%d/160", mEdAutoReplySmsSim2.getText().toString().length()));
                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                sharedPreferences.edit().putString(Constant.SH_KEY_SIM2_AUTO_REPLY_SMS, mEdAutoReplySmsSim2.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        updateViewForSim();
    }

    private void updateViewForSim() {
        if (getSimCount() == 2) {
            mLlSettingOneSimRoot.setVisibility(View.GONE);
            mLlSettingTwoSimRoot.setVisibility(View.VISIBLE);
        } else {
            mLlSettingOneSimRoot.setVisibility(View.VISIBLE);
            mLlSettingTwoSimRoot.setVisibility(View.GONE);
        }
    }

    private void handleToggleAutoReplySmsSwitch(){
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        boolean bCurrentStatusOverall = sharedPreferences.getBoolean(Constant.SH_KEY_AUTO_REPLY_SMS_LISTENER_STATUS, false);
        boolean bCurrentStatusSim = sharedPreferences.getBoolean(Constant.SH_KEY_SIM_AUTO_REPLY_SMS_STATUS, false);
        boolean bCurrentStatusSim1 = sharedPreferences.getBoolean(Constant.SH_KEY_SIM1_AUTO_REPLY_SMS_STATUS, false);
        boolean bCurrentStatusSim2 = sharedPreferences.getBoolean(Constant.SH_KEY_SIM2_AUTO_REPLY_SMS_STATUS, false);
        boolean bStatusAllSims = bCurrentStatusSim || bCurrentStatusSim1 || bCurrentStatusSim2;
        if (!bCurrentStatusOverall && bStatusAllSims){
            SmsMonitoringService.start(getApplicationContext());
            sharedPreferences.edit().putBoolean(Constant.SH_KEY_AUTO_REPLY_SMS_LISTENER_STATUS, true).apply();
        }else if(bCurrentStatusOverall && !bStatusAllSims){
            SmsMonitoringService.stop(getApplicationContext());
            sharedPreferences.edit().putBoolean(Constant.SH_KEY_AUTO_REPLY_SMS_LISTENER_STATUS, false).apply();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_READ_PHONE_STATE_SIM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read Phone State Permission Granted.", Toast.LENGTH_LONG).show();
                Constant.m_bReadPhoneState = true;
                updateViewForSim();
            } else {
                Constant.m_bReadPhoneState = false;
                Toast.makeText(this, "Read Phone State Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQ_CODE_ANSWER_PHONE_CALL_SIM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Answer Call Phone Permission Granted.", Toast.LENGTH_LONG).show();
                mSwRejectAllCallsSim.setChecked(true);
            } else {
                mSwRejectAllCallsSim.setChecked(false);
                Toast.makeText(this, "Answer Call Phone Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQ_CODE_READ_CALL_LOG_SIM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read Call Log Permission Granted.", Toast.LENGTH_LONG).show();
                mSwRejectWithSmsSim.setChecked(true);
            } else {
                mSwRejectWithSmsSim.setChecked(false);
                Toast.makeText(this, "Read Call Log Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQ_CODE_SEND_SMS_SIM_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Send Sms Permission Granted.", Toast.LENGTH_LONG).show();
                mSwRejectWithSmsSim.setChecked(true);
            } else {
                mSwRejectWithSmsSim.setChecked(false);
                Toast.makeText(this, "Send Sms Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQ_CODE_SEND_SMS_SIM_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Send Sms Permission Granted.", Toast.LENGTH_LONG).show();
                mSwAutoReplySmsSim.setChecked(true);
            } else {
                mSwAutoReplySmsSim.setChecked(false);
                Toast.makeText(this, "Send Sms Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQ_CODE_RECEIVE_SMS_SIM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Receive Sms Permission Granted.", Toast.LENGTH_LONG).show();
                mSwAutoReplySmsSim.setChecked(true);
            } else {
                mSwAutoReplySmsSim.setChecked(false);
                Toast.makeText(this, "Receive Sms Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQ_CODE_READ_CALL_LOG_SIM1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read Call Log Permission Granted.", Toast.LENGTH_LONG).show();
                mSwRejectWithSmsSim1.setChecked(true);
            } else {
                mSwRejectWithSmsSim1.setChecked(false);
                Toast.makeText(this, "Read Call Log Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQ_CODE_SEND_SMS_SIM1_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Send Sms Permission Granted.", Toast.LENGTH_LONG).show();
                mSwRejectWithSmsSim1.setChecked(true);
            } else {
                mSwRejectWithSmsSim1.setChecked(false);
                Toast.makeText(this, "Send Sms Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQ_CODE_SEND_SMS_SIM1_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Send Sms Permission Granted.", Toast.LENGTH_LONG).show();
                mSwAutoReplySmsSim1.setChecked(true);
            } else {
                mSwAutoReplySmsSim1.setChecked(false);
                Toast.makeText(this, "Send Sms Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQ_CODE_RECEIVE_SMS_SIM1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Receive Sms Permission Granted.", Toast.LENGTH_LONG).show();
                mSwAutoReplySmsSim1.setChecked(true);
            } else {
                mSwAutoReplySmsSim1.setChecked(false);
                Toast.makeText(this, "Receive Sms Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQ_CODE_READ_CALL_LOG_SIM2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read Call Log Permission Granted.", Toast.LENGTH_LONG).show();
                mSwRejectWithSmsSim2.setChecked(true);
            } else {
                mSwRejectWithSmsSim2.setChecked(false);
                Toast.makeText(this, "Read Call Log Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQ_CODE_SEND_SMS_SIM2_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Send Sms Permission Granted.", Toast.LENGTH_LONG).show();
                mSwRejectWithSmsSim2.setChecked(true);
            } else {
                mSwRejectWithSmsSim2.setChecked(false);
                Toast.makeText(this, "Send Sms Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQ_CODE_SEND_SMS_SIM2_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Send Sms Permission Granted.", Toast.LENGTH_LONG).show();
                mSwAutoReplySmsSim2.setChecked(true);
            } else {
                mSwAutoReplySmsSim2.setChecked(false);
                Toast.makeText(this, "Send Sms Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == REQ_CODE_RECEIVE_SMS_SIM2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Receive Sms Permission Granted.", Toast.LENGTH_LONG).show();
                mSwAutoReplySmsSim2.setChecked(true);
            } else {
                mSwAutoReplySmsSim2.setChecked(false);
                Toast.makeText(this, "Receive Sms Permission Denied, please manually grant in Settings", Toast.LENGTH_LONG).show();
            }
        }

    }

    public int getSimCount() {
//        return 2;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE},
                        REQ_CODE_READ_PHONE_STATE_SIM);
            }
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

    public boolean checkIfHasDualSim() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    REQ_CODE_READ_PHONE_STATE_SIM);
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
                    return localList != null && localList.size() > 1;
                }
            }
        }
        return false;
    }
}
