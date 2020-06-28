package com.qiniu.droid.video.cloud.player;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qiniu.droid.video.cloud.streaming.utils.Config;
import com.qiniu.droid.video.cloud.streaming.utils.QNAppServer;
import com.qiniu.droid.video.cloud.streaming.utils.StreamingSettings;
import com.qiniu.droid.video.cloud.streaming.utils.ToastUtils;


public class PlayerAddressActivity extends AppCompatActivity {
    private static final String URL_REGEX = "(rtmp|http)://[-a-zA-Z0-9._?=/%&+~:]+";
    private static final String ROOM_NAME_REGEX = "[-a-zA-Z0-9_]+";

    private EditText mAddressConfigEditText;
    private Button mStartLivingButton;

    private String mPlayingUrl;
    private View.OnClickListener mOnStartLivingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!QNAppServer.isNetworkAvailable(PlayerAddressActivity.this)) {
                ToastUtils.s(PlayerAddressActivity.this, getString(R.string.player_network_disconnected));
                return;
            }
            final String roomName = mAddressConfigEditText.getText().toString().trim();
            if ("".equals(roomName)) {
                ToastUtils.s(PlayerAddressActivity.this, getString(R.string.player_null_room_name_toast));
                return;
            }
            SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
            editor.putString(StreamingSettings.PLAYING_ROOMNAME, roomName);
            editor.apply();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (roomName.matches(ROOM_NAME_REGEX)) {
                        mPlayingUrl = QNAppServer.getInstance().requestPlayUrl(roomName);
                    } else if (roomName.matches(URL_REGEX)) {
                        mPlayingUrl = roomName;
                    } else {
                        mPlayingUrl = null;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mPlayingUrl == null) {
                                ToastUtils.s(PlayerAddressActivity.this,
                                        roomName.matches(ROOM_NAME_REGEX)
                                                ? getString(R.string.player_get_url_failed)
                                                : getString(R.string.player_illegal_play_url));
                                return;
                            }
                            Intent intent = new Intent(PlayerAddressActivity.this, PlayingActivity.class);
                            intent.putExtra(Config.PLAYING_URL, mPlayingUrl);
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

        setContentView(R.layout.player_activity_player_address);
        mAddressConfigEditText = (EditText) findViewById(R.id.address_config_edit_text);
        mStartLivingButton = (Button) findViewById(R.id.start_living_button);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String roomName = preferences.getString(StreamingSettings.PLAYING_ROOMNAME, "");
        mAddressConfigEditText.setText(roomName);

        mAddressConfigEditText.setHint(R.string.player_playing_mode_hint);
        mStartLivingButton.setText(R.string.player_playing_mode_button_text);

        mStartLivingButton.setOnClickListener(mOnStartLivingClickListener);
    }

    public void onClickBack(View v) {
        finish();
    }

}
