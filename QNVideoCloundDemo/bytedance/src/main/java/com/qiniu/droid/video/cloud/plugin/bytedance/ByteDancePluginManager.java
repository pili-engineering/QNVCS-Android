package com.qiniu.droid.video.cloud.plugin.bytedance;

import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.qiniu.bytedanceplugin.ByteDancePlugin;
import com.qiniu.bytedanceplugin.model.ComposerMode;
import com.qiniu.bytedanceplugin.model.ProcessType;
import com.qiniu.droid.video.cloud.plugin.bytedance.ui.ByteDanceDialog;
import com.qiniu.droid.video.cloud.plugin.bytedance.utils.LoadResourcesTask;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

public class ByteDancePluginManager {


    private FragmentActivity mActivity;
    private ByteDancePlugin mByteDancePlugin;
    // 特效处理列表，其中存储的是将纹理、YUV 转正所需要进行的处理
    private volatile CopyOnWriteArrayList<ProcessType> mProcessTypes;
    private int mRotation;
    private boolean mIsCameraFront;
    private Handler mHandler;
    private GLSurfaceView mGLThreadPoster;

    private ByteDanceDialog mEffectDialog;

    public ByteDancePluginManager(FragmentActivity activity, boolean enableProcessYUV) {
        mActivity = activity;
        mByteDancePlugin = new ByteDancePlugin(activity, ByteDancePlugin.PluginType.record);
        mByteDancePlugin.setYUVProcessEnabled(enableProcessYUV);
        mProcessTypes = new CopyOnWriteArrayList<>();
        new LoadResourcesTask(mActivity.getApplicationContext()).execute();
    }

    public void init(GLSurfaceView glSurfaceView) {
        //此路径为之前拷贝资源的地址
        if (glSurfaceView != null) {
            mGLThreadPoster = glSurfaceView;
        } else {
            mHandler = new Handler();
        }
        final String resourcePath = mActivity.getExternalFilesDir("assets") + File.separator + "resource";
        mByteDancePlugin.init(resourcePath);
        mByteDancePlugin.setComposerMode(ComposerMode.SHARE);
        mByteDancePlugin.onSurfaceCreated();
    }

    public int processTexture(int texId, int width, int height, boolean isOES) {
        return mByteDancePlugin.onDrawFrame(texId, width, height, System.nanoTime(), mProcessTypes, isOES);
    }

    public void processBuffer(final byte[] data) {
        mByteDancePlugin.processData(data);
    }

    public void destroy() {
        mByteDancePlugin.onSurfaceDestroy();
    }

    public void showPanel() {
        synchronized (this) {
            if (!LoadResourcesTask.isResourceReady(mActivity)) {
                Toast.makeText(mActivity, "资源准备中，请稍后再试", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mEffectDialog == null) {
                if (mHandler == null) {
                    mEffectDialog = new ByteDanceDialog(mByteDancePlugin, mGLThreadPoster);
                } else {
                    mEffectDialog = new ByteDanceDialog(mByteDancePlugin, mHandler);
                }
            }
            mEffectDialog.showDialog(mActivity.getSupportFragmentManager(), null);
        }
    }

    /**
     * 根据传入的旋转角度和摄像头的前后置情况对纹理、YUV 所需要进行的处理进行改变
     */
    public void updateRotation(int rotation, boolean isCameraFront) {
        if (mRotation == rotation && mIsCameraFront == isCameraFront) {
            return;
        }
        mRotation = rotation;
        mIsCameraFront = isCameraFront;
        switch (rotation) {
            case 0:
                addProcessType(ProcessType.ROTATE_0);
                break;
            case 90:
                addProcessType(ProcessType.ROTATE_90);
                break;
            case 180:
                addProcessType(ProcessType.ROTATE_180);
                break;
            case 270:
                addProcessType(ProcessType.ROTATE_270);
                break;
            default:
                addProcessType(ProcessType.ROTATE_0);
                break;
        }
        if (mIsCameraFront) {
            mProcessTypes.add(ProcessType.FLIPPED_HORIZONTAL);
        }
    }

    /**
     * 根据当前摄像头的前后置状态为 mProcessTypes 变量添加不同的 processType
     */
    private void addProcessType(ProcessType processType) {
        mProcessTypes.clear();
        mProcessTypes.add(processType);
    }


}
