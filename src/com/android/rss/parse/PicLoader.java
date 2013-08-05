package com.android.rss.parse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.widget.ImageView;

import com.android.rss.common.FeedInfo;
import com.android.rss.common.Constant;
import com.android.rss.util.Log;
import com.android.rss.util.NetworkUtil;

public class PicLoader extends Thread{
    private static final String TAG = "IconLoader";
    private Context mContext;
    private int feedId;
    private String iconUrl;
    private ImageView mImageView;
    private Handler mHandler;
    private IconLoadFinishListener mIconLoadFinishListener;
    private static final int MAX_ICON_SIZE = 50 * 1000;
    private FeedInfo mFeedInfo;
    
    public PicLoader(Context context, int feedId, String iconUrl, ImageView image){
        mContext = context;
        this.feedId = feedId;
        this.iconUrl = iconUrl;    
        mImageView = null;
        mHandler = new Handler(context.getMainLooper());
    }
    public PicLoader(Context context, FeedInfo info){
        mContext = context;
        this.feedId = info.feedId;
        this.iconUrl = info.feedIcon;    
        mImageView = null;
        mFeedInfo = info;
        mHandler = new Handler(context.getMainLooper());
    }
    
    public void run(){
        Log.d(TAG, "IconLoader run, start fetch image icon");
        ContentValues values = new ContentValues();
        
        try {
            String where = Constant.Content._ID + "=" + feedId;
            byte image[] = getImage(iconUrl);
            Log.d(TAG, "image = " + image);
            values.put(Constant.Content.FEED_ICON, image);
            if(image != null){
                values.put(Constant.Content.FEED_ICON_STATE, "icon");
                mFeedInfo.feedIconState = "icon";
            }else{
                values.put(Constant.Content.FEED_ICON_STATE, "noicon");
                mFeedInfo.feedIconState = "noicon";
            }
            Log.d(TAG, "iconUrl = " + iconUrl + " , feedId = " + feedId + " , state = " + values.getAsString(Constant.Content.FEED_ICON_STATE));
            mContext.getContentResolver().update(Constant.Content.FEED_URI, values, where, null);
            synchronized (mFeedInfo.feedIcon) {
                mFeedInfo.feedIcon.notify();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if(mIconLoadFinishListener != null){
                mIconLoadFinishListener.IconLoadFinish(feedId);
            }
        }
    }
    
    
    public static byte[] getImage(String urlpath) throws Exception {
        InputStream in = NetworkUtil.getImageStream(urlpath);
        if(in != null){
            Log.d(TAG, "fetched the image input stream");
            return readStream(in);
        }else{
            Log.d(TAG, "fetched no image input stream");
            return null;
        }
    }
    
    
    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outstream.write(buffer, 0, len);
        }
        outstream.close();
        inStream.close();
        Log.d(TAG, "size is " + outstream.toByteArray().length);
        if(outstream.toByteArray().length > MAX_ICON_SIZE) {
            return null;
        }        
        return outstream.toByteArray();
    }
    
    public void setLoadFinishListener(IconLoadFinishListener listener) {
        mIconLoadFinishListener = listener;
    }
    
    public interface IconLoadFinishListener {
        public void IconLoadFinish(int feedId);
    }
}
