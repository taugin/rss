package com.android.rss;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.rss.common.Constant;
import com.android.rss.util.Log;

public class RSSSearchActivity extends ListActivity {

    private static final String TAG = "RSSSearchActivity";
    private static final int QUERY_TOKEN = 1000;
    private RSSAsyncHandler mRSSAsyncHandler;
    private RSSSearchAdapter mRSSSearchAdapter;
    private String[] PROJECTION = new String[] { Constant.Content._ID, Constant.Content.ITEM_TITLE, Constant.Content.ITEM_URL };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);
        mRSSAsyncHandler = new RSSAsyncHandler(getContentResolver(), this);
        mRSSSearchAdapter = new RSSSearchAdapter(this, null);
        getListView().setAdapter(mRSSSearchAdapter);
        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String url = (String)v.getTag();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {

        }
    }

    private void handleIntent(Intent intent) {
        Log.d(TAG, "action = " + intent.getAction() + " , data = " + intent.getData());
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            try {
                String url = intent.getDataString();
                String realString = url.substring(0, url.lastIndexOf('/'));
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(realString)));
                finish();
            } catch (ActivityNotFoundException e) {

            }
        }
    }

    private void doSearch(String queryStr) {
        String selection = Constant.Content.ITEM_TITLE + " like " + "'"
                + queryStr + "%'";
        mRSSAsyncHandler.startQuery(QUERY_TOKEN, null, Constant.Content.ITEM_URI, PROJECTION,
                selection, null, null);
    }

    class RSSSearchAdapter extends CursorAdapter {

        private LayoutInflater mLayoutInflater = null;

        public RSSSearchAdapter(Context context, Cursor c) {
            super(context, c);
            mLayoutInflater = (LayoutInflater) context
                    .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView textView = (TextView) view;
            String displayName = cursor
                    .getString(cursor
                            .getColumnIndexOrThrow(Constant.Content.ITEM_TITLE));
            String url = cursor.getString(cursor.getColumnIndexOrThrow(Constant.Content.ITEM_URL));
            textView.setTag(url);
            textView.setText(displayName);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            TextView textView = (TextView) mLayoutInflater.inflate(
                    android.R.layout.simple_list_item_1, null);
            return textView;
        }

    }

    class RSSAsyncHandler extends AsyncQueryHandler {

        private WeakReference<Activity> mActivity;

        public RSSAsyncHandler(ContentResolver cr, Activity activity) {
            super(cr);
            mActivity = new WeakReference<Activity>(activity);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (QUERY_TOKEN == token) {
                mRSSSearchAdapter.changeCursor(cursor);
            }
        }
    }

}
