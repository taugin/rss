package com.android.rss.widget;

import java.util.ArrayList;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.rss.R;
import com.android.rss.common.ItemInfo;
import com.android.rss.common.PreferenceKeys;
import com.android.rss.common.Constant;

public class RSSWidgetService extends RemoteViewsService {

    public static final String TOAST_ACTION ="com.android.rss.widget.TOAST_ACTION"; 
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RSSRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    class RSSRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private Context mContext;
        private Intent mIntent;
        private ArrayList<ItemInfo> mItemList = null;
        private SharedPreferences SharedPreferences;
        private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        public RSSRemoteViewsFactory(Context context, Intent intent){
            mContext = context;
            mIntent = intent;
            SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public RemoteViews getLoadingView() {
            RemoteViews remoteViews = null;
            int widgetItemLayout = 0;
            String value = PreferenceManager.getDefaultSharedPreferences(mContext).getString(PreferenceKeys.KEY_WIDGET_LAYOUT, "grid");
            if("list".equals(value)){
                remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);
                widgetItemLayout = R.id.list_item_layout;
            }else{
                remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_grid_item);
                widgetItemLayout = R.id.grid_item_layout;
            }
            remoteViews.setViewVisibility(widgetItemLayout, View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.loading_tips, View.VISIBLE);
            return remoteViews;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if(getCount() <= 0){
                return null;
            }
            ItemInfo item = mItemList.get(position);
            RemoteViews remoteViews = null;
            int widgetItemLayout = 0;
            int itemLayout = 0;
            int itemTitle = 0;
            int itemDes = 0;
            String value = PreferenceManager.getDefaultSharedPreferences(mContext).getString(PreferenceKeys.KEY_WIDGET_LAYOUT, "grid");
            if("list".equals(value)){
                remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);
                widgetItemLayout = R.id.widget_list_item_layout;
                itemLayout = R.id.list_item_layout;
                itemTitle = R.id.list_item_title;
                itemDes = R.id.list_item_des;
            }else if("grid".equals(value)){
                remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_grid_item);
                widgetItemLayout = R.id.widget_grid_item_layout;
                itemLayout = R.id.grid_item_layout;
                itemTitle = R.id.grid_item_title;
                itemDes = R.id.grid_item_des;
            }else {
                remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_grid_item);
                widgetItemLayout = R.id.grid_item_layout;
                itemLayout = R.id.grid_item_layout;
                itemTitle = R.id.grid_item_title;
                itemDes = R.id.grid_item_des;
            }
            remoteViews.setViewVisibility(itemLayout, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.loading_tips, View.INVISIBLE);
            remoteViews.setTextViewText(itemTitle, item.itemTitle);
            remoteViews.setTextViewText(itemDes, item.itemDescription);
            remoteViews.setViewVisibility(widgetItemLayout, View.VISIBLE);
            
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(Constant.Content.EXTRA_POSITION, position);
            fillInIntent.putExtra(Constant.Content.EXTRA_NEWS_ID, item.itemId);
            fillInIntent.putExtra(Constant.Content.EXTRA_FEED_ID, item.feedId);
            remoteViews.setOnClickFillInIntent(widgetItemLayout, fillInIntent);
            return remoteViews;
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public void onCreate() {
            mItemList = new ArrayList<ItemInfo>();
        }

        @Override
        public void onDataSetChanged() {
            mItemList.clear();
            queryNews();
        }

        @Override
        public void onDestroy() {
            mItemList.clear();
            mItemList = null;
        }
        
        private void queryNews(){
            String selection = Constant.Content.WIDGET_ID + "=" + mAppWidgetId;
            Cursor c = null;
            String feeds = null;
            try{
                c = mContext.getContentResolver().query(Constant.Content.WIDGET_URI, null, selection, null, null);
                if(c != null){
                    if(c.moveToFirst()){
                        feeds = c.getString(c.getColumnIndex(Constant.Content.WIDGET_FEEDS));
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if(c != null){
                    c.close();
                }
            }
            selection = Constant.Content.FEED_ID + " in (" + feeds + ")";
            String orderBy = Constant.Content.ITEM_PUBDATE + " desc";
            String []projection = new String[]{Constant.Content._ID, Constant.Content.FEED_ID, 
                    Constant.Content.ITEM_TITLE,Constant.Content.ITEM_URL, Constant.Content.ITEM_DES_BRIEF,
                    Constant.Content.ITEM_PUBDATE, Constant.Content.ITEM_STATE};
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
                            mItemList.add(item);
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
    }
}
