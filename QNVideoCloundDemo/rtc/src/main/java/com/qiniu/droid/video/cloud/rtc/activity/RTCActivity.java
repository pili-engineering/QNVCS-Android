package com.qiniu.droid.video.cloud.rtc.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bugsnag.android.Bugsnag;
import com.qiniu.droid.rtc.QNScreenCaptureUtil;
import com.qiniu.droid.video.cloud.rtc.R;
import com.qiniu.droid.video.cloud.rtc.model.ProgressEvent;
import com.qiniu.droid.video.cloud.rtc.model.UpdateInfo;
import com.qiniu.droid.video.cloud.rtc.service.DownloadService;
import com.qiniu.droid.video.cloud.rtc.ui.RadioGroupFlow;
import com.qiniu.droid.video.cloud.rtc.utils.Config;
import com.qiniu.droid.video.cloud.rtc.utils.QNAppServer;
import com.qiniu.droid.video.cloud.rtc.utils.ToastUtils;
import com.qiniu.droid.video.cloud.rtc.utils.Utils;

import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;

public class RTCActivity extends AppCompatActivity {
    private static final int USERNAME_REQUEST_CODE = 0;

    private EditText mRoomEditText;
    private ProgressDialog mProgressDialog;
    private RadioGroupFlow mCaptureModeRadioGroup;
    private RadioButton mScreenCapture;
    private RadioButton mCameraCapture;
    private RadioButton mOnlyAudioCapture;
    private RadioButton mMultiTrackCapture;

    private String mUserName;
    private String mRoomName;
    private boolean mIsScreenCaptureEnabled;
    private int mCaptureMode = 0;
    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            if (checkedRadioButtonId == R.id.camera_capture_button) {
                mCaptureMode = Config.CAMERA_CAPTURE;
            } else if (checkedRadioButtonId == R.id.screen_capture_button) {
                mCaptureMode = Config.SCREEN_CAPTURE;
            } else if (checkedRadioButtonId == R.id.audio_capture_button) {
                mCaptureMode = Config.ONLY_AUDIO_CAPTURE;
            } else if (checkedRadioButtonId == R.id.muti_track_button) {
                mCaptureMode = Config.MUTI_TRACK_CAPTURE;
            }
        }
    };

    public static boolean isUserNameOk(String userName) {
        Pattern pattern = Pattern.compile(Config.USER_NAME_RULE);
        return pattern.matcher(userName).matches();
    }

    public static boolean isRoomNameOk(String roomName) {
        Pattern pattern = Pattern.compile(Config.ROOM_NAME_RULE);
        return pattern.matcher(roomName).matches();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        Bugsnag.init(this);
        EventBus.getDefault().registerSticky(this);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        mUserName = preferences.getString(Config.USER_NAME, "");
        if (mUserName.equals("")) {
            Intent intent = new Intent(this, UserConfigActivity.class);
            startActivityForResult(intent, USERNAME_REQUEST_CODE);
        } else {
            initView();
            checkUpdate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == USERNAME_REQUEST_CODE) {
            mUserName = data.getStringExtra(Config.USER_NAME);
            SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).edit();
            editor.putString(Config.USER_NAME, mUserName);
            editor.apply();
            initView();
            checkUpdate();
        } else if (requestCode == QNScreenCaptureUtil.SCREEN_CAPTURE_PERMISSION_REQUEST_CODE &&
                QNScreenCaptureUtil.onActivityResult(requestCode, resultCode, data)) {
            startConference(mRoomName);
        }
    }

    public void onEvent(ProgressEvent progressEvent) {
        mProgressDialog.setProgress(progressEvent.getProgress());
        if (progressEvent.getProgress() == 100) {
            mProgressDialog.dismiss();
        }
    }

    public void onClickConference(final View v) {
        handleRoomInfo();
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        mUserName = preferences.getString(Config.USER_NAME, "");
        mIsScreenCaptureEnabled = (mCaptureMode == Config.SCREEN_CAPTURE || mCaptureMode == Config.MUTI_TRACK_CAPTURE);
        if (mIsScreenCaptureEnabled) {
            QNScreenCaptureUtil.requestScreenCapture(this);
        } else {
            startConference(mRoomName);
        }
    }

    public void onClickToSetting(View v) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    private void startConference(final String roomName) {
        if (!handleRoomInfo()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取连麦所需的 RoomToken，需要您自行实现业务服务器的相关逻辑
                // 详情请参考【服务端开发说明.RoomToken 签发服务】https://doc.qnsdk.com/rtn/docs/server_overview#1
                final String token = QNAppServer.getInstance().requestRoomToken(RTCActivity.this, mUserName, roomName);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (token == null) {
                            ToastUtils.s(RTCActivity.this, getString(R.string.rtc_null_room_token_toast));
                            return;
                        }
                        Intent intent = new Intent(RTCActivity.this, RoomActivity.class);
                        intent.putExtra(RoomActivity.EXTRA_ROOM_ID, roomName.trim());
                        intent.putExtra(RoomActivity.EXTRA_ROOM_TOKEN, token);
                        intent.putExtra(RoomActivity.EXTRA_USER_ID, mUserName);
                        startActivity(intent);
                    }
                });
            }
        }).start();
    }

    private boolean handleRoomInfo() {
        String roomName = mRoomEditText.getText().toString().trim();
        if (roomName.equals("")) {
            ToastUtils.s(this, getString(R.string.rtc_null_room_name_toast));
            return false;
        }
        if (!RTCActivity.isRoomNameOk(roomName)) {
            ToastUtils.s(this, getString(R.string.rtc_wrong_room_name_toast));
            return false;
        }

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        mUserName = preferences.getString(Config.USER_NAME, "");
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Config.ROOM_NAME, roomName);
        editor.putInt(Config.CAPTURE_MODE, mCaptureMode);
        if (mCaptureMode == Config.SCREEN_CAPTURE) {
            editor.putInt(Config.CODEC_MODE, Config.HW);
        }
        editor.apply();
        mRoomName = roomName;
        return true;
    }

    private void initView() {
        setContentView(R.layout.rtc_activity_rtc);
        mRoomEditText = (EditText) findViewById(R.id.room_edit_text);
        mCaptureModeRadioGroup = findViewById(R.id.capture_mode_button);
        mCaptureModeRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mScreenCapture = (RadioButton) findViewById(R.id.screen_capture_button);
        mCameraCapture = (RadioButton) findViewById(R.id.camera_capture_button);
        mOnlyAudioCapture = (RadioButton) findViewById(R.id.audio_capture_button);
        mMultiTrackCapture = findViewById(R.id.muti_track_button);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        String roomName = preferences.getString(Config.ROOM_NAME, Config.PILI_ROOM);
        int captureMode = preferences.getInt(Config.CAPTURE_MODE, Config.CAMERA_CAPTURE);
        if (QNScreenCaptureUtil.isScreenCaptureSupported()) {
            if (captureMode == Config.SCREEN_CAPTURE) {
                mScreenCapture.setChecked(true);
            } else if (captureMode == Config.CAMERA_CAPTURE) {
                mCameraCapture.setChecked(true);
            } else if (captureMode == Config.ONLY_AUDIO_CAPTURE) {
                mOnlyAudioCapture.setChecked(true);
            } else {
                mMultiTrackCapture.setChecked(true);
            }
        } else {
            mScreenCapture.setEnabled(false);
        }
        mRoomEditText.setText(roomName);
        mRoomEditText.setSelection(roomName.length());
    }

    private void checkUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final UpdateInfo updateInfo = QNAppServer.getInstance().getUpdateInfo();
                if (updateInfo != null && updateInfo.getVersion() > Utils.appVersion(getApplicationContext())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showUpdateDialog(updateInfo.getDescription(), updateInfo.getDownloadURL());
                        }
                    });
                }
            }
        }).start();
    }

    private void showUpdateDialog(String content, final String downloadUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.rtc_android_auto_update_dialog_title);
        builder.setMessage(Html.fromHtml(content))
                .setPositiveButton(R.string.rtc_android_auto_update_dialog_btn_download, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        createProgressDialog();
                        goToDownload(downloadUrl);
                    }
                })
                .setNegativeButton(R.string.rtc_android_auto_update_dialog_btn_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void createProgressDialog() {
        mProgressDialog = new ProgressDialog(RTCActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(getString(R.string.rtc_updating));
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    private void goToDownload(String downloadUrl) {
        Intent intent = new Intent(RTCActivity.this, DownloadService.class);
        intent.putExtra(Config.DOWNLOAD_URL, downloadUrl);
        startService(intent);
    }
}
