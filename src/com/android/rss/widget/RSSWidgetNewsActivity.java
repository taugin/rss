package com.android.rss.widget;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.rss.R;
import com.android.rss.RSSWebView;
import com.android.rss.common.ItemInfo;
import com.android.rss.common.PreferenceKeys;
import com.android.rss.common.Constant;
import com.android.rss.util.Log;
import com.android.rss.util.NetworkUtil;

public class RSSWidgetNewsActivity extends Activity implements OnClickListener{

    private static final String TAG = "RSSNewsActivity";
    private TextView mFeedTitle;
    private WebView mCurWebView;
    private ImageView mFeedIcon;
    private TextView mNewsTitle;
    private TextView mPubDate;
    private AlertDialog mTitleTipDialog;
    private ItemInfo mItemInfo = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_widget_news_preview);
        
        Intent intent = getIntent();
        int itemId = intent.getIntExtra(Constant.Content.EXTRA_NEWS_ID, -1);
        int feedId = intent.getIntExtra(Constant.Content.EXTRA_FEED_ID, -1);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setCustomView(R.layout.rss_widget_news_title);
        View customView = actionBar.getCustomView();
        mNewsTitle = (TextView) customView.findViewById(R.id.news_title);
        mFeedIcon = (ImageView) customView.findViewById(R.id.feed_icon);
        mFeedTitle = (TextView) findViewById(R.id.feed_title);
        mPubDate = (TextView) findViewById(R.id.news_pubdate);
        mCurWebView = (WebView) findViewById(R.id.webview_news);
        mCurWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        mNewsTitle.setOnClickListener(this);
        mItemInfo = new ItemInfo();
        fetchFeedInfo(feedId);
        fetchItemInfo(itemId);
        show();
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
//              WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int itemId = intent.getIntExtra(Constant.Content.EXTRA_NEWS_ID, -1);
        int feedId = intent.getIntExtra(Constant.Content.EXTRA_FEED_ID, -1);
        fetchFeedInfo(feedId);
        fetchItemInfo(itemId);
        show();
    }


    private void fetchFeedInfo(int feedId){
        Cursor c = null;
        String selection = Constant.Content._ID + "=" + feedId;
        String []projection = new String[]{Constant.Content.FEED_TITLE, 
                Constant.Content.FEED_ICON, Constant.Content.FEED_URL, 
                Constant.Content.FEED_ORI_TITLE, };
        try {
            c = getContentResolver().query(Constant.Content.FEED_URI, 
                    projection, selection, null, null);
            if(c != null){
                if(c.moveToFirst()){
                    final String title = c.getString(c.getColumnIndex(Constant.Content.FEED_TITLE));
                    String oriTitle = c.getString(c.getColumnIndex(Constant.Content.FEED_ORI_TITLE));
                    byte[] image = c.getBlob(c.getColumnIndex(Constant.Content.FEED_ICON));
                    mItemInfo.feedUrl = c.getString(c.getColumnIndex(Constant.Content.FEED_URL));
                    Bitmap bitmap = null;
                    if(image != null){
                        bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);                            
                    }else{
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rss);
                    }
                    if(TextUtils.isEmpty(title)){
                        mItemInfo.feedTitle = oriTitle;
                    }else{
                        mItemInfo.feedTitle = title;
                    }
                    mItemInfo.feedIcon = bitmap;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(c != null){
                c.close();
            }
        }
    }
    private void fetchItemInfo(int itemId){
        String selection = Constant.Content._ID + "="
                + itemId;
        String orderBy = Constant.Content.ITEM_PUBDATE + " desc";
        String []projection = new String[]{Constant.Content._ID, Constant.Content.FEED_ID, 
                Constant.Content.ITEM_TITLE,Constant.Content.ITEM_URL, Constant.Content.ITEM_DESCRIPTION,
                Constant.Content.ITEM_PUBDATE, Constant.Content.ITEM_STATE};
        Cursor itemCursor = null;
        String temp = null;
        try {
            itemCursor = getContentResolver().query(Constant.Content.ITEM_URI, 
                    projection, selection, null, orderBy);
            if (itemCursor != null) {
                if (itemCursor.moveToFirst()) {
                    mItemInfo.itemId = itemCursor.getInt(itemCursor.getColumnIndex(Constant.Content._ID));
                    mItemInfo.feedId = itemCursor.getInt(itemCursor.getColumnIndex(Constant.Content.FEED_ID));
                    mItemInfo.itemTitle = itemCursor.getString(itemCursor.getColumnIndex(Constant.Content.ITEM_TITLE));
                    mItemInfo.itemUrl = itemCursor.getString(itemCursor.getColumnIndex(Constant.Content.ITEM_URL));
                    temp = itemCursor.getString(itemCursor.getColumnIndex(Constant.Content.ITEM_DESCRIPTION));
                    if(temp == null) {
                        mItemInfo.itemDescription = "";
                    } else {
                        mItemInfo.itemDescription = temp;
                    }
                    mItemInfo.itemState = itemCursor.getInt(itemCursor.getColumnIndex(Constant.Content.ITEM_STATE));
                    mItemInfo.itemPubdate = itemCursor.getLong(itemCursor.getColumnIndex(Constant.Content.ITEM_PUBDATE));
                } else {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (itemCursor != null) {
                itemCursor.close();
            }
        }
    }
    private void show(){
        mFeedTitle.setText(mItemInfo.feedTitle);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date(mItemInfo.itemPubdate));
        mNewsTitle.setText(mItemInfo.itemTitle);
        mFeedIcon.setVisibility(View.VISIBLE);
        mFeedIcon.setImageBitmap(mItemInfo.feedIcon);
        long currentTime = 0;
        long thisTime = 0;
        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");        
        try {
            currentTime = formatter.parse(formatter.format(today)).getTime();
            thisTime = formatter.parse(formatter.format(new Date(mItemInfo.itemPubdate))).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(currentTime == thisTime){
            sdf = new SimpleDateFormat ("MM-dd HH:mm");
            mPubDate.setText(sdf.format(new Date(mItemInfo.itemPubdate)));
        }else{
            sdf = new SimpleDateFormat ("yyyy-MM-dd");
            mPubDate.setText(sdf.format(new Date(mItemInfo.itemPubdate)));
        }
//        LogUtils.debug(TAG, "details = " + details);
        String baseUrl = null;
        String feedUrl = null;
        try {
            if(mItemInfo.feedUrl != null){
                if(mItemInfo.feedUrl.startsWith("http://") || mItemInfo.feedUrl.startsWith("https://")){
                    feedUrl = mItemInfo.feedUrl;
                }else{
                    feedUrl = "http://";
                    feedUrl += mItemInfo.feedUrl;
                }
            }
            if(feedUrl != null){
                URL url = new URL(feedUrl);
                String protocol = url.getProtocol();
                String host = url.getHost();
                baseUrl = protocol + "://" + host;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        mCurWebView.loadDataWithBaseURL(baseUrl, customWebView(mItemInfo.itemDescription), "text/html", "utf-8", null);
        if(mItemInfo.itemState == Constant.State.STATE_ITEM_UNREAD){
            mItemInfo.itemState = Constant.State.STATE_ITEM_READ;
            updateItemState(mItemInfo);
        }
    }

    @Override
    protected void onDestroy() {
        File file = getCacheDir();
        deleteFile(file);
        deleteDatabase("webview.db");
        deleteDatabase("webview.db-shm");
        deleteDatabase("webview.db-wal");
        deleteDatabase("webviewCookiesChromium.db");
        deleteDatabase("webviewCookiesChromium.db-journal");
        deleteDatabase("webviewCookiesChromiumPrivate.db");
        super.onDestroy();
    }
    private void updateItemState(ItemInfo info){
        ContentValues values = new ContentValues();
        String where = Constant.Content._ID + "=" + info.itemId;
        values.put(Constant.Content.ITEM_STATE, info.itemState);
        getContentResolver().update(Constant.Content.ITEM_URI, values, where, null);
    }
    private void deleteFile(File file){
        if(file != null && file.exists() && file.isDirectory()){
            for(File f : file.listFiles()){
                Log.e(TAG, "F = " + f);
                if(f.isFile()){
                    f.delete();
                }else{
                    deleteFile(f);
                }
            }
            file.delete();
        }else{
            if(file != null){
                file.delete();
            }
        }
    }
    private String customWebView(String str){
        if(str == null){
            str = getResources().getString(R.string.empty_article_detail);
        }
        if(str != null && str.trim().equals("")){
            str = getResources().getString(R.string.empty_article_detail);
        }
        str = str.replaceAll("&gt;", ">");
        str = str.replaceAll("&lt;", "<");
        str = str.replaceAll("&amp;", "&");
        str = str.replaceAll("&quot;", "\"");
        str = str.replaceAll("&nbsp;", " ");
        str = str.replaceAll("<!--.*?-->", "<br>");
        str = str.replaceAll("<script.*?>", "<br>");
        str = str.replaceAll("<link.*?>", "<br>");
        str = didShowPictures(str);
        return str;
    }

    private String didShowPictures(String str){
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showPictures = preference.getBoolean(PreferenceKeys.KEY_SHOW_PICTURES, false);
        if(!showPictures){
            str = str.replaceAll("<img.*?>", "");
            return str;
        }
        boolean onlyShowPictureInWifi = preference.getBoolean(PreferenceKeys.KEY_PICTURE_ONLY_WIFI, false);
        if(onlyShowPictureInWifi){
            NetworkInfo networkInfo = NetworkUtil.getCurrentNetwork(this);
            if(networkInfo == null || networkInfo.getType() != ConnectivityManager.TYPE_WIFI){
                str = str.replaceAll("<img.*?>", "");
            }
        }
        return str;
    }

    @Override
    public void onClick(View v) {
        Animation animation = null;
        int id = v.getId();
        switch(id){
        case R.id.news_title:
            if(mTitleTipDialog == null){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                mTitleTipDialog = builder.create();
            }
            mTitleTipDialog.setMessage(mNewsTitle.getText());
            mTitleTipDialog.show();
            break;
        case R.id.button_close:
            finish();
            break;
        case R.id.view_original:
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mItemInfo.itemUrl));
            boolean innerBrowser = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceKeys.KEY_INNER_BROWSER, false);
            if(innerBrowser){
                intent.setComponent(new ComponentName(this, RSSWebView.class));
            }
            startActivity(intent);
            break;
        default:
            break;
        }
    }
}
