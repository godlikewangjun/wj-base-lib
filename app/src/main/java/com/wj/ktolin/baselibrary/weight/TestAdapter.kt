package com.wj.ktolin.baselibrary.weight

import android.media.MediaPlayer
import androidx.annotation.RequiresPermission
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.wj.ktolin.baselibrary.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.share_listitem_layout.view.*

/**
 *
 * @author Administrator
 * @version 1.0
 * @date 2018/8/8/008
 */
class TestAdapter : BaseAdapter() {
    private var choose=-1
    val hashMap=HashMap<Int,MediaPlayer>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.share_listitem_layout, parent, false))
    }

    @RequiresPermission("android.permission.RECORD_AUDIO")
    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource("http://www.ytmp3.cn/down/51447.mp3")
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            holder.itemView.play.setOnClickListener {
                holder.itemView.play.isSelected = !holder.itemView.play.isSelected
                if (holder.itemView.play.isSelected) {
                    holder.itemView.play.text = "播放"
                    choose=position
                    stopPlay(position)
                    mediaPlayer.start()
                } else if(mediaPlayer.isPlaying){
                    mediaPlayer.pause()
                    mediaPlayer.seekTo(0)
                    holder.itemView.play.text = "停止"
                }
            }
        }

        hashMap[position] = mediaPlayer
    }
    fun stopPlay(choose:Int){
       for (index in 0 until hashMap.size){
           val mode=hashMap[index]
           if(choose!=index && mode!!.isPlaying){
               mode.pause()
               mode.seekTo(0)
           }
       }
    }


    override fun getItemCount(): Int {
        return 6
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    private fun play() {

    }
}