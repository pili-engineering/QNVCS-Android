package com.qiniu.droid.video.cloud.plugin.bytedance.fragment.contract.presenter;

import com.qiniu.bytedanceplugin.ByteDancePlugin;
import com.qiniu.bytedanceplugin.model.FilterModel;
import com.qiniu.droid.video.cloud.plugin.bytedance.fragment.contract.FilterContract;

import java.util.ArrayList;
import java.util.List;

public class FilterPresenter extends FilterContract.Presenter {
    private List<FilterModel> mItems = new ArrayList<>();

    @Override
    public List<FilterModel> getItems() {
        if (mItems.size() == 0) {
            mItems.add(new FilterModel().setDisplayName("正常"));
            mItems.addAll(ByteDancePlugin.getFilterList());
        }
        return mItems;
    }
}
