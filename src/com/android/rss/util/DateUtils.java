package com.android.rss.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final String TAG = "DateUtils";
    private static final int dateFormat_default = 2;
    private static final SimpleDateFormat[] sSimpleDateFormats;
    public static String[] sDateFormats = null;
    static {
        
        sDateFormats = new String[19];
        sDateFormats[0] = "EEE, dd MMM yyyy HH:mm:ss z";
        sDateFormats[1] = "EEE, dd MMM yyyy HH:mm zzzz";
        sDateFormats[2] = "dd MMM yy HH:mm:ss z";
        sDateFormats[3] = "dd MMM yy HH:mm z";
        sDateFormats[4] = "yyyy-MM-dd'T'HH:mm:ssZ";
        sDateFormats[5] = "yyyy-MM-dd'T'HH:mm:ss.SSSzzzz";
        sDateFormats[6] = "yyyy-MM-dd'T'HH:mm:sszzzz";
        sDateFormats[7] = "yyyy-MM-dd'T'HH:mm:ss z";
        sDateFormats[8] = "yyyy-MM-dd'T'HH:mm:ssz";
        sDateFormats[9] = "yyyy-MM-dd'T'HH:mm:ss";
        sDateFormats[10] = "yyyy-MM-dd'T'HHmmss.SSSz";
        sDateFormats[11] = "yyyy-MM'T'HH:mmz";
        sDateFormats[12] = "yyyy'T'HH:mmz";
        sDateFormats[13] = "yyyy-MM-dd HH:mm";
        sDateFormats[14] = "yyyy-MM-dd HH:mmZ";
        sDateFormats[15] = "yyyy-MM-dd HH:mm:ss.SSSZ";
        sDateFormats[16] = "yyyy-MM-dd";
        sDateFormats[17] = "yyyy-MM";
        sDateFormats[18] = "yyyy";

        sSimpleDateFormats = new SimpleDateFormat[sDateFormats.length];
        SimpleDateFormat[] sdfs = sSimpleDateFormats;
        for (int i = 0; i < sDateFormats.length; i++) {
            String str = sDateFormats[i];
            Locale localLocale = Locale.ENGLISH;
            SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(str,
                    localLocale);
            sdfs[i] = localSimpleDateFormat;
        }
        
    }

    public static String formatDate(Date paramDate) {
        return sSimpleDateFormats[dateFormat_default].format(paramDate);
    }

    public static Date getDate(String paramString){
        Date localDate = null;
        paramString = paramString.trim();
        int len = sSimpleDateFormats.length;
        int i = 0;
        while(i < len){
            try {
                localDate = sSimpleDateFormats[i].parse(paramString);
                return localDate;
            } catch (java.text.ParseException e) {
                
            }
            i++;
        }
        Log.d(TAG, "use current date !");
        return new Date();
    }
}
