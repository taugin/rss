package com.android.rss.widget;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.rss.R;
import com.android.rss.common.Feed;
import com.android.rss.common.Constant;
import com.android.rss.fragment.SubscribedListFragment;

public class SelectFeedForWidgetActivity extends Activity {
    private static final int MENU_ITEM_SELECTALL = 0;
    private int mAppWidgetId;
    private FragmentManager mFragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_feed);
        mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        mFragmentManager = getFragmentManager();
        SubscribedListFragment fragment = new SubscribedListFragment(mAppWidgetId);
        mFragmentManager.beginTransaction().replace(R.id.content_select_feed, fragment, "select_feed").commit();
    }
    
    public void onClick(View view){
        int id = view.getId();
        switch(id){
        case R.id.ok:
            updateWidgetTable();
            updateWidget();
            finish();
            break;
        case R.id.create_cancel:
            finish();
            break;
        default:
            break;
        }
    }
    
    private void updateWidgetTable(){
        ListFragment fragment = (ListFragment) mFragmentManager.findFragmentByTag("select_feed");
        ListView listview = fragment.getListView();
        ListAdapter adapter = listview.getAdapter();
        String feeds = "";
        Feed feed = null;
        SparseBooleanArray array = listview.getCheckedItemPositions();
        for(int i = 0; i<listview.getCount(); i++){
            if(array.get(i)){
                feed = (Feed) adapter.getItem(i);
                feeds += "," + feed.feedId;
            }
        }
        if(feeds.length() > 1){
            feeds = feeds.substring(1);
        }
        String where = Constant.Content.WIDGET_ID + "=" + mAppWidgetId;
        ContentValues values = new ContentValues();
        values.put(Constant.Content.WIDGET_FEEDS, feeds);
        getContentResolver().update(Constant.Content.WIDGET_URI, values, where, null);
    }
    private void updateWidget(){
        Intent intent = new Intent(Constant.Intent.INTENT_RSSAPP_WIDGET_UPDATE);
        sendBroadcast(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ITEM_SELECTALL, 0, R.string.select_all).setCheckable(true).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(MENU_ITEM_SELECTALL);
        item.setChecked(allFeedSelected());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        selectAll(!item.isChecked());
        return true;
    }
    private boolean allFeedSelected(){
        ListFragment fragment = (ListFragment) mFragmentManager.findFragmentByTag("select_feed");
        ListView listview = fragment.getListView();
        return listview.getCheckedItemCount() == listview.getCount();
    }
    private void selectAll(boolean selectAll){
        ListFragment fragment = (ListFragment) mFragmentManager.findFragmentByTag("select_feed");
        ListView listview = fragment.getListView();
        int count = listview.getCount();
        for(int i = 0; i < count; i++){
            listview.setItemChecked(i, selectAll);
        }
    }
}
