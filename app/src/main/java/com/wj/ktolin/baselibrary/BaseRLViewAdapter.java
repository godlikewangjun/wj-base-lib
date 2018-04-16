package com.wj.ktolin.baselibrary;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

/**
 * 基础recyerlistview适配器
 *
 * @author Admin
 * @version 1.0
 * @date 2017/4/15
 */

public abstract class BaseRLViewAdapter extends RecyclerView.Adapter {
    private RecyListViewOnItemClick recyListViewOnItemClick;
    public RequestManager requestManager;

    /**
     * 设置点击事件
     */
    public BaseRLViewAdapter setRecyListViewOnItemClick(RecyListViewOnItemClick recyListViewOnItemClick) {
        this.recyListViewOnItemClick = recyListViewOnItemClick;
        return this;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (requestManager == null) {
            requestManager = Glide.with(parent.getContext());
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyListViewOnItemClick != null) {
                    recyListViewOnItemClick.onItemClick(view, position);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
