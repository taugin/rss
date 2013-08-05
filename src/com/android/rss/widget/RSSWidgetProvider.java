package com.android.rss.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import com.android.rss.R;
import com.android.rss.RSSFeedsList;
import com.android.rss.common.PreferenceKeys;
import com.android.rss.common.Constant;

public class RSSWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sharedPreferences.getString(PreferenceKeys.KEY_WIDGET_LAYOUT, "list");
        for(int id : appWidgetIds){
            startRemoteService(context, appWidgetManager, id, value);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void startRemoteService(Context context, AppWidgetManager appWidgetManager, int id, String value){
        PendingIntent pendingIntent = null;
        Intent intent = new Intent(context, RSSWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        rv.setViewVisibility(R.id.rssnews_list, View.INVISIBLE);
        rv.setViewVisibility(R.id.rssnews_grid, View.INVISIBLE);
        rv.setViewVisibility(R.id.rssnews_stack, View.INVISIBLE);
        int rssId = 0;
        if("list".equals(value)){
            rssId = R.id.rssnews_list;
        }else if("grid".equals(value)){
            rssId = R.id.rssnews_grid;
        }else {
            rssId = R.id.rssnews_stack;
        }
        rv.setViewVisibility(rssId, View.VISIBLE);
        Intent settingIntent = new Intent("com.android.rss.intent.action.RSSAPP_SETTINGS");
        pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), settingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widget_settings, pendingIntent);
        
        Intent selectFeed = new Intent(context, SelectFeedForWidgetActivity.class);
        selectFeed.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), selectFeed, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widget_select_feed, pendingIntent);
        
        Intent mainApp = new Intent(context, RSSFeedsList.class);
        selectFeed.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), mainApp, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widget_main_app, pendingIntent);

        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setRemoteAdapter(id, rssId, intent);
        rv.setEmptyView(rssId, R.id.nonews);


        Intent openIntent = new Intent(context, RSSWidgetNewsActivity.class);
        openIntent.setAction(Constant.Intent.INTENT_RSSAPP_OPEN_ARTICLE);
        openIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        pendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(rssId, pendingIntent);
        appWidgetManager.notifyAppWidgetViewDataChanged(id, rssId);
        appWidgetManager.updateAppWidget(id, rv);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = null;
        if(intent != null){
            action = intent.getAction();
            if(Constant.Intent.INTENT_RSSAPP_WIDGET_UPDATE.equals(action)){
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, RSSWidgetProvider.class));
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                String value = sharedPreferences.getString(PreferenceKeys.KEY_WIDGET_LAYOUT, "list");
                for(int id : appWidgetIds){
                    startRemoteService(context, appWidgetManager, id, value);
                }
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for(int id : appWidgetIds){
            String where = Constant.Content.WIDGET_ID + "=" + id;
            context.getContentResolver().delete(Constant.Content.WIDGET_URI, where, null);
        }
        super.onDeleted(context, appWidgetIds);
    }
    
    
}
