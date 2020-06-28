package com.qiniu.droid.video.cloud.streaming.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qiniu.droid.video.cloud.streaming.R;
import com.qiniu.droid.video.cloud.streaming.utils.Config;
import com.qiniu.droid.video.cloud.streaming.utils.PermissionChecker;
import com.qiniu.droid.video.cloud.streaming.utils.QNAppServer;
import com.qiniu.droid.video.cloud.streaming.utils.StreamingSettings;
import com.qiniu.droid.video.cloud.streaming.utils.ToastUtils;

public class StreamingAddressActivity extends AppCompatActivity {
    private static final String URL_REGEX = "(rtmp|http)://[-a-zA-Z0-9._?=/%&+~:]+";
    private static final String ROOM_NAME_REGEX = "[-a-zA-Z0-9_]+";

    private EditText mAddressConfigEditText;
    private Button mStartLivingButton;
    private RadioButton mProtocolRadioButton;
    private LinearLayout mProtocolLayout;

    private boolean mIsProtocolAgreed;
    private String mPublishUrl;
    private View.OnClickListener mOnStartLivingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isPermissionOK()) {
                return;
            }
            if (!QNAppServer.isNetworkAvailable(StreamingAddressActivity.this)) {
                ToastUtils.s(StreamingAddressActivity.this, getString(R.string.streaming_network_disconnected));
                return;
            }
            if (!mProtocolRadioButton.isChecked()) {
                ToastUtils.s(StreamingAddressActivity.this, getString(R.string.streaming_niuliving_protocol_tips));
                return;
            }
            final String roomName = mAddressConfigEditText.getText().toString().trim();
            if ("".equals(roomName)) {
                ToastUtils.s(StreamingAddressActivity.this, getString(R.string.streaming_null_room_name_toast));
                return;
            }
            SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.streaming_app_name), MODE_PRIVATE).edit();
            editor.putString(StreamingSettings.STREAMING_ROOMNAME, roomName);
            editor.apply();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (roomName.matches(ROOM_NAME_REGEX)) {
                        mPublishUrl = QNAppServer.getInstance().requestPublishUrl(roomName);
                    } else if (roomName.matches(URL_REGEX)) {
                        mPublishUrl = roomName;
                    } else {
                        mPublishUrl = null;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mPublishUrl == null) {
                                ToastUtils.s(StreamingAddressActivity.this,
                                        roomName.matches(ROOM_NAME_REGEX)
                                                ? getString(R.string.streaming_get_url_failed)
                                                : getString(R.string.streaming_illegal_publish_url));
                                return;
                            }
                            Intent intent = new Intent(StreamingAddressActivity.this, StreamingActivity.class);
                            intent.putExtra(Config.STREAMING_URL, mPublishUrl);
                            startActivity(intent);
                        }
                    });
                }
            }).start();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.streaming_activity_streaming_address);
        mAddressConfigEditText = (EditText) findViewById(R.id.address_config_edit_text);
        mStartLivingButton = (Button) findViewById(R.id.start_living_button);
        mProtocolLayout = (LinearLayout) findViewById(R.id.protocol_layout);
        mProtocolRadioButton = (RadioButton) findViewById(R.id.protocol_radio_button);
        mProtocolRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsProtocolAgreed = !mIsProtocolAgreed;
                mProtocolRadioButton.setChecked(mIsProtocolAgreed);
            }
        });

        mProtocolLayout.setVisibility(View.VISIBLE);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.streaming_app_name), MODE_PRIVATE);
        String roomName = preferences.getString(
                StreamingSettings.STREAMING_ROOMNAME, "");
        mAddressConfigEditText.setText(roomName);

        mAddressConfigEditText.setHint(R.string.streaming_streaming_mode_hint);
        mStartLivingButton.setText(R.string.streaming_streaming_mode_button_text);
        mStartLivingButton.setOnClickListener(mOnStartLivingClickListener);
    }

    public void onClickBack(View v) {
        finish();
    }

    public void onClickSetting(View v) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    public void onClickProtocol(View v) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.streaming_dialog_protocol);
        WebView webView = (WebView) dialog.findViewById(R.id.protocol_web_view);
        webView.loadUrl("file:///android_asset/user_declare.html");
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }

    private boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.l(this, "Some permissions is not approved !!!");
        }
        return isPermissionOK;
    }
}
