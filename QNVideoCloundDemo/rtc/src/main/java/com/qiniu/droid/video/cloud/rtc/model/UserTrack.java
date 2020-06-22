package com.qiniu.droid.video.cloud.rtc.model;

import com.qiniu.droid.rtc.QNTrackInfo;
import com.qiniu.droid.rtc.model.QNMergeTrackOption;
import com.qiniu.droid.video.cloud.rtc.utils.QNAppServer;

public class UserTrack {

    private final String mTrackId;
    private final QNTrackInfo mQNTrackInfo;
    private final QNMergeTrackOption mQNMergeTrackOption;
    private boolean mTrackInclude = true;

    public UserTrack(QNTrackInfo QNTrackInfo) {
        mQNTrackInfo = QNTrackInfo;
        mTrackId = mQNTrackInfo.getTrackId();

        mQNMergeTrackOption = new QNMergeTrackOption();
        mQNMergeTrackOption.setWidth(QNAppServer.STREAMING_WIDTH);
        mQNMergeTrackOption.setHeight(QNAppServer.STREAMING_HEIGHT);
        mQNMergeTrackOption.setTrackId(mTrackId);
    }

    public String getTrackId() {
        return mTrackId;
    }

    public QNTrackInfo getQNTrackInfo() {
        return mQNTrackInfo;
    }

    public QNMergeTrackOption getQNMergeTrackOption() {
        return mQNMergeTrackOption;
    }

    public boolean isTrackInclude() {
        return mTrackInclude;
    }

    public void setTrackInclude(boolean trackInclude) {
        mTrackInclude = trackInclude;
    }

    public void updateQNMergeTrackOption(QNMergeTrackOption option) {
        if (option == null) {
            return;
        }
        mQNMergeTrackOption.setX(option.getX());
        mQNMergeTrackOption.setY(option.getY());
        mQNMergeTrackOption.setZ(option.getZ());
        mQNMergeTrackOption.setWidth(option.getWidth());
        mQNMergeTrackOption.setHeight(option.getHeight());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserTrack) {
            return mQNTrackInfo.equals(((UserTrack) obj).mQNTrackInfo);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return mQNTrackInfo.hashCode();
    }
}
