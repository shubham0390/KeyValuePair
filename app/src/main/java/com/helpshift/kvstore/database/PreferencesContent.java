/*
 * Copyright (c) 2016. Subham Tyagi
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.helpshift.kvstore.database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;


public class PreferencesContent implements PreferencesColumns {

    public static final String TABLE_NAME = "sharedPreferences";

    public static final String[] PREFERENCES_PROJECTION = {COLUMN_NAME_KEY, COLUMN_NAME_VALUE};

    public static final String SELECTION_VAI_KEY = COLUMN_NAME_KEY + "=?";

    public static final String KEY_PREFERENCE_NAME = "preferenceName";

    public static String mProviderAuthority;

    public static Uri BASE_CONTENT_URI;

    public static String createTableQuery() {
        return "CREATE TABLE IF NOT EXISTS" + TABLE_NAME
                + "("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                + COLUMN_NAME_KEY + " TEXT NOT NULL,"
                + COLUMN_NAME_VALUE + " TEXT NOT NULL );";
    }

    public static void createTableQuery(Context context, String tableName) {
        String s = "CREATE TABLE IF NOT EXISTS " + tableName
                + "("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                + COLUMN_NAME_KEY + " TEXT NOT NULL,"
                + COLUMN_NAME_VALUE + " TEXT NOT NULL );";
        SQLiteDatabaseHelper sqLiteDatabaseHelper = null;
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabaseHelper = new SQLiteDatabaseHelper(context);
            sqLiteDatabase = sqLiteDatabaseHelper.getWritableDatabase();
            sqLiteDatabase.execSQL(s);
        } finally {
            if (sqLiteDatabase != null)
                sqLiteDatabase.close();
            if (sqLiteDatabaseHelper != null)
                sqLiteDatabaseHelper.close();
        }
    }

    public static void setProviderAuthority(String packageName) {
        mProviderAuthority = packageName + "." + PreferencesProvider.class.getSimpleName();
        BASE_CONTENT_URI = Uri.EMPTY.buildUpon().scheme(ContentResolver.SCHEME_CONTENT).authority(mProviderAuthority)
                .build();
    }
}
