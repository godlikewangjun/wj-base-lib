package com.wj.ktolin.baselibrary;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * @author Admin
 * @version 1.0
 * @date 2017/5/9
 */

public class ShareWzModel implements Parcelable {

    /**
     * code : 6e53b891-ea67-4432-80db-25ec382af62f
     * title : 测试文章
     * sharetitle : null
     * icon : http://down.handzb.com/api/file/GetSource/c3755990e32041fd8d7387df9218aa15?buskey=67f777d627394fae8a62fb5917046e3a&buscode=0e863d3893054d718fc43c20e01a454f&timespan=1515162941&sign=0a5b8327a6f4830dc1cdd6bcbeca6586
     * shareicon : null
     * lable : 阿斯蒂芬
     * readcount : 100
     * contype : 1
     * price : 1.0
     * shareurl : null
     * detailurl : http://qwerty.eiigv.cnAd/InArticle/ArticleDetail?user=489E8ED4F6B34EA78AB12E0AB3048851&code=6e53b891-ea67-4432-80db-25ec382af62f&group=4
     * desc : null
     * platforms : ["2","3"]
     */

    private String code;
    private String title;
    private String sharetitle;
    private String icon;
    private String shareicon;
    private String lable;
    private int readcount;
    private int contype;
    private double price;
    private String shareurl;
    private String detailurl;
    private String desc;
    private List<String> platforms;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLable() {
        return lable;
    }

    public void setLable(String lable) {
        this.lable = lable;
    }

    public int getReadcount() {
        return readcount;
    }

    public void setReadcount(int readcount) {
        this.readcount = readcount;
    }

    public int getContype() {
        return contype;
    }

    public void setContype(int contype) {
        this.contype = contype;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSharetitle(String sharetitle) {
        this.sharetitle = sharetitle;
    }

    public void setShareicon(String shareicon) {
        this.shareicon = shareicon;
    }

    public void setShareurl(String shareurl) {
        this.shareurl = shareurl;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDetailurl() {
        return detailurl;
    }

    public void setDetailurl(String detailurl) {
        this.detailurl = detailurl;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    public ShareWzModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.title);
        dest.writeString(this.sharetitle);
        dest.writeString(this.icon);
        dest.writeString(this.shareicon);
        dest.writeString(this.lable);
        dest.writeInt(this.readcount);
        dest.writeInt(this.contype);
        dest.writeDouble(this.price);
        dest.writeString(this.shareurl);
        dest.writeString(this.detailurl);
        dest.writeString(this.desc);
        dest.writeStringList(this.platforms);
    }

    protected ShareWzModel(Parcel in) {
        this.code = in.readString();
        this.title = in.readString();
        this.sharetitle = in.readString();
        this.icon = in.readString();
        this.shareicon = in.readString();
        this.lable = in.readString();
        this.readcount = in.readInt();
        this.contype = in.readInt();
        this.price = in.readDouble();
        this.shareurl = in.readString();
        this.detailurl = in.readString();
        this.desc = in.readString();
        this.platforms = in.createStringArrayList();
    }

    public static final Creator<ShareWzModel> CREATOR = new Creator<ShareWzModel>() {
        @Override
        public ShareWzModel createFromParcel(Parcel source) {
            return new ShareWzModel(source);
        }

        @Override
        public ShareWzModel[] newArray(int size) {
            return new ShareWzModel[size];
        }
    };
}
