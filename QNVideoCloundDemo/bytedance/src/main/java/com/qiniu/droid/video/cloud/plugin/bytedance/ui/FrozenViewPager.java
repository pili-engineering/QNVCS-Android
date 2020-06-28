package com.qiniu.droid.video.cloud.plugin.bytedance.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class FrozenViewPager extends ViewPager {
    private boolean isScroll = false;

    public FrozenViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FrozenViewPager(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isScroll) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isScroll) {
            return super.onTouchEvent(ev);
        } else {
            return true;
        }
    }
}
