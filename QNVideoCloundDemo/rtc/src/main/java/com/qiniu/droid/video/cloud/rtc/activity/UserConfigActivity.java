package com.qiniu.droid.video.cloud.rtc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.qiniu.droid.video.cloud.rtc.R;
import com.qiniu.droid.video.cloud.rtc.utils.Config;
import com.qiniu.droid.video.cloud.rtc.utils.PermissionChecker;
import com.qiniu.droid.video.cloud.rtc.utils.ToastUtils;


public class UserConfigActivity extends AppCompatActivity {

    private static final String TAG = "UserConfigActivity";

    private EditText mUsernameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rtc_activity_user_config);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mUsernameEditText = (EditText) findViewById(R.id.user_name_edit_text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onClickNext(View view) {
        saveUserName();
    }

    @Override
    public void onBackPressed() {
        saveUserName();
    }

    private boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.l(this, "Some permissions is not approved !!!");
        }
        return isPermissionOK;
    }

    private void saveUserName() {
        final String userName = mUsernameEditText.getText().toString();
        if (userName == null || userName.isEmpty()) {
            ToastUtils.s(this, getString(R.string.rtc_null_user_name_toast));
            return;
        }
        if (!RTCActivity.isUserNameOk(userName)) {
            ToastUtils.s(this, getString(R.string.rtc_wrong_user_name_toast));
            return;
        }
        if (!isPermissionOK()) {
            return;
        }

        Intent intent = new Intent(this, RTCActivity.class);
        intent.putExtra(Config.USER_NAME, userName);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
