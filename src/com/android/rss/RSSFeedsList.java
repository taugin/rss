package com.android.rss;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.util.Linkify;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.rss.common.FeedInfo;
import com.android.rss.common.Constant;
import com.android.rss.parse.OpmlParser;
import com.android.rss.refreshlist.RefreshListView.OnRefreshListener;
import com.android.rss.service.RSSService;
import com.android.rss.util.Log;
import com.android.rss.view.RefreshButton;
import com.android.rss.view.TabContainer;
import com.android.rss.view.TabContainer.OnTabChangeListener;

public class RSSFeedsList extends ListActivity implements OnClickListener, 
                        DialogInterface.OnClickListener, OnRefreshListener, OnItemClickListener, OnTabChangeListener
                        {

    private static final String TAG = "RSSFeedsList";
    private static final int MSG_INIT_FEED = 1;
    private static final int MSG_UPDATE_FINISHED = 2;
    private static final int MSG_VIEW_SUBSCRIBED = 3;
    private static final int MSG_VIEW_BUNDLES = 4;
    private static final int MSG_VIEW_CUSTOM = 5;
    private static final int MSG_CLEAR_FEEDS = 6;
    private static final int MSG_REFRESH_STATE = 7;
    
    private static final int MENU_ITEM_ADD_FEED = 0;
    private static final int MENU_ITEM_NEWS_LIST = 1;
    private static final int MENU_ITEM_SUBSCRIBED = 2;
    private static final int MENU_ITEM_MANAGE_SUB = 3;
    private static final int MENU_ITEM_BUNDLES = 4;
    private static final int MENU_ITEM_CUSTOM = 5;
    private static final int MENU_ITEM_MANAGE_CUSTOM = 6;
    private static final int MENU_ITEM_FIND_RSS = 7;
    private static final int MENU_ITEM_CLEAR = 8;
    private static final int MENU_ITEM_SETTINGS = 9;
    
    private static final int CONTEXT_MENU_OPEN = 0;
    private static final int CONTEXT_MENU_RENAME = 1;
    private static final int CONTEXT_MENU_DELETE = 2;
    private static final int CONTEXT_MENU_DETAIL = 3;
    private static final int CONTEXT_MENU_COPYURL = 4;
    
    private static final int DIALOG_ID_ADDFEED = 0;
    private static final int DIALOG_ID_EXIT = 1;
    private static final int DIALOG_ID_RENAME = 2;
    
    private static final String KEY_FEED_ID = "feedId";
    private static final String KEY_FEED_SORT = "feed_sort";
    private ArrayList<FeedInfo> mFeedInfos = null;
    private Handler mHandler = null;
    private ListView mRssFeedList = null;
    private BundleAdapter mFeedListAdapter = null;
    private FeedHandler mFeedHandler;
    private HashMap<String, Bitmap>  mHashMap = null;
    private TextView mFeedTitleLabel;
    private EditText mFeedTitle;
    private TextView mFeedUrlLabel;
    private AutoCompleteTextView mFeedUrl;
    private PopupWindow mPopupWindow;
    private int mFeedView = Constant.State.STATE_VIEW_SUBSCRIBED;
    private boolean mDataLoading = false;
    private TabContainer mTabContainer;
    private boolean mStopFetching = false;
    private EditText mRenameView;
    private FeedInfo mContextSelectedInfo;
//    private SubscribedFeedAdapter mSubscribedFeedAdapter;
//    private Spinner mRecentAdded = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.rss_list_layout);
        mTabContainer = (TabContainer) findViewById(R.id.function_tab);
        mTabContainer.setOnTabChangeListener(this);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        setTitle(R.string.feeds);
        mHandler = new Handler();
        mHashMap = new HashMap<String, Bitmap>();
        HandlerThread thread = new HandlerThread("RssAppSettings");
        thread.start();
        mFeedHandler = new FeedHandler(this, thread.getLooper());
        mFeedInfos = new ArrayList<FeedInfo>();
        mRssFeedList = getListView();
//        mRssFeedList.setOnRefreshListener(this);
        mRssFeedList.setOnItemClickListener(this);
        registerForContextMenu(mRssFeedList);
        mFeedListAdapter = new BundleAdapter(this, mFeedInfos);
        mRssFeedList.setAdapter(mFeedListAdapter);
        
        mFeedHandler.sendEmptyMessage(MSG_INIT_FEED);
//        SharedPreferences shared = getSharedPreferences(RssConstant.Content.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
//        int refreshState = shared.getInt(RssConstant.Key.KEY_REFRESH_STATE, RssConstant.State.STATE_REFRESH_NORMAL);
//        if(refreshState == RssConstant.State.STATE_REFRESH_ONGOING){
//            mRssFeedList.setRefreshState();
//        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.Intent.INTENT_RSSAPP_UPDATE_FINISHED);
        filter.addAction(Constant.Intent.INTENT_RSSAPP_SUBSCRIBE_FINISHED);
        filter.addAction(Constant.Intent.INTENT_RSSAPP_STARTREFRESH);
        registerReceiver(mSubscribeReceiver, filter);
        Intent intent = new Intent(Constant.Intent.INTENT_RSSAPP_MANUALREFRESH);
        startService(intent);
        
        /*
        Intent shortCut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        Intent shortCutIntent = new Intent(Intent.ACTION_MAIN);
        ComponentName name = new ComponentName(this, RssAppSettings.class);
        shortCutIntent.setComponent(name);
        ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(this, R.drawable.rss);
        shortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        shortCut.putExtra(Intent.EXTRA_SHORTCUT_INTENT,shortCutIntent);
        shortCut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.rss));
        sendBroadcast(shortCut);
        */
    }
    @Override
    protected void onResume() {
        super.onResume();
//        mFeedHandler.sendEmptyMessage(MSG_INIT_FEED);
    }
    class FeedAdder implements Runnable{
        private FeedInfo mFeedInfo;
        public FeedAdder(FeedInfo info){
            mFeedInfo = info;
        }
        @Override
        public void run() {
            mFeedListAdapter.add(mFeedInfo);
            mFeedListAdapter.notifyDataSetChanged();
        }
    }
    class ViewHolder{
        ImageView feedIcon;
        TextView feedTitle;
        ImageButton subscribe;
        ImageButton remove;
        RefreshButton refresh;
        ImageButton subscribed;
        View progress;
    }
    
    private class BundleAdapter extends ArrayAdapter<FeedInfo>{
        private LayoutInflater mInflater;
        public BundleAdapter(Context context, ArrayList<FeedInfo> list) {
            super(context, 0,list);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.rss_feed_item, null);
                holder.feedIcon = (ImageView)convertView.findViewById(R.id.rss_feed_img);
                holder.feedTitle = (TextView) convertView.findViewById(R.id.rss_feed_name);
                holder.subscribe = (ImageButton) convertView.findViewById(R.id.rss_feed_subscribe);
                holder.remove = (ImageButton) convertView.findViewById(R.id.rss_feed_remove);
                holder.refresh = (RefreshButton) convertView.findViewById(R.id.rss_feed_refresh);
                holder.subscribed = (ImageButton) convertView.findViewById(R.id.rss_feed_subscribed);
                holder.subscribe.setOnClickListener(RSSFeedsList.this);
                holder.remove.setOnClickListener(RSSFeedsList.this);
                holder.refresh.setOnClickListener(RSSFeedsList.this);
                holder.progress = convertView.findViewById(R.id.subscribing);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            
            holder.feedIcon.setTag(Integer.valueOf(position));
            holder.subscribe.setTag(Integer.valueOf(position));
            holder.remove.setTag(Integer.valueOf(position));
            holder.feedTitle.setTag(Integer.valueOf(position));
            holder.refresh.setTag(Integer.valueOf(position));
            
            FeedInfo info = getItem(position);
            if(info != null){
                if(TextUtils.isEmpty(info.feedTitle)){
                    if(TextUtils.isEmpty(info.feedOriTitle)){
                        holder.feedTitle.setText(info.feedUrl);
                    }else{
                        holder.feedTitle.setText(info.feedOriTitle);
                    }
                }else{
                    holder.feedTitle.setText(info.feedTitle);
                }
//                holder.feedTitle.setOnClickListener(RSSFeedsList.this);
//                holder.feedTitle.setBackgroundResource(R.drawable.button_background);
                if(mFeedView == Constant.State.STATE_VIEW_SUBSCRIBED){
                    ((ViewGroup)convertView).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                }else{
                    ((ViewGroup)convertView).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                }
                holder.feedIcon.setVisibility(View.VISIBLE);
                if(info.feedBitmap != null){
                    holder.feedIcon.setImageBitmap(info.feedBitmap);
                }else{
                    holder.feedIcon.setImageResource(R.drawable.ic_rss_small);
                }
                
                holder.feedIcon.setVisibility(View.GONE);
                holder.feedTitle.setTextColor(Color.GRAY);
                holder.progress.setVisibility(View.INVISIBLE);
                holder.subscribe.setVisibility(View.INVISIBLE);
                holder.remove.setVisibility(View.INVISIBLE);
                holder.refresh.setVisibility(View.INVISIBLE);
                holder.subscribed.setVisibility(View.INVISIBLE);
                holder.feedTitle.setEnabled(false);
                if(info.feedState == Constant.State.STATE_WAITING || info.feedState == Constant.State.STATE_SUBSCRIBING){
                    holder.progress.setVisibility(View.VISIBLE);
                    if(mFeedView == Constant.State.STATE_VIEW_SUBSCRIBED){
                        holder.feedIcon.setVisibility(View.VISIBLE);
                        if(info.itemCount > 0){
                            holder.feedTitle.setEnabled(true);
                            holder.feedTitle.setTextColor(Color.BLACK);
                        }else{
                            holder.feedTitle.setEnabled(false);
                            holder.feedTitle.setTextColor(Color.GRAY);
                        }
                    }else{
                        holder.feedTitle.setEnabled(false);
                        holder.feedTitle.setTextColor(Color.BLACK);
                    }
                }else if(info.feedState == Constant.State.STATE_NO_SUBSCRIBED){
                    if(mFeedView == Constant.State.STATE_VIEW_MANAGE_CUSTOM){
                        holder.remove.setVisibility(View.VISIBLE);
                    }else{
                        holder.remove.setVisibility(View.INVISIBLE);
                        holder.subscribe.setVisibility(View.VISIBLE);
                    }
                    if(mFeedView == Constant.State.STATE_VIEW_BUNDLES){
                        holder.feedTitle.setTextColor(Color.BLACK);
                    }
                }else{
                    holder.feedTitle.setTextColor(Color.BLACK);
                    if(mFeedView == Constant.State.STATE_VIEW_SUBSCRIBED){
                        holder.feedIcon.setVisibility(View.VISIBLE);
                        holder.refresh.setVisibility(View.VISIBLE);
                        if(info.itemCount > 0){
                            holder.feedTitle.setEnabled(true);
                            holder.feedTitle.setTextColor(Color.BLACK);
                        }else{
                            holder.feedTitle.setEnabled(false);
                            holder.feedTitle.setTextColor(Color.GRAY);
                        }
                    }else if(mFeedView == Constant.State.STATE_VIEW_MANAGE_SUB){
                        holder.remove.setVisibility(View.VISIBLE);
                    }else if(mFeedView == Constant.State.STATE_VIEW_BUNDLES){
                        holder.subscribed.setVisibility(View.VISIBLE);
                        holder.feedTitle.setTextColor(Color.BLACK);
                    }else if(mFeedView == Constant.State.STATE_VIEW_CUSTOM){
                        holder.subscribed.setVisibility(View.VISIBLE);
                    }else if(mFeedView == Constant.State.STATE_VIEW_MANAGE_CUSTOM){
                        holder.remove.setVisibility(View.VISIBLE);
                    }
                }
            }
            return convertView;
        }
        
    }
    
    BroadcastReceiver mSubscribeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = null;
            if(intent != null){
                action = intent.getAction();
                Log.d(TAG ,"action = " + action);
                if(Constant.Intent.INTENT_RSSAPP_SUBSCRIBE_FINISHED.equals(action)){
                    int feedId = intent.getIntExtra(KEY_FEED_ID, -1);
                    Message msg = mFeedHandler.obtainMessage(MSG_UPDATE_FINISHED, feedId, 0);
                    mFeedHandler.sendMessage(msg);
                }else if(Constant.Intent.INTENT_RSSAPP_UPDATE_FINISHED.equals(action)){
                    SharedPreferences shared = getSharedPreferences(Constant.Content.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
                    int refreshState = shared.getInt(Constant.Key.KEY_REFRESH_STATE, Constant.State.STATE_REFRESH_NORMAL);
                    shared.edit().putInt(Constant.Key.KEY_REFRESH_STATE, Constant.State.STATE_REFRESH_NORMAL).commit();
                    if(refreshState == Constant.State.STATE_REFRESH_NORMAL && mFeedView == Constant.State.STATE_VIEW_SUBSCRIBED){
                        int size = mFeedInfos.size();
                        for(int i=0;i<size;i++){
                            mFeedInfos.get(i).feedState = Constant.State.STATE_SUBSCRIBED;
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mFeedListAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }else if(Constant.Intent.INTENT_RSSAPP_STARTREFRESH.equals(action)){
                    int size = mFeedInfos.size();
                    for(int i=0;i<size;i++){
                        mFeedInfos.get(i).feedState = Constant.State.STATE_WAITING;
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mFeedListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }
    };
    private class FeedHandler extends Handler{
        private Context mContext;
        public FeedHandler(Context context, Looper looper) {
            super(looper);
            mContext = context;
        }
        @Override
        public void handleMessage(Message msg) {
            mDataLoading = true;
            updateProgress(true);
            int what = msg.what;
            final TextView emptyView = (TextView) mRssFeedList.getEmptyView();
            int resId = 0;
            if(mFeedView == Constant.State.STATE_VIEW_SUBSCRIBED){
                resId = R.string.feed_subscribed;
            }else if(mFeedView == Constant.State.STATE_VIEW_BUNDLES){
                resId = R.string.addbybundlestitle;
            }else if(mFeedView == Constant.State.STATE_VIEW_CUSTOM){
                resId = R.string.custom;
            }else if(mFeedView == Constant.State.STATE_VIEW_MANAGE_SUB){
                resId = R.string.manager_sub;
            }else if(mFeedView == Constant.State.STATE_VIEW_MANAGE_CUSTOM){
                resId = R.string.manage_custom;
            }
            Log.d(TAG, "String : " + mContext.getResources().getString(resId));
            final int textResId = resId;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    emptyView.setText(textResId);
                }
            });
            
            switch(what){
            case MSG_UPDATE_FINISHED:
                updateFinish(msg.arg1);
                break;
            case MSG_INIT_FEED:
            case MSG_VIEW_SUBSCRIBED:
                clearAdapter();
                fetchSubscribedFeed();
                updateListView();
                break;
            case MSG_VIEW_BUNDLES:
                clearAdapter();
                fetchBundle();
                updateListView();
                break;
            case MSG_VIEW_CUSTOM:
                clearAdapter();
                fetchHistoryFeed();
                updateListView();
                break;
            case MSG_CLEAR_FEEDS:
                int result = 0;
                if(mFeedView == Constant.State.STATE_VIEW_MANAGE_SUB){
                    result = getContentResolver().delete(Constant.Content.FEED_URI, null, null);
                    Log.d(TAG, "Feed Table delete operation result : " + result);
                    result = getContentResolver().delete(Constant.Content.FEED_INDEX_URI, null, null);
                    Log.d(TAG, "Feed Index Table delete operation result : " + result);
                    result = getContentResolver().delete(Constant.Content.ITEM_INDEX_URI, null, null);
                    Log.d(TAG, "Item Index Table delete operation result : " + result);
                    result = getContentResolver().delete(Constant.Content.ITEM_URI, null, null);
                    Log.d(TAG, "Item Table delete operation result : " + result);
                    clearAdapter();
                }else if(mFeedView == Constant.State.STATE_VIEW_MANAGE_CUSTOM){
                    result = getContentResolver().delete(Constant.Content.HISTORY_URI, null, null);
                    Log.d(TAG, "History Table delete operation result : " + result);
                    clearAdapter();
                }
                updateWidget();
                break;
            case MSG_REFRESH_STATE:
                updateFeedState();
                break;
            default:
                break;
            }
            mStopFetching = false;
            mDataLoading = false;
            updateProgress(false);
        }
        
        private void fetchHistoryFeed(){
            String orderBy = Constant.Content._ID + " asc";
            Cursor c = null;
            FeedInfo info = null;
            try{
                c = mContext.getContentResolver().query(Constant.Content.HISTORY_URI, null, null, null, orderBy);
                if(c != null){
                    if(c.moveToFirst()){
                        do{
                            info = new FeedInfo();
                            info.feedId = -1;
                            info.feedTitle = c.getString(c.getColumnIndex(Constant.Content.HISTORY_TITLE));
                            info.feedUrl = c.getString(c.getColumnIndex(Constant.Content.HISTORY_URL));
                            info.feedGuid = c.getString(c.getColumnIndex(Constant.Content.FEED_GUID));
                            info.feedIconState = "unknown";
                            info.feedIsBundle = 0;
                            if(info.feedUrl != null){
                                info.feedGuid = String.valueOf(info.feedUrl.hashCode());
                                info.feedState = feedState(info.feedUrl.hashCode(), info);
                            }else{
                                info.feedState = Constant.State.STATE_NO_SUBSCRIBED;
                            }
                            mHandler.post(new FeedAdder(info));
//                            mFeedInfos.add(info);
                        }while(c.moveToNext() && !mStopFetching);
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
        private void fetchSubscribedFeed(){
            String []projection = new String[]{Constant.Content._ID, Constant.Content.FEED_STATE, 
                    Constant.Content.FEED_ICON, Constant.Content.FEED_TITLE, 
                    Constant.Content.FEED_URL, Constant.Content.FEED_ORI_TITLE, 
                    Constant.Content.FEED_GUID, Constant.Content.ITEM_COUNT,Constant.Content.FEED_PUBDATE};
            String orderBy = Constant.Content._ID + " asc";
            Cursor c = null;
            FeedInfo info = null;
            try{
                c = mContext.getContentResolver().query(Constant.Content.FEED_URI, projection, null, null, orderBy);
                if(c != null){
                    if(c.moveToFirst()){
                        do{
                            info = new FeedInfo();
                            info.feedId = c.getInt(c.getColumnIndex(Constant.Content._ID));
                            info.feedTitle = c.getString(c.getColumnIndex(Constant.Content.FEED_TITLE));
                            info.feedOriTitle = c.getString(c.getColumnIndex(Constant.Content.FEED_ORI_TITLE));
                            info.feedUrl = c.getString(c.getColumnIndex(Constant.Content.FEED_URL));
                            info.feedGuid = c.getString(c.getColumnIndex(Constant.Content.FEED_GUID));
                            info.feedState = c.getInt(c.getColumnIndex(Constant.Content.FEED_STATE));
                            info.itemCount = c.getInt(c.getColumnIndex(Constant.Content.ITEM_COUNT));
                            info.feedPubdate = c.getLong(c.getColumnIndex(Constant.Content.FEED_PUBDATE));
                            info.feedBitmap = getIcon(c, info);
                            mHandler.post(new FeedAdder(info));
//                            mFeedInfos.add(info);
                        }while(c.moveToNext() && !mStopFetching);
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
        private void fetchBundle(){
            Cursor c = null;
            String orderBy = Constant.Content.BUNDLE_DATE + " desc , " + Constant.Content.BUNDLE_TIMES + " desc";
            FeedInfo info = null;
            try{
                c = mContext.getContentResolver().query(Constant.Content.BUNDLE_URI, null, null, null, orderBy);
                if(c != null){
                    if(c.moveToFirst()){
                        do{
                            info = new FeedInfo();
                            info.feedId = -1;
                            info.feedTitle = c.getString(c.getColumnIndex(Constant.Content.BUNDLE_TITLE));
                            info.feedUrl = c.getString(c.getColumnIndex(Constant.Content.BUNDLE_URL));
                            info.feedIconState = "unknown";
                            info.feedIsBundle = 1;
                            if(info.feedUrl != null){
                                info.feedGuid = String.valueOf(info.feedUrl.hashCode());
                                info.feedState = feedState(info.feedUrl.hashCode(), info);
                            }else{
                                info.feedState = Constant.State.STATE_NO_SUBSCRIBED;
                            }
                            mHandler.post(new FeedAdder(info));
                        }while(c.moveToNext() && !mStopFetching);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if(c != null){
                    c.close();
                }
            }
            
        }
        private Bitmap getIcon(Cursor c, FeedInfo item){
            if(mHashMap != null){
                if(mHashMap.containsKey(item.feedGuid)){
                    return mHashMap.get(item.feedGuid);
                }
                byte []image = c.getBlob(c.getColumnIndex(Constant.Content.FEED_ICON));
                if(image != null){
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    mHashMap.put(item.feedGuid, bitmap);
                    return bitmap;
                }
            }
            return null;
        }
        private int feedState(int hashCode, FeedInfo info){
            String selection = Constant.Content.FEED_GUID + "=" + String.valueOf(hashCode);
            String []projection = new String[]{Constant.Content._ID, Constant.Content.FEED_STATE, Constant.Content.FEED_ICON};
            int state = Constant.State.STATE_NO_SUBSCRIBED;
            Cursor c = null;
            try{
                c = mContext.getContentResolver().query(Constant.Content.FEED_URI, projection, selection, null, null);
                if(c != null){
                    if(c.moveToFirst()){
                        info.feedId = c.getInt(c.getColumnIndex(Constant.Content._ID));
                        state = c.getInt(c.getColumnIndex(Constant.Content.FEED_STATE));
                        info.feedBitmap = getIcon(c, info);
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
                state = Constant.State.STATE_NO_SUBSCRIBED;
                return state;
            }finally{
                if(c != null){
                    c.close();
                }
            }
            return state;
        }
        private void updateFinish(int feedId){
            Log.d(TAG ,"updateFinish FeedId = " + feedId);
            String selection = Constant.Content._ID + "=" + feedId;
            Cursor c = null;
            FeedInfo newInfo = new FeedInfo();
            try{
                c = mContext.getContentResolver().query(Constant.Content.FEED_URI, null, selection, null, null);
                if(c != null){
                    if(c.moveToFirst()){
                        newInfo.feedId = feedId;
                        newInfo.feedState = c.getInt(c.getColumnIndex(Constant.Content.FEED_STATE));
                        newInfo.feedUrl = c.getString(c.getColumnIndex(Constant.Content.FEED_URL));
                        newInfo.feedOriTitle = c.getString(c.getColumnIndex(Constant.Content.FEED_ORI_TITLE));
                        newInfo.feedGuid = c.getString(c.getColumnIndex(Constant.Content.FEED_GUID));
                        newInfo.itemCount = c.getInt(c.getColumnIndex(Constant.Content.ITEM_COUNT));
                        newInfo.feedPubdate = c.getLong(c.getColumnIndex(Constant.Content.FEED_PUBDATE));
                        newInfo.feedBitmap = getIcon(c, newInfo);
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }finally{
                if(c != null){
                    c.close();
                }
            }
            int i = 0;
            int size = mFeedInfos.size();
            FeedInfo info = null;
            boolean finded = false;
            for(i = 0; i < size; i++){
                info = mFeedInfos.get(i);
                if(info.feedId == newInfo.feedId){
                    info.feedState = newInfo.feedState;
                    info.feedBitmap = newInfo.feedBitmap;
                    info.feedOriTitle = newInfo.feedOriTitle;
                    info.itemCount = newInfo.itemCount;
                    info.feedPubdate = newInfo.feedPubdate;
                    finded = true;
                    break;
                }
            }
            if(finded){
                Log.d(TAG, "feedState = " + info.feedState + " , bitmap = " + info.feedBitmap);
                Log.d(TAG ,"find the updated item : " + info.toString());
                /*
                if(info.feedState == RssConstant.State.STATE_FAILURE){
                    if(mFeedView == RssConstant.State.STATE_VIEW_SUBSCRIBED){
                        if(info.itemCount <= 0){
                            String where = RssConstant.Content._ID + "=" + info.feedId;
                            mContext.getContentResolver().delete(RssConstant.Content.FEED_URI, where, null);
                            info.feedState = RssConstant.State.STATE_NO_FEED;
                        }
                    }
                    final FeedInfo feed = info;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(mFeedView == RssConstant.State.STATE_VIEW_SUBSCRIBED){
                                if(feed.itemCount <= 0){
                                    mFeedListAdapter.remove(feed);
                                }
                            }
                            String text = getResources().getString(R.string.refresh_failed);
                            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                */
                updateListView();
            }else{
                Log.d(TAG, "A new Feed is added to db");
                if(newInfo.feedState == Constant.State.STATE_FAILURE){
                    if(mFeedView == Constant.State.STATE_VIEW_SUBSCRIBED){
                        if(newInfo.itemCount <= 0){
                            String where = Constant.Content._ID + "=" + newInfo.feedId;
                            mContext.getContentResolver().delete(Constant.Content.FEED_URI, where, null);
                            info.feedState = Constant.State.STATE_NO_SUBSCRIBED;
                        }
                    }
                }else{
                    final FeedInfo feed = newInfo;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(mFeedView == Constant.State.STATE_VIEW_SUBSCRIBED){
                                mFeedListAdapter.add(feed);
                            }
                        }
                    });
                }
                updateListView();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "RssAppSettings : onDestroy");
        unregisterReceiver(mSubscribeReceiver);
        if(mFeedHandler != null){
            Looper looper = mFeedHandler.getLooper();
            if(looper != null){
                looper.quit();
            }
        }
        Intent intent = new Intent(this, RSSService.class);
        stopService(intent);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        Integer integer = (Integer) v.getTag();
        int position = integer.intValue();
        int id = v.getId();
        FeedInfo info = mFeedInfos.get(position);
        Log.d(TAG, "position = " + integer.intValue() + " : " + info.feedTitle);
        String where = null;
        ContentValues values = null;
        switch(id){
        case R.id.rss_feed_subscribe:
            if(!feedSubscribed(info)){
                info.feedState = Constant.State.STATE_WAITING;
                info.feedGuid = String.valueOf(info.feedUrl.hashCode());
                values = new ContentValues();
                values.put(Constant.Content.FEED_TITLE, info.feedTitle);
                values.put(Constant.Content.FEED_URL, info.feedUrl);
                values.put(Constant.Content.FEED_STATE, info.feedState);
                values.put(Constant.Content.FEED_ICON_STATE, info.feedIconState);
                values.put(Constant.Content.FEED_CLASS, "other");
                values.put(Constant.Content.FEED_CLASS_GUID, String.valueOf("other".hashCode()));
                values.put(Constant.Content.FEED_GUID, info.feedGuid);
                values.put(Constant.Content.FEED_IS_BUNDLE, info.feedIsBundle);
                Uri uri = getContentResolver().insert(Constant.Content.FEED_URI, values);
                info.feedId = (int) ContentUris.parseId(uri);
                
                where = Constant.Content.BUNDLE_GUID + "=" + info.feedGuid;
                uri = Uri.withAppendedPath(Constant.Content.BUNDLE_URI, "times/0");
                getContentResolver().update(uri, values, where, null);
                Intent intent = new Intent(Constant.Intent.INTENT_RSSAPP_MANUALREFRESH);
                startService(intent);
                Log.d(TAG,"info.feedId = " + info.feedId);
            }
            break;
        case R.id.rss_feed_remove:
            int result = 0;
            if(mFeedView == Constant.State.STATE_VIEW_MANAGE_SUB){
                info.feedBitmap = null;
                info.feedState = Constant.State.STATE_NO_SUBSCRIBED;
                where = Constant.Content._ID + "=" + info.feedId;
                result = getContentResolver().delete(Constant.Content.FEED_URI, where, null);
                Log.d(TAG, "Feed Table delete operation result : " + result);
                /*
                where = RssConstant.Content.FEED_ID + "=" + info.feedId;
                result = getContentResolver().delete(RssConstant.Content.FEED_INDEX_URI, where, null);
                LogUtils.debug(TAG, "Feed Index Table delete operation result : " + result);
                result = getContentResolver().delete(RssConstant.Content.ITEM_INDEX_URI, where, null);
                LogUtils.debug(TAG, "Item Index Table delete operation result : " + result);
                result = getContentResolver().delete(RssConstant.Content.ITEM_URI, where, null);
                LogUtils.debug(TAG, "Item Table delete operation result : " + result);
                */
                info.feedId = -1;
                mFeedListAdapter.remove(info);
            }else if(mFeedView == Constant.State.STATE_VIEW_MANAGE_CUSTOM){
                where = Constant.Content.FEED_GUID + "=" + String.valueOf(info.feedUrl.hashCode());
                result = getContentResolver().delete(Constant.Content.HISTORY_URI, where, null);
                Log.d(TAG, "History Table delete operation result : " + result);
                mFeedListAdapter.remove(info);
            }
            updateWidget();
            break;
        case R.id.rss_feed_name:
            Intent intent = new Intent(this, RSSNewsList.class);
            intent.putExtra(KEY_FEED_ID, info.feedId);
            startActivity(intent);
            break;
        case R.id.rss_feed_refresh:
            info.feedState = Constant.State.STATE_WAITING;
            where = Constant.Content._ID + "=" + info.feedId;
            values = new ContentValues();
            values.put(Constant.Content.FEED_STATE, info.feedState);
            getContentResolver().update(Constant.Content.FEED_URI, values, where, null);
            intent = new Intent(Constant.Intent.INTENT_RSSAPP_MANUALREFRESH);
            startService(intent);
            break;
        case R.id.delete_feed:
            break;
        default:
            break;
        }
        mFeedListAdapter.notifyDataSetChanged();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = null;
        View view = null;
        AlertDialog dialog = null;
        ArrayList<FeedInfo> subscribedFeeds = null;
        switch(id){
        case DIALOG_ID_ADDFEED:
            builder = new AlertDialog.Builder(this);
            view = getLayoutInflater().inflate(R.layout.add_feed, null);
            mFeedTitleLabel = (TextView) view.findViewById(R.id.feed_title_label);
            mFeedTitle = (EditText) view.findViewById(R.id.feed_title);
            mFeedUrlLabel = (TextView) view.findViewById(R.id.feed_url_label);
            mFeedUrl = (AutoCompleteTextView) view.findViewById(R.id.setfeedurl);
    //        mRecentAdded = (Spinner) view.findViewById(R.id.recent_added_feed);
            subscribedFeeds = new ArrayList<FeedInfo>();
    //        mSubscribedFeedAdapter = new SubscribedFeedAdapter(this, subscribedFeeds);
    //        mFeedAddress.setAdapter(mSubscribedFeedAdapter);
    //        mFeedAddress.setOnFocusChangeListener(this);
    //        mFeedAddress.setOnItemClickListener(this);
            
            
    //        mRecentAdded.setAdapter(mSubscribedFeedAdapter);
    //        mRecentAdded.setOnItemSelectedListener(this);
    //        View emptyView = getLayoutInflater().inflate(R.layout.empty_view, null);
    //        mRecentAdded.setEmptyView(emptyView);
            builder = builder.setView(view);
            builder = builder.setPositiveButton(getResources().getString(R.string.ok), this);
            builder = builder.setNegativeButton(getResources().getString(R.string.cancel), null);
            dialog = builder.create();
            dialog.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            return dialog;
        case DIALOG_ID_EXIT:
            builder = new AlertDialog.Builder(this);
            builder = builder.setTitle(R.string.exit);
            builder = builder.setMessage(R.string.exit_rssreader);
            builder = builder.setIcon(R.drawable.rss);
            builder = builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder = builder.setNegativeButton(R.string.cancel, null);
            builder = builder.setCancelable(true);
            dialog = builder.create();
            return dialog;
        case DIALOG_ID_RENAME:
            builder = new AlertDialog.Builder(this);
            builder = builder.setTitle(R.string.context_menu_rename);
            builder = builder.setIcon(R.drawable.rss);
            builder = builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ContentValues values = new ContentValues();
                    String where = Constant.Content._ID + "=" + mContextSelectedInfo.feedId;
                    String title = mRenameView.getEditableText().toString();
                    if(!TextUtils.isEmpty(title)){
                        mContextSelectedInfo.feedTitle = title;
                        values.put(Constant.Content.FEED_TITLE, title);
                        getContentResolver().update(Constant.Content.FEED_URI, values, where, null);
                        mFeedListAdapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(getApplicationContext(), "Title can not be empty!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder = builder.setNegativeButton(R.string.cancel, null);
            mRenameView = new EditText(this);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            mRenameView.setLayoutParams(params);
            builder.setView(mRenameView);
            dialog = builder.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            return dialog;
        }
        return null;
    }
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id){
        case DIALOG_ID_ADDFEED:
            if(mFeedView == Constant.State.STATE_VIEW_CUSTOM){
                mFeedTitleLabel.setVisibility(View.GONE);
                mFeedTitle.setVisibility(View.GONE);
                mFeedUrlLabel.setText(R.string.opml);
            }else{
                mFeedUrlLabel.setText(R.string.url);
                mFeedTitleLabel.setVisibility(View.VISIBLE);
                mFeedTitle.setVisibility(View.VISIBLE);
            }
            mFeedTitle.setText(null);
            mFeedUrl.setText(null);
            break;
        case DIALOG_ID_RENAME:
            mRenameView.setText(mContextSelectedInfo.feedTitle);
            mRenameView.setSelection(0, mContextSelectedInfo.feedTitle.length());
            break;
        }
    }
    class SubscribedFeedAdapter extends ArrayAdapter<FeedInfo>{

        private Context mContext = null;
        private LayoutInflater mInflater = null;
        public SubscribedFeedAdapter(Context context, List<FeedInfo> objects) {
            super(context, 0, 0, objects);
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
            }
            convertView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, 50));
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setTextColor(Color.BLACK);
            tv.setSingleLine();
            FeedInfo info = getItem(position);
            if(info != null){
                if(TextUtils.isEmpty(info.feedTitle)){
                    tv.setText(info.feedUrl);
                }else{
                    tv.setText(info.feedTitle);                    
                }
            }
            return convertView;
        }
        @Override
        public View getDropDownView(int position, View convertView,
                ViewGroup parent) {
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.recent_feed, null);
            }
            TextView tv = (TextView) convertView.findViewById(R.id.recent_feed_title);
            View view = convertView.findViewById(R.id.recent_delete);
            final FeedInfo info = getItem(position);            
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String where = Constant.Content._ID + "=" + info.feedId;
                    mContext.getContentResolver().delete(Constant.Content.HISTORY_URI, where, null);
                    remove(info);
                }
            });
            view.setTag(Integer.valueOf(position));
            tv.setTextColor(Color.BLACK);
            tv.setSingleLine();
            if(info != null){
                if(TextUtils.isEmpty(info.feedTitle)){
                    tv.setText(info.feedUrl);
                }else{
                    tv.setText(info.feedTitle);                    
                }
                if(info.feedId == -1){
                    view.setVisibility(View.INVISIBLE);
                }else{
                    view.setVisibility(View.VISIBLE);
                }
            }
            return convertView;
        }
    }
    /*
    private void fillAdapter(){
        mSubscribedFeedAdapter.clear();
        Cursor c = null;
        FeedInfo info = null;
        try{
            c = getContentResolver().query(RssConstant.Content.HISTORY_URI, null, null, null, null);
            if(c != null){
                if(c.moveToFirst()){
                    do{
                        info = new FeedInfo();
                        info.feedId = c.getInt(c.getColumnIndex(RssConstant.Content._ID));
                        info.feedTitle = c.getString(c.getColumnIndex(RssConstant.Content.HISTORY_TITLE));
                        info.feedUrl = c.getString(c.getColumnIndex(RssConstant.Content.HISTORY_URL));
                        info.feedGuid = c.getString(c.getColumnIndex(RssConstant.Content.FEED_GUID));
                        LogUtils.debug(TAG ,"fillAdapter : feedTitle = " + info.feedTitle + " , feedUrl = " + info.feedUrl);
                        mSubscribedFeedAdapter.add(info);
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
        if(mSubscribedFeedAdapter.getCount() > 0){
            info = new FeedInfo();
            info.feedId = -1;
            info.feedTitle = getResources().getString(R.string.clear);
            mSubscribedFeedAdapter.insert(info, 0);
        }
    }
    */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mFeedView == Constant.State.STATE_VIEW_SUBSCRIBED){
            menu.setGroupVisible(0, true);
            menu.findItem(MENU_ITEM_SUBSCRIBED).setVisible(false);
            menu.findItem(MENU_ITEM_MANAGE_CUSTOM).setVisible(false);
            menu.findItem(MENU_ITEM_CLEAR).setVisible(false);
            menu.findItem(MENU_ITEM_BUNDLES).setVisible(false);
            menu.findItem(MENU_ITEM_CUSTOM).setVisible(false);
        }else if(mFeedView == Constant.State.STATE_VIEW_MANAGE_SUB){
            menu.setGroupVisible(0, false);
            menu.findItem(MENU_ITEM_CLEAR).setVisible(true);
            if(mFeedListAdapter.getCount() > 0){
                menu.findItem(MENU_ITEM_CLEAR).setEnabled(true);
            }else{
                menu.findItem(MENU_ITEM_CLEAR).setEnabled(false);
            }
        }else if(mFeedView == Constant.State.STATE_VIEW_BUNDLES){
            menu.setGroupVisible(0, false);
        }else if(mFeedView == Constant.State.STATE_VIEW_CUSTOM){
            menu.setGroupVisible(0, false);
            menu.findItem(MENU_ITEM_ADD_FEED).setVisible(true);
            menu.findItem(MENU_ITEM_MANAGE_CUSTOM).setVisible(true);
        }else if(mFeedView == Constant.State.STATE_VIEW_MANAGE_CUSTOM){
            menu.setGroupVisible(0, false);
            menu.findItem(MENU_ITEM_CLEAR).setVisible(true);
            if(mFeedListAdapter.getCount() > 0){
                menu.findItem(MENU_ITEM_CLEAR).setEnabled(true);
            }else{
                menu.findItem(MENU_ITEM_CLEAR).setEnabled(false);
            }
        }
//        menu.findItem(MENU_ITEM_SETTINGS).setVisible(true);
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG ,"onCreateOptionsMenu");
        menu.add(0, MENU_ITEM_ADD_FEED,     0, R.string.addfeed_prefenrence);
        menu.add(0, MENU_ITEM_SUBSCRIBED,     1, R.string.feed_subscribed);
        menu.add(0, MENU_ITEM_MANAGE_SUB,     2, R.string.manager_sub);
        menu.add(0, MENU_ITEM_NEWS_LIST,     3, R.string.articlelist);
        menu.add(0, MENU_ITEM_BUNDLES,         4, R.string.addbybundlestitle);
        menu.add(0, MENU_ITEM_CUSTOM,         5, R.string.custom);
        menu.add(0, MENU_ITEM_MANAGE_CUSTOM,6, R.string.manage_custom);
        menu.add(0, MENU_ITEM_FIND_RSS,     7, R.string.find_rss);
        menu.add(0, MENU_ITEM_CLEAR,         8, R.string.clear);
        menu.add(1, MENU_ITEM_SETTINGS,        9, R.string.rss_settings);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDataLoading){
            return true;
        }
        TextView emptyView = (TextView) mRssFeedList.getEmptyView();
        int id = item.getItemId();
        switch(id){
        case MENU_ITEM_ADD_FEED:
            showDialog(DIALOG_ID_ADDFEED);
            break;
        case MENU_ITEM_NEWS_LIST:
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Constant.Content.ITEM_URI);
            startActivity(intent);
            break;
        case MENU_ITEM_SUBSCRIBED:
            mFeedView = Constant.State.STATE_VIEW_SUBSCRIBED;
            setTitle(R.string.feed_subscribed);
            mFeedHandler.sendEmptyMessage(MSG_VIEW_SUBSCRIBED);
            break;
        case MENU_ITEM_MANAGE_SUB:
            mFeedView = Constant.State.STATE_VIEW_MANAGE_SUB;
            setTitle(R.string.manager_sub);
            emptyView.setText(R.string.manager_sub);
            mFeedHandler.sendEmptyMessage(MSG_VIEW_SUBSCRIBED);
            mFeedListAdapter.notifyDataSetChanged();
            break;
        case MENU_ITEM_BUNDLES:
            mFeedView = Constant.State.STATE_VIEW_BUNDLES;
            setTitle(R.string.addbybundlestitle);
            mFeedHandler.sendEmptyMessage(MSG_VIEW_BUNDLES);
            break;
        case MENU_ITEM_CUSTOM:
            mFeedView = Constant.State.STATE_VIEW_CUSTOM;
            setTitle(R.string.custom);
            mFeedHandler.sendEmptyMessage(MSG_VIEW_CUSTOM);
            break;
        case MENU_ITEM_MANAGE_CUSTOM:
            mFeedView = Constant.State.STATE_VIEW_MANAGE_CUSTOM;
            setTitle(R.string.manage_custom);
//            mFeedHandler.sendEmptyMessage(MSG_VIEW_CUSTOM);
            emptyView.setText(R.string.manage_custom);
            mFeedListAdapter.notifyDataSetChanged();
            break;
        case MENU_ITEM_FIND_RSS:
            intent = new Intent(this, RSSWebView.class);
            startActivity(intent);
            break;
        case MENU_ITEM_CLEAR:
            mFeedHandler.sendEmptyMessage(MSG_CLEAR_FEEDS);
            break;
        case MENU_ITEM_SETTINGS:
            intent = new Intent("com.android.rss.intent.action.RSSAPP_SETTINGS");
            startActivity(intent);
            break;
        default:
            break;
        }
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(mFeedView == Constant.State.STATE_VIEW_CUSTOM){
            String opmlUrl = mFeedUrl.getText().toString();
            if(!TextUtils.isEmpty(opmlUrl)){
                OpmlParser opmlParser = new OpmlParser(this, opmlUrl);
                opmlParser.start();
            }
            return ;
        }
        String title = mFeedTitle.getText().toString();
        String address = mFeedUrl.getText().toString();
        if(TextUtils.isEmpty(address)){
            return ;
        }
        String feedGuid = String.valueOf(address.hashCode());
        Cursor c = null;
        String selection = Constant.Content.FEED_GUID + "=" + feedGuid;
        boolean existed = false;
        try{
            c = getContentResolver().query(Constant.Content.FEED_URI, null, selection, null, null);
            if(c != null){
                existed = c.getCount() > 0;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(c != null){
                c.close();
            }
        }
        if(existed){
            String added = getResources().getString(R.string.recent_added);
            Toast.makeText(this, added, Toast.LENGTH_SHORT).show();
            return ;
        }
        FeedInfo info = new FeedInfo();
        info.feedTitle = title;
        info.feedUrl = address;
        info.feedState = Constant.State.STATE_WAITING;
        info.feedIconState = "unknown";
        info.feedGuid = feedGuid;
        info.feedIsBundle = 0;
        ContentValues values = new ContentValues();
        values.put(Constant.Content.FEED_TITLE, info.feedTitle);
        values.put(Constant.Content.FEED_URL, info.feedUrl);
        values.put(Constant.Content.FEED_STATE, info.feedState);
        values.put(Constant.Content.FEED_ICON_STATE, info.feedIconState);
        values.put(Constant.Content.FEED_GUID, String.valueOf(info.feedUrl.hashCode()));
        values.put(Constant.Content.FEED_IS_BUNDLE, info.feedIsBundle);
        Uri uri = getContentResolver().insert(Constant.Content.FEED_URI, values);
        Intent intent = new Intent(Constant.Intent.INTENT_RSSAPP_MANUALREFRESH);
        startService(intent);
        info.feedId = (int) ContentUris.parseId(uri);
        mFeedListAdapter.add(info);
        mFeedListAdapter.notifyDataSetChanged();
    }

    private void showPopupWindow(int position, Integer integer){
        int firstVisible = mRssFeedList.getFirstVisiblePosition();
        ViewGroup viewGroup = (ViewGroup) mRssFeedList.getChildAt(position - firstVisible);
        int count = viewGroup.getChildCount();
        View view = null;
        for(int i=0; i<count;i++){
            view = viewGroup.getChildAt(i);
            if(view instanceof FrameLayout){
                break;
            }
        }
        int frameWidth = 0;
        int frameHeight = 0;
        if(view != null){
            frameWidth = view.getWidth();
            frameHeight = view.getHeight();
        }
        View contentView = null;
        if(mPopupWindow == null){
            contentView = getLayoutInflater().inflate(R.layout.option, null);
            mPopupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, frameHeight);
        }else{
            contentView = mPopupWindow.getContentView();
        }
        View button = contentView.findViewById(R.id.update_feed);
        button.setTag(integer);
        button.setOnClickListener(this);
        button = contentView.findViewById(R.id.delete_feed);
        button.setTag(integer);
        button.setOnClickListener(this);
        int xOff = -frameWidth;
        int yOff = -frameHeight;
        if(!mPopupWindow.isShowing()){
            mPopupWindow.setFocusable(true);
            mPopupWindow.setTouchable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mPopupWindow.showAsDropDown(view, xOff, yOff);
        }
    }
    @Override
    public void onBackPressed() {
        if(mDataLoading){
            return ;
        }
        Log.d("weiliuzhao", "mFeedView = " + mFeedView);
        TextView emptyView = (TextView) mRssFeedList.getEmptyView();
        if(mFeedView == Constant.State.STATE_VIEW_MANAGE_CUSTOM){
            mFeedView = Constant.State.STATE_VIEW_CUSTOM;
//            mFeedHandler.sendEmptyMessage(MSG_VIEW_CUSTOM);
            emptyView.setText(R.string.custom);
            mFeedListAdapter.notifyDataSetChanged();
            setTitle(R.string.custom);
        }else if(mFeedView == Constant.State.STATE_VIEW_MANAGE_SUB){
            mFeedView = Constant.State.STATE_VIEW_SUBSCRIBED;
//            mFeedHandler.sendEmptyMessage(MSG_VIEW_SUBSCRIBED);
            emptyView.setText(R.string.feed_subscribed);
            mFeedListAdapter.notifyDataSetChanged();
            setTitle(R.string.feed_subscribed);
        }else if(mFeedView == Constant.State.STATE_VIEW_MANAGE_CUSTOM){
            mFeedView = Constant.State.STATE_VIEW_BUNDLES;
//            mFeedHandler.sendEmptyMessage(MSG_VIEW_SUBSCRIBED);
            emptyView.setText(R.string.addbybundlestitle);
            mFeedListAdapter.notifyDataSetChanged();
            setTitle(R.string.addbybundlestitle);
        }else{
            showDialog(DIALOG_ID_EXIT);
        }
    }
    private void updateListView(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFeedListAdapter.notifyDataSetChanged();
            }
        });
    }
    private void clearAdapter(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mFeedListAdapter != null){
                    mFeedListAdapter.clear();
                    mFeedListAdapter.notifyDataSetChanged();
                }
            }
        });
    }
    private void updateProgress(boolean visible){
        final boolean v = visible;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setProgressBarIndeterminateVisibility(v);
            }
        });
    }
    public boolean feedSubscribed(FeedInfo info){
        if(info == null){
            return false;
        }
        String selection = Constant.Content.FEED_GUID + "=" + info.feedGuid;
        boolean existed = false;
        Cursor c = null;
        try{
            c = getContentResolver().query(Constant.Content.FEED_URI, null, selection, null, null);
            if(c != null){
                if(c.moveToFirst()){
                    existed = c.getCount() > 0;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            existed = false;
        }finally{
            if(c != null){
                c.close();
            }
        }
        return existed;
    }
    @Override
    public void onRefresh() {
        mFeedHandler.sendEmptyMessage(MSG_REFRESH_STATE);
    }
    private void updateFeedState(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                int size = mFeedListAdapter.getCount();
                FeedInfo feedInfo = null;
                for(i = 0; i < size; i++){
                    feedInfo = mFeedListAdapter.getItem(i);
                    feedInfo.feedState = Constant.State.STATE_WAITING;
                }
                mFeedListAdapter.notifyDataSetChanged();
                
                ContentValues values = new ContentValues();
                values.put(Constant.Content.FEED_STATE, Constant.State.STATE_WAITING);
                getContentResolver().update(Constant.Content.FEED_URI, values, null, null);
                Intent intent = new Intent(Constant.Intent.INTENT_RSSAPP_MANUALREFRESH);
                startService(intent);
            }
        });
    }
    
    private void updateWidget(){
        Intent intent = new Intent(Constant.Intent.INTENT_RSSAPP_WIDGET_UPDATE);
        sendBroadcast(intent);
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        FeedInfo info = mFeedInfos.get(position);
        Intent intent = new Intent(this, RSSNewsList.class);
        intent.putExtra(KEY_FEED_ID, info.feedId);
        startActivity(intent);
    }
    @Override
    public void onTabChange(int id) {
        TextView emptyView = (TextView) mRssFeedList.getEmptyView();
        switch(id){
        case R.id.tab_subscribed:
            mFeedView = Constant.State.STATE_VIEW_SUBSCRIBED;
            setTitle(R.string.feed_subscribed);
            mFeedHandler.sendEmptyMessage(MSG_VIEW_SUBSCRIBED);
            break;
        case R.id.tab_bundle:
            mFeedView = Constant.State.STATE_VIEW_BUNDLES;
            setTitle(R.string.addbybundlestitle);
            mFeedHandler.sendEmptyMessage(MSG_VIEW_BUNDLES);
            break;
        case R.id.tab_custom:
            mFeedView = Constant.State.STATE_VIEW_CUSTOM;
            setTitle(R.string.custom);
            mFeedHandler.sendEmptyMessage(MSG_VIEW_CUSTOM);
            break;
        default:
            break;
        }
    }
    @Override
    public void onClick(){
        if(mDataLoading){
            mStopFetching = true;
        }
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        menu.add(0, CONTEXT_MENU_OPEN,   0, R.string.context_menu_open);
        menu.add(0, CONTEXT_MENU_RENAME, 0, R.string.context_menu_rename);
//        menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.context_menu_delete);
        menu.add(0, CONTEXT_MENU_COPYURL, 0, R.string.context_menu_copyurl);
        menu.add(0, CONTEXT_MENU_DETAIL, 0, R.string.context_menu_property);
        AdapterView.AdapterContextMenuInfo info =  (AdapterView.AdapterContextMenuInfo)menuInfo;
        menu.setHeaderTitle(mFeedListAdapter.getItem(info.position).feedTitle);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int contextMenuId = item.getItemId();
        FeedInfo feedInfo = mFeedInfos.get(info.position);
        Intent intent = null;
        switch(contextMenuId){
        case CONTEXT_MENU_OPEN:
            intent = new Intent(this, RSSNewsList.class);
            intent.putExtra(KEY_FEED_ID, feedInfo.feedId);
            startActivity(intent);
            break;
        case CONTEXT_MENU_RENAME:
            mContextSelectedInfo = feedInfo;
            showDialog(DIALOG_ID_RENAME);
            break;
        case CONTEXT_MENU_DELETE:
            break;
        case CONTEXT_MENU_DETAIL:
//            showDialog(DIALOG_ID_DETAIL);
            DetailFragment fragment = DetailFragment.getInstance(this);
            fragment.setFeedInfo(feedInfo);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if(prev != null){
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            fragment.show(ft, "dialog");
            break;
        case CONTEXT_MENU_COPYURL:
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("copyurl", feedInfo.feedUrl));
            Toast.makeText(this, R.string.toast_url_copied, Toast.LENGTH_SHORT).show();
            break;
        default:
            break;
        }
        return true;
    }
    
    private static class DetailFragment extends DialogFragment{
        private Context mContext;
        private FeedInfo mFeedInfo;
        public static DetailFragment getInstance(Context context){
            return new DetailFragment(context);
        }
        private DetailFragment(Context context){
            mContext = context;
        }
        public void setFeedInfo(FeedInfo info){
            mFeedInfo = info;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = null;
            TextView view = null;
            AlertDialog dialog = null;
            builder = new AlertDialog.Builder(mContext);
            view = new TextView(mContext);
            view.setAutoLinkMask(Linkify.WEB_URLS);
            view.setTextIsSelectable(true);
            view.setTextColor(Color.WHITE);
            view.setTextSize(20.0f);
            String titleLabel = mContext.getResources().getString(R.string.title);
            String urlLabel  = mContext.getResources().getString(R.string.url);
            String dateLabel = mContext.getResources().getString(R.string.date);
            long dateMis = mFeedInfo.feedPubdate == 0 ? System.currentTimeMillis() : mFeedInfo.feedPubdate;
            String date = DateFormat.getDateFormat(getActivity()).format(new Date(dateMis));
            String time = DateFormat.getTimeFormat(getActivity()).format(new Date(dateMis));
            String text = "";
            text += titleLabel + ":\n" + mFeedInfo.feedTitle + "\n";
            text += urlLabel + ":\n" + mFeedInfo.feedUrl + "\n";
            text += dateLabel + ":\n" + date + " " + time;
            String htmlCode = getString(R.string.property_title_format, titleLabel, mFeedInfo.feedTitle)
                    + getString(R.string.property_url_format, urlLabel, mFeedInfo.feedUrl)
                    + getString(R.string.property_date_format, dateLabel, date + " " + time);
            Spanned spannable = Html.fromHtml(htmlCode);
            view.setText(spannable);
            view.setTextIsSelectable(false);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            view.setLayoutParams(params);
            builder.setView(view);
            builder.setTitle(R.string.context_menu_property);
            builder.setPositiveButton(R.string.ok, null);
            dialog = builder.create();
            return dialog;
        }
    }
}
