package com.android.rss.parse;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Process;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.android.rss.common.FeedInfo;
import com.android.rss.common.ItemInfo;
import com.android.rss.common.PreferenceKeys;
import com.android.rss.common.Constant;
import com.android.rss.util.Log;

public class RSSWorkerThread extends Thread {

    private String TAG = "RSSWorker";
    private static final String KEY_FEED_ID = "feedId";
    private Context mContext = null;
    
    private int mAppWidgetId;
    
    public RSSWorkerThread(Context context){
        mContext = context;
    }
    private boolean updateRss(FeedInfo info){
        if(info == null){
            Log.d(TAG, "arguement is null in insertRSS");
            return false;
        }
        String url = info.feedUrl;
        List<ItemInfo> infos = new ArrayList<ItemInfo>();
        RSSXmlParser parser = RSSXmlParser.getInstance(mContext);
        int result = parser.parse(url, info, infos, true); //Modify by wdmk68: corrected date
        Log.d(TAG, "updated = " + infos.size());
        if(result == Constant.State.STATE_SUCCESS){
            updateRssNews(info, infos);
            infos.clear();
            infos = null;
            String where = Constant.Content._ID + "=" + info.feedId;
            ContentValues values = new ContentValues();
            values.put(Constant.Content.FEED_STATE, Constant.State.STATE_SUBSCRIBED);
            values.put(Constant.Content.FEED_PUBDATE, info.feedPubdate);
            mContext.getContentResolver().update(Constant.Content.FEED_URI, values, where, null);
            Log.d(TAG ,"Success : " + info.toString());
            return true;
        } else {
            String where = Constant.Content._ID + "=" + info.feedId;
            ContentValues values = new ContentValues();
            values.put(Constant.Content.FEED_STATE, Constant.State.STATE_FAILURE);
            mContext.getContentResolver().update(Constant.Content.FEED_URI, values, where, null);
        }
        infos.clear();
        infos = null;
        return false;
    }
    public void run(){
        synchronized (RSSWorkerThread.class) {
            setPriority(Thread.MAX_PRIORITY);
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(Constant.Key.KEY_THREAD_RUNNING, true).commit();
            String selection = null;
            String sortBy = Constant.Content._ID + " asc";
            FeedInfo info = new FeedInfo();
            Cursor c = null;
            refreshStart();
            while(true){
                selection = Constant.Content.FEED_STATE + "=" + Constant.State.STATE_WAITING
                        + " or " + Constant.Content.FEED_STATE + "=" + Constant.State.STATE_SUBSCRIBING;
                try{
                    c = mContext.getContentResolver().query(Constant.Content.FEED_URI, null, selection, null, sortBy);
                    if(c != null){
                        if(c.moveToFirst()){
                            info.reset();
                            info.feedId = c.getInt(c.getColumnIndex(Constant.Content._ID));
                            info.feedTitle = c.getString(c.getColumnIndex(Constant.Content.FEED_TITLE));
                            info.feedUrl = c.getString(c.getColumnIndex(Constant.Content.FEED_URL));
                            info.feedIconState = c.getString(c.getColumnIndex(Constant.Content.FEED_ICON_STATE));
                            info.feedIsBundle = c.getInt(c.getColumnIndex(Constant.Content.FEED_IS_BUNDLE));
                            info.feedClass = c.getString(c.getColumnIndex(Constant.Content.FEED_CLASS));
                            info.feedClassGuid = c.getString(c.getColumnIndex(Constant.Content.FEED_CLASS_GUID));
                        }else{
                            break;
                        }
                    }else{
                        break;
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    if(c != null){
                        c.close();
                    }
                }
                ContentValues values = new ContentValues();
                values.put(Constant.Content.FEED_STATE, Constant.State.STATE_SUBSCRIBING);
                String where = Constant.Content._ID + "=" + info.feedId;
                mContext.getContentResolver().update(Constant.Content.FEED_URI, values, where, null);
                Log.d(TAG, "Start subscribe " + info.feedTitle);
                if(updateRss(info)){
                    Log.d(TAG, "feedIsBundle = " + info.feedIsBundle);
                    if(info.feedIsBundle == 0){
                        addToHistory(info);
                    }
                    if("unknown".equals(info.feedIconState) && !TextUtils.isEmpty(info.feedIcon)){
                        PicLoader loader = new PicLoader(mContext, info);
                        loader.start();
                        synchronized (info.feedIcon) {
                            try {
                                Log.d(TAG, "Waiting for icon loaded completely");
                                info.feedIcon.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.d(TAG, "Waiting complete icon = " + info.feedIconState);
                    }
                }
                updateRssComplete(info);
            }
            allFeedsUpdated();
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(Constant.Key.KEY_THREAD_RUNNING, false).commit();
        }
    }
    private void refreshStart(){
        SharedPreferences shared = mContext.getSharedPreferences(Constant.Content.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        shared.edit().putInt(Constant.Key.KEY_REFRESH_STATE, Constant.State.STATE_REFRESH_ONGOING).commit();
    }
    private void allFeedsUpdated(){
        SharedPreferences shared = mContext.getSharedPreferences(Constant.Content.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        shared.edit().putInt(Constant.Key.KEY_REFRESH_STATE, Constant.State.STATE_REFRESH_NORMAL).commit();
        Intent intent = new Intent(Constant.Intent.INTENT_RSSAPP_UPDATE_FINISHED);
        mContext.sendBroadcast(intent);
    }
    private void updateRssComplete(FeedInfo info){
        Log.d(TAG, "mAppWidgetId is " + mAppWidgetId);
        Intent updateIntent = new Intent();
        updateIntent.setAction(Constant.Intent.INTENT_RSSAPP_SUBSCRIBE_FINISHED);
        updateIntent.putExtra(KEY_FEED_ID, info.feedId);
        mContext.sendBroadcast(updateIntent);
        Intent updateList = new Intent();
        updateList.setAction(Constant.Intent.INTENT_RSSAPP_UPDATE_NEWS_LIST);
        updateList.putExtra(KEY_FEED_ID, info.feedId);
        mContext.sendBroadcast(updateList);
        Intent intent = new Intent(Constant.Intent.INTENT_RSSAPP_WIDGET_UPDATE);
        mContext.sendBroadcast(intent);
    }
    private void updateRssNews(FeedInfo info, List<ItemInfo> infos){
        info.feedGuid = String.valueOf(info.feedUrl.hashCode());
        int size = infos.size();
        int i = 0;
        String where = Constant.Content._ID + "=" + info.feedId;
        ContentValues values = new ContentValues();
        values.put(Constant.Content.FEED_ORI_TITLE, info.feedOriTitle);
//        values.put(RssConstant.Content.ITEM_COUNT, infos.size());
        mContext.getContentResolver().update(Constant.Content.FEED_URI, values, where, null);
        boolean deleteOldNews = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(PreferenceKeys.KEY_DELETE_ON_REFRESH, true);
        if(deleteOldNews){
            where = Constant.Content.FEED_ID + "=" + info.feedId;
            mContext.getContentResolver().delete(Constant.Content.ITEM_URI, where, null);
        }
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        Builder builder = null;
        ContentProviderOperation cpo = null;
        while(i<size){
            ItemInfo item = infos.get(i);
            String itemDesBrief = null;
            if(item.itemDescription != null) {
                itemDesBrief = removeTags(item.itemDescription);
                if(itemDesBrief.length() > MAX_DESCRIPTION_LENGTH){
                    itemDesBrief = itemDesBrief.substring(0, MAX_DESCRIPTION_LENGTH);
                    if(!itemDesBrief.endsWith("...")){
                        itemDesBrief += "...";
                    }
                }
            }
            values = new ContentValues();
            values.put(Constant.Content.FEED_ID, item.feedId);
            values.put(Constant.Content.ITEM_TITLE, removeTags(item.itemTitle));
            values.put(Constant.Content.ITEM_URL, item.itemUrl);
            values.put(Constant.Content.ITEM_DESCRIPTION, item.itemDescription);
            values.put(Constant.Content.ITEM_DES_BRIEF, removeTags(itemDesBrief));
            values.put(Constant.Content.ITEM_GUID, item.itemGuid); 
            values.put(Constant.Content.ITEM_AUTHOR, item.itemAuthor);
            values.put(Constant.Content.ITEM_PUBDATE, item.itemPubdate);
            i++;
            builder = ContentProviderOperation.newInsert(Constant.Content.ITEM_URI);
            builder = builder.withValues(values);
            cpo = builder.build();
            ops.add(cpo);
        }
        try {
            mContext.getContentResolver().applyBatch(Constant.Content.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
    private void addToHistory(FeedInfo info){
        Cursor c = null;
        String selection = Constant.Content.FEED_GUID + "=" + info.feedGuid;
        boolean existed = false;
        try{
            c = mContext.getContentResolver().query(Constant.Content.HISTORY_URI, null, selection, null, null);
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
            return ;
        }
        Log.d(TAG, "addToHistory ---> feedUrl : " + info.feedUrl);
        String title = null;
        ContentValues values = new ContentValues();
        if(TextUtils.isEmpty(info.feedTitle)){
            title = info.feedOriTitle;
        }else{
            title = info.feedTitle;
        }
        values.put(Constant.Content.HISTORY_TITLE, title);
        values.put(Constant.Content.HISTORY_URL, info.feedUrl);
        values.put(Constant.Content.FEED_GUID, info.feedGuid);
        values.put(Constant.Content.FEED_CLASS, info.feedClass);
        values.put(Constant.Content.FEED_CLASS_GUID, info.feedClassGuid);
        values.put(Constant.Content.HISTORY_DATE, System.currentTimeMillis());
        mContext.getContentResolver().insert(Constant.Content.HISTORY_URI, values);
    }
    private String removeTags(String str) {
        if(str != null){
            str = str.replaceAll("&gt;", ">");
            str = str.replaceAll("&lt;", "<");
            str = str.replaceAll("&amp;", "&");
            str = str.replaceAll("&quot;", "\"");
            str = str.replaceAll("&nbsp;", " ");
            str = str.replaceAll("<.*?>", " ");
            str = str.replaceAll("\\s+", " ");
            str = str.replaceAll("&ldquo;", "\"");
            str = str.replaceAll("&rdquo;", "\"");
        }
        return str;
    }
    private static final int MAX_DESCRIPTION_LENGTH = 50;
}
