package com.android.rss.common;

import android.net.Uri;

public class Constant{
    
    public static class Intent {
        public static final String INTENT_RSSAPP_BOOT_COMPLETED                     = "com.android.rss.intent.action.RSSSERVICE_BOOT_COMPLETED";
        public static final String INTENT_RSSAPP_SERVICE_START                         = "com.android.rss.intent.action.RSSSERVICE_START";
        public static final String INTENT_RSSAPP_WIDGET_UPDATE                        = "com.android.rss.intent.action.RSSWIDGET_UPDATE";
        public static final String INTENT_RSSAPP_WIDGET_REFRESH                     = "com.android.rss.intent.action.RSSWIDGET_START_RFRESH";
        public static final String INTENT_RSSAPP_FEED_SUBSCRIBE                     = "com.android.rss.intent.action.FEED_SUBSCRIBE";
        public static final String INTENT_RSSAPP_NETWORK_NOTAVAILABLE                 = "com.android.rss.intent.action.NETWORK_NOTAVAILABLE";
        public static final String INTENT_RSSAPP_AUTOREFRESH                         = "com.android.rss.intent.action.RSSSERVICE_AUTOREFRESH";
        public static final String INTENT_RSSAPP_MANUALREFRESH                         = "com.android.rss.intent.action.RSSSERVICE_MANUALREFRESH";
        public static final String INTENT_RSSAPP_STARTREFRESH                         = "com.android.rss.intent.action.RSSSERVICE_STARTREFRESH";
        public static final String INTENT_RSSAPP_ADDFEED                             = "com.android.rss.intent.action.RSSSERVICE_ADDFEED";
        public static final String INTENT_RSSAPP_VALIDITY_CHECKING                     = "com.android.rss.intent.action.RSSSERVICE_VALIDITY_CHECKING";
        public static final String INTENT_RSSAPP_WIDGET_DELETE                         = "com.android.rss.intent.action.RSSWIDGET_DELETE";
        public static final String INTENT_RSSAPP_WIDGET_CONFIG                         = "com.android.rss.intent.action.APPWIDGET_CONFIGURE";
        public static final String INTENT_RSSAPP_SETTINGS                             = "com.android.rss.intent.action.SETTINGS";
        public static final String INTENT_RSSAPP_UPDATE_ALARM                         = "com.android.rss.intent.action.RSSAPP_UPDATE_ALARM";
        public static final String INTENT_RSSAPP_DELETE_FEED                         = "com.android.rss.intent.action.RSSAPP_DELETE_FEED";
        public static final String INTENT_RSSAPP_ADD_ALARM                             = "com.android.rss.intent.action.RSSAPP_ADD_ALARM";
        public static final String INTENT_RSSAPP_SET_ALARM                             = "com.android.rss.intent.action.RSSAPP_SET_ALARM";
        public static final String INTENT_RSSAPP_DELETE_ALARM                         = "com.android.rss.intent.action.RSSAPP_DELETE_ALARM";
        public static final String INTENT_RSSAPP_SUBSCRIBE_FINISHED                 = "com.android.rss.intent.action.RSSAPP_SUBSCRIBE_FINISHED";
        public static final String INTENT_RSSAPP_UPDATE_FINISHED                     = "com.android.rss.intent.action.RSSAPP_UPDATE_FINISHED";
        public static final String INTENT_RSSAPP_FEED_ADDED_FROM_EMPTY_FINISHED     = "com.android.rss.intent.action.RSSAPP_FEED_ADDED_FROM_EMPTY_FINISHED";
        public static final String INTENT_RSSAPP_OPENARTICLE_DETAIL                 = "com.android.rss.intent.action.OPEN_ARTICLE_DETAIL";
        public static final String INTENT_RSSAPP_OPENARTICLE_DETAIL_FROMLIST         = "com.android.rss.intent.action.OPEN_ARTICLE_DETAIL_FROMLIST";
        public static final String INTENT_RSSAPP_CHECK_URL                             = "com.android.rss.intent.action.CHECK_URL";
        public static final String INTENT_RSSAPP_CHECK_URL_COMPLETE                 = "com.android.rss.intent.action.CHECK_URL_COMPLETE";
        public static final String INTENT_RSSAPP_EXITED                             = "com.android.rss.intent.action.RSSAPP_EXITED";
        public static final String INTENT_RSSAPP_STOP_UPDATING                         = "com.android.rss.intent.action.STOP_UPDATING";
        public static final String INTENT_RSSAPP_UPDATE_ITEM                         = "com.android.rss.intent.action.UPDATE_ITEM";
        public static final String INTENT_RSSAPP_NOTIFY_WIDGET_UPDATE                 = "com.android.rss.intent.action.NOTIFY_WIDGET_UPDATE";
        public static final String INTENT_RSSAPP_INSTALL_EMPTY_VIEW                 = "com.android.rss.intent.action.INSTALL_EMPTY_VIEW";
        public static final String INTENT_RSSAPP_NEW_ITEMS_DISPLAY                     = "com.android.rss.intent.action.NEW_ITEMS_DISPLAY";
        public static final String INTENT_RSSAPP_CLEAR_NOTIFICATION                 = "com.android.rss.intent.action.CLEAR_NOTIFICATION";
        public static final String INTENT_RSSAPP_UPDATE_WIDGET_NAME                 = "com.android.rss.intent.action.UPDATE_WIDGET_NAME";
        public static final String INTENT_RSSAPP_UPDATE_WIDGET_FOR_EXCEPTION         = "com.android.rss.intent.action.UPDATE_WIDGET_FOR_EXCEPTION";
        public static final String INTENT_RSSAPP_SERVICE_RESTART                     = "com.android.rss.intent.action.SERVICE_RESTART";
        public static final String INTENT_RSSAPP_START_ARTICLE_ACTIVITY             = "com.android.rss.intent.action.START_ARTICLE_ACTIVITY";
        public static final String INTENT_RSSAPP_UPDATE_NEWS_LIST                     = "com.android.rss.intent.action.UPDATE_NEWS_LIST";
        public static final String INTENT_RSSAPP_OPEN_ARTICLE                         = "com.android.rss.intent.action.OPEN_ARTICLE";
    }
    
    public static class Content {
        public static final int DB_VERSION = 1;
        public static final String AUTHORITY = "com.android.rss.database.RSSProvider";
        public static final String DB_NAME = "rsswidget.db";
        public static final String TABLE_WIDGETS = "widget_table";
        public static final String TABLE_FEEDS = "feed_table";
        public static final String TABLE_ITEMS = "item_table";
        public static final String TABLE_FEED_INDEXS = "feed_index_table";
        public static final String TABLE_ITEM_INDEXS = "item_index_table";
        public static final String TABLE_HISTORYS = "history_table";
        public static final String TABLE_BUNDLES = "bundle_table";
        public static final String VIEW_QUERY_ITEM = "view_query_item";
        public static final String VIEW_QUERY_FEED = "view_query_feed";
        public static final String TABLE_ACTIVITYCOUNT = "activitycount_table";
        public static final String VIEW_LIMITED_ITEM = "view_limited_item";
        
        public static final Uri WIDGET_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_WIDGETS);
        public static final Uri FEED_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_FEEDS);
        public static final Uri ITEM_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_ITEMS);
        public static final Uri FEED_INDEX_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_FEED_INDEXS);
        public static final Uri ITEM_INDEX_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_ITEM_INDEXS);
        public static final Uri HISTORY_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_HISTORYS);
        public static final Uri ACTIVITYCOUNT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_ACTIVITYCOUNT);
        public static final Uri VIEW_FEED_URI = Uri.parse("content://" + AUTHORITY + "/" + VIEW_QUERY_FEED);
        public static final Uri VIEW_ITEM_URI = Uri.parse("content://" + AUTHORITY + "/" + VIEW_QUERY_ITEM);
        public static final Uri VIEW_LIMITED_ITEM_URI = Uri.parse("content://" + AUTHORITY + "/" + VIEW_LIMITED_ITEM);
        public static final Uri BUNDLE_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_BUNDLES);

        public static final String WIDGET_CONTENT_TYPE = "vnd.android.cursor.item/vnd.rss.items";
        public static final String FEED_CONTENT_TYPE = "vnd.android.cursor.item/vnd.rss.feeds";
        public static final String ITEM_CONTENT_TYPE = "vnd.android.cursor.item/vnd.rss.items";
        public static final String INDEX_CONTENT_TYPE = "vnd.android.cursor.item/vnd.rss.indexs";
        public static final String HISTORY_CONTENT_TYPE = "vnd.android.cursor.item/vnd.rss.items";
        public static final String ACTIVITY_COUNT_CONTENT_TYPE = "vnd.android.cursor.item/vnd.rss.items";
        
        public static final String WIDGET_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.rss.item";
        public static final String FEED_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.rss.feed";
        public static final String ITEM_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.rss.item";
        public static final String INDEX_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.rss.item";
        public static final String HISTORY_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.rss.item";
        
        public static final String _ID = "_id";
        
        public static final String WIDGET_ID = "widget_id";
        public static final String FEED_ID = "feed_id";
        public static final String ITEM_ID = "item_id";
        public static final String DELETED = "deleted";
        public static final String FOO = "foo";
        
        public static final String WIDGET_TITLE = "widget_title";
        public static final String WIDGET_LAYOUT = "widget_update_frequency";
        public static final String WIDGET_FEEDS = "widget_validaty_time";
        
        public static final String FEED_TITLE = "feed_title";        
        public static final String FEED_URL = "feed_url";
        public static final String FEED_AUTHOR = "feed_author";
        public static final String FEED_IS_BUNDLE = "feed_is_bundle";
        public static final String FEED_ICON = "feed_icon";
        public static final String FEED_PUBDATE = "feed_pubdate";
        public static final String FEED_GUID = "feed_guid";
        public static final String FEED_STATE = "feed_state";
        public static final String FEED_ICON_STATE = "feed_icon_state";
        public static final String FEED_ORI_TITLE = "feed_ori_title";
        public static final String FEED_CLASS = "feed_class";
        public static final String FEED_CLASS_GUID = "feed_class_guid";
        public static final String FEEDS_ON_WIDGET = "feed_on_widget";
        
        public static final String ITEM_TITLE = "item_title";
        public static final String ITEM_URL = "item_url";
        public static final String ITEM_DESCRIPTION = "item_description";
        public static final String ITEM_GUID = "item_guid";
        public static final String ITEM_STATE = "item_state";
        public static final String ITEM_AUTHOR = "item_author";
        public static final String ITEM_PUBDATE = "item_pubdate";
        public static final String ITEM_DES_BRIEF = "item_des_brief";
        public static final String ITEM_COUNT = "item_count";
        public static final String ITEM_READ_COUNT = "item_read_count";
        public static final String ITEM_UNREAD_COUNT = "item_unread_count";
        
        public static final String HISTORY_TITLE = "title";
        public static final String HISTORY_URL = "url";
        public static final String HISTORY_TIMES = "times";
        public static final String HISTORY_DATE = "date";
        
        public static final String ACTIVITY_COUNT = "count";
        
        
        public static final String VIEW_WIDGET_ID = "view_widget_id";
        public static final String VIEW_FEED_ID = "view_feed_id";
        public static final String VIEW_FEED_TITLE = "view_feed_title";
        
        public static final String VIEW_ITEM_ID = "view_item_id";
        public static final String VIEW_ITEM_TITLE = "view_item_title";
        public static final String VIEW_ITEM_URL = "view_item_url";
        public static final String VIEW_ITEM_DESCRIPTION = "view_item_description";
        public static final String VIEW_ITEM_AUTHOR = "view_item_author";
        public static final String VIEW_ITEM_STATE = "view_item_state";
        public static final String VIEW_ITEM_PUBDATE = "view_item_pubdate";
        
        
        public static final String BUNDLE_TITLE = "bundle_title";
        public static final String BUNDLE_URL = "bundle_url";
        public static final String BUNDLE_GUID = "bundle_guid";
        public static final String BUNDLE_TIMES = "bundle_times";
        public static final String BUNDLE_DATE = "bundle_date";
                
        /**
         *Update frequency 
         */
        public static final int UPDATE_FRE_NEVER = 0;
        public static final int UPDATE_FRE_ONE_HOUR = 1;
        public static final int UPDATE_FRE_THREE_HOURS = 2;
        public static final int UPDATE_FRE_SIX_HOURS = 3;
        public static final int UPDATE_FRE_TWELVE_HOURS = 4;
        public static final int UPDATE_FRE_ONE_DAY = 5;
        
        /**
         *Show item for 
         */
        public static final int SHOW_ITEMS_ONE_DAY = 0;
        public static final int SHOW_ITEMS_TWO_DAYS = 1;
        public static final int SHOW_ITEMS_THREE_DAYS = 2;
        public static final int SHOW_ITEMS_ONE_WEEK = 3;
        public static final int SHOW_ITEMS_ONE_MONTH = 4;
        
        
        public static final String SHARED_PREFERENCE_NAME = "rss_widget_shared_preference";
        
        
        public static final String EXTRA_POSITION = "extra_position";
        public static final String EXTRA_NEWS_ID = "extra_news_id";
        public static final String EXTRA_FEED_ID = "extra_feed_id";
    }
    
    public static class Flag{
        public static final int FEED_REQUESTCODE = 0;
        public static final int FEEDBUNDLELIST_RESULECODE = 1;
        public static final int FEEDCUSTOMADDED_RESULECODE = 2;
        public static final int FEEDCUSTOMCONFIRM_RESULECODE = 3;
        public static final int FEEDADDBYCUSTOM_RESULECODE = 4;
        public static final int FEEDADDBYBUNDLE_RESULECODE = 5;
        public static final int FEEDADDBYHISTORY_RESULECODE = 6;
        public static final int FEEDHISTORYLIST_RESULECODE = 7;
        public static final int WIDGETCONFIG_NEWADDED_RESULTCODE = 8;
        public static final int WIDGETCONFIG_NOADDED_RESULTCODE = 9;
        
        
        public static final int ADD_RSS_NEWS = 1;
        public static final int UPDATE_RSS_NEWS = 2;
        public static final int CHECK_RSS_ADDRESS = 3;
        public static final String ITEM_POSITION = "item_position";
        
        public static final int WIDGET_NOFEED = 0;
        public static final int WIDGET_NOFEED_ITEMSTATE = 0;
    }
    
    public static class State{
        public static final int STATE_ITEM_UNREAD = 0;
        public static final int STATE_ITEM_READ = 1;
        
        public static final int STATE_UNDELETED = 0;
        public static final int STATE_DELETED = 1;
        
        public static final int STATE_NO_BUNDLE = 0;
        public static final int STATE_BUNDLE = 1;
        
        public static final int STATE_SUCCESS = 0;
        
        public static final int STATE_NETWORK_NOT_AVAILABLE = 1;
        
        public static final int STATE_PARSE_XML_FAILURE = 2;
        
        public static final int STATE_URL_NOT_AVAILABLE = 3;
        
        public static final int STATE_CONNECTY_FAILURE = 4;

        public static final int STATE_HAS_INVALID_DATA = 5; //Add by wdmk68
        
        
        public static final int STATE_NO_PARSER = 6;
        
        public static final int STATE_SERVICE_RESTART = 7;
        
        public static final int STATE_NO_SUBSCRIBED = -1;
        public static final int STATE_SUBSCRIBED = 0;
        public static final int STATE_SUBSCRIBING = 1;
        public static final int STATE_WAITING = 2;
        public static final int STATE_FAILURE = 3;
        public static final int STATE_SELECTED = 4;
        
        public static final int STATE_VIEW_SUBSCRIBED = 0;
        public static final int STATE_VIEW_BUNDLES = 1;
        public static final int STATE_VIEW_CUSTOM = 2;
        public static final int STATE_VIEW_MANAGE_SUB = 3;
        public static final int STATE_VIEW_MANAGE_CUSTOM = 4;
        
        public static final int STATE_REFRESH_NORMAL = 1;
        public static final int STATE_REFRESH_ONGOING = 2;
    }    
    
    public static class Key{
        public static final String KEY_WIDGET_ID = "key_widget_id";
        public static final String KEY_FEED_ID = "key_feed_id";
        public static final String KEY_ITEM_ID = "key_item_id";        
        public static final String KEY_CHECK_URL = "key_check_url";
        public static final String KEY_REFRESH_STATE = "key_refresh_complete";
        public static final String KEY_THREAD_RUNNING = "key_thread_running";
        public static final String KEY_NEXT_TIME = "key_next_time";
    }
    
    public static class Value{
        public static final long ONE_HOUR = 1 * 3600 * 1000l;
        public static final long THREE_HOURS = 3 * 3600 * 1000l;
        public static final long SIX_HOURS = 6 * 3600 * 1000l;
        public static final long TWELVE_HOURS = 12 * 3600 * 1000l;
        public static final long ONE_DAY = 24 * 3600 * 1000l;
        public static final long TWO_DAYS = 2 * 24 * 3600 * 1000l;
        public static final long THREE_DAYS = 3 * 24 * 3600 * 1000l;
        public static final long ONE_WEEK = 7 * 24 * 3600 * 1000l;
        public static final long ONE_MONTH = 30 * 24 * 3600 * 1000l;
        
        public static final int MAX_FEED_ADDEDNUM  = 50;
        public static final int MAX_HISTORY = 50;
    }
    
}
