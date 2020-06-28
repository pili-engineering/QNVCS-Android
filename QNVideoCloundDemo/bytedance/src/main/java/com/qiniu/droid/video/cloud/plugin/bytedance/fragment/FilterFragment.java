package com.qiniu.droid.video.cloud.plugin.bytedance.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.bytedanceplugin.model.FilterModel;
import com.qiniu.droid.video.cloud.plugin.bytedance.R;
import com.qiniu.droid.video.cloud.plugin.bytedance.adapter.FilterRVAdapter;
import com.qiniu.droid.video.cloud.plugin.bytedance.fragment.contract.FilterContract;
import com.qiniu.droid.video.cloud.plugin.bytedance.fragment.contract.OnCloseListener;
import com.qiniu.droid.video.cloud.plugin.bytedance.fragment.contract.presenter.FilterPresenter;

public class FilterFragment extends BaseFeatureFragment<FilterContract.Presenter, FilterFragment.IFilterCallback>
        implements FilterRVAdapter.OnItemClickListener, OnCloseListener, FilterContract.View {
    private RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rv = (RecyclerView) View.inflate(getContext(), R.layout.fragment_filter, null);
        return rv;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPresenter(new FilterPresenter());

        FilterRVAdapter adapter = new FilterRVAdapter(mPresenter.getItems(), this);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter);
    }

    public void setSelect(int select) {
        ((FilterRVAdapter) rv.getAdapter()).setSelect(select);
    }

    public void setSelectItem(String fileName) {
        ((FilterRVAdapter) rv.getAdapter()).setSelectItem(fileName);
    }

    @Override
    public void onItemClick(FilterModel filterModel) {
        if (getCallback() == null) {
            return;
        }
        getCallback().onFilterSelected(filterModel);
    }

    @Override
    public void onClose() {
        ((FilterRVAdapter) rv.getAdapter()).setSelect(0);
    }

    public interface IFilterCallback {
        void onFilterSelected(FilterModel filterItem);
    }
}
