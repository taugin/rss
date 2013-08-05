package com.android.rss.database;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.android.rss.common.Constant;
import com.android.rss.util.Log;

public class RSSProvider extends ContentProvider {
    public final String TAG = "RssContentProvider";

    private SqlDatabaseHelper mSQLDatabaseHelper = null;
    
    
    private static final int WIDGET_TABLE = 1;
    private static final int WIDGET_TABLE_ID = 2;
    private static final int FEED_TABLE = 3;  
    private static final int FEED_TABLE_ID = 4; 
    private static final int ITEM_TABLE = 5;
    private static final int ITEM_TABLE_ID = 6;
    private static final int ITEM_INDEX_TABLE = 7;
    private static final int ITEM_INDEX_TABLE_ID = 8;
    private static final int HISTORY_TABLE = 9;
    private static final int HISTORY_TABLE_ID = 10;
    private static final int FEED_VIEW = 11;
    private static final int ITEM_VIEW = 12;
    private static final int FEED_INDEX_TABLE = 13;
    private static final int FEED_INDEX_TABLE_ID = 14;
    private static final int ACTIVITY_COUNT_TABLE = 15;
    private static final int ITEM_LIMITED_VIEW = 16;
    private static final int BUNDLE_TABLE = 17;
    private static final int BUNDLE_TABLE_ID = 18;
    private static final int BUNDLE_TABLE_TIMES = 19;
    
    private static final String QUERY_LIMIT = "40";
    private static final UriMatcher sUriMatcher;     
    static {  
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_WIDGETS, WIDGET_TABLE);  
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_WIDGETS + "/#", WIDGET_TABLE_ID);
        
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_FEEDS, FEED_TABLE);  
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_FEEDS + "/#", FEED_TABLE_ID);
        
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_ITEMS, ITEM_TABLE);  
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_ITEMS + "/#", ITEM_TABLE_ID);
        
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_ITEM_INDEXS, ITEM_INDEX_TABLE);  
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_ITEM_INDEXS + "/#", ITEM_INDEX_TABLE_ID);
        
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_HISTORYS, HISTORY_TABLE);  
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_HISTORYS + "/#", HISTORY_TABLE_ID);
        
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_ACTIVITYCOUNT, ACTIVITY_COUNT_TABLE);
        
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.VIEW_QUERY_FEED, FEED_VIEW); 
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.VIEW_QUERY_ITEM, ITEM_VIEW);  
        
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_FEED_INDEXS, FEED_INDEX_TABLE);  
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_FEED_INDEXS + "/#", FEED_INDEX_TABLE_ID);
        
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.VIEW_LIMITED_ITEM, ITEM_LIMITED_VIEW);
        
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_BUNDLES, BUNDLE_TABLE);
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_BUNDLES + "/#", BUNDLE_TABLE_ID);
        sUriMatcher.addURI(Constant.Content.AUTHORITY, Constant.Content.TABLE_BUNDLES + "/times/#", BUNDLE_TABLE_TIMES);
    }
    
    @Override
    public boolean onCreate() {
        Log.d(TAG, "RSSContentProvider onCreate");
        mSQLDatabaseHelper = new SqlDatabaseHelper(getContext());
        if(mSQLDatabaseHelper != null){
            return true;
        }
        return false;
    }
    
    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType");
        Log.d(TAG, "uri = " + uri.toString());
        switch(sUriMatcher.match(uri)){
        case WIDGET_TABLE:
            return Constant.Content.WIDGET_CONTENT_TYPE;
        case WIDGET_TABLE_ID:
            return Constant.Content.WIDGET_CONTENT_ITEM_TYPE;
        case FEED_TABLE:
            return Constant.Content.FEED_CONTENT_TYPE;
        case FEED_TABLE_ID:
            return Constant.Content.FEED_CONTENT_ITEM_TYPE;
        case ITEM_TABLE:
            return Constant.Content.ITEM_CONTENT_TYPE;
        case ITEM_TABLE_ID:
            return Constant.Content.ITEM_CONTENT_ITEM_TYPE;
        case ITEM_INDEX_TABLE:
            return Constant.Content.INDEX_CONTENT_TYPE;
        case ITEM_INDEX_TABLE_ID:
            return Constant.Content.INDEX_CONTENT_ITEM_TYPE;
        case HISTORY_TABLE:
            return Constant.Content.HISTORY_CONTENT_TYPE;
        case HISTORY_TABLE_ID:
            return Constant.Content.HISTORY_CONTENT_ITEM_TYPE;
        case ACTIVITY_COUNT_TABLE:
            return Constant.Content.ACTIVITY_COUNT_CONTENT_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);        
        }
    }
    
    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = mSQLDatabaseHelper.getReadableDatabase();
        int ret = -1;
        long id = -1;
        try{
            switch(sUriMatcher.match(uri)){
            case WIDGET_TABLE:
                ret = db.delete(Constant.Content.TABLE_WIDGETS, whereClause, whereArgs);
                notifyWidgetTableChanged();
                break;
            case WIDGET_TABLE_ID:
                id = ContentUris.parseId(uri);
                ret = db.delete(Constant.Content.TABLE_WIDGETS, Constant.Content._ID + "=" + id, whereArgs);
                notifyWidgetTableChanged();
                break;
            case FEED_TABLE:
                ret = db.delete(Constant.Content.TABLE_FEEDS, whereClause, whereArgs);
//                notifyFeedTableChanged();
                break;
            case FEED_TABLE_ID:
                id = ContentUris.parseId(uri);
                ret = db.delete(Constant.Content.TABLE_FEEDS, Constant.Content._ID + "=" + id, whereArgs);
                notifyFeedTableChanged();
                break;
            case ITEM_TABLE:
                ret = db.delete(Constant.Content.TABLE_ITEMS, whereClause, whereArgs);
                notifyItemTableChanged();
                break;
            case ITEM_TABLE_ID:
                id = ContentUris.parseId(uri);
                ret = db.delete(Constant.Content.TABLE_ITEMS, Constant.Content._ID + "=" + id, whereArgs);
                notifyItemTableChanged();
                break;
            case ITEM_INDEX_TABLE:
                ret = db.delete(Constant.Content.TABLE_ITEM_INDEXS, whereClause, whereArgs);
//                notifyWidgetTableChanged();
                break;
            case FEED_INDEX_TABLE:
                ret = db.delete(Constant.Content.TABLE_FEED_INDEXS, whereClause, whereArgs);
//                notifyWidgetTableChanged();
                break;
            case HISTORY_TABLE:
                ret = db.delete(Constant.Content.TABLE_HISTORYS, whereClause, whereArgs);
                notifyHistoryTableChanged();
                break;
            case HISTORY_TABLE_ID:
                id = ContentUris.parseId(uri);
                ret = db.delete(Constant.Content.TABLE_HISTORYS, Constant.Content._ID + "=" + id, whereArgs);
                notifyHistoryTableChanged();
                break;
            case ACTIVITY_COUNT_TABLE:
                ret = db.delete(Constant.Content.TABLE_ACTIVITYCOUNT, whereClause, whereArgs);
                notifyActivityCountTableChanged();
            case BUNDLE_TABLE:
                ret = db.delete(Constant.Content.TABLE_BUNDLES, whereClause, whereArgs);
                break;
            case BUNDLE_TABLE_ID:
                id = ContentUris.parseId(uri);
                ret = db.delete(Constant.Content.TABLE_BUNDLES, Constant.Content._ID + "=" + id, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);        
            }
        }catch(SQLException e){
            Log.d(TAG, e.getMessage());
            return 0;
        }
        return ret;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mSQLDatabaseHelper.getReadableDatabase();
        long id = -1;
        try{
            switch(sUriMatcher.match(uri)){
            case WIDGET_TABLE:
                id = db.insert(Constant.Content.TABLE_WIDGETS, Constant.Content.FOO, values);
                notifyWidgetTableChanged();
            break;
            case FEED_TABLE:
                id = db.insertOrThrow(Constant.Content.TABLE_FEEDS, Constant.Content.FOO, values);
//                LogUtils.debug(TAG, "Feed id = " + id);
                notifyFeedTableChanged();
                break;
            case ITEM_TABLE:
                id = db.insertOrThrow(Constant.Content.TABLE_ITEMS, Constant.Content.FOO, values);
//                id = db.insert(RssConstant.Content.TABLE_ITEMS, RssConstant.Content.FOO, values);
//                LogUtils.debug(TAG, "Item id = " + id);
                notifyItemTableChanged();
                break;
            case ITEM_INDEX_TABLE:
                id = db.insert(Constant.Content.TABLE_ITEM_INDEXS, Constant.Content.FOO, values);
//                notifyIndexTableChanged();
                break;
            case FEED_INDEX_TABLE:
                id = db.insert(Constant.Content.TABLE_FEED_INDEXS, Constant.Content.FOO, values);
//                notifyIndexTableChanged();
                break;
            case HISTORY_TABLE:
                id = db.insert(Constant.Content.TABLE_HISTORYS, Constant.Content.FOO, values);
                notifyHistoryTableChanged();
                break;
            case ACTIVITY_COUNT_TABLE:
                id = db.insert(Constant.Content.TABLE_ACTIVITYCOUNT, Constant.Content.FOO, values);
                notifyActivityCountTableChanged();
                break;
            case BUNDLE_TABLE:
                id = db.insert(Constant.Content.TABLE_BUNDLES, Constant.Content.FOO, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);        
            }
        }catch(SQLException e){
            Log.e(TAG, "The item has inserted into the database ! : " + e.getLocalizedMessage());
            Uri resultUri = ContentUris.withAppendedId(uri, 0);
            return resultUri;
        }
        Uri resultUri = ContentUris.withAppendedId(uri, id);
        return resultUri;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mSQLDatabaseHelper.getReadableDatabase();
        Cursor c = null;
        long id = -1;
        try{
            switch(sUriMatcher.match(uri)){
            case WIDGET_TABLE:
                c = db.query(Constant.Content.TABLE_WIDGETS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case WIDGET_TABLE_ID:
                id = ContentUris.parseId(uri);
                c = db.query(Constant.Content.TABLE_WIDGETS, projection, Constant.Content._ID + "=" + id, selectionArgs, null, null, sortOrder);
                break;
            case FEED_TABLE:
                c = db.query(true, Constant.Content.TABLE_FEEDS, projection, selection, selectionArgs, null, null, sortOrder, null);
                break;
            case FEED_TABLE_ID:
                id = ContentUris.parseId(uri);
                c = db.query(Constant.Content.TABLE_FEEDS, projection, Constant.Content._ID + "=" + id, selectionArgs, null, null, sortOrder);
                break;
            case ITEM_TABLE:
                c = db.query(Constant.Content.TABLE_ITEMS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ITEM_TABLE_ID:
                id = ContentUris.parseId(uri);
                c = db.query(Constant.Content.TABLE_ITEMS, projection, Constant.Content._ID + "=" + id, selectionArgs, null, null, sortOrder);
                break;
            case ITEM_INDEX_TABLE:
                c = db.query(true, Constant.Content.TABLE_ITEM_INDEXS, projection, selection, selectionArgs, null, null, sortOrder, null);
                break;
            case FEED_INDEX_TABLE:
                c = db.query(true, Constant.Content.TABLE_FEED_INDEXS, projection, selection, selectionArgs, null, null, sortOrder, null);
                break;
            case HISTORY_TABLE:
                c = db.query(Constant.Content.TABLE_HISTORYS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case HISTORY_TABLE_ID:
                id = ContentUris.parseId(uri);
                c = db.query(Constant.Content.TABLE_HISTORYS, projection, Constant.Content._ID + "=" + id, selectionArgs, null, null, sortOrder);
                break;
            case FEED_VIEW:
                c = db.query(true, Constant.Content.VIEW_QUERY_FEED, projection, selection, selectionArgs, null, null, sortOrder, null);
                break;
            case ITEM_VIEW:
                c = db.query(true, Constant.Content.VIEW_QUERY_ITEM, projection, selection, selectionArgs, null, null, sortOrder, null);
                break;
            case ITEM_LIMITED_VIEW:
                c = db.query(true, Constant.Content.VIEW_QUERY_ITEM, projection, selection, selectionArgs, null, null, sortOrder, QUERY_LIMIT);
                break;
            case ACTIVITY_COUNT_TABLE:
                c = db.query(Constant.Content.TABLE_ACTIVITYCOUNT, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BUNDLE_TABLE:
                c = db.query(Constant.Content.TABLE_BUNDLES, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BUNDLE_TABLE_ID:
                id = ContentUris.parseId(uri);
                c = db.query(Constant.Content.TABLE_BUNDLES, projection, Constant.Content._ID + "=" + id, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }catch(SQLException e){
            Log.d(TAG, e.getMessage());
            return null;
        }
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int ret = -1;
        long id = -1;
        SQLiteDatabase db = mSQLDatabaseHelper.getReadableDatabase();
        try{
            switch(sUriMatcher.match(uri)){
            case WIDGET_TABLE:
                ret = db.update(Constant.Content.TABLE_WIDGETS, values, selection, selectionArgs);
                notifyWidgetTableChanged();
                break;
            case WIDGET_TABLE_ID:
                id = ContentUris.parseId(uri);
                ret = db.update(Constant.Content.TABLE_WIDGETS, values, Constant.Content._ID + "=" + id, selectionArgs);
                notifyWidgetTableChanged();
                break;
            case FEED_TABLE:
                ret = db.update(Constant.Content.TABLE_FEEDS, values, selection, selectionArgs);
//                notifyFeedTableChanged();
                break;
            case FEED_TABLE_ID:
                id = ContentUris.parseId(uri);
                ret = db.update(Constant.Content.TABLE_FEEDS, values, Constant.Content._ID + "=" + id, selectionArgs);
                notifyFeedTableChanged();
                break;
            case ITEM_TABLE:
                ret = db.update(Constant.Content.TABLE_ITEMS, values, selection, selectionArgs);
                notifyItemTableChanged();
                break;
            case ITEM_TABLE_ID:
                id = ContentUris.parseId(uri);
                ret = db.update(Constant.Content.TABLE_ITEMS, values, Constant.Content._ID + "=" + id, selectionArgs);
                notifyItemTableChanged();
                break;
            case FEED_INDEX_TABLE:
                ret = db.update(Constant.Content.TABLE_FEED_INDEXS, values, selection, selectionArgs);
//                notifyFeedIndexTableChanged();
                break;
            case ITEM_INDEX_TABLE:
                ret = db.update(Constant.Content.TABLE_ITEM_INDEXS, values, selection, selectionArgs);
                notifyIndexTableChanged();
//                notifyItemIndexTableChanged();
                break;
            case HISTORY_TABLE:
                ret = db.update(Constant.Content.TABLE_HISTORYS, values, selection, selectionArgs);
                notifyHistoryTableChanged();
                break;
            case HISTORY_TABLE_ID:
                id = ContentUris.parseId(uri);
                ret = db.update(Constant.Content.TABLE_HISTORYS, values, Constant.Content._ID + "=" + id, selectionArgs);
                notifyHistoryTableChanged();
                break;
            case ACTIVITY_COUNT_TABLE:
                ret = db.update(Constant.Content.TABLE_ACTIVITYCOUNT, values, selection, selectionArgs);
                notifyActivityCountTableChanged();
                break;
            case BUNDLE_TABLE:
                
                ret = db.update(Constant.Content.TABLE_BUNDLES, values, selection, selectionArgs);
                break;
            case BUNDLE_TABLE_ID:
                id = ContentUris.parseId(uri);
                ret = db.update(Constant.Content.TABLE_BUNDLES, values, Constant.Content._ID + "=" + id, selectionArgs);
                break;
            case BUNDLE_TABLE_TIMES:
                long orderTime = System.currentTimeMillis();
                String updateSql = "UPDATE " + Constant.Content.TABLE_BUNDLES + " SET " + Constant.Content.BUNDLE_DATE + "=" + orderTime + ", " + Constant.Content.BUNDLE_TIMES + "=" + Constant.Content.BUNDLE_TIMES + "+1 WHERE " + selection;
                db.execSQL(updateSql);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }catch(SQLException e){
            Log.d(TAG, e.getMessage());
            return -1;
        }
        return ret;
    }
    
    protected void notifyWidgetTableChanged(){
        getContext().getContentResolver().notifyChange(Constant.Content.WIDGET_URI, null);
    }
    protected void notifyFeedTableChanged(){
        getContext().getContentResolver().notifyChange(Constant.Content.FEED_URI, null);
    }
    protected void notifyItemTableChanged(){
        getContext().getContentResolver().notifyChange(Constant.Content.ITEM_URI, null);
    }
    protected void notifyIndexTableChanged(){
        getContext().getContentResolver().notifyChange(Constant.Content.ITEM_INDEX_URI, null);
    }
    protected void notifyHistoryTableChanged(){
        getContext().getContentResolver().notifyChange(Constant.Content.HISTORY_URI, null);
    }
    
    protected void notifyActivityCountTableChanged() {
        getContext().getContentResolver().notifyChange(Constant.Content.ACTIVITYCOUNT_URI, null);
    }
    @Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        SQLiteDatabase db = mSQLDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentProviderResult[] results = super.applyBatch(operations);
            db.setTransactionSuccessful();
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            db.endTransaction();
        }
    }
}
