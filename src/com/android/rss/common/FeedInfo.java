package com.android.rss.common;

import java.util.Date;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class FeedInfo implements Parcelable {

    public int feedId;
    public int widgetId;
    public String feedTitle;
    public String feedUrl;
    public String feedOriTitle;
    public int feedIsBundle;
    public String feedIcon;
    public long feedPubdate;
    public String feedGuid;
    public int feedState;
    public Bitmap feedBitmap;
    public String feedIconState;
    public int itemCount;
    public String feedClass;
    public String feedClassGuid;

    @Override
    public String toString() {
        String pubDate = "";
        Date date = new Date(feedPubdate);
        pubDate = date.toLocaleString();
        String out = "feedId = " + feedId + " , feedTitle = " + feedTitle
                + " , widgetId = " + widgetId + " , feedUrl = " + feedUrl
                + " , feedIcon = " + feedIcon + " , feedIsBundle = "
                + feedIsBundle + " , feedPubdate = " + pubDate
                + " , feedGuid = " + feedGuid + " , feedClass = " + feedClass;
        return out;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(feedId);
        dest.writeInt(widgetId);
        dest.writeString(feedTitle);
        dest.writeString(feedUrl);
    }

    public static final Parcelable.Creator<FeedInfo> CREATOR = new Creator<FeedInfo>() {

        public FeedInfo createFromParcel(Parcel source) {
/*            Log.i("dd", "createFromParcel:" + source);*/
            FeedInfo info = new FeedInfo();
            info.feedId = source.readInt();
            info.widgetId = source.readInt();
            info.feedTitle = source.readString();
            info.feedUrl = source.readString();
            return info;
        }

        public FeedInfo[] newArray(int size) {
            return new FeedInfo[size];
        }

    };
    public void reset(){
        feedId = -1;
        widgetId = -1;
        feedTitle = null;
        feedUrl = null;
        feedOriTitle = null;
        feedIsBundle = 0;
        feedIcon = null;
        feedPubdate = 0;
        feedGuid = null;
        feedState = 0;
        feedBitmap = null;
        feedIconState = null;
    }
}
