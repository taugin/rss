package com.android.rss.util;

public class Log {
    private static final boolean DEBUG = false;
    private static final boolean ERROR = false;
    public static final void d(String tag, String msg){
        if(DEBUG){
            android.util.Log.d(tag, msg);
        }
    }

    public static final void e(String tag, String msg){
        if(ERROR){
            android.util.Log.e(tag, msg);
        }
    }
    public static final void v(String tag, String msg){
        if(ERROR){
            android.util.Log.v(tag, msg);
        }
    }
}
