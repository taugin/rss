package com.android.rss.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.android.rss.R;

public class RSSSettings extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_settings);
    }

}
