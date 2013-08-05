package com.android.rss.common;

import java.util.Date;

import android.graphics.Bitmap;

public class ItemInfo {

    public int itemId;
    public int feedId;
    public String itemTitle;
    public String itemUrl;
    public String itemDescription;
    public String itemGuid;
    public String itemAuthor;
    public long itemPubdate;
    public Bitmap feedIcon;
    public String feedTitle;
    public String feedUrl;
    public int itemState;
    public int itemCount;
    public int itemUnReadCount;

    @Override
    public String toString() {
        String pubDate = "";
        Date date = new Date(itemPubdate);
        pubDate = date.toLocaleString();
        String out = "itemId = " + itemId + " , itemTitle = " + itemTitle
                + " , itemUrl = " + itemUrl + " , itemGuid = " + itemGuid
                + " , itemAuthor = " + itemAuthor + " , itemPubdate = "
                + pubDate;

        return out;
    }

}
