package com.qiniu.droid.video.cloud.plugin.bytedance.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.droid.video.cloud.plugin.bytedance.R;
import com.qiniu.droid.video.cloud.plugin.bytedance.model.ButtonItem;
import com.qiniu.droid.video.cloud.plugin.bytedance.ui.ButtonView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ButtonViewRVAdapter extends SelectRVAdapter<ButtonViewRVAdapter.ViewHolder> {
    private List<ButtonItem> mItemList;
    private OnItemClickListener mListener;
    private Set<Integer> mPointOnItems;

    public ButtonViewRVAdapter(List<ButtonItem> itemList, OnItemClickListener listener) {
        this(itemList, listener, 0);
    }

    public ButtonViewRVAdapter(List<ButtonItem> itemList, OnItemClickListener listener, int selectItem) {
        mItemList = itemList;
        mListener = listener;
        mSelect = selectItem;
        mPointOnItems = new HashSet<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_button_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ButtonItem item = mItemList.get(position);
        holder.bv.setIconPath(item.getIconPath());
        holder.bv.setTitle(item.getTitle());

        if (position == mSelect) {
            holder.bv.on();
        } else {
            holder.bv.off();
        }
        holder.bv.pointChange(mPointOnItems.contains(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelect(position);
                mListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public void setItemList(List<ButtonItem> itemList) {
        mItemList = itemList;
        notifyDataSetChanged();
    }

    public void onClose() {
        mPointOnItems.clear();
        mSelect = 0;
        notifyDataSetChanged();
    }

    public void onProgress(float progress, int id) {
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i).getNode().getId() == id) {
                if ((progress > 0 && mPointOnItems.add(i))
                        || (progress == 0 && mPointOnItems.remove(i))) {
                    notifyItemChanged(i);
                }
            }
        }
    }

    public void setSelectItem(int id) {
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i).getNode().getId() == id) {
                setSelect(i);
                return;
            }
        }
        setSelect(-1);
    }

    public interface OnItemClickListener {
        void onItemClick(ButtonItem item);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ButtonView bv;

        public ViewHolder(View itemView) {
            super(itemView);
            bv = (ButtonView) itemView;
        }
    }
}
