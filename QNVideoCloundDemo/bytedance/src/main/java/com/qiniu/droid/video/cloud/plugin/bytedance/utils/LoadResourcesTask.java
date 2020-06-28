package com.qiniu.droid.video.cloud.plugin.bytedance.utils;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class LoadResourcesTask extends AsyncTask<Void, Void, Boolean> {

    public static final String KEY_SP = "byte_dance";
    public static final String KEY_RESOURCE_READY = "resource_ready";
    public static final int V_RESOURCE_NOT_READY = 0;
    public static final int V_RESOURCE_COPYING = 1;
    public static final int V_RESOURCE_READY = 2;
    private static final String DST_FOLDER = "resource";
    private WeakReference<Context> mWeakContextRef;

    public LoadResourcesTask(Context context) {
        mWeakContextRef = new WeakReference<>(context);
    }

    public static boolean isResourceReady(Context context) {
        return context.getSharedPreferences(KEY_SP, Context.MODE_PRIVATE).getInt(KEY_RESOURCE_READY, V_RESOURCE_NOT_READY) == V_RESOURCE_READY;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        synchronized (LoadResourcesTask.class) {
            if (!isResourceReady(mWeakContextRef.get())) {
                setResourceCopying();
            }
        }
    }

    @Override
    protected Boolean doInBackground(Void... strings) {
        synchronized (LoadResourcesTask.class) {
            if (isResourceReady(mWeakContextRef.get())) {
                return true;
            }
            String path = DST_FOLDER;
            File dstFile = mWeakContextRef.get().getExternalFilesDir("assets");
            FileUtils.clearDir(new File(dstFile, path));
            try {
                FileUtils.copyAssets(mWeakContextRef.get().getAssets(), path, dstFile.getAbsolutePath());
                setResourceReady();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private void setResourceReady() {
        mWeakContextRef.get().getSharedPreferences(KEY_SP, Context.MODE_PRIVATE).edit().putInt(KEY_RESOURCE_READY, V_RESOURCE_READY).apply();
    }

    private void setResourceCopying() {
        mWeakContextRef.get().getSharedPreferences(KEY_SP, Context.MODE_PRIVATE).edit().putInt(KEY_RESOURCE_READY, V_RESOURCE_COPYING).apply();
    }

}
