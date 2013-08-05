package com.android.rss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.rss.common.ExpandInfo;
import com.android.rss.common.FeedInfo;
import com.android.rss.common.Constant;
import com.android.rss.service.RSSService;
import com.android.rss.util.Log;

public class FeedSource extends ExpandableListActivity implements OnClickListener, 
                                    DialogInterface.OnClickListener{

    private static final String TAG = "FeedSource";
    private ExpandableListView mExpandableListView;
    private ArrayList<ExpandInfo> mExpandInfos;
    private ExpandableListAdapter mExpandableListAdapter;

    private static final int MSG_INIT_FEED = 1;
    private static final int MSG_UPDATE_FINISHED = 2;
    private static final String KEY_FEED_ID = "feedId";
    private static final String KEY_FEED_SORT = "feed_sort";
    private Handler mHandler = null;
    private FeedHandler mFeedHandler;
    private HashMap<String, Bitmap>  mHashMap = null;
    private EditText mFeedTitle;
    private AutoCompleteTextView mFeedAddress;
    private PopupWindow mPopupWindow;
    private boolean mFeedSubscribed = true;
    private SubscribedFeedAdapter mSubscribedFeedAdapter;
    private Spinner mFeedClassSpinner = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_source);
        mExpandableListView = getExpandableListView();
        mExpandInfos = new ArrayList<ExpandInfo>();
        setTitle(R.string.feeds);
        mExpandableListAdapter = new ExpandableListAdapter(this);
        mExpandableListView.setAdapter(mExpandableListAdapter);
        mHandler = new Handler();
        mHashMap = new HashMap<String, Bitmap>();
        HandlerThread thread = new HandlerThread("RssAppSettings");
        thread.start();
        mFeedHandler = new FeedHandler(this, thread.getLooper());
        mFeedHandler.sendEmptyMessage(MSG_INIT_FEED);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.Intent.INTENT_RSSAPP_UPDATE_FINISHED);
        registerReceiver(mSubscribeReceiver, filter);
        Intent intent = new Intent(this, RSSService.class);
        startService(intent);
    }
    
    private void initArrayList(){
        FeedInfo feedInfo = null;
        ExpandInfo expandInfo = null;
        String []feeds = getResources().getStringArray(R.array.bundle_rss);
        expandInfo = new ExpandInfo();
        expandInfo.feedClass = "Sina";
        expandInfo.feedClassGuid = String.valueOf(expandInfo.feedClass.hashCode());
        expandInfo.feedInfos = new ArrayList<FeedInfo>();
        for(int i = 0; i < feeds.length; i+=2){
            feedInfo = new FeedInfo();
            feedInfo.feedTitle = feeds[i];
            feedInfo.feedUrl = feeds[i+1];
            feedInfo.feedClass = expandInfo.feedClass;
            feedInfo.feedClassGuid = expandInfo.feedClassGuid;
            feedInfo.feedIsBundle = 1;
            feedInfo.feedState = feedState(feedInfo.feedUrl.hashCode(), feedInfo);
            expandInfo.feedInfos.add(feedInfo);
        }
        mExpandInfos.add(expandInfo);
        /*
        feeds = getResources().getStringArray(R.array.rss_reily);
        expandInfo = new ExpandInfo();
        expandInfo.feedClass = "Reilay";
        expandInfo.feedClassGuid = String.valueOf(expandInfo.feedClass.hashCode());
        expandInfo.feedInfos = new ArrayList<FeedInfo>();
        for(int i = 0; i < feeds.length; i+=2){
            feedInfo = new FeedInfo();
            feedInfo.feedTitle = feeds[i];
            feedInfo.feedUrl = feeds[i+1];
            feedInfo.feedClass = expandInfo.feedClass;
            feedInfo.feedClassGuid = expandInfo.feedClassGuid;
            feedInfo.feedIsBundle = 1;
            feedInfo.feedState = feedState(feedInfo.feedUrl.hashCode(), feedInfo);
            expandInfo.feedInfos.add(feedInfo);
        }
        mExpandInfos.add(expandInfo);
        
        feeds = getResources().getStringArray(R.array.rss_hexun);
        expandInfo = new ExpandInfo();
        expandInfo.feedClass = "Hexun";
        expandInfo.feedClassGuid = String.valueOf(expandInfo.feedClass.hashCode());
        expandInfo.feedInfos = new ArrayList<FeedInfo>();
        for(int i = 0; i < feeds.length; i+=2){
            feedInfo = new FeedInfo();
            feedInfo.feedTitle = feeds[i];
            feedInfo.feedUrl = feeds[i+1];
            feedInfo.feedClass = expandInfo.feedClass;
            feedInfo.feedClassGuid = expandInfo.feedClassGuid;
            feedInfo.feedIsBundle = 1;
            feedInfo.feedState = feedState(feedInfo.feedUrl.hashCode(), feedInfo);
            expandInfo.feedInfos.add(feedInfo);
        }
        mExpandInfos.add(expandInfo);
        
        feeds = getResources().getStringArray(R.array.rss_qianchengwuyou);
        expandInfo = new ExpandInfo();
        expandInfo.feedClass = "QianchengWuyou";
        expandInfo.feedClassGuid = String.valueOf(expandInfo.feedClass.hashCode());
        expandInfo.feedInfos = new ArrayList<FeedInfo>();
        for(int i = 0; i < feeds.length; i+=2){
            feedInfo = new FeedInfo();
            feedInfo.feedTitle = feeds[i];
            feedInfo.feedUrl = feeds[i+1];
            feedInfo.feedClass = expandInfo.feedClass;
            feedInfo.feedClassGuid = expandInfo.feedClassGuid;
            feedInfo.feedIsBundle = 1;
            feedInfo.feedState = feedState(feedInfo.feedUrl.hashCode(), feedInfo);
            
            expandInfo.feedInfos.add(feedInfo);
        }
        mExpandInfos.add(expandInfo);
        
        feeds = getResources().getStringArray(R.array.rss_other);
        expandInfo = new ExpandInfo();
        expandInfo.feedClass = "Other";
        expandInfo.feedClassGuid = String.valueOf(expandInfo.feedClass.hashCode());
        expandInfo.feedInfos = new ArrayList<FeedInfo>();
        for(int i = 0; i < feeds.length; i+=2){
            feedInfo = new FeedInfo();
            feedInfo.feedTitle = feeds[i];
            feedInfo.feedUrl = feeds[i+1];
            feedInfo.feedClass = expandInfo.feedClass;
            feedInfo.feedClassGuid = expandInfo.feedClassGuid;
            feedInfo.feedIsBundle = 1;
            feedInfo.feedState = feedState(feedInfo.feedUrl.hashCode(), feedInfo);
            expandInfo.feedInfos.add(feedInfo);
        }
        mExpandInfos.add(expandInfo);
        */
    }
    class ViewHolder{
        ImageView feedIcon;
        TextView feedTitle;
        ImageButton subscribe;
        ImageButton remove;
        ImageButton refresh;
        View progress;
    }
    class Tag{
        int groupPosition;
        int childPosition;
    }
    class ExpandableListAdapter extends BaseExpandableListAdapter{
        
        private LayoutInflater mInflater = null;
        public ExpandableListAdapter(Context context){
            mInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mExpandInfos.get(groupPosition).feedInfos.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                boolean isLastChild, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.rss_feed_item, null);
                holder.feedIcon = (ImageView)convertView.findViewById(R.id.rss_feed_img);
                holder.feedTitle = (TextView) convertView.findViewById(R.id.rss_feed_name);
                holder.subscribe = (ImageButton) convertView.findViewById(R.id.rss_feed_subscribe);
                holder.remove = (ImageButton) convertView.findViewById(R.id.rss_feed_remove);
                holder.refresh = (ImageButton) convertView.findViewById(R.id.rss_feed_refresh);
                holder.subscribe.setOnClickListener(FeedSource.this);
                holder.remove.setOnClickListener(FeedSource.this);
                holder.refresh.setOnClickListener(FeedSource.this);
                holder.progress = convertView.findViewById(R.id.subscribing);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            
            Tag tag = new Tag();
            tag.groupPosition = groupPosition;
            tag.childPosition = childPosition;
            holder.feedIcon.setTag(tag);
            holder.subscribe.setTag(tag);
            holder.remove.setTag(tag);
            holder.feedTitle.setTag(tag);
            holder.refresh.setTag(tag);
            FeedInfo info = (FeedInfo) getChild(groupPosition, childPosition);
            Log.d(TAG, "info = " + info);
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
                if(mFeedSubscribed){
                    holder.feedIcon.setVisibility(View.VISIBLE);
                    if(info.feedBitmap != null){
                        holder.feedIcon.setImageBitmap(info.feedBitmap);
                    }else{
                        holder.feedIcon.setImageResource(R.drawable.ic_rss_small);
                    }
                    if(info.feedState == Constant.State.STATE_WAITING || info.feedState == Constant.State.STATE_SUBSCRIBING){
                        if(info.itemCount > 0){
                            holder.feedTitle.setEnabled(true);
                            holder.feedTitle.setTextColor(Color.BLACK);
                        }else{
                            holder.feedTitle.setEnabled(false);
                            holder.feedTitle.setTextColor(Color.GRAY);
                        }
                        holder.progress.setVisibility(View.VISIBLE);
                        holder.subscribe.setVisibility(View.INVISIBLE);
                        holder.remove.setVisibility(View.INVISIBLE);
                        holder.refresh.setVisibility(View.INVISIBLE);
                    }else{
                        holder.feedTitle.setTextColor(Color.BLACK);
                        holder.progress.setVisibility(View.INVISIBLE);
                        holder.subscribe.setVisibility(View.INVISIBLE);
                        holder.remove.setVisibility(View.INVISIBLE);
                        holder.refresh.setVisibility(View.VISIBLE);
                        holder.feedTitle.setEnabled(true);
                    }
                    holder.feedTitle.setOnClickListener(FeedSource.this);
                    holder.feedTitle.setBackgroundResource(android.R.drawable.list_selector_background);
                }else{
                    holder.feedIcon.setVisibility(View.GONE);
                    
                    if(info.feedState == Constant.State.STATE_WAITING || info.feedState == Constant.State.STATE_SUBSCRIBING){
                        holder.feedTitle.setTextColor(Color.GRAY);
                        holder.progress.setVisibility(View.VISIBLE);
                        holder.subscribe.setVisibility(View.INVISIBLE);
                        holder.remove.setVisibility(View.INVISIBLE);
                        holder.refresh.setVisibility(View.INVISIBLE);
                        holder.feedTitle.setEnabled(false);
                    }else if(info.feedState == Constant.State.STATE_NO_SUBSCRIBED){
                        holder.feedTitle.setTextColor(Color.GRAY);
                        holder.progress.setVisibility(View.INVISIBLE);
                        holder.subscribe.setVisibility(View.VISIBLE);
                        holder.remove.setVisibility(View.INVISIBLE);
                        holder.refresh.setVisibility(View.INVISIBLE);
                        holder.feedTitle.setEnabled(false);
                    }else{
                        holder.feedTitle.setTextColor(Color.BLACK);
                        holder.progress.setVisibility(View.INVISIBLE);
                        holder.subscribe.setVisibility(View.INVISIBLE);
                        holder.remove.setVisibility(View.VISIBLE);
                        holder.refresh.setVisibility(View.INVISIBLE);
                        holder.feedTitle.setEnabled(false);
                    }
                }
            }
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mExpandInfos.get(groupPosition).feedInfos.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mExpandInfos.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return mExpandInfos.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
            }
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setTextColor(Color.BLACK);
            tv.setPadding(60, 0, 0, 0);
            ExpandInfo info = (ExpandInfo) getGroup(groupPosition);
            tv.setText(info.feedClass);
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
        
    }
    class FeedAdder implements Runnable{
        private FeedInfo mFeedInfo;
        private int mGroupPosition;
        public FeedAdder(FeedInfo info, int groupPosition){
            mFeedInfo = info;
            mGroupPosition = groupPosition;
        }
        @Override
        public void run() {
            mExpandInfos.get(mGroupPosition).feedInfos.add(mFeedInfo);
            mExpandableListAdapter.notifyDataSetChanged();
        }
    }
    class FeedClassAdder implements Runnable{
        private ExpandInfo mFeedInfo;
        public FeedClassAdder(ExpandInfo info){
            mFeedInfo = info;
        }
        @Override
        public void run() {
            mExpandInfos.add(mFeedInfo);
            mExpandableListAdapter.notifyDataSetChanged();
        }
    }
    private class BundleAdapter extends ArrayAdapter<FeedInfo>{
        private LayoutInflater mInflater;
        public BundleAdapter(Context context, ArrayList<FeedInfo> list) {
            super(context, 0,list);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }
    
    BroadcastReceiver mSubscribeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = null;
            if(intent != null){
                action = intent.getAction();
                Log.d(TAG ,"action = " + action);
                if(Constant.Intent.INTENT_RSSAPP_UPDATE_FINISHED.equals(action)){
                    int feedId = intent.getIntExtra(KEY_FEED_ID, -1);
                    Message msg = mFeedHandler.obtainMessage(MSG_UPDATE_FINISHED, feedId, 0);
                    mFeedHandler.sendMessage(msg);
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
            int what = msg.what;
            switch(what){
            case MSG_INIT_FEED:
                init();
                break;
            case MSG_UPDATE_FINISHED:
                updateFinish(msg.arg1);
                break;
            default:
                break;
            }
        }
        
        public void init() {
            clearList();
            Log.d(TAG, "size = " + mExpandInfos.size());
            if(mFeedSubscribed){
                fetchSubscribedFeed();
            }else{
                fetchHistoryFeed();
                initArrayList();
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mExpandableListAdapter.notifyDataSetChanged();                    
                }
            });
        }
        private void fetchHistoryFeed(){
            fetchFeedClass(Constant.Content.HISTORY_URI);
            Cursor c = null;
            FeedInfo info = null;
            String selection = null;
            int i = 0;
            ArrayList<FeedInfo> feedInfos = null;
            ExpandInfo expandInfo = null;
            while(i < mExpandInfos.size()){
                expandInfo = mExpandInfos.get(i);
                feedInfos = new ArrayList<FeedInfo>();
                expandInfo.feedInfos = feedInfos;
                selection = Constant.Content.FEED_CLASS_GUID + "=" + expandInfo.feedClassGuid;
                try{
                    c = mContext.getContentResolver().query(Constant.Content.HISTORY_URI, null, selection, null, null);
                    if(c != null){
                        if(c.moveToFirst()){
                            do{
                                info = new FeedInfo();
                                info.feedId = -1;
                                info.feedTitle = c.getString(c.getColumnIndex(Constant.Content.HISTORY_TITLE));
                                info.feedUrl = c.getString(c.getColumnIndex(Constant.Content.HISTORY_URL));
                                info.feedGuid = c.getString(c.getColumnIndex(Constant.Content.FEED_GUID));
                                info.feedClass = expandInfo.feedClass;
                                info.feedClassGuid = expandInfo.feedClassGuid;
                                info.feedIconState = "unknown";
                                info.feedIsBundle = 0;
                                if(info.feedUrl != null){
                                    info.feedGuid = String.valueOf(info.feedUrl.hashCode());
                                    info.feedState = feedState(info.feedUrl.hashCode(), info);
                                }else{
                                    info.feedState = Constant.State.STATE_NO_SUBSCRIBED;
                                }
                                feedInfos.add(info);
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
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mExpandableListAdapter.notifyDataSetChanged();
                    }
                });
                i++;
            }
        }
        private void fetchFeedClass(Uri uri){
            String []projection = new String[]{Constant.Content.FEED_CLASS, Constant.Content.FEED_CLASS_GUID};
            String orderBy = Constant.Content._ID + " asc";
            Cursor c = null;
            ExpandInfo info = null;
            try{
                
                c = mContext.getContentResolver().query(uri, projection, null, null, orderBy);
                if(c != null){
                    if(c.moveToFirst()){
                        do{
                            info = new ExpandInfo();
                            info.feedClass = c.getString(c.getColumnIndex(Constant.Content.FEED_CLASS));
                            info.feedClassGuid = c.getString(c.getColumnIndex(Constant.Content.FEED_CLASS_GUID));
                            mExpandInfos.add(info);
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
            /*
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mExpandableListAdapter.notifyDataSetChanged();
                }
            });
            */
        }
        private void fetchSubscribedFeed(){
            fetchFeedClass(Constant.Content.FEED_URI);
            String []projection = new String[]{Constant.Content._ID, Constant.Content.FEED_STATE, 
                    Constant.Content.FEED_ICON, Constant.Content.FEED_TITLE, 
                    Constant.Content.FEED_URL, Constant.Content.FEED_ORI_TITLE, 
                    Constant.Content.FEED_GUID, Constant.Content.ITEM_COUNT};
            String orderBy = Constant.Content._ID + " asc";
            String selection = null;
            Cursor c = null;
            FeedInfo info = null;
            
            int i = 0;
            ArrayList<FeedInfo> feedInfos = null;
            ExpandInfo expandInfo = null;
            while(i < mExpandInfos.size()){
                expandInfo = mExpandInfos.get(i);
                feedInfos = new ArrayList<FeedInfo>();
                expandInfo.feedInfos = feedInfos;
                selection = Constant.Content.FEED_CLASS_GUID + "=" + expandInfo.feedClassGuid;
                try{
                    c = mContext.getContentResolver().query(Constant.Content.FEED_URI, projection, selection, null, orderBy);
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
                                info.feedBitmap = getIcon(c, info);
                                info.feedClass = expandInfo.feedClass;
                                info.feedClassGuid = expandInfo.feedClassGuid;
                                feedInfos.add(info);
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
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mExpandableListAdapter.notifyDataSetChanged();
                    }
                });
                i++;
            }
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
            FeedInfo info = null;
            boolean finded = false;
            ExpandInfo expandInfo = null;
            int groupSize = mExpandInfos.size();
            int childSize = 0;
            for(i = 0; i < groupSize; i++){
                expandInfo = mExpandInfos.get(i);
                childSize = expandInfo.feedInfos.size();
                for(int j = 0;j<childSize;j++){
                    info = expandInfo.feedInfos.get(j);
                    if(info.feedId == newInfo.feedId){
                        info.feedState = newInfo.feedState;
                        info.feedBitmap = newInfo.feedBitmap;
                        info.feedOriTitle = newInfo.feedOriTitle;
                        info.itemCount = newInfo.itemCount;
                        finded = true;
                        break;
                    }
                }
            }
            if(finded){
                Log.d(TAG, "feedState = " + info.feedState + " , bitmap = " + info.feedBitmap);
                Log.d(TAG ,"find the updated item : " + info.toString());
                if(info.feedState == Constant.State.STATE_FAILURE){
                    String where = Constant.Content._ID + "=" + info.feedId;
                    mContext.getContentResolver().delete(Constant.Content.FEED_URI, where, null);
                    info.feedState = Constant.State.STATE_NO_SUBSCRIBED;
                    final FeedInfo feed = info;
                    mExpandInfos.remove(expandInfo);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String text = getResources().getString(R.string.refresh_failed);
                            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                            mExpandableListAdapter.notifyDataSetChanged();
                        }
                    });
                }
                mHandler.post(new Runnable() {
                    
                    @Override
                    public void run() {
                        mExpandableListAdapter.notifyDataSetChanged();
                    }
                });
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
        Tag tag = (Tag) v.getTag();
        int id = v.getId();
        FeedInfo info = mExpandInfos.get(tag.groupPosition).feedInfos.get(tag.childPosition);
        Log.d(TAG, "position = " + tag.groupPosition + " , child = " + tag.childPosition + " : " + info.feedTitle + " , class = " + info.feedClass);
        String where = null;
        switch(id){
        case R.id.rss_feed_subscribe:
            info.feedState = Constant.State.STATE_WAITING;
            ContentValues values = new ContentValues();
            values.put(Constant.Content.FEED_TITLE, info.feedTitle);
            values.put(Constant.Content.FEED_URL, info.feedUrl);
            values.put(Constant.Content.FEED_STATE, info.feedState);
            values.put(Constant.Content.FEED_ICON_STATE, info.feedIconState);
            values.put(Constant.Content.FEED_CLASS, info.feedClass);
            values.put(Constant.Content.FEED_CLASS_GUID, info.feedClassGuid);
            values.put(Constant.Content.FEED_GUID, String.valueOf(info.feedUrl.hashCode()));
            values.put(Constant.Content.FEED_IS_BUNDLE, info.feedIsBundle);
            Uri uri = getContentResolver().insert(Constant.Content.FEED_URI, values);
            info.feedId = (int) ContentUris.parseId(uri);
            Log.d(TAG,"info.feedId = " + info.feedId);
            break;
        case R.id.rss_feed_remove:
            info.feedBitmap = null;
            info.feedState = Constant.State.STATE_NO_SUBSCRIBED;
            where = Constant.Content.FEED_GUID + "=" + String.valueOf(info.feedUrl.hashCode());
            getContentResolver().delete(Constant.Content.FEED_URI, where, null);
            where = Constant.Content._ID + "=" + info.feedId;
            getContentResolver().delete(Constant.Content.FEED_INDEX_URI, where, null);
            getContentResolver().delete(Constant.Content.ITEM_INDEX_URI, where, null);
            where = Constant.Content.FEED_ID + "=" + info.feedId;
            getContentResolver().delete(Constant.Content.ITEM_URI, where, null);
            Log.d(TAG, "feedIsBundle = " + info.feedIsBundle);
            if(info.feedIsBundle == 0 && mFeedSubscribed){
                mExpandInfos.remove(info);
            }
            info.feedId = -1;
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
        mExpandableListAdapter.notifyDataSetChanged();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.add_feed_class, null);
        mFeedTitle = (EditText) view.findViewById(R.id.feed_title);
        mFeedAddress = (AutoCompleteTextView) view.findViewById(R.id.setfeedurl);
        mFeedClassSpinner = (Spinner) view.findViewById(R.id.recent_added_feed);
        ArrayList<FeedInfo> subscribedFeeds = new ArrayList<FeedInfo>();
        mSubscribedFeedAdapter = new SubscribedFeedAdapter(this, subscribedFeeds);
        
        mFeedClassSpinner.setAdapter(mSubscribedFeedAdapter);
        View emptyView = getLayoutInflater().inflate(R.layout.empty_view, null);
        mFeedClassSpinner.setEmptyView(emptyView);
        builder = builder.setView(view);
        builder = builder.setPositiveButton(getResources().getString(R.string.ok), this);
        builder = builder.setNegativeButton(getResources().getString(R.string.cancel), null);
        AlertDialog dialog = builder.create();
        return dialog;
    }
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        mFeedTitle.setText(null);
        mFeedAddress.setText(null);
        mFeedClassSpinner.setSelection(1);
        fillAdapter();
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
                tv.setText(info.feedClass);
            }
            return convertView;
        }
        @Override
        public View getDropDownView(int position, View convertView,
                ViewGroup parent) {
            if(convertView == null){
                convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
            }
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            FeedInfo info = getItem(position);            
            tv.setTextColor(Color.BLACK);
            tv.setSingleLine();
            if(info != null){
                tv.setText(info.feedClass);
            }
            return convertView;
        }
    }
    private void fillAdapter(){
        mSubscribedFeedAdapter.clear();
        Cursor c = null;
        FeedInfo info = null;
        String []projections = new String[]{Constant.Content.FEED_CLASS, Constant.Content.FEED_CLASS_GUID};
        try{
            c = getContentResolver().query(Constant.Content.FEED_URI, projections, null, null, null);
            if(c != null){
                if(c.moveToFirst()){
                    do{
                        info = new FeedInfo();
                        info.feedClass = c.getString(c.getColumnIndex(Constant.Content.FEED_CLASS));
                        info.feedClassGuid = c.getString(c.getColumnIndex(Constant.Content.FEED_CLASS_GUID));
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
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mFeedSubscribed){
            menu.findItem(2).setTitle(R.string.feed_all);
        }else{
            menu.findItem(2).setTitle(R.string.feed_subscribed);
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.addfeed_prefenrence);
        menu.add(0, 2, 1, R.string.feed_subscribed);
        menu.add(0, 1, 2, R.string.articlelist);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
        case 0:
            showDialog(0);
            break;
        case 1:
            Intent intent = new Intent(this, RSSNewsList.class);
            startActivity(intent);
            break;
        case 2:
            if(mFeedSubscribed){
                mFeedSubscribed = false;
            }else{
                mFeedSubscribed = true;
            }
            mFeedHandler.sendEmptyMessage(MSG_INIT_FEED);
            break;
        default:
            break;
        }
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        
        String title = mFeedTitle.getText().toString();
        String address = mFeedAddress.getText().toString();
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
        FeedInfo feedClass = (FeedInfo) mFeedClassSpinner.getSelectedItem();
        int i = 0;
        int size = mExpandInfos.size();
        ExpandInfo expandInfo = null;
        boolean finded = false;
        while(i < size){
            expandInfo = mExpandInfos.get(i);
            if(feedClass.feedClassGuid.equals(expandInfo.feedClassGuid)){
                finded = true;
                break;
            }
            i++;
        }
        FeedInfo info = new FeedInfo();
        info.feedTitle = title;
        info.feedUrl = address;
        info.feedState = Constant.State.STATE_WAITING;
        info.feedIconState = "unknown";
        info.feedGuid = feedGuid;
        info.feedClass = expandInfo.feedClass;
        info.feedClassGuid = expandInfo.feedClassGuid;
        info.feedIsBundle = 0;
        if(!finded){
            expandInfo = new ExpandInfo();
            expandInfo.feedClass = feedClass.feedClass;
            expandInfo.feedClassGuid = feedClass.feedClassGuid;
            expandInfo.feedInfos = new ArrayList<FeedInfo>();
            expandInfo.feedInfos.add(info);
            mExpandInfos.add(expandInfo);
        }else{
            expandInfo.feedInfos.add(info);
        }
        ContentValues values = new ContentValues();
        values.put(Constant.Content.FEED_TITLE, info.feedTitle);
        values.put(Constant.Content.FEED_URL, info.feedUrl);
        values.put(Constant.Content.FEED_STATE, info.feedState);
        values.put(Constant.Content.FEED_ICON_STATE, info.feedIconState);
        values.put(Constant.Content.FEED_CLASS, info.feedClass);
        values.put(Constant.Content.FEED_CLASS_GUID, info.feedClassGuid);
        values.put(Constant.Content.FEED_GUID, String.valueOf(info.feedUrl.hashCode()));
        values.put(Constant.Content.FEED_IS_BUNDLE, info.feedIsBundle);
        Uri uri = getContentResolver().insert(Constant.Content.FEED_URI, values);
        info.feedId = (int) ContentUris.parseId(uri);
        mExpandableListAdapter.notifyDataSetChanged();
    }
    @Override
    public void onBackPressed() {
        if(!mFeedSubscribed){
            mFeedSubscribed = true;
            mFeedHandler.sendEmptyMessage(MSG_INIT_FEED);
        }else{
            super.onBackPressed();
        }
    }
    private void clearList(){
        int size = mExpandInfos.size();
        int i = 0;
        ExpandInfo expandInfo = null;
        for(i = 0; i < size; i++){
            expandInfo = mExpandInfos.get(i);
            expandInfo.feedInfos.clear();
        }
        mExpandInfos.clear();
    }
    private int feedState(int hashCode, FeedInfo info){
        String selection = Constant.Content.FEED_GUID + "=" + String.valueOf(hashCode);
        String []projection = new String[]{Constant.Content._ID, Constant.Content.FEED_STATE, Constant.Content.FEED_ICON};
        int state = Constant.State.STATE_NO_SUBSCRIBED;
        Cursor c = null;
        try{
            c = getContentResolver().query(Constant.Content.FEED_URI, projection, selection, null, null);
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
}
