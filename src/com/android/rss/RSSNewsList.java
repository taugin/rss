package com.android.rss;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.rss.common.ItemInfo;
import com.android.rss.common.PreferenceKeys;
import com.android.rss.common.Constant;
import com.android.rss.refreshlist.RefreshListView.OnRefreshListener;
import com.android.rss.util.Log;
import com.android.rss.util.NetworkUtil;

public class RSSNewsList extends ListActivity implements OnClickListener, 
        OnItemClickListener, OnDismissListener, 
        OnLongClickListener, OnTouchListener, OnRefreshListener, 
        AnimationListener{

    private static final boolean sHeaderRefresh = false;
    private static final String TAG = "NewsActivity";
    private static final String KEY_FEED_ID = "feedId";
    private static final int MENU_ITEM_CLEAR = 0;
    private static final int MENU_ITEM_SETTINGS = 1;
    private static final int MSG_INIT_NEWS = 0;
    private static final int MSG_CLEAR_ALL_NEWS = 1;
    private static final int MSG_UPDATE_NEWS_LIST = 2;
    private ListView mListView = null;
    private NewsHandler mNewsHandler = null;;
    private Handler mHandler = null;
    private ArrayList<ItemInfo> mItemList = null;
    private NewsAdapter mNewsAdapter = null;
    private int mCurPosition = -1;
    private TextView mFeedTitle;
    private WebView mWebViewBack;
    private WebView mWebViewFront;
    private WebView mCurWebView;
    private FrameLayout mNewLayout = null;
    private ImageView mFeedIcon;
    private TextView mNewsIndex;
    private TextView mNewsTitle;
    private TextView mPubDate;
    private int mFeedId;
    private boolean mDataLoading = false;
    private boolean mAnimationRunning = false;
    private AlertDialog mTitleTipDialog;
    private ImageButton mPreButton;
    private ImageButton mNextButton;
    private ImageButton mOriButton;
    private ImageButton mRefreshButton;
    private ProgressBar mProgressBar;
    private Feed mFeed;
    private boolean mRSSListUpdated;
    private ProgressBar mNewsProgress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.rss_news_layout);
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.rss_news_title);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setCustomView(R.layout.rss_news_title);
        View customView = actionBar.getCustomView();
        mNewsTitle = (TextView) customView.findViewById(R.id.news_title);
        mFeedIcon = (ImageView) customView.findViewById(R.id.feed_icon);
        mNewsIndex = (TextView) customView.findViewById(R.id.news_index);
        mNewsProgress = (ProgressBar) customView.findViewById(R.id.news_progress_refresh);
        mNewsTitle.setOnClickListener(null);
        mNewsTitle.setClickable(false);
        mRSSListUpdated = false;
        Intent intent = getIntent();
        int feedId = -1;
        if(intent != null){
            feedId = intent.getIntExtra(KEY_FEED_ID, -1);
        }
        mFeedId = feedId;
        Log.d(TAG ,"feedId = " + feedId);
        mItemList = new ArrayList<ItemInfo>();
        mNewLayout = (FrameLayout) findViewById(R.id.news_details);
        mPreButton = (ImageButton) findViewById(R.id.previous);
        mPreButton.setOnClickListener(this);
        mPreButton.setOnLongClickListener(this);
        mPreButton.setOnTouchListener(this);
        mOriButton = (ImageButton) findViewById(R.id.viewOriginal);
        mOriButton.setOnClickListener(this);
        mNextButton = (ImageButton) findViewById(R.id.next);
        mNextButton.setOnClickListener(this);
        mNextButton.setOnLongClickListener(this);
        mNextButton.setOnTouchListener(this);
        mNewLayout.setVisibility(View.VISIBLE);
        mNewLayout.setVisibility(View.INVISIBLE);
        mFeedTitle = (TextView) findViewById(R.id.feed_title);
        mPubDate = (TextView) findViewById(R.id.news_pubdate);
        mWebViewBack = (WebView) findViewById(R.id.fullscreen_webview_1);
        mWebViewFront = (WebView) findViewById(R.id.fullscreen_webview_2);
        mWebViewBack.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        mWebViewFront.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        mCurWebView = mWebViewFront;
        mCurWebView.setWebViewClient(mWebViewClient);
        String title = getResources().getString(R.string.articlelist);
        setTitle(title);
        mListView = (ListView) getListView();
        mListView.setOnItemClickListener(this);
        if(sHeaderRefresh){
            if(mFeedId != -1){
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View headerView = inflater.inflate(R.layout.rss_newslist_header, null);
                mRefreshButton = (ImageButton) headerView.findViewById(R.id.rss_news_header_refresh);
                mRefreshButton.setOnClickListener(this);
                mProgressBar = (ProgressBar) headerView.findViewById(R.id.process_refresh);
                int state = getFeedState(mFeedId);
                if(state == Constant.State.STATE_WAITING || state == Constant.State.STATE_SUBSCRIBING){
                    mRefreshButton.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                }else{
                    mRefreshButton.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
                mListView.addHeaderView(headerView);
            }
        }
        /**/
        int state = getFeedState(mFeedId);
        if(state == Constant.State.STATE_WAITING || state == Constant.State.STATE_SUBSCRIBING){
            mNewsIndex.setVisibility(View.INVISIBLE);
            mNewsProgress.setVisibility(View.VISIBLE);
        }else{
            mNewsIndex.setVisibility(View.VISIBLE);
            mNewsProgress.setVisibility(View.INVISIBLE);
        }
        /**/
        mNewsAdapter = new NewsAdapter(this, mItemList);
        mListView.setAdapter(mNewsAdapter);
        mHandler = new Handler();
        HandlerThread thread = new HandlerThread("RssAppSettings");
        thread.start();
        mNewsHandler = new NewsHandler(this, thread.getLooper());
        Message msg = mNewsHandler.obtainMessage(MSG_INIT_NEWS);
        mNewsHandler.sendMessage(msg);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.Intent.INTENT_RSSAPP_UPDATE_NEWS_LIST);
        filter.addAction(Constant.Intent.INTENT_RSSAPP_STARTREFRESH);
        registerReceiver(mNewRecevier, filter);
    }
    private void show(){
        ItemInfo info = mItemList.get(mCurPosition);
        mFeedTitle.setText(info.feedTitle);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date(info.itemPubdate));
        mNewsTitle.setText(info.itemTitle);
        mNewsIndex.setText((mCurPosition + 1) + "/" + mItemList.size());
        mFeedIcon.setVisibility(View.VISIBLE);
        mFeedIcon.setImageBitmap(info.feedIcon);
        long currentTime = 0;
        long thisTime = 0;
        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");        
        try {
            currentTime = formatter.parse(formatter.format(today)).getTime();
            thisTime = formatter.parse(formatter.format(new Date(info.itemPubdate))).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(currentTime == thisTime){
            sdf = new SimpleDateFormat ("MM-dd HH:mm");
            mPubDate.setText(sdf.format(new Date(info.itemPubdate)));
        }else{
            sdf = new SimpleDateFormat ("yyyy-MM-dd");
            mPubDate.setText(sdf.format(new Date(info.itemPubdate)));
        }
        String details = queryDetails(info.itemId);
//        LogUtils.debug(TAG, "details = " + details);
        String baseUrl = null;
        String feedUrl = null;
        try {
            if(info.feedUrl != null){
                if(info.feedUrl.startsWith("http://") || info.feedUrl.startsWith("https://")){
                    feedUrl = info.feedUrl;
                }else{
                    feedUrl = "http://";
                    feedUrl += info.feedUrl;
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
        mCurWebView.loadDataWithBaseURL(baseUrl, customWebView(details), "text/html", "utf-8", null);
        mListView.setSelection(mCurPosition);
        if(info.itemState == Constant.State.STATE_ITEM_UNREAD){
            info.itemState = Constant.State.STATE_ITEM_READ;
            updateItemState(info);
        }
        mNewsAdapter.notifyDataSetChanged();
    }
    private String queryDetails(int itemId){
        Cursor c = null;
        String selection = Constant.Content._ID + "=" + itemId;
        String projection[] = new String[]{Constant.Content.ITEM_DESCRIPTION};
        String details = null;
        try{
            c = getContentResolver().query(Constant.Content.ITEM_URI, projection, selection, null, null);
            if(c != null){
                if(c.moveToFirst()){
                    details = c.getString(c.getColumnIndex(Constant.Content.ITEM_DESCRIPTION));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            details = null;
            return details;
        }finally{
            if(c != null){
                c.close();
            }
        }
        return details;
    }
    class NewsAdder implements Runnable{
        private ItemInfo mNewsInfo;
        private boolean mInsert;
        public NewsAdder(ItemInfo info, boolean insert){
            mNewsInfo = info;
            mInsert = insert;
        }
        @Override
        public void run() {
            if(mInsert){
                mNewsAdapter.insert(mNewsInfo, 0);
            }else{
                mNewsAdapter.add(mNewsInfo);
            }
        }
    }
    class Feed{
        String feedTitle;
        Bitmap feedIcon;
        String feedUrl;
        int itemCount;
        int itemUnReadCount;
    }
    private class NewsHandler extends Handler{
        private Context mContext;
        public NewsHandler(Context context, Looper looper) {
            super(looper);
            mContext = context;
        }
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            mDataLoading = true;
            switch(what){
            case MSG_INIT_NEWS:
                init(mFeedId);
                break;
            case MSG_CLEAR_ALL_NEWS:
                if(mFeedId == -1){
                    mContext.getContentResolver().delete(Constant.Content.ITEM_URI, null, null);
                }else{
                    String where = Constant.Content.FEED_ID + " = " + mFeedId;
                    mContext.getContentResolver().delete(Constant.Content.ITEM_URI, where, null);
                    updateWidget();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mNewsAdapter.clear();
                        mNewsAdapter.notifyDataSetChanged();
                    }
                });
                break;
            case MSG_UPDATE_NEWS_LIST:
                mRSSListUpdated = true;
                queryNews(mFeedId, mFeed);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(sHeaderRefresh){
                            mRefreshButton.setVisibility(View.VISIBLE);
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                        mNewsIndex.setVisibility(View.VISIBLE);
                        mNewsProgress.setVisibility(View.INVISIBLE);
                        mNewsIndex.setText("" + mItemList.size());
                    }
                });
                mRSSListUpdated = false;
                break;
            default:
                break;
            }
            mDataLoading = false;
        }
        private void initTitleAndIcon(int feedId, Feed feed){
            Cursor c = null;
            String selection = Constant.Content._ID + "=" + feedId;
            String []projection = new String[]{Constant.Content.FEED_TITLE, 
                    Constant.Content.FEED_ICON, Constant.Content.FEED_URL, 
                    Constant.Content.FEED_ORI_TITLE, Constant.Content.ITEM_COUNT,
                    Constant.Content.ITEM_UNREAD_COUNT};
            try {
                c = mContext.getContentResolver().query(Constant.Content.FEED_URI, 
                        projection, selection, null, null);
                if(c != null){
                    if(c.moveToFirst()){
                        final String title = c.getString(c.getColumnIndex(Constant.Content.FEED_TITLE));
                        String oriTitle = c.getString(c.getColumnIndex(Constant.Content.FEED_ORI_TITLE));
                        byte[] image = c.getBlob(c.getColumnIndex(Constant.Content.FEED_ICON));
                        feed.feedUrl = c.getString(c.getColumnIndex(Constant.Content.FEED_URL));
                        feed.itemCount = c.getInt(c.getColumnIndex(Constant.Content.ITEM_COUNT));
                        feed.itemUnReadCount = c.getInt(c.getColumnIndex(Constant.Content.ITEM_UNREAD_COUNT));
                        Bitmap bitmap = null;
                        if(image != null){
                            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);                            
                        }else{
                            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.rss);
                        }
                        if(TextUtils.isEmpty(title)){
                            feed.feedTitle = oriTitle;
                        }else{
                            feed.feedTitle = title;
                        }
                        feed.feedIcon = bitmap;
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
        private void init(int feedId){
            if(feedId != -1){
                mFeed = new Feed();
                initTitleAndIcon(feedId, mFeed);
                final Bitmap icon = mFeed.feedIcon;
                final String title = mFeed.feedTitle;
                Log.d(TAG, "feedIcon = " + mFeed.feedIcon + " , feedTitle = " + mFeed.feedTitle);
                queryNews(feedId, mFeed);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        BitmapDrawable drawable = new BitmapDrawable(icon);
//                        setIcon(drawable);
                        setTitle(title);
                    }
                });
            }else{
                Cursor c = null;
                String projection[] = new String[]{Constant.Content._ID};
                try{
                    c = mContext.getContentResolver().query(Constant.Content.FEED_URI, projection, null, null, null);
                    if(c != null){
                        if(c.moveToFirst()){
                            Feed feed = new Feed();
                            do{
                                feedId = c.getInt(c.getColumnIndex(Constant.Content._ID));
                                feed.feedIcon = null;
                                feed.feedTitle = null;
                                initTitleAndIcon(feedId, feed);
                                queryNews(feedId, feed);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String title = getResources().getString(R.string.articlelist);
                                        setTitle(title);
                                    }
                                });
                            }while(c.moveToNext());
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
        }
        private void queryNews(int feedId, Feed feed){
            String selection = Constant.Content.FEED_ID + "="
                    + feedId;
            String orderBy = null;
            if(!mRSSListUpdated){
                orderBy = Constant.Content.ITEM_PUBDATE + " desc";
            }else{
                orderBy = Constant.Content.ITEM_PUBDATE + " asc";
            }
            String []projection = new String[]{Constant.Content._ID, Constant.Content.FEED_ID, 
                    Constant.Content.ITEM_TITLE,Constant.Content.ITEM_URL, Constant.Content.ITEM_DES_BRIEF,
                    Constant.Content.ITEM_PUBDATE, Constant.Content.ITEM_STATE, Constant.Content.ITEM_GUID};
            Cursor itemCursor = null;
            String temp = null;
            try {
                itemCursor = mContext.getContentResolver().query(Constant.Content.ITEM_URI, 
                        projection, selection, null, orderBy);
                if (itemCursor != null) {
                    if (itemCursor.moveToFirst()) {
                        do {
                            ItemInfo item = new ItemInfo();
                            item.itemId = itemCursor.getInt(itemCursor.getColumnIndex(Constant.Content._ID));
                            item.feedId = itemCursor.getInt(itemCursor.getColumnIndex(Constant.Content.FEED_ID));
                            item.itemTitle = itemCursor.getString(itemCursor.getColumnIndex(Constant.Content.ITEM_TITLE));
                            item.itemUrl = itemCursor.getString(itemCursor.getColumnIndex(Constant.Content.ITEM_URL));
                            temp = itemCursor.getString(itemCursor.getColumnIndex(Constant.Content.ITEM_DES_BRIEF));
                            if(temp == null) {
                                item.itemDescription = "";
                            } else {
                                item.itemDescription = temp;
                            }
                            item.itemState = itemCursor.getInt(itemCursor.getColumnIndex(Constant.Content.ITEM_STATE));
                            item.itemPubdate = itemCursor.getLong(itemCursor.getColumnIndex(Constant.Content.ITEM_PUBDATE));
                            item.itemGuid = itemCursor.getString(itemCursor.getColumnIndex(Constant.Content.ITEM_GUID));
                            item.feedIcon = feed.feedIcon;
                            item.feedTitle = feed.feedTitle;
                            item.feedUrl = feed.feedUrl;
                            item.itemCount = feed.itemCount;
                            item.itemUnReadCount = feed.itemUnReadCount;
                            if(mRSSListUpdated){
                                boolean existed = itemExist(item);
                                if(!existed){
                                    mHandler.post(new NewsAdder(item, true));
                                }
                            }else{
                                mHandler.post(new NewsAdder(item, false));
                            }
                        } while(itemCursor.moveToNext());
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
        private boolean itemExist(ItemInfo item){
            int size = mItemList.size();
            ItemInfo info = null;
            for(int i = 0; i < size; i++){
                info = mItemList.get(i);
                if(item.itemGuid.endsWith(info.itemGuid)){
                    return true;
                }
            }
            return false;
        }
    }
    class Holder{
        ImageView feedIcon;
        TextView itemTitle;
        TextView itemDes;
        TextView feedTitle;
        TextView itemDate;
    }
    class NewsAdapter extends ArrayAdapter<ItemInfo>{
        private LayoutInflater mInflater;
        public NewsAdapter(Context context, List<ItemInfo> objects) {
            super(context, 0, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if (convertView == null) {
                holder = new Holder();
                convertView = mInflater.inflate(R.layout.rss_news_item, null);
                holder.feedIcon = (ImageView) convertView.findViewById(R.id.img);
                holder.itemTitle = (TextView) convertView.findViewById(R.id.news_title);
                holder.itemDes = (TextView) convertView.findViewById(R.id.news_des);
                holder.feedTitle = (TextView) convertView.findViewById(R.id.news_author);
                holder.itemDate = (TextView) convertView.findViewById(R.id.news_pubdate);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            
            
            ItemInfo item = this.getItem(position);
            if(item != null){
                holder.feedIcon.setImageBitmap(item.feedIcon);
                holder.feedTitle.setText(item.feedTitle);
                holder.itemTitle.setText(item.itemTitle);
                holder.itemDes.setText(item.itemDescription);
                
                holder.itemTitle.setTextColor(Color.BLACK);
                holder.feedTitle.setTextColor(Color.BLACK);
                holder.itemDes.setTextColor(Color.BLACK);
                holder.itemDate.setTextColor(Color.BLACK);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sdf.format(new Date(item.itemPubdate));
                holder.itemDate.setText(date);
                /*
                if(item.itemState == RssConstant.State.STATE_ITEM_READ){
                    holder.itemTitle.setTextColor(Color.GRAY);
                    holder.feedTitle.setTextColor(Color.GRAY);
                    holder.itemDes.setTextColor(Color.GRAY);
                    holder.itemDate.setTextColor(Color.GRAY);
                }else{
                    holder.itemTitle.setTextColor(Color.BLACK);
                    holder.feedTitle.setTextColor(Color.BLACK);
                    holder.itemDes.setTextColor(Color.BLACK);
                    holder.itemDate.setTextColor(Color.BLACK);
                }*/
            }
            /*
            if(mCurPosition == position){
                convertView.setBackgroundResource(R.drawable.list_read_);
            }else{
                convertView.setBackgroundDrawable(null);
            }
            */
            return convertView;
        }
        
    }
    @Override
    protected void onDestroy() {
        unregisterReceiver(mNewRecevier);
        if(mNewsHandler != null){
            if(mNewsHandler.getLooper() != null){
                mNewsHandler.getLooper().quit();
            }
        }
        
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
    @Override
    public void onClick(View v) {
        Animation animation = null;
        int id = v.getId();
        switch(id){
        case R.id.previous:
            if(mCurPosition == 0){
                mCurPosition = mItemList.size();
            }
            mCurPosition --;
//            mWebViewFront.clearAnimation();
//            animation = AnimationUtils.loadAnimation(this, R.anim.slide_right_to_left);
//            mWebViewFront.startAnimation(animation);
//            animation.setAnimationListener(mPageChangeListener);
//            mCurWebView = mWebViewBack;
            show();
            break;
        case R.id.next:
            if(mCurPosition == mItemList.size() - 1){
                mCurPosition = -1;
            }
            mCurPosition ++;
//            mWebViewFront.clearAnimation();
//            animation = AnimationUtils.loadAnimation(this, R.anim.slide_left_to_right);
//            mWebViewFront.startAnimation(animation);
//            animation.setAnimationListener(mPageChangeListener);
//            mCurWebView = mWebViewBack;
            show();
            break;
        case R.id.viewOriginal:
            ItemInfo info = mItemList.get(mCurPosition);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.itemUrl));
            boolean innerBrowser = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceKeys.KEY_INNER_BROWSER, false);
            if(innerBrowser){
                intent.setComponent(new ComponentName(this, RSSWebView.class));
            }
            startActivity(intent);
            break;
        case R.id.news_title:
            if(mTitleTipDialog == null){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                mTitleTipDialog = builder.create();
            }
            mTitleTipDialog.setMessage(mNewsTitle.getText());
            mTitleTipDialog.show();
            break;
        case R.id.rss_news_header_refresh:
            String where = Constant.Content._ID + "=" + mFeedId;
            ContentValues values = new ContentValues();
            values.put(Constant.Content.FEED_STATE, Constant.State.STATE_WAITING);
            getContentResolver().update(Constant.Content.FEED_URI, values, where, null);
            intent = new Intent(Constant.Intent.INTENT_RSSAPP_MANUALREFRESH);
            startService(intent);
            mRefreshButton.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            break;
        default:
            break;
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
        str = showPicturesIfNeed(str);
        return str;
    }

    private String showPicturesIfNeed(String str){
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showPictures = preference.getBoolean(PreferenceKeys.KEY_SHOW_PICTURES, true);
        if(!showPictures){
            str = str.replaceAll("<img.*?>", "");
            return str;
        }
        boolean onlyShowPictureInWifi = preference.getBoolean(PreferenceKeys.KEY_PICTURE_ONLY_WIFI, true);
        if(onlyShowPictureInWifi){
            NetworkInfo networkInfo = NetworkUtil.getCurrentNetwork(this);
            if(networkInfo == null || networkInfo.getType() != ConnectivityManager.TYPE_WIFI){
                str = str.replaceAll("<img.*?>", "");
            }
        }
        return str;
    }
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if(sHeaderRefresh){
            if(mFeedId != -1){
                mCurPosition = arg2 - 1;
            }else{
                mCurPosition = arg2;
            }
        }
        mCurPosition = arg2;
        if(mNewLayout.getVisibility() != View.VISIBLE){
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
            animation.setAnimationListener(this);
            show();
            boolean enableAnimation = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceKeys.KEY_NEWS_ANIMATION, true);
            if(enableAnimation){
                mNewLayout.startAnimation(animation);
            }
            mNewLayout.setVisibility(View.VISIBLE);
            mNewsTitle.setOnClickListener(this);
            mNewsTitle.setClickable(true);
            setFullScreen(false);
            setTitle("");
        }
    }

    private void setFullScreen(boolean fullScreen){
        if(fullScreen){
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else{
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        mCurPosition = -1;
        mNewsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if(mDataLoading || mAnimationRunning){
            return ;
        }
        if(mNewLayout.getVisibility() == View.VISIBLE){
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
            boolean enableAnimation = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceKeys.KEY_NEWS_ANIMATION, true);
            if(enableAnimation){
                mNewLayout.startAnimation(animation);
            }
            mNewLayout.setVisibility(View.GONE);
            mNewsTitle.setOnClickListener(null);
            mNewsTitle.setClickable(false);
            setFullScreen(false);
            if(mFeedId == -1){
                String title = getResources().getString(R.string.articlelist);
                setTitle(title);
            }else{
                setTitle(mItemList.get(0).feedTitle);
            }
        }else{
            super.onBackPressed();
        }
    }
    private void setTitle(String title){
        if(mNewLayout.getVisibility() == View.VISIBLE){
            mFeedIcon.setVisibility(View.VISIBLE);
        }else{
            mFeedIcon.setVisibility(View.GONE);
            mNewsTitle.setText(title);
            mNewsIndex.setText("" + mItemList.size());
            /*
            int size = mItemList.size();
            if(size <= 0){
                mNewsIndex.setText(0 + "/" + size);
            }else{
                int unRead = mItemList.get(0).itemUnReadCount;
                mNewsIndex.setText(unRead + "/" + mItemList.size());
            }
            */
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int id = v.getId();
        switch(id){
        case R.id.previous:
            mDir = -1;
            mLongPressing = true;
            mHandler.post(mLongPress);
            Log.d(TAG, "longClick : " + "previous");
            break;
        case R.id.next:
            Log.d(TAG, "longClick : " + "next");
            mDir = 1;
            mLongPressing = true;
            mHandler.post(mLongPress);
            break;
        default:
            break;
        }
        return true;
    }

    private boolean mLongPressing = false;    
    private LongPress mLongPress = new LongPress();
    private int mDir;
    class LongPress implements Runnable{
        @Override
        public void run() {
            if(!mLongPressing){
                return ;
            }
            if(mDir == -1){
                if(mCurPosition == 0){
                    mCurPosition = mItemList.size();
                }
                mCurPosition --;
                show();
            }else{
                if(mCurPosition == mItemList.size() - 1){
                    mCurPosition = -1;
                }
                mCurPosition ++;
                show();
            }
            mHandler.postDelayed(mLongPress, 100);
        }
        
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.previous || v.getId() == R.id.next){
            return onButtonTouch(v, event);
        }
        return false;
    }
    boolean onButtonTouch(View v, MotionEvent event){
        int action = event.getAction();
        if(action == MotionEvent.ACTION_UP){
            mLongPressing = false;
        }
        return false;
    }
    
    private void refresh(){
        String where = Constant.Content._ID + "=" + mFeedId;
        ContentValues values = new ContentValues();
        values.put(Constant.Content.FEED_STATE, Constant.State.STATE_WAITING);
        getContentResolver().update(Constant.Content.FEED_URI, values, where, null);
        Intent intent = new Intent(Constant.Intent.INTENT_RSSAPP_MANUALREFRESH);
        startService(intent);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
        case 0:
            if(mNewsAdapter.getCount() > 0 && mNewLayout.getVisibility() != View.VISIBLE){
                mNewsHandler.sendEmptyMessage(MSG_CLEAR_ALL_NEWS);
            }
        break;
        case MENU_ITEM_SETTINGS:
            Intent intent = new Intent("com.android.rss.intent.action.RSSAPP_SETTINGS");
            startActivity(intent);
            break;
        default:
            break;
        }
        
        return true;
    }

    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mNewLayout.getVisibility() != View.VISIBLE){
            menu.findItem(MENU_ITEM_CLEAR).setEnabled(true);
        }else{
            menu.findItem(MENU_ITEM_CLEAR).setEnabled(false);
        }    
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ITEM_CLEAR, 0, R.string.clear);
        menu.add(0, MENU_ITEM_SETTINGS, 1, R.string.settings);
        return true;
    }

    @Override
    public void onRefresh() {
        // TODO Auto-generated method stub
        
    }
    
    BroadcastReceiver mNewRecevier = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null && Constant.Intent.INTENT_RSSAPP_UPDATE_NEWS_LIST.equals(intent.getAction())){
                int feedId = intent.getIntExtra(KEY_FEED_ID, -2);
                if(mFeedId != feedId){
                    return ;
                }
                Message msg = mNewsHandler.obtainMessage(MSG_UPDATE_NEWS_LIST);
                mNewsHandler.sendMessage(msg);
            }else if(intent != null && Constant.Intent.INTENT_RSSAPP_STARTREFRESH.equals(intent.getAction())){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mNewsIndex.setVisibility(View.INVISIBLE);
                        mNewsProgress.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    };

    @Override
    public void onAnimationEnd(Animation animation) {
        mAnimationRunning = false;
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        
    }

    @Override
    public void onAnimationStart(Animation animation) {
        mAnimationRunning = true;
    }
    
    private AnimationListener mPageChangeListener = new AnimationListener() {
        
        @Override
        public void onAnimationStart(Animation animation) {
            mPreButton.setEnabled(false);
            mNextButton.setEnabled(false);
        }
        
        @Override
        public void onAnimationRepeat(Animation animation) {
            
        }
        
        @Override
        public void onAnimationEnd(Animation animation) {
            mCurWebView = mWebViewFront;
            show();
            mPreButton.setEnabled(true);
            mNextButton.setEnabled(true);
        }
    };
    
    private void updateWidget(){
        Intent intent = new Intent(Constant.Intent.INTENT_RSSAPP_WIDGET_UPDATE);
        sendBroadcast(intent);
    }
    
    private int getFeedState(int feedId){
        Cursor c = null;
        int state = Constant.State.STATE_NO_SUBSCRIBED;
        String selection = Constant.Content._ID + "=" + feedId;
        String []projection = new String[]{Constant.Content.FEED_STATE};
        try {
            c = getContentResolver().query(Constant.Content.FEED_URI, 
                    projection, selection, null, null);
            if(c != null){
                if(c.moveToFirst()){
                    state = c.getInt(c.getColumnIndex(Constant.Content.FEED_STATE));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            state = Constant.State.STATE_NO_SUBSCRIBED;
        }finally{
            if(c != null){
                c.close();
            }
        }
        return state;
    }
    private WebViewClient mWebViewClient = new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            boolean innerBrowser = PreferenceManager.getDefaultSharedPreferences(RSSNewsList.this).getBoolean(PreferenceKeys.KEY_INNER_BROWSER, false);
            if(innerBrowser){
                intent.setComponent(new ComponentName(RSSNewsList.this, RSSWebView.class));
            }
            startActivity(intent);
            return true;
        }
    };
}
