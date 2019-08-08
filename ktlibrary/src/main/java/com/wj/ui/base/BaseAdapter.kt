package com.wj.ui.base

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wj.ui.interfaces.RecyerViewItemListener
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.wj.ui.base.viewhoder.CustomVhoder

/**
 * @author Admin
 * @version 1.0
 * @date 2018/1/2
 */

open class BaseAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    init {
        this.setHasStableIds(true)// 图片闪烁
    }
    var inflater: LayoutInflater? = null
    var context: Context? = null
    var glide: RequestManager? = null
    var onItemClickListener: RecyerViewItemListener?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        if (context == null) {
            context = parent.context
        }
        if (inflater == null) {
            inflater = LayoutInflater.from(context)
        }
        if (glide == null) {
            glide = Glide.with(context!!)
        }
        return CustomVhoder(parent)
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
