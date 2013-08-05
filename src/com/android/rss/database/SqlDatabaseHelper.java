package com.android.rss.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.rss.R;
import com.android.rss.common.FeedInfo;
import com.android.rss.common.Constant;
import com.android.rss.util.Log;

public class SqlDatabaseHelper extends SQLiteOpenHelper {

    public String TAG = "SQLDatabaseHelper";
    private Context mContext;

    private final String WIDGETS_TABLE_CREATE = 
            "create table if not exists " + Constant.Content.TABLE_WIDGETS
            + "("
            + Constant.Content._ID + " integer primary key autoincrement,"
            + Constant.Content.WIDGET_ID + " integer,"
            + Constant.Content.WIDGET_TITLE + " text," 
            + Constant.Content.WIDGET_LAYOUT + " text default list," 
            + Constant.Content.WIDGET_FEEDS + " integer default 0,"
            + Constant.Content.FEEDS_ON_WIDGET + " text default null,"
            + Constant.Content.DELETED + " integer default 0,"
            + Constant.Content.FOO + " text" 
            + ")";

    private final String FEED_TABLE_CREATE = 
        "create table if not exists " + Constant.Content.TABLE_FEEDS 
        + "(" 
        + Constant.Content._ID + " integer primary key autoincrement,"
        + Constant.Content.FEED_TITLE + " text," 
        + Constant.Content.FEED_URL + " text," 
        + Constant.Content.FEED_IS_BUNDLE + " integer,"
        + Constant.Content.FEED_ICON + " blob,"
        + Constant.Content.FEED_PUBDATE + " long default 0,"
        + Constant.Content.FEED_GUID + " text unique,"
        + Constant.Content.FEED_STATE + " integer default 0,"
        + Constant.Content.FEED_ICON_STATE + " text,"
        + Constant.Content.FEED_ORI_TITLE + " text,"
        + Constant.Content.FEED_CLASS + " text,"
        + Constant.Content.FEED_CLASS_GUID + " text,"
        + Constant.Content.ITEM_COUNT + " integer default 0,"
        + Constant.Content.ITEM_READ_COUNT + " integer default 0,"
        + Constant.Content.ITEM_UNREAD_COUNT + " integer default 0,"
        + Constant.Content.FOO + " text" 
        + ")";


    private final String ITEM_TABLE_CREATE = 
        "create table if not exists " + Constant.Content.TABLE_ITEMS 
        + "("
        + Constant.Content._ID + " integer primary key autoincrement," 
        + Constant.Content.FEED_ID + " integer references " + Constant.Content.TABLE_FEEDS + "(" + Constant.Content._ID + "),"
        + Constant.Content.ITEM_TITLE + " text," 
        + Constant.Content.ITEM_URL + " text,"
        + Constant.Content.ITEM_DESCRIPTION + " text,"
        + Constant.Content.ITEM_DES_BRIEF + " text," 
        + Constant.Content.ITEM_GUID + " text unique,"
        + Constant.Content.ITEM_AUTHOR + " text,"
        + Constant.Content.ITEM_PUBDATE + " long not null default 0,"
        + Constant.Content.ITEM_STATE + " integer default 0,"
        + Constant.Content.FOO + " text" 
        + ")";

    private final String FEED_INDEX_TABLE_CREATE = 
            "create table if not exists " + Constant.Content.TABLE_FEED_INDEXS
            +"("
            + Constant.Content._ID + " integer primary key autoincrement,"
            + Constant.Content.WIDGET_ID + " integer references " + Constant.Content.TABLE_WIDGETS + "(" + Constant.Content.WIDGET_ID + "),"
            + Constant.Content.FEED_ID + " integer references " + Constant.Content.TABLE_FEEDS + "(" + Constant.Content._ID + "),"
            + Constant.Content.DELETED + " integer default 0,"
            + Constant.Content.FOO + " text" 
            + ")";
        
        private final String ITEM_INDEX_TABLE_CREATE = 
            "create table if not exists " + Constant.Content.TABLE_ITEM_INDEXS
            +"("
            + Constant.Content._ID + " integer primary key autoincrement,"
            + Constant.Content.WIDGET_ID + " integer references " + Constant.Content.TABLE_WIDGETS + "(" + Constant.Content.WIDGET_ID + "),"
            + Constant.Content.FEED_ID + " integer references " + Constant.Content.TABLE_FEEDS + "(" + Constant.Content._ID + "),"
            + Constant.Content.ITEM_ID + " long references " + Constant.Content.TABLE_ITEMS + "(" + Constant.Content._ID +"),"
            + Constant.Content.ITEM_STATE + " integer default 0,"
            + Constant.Content.DELETED + " integer default 0,"
            + Constant.Content.FOO + " text" 
            + ")";


    private final String HISTORYS_TABLE_CREATE = 
        "create table if not exists " + Constant.Content.TABLE_HISTORYS
        + "("
        + Constant.Content._ID + " integer primary key autoincrement," 
        + Constant.Content.HISTORY_TITLE + " text," 
        + Constant.Content.HISTORY_URL + " text," 
        + Constant.Content.FEED_GUID + " text,"
        + Constant.Content.FEED_CLASS + " text,"
        + Constant.Content.FEED_CLASS_GUID + " text,"
        + Constant.Content.HISTORY_TIMES + " integer default 0,"
        + Constant.Content.HISTORY_DATE + " long,"
        + Constant.Content.FOO + " text" 
        + ")";
    
    private final String BUNDLE_TABLE_CREATE = 
            "create table if not exists " + Constant.Content.TABLE_BUNDLES
            + "("
            + Constant.Content._ID + " integer primary key autoincrement," 
            + Constant.Content.BUNDLE_TITLE + " text," 
            + Constant.Content.BUNDLE_URL + " text," 
            + Constant.Content.BUNDLE_GUID + " text unique,"
            + Constant.Content.BUNDLE_TIMES + " integer default 0,"
            + Constant.Content.BUNDLE_DATE + " long,"
            + Constant.Content.FOO + " text" 
            + ")";
    
    private final String ACTIVITYCOUNT_TABLE_CREATE = "create table if not exists " +  Constant.Content.TABLE_ACTIVITYCOUNT
            + "("
            + Constant.Content.ACTIVITY_COUNT + " integer integer default 0,"
            + Constant.Content.FOO + " text" 
            + ")";
    /**
    private final String QUERY_ITEMS_VIEW = 
        "create view " + RssConstant.Content.VIEW_QUERY_ITEM + " as select "
                + RssConstant.Content.TABLE_INDEXS + "." + RssConstant.Content.WIDGET_ID + " as " + RssConstant.Content.VIEW_WIDGET_ID
                + " , " + RssConstant.Content.TABLE_INDEXS + "." + RssConstant.Content.ITEM_ID + " as " + RssConstant.Content.VIEW_ITEM_ID
                + " , " + RssConstant.Content.TABLE_INDEXS + "." + RssConstant.Content.ITEM_STATE + " as " + RssConstant.Content.VIEW_ITEM_STATE
                + " , " + RssConstant.Content.TABLE_ITEMS + "." + RssConstant.Content.ITEM_TITLE + " as " + RssConstant.Content.VIEW_ITEM_TITLE
                + " , " + RssConstant.Content.TABLE_ITEMS + "." + RssConstant.Content.ITEM_URL + " as " + RssConstant.Content.VIEW_ITEM_URL
                + " , " + RssConstant.Content.TABLE_ITEMS + "." + RssConstant.Content.ITEM_DESCRIPTION + " as " + RssConstant.Content.VIEW_ITEM_DESCRIPTION
                + " , " + RssConstant.Content.TABLE_ITEMS + "." + RssConstant.Content.ITEM_AUTHOR + " as " + RssConstant.Content.VIEW_ITEM_AUTHOR
                + " , " + RssConstant.Content.TABLE_ITEMS + "." + RssConstant.Content.ITEM_PUBDATE + " as " + RssConstant.Content.VIEW_ITEM_PUBDATE
                + " from (select * from " + RssConstant.Content.TABLE_INDEXS + ") as indexs " + "left join" 
                + " (select * from " + RssConstant.Content.TABLE_ITEMS + ") as items "
                + " on indexs." + RssConstant.Content.ITEM_ID + "" + "=" + "items." + RssConstant.Content._ID + "";
    */
    private final String VIEW_FEEDS_CREATE = 
             "create view " + Constant.Content.VIEW_QUERY_FEED + " as select "
                    +         Constant.Content._ID
                    + " , " + Constant.Content.WIDGET_ID
                    + " , " + Constant.Content.FEED_ID
                    + " , " + Constant.Content.DELETED
                    + " , " + Constant.Content.FEED_TITLE
                    + " , " + Constant.Content.FEED_URL
                    + " , " + Constant.Content.FEED_IS_BUNDLE
                    + " , " + Constant.Content.FEED_ICON
                    + " , " + Constant.Content.FEED_PUBDATE
                    + " , " + Constant.Content.FEED_GUID
                    + " from (select * from " + Constant.Content.TABLE_FEED_INDEXS 
                    + " where " + Constant.Content.DELETED + "=" + Constant.State.STATE_UNDELETED
                    + ") as indexfeeds " + "left join"
                    + " (select "
                    + Constant.Content._ID + " as " + "feed_id_view"
                    + " , " + Constant.Content.FEED_TITLE
                    + " , " + Constant.Content.FEED_URL
                    + " , " + Constant.Content.FEED_IS_BUNDLE
                    + " , " + Constant.Content.FEED_ICON
                    + " , " + Constant.Content.FEED_PUBDATE
                    + " , " + Constant.Content.FEED_GUID
                    + " from " + Constant.Content.TABLE_FEEDS + ") as feeds "
                    + " on indexfeeds." + Constant.Content.FEED_ID + "=" + "feeds." + "feed_id_view";
     private final String VIEW_ITEMS_CREATE = 
             "create view " + Constant.Content.VIEW_QUERY_ITEM + " as select "
                     +         Constant.Content._ID
                    + " , " + Constant.Content.WIDGET_ID
                    + " , " + Constant.Content.FEED_ID
                    + " , " + Constant.Content.DELETED
                    + " , " + Constant.Content.FEED_TITLE
                    + " , " + Constant.Content.FEED_URL
                    + " , " + Constant.Content.FEED_IS_BUNDLE
                    + " , " + Constant.Content.FEED_ICON
                    + " , " + Constant.Content.FEED_PUBDATE
                    + " , " + Constant.Content.FEED_GUID
                    + " , " + Constant.Content.ITEM_ID 
                    + " , " + Constant.Content.ITEM_STATE
                    + " , " + Constant.Content.ITEM_TITLE
                    + " , " + Constant.Content.ITEM_URL
                    + " , " + Constant.Content.ITEM_DESCRIPTION
                    + " , " + Constant.Content.ITEM_DES_BRIEF
                    + " , " + Constant.Content.ITEM_AUTHOR
                    + " , " + Constant.Content.ITEM_PUBDATE
                    + " from (select * "            
                    + " from " + Constant.Content.TABLE_ITEM_INDEXS 
                    + " where " + Constant.Content.DELETED + "=" + Constant.State.STATE_UNDELETED
                    + ") as indexitems" 
                    + "    left join" 
                    + " (select "
                    + Constant.Content._ID + " as " + "feed_id_view"
                    + " , " + Constant.Content.FEED_TITLE
                    + " , " + Constant.Content.FEED_URL
                    + " , " + Constant.Content.FEED_IS_BUNDLE
                    + " , " + Constant.Content.FEED_ICON
                    + " , " + Constant.Content.FEED_PUBDATE
                    + " , " + Constant.Content.FEED_GUID
                    + " from " + Constant.Content.TABLE_FEEDS + ") as feeds "
                    + " on indexitems." + Constant.Content.FEED_ID + "=" + "feeds." + "feed_id_view"
                    + "    left join"
                    + " (select "
                    + Constant.Content._ID + " as " + "item_id_view"
                    + " , " + Constant.Content.ITEM_TITLE
                    + " , " + Constant.Content.ITEM_URL
                    + " , " + Constant.Content.ITEM_DESCRIPTION
                    + " , " + Constant.Content.ITEM_DES_BRIEF
                    + " , " + Constant.Content.ITEM_GUID
                    + " , " + Constant.Content.ITEM_AUTHOR
                    + " , " + Constant.Content.ITEM_PUBDATE
                    + " from " + Constant.Content.TABLE_ITEMS + ") as items "
                    + " on indexitems." + Constant.Content.ITEM_ID + "=" + "items." + "item_id_view";

    private final String WIDGET_TABLE_DROP = "drop table " + Constant.Content.TABLE_WIDGETS + " if exists";
    private final String FEED_TABLE_DROP = "drop table " + Constant.Content.TABLE_FEEDS + " if exists";
    private final String ITEM_TABLE_DROP = "drop table " + Constant.Content.TABLE_ITEMS + " if exists";
    private final String FEED_INDEX_TABLE_DROP = "drop table " + Constant.Content.TABLE_FEED_INDEXS + " if exists";
    private final String ITEM_INDEX_TABLE_DROP = "drop table " + Constant.Content.TABLE_ITEM_INDEXS + " if exists";
    private final String ACTIVITYCOUNT_TABLE_DROP = "drop table " + Constant.Content.TABLE_ACTIVITYCOUNT + " if exists";
    private final String HISTORY_TABLE_DROP = "drop table " + Constant.Content.TABLE_HISTORYS + " if exists";
    private final String VIEW_FEED_DROP = "drop view " + Constant.Content.VIEW_QUERY_FEED + " if exists";
    private final String VIEW_ITEM_DROP = "drop view " + Constant.Content.VIEW_QUERY_ITEM + " if exists";

    public SqlDatabaseHelper(Context context) {
        super(context, Constant.Content.DB_NAME, null, Constant.Content.DB_VERSION);
        mContext = context;
        Log.d(TAG, "SQLDatabaseHelper --- DB_NAME = " + Constant.Content.DB_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "SQLDatabaseHelper --- onCreate");
        try{
            db.execSQL(WIDGETS_TABLE_CREATE);
            db.execSQL(FEED_INDEX_TABLE_CREATE);
            db.execSQL(ITEM_INDEX_TABLE_CREATE);
            db.execSQL(FEED_TABLE_CREATE);
            db.execSQL(ITEM_TABLE_CREATE);
            db.execSQL(HISTORYS_TABLE_CREATE);
            db.execSQL(ACTIVITYCOUNT_TABLE_CREATE);
            db.execSQL(BUNDLE_TABLE_CREATE);
            Log.d(TAG, VIEW_ITEMS_CREATE);

            
            db.execSQL(TRIGGER_ADD_ITEM_CREATE);
            db.execSQL(TRIGGER_DELETE_ITEM_CREATE);
            db.execSQL(TRIGGER_UPDATE_ITEM_CREATE);
            db.execSQL(TRIGGER_DELETE_FEED_CREATE);

            loadBundles(db);
//            db.execSQL(VIEW_FEEDS_CREATE);
//            db.execSQL(VIEW_ITEMS_CREATE);
            
        } catch(SQLException e){
            Log.d(TAG, "create table failed");
            e.printStackTrace();
        } finally{
            
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "SQLDatabaseHelper --- onUpgrade oldVersion = " + oldVersion + " , newVersion = " + newVersion);
        if(newVersion > oldVersion){
            try{
                db.execSQL(WIDGET_TABLE_DROP);
                db.execSQL(FEED_TABLE_DROP);
                db.execSQL(ITEM_TABLE_DROP);
                db.execSQL(FEED_INDEX_TABLE_DROP);
                db.execSQL(ITEM_INDEX_TABLE_DROP);
                db.execSQL(HISTORY_TABLE_DROP);
                db.execSQL(ACTIVITYCOUNT_TABLE_DROP);
                db.execSQL(VIEW_FEED_DROP);
                db.execSQL(VIEW_ITEM_DROP);
            } catch(SQLException e){
                e.printStackTrace();
            } finally{
                onCreate(db);
            }
        }
    }
    
    private String TRIGGER_DELETE_ITEM = "trigger_delete_item";
    private String TRIGGER_ADD_ITEM = "trigger_add_item";
    private String TRIGGER_UPDATE_ITEM = "trigger_update_item";
    private String TRIGGER_DELETE_FEED = "trigger_delete_feed";
    
    private String TRIGGER_WIDGET = "trigger_widget";
    private String TRIGGER_ITEM = "trigger_items";
    private String VIEW_QUERY_FEED = "view_query_feed";
    private String VIEW_QUERY_ITEM = "view_query_item";
    
    private String TRIGGER_DELETE_ITEM_CREATE = "create trigger " + TRIGGER_DELETE_ITEM
                + " after delete "
                + " on " + Constant.Content.TABLE_ITEMS
                + " for each row "
                + " begin "
                + " update " + Constant.Content.TABLE_FEEDS
                + " set " + Constant.Content.ITEM_COUNT + "=" + Constant.Content.ITEM_COUNT + "-1"
                + " where old." + Constant.Content.FEED_ID + "=" + Constant.Content.TABLE_FEEDS + "." + Constant.Content._ID + ";"
                
                + " update " + Constant.Content.TABLE_FEEDS
                + " set " + Constant.Content.ITEM_UNREAD_COUNT + "=" + Constant.Content.ITEM_UNREAD_COUNT + "-1"
                + " where old." + Constant.Content.FEED_ID + "=" + Constant.Content.TABLE_FEEDS + "." + Constant.Content._ID 
                + " and old." + Constant.Content.ITEM_STATE + "=" + Constant.State.STATE_ITEM_UNREAD + ";"
                
                + " update " + Constant.Content.TABLE_FEEDS
                + " set " + Constant.Content.ITEM_READ_COUNT + "=" + Constant.Content.ITEM_READ_COUNT + "-1"
                + " where old." + Constant.Content.FEED_ID + "=" + Constant.Content.TABLE_FEEDS + "." + Constant.Content._ID 
                + " and old." + Constant.Content.ITEM_STATE + "=" + Constant.State.STATE_ITEM_READ + ";"
                + " end ";
    
    private String TRIGGER_ADD_ITEM_CREATE = "create trigger " + TRIGGER_ADD_ITEM
                + " after insert "
                + " on " + Constant.Content.TABLE_ITEMS
                + " for each row "
                + " begin "
                + " update " + Constant.Content.TABLE_FEEDS
                + " set " + Constant.Content.ITEM_COUNT + "=" + Constant.Content.ITEM_COUNT + "+1"
                + " where new." + Constant.Content.FEED_ID + "=" + Constant.Content.TABLE_FEEDS + "." + Constant.Content._ID + ";"
                
                + " update " + Constant.Content.TABLE_FEEDS
                + " set " + Constant.Content.ITEM_UNREAD_COUNT + "=" + Constant.Content.ITEM_UNREAD_COUNT + "+1"
                + " where new." + Constant.Content.FEED_ID + "=" + Constant.Content.TABLE_FEEDS + "." + Constant.Content._ID + ";"
                + " end ";
    
    private String TRIGGER_DELETE_FEED_CREATE = "create trigger " + TRIGGER_DELETE_FEED
                + " after delete "
                + " on " + Constant.Content.TABLE_FEEDS
                + " for each row "
                + " begin "
                + " delete from " + Constant.Content.TABLE_FEED_INDEXS 
                + " where old." + Constant.Content._ID + "=" + Constant.Content.TABLE_FEED_INDEXS + "." + Constant.Content.FEED_ID + ";"
                + " delete from " + Constant.Content.TABLE_ITEM_INDEXS 
                + " where old." + Constant.Content._ID + "=" + Constant.Content.TABLE_ITEM_INDEXS + "." + Constant.Content.FEED_ID + ";"
                + " delete from " + Constant.Content.TABLE_ITEMS 
                + " where old." + Constant.Content._ID + "=" + Constant.Content.TABLE_ITEMS + "." + Constant.Content.FEED_ID + ";"
                + " end ";
    private String TRIGGER_UPDATE_ITEM_CREATE = "create trigger " + TRIGGER_UPDATE_ITEM
                + " after update "
                + " on " + Constant.Content.TABLE_ITEMS
                + " for each row " 
                + " begin "
                + " update " + Constant.Content.TABLE_FEEDS
                + " set " + Constant.Content.ITEM_UNREAD_COUNT + "=" + Constant.Content.ITEM_UNREAD_COUNT + "-1"
                + " , " + Constant.Content.ITEM_READ_COUNT + "=" + Constant.Content.ITEM_READ_COUNT + "+1"
                + " where new." + Constant.Content.FEED_ID + "=" + Constant.Content.TABLE_FEEDS + "." + Constant.Content._ID
                + " and " + "new." + Constant.Content.ITEM_STATE + "=" + Constant.State.STATE_ITEM_READ
                + " and " + "old." + Constant.Content.ITEM_STATE + "=" + Constant.State.STATE_ITEM_UNREAD + ";"
                
                + " update " + Constant.Content.TABLE_FEEDS
                + " set " + Constant.Content.ITEM_UNREAD_COUNT + "=" + Constant.Content.ITEM_UNREAD_COUNT + "+1"
                + " , " + Constant.Content.ITEM_READ_COUNT + "=" + Constant.Content.ITEM_READ_COUNT + "-1"
                + " where new." + Constant.Content.FEED_ID + "=" + Constant.Content.TABLE_FEEDS + "." + Constant.Content._ID
                + " and " + "new." + Constant.Content.ITEM_STATE + "=" + Constant.State.STATE_ITEM_UNREAD 
                + " and " + "old." + Constant.Content.ITEM_STATE + "=" + Constant.State.STATE_ITEM_READ + ";"
                + " end ";
    
    
    private void loadBundles(SQLiteDatabase db){
    	String []rss = mContext.getResources().getStringArray(R.array.bundle_rss);
        String bundleTitle = null;
        String bundleUrl = null;
        String bundleGuid = null;
        int bundleTiems = 0;
        long bundleDate = 0;
        ContentValues values = new ContentValues();
        for(int i=0;i<rss.length;i+=2){
            bundleTitle = rss[i];
            bundleUrl = rss[i+1];
            bundleGuid = String.valueOf(bundleUrl.hashCode());
            
            values.put(Constant.Content.BUNDLE_TITLE, bundleTitle);
            values.put(Constant.Content.BUNDLE_URL, bundleUrl);
            values.put(Constant.Content.BUNDLE_GUID, bundleGuid);
            values.put(Constant.Content.BUNDLE_TIMES, bundleTiems);
            values.put(Constant.Content.BUNDLE_DATE, bundleDate);
            try{
                db.insertOrThrow(Constant.Content.TABLE_BUNDLES, null, values);
            }catch(SQLException e){

            }
        }
    }
}
