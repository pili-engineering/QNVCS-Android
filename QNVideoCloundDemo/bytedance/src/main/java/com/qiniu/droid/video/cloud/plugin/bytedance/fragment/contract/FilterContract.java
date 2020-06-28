package com.qiniu.droid.video.cloud.plugin.bytedance.fragment.contract;

import com.qiniu.bytedanceplugin.model.FilterModel;
import com.qiniu.droid.video.cloud.plugin.bytedance.base.BasePresenter;
import com.qiniu.droid.video.cloud.plugin.bytedance.base.IView;

import java.util.List;

public interface FilterContract {
    interface View extends IView {

    }

    abstract class Presenter extends BasePresenter<View> {
        public abstract List<FilterModel> getItems();
    }
}
