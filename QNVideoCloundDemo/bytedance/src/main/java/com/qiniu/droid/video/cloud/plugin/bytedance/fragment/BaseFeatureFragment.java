package com.qiniu.droid.video.cloud.plugin.bytedance.fragment;


import com.qiniu.droid.video.cloud.plugin.bytedance.base.BaseFragment;
import com.qiniu.droid.video.cloud.plugin.bytedance.base.IPresenter;

/**
 * 每个功能 fragemnt 的基类
 *
 * @param <T>
 */
public abstract class BaseFeatureFragment<T extends IPresenter, Callback> extends BaseFragment<T> {
    private Callback mCallback;

    public Callback getCallback() {
        return mCallback;
    }

    public BaseFeatureFragment setCallback(Callback t) {
        this.mCallback = t;
        return this;
    }
}
