package com.android.rss.fragment;

import java.util.ArrayList;

import android.app.ListFragment;
import android.appwidget.AppWidgetManager;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.rss.R;
import com.android.rss.common.Feed;
import com.android.rss.common.Constant;

public class SubscribedListFragment extends ListFragment {

    private ArrayList<Feed> mFeedList;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    public SubscribedListFragment(){
        
    }
    public SubscribedListFragment(int appWidgetId){
        mAppWidgetId = appWidgetId;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.subscribed_list, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFeedList = new ArrayList<Feed>();
        getFeedList();
        ArrayAdapter<Feed> adapter = null;
        adapter = new ArrayAdapter<Feed>(getActivity(), android.R.layout.simple_list_item_multiple_choice, android.R.id.text1,  mFeedList);
        getListView().setAdapter(adapter);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        updateSelect();
    }
    private void updateSelect(){
        Cursor c = null;
        String feeds = null;
        String selection = Constant.Content.WIDGET_ID + "=" + mAppWidgetId;
        try{
            c = getActivity().getContentResolver().query(Constant.Content.WIDGET_URI, null, selection, null, null);
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
        if(!TextUtils.isEmpty(feeds)){
            String []feedIds = feeds.split(",");
            for(int i = 0; i< feedIds.length;i++){
                int id = Integer.parseInt(feedIds[i]);
                setSelectFeed(id);
            }
        }
    }
    private void setSelectFeed(int id){
        ListAdapter adapter = getListView().getAdapter();
        int count = adapter.getCount();
        for(int i = 0; i< count; i++){
            Feed feed = (Feed) adapter.getItem(i);
            if(feed.feedId == id){
                getListView().setItemChecked(i, true);
                break;
            }
        }
    }
    private void getFeedList(){
        mFeedList.clear();
        String selection = Constant.Content.FEED_STATE + " = " + Constant.State.STATE_SUBSCRIBED;
        String []projection = new String[]{Constant.Content._ID, Constant.Content.FEED_TITLE, Constant.Content.FEED_ORI_TITLE};
        String sortOrder = Constant.Content.FEED_PUBDATE + " desc";
        Cursor c = null;
        Feed info = null;
        try{
            c = getActivity().getContentResolver().query(Constant.Content.FEED_URI, projection, selection, null, sortOrder);
            if(c != null){
                if(c.moveToFirst()){
                    do{
                        info = new Feed();
                        info.feedId = c.getInt(c.getColumnIndex(Constant.Content._ID));
                        info.feedTitle = c.getString(c.getColumnIndex(Constant.Content.FEED_TITLE));
                        if(TextUtils.isEmpty(info.feedTitle)){
                            info.feedTitle = c.getString(c.getColumnIndex(Constant.Content.FEED_ORI_TITLE));
                        }
                        mFeedList.add(info);
                    }while(c.moveToNext());
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
    
}
