package com.qiniu.droid.video.cloud.plugin.bytedance.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.qiniu.bytedanceplugin.ByteDancePlugin;
import com.qiniu.droid.video.cloud.plugin.bytedance.R;
import com.qiniu.droid.video.cloud.plugin.bytedance.fragment.EffectFragment;
import com.qiniu.droid.video.cloud.plugin.bytedance.fragment.StickerFragment;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class ByteDanceDialog extends DialogFragment {

    private ByteDancePlugin mByteDancePlugin;
    private Handler mHandler;
    private GLSurfaceView mGLSurfaceView;
    private boolean mIsShowing;

    public ByteDanceDialog(ByteDancePlugin byteDancePlugin, Handler handler) {
        mByteDancePlugin = byteDancePlugin;
        mHandler = handler;
    }

    public ByteDanceDialog(ByteDancePlugin byteDancePlugin, GLSurfaceView glSurfaceView) {
        mByteDancePlugin = byteDancePlugin;
        mGLSurfaceView = glSurfaceView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    EffectFragment effectFragment = new EffectFragment();
                    effectFragment.setCallback(new EffectFragment.IEffectCallback() {

                        @Override
                        public void updateComposeNodes(final String[] nodes) {
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    mByteDancePlugin.setComposerNodes(nodes);
                                }
                            };
                            if (mGLSurfaceView != null) {
                                mGLSurfaceView.queueEvent(runnable);
                            } else {
                                mHandler.post(runnable);
                            }
                        }

                        @Override
                        public void updateComposeNodeIntensity(final String path, final String key, final float value) {
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    mByteDancePlugin.updateComposerNode(path, key, value);
                                }
                            };
                            if (mGLSurfaceView != null) {
                                mGLSurfaceView.queueEvent(runnable);
                            } else {
                                mHandler.post(runnable);
                            }
                        }

                        @Override
                        public void onFilterSelected(final String fileName) {
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    mByteDancePlugin.setFilter(fileName);
                                }
                            };
                            if (mGLSurfaceView != null) {
                                mGLSurfaceView.queueEvent(runnable);
                            } else {
                                mHandler.post(runnable);
                            }
                        }

                        @Override
                        public void onFilterValueChanged(final float value) {
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    mByteDancePlugin.updateFilterIntensity(value);
                                }
                            };
                            if (mGLSurfaceView != null) {
                                mGLSurfaceView.queueEvent(runnable);
                            } else {
                                mHandler.post(runnable);
                            }
                        }

                        @Override
                        public void setEffectOn(final boolean isOn) {
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    mByteDancePlugin.setEffectOn(isOn);
                                }
                            };
                            if (mGLSurfaceView != null) {
                                mGLSurfaceView.queueEvent(runnable);
                            } else {
                                mHandler.post(runnable);
                            }
                        }

                        @Override
                        public void onDefaultClick() {

                        }
                    });
                    return effectFragment;
                } else {
                    StickerFragment stickerFragment = new StickerFragment();
                    stickerFragment.setCallback(new StickerFragment.IStickerCallback() {
                        @Override
                        public void onStickerSelected(final String fileName) {
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    mByteDancePlugin.setSticker(fileName);
                                }
                            };
                            if (mGLSurfaceView != null) {
                                mGLSurfaceView.queueEvent(runnable);
                            } else {
                                mHandler.post(runnable);
                            }
                        }
                    });
                    return stickerFragment;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                if (position == 0) {
                    return "beauty";
                } else {
                    return "sticker";
                }
            }
        };

        TabLayout tabLayout = getView().findViewById(R.id.tab_layout);
        ViewPager viewPager = getView().findViewById(R.id.view_pager);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        WindowManager.LayoutParams params = getDialog().getWindow()
                .getAttributes();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        getDialog().getWindow().setAttributes(params);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setDimAmount(0);
        View mContentView = inflater.inflate(R.layout.dialog_byte_dance, container, false);
        return mContentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        getDialog().getWindow().setLayout(displayMetrics.widthPixels, displayMetrics.heightPixels / 2);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mIsShowing = false;
    }

    public void showDialog(FragmentManager fragmentManager, String tag) {
        if (mIsShowing) {
            return;
        }
        mIsShowing = true;
        showNow(fragmentManager, tag);
    }
}
