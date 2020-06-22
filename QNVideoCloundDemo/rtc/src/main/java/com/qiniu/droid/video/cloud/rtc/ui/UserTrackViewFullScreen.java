package com.qiniu.droid.video.cloud.rtc.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qiniu.droid.video.cloud.rtc.R;


public class UserTrackViewFullScreen extends UserTrackView {

    public UserTrackViewFullScreen(@NonNull Context context) {
        super(context);
    }

    public UserTrackViewFullScreen(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayout() {
        return R.layout.rtc_user_tracks_view_full_screen;
    }
}
