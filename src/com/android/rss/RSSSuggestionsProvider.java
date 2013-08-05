/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.rss;

import android.app.SearchManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.android.rss.common.Constant;
import com.android.rss.util.Log;

public class RSSSuggestionsProvider extends
        android.content.SearchRecentSuggestionsProvider {

    private final boolean DEBUG = true;
    private static final String TAG = "RssSuggestionsProvider";
    final static String AUTHORITY = "com.android.rss.RssSuggestionsProvider";
    final static int MODE = DATABASE_MODE_QUERIES + DATABASE_MODE_2LINES;

    public RSSSuggestionsProvider() {
        super();
        setupSuggestions(AUTHORITY, MODE);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        projection = new String[] { Constant.Content._ID, Constant.Content.ITEM_TITLE, Constant.Content.ITEM_URL};
        Uri u = Constant.Content.ITEM_URI;
        selection = Constant.Content.ITEM_TITLE + " like " + "'"
                + selectionArgs[0] + "%'";
        Log.d(TAG, "selection = " + selection);
        MatrixCursor cursor = null;
        Cursor c = null;
        Object cursorColumns[] = null;
        try {
            c = getContext().getContentResolver().query(u, projection,
                    selection, null, null);
            if (c != null) {
                cursor = new MatrixCursor(mVirtualColumns);
                if (c.moveToFirst()) {
                    do {
                        cursorColumns = new Object[mVirtualColumns.length];
                        int _id = c
                                .getInt(c
                                        .getColumnIndexOrThrow(Constant.Content._ID));
                        String displayName = c
                                .getString(c
                                        .getColumnIndexOrThrow(Constant.Content.ITEM_TITLE));
                        String url = c.getString(c.getColumnIndexOrThrow(Constant.Content.ITEM_URL));
                        cursorColumns[ID_COLUMN] = _id;
                        cursorColumns[DISPLAY_NAME_COLUMN] = displayName;
                        cursorColumns[INTENT_DATA_COLUMN] = Uri.parse(url);
                        cursorColumns[INTENT_ACTION_COLUMN] = "android.intent.action.VIEW";
                        cursorColumns[INTENT_EXTRA_ID_COLUMN] = _id;
                        cursor.addRow(cursorColumns);
                        if (DEBUG) {
                            Log.d(TAG, "_id = " + _id + " , displayName = "
                                    + displayName + " , url = " + url);
                        }
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        return 0;
    }

    private String[] mVirtualColumns = new String[] { BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};

    private final int ID_COLUMN = 0;
    private final int DISPLAY_NAME_COLUMN = 1;
    private final int INTENT_DATA_COLUMN = 2;
    private final int INTENT_ACTION_COLUMN = 3;
    private final int INTENT_EXTRA_ID_COLUMN = 4;
}
