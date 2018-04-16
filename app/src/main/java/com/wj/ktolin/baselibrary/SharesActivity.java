package com.wj.ktolin.baselibrary;


import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;

import com.abase.okhttp.OhHttpClient;
import com.abase.okhttp.OhObjectListener;
import com.abase.util.AbAppUtil;
import com.abase.util.AbStrUtil;
import com.abase.util.Tools;
import com.abase.view.parent.BaseActivity;
import com.abase.view.weight.RecyclerSpace;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Headers;
import okhttp3.RequestBody;

/**
 * @author Admin
 * @version 1.0
 * @date 2018/1/5
 */

public class SharesActivity extends BaseActivity implements RecyListViewOnItemClick {
    private RecyclerView recycler_list;
    private LinearLayoutManager linearLayoutManager;
    private ShareListAdapter shareListAdapter;//文章列表
    private int page = 1, endCount;//页数 结束文章的数量
    private List<ShareWzModel> sharesActivities = new ArrayList<>();
    private Handler handler = new Handler();

    @Override
    public int setContentView() {
        return R.layout.activity_sharelayout;
    }

    @Override
    public void afertOp() {
        title_content.setText("分享红包");
        if (getPackageName().toString().contains("hixiu")) {
            lin_back.setVisibility(View.GONE);
        } else {
            backto.setText("返回");
        }
        other.setText("如何赚钱");
        other_icon.setVisibility(View.GONE);
        setThemeColor(android.R.color.holo_red_dark);

        handler.postDelayed(runnable, 8 * 1000);
        init();


    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 初始化
     */
    private void init() {
        recycler_list = (RecyclerView) findViewById(R.id.recycler_list);
        linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        recycler_list.setLayoutManager(linearLayoutManager);
        recycler_list.addItemDecoration(new RecyclerSpace(5));

        recycler_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange()) {//加载更多
                    page += 1;
                }
            }
        });

        bindData();
        getAd();
    }

    private void getAd() {
        OhHttpClient ohHttpClient=OhHttpClient.getInit();
        ohHttpClient.setJsonFromMat(true);
        String url="http://192.168.0.158:7074/mobile_v2/taskwall/getad?is_new_user=1";
        HashMap<String,String> map=new HashMap<>();
        map.put("LM-ART-SIGN",spellSing(url));
        map.put("LM-ART-TID","90b9edc3-413a-43d3-a886-ff257680910e");
        map.put("LM-ART-DID","862179037250577");
        map.put("LM-ART-TOKEN","LzwV67Eu9UavxhVMqf4S0GfQRwpICXHy");
        map.put("YQX-Header","{\"device\":\"862179037250577\",\"sign\":\"39912d44d060fe885c684684f8dae70c\",\"token\":\"\",\"packname\":\"com.mx.morehb\",\"timestamp\":\"1515143985\",\"channel\":\"\",\"user\":\"\",\"code\":\"\",\"openid\":\"\",XHY:\"4G\"}");
        map.put("device", AbAppUtil.getDeviceId(activity));
        ohHttpClient.setHeaders(Headers.of(map)).get(url,new OhObjectListener<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFailure(int code, String content, Throwable error) {
            }

            @Override
            public void onSuccess(String content) {
                JSONObject jsonObject = JSON.parseObject(content);
                if (jsonObject.getInteger("state") == 1) {
                    if(sharesActivities.size()>0){
                        sharesActivities.addAll(JSON.parseArray(jsonObject.getJSONObject("data").getString("adlist"), ShareWzModel.class));
                        shareListAdapter.notifyDataSetChanged();
                    }else{
                        sharesActivities.addAll(JSON.parseArray(jsonObject.getJSONObject("data").getString("adlist"), ShareWzModel.class));
                        shareListAdapter = new ShareListAdapter(sharesActivities);
                        recycler_list.setAdapter(shareListAdapter);
                        shareListAdapter.setRecyListViewOnItemClick(SharesActivity.this);
                    }
                }
            }

            @Override
            public void onFinish() {

            }
        });
    }

    /**
     * 绑定数据
     */
    private void bindData() {
        OhHttpClient ohHttpClient=OhHttpClient.getInit();
        ohHttpClient.setJsonFromMat(true);
        String url="http://192.168.0.158:7074/mobile_v2/taskwall/getarticle?pageindex=1&pagesize=10&is_new_user=1&device=868617024702229&appid=90b9edc3-413a-43d3-a886-ff257680910e&appcode=90b9edc3-413a-43d3-a886-ff257680910e&user=12&token=7c2ac9d1-2f46-49dd-81d1-f44e73452c0e&LM-ART-SIGN=97e0242e13ebd954c5f515ae148a83ad";
        HashMap<String,String> map=new HashMap<>();
        map.put("LM-ART-SIGN",spellSing(url));
        map.put("LM-ART-TID","90b9edc3-413a-43d3-a886-ff257680910e");
        map.put("LM-ART-DID","862179037250577");
        map.put("LM-ART-TOKEN","LzwV67Eu9UavxhVMqf4S0GfQRwpICXHy");
        map.put("YQX-Header","{\"device\":\"862179037250577\",\"sign\":\"39912d44d060fe885c684684f8dae70c\",\"token\":\"\",\"packname\":\"com.mx.morehb\",\"timestamp\":\"1515143985\",\"channel\":\"\",\"user\":\"\",\"code\":\"\",\"openid\":\"\",XHY:\"WIFI/4G\"}");
        map.put("device", AbAppUtil.getDeviceId(activity));
        ohHttpClient.setHeaders(Headers.of(map)).get(url,new OhObjectListener<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFailure(int code, String content, Throwable error) {
            }

            @Override
            public void onSuccess(String content) {
                JSONObject jsonObject = JSON.parseObject(content);
                if (jsonObject.getInteger("state") == 1) {
                    if(sharesActivities.size()>0){
                        sharesActivities.addAll(JSON.parseArray(jsonObject.getString("data"), ShareWzModel.class));
                        shareListAdapter.notifyDataSetChanged();
                    }else{
                        sharesActivities.addAll(JSON.parseArray(jsonObject.getString("data"), ShareWzModel.class));
                        shareListAdapter = new ShareListAdapter(sharesActivities);
                        recycler_list.setAdapter(shareListAdapter);
                        shareListAdapter.setRecyListViewOnItemClick(SharesActivity.this);
                    }
                }
            }

            @Override
            public void onFinish() {

            }
        });
    }

    private String spellSing(String url){
        StringBuilder str= new StringBuilder();
        if(url.contains("?")){
            String[] keyValues=url.split("\\?")[1].split("&");
            for (int i=0;i<keyValues.length;i++){
                if(i!=keyValues.length-1){
                    str.append(keyValues[i].split("=")[1]).append("|");
                }else if(i==keyValues.length-1){
                    str.append(keyValues[i].split("=")[1]);
                }
            }
        }

        System.out.println(str+"=====请求的body");
//        System.out.println(str+"---------"+ InitData.keyid+"---------"+Configs.SERVICE_TOKEN);
        return Tools.setMD5(str+ "90b9edc3-413a-43d3-a886-ff257680910e"+"7c2ac9d1-2f46-49dd-81d1-f44e73452c0e");
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (sharesActivities.size() > 0) {
            shareListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(activity, ShareDetail.class);
        intent.putExtra("data", sharesActivities.get(position));
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
