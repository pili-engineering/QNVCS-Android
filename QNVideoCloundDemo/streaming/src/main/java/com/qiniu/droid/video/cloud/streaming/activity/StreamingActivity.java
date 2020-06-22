package com.qiniu.droid.video.cloud.streaming.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.androidadvance.topsnackbar.TSnackbar;
import com.qiniu.droid.video.cloud.plugin.bytedance.ByteDancePluginManager;
import com.qiniu.droid.video.cloud.streaming.R;
import com.qiniu.droid.video.cloud.streaming.ui.CameraPreviewFrameView;
import com.qiniu.droid.video.cloud.streaming.ui.RotateLayout;
import com.qiniu.droid.video.cloud.streaming.utils.Config;
import com.qiniu.droid.video.cloud.streaming.utils.QNAppServer;
import com.qiniu.droid.video.cloud.streaming.utils.StreamingSettings;
import com.qiniu.droid.video.cloud.streaming.utils.ToastUtils;
import com.qiniu.pili.droid.streaming.AVCodecType;
import com.qiniu.pili.droid.streaming.CameraStreamingSetting;
import com.qiniu.pili.droid.streaming.MediaStreamingManager;
import com.qiniu.pili.droid.streaming.StreamStatusCallback;
import com.qiniu.pili.droid.streaming.StreamingPreviewCallback;
import com.qiniu.pili.droid.streaming.StreamingProfile;
import com.qiniu.pili.droid.streaming.StreamingSessionListener;
import com.qiniu.pili.droid.streaming.StreamingState;
import com.qiniu.pili.droid.streaming.StreamingStateChangedListener;
import com.qiniu.pili.droid.streaming.SurfaceTextureCallback;

import java.net.URISyntaxException;
import java.util.List;

import static com.qiniu.pili.droid.streaming.AVCodecType.HW_VIDEO_SURFACE_AS_INPUT_WITH_HW_AUDIO_CODEC;

public class StreamingActivity extends AppCompatActivity {
    public static final String TAG = "StreamingActivity";

    private static final int MESSAGE_ID_RECONNECTING = 0x01;
    private TextView mLogText;
    private ImageButton mToggleLightButton;
    private CameraPreviewFrameView mCameraPreviewFrameView;

    private MediaStreamingManager mMediaStreamingManager;
    private AVCodecType mCodecType;
    private CameraStreamingSetting mCameraStreamingSetting;
    private StreamingProfile mStreamingProfile;
    private RotateLayout mRotateLayout;

    private boolean mIsInReadyState;
    private boolean mIsActivityPaused = true;
    private boolean mIsQuicEnabled = false;
    private boolean mIsLightOn = false;
    private boolean mIsFaceBeautyOn = false;
    private int mCurrentZoom = 0;
    private int mMaxZoom = 0;
    private int mCurrentCamFacingIndex;
    private int mEncodingWidth;
    private int mEncodingHeight;
    private String mRoomName;

    private boolean mIsByteDanceFaceBeauty = true;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MESSAGE_ID_RECONNECTING || mIsActivityPaused) {
                return;
            }
            if (!QNAppServer.isNetworkAvailable(StreamingActivity.this)) {
                sendReconnectMessage();
                return;
            }
            Log.d(TAG, "do reconnecting ...");
            mMediaStreamingManager.startStreaming();
        }
    };
    private StreamingStateChangedListener mStreamingStateChangedListener = new StreamingStateChangedListener() {
        @Override
        public void onStateChanged(final StreamingState state, Object o) {
            switch (state) {
                case PREPARING:
                    Log.d(TAG, "onStateChanged state:" + "preparing");
                    break;
                case READY:
                    mIsInReadyState = true;
                    mMaxZoom = mMediaStreamingManager.getMaxZoom();
                    mMediaStreamingManager.startStreaming();
                    Log.d(TAG, "onStateChanged state:" + "ready");
                    break;
                case CONNECTING:
                    Log.d(TAG, "onStateChanged state:" + "connecting");
                    break;
                case STREAMING:
                    Log.d(TAG, "onStateChanged state:" + "streaming");
                    break;
                case SHUTDOWN:
                    mIsInReadyState = true;
                    Log.d(TAG, "onStateChanged state:" + "shutdown");
                    break;
                case UNKNOWN:
                    Log.d(TAG, "onStateChanged state:" + "unknown");
                    break;
                case SENDING_BUFFER_EMPTY:
                    Log.d(TAG, "onStateChanged state:" + "sending buffer empty");
                    break;
                case SENDING_BUFFER_FULL:
                    Log.d(TAG, "onStateChanged state:" + "sending buffer full");
                    break;
                case OPEN_CAMERA_FAIL:
                    Log.d(TAG, "onStateChanged state:" + "open camera failed");
                    showToast(getString(R.string.streaming_failed_open_camera), Toast.LENGTH_SHORT);
                    break;
                case AUDIO_RECORDING_FAIL:
                    Log.d(TAG, "onStateChanged state:" + "audio recording failed");
                    showToast(getString(R.string.streaming_failed_open_microphone), Toast.LENGTH_SHORT);
                    break;
                case IOERROR:
                    /**
                     * Network-connection is unavailable when `startStreaming`.
                     * You can do reconnecting or just finish the streaming
                     */
                    Log.d(TAG, "onStateChanged state:" + "io error");
                    showToast(getString(R.string.streaming_streaming_io_error), Toast.LENGTH_SHORT);
                    sendReconnectMessage();
                    break;
                case DISCONNECTED:
                    /**
                     * Network-connection is broken after `startStreaming`.
                     * You can do reconnecting in `onRestartStreamingHandled`
                     */
                    Log.d(TAG, "onStateChanged state:" + "disconnected");
                    showToast(getString(R.string.streaming_disconnected), Toast.LENGTH_SHORT);
                    // we will process this state in `onRestartStreamingHandled`
                    break;
            }
        }
    };
    private StreamingSessionListener mStreamingSessionListener = new StreamingSessionListener() {
        @Override
        public boolean onRecordAudioFailedHandled(int code) {
            return false;
        }

        /**
         * When the network-connection is broken, StreamingState#DISCONNECTED will notified first,
         * and then invoked this method if the environment of restart streaming is ready.
         *
         * @return true means you handled the event; otherwise, given up and then StreamingState#SHUTDOWN
         * will be notified.
         */
        @Override
        public boolean onRestartStreamingHandled(int code) {
            Log.d(TAG, "onRestartStreamingHandled, reconnect ...");
            return mMediaStreamingManager.startStreaming();
        }

        @Override
        public Camera.Size onPreviewSizeSelected(List<Camera.Size> list) {
            for (Camera.Size size : list) {
                if (size.height >= 480) {
                    return size;
                }
            }
            return null;
        }

        @Override
        public int onPreviewFpsSelected(List<int[]> list) {
            return -1;
        }
    };
    private StreamStatusCallback mStreamStatusCallback = new StreamStatusCallback() {
        @Override
        public void notifyStreamStatusChanged(final StreamingProfile.StreamStatus streamStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String stat = String.format(getString(R.string.streaming_streaming_log_text),
                            mIsQuicEnabled ? getString(R.string.streaming_QUIC_protocol) : getString(R.string.streaming_TCP_protocol),
                            mEncodingWidth, mEncodingHeight, streamStatus.videoBitrate / 1024, streamStatus.audioBitrate / 1024,
                            streamStatus.videoFps, streamStatus.audioFps);
                    mLogText.setText(stat);
                }
            });
        }
    };
    private CameraPreviewFrameView.Listener mCameraPreviewListener = new CameraPreviewFrameView.Listener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i(TAG, "onSingleTapUp X:" + e.getX() + ",Y:" + e.getY());
            if (mIsInReadyState) {
                setFocusAreaIndicator();
                mMediaStreamingManager.doSingleTapUp((int) e.getX(), (int) e.getY());
                return true;
            }
            return false;
        }

        @Override
        public boolean onZoomValueChanged(float factor) {
            if (mIsInReadyState && mMediaStreamingManager.isZoomSupported()) {
                mCurrentZoom = (int) (mMaxZoom * factor);
                mCurrentZoom = Math.min(mCurrentZoom, mMaxZoom);
                mCurrentZoom = Math.max(0, mCurrentZoom);
                Log.d(TAG, "zoom ongoing, scale: " + mCurrentZoom + ",factor:" + factor + ",maxZoom:" + mMaxZoom);
                mMediaStreamingManager.setZoomValue(mCurrentZoom);
            }
            return false;
        }
    };
    /**
     * ################################ 特效相关 ###################################
     */

    private ByteDancePluginManager mEffectManager;
    /**
     * ———————————————————————————————— 特效处理相关 ————————————————————————————————————
     */

    SurfaceTextureCallback mSurfaceTextureCallback = new SurfaceTextureCallback() {
        private int count = 0;

        @Override
        public void onSurfaceCreated() {
            mEffectManager.init(mCameraPreviewFrameView);
        }

        @Override
        public void onSurfaceChanged(int width, int height) {
            count = 0;
        }

        @Override
        public void onSurfaceDestroyed() {
            mEffectManager.destroy();
        }

        @Override
        public int onDrawFrame(int texId, int width, int height, float[] transformMatrix) {
            if (mMediaStreamingManager.isPictureStreaming()) {
                return texId;
            }
            //跳过两帧是为了等待 OpenGL 环境的初始化完成，之后可以正常处理纹理
            if (count < 2) {
                count++;
                return texId;
            }
            // 返回的纹理格式为 2D
            return mEffectManager.processTexture(texId, width, height, true);
        }
    };
    StreamingPreviewCallback mStreamingPreviewCallback = new StreamingPreviewCallback() {
        @Override
        public boolean onPreviewFrame(final byte[] data, final int width, final int height, int rotation, int fmt, final long tsInNanoTime) {
            mEffectManager.updateRotation(rotation, mCurrentCamFacingIndex == CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT.ordinal());
            if (mCodecType != AVCodecType.SW_VIDEO_WITH_SW_AUDIO_CODEC
                    && mCodecType != AVCodecType.HW_VIDEO_YUV_AS_INPUT_WITH_HW_AUDIO_CODEC) {
                //非软编,不处理
                return false;
            }
            if (mMediaStreamingManager.isPictureStreaming()) {
                return false;
            }
            mEffectManager.processBuffer(data);
            return true;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.streaming_activity_streaming);

        mLogText = (TextView) findViewById(R.id.log_text);
        mToggleLightButton = (ImageButton) findViewById(R.id.toggle_light_button);
        mCameraPreviewFrameView = (CameraPreviewFrameView) findViewById(R.id.camera_preview_surfaceview);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.streaming_app_name), MODE_PRIVATE);
        mRoomName = preferences.getString(StreamingSettings.STREAMING_ROOMNAME, "");
        mIsQuicEnabled = preferences.getBoolean(StreamingSettings.QUIC_ENABLE, false);

        CameraStreamingSetting.CAMERA_FACING_ID facingId = chooseCameraFacingId();
        mCurrentCamFacingIndex = facingId.ordinal();

        /**
         * config camera & microphone settings
         */
        mCameraStreamingSetting = new CameraStreamingSetting();
        mCameraStreamingSetting.setCameraFacingId(facingId)
                .setContinuousFocusModeEnabled(true)
                .setRecordingHint(false)
                .setResetTouchFocusDelayInMs(3000)
                .setFocusMode(CameraStreamingSetting.FOCUS_MODE_CONTINUOUS_PICTURE)
                .setCameraPrvSizeLevel(CameraStreamingSetting.PREVIEW_SIZE_LEVEL.MEDIUM)
                .setCameraPrvSizeRatio(CameraStreamingSetting.PREVIEW_SIZE_RATIO.RATIO_16_9)
                .setPreviewAdaptToEncodingSize(false)
                .setBuiltInFaceBeautyEnabled(mIsFaceBeautyOn)
                .setFaceBeautySetting(new CameraStreamingSetting.FaceBeautySetting(0.8f, 0.8f, 0.6f))
                .setVideoFilter(CameraStreamingSetting.VIDEO_FILTER_TYPE.VIDEO_FILTER_BEAUTY); // set the beauty on/off

        mCodecType = preferences.getBoolean(StreamingSettings.SW_ENABLE, false)
                ? AVCodecType.SW_VIDEO_WITH_SW_AUDIO_CODEC : AVCodecType.HW_VIDEO_SURFACE_AS_INPUT_WITH_HW_AUDIO_CODEC;

        mMediaStreamingManager = new MediaStreamingManager(getApplicationContext(), mCameraPreviewFrameView, mCodecType);
        mMediaStreamingManager.setStreamingStateListener(mStreamingStateChangedListener);
        mMediaStreamingManager.setStreamingSessionListener(mStreamingSessionListener);
        mMediaStreamingManager.setStreamStatusCallback(mStreamStatusCallback);

        mStreamingProfile = new StreamingProfile();
        try {
            mStreamingProfile.setPublishUrl(getIntent().getStringExtra(Config.STREAMING_URL));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mStreamingProfile.setAudioQuality(StreamingProfile.AUDIO_QUALITY_MEDIUM1)
                .setEncoderRCMode(preferences.getBoolean(StreamingSettings.QUALITY_PRIORITY_ENABLE, true) ?
                        StreamingProfile.EncoderRCModes.QUALITY_PRIORITY : StreamingProfile.EncoderRCModes.BITRATE_PRIORITY)
                .setFpsControllerEnable(true)
                .setQuicEnable(mIsQuicEnabled)
                .setYuvFilterMode(StreamingSettings.YUV_FILTER_MODE_MAPPING[getIntent().getIntExtra("yuvFilterMode", 0)])
                .setSendingBufferProfile(new StreamingProfile.SendingBufferProfile(0.2f, 0.8f, 3.0f, 20 * 1000))
                .setBitrateAdjustMode(preferences.getBoolean(StreamingSettings.AUTO_BITRATE_ENABLED, true)
                        ? StreamingProfile.BitrateAdjustMode.Auto : StreamingProfile.BitrateAdjustMode.Disable);

        // set the video quality
        if (preferences.getBoolean(StreamingSettings.VIDEO_QUALITY_PREBUILT_ENABLE, true)) {
            mStreamingProfile.setVideoQuality(StreamingSettings.PREBUILT_VIDEO_QUALITY[preferences.getInt(StreamingSettings.PREBUILT_VIDEO_QUALITY_POS, 3)]);
        } else {
            StreamingProfile.AudioProfile aProfile = new StreamingProfile.AudioProfile(44100, 48 * 1024);
            StreamingProfile.VideoProfile vProfile = new StreamingProfile.VideoProfile(preferences.getInt(StreamingSettings.TARGET_FPS, 20),
                    preferences.getInt(StreamingSettings.TARGET_BITRATE, 1000) * 1024,
                    preferences.getInt(StreamingSettings.TARGET_GOP, 60),
                    StreamingProfile.H264Profile.HIGH);
            StreamingProfile.AVProfile avProfile = new StreamingProfile.AVProfile(vProfile, aProfile);
            mStreamingProfile.setAVProfile(avProfile);
        }

        // set the encoding size
        if (preferences.getBoolean(StreamingSettings.CODEC_SIZE_PREBUILT_ENABLE, true)) {
            int prebuiltCodecSizePos = preferences.getInt(StreamingSettings.PREBUILT_CODEC_SIZE_POS, 1);
            mStreamingProfile.setEncodingSizeLevel(StreamingSettings.PREBUILT_CODEC_SIZE[prebuiltCodecSizePos]);
            mEncodingWidth = StreamingSettings.CODEC_SIZE[prebuiltCodecSizePos][0];
            mEncodingHeight = StreamingSettings.CODEC_SIZE[prebuiltCodecSizePos][1];
        } else {
            mEncodingWidth = preferences.getInt(StreamingSettings.TARGET_WIDTH, 480);
            mEncodingHeight = preferences.getInt(StreamingSettings.TARGET_HEIGHT, 848);
            mStreamingProfile.setPreferredVideoEncodingSize(mEncodingWidth, mEncodingHeight);
        }

        mEffectManager = new ByteDancePluginManager(this, mCodecType != HW_VIDEO_SURFACE_AS_INPUT_WITH_HW_AUDIO_CODEC);
        if (mIsByteDanceFaceBeauty) {
            // 设置纹理回调
            mMediaStreamingManager.setSurfaceTextureCallback(mSurfaceTextureCallback);
            // 设置 YUV 回调
            mMediaStreamingManager.setStreamingPreviewCallback(mStreamingPreviewCallback);
        }
        mMediaStreamingManager.prepare(mCameraStreamingSetting, null, null, mStreamingProfile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsActivityPaused = false;
        mMediaStreamingManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsActivityPaused = true;
        mMediaStreamingManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaStreamingManager.destroy();
    }

    public void onClickLogButton(View v) {
        mLogText.setVisibility(mLogText.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
    }

    public void onClickClose(View v) {
        mMediaStreamingManager.stopStreaming();
        finish();
    }

    public void onClickSwitchCamera(View v) {
        mCurrentCamFacingIndex = (mCurrentCamFacingIndex + 1) % CameraStreamingSetting.getNumberOfCameras();
        CameraStreamingSetting.CAMERA_FACING_ID facingId;
        if (mCurrentCamFacingIndex == CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_BACK.ordinal()) {
            facingId = CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_BACK;
        } else if (mCurrentCamFacingIndex == CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT.ordinal()) {
            facingId = CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT;
        } else {
            facingId = CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD;
        }
        Log.i(TAG, "switchCamera:" + facingId);
        mMediaStreamingManager.switchCamera(facingId);
    }

    public void onClickCopyRoomName(View v) {
        copyToClipboard(mRoomName);
        TSnackbar snackBar = TSnackbar.make(findViewById(R.id.streaming_layout),
                String.format(getString(R.string.streaming_copy_to_clipboard), mRoomName), TSnackbar.LENGTH_SHORT);
        View snackView = snackBar.getView();
        snackView.setBackgroundColor(getResources().getColor(R.color.streaming_backgroundEndColor));
        TextView textView = (TextView) snackView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        snackBar.show();
    }

    public void onClickToggleLight(View v) {
        if (mCurrentCamFacingIndex == CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT.ordinal()) {
            ToastUtils.s(StreamingActivity.this, getString(R.string.streaming_cannot_toggle_light_in_front));
            return;
        }
        mIsLightOn = !mIsLightOn;
        if (mIsLightOn) {
            mMediaStreamingManager.turnLightOn();
        } else {
            mMediaStreamingManager.turnLightOff();
        }
        mToggleLightButton.setImageResource(mIsLightOn ? R.mipmap.streaming_light_off : R.mipmap.streaming_light_on);
    }

    public void onClickToggleFaceBeauty(View v) {
        if (mIsByteDanceFaceBeauty) {
            mEffectManager.showPanel();
        } else {
            mIsFaceBeautyOn = !mIsFaceBeautyOn;
            mMediaStreamingManager.setVideoFilterType(mIsFaceBeautyOn ?
                    CameraStreamingSetting.VIDEO_FILTER_TYPE.VIDEO_FILTER_BEAUTY
                    : CameraStreamingSetting.VIDEO_FILTER_TYPE.VIDEO_FILTER_NONE);
        }
    }

    protected void setFocusAreaIndicator() {
        if (mRotateLayout == null) {
            mRotateLayout = (RotateLayout) findViewById(R.id.focus_indicator_rotate_layout);
            mMediaStreamingManager.setFocusAreaIndicator(mRotateLayout, mRotateLayout.findViewById(R.id.focus_indicator));
        }
    }

    private CameraStreamingSetting.CAMERA_FACING_ID chooseCameraFacingId() {
        if (CameraStreamingSetting.hasCameraFacing(CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD)) {
            return CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD;
        } else if (CameraStreamingSetting.hasCameraFacing(CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT)) {
            return CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT;
        } else {
            return CameraStreamingSetting.CAMERA_FACING_ID.CAMERA_FACING_BACK;
        }
    }

    private void copyToClipboard(String content) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData clipData = ClipData.newPlainText("Label", content);
            cm.setPrimaryClip(clipData);
        }
    }

    private void showToast(final String text, final int duration) {
        if (mIsActivityPaused) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (duration == Toast.LENGTH_SHORT) {
                    ToastUtils.s(StreamingActivity.this, text);
                } else {
                    ToastUtils.l(StreamingActivity.this, text);
                }
            }
        });
    }

    private void sendReconnectMessage() {
        showToast(getString(R.string.streaming_reconnecting), Toast.LENGTH_SHORT);
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_ID_RECONNECTING), 500);
    }


}
