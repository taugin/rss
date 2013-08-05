package com.android.rss.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.AndroidRuntimeException;

import com.android.rss.common.PreferenceKeys;
import com.android.rss.common.Constant;
import com.android.rss.parse.RSSWorkerThread;
import com.android.rss.util.AlarmUtil;
import com.android.rss.util.Log;

public class RSSService extends Service {
    static final int THREAD_POOL_THREAD_TIMEOUT = 60; 
    private static final String TAG = "RSSService";
    private RSSWorkerThread mRSSWorker2;
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
    }

    @Override
    public void onDestroy() {
        Log.d(TAG ,"onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        LogUtils.debug(TAG, "onStartCommand called intent = " + intent);
        if(intent != null){
            if(Constant.Intent.INTENT_RSSAPP_MANUALREFRESH.equals(intent.getAction())){
               startupThread();
            }else if(Constant.Intent.INTENT_RSSAPP_VALIDITY_CHECKING.equals(intent.getAction())){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String date = sdf.format(System.currentTimeMillis());
                long validate = 0;
                long today = 0;
                try {
                    today = sdf.parse(date).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String dayString = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceKeys.KEY_VALIDADY_TIME, "0");
                int day = Integer.parseInt(dayString);
                validate = day * 24 * 3600l;
                long specTime = today - validate; 
                String where = Constant.Content.ITEM_PUBDATE + " < " + specTime;
                getContentResolver().delete(Constant.Content.ITEM_URI, where, null);
                updateWidget();
            }else if(Constant.Intent.INTENT_RSSAPP_SET_ALARM.equals(intent.getAction())){
                long now = System.currentTimeMillis();
                AlarmUtil.setAlarm(this, now);
                AlarmUtil.setNextUpdateTime(this, now);
            }else if(Constant.Intent.INTENT_RSSAPP_STARTREFRESH.equals(intent.getAction())){
            	AlarmUtil.setNextUpdateTime(this, System.currentTimeMillis());
                Intent autoIntent = new Intent(Constant.Intent.INTENT_RSSAPP_STARTREFRESH);
                sendBroadcast(autoIntent);
                ContentValues values = new ContentValues();
                values.put(Constant.Content.FEED_STATE, Constant.State.STATE_WAITING);
                getContentResolver().update(Constant.Content.FEED_URI, values, null, null);
                startupThread();
            }
        }
        if(intent == null){
            Log.d(TAG ,"intent is null ,so stop the service itself");
            stopSelf();
        }

        return START_STICKY;
    }

    /*
    class FeedAddedObserver extends ContentObserver{
        private Context mContext;
        public FeedAddedObserver(Context context, Handler handler) {
            super(handler);
            mContext = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            LogUtils.debug(TAG, "FeedAddedObserver : onChange");
//            handleAlarm();
            if(mRssThread == null){
                mRssThread = new RSSWorker2(mContext);
                mRssThread.start();
            }else if(mRssThread != null && !mRssThread.isAlive()){
                mRssThread = new RSSWorker2(mContext);
                mRssThread.start();
            }else{
                
            }
        }
    }
    private void handleAlarm(){
        Cursor c = null;
        int count = 0;
        try{
            c = getContentResolver().query(RssConstant.Content.FEED_URI, null, null, null, null);
            if(c != null){
                count = c.getCount();
            }
        }catch (Exception e) {
            e.printStackTrace();
            count = 0;
        }finally{
            if(c != null){
                c.close();
            }
        }
        int index = -1;
        try{
            c = getContentResolver().query(RssConstant.Content.WIDGET_URI, null, null, null, null);
            if(c != null){
                if(c.moveToFirst()){
                    index = c.getInt(c.getColumnIndex(RssConstant.Content.WIDGET_LAYOUT));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            index = -1;
        }finally{
            if(c != null){
                c.close();
            }
        }
        LogUtils.debug(TAG, "index = " + index);
        long updateFrequency = SettingsUtil.indexToUpdateFrequency(index);
        SharedPreferences shared = getSharedPreferences("RSS", MODE_PRIVATE);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Service.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(RssConstant.Intent.INTENT_RSSAPP_STARTREFRESH);
        int requesetCode = -1;
        if(count <= 0){
            LogUtils.debug(TAG, "Need delete alarm !");
            if(shared.contains("TimerForUpdate")){
                LogUtils.debug(TAG, "Alarm has setted , so delete it !");
                requesetCode = shared.getInt("TimerForUpdate", -1);
                PendingIntent refreshPending = PendingIntent.getService(this, requesetCode, intent, 0);
                alarmManager.cancel(refreshPending);
                shared.edit().clear();
            }
        }else{
            LogUtils.debug(TAG, "Need set alarm !");
            if(!shared.contains("TimerForUpdate")){
                LogUtils.debug(TAG, "Alarm has not setted , so set it !");
                requesetCode = Long.valueOf(System.currentTimeMillis()).intValue();
                shared.edit().putInt("TimerForUpdate", requesetCode).commit();
                PendingIntent refreshPending = PendingIntent.getService(this, requesetCode, intent, 0);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + updateFrequency, updateFrequency, refreshPending);
            }
        }
    }
    
    class SettingObserver extends ContentObserver{

        private Context mContext;
        public SettingObserver(Context context, Handler handler) {
            super(handler);
            mContext = context;
        }
        @Override
        public void onChange(boolean selfChange) {
            LogUtils.debug(TAG, "SettingObserver : onChange");
            Cursor c = null;
            int count = 0;
            try{
                c = getContentResolver().query(RssConstant.Content.FEED_URI, null, null, null, null);
                if(c != null){
                    count = c.getCount();
                }
            }catch (Exception e) {
                e.printStackTrace();
                count = 0;
            }finally{
                if(c != null){
                    c.close();
                }
            }
            int index = -1;
            try{
                c = getContentResolver().query(RssConstant.Content.WIDGET_URI, null, null, null, null);
                if(c != null){
                    if(c.moveToFirst()){
                        index = c.getInt(c.getColumnIndex(RssConstant.Content.WIDGET_UPDATE_FREQUENCY));
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
                index = -1;
            }finally{
                if(c != null){
                    c.close();
                }
            }
            LogUtils.debug(TAG, "index = " + index);
            long updateFrequency = SettingsUtil.indexToUpdateFrequency(index);
            SharedPreferences shared = getSharedPreferences("RSS", MODE_PRIVATE);
            AlarmManager alarmManager = (AlarmManager)getSystemService(Service.ALARM_SERVICE);
            Intent intent = new Intent();
            intent.setAction(RssConstant.Intent.INTENT_RSSSERVICE_STARTREFRESH);
            int requesetCode = -1;
            if(count > 0){
                if(shared.contains("TimerForUpdate")){
                    LogUtils.debug(TAG, "Alarm has setted , so delete it !");
                    requesetCode = shared.getInt("TimerForUpdate", -1);
                    PendingIntent refreshPending = PendingIntent.getService(mContext, requesetCode, intent, 0);
                    alarmManager.cancel(refreshPending);
                    shared.edit().clear();
                }
                LogUtils.debug(TAG, "set a new alarm !");
                requesetCode = Long.valueOf(System.currentTimeMillis()).intValue();
                shared.edit().putInt("TimerForUpdate", requesetCode).commit();
                PendingIntent refreshPending = PendingIntent.getService(mContext, requesetCode, intent, 0);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + updateFrequency, updateFrequency, refreshPending);
            }
            intent = new Intent();
            intent.setAction(RssConstant.Intent.INTENT_RSSSERVICE_STARTREFRESH);
            if(index == 0){
                if(shared.contains("TimerForUpdate")){
                    LogUtils.debug(TAG, "index is 0 , so delete it !");
                    requesetCode = shared.getInt("TimerForUpdate", -1);
                    PendingIntent refreshPending = PendingIntent.getService(mContext, requesetCode, intent, 0);
                    alarmManager.cancel(refreshPending);
                    shared.edit().clear();
                }
            }
        }
        
    }*/
    private void updateWidget(){
        Intent intent = new Intent(Constant.Intent.INTENT_RSSAPP_WIDGET_UPDATE);
        sendBroadcast(intent);
    }
    private void startupThread(){
        if(mRSSWorker2 != null){
            Log.d(TAG, "mRSSWorker2 = " + mRSSWorker2 + " , mRSSWorker2.getState() = " + mRSSWorker2.getState());
        }else{
            Log.d(TAG, "mRSSWorker2 = " + mRSSWorker2);
        }
        if(mRSSWorker2 == null){
            mRSSWorker2 = new RSSWorkerThread(this);
            mRSSWorker2.start();
        }else if(mRSSWorker2 != null && mRSSWorker2.getState() == Thread.State.TERMINATED){
            mRSSWorker2 = new RSSWorkerThread(this);
            mRSSWorker2.start();
        }else{
            Log.d(TAG, "RSSWorker2 is running");
        }
    }
}
