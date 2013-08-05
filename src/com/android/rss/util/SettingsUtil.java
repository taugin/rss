package com.android.rss.util;

import android.content.Context;

import com.android.rss.R;
import com.android.rss.common.Constant;


public class SettingsUtil {
    private static final String TAG = "SettingsUtil"; 

    public static String longTimeToStringTime(Context context, long time){
        String timeShow = "";
        if(time == Constant.Value.ONE_HOUR){
            timeShow = context.getResources().getString(R.string.onehour);
        } else if(time == Constant.Value.THREE_HOURS){
            timeShow = context.getResources().getString(R.string.threehours);
        }else if(time == Constant.Value.SIX_HOURS){
            timeShow = context.getResources().getString(R.string.sixhours);
        }else if(time == Constant.Value.TWELVE_HOURS){
            timeShow = context.getResources().getString(R.string.twelvehours);
        }else if(time == Constant.Value.ONE_DAY){
            timeShow = context.getResources().getString(R.string.oneday);
        }else if(time == Constant.Value.TWO_DAYS){
            timeShow = context.getResources().getString(R.string.twodays);
        }else if(time == Constant.Value.THREE_DAYS){
            timeShow = context.getResources().getString(R.string.threedays);
        }else if(time == Constant.Value.ONE_WEEK){
            timeShow = context.getResources().getString(R.string.oneweek);
        }else if(time == Constant.Value.ONE_MONTH){
            timeShow = context.getResources().getString(R.string.onemonth);
        }
        return "";
    }
    
    public static long stringTimeToLongTime(String s){
        return 0;
    }
    
    public static int showNameToindex(Context context, String appear){
        int index = 0;
        Log.d(TAG, "appear = " + appear);
        if(appear.equals(context.getResources().getString(R.string.oneday))){
            index = Constant.Content.SHOW_ITEMS_ONE_DAY;
        }else if(appear.equals(context.getResources().getString(R.string.twodays))){
            index = Constant.Content.SHOW_ITEMS_TWO_DAYS;
        }else if(appear.equals(context.getResources().getString(R.string.threedays))){
            index = Constant.Content.SHOW_ITEMS_THREE_DAYS;
        }else if(appear.equals(context.getResources().getString(R.string.oneweek))){
            index = Constant.Content.SHOW_ITEMS_ONE_WEEK;
        }else if(appear.equals(context.getResources().getString(R.string.onemonth))){
            index = Constant.Content.SHOW_ITEMS_ONE_MONTH;
        }
        return index;
    }
    
    public static int updateNameToindex(Context context, String appear){
        int index = 0;
        if(appear.equals(context.getResources().getString(R.string.oneday))){
            index = Constant.Content.UPDATE_FRE_ONE_DAY;
        }else if(appear.equals(context.getResources().getString(R.string.onehour))){
            index = Constant.Content.UPDATE_FRE_ONE_HOUR;
        }else if(appear.equals(context.getResources().getString(R.string.threehours))){
            index = Constant.Content.UPDATE_FRE_THREE_HOURS;
        }else if(appear.equals(context.getResources().getString(R.string.sixhours))){
            index = Constant.Content.UPDATE_FRE_SIX_HOURS;
        }else if(appear.equals(context.getResources().getString(R.string.twelvehours))){
            index = Constant.Content.UPDATE_FRE_TWELVE_HOURS;
        }else if(appear.equals(context.getResources().getString(R.string.never))){
            index = Constant.Content.UPDATE_FRE_NEVER;
        }
        return index;
    }
    
    public static String indexToShowName(Context context, int index){
        String string = "";
        switch(index){
        case Constant.Content.SHOW_ITEMS_ONE_DAY:
            string =  context.getResources().getString(R.string.oneday);
            break;
        case Constant.Content.SHOW_ITEMS_TWO_DAYS:
            string =  context.getResources().getString(R.string.twodays);
            break;
        case Constant.Content.SHOW_ITEMS_THREE_DAYS:
            string =  context.getResources().getString(R.string.threedays);
            break;
        case Constant.Content.SHOW_ITEMS_ONE_WEEK:
            string =  context.getResources().getString(R.string.oneweek);
            break;
        case Constant.Content.SHOW_ITEMS_ONE_MONTH:
            string =  context.getResources().getString(R.string.onemonth);
            break;
        }
        return string;
    }
    public static String indexToUpdateName(Context context, int index){
        String string = "";
        switch(index){
        case Constant.Content.UPDATE_FRE_ONE_DAY:
            string =  context.getResources().getString(R.string.oneday);
            break;
        case Constant.Content.UPDATE_FRE_ONE_HOUR:
            string =  context.getResources().getString(R.string.onehour);
            break;
        case Constant.Content.UPDATE_FRE_THREE_HOURS:
            string =  context.getResources().getString(R.string.threehours);
            break;
        case Constant.Content.UPDATE_FRE_SIX_HOURS:
            string =  context.getResources().getString(R.string.sixhours);
            break;
        case Constant.Content.UPDATE_FRE_TWELVE_HOURS:
            string =  context.getResources().getString(R.string.twelvehours);
            break;
        case Constant.Content.UPDATE_FRE_NEVER:
            string =  context.getResources().getString(R.string.never);
            break;
        }
        return string;
    }
    public static long indexToUpdateFrequency(int index){
        long update_frequency = 0;
        switch(index){
        case Constant.Content.UPDATE_FRE_ONE_HOUR:
            update_frequency = Constant.Value.ONE_HOUR;
            break;
        case Constant.Content.UPDATE_FRE_THREE_HOURS:
            update_frequency = Constant.Value.THREE_HOURS;
            break;
        case Constant.Content.UPDATE_FRE_SIX_HOURS:
            update_frequency = Constant.Value.SIX_HOURS;
            break;
        case Constant.Content.UPDATE_FRE_TWELVE_HOURS:
            update_frequency = Constant.Value.TWELVE_HOURS;
            break;
        case Constant.Content.UPDATE_FRE_ONE_DAY:
            update_frequency = Constant.Value.ONE_DAY;
            break;
        }
        return update_frequency;
    }
    public static long indexToShowItemFor(int index){
        long showItemfor = 0;
        switch(index){
        case Constant.Content.SHOW_ITEMS_ONE_DAY:
            showItemfor = Constant.Value.ONE_DAY;
            break;
        case Constant.Content.SHOW_ITEMS_TWO_DAYS:
            showItemfor = Constant.Value.TWO_DAYS;
            break;
        case Constant.Content.SHOW_ITEMS_THREE_DAYS:
            showItemfor = Constant.Value.THREE_DAYS;
            break;
        case Constant.Content.SHOW_ITEMS_ONE_WEEK:
            showItemfor = Constant.Value.ONE_WEEK;
            break;
        case Constant.Content.SHOW_ITEMS_ONE_MONTH:
            showItemfor = Constant.Value.ONE_MONTH;
            break;
        }
        return showItemfor;
    }
}
