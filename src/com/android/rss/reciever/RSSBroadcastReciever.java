package com.android.rss.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.android.rss.common.Constant;
import com.android.rss.util.Log;

public class RSSBroadcastReciever extends BroadcastReceiver {

    private static final String TAG = "RSSBroadcastReciever";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, intent.toString());
        if(intent != null){
            if(Intent.ACTION_DATE_CHANGED.equals(intent.getAction())){
                context.startService(new Intent(Constant.Intent.INTENT_RSSAPP_VALIDITY_CHECKING));
            }else if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
                Intent service = new Intent(Constant.Intent.INTENT_RSSAPP_SET_ALARM);
                context.startService(service);
                //Reset the thread state.
                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Constant.Key.KEY_THREAD_RUNNING, false).commit();
            }
        }
    }

}
