package com.android.rss.settings;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.android.rss.R;
import com.android.rss.common.PreferenceKeys;
import com.android.rss.common.Constant;
import com.android.rss.util.AlarmUtil;

public class GeneralSettingsFrament extends PreferenceFragment implements OnPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.general_settings);
        ListPreference preference = null;
        String value = null;
        int index = -1;
        String summary = null;
        
        preference = (ListPreference)findPreference(PreferenceKeys.KEY_WIDGET_LAYOUT);
        value = preference.getValue();
        index = preference.findIndexOfValue(value);
        summary = preference.getEntries()[index].toString();
        preference.setSummary(summary);
        preference.setOnPreferenceChangeListener(this);
        
        preference = (ListPreference)findPreference(PreferenceKeys.KEY_VALIDADY_TIME);
        value = preference.getValue();
        index = preference.findIndexOfValue(value);
        summary = preference.getEntries()[index].toString();
        preference.setSummary(summary);
        preference.setOnPreferenceChangeListener(this);
        
        preference = (ListPreference)findPreference(PreferenceKeys.KEY_UPDATE_FREQUENCY);
        preference.setOnPreferenceChangeListener(this);
        
    }

    
    @Override
    public void onResume() {
        super.onResume();
        ListPreference preference = (ListPreference)findPreference(PreferenceKeys.KEY_UPDATE_FREQUENCY);
        updateFrequencySummary(preference);
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(PreferenceKeys.KEY_WIDGET_LAYOUT.equals(preference.getKey())){
            ListPreference listPreference = (ListPreference)preference;
            String value = listPreference.getValue();
            if(value.equals(newValue)){
                return false;
            }
            int index = listPreference.findIndexOfValue(newValue.toString());
            String summary = listPreference.getEntries()[index].toString();
            preference.setSummary(summary);
            Intent intent = new Intent(Constant.Intent.INTENT_RSSAPP_WIDGET_UPDATE);
            getActivity().sendBroadcast(intent);
            return true;
        }else if(PreferenceKeys.KEY_VALIDADY_TIME.equals(preference.getKey())){
            ListPreference listPreference = (ListPreference)preference;
            String value = listPreference.getValue();
            if(value.equals(newValue)){
                return false;
            }
            int index = listPreference.findIndexOfValue(newValue.toString());
            String summary = listPreference.getEntries()[index].toString();
            preference.setSummary(summary);
            return true;
        }else if(PreferenceKeys.KEY_UPDATE_FREQUENCY.equals(preference.getKey())){
            ListPreference listPreference = (ListPreference)preference;
            String value = listPreference.getValue();
            if(value.equals(newValue)){
                return false;
            }
            listPreference.setValue(newValue.toString());
            long time = System.currentTimeMillis();
            AlarmUtil.setAlarm(getActivity(), time);
            AlarmUtil.setNextUpdateTime(getActivity(), time);
            updateFrequencySummary(preference);
            return true;
        }
        return false;
    }

    private void sendBroadcast(Intent intent) {
    }
    
    private void updateFrequencySummary(Preference preference){
        ListPreference listPreference = (ListPreference)preference;
        String value = null;
        int index = -1;
        String summary = null;
        value = listPreference.getValue();
        index = listPreference.findIndexOfValue(value);
        summary = listPreference.getEntries()[index].toString();
        long now = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong(Constant.Key.KEY_NEXT_TIME, 0);
        if(now != 0 && !value.equals("0")){
            String timeString = null;
            String date = DateFormat.getDateFormat(getActivity()).format(new Date(now));
            String time = DateFormat.getTimeFormat(getActivity()).format(new Date(now));
            timeString = date + " " + time;
            String nextTime = getActivity().getResources().getString(R.string.next_update_time, timeString);
            summary += "\n" + nextTime;
        }
        listPreference.setSummary(summary);
    }
}
