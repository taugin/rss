package com.android.rss.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.android.rss.common.PreferenceKeys;
import com.android.rss.common.Constant;

public class AlarmUtil {
    private static final String TAG = "AlarmUtil";
    public static final void setAlarm(Context context, long time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
        String curMinituString = sdf.format(new Date(time));
        Log.d(TAG, "curMinituString = " + curMinituString);
        long curMinitu = 0;
        try {
			Date date = sdf.parse(curMinituString);
			curMinitu = date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
        String hourString = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.KEY_UPDATE_FREQUENCY, "0");
        int hour = Integer.parseInt(hourString);
        Log.d(TAG, "Hour = " + hour);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Service.ALARM_SERVICE);
        Intent alarmIntent = new Intent();
        PendingIntent pendingIntent = null;
        alarmIntent.setAction(Constant.Intent.INTENT_RSSAPP_STARTREFRESH);
        int requestCode = 0;
        pendingIntent = PendingIntent.getService(context, requestCode, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        long duration = hour * 60 * 60 * 1000;
        if(hour == 0){
            alarmManager.cancel(pendingIntent);
        }else{
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, curMinitu + duration, duration, pendingIntent);
        }
    }
    
    public static final void setNextUpdateTime(Context context, long now){
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
        String curMinituString = sdf.format(new Date(now));
        Log.d(TAG, "curMinituString = " + curMinituString);
        long curMinitu = 0;
        try {
			Date date = sdf.parse(curMinituString);
			curMinitu = date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
        String hourString = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceKeys.KEY_UPDATE_FREQUENCY, "0");
        int hour = Integer.parseInt(hourString);
        long duration = hour * 60 * 60 * 1000;
        long nextTime = curMinitu + duration;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(Constant.Key.KEY_NEXT_TIME, nextTime).commit();
    }
}
