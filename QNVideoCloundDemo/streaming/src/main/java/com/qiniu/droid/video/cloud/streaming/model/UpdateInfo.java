package com.qiniu.droid.video.cloud.streaming.model;

public class UpdateInfo {
    private String mAppID;
    private int mVersion;
    private String mDescription;
    private String mDownloadURL;
    private String mCreateTime;

    public String getAppID() {
        return mAppID;
    }

    public void setAppID(String appID) {
        this.mAppID = appID;
    }

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(int version) {
        this.mVersion = version;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getDownloadURL() {
        return mDownloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.mDownloadURL = downloadURL;
    }

    public String getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(String createTime) {
        this.mCreateTime = createTime;
    }
}
