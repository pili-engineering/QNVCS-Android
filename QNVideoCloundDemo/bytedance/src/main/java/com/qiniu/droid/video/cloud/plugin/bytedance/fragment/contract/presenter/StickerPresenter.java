package com.qiniu.droid.video.cloud.plugin.bytedance.fragment.contract.presenter;

import com.qiniu.bytedanceplugin.ByteDancePlugin;
import com.qiniu.bytedanceplugin.model.StickerModel;
import com.qiniu.droid.video.cloud.plugin.bytedance.fragment.contract.StickerContract;

import java.util.ArrayList;
import java.util.List;

public class StickerPresenter extends StickerContract.Presenter {
    private List<StickerModel> mStickerItems = new ArrayList<>();

    @Override
    public List<StickerModel> getStickersItems() {
        if (mStickerItems.size() == 0) {
            mStickerItems.add(0, new StickerModel());
            mStickerItems.addAll(ByteDancePlugin.getStickerList());
        }
        return mStickerItems;
    }
}
