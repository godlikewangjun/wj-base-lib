package com.wj.ktolin.baselibrary.weight

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.wj.ktolin.baselibrary.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder

/**
 *
 * @author Administrator
 * @version 1.0
 * @date 2018/8/8/008
 */
class TestAdapter:BaseAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.share_listitem_layout,null))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
    }

    override fun getItemCount(): Int {
        return 18
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }
}