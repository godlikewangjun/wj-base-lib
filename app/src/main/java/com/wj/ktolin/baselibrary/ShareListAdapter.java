package com.wj.ktolin.baselibrary;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abase.util.AbDoubleTool;

import java.util.List;

/**
 * 分享文章的列表
 * @author Admin
 * @version 1.0
 * @date 2017/5/5
 */

public class ShareListAdapter extends BaseRLViewAdapter{
    private List<ShareWzModel> sharesActivities;

    public ShareListAdapter(List<ShareWzModel> sharesActivities) {
        this.sharesActivities = sharesActivities;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       super.onCreateViewHolder(parent, viewType);
        return new ShareListAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_listitem_layout, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ShareListAdapter.ViewHolder viewHolder= (ViewHolder) holder;
        viewHolder.textView.setText(sharesActivities.get(position).getTitle());
        requestManager.load(sharesActivities.get(position).getIcon()).into(viewHolder.image);
        if(sharesActivities.get(position).getReadcount()>=10000){
            viewHolder.text.setText(AbDoubleTool.div(sharesActivities.get(position).getReadcount(),1000)+"万阅读");
        }else{
            viewHolder.text.setText(sharesActivities.get(position).getReadcount()+"阅读");
        }
        viewHolder.textView1.setText(sharesActivities.get(position).getPrice()+"");
    }

    @Override
    public int getItemCount() {
        return sharesActivities.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView,textView1,text;
        public ImageView image,imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            textView= (TextView) itemView.findViewById(R.id.textView);
            textView1= (TextView) itemView.findViewById(R.id.textView1);
            text=(TextView) itemView.findViewById(R.id.text);
            image= (ImageView) itemView.findViewById(R.id.image);
            imageView=(ImageView) itemView.findViewById(R.id.imageView);
        }
    }
}
