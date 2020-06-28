package com.qiniu.droid.video.cloud.plugin.bytedance.fragment.contract;

import android.util.SparseArray;

import com.qiniu.droid.video.cloud.plugin.bytedance.base.BasePresenter;
import com.qiniu.droid.video.cloud.plugin.bytedance.base.IView;
import com.qiniu.droid.video.cloud.plugin.bytedance.model.ComposerNode;


public interface EffectContract {

    interface View extends IView {

    }

    abstract class Presenter extends BasePresenter<View> {

        abstract public void removeNodesOfType(SparseArray<ComposerNode> composerNodeMap, int type);

        abstract public void removeProgressInMap(SparseArray<Float> progressMap, int type);

        abstract public String[] generateComposerNodes(SparseArray<ComposerNode> composerNodeMap);

        abstract public void generateDefaultBeautyNodes(SparseArray<ComposerNode> composerNodeMap);

        abstract public boolean hasIntensity(int type);

        abstract public boolean isHairType(int id);
    }
}
