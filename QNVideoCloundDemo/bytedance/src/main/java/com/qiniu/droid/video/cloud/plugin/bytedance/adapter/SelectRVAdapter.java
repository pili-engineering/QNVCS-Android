package com.qiniu.droid.video.cloud.plugin.bytedance.adapter;


import androidx.recyclerview.widget.RecyclerView;

abstract public class SelectRVAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected int mSelect;

    public int getSelect() {
        return mSelect;
    }

    public void setSelect(int select) {
        if (mSelect != select) {
            int oldSelect = mSelect;
            mSelect = select;
            notifyItemChanged(oldSelect);
            notifyItemChanged(select);
        }
    }
}
