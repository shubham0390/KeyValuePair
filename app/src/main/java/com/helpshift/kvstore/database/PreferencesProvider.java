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

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;


public class PreferencesProvider extends ContentProvider {

    public static final String DATABASE_NAME = "app_settings.db";

    public static final int DATABASE_VERSION = 1;

    private SQLiteDatabase mReadDatabase;

    private SQLiteDatabase mWriteDatabase;

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return getWritableDatabase().delete(getTable(uri), selection, selectionArgs);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.dir/vnd.provider.preference";
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Uri resultUri;
        long longId = getWritableDatabase().insert(getTable(uri), "foo", values);
        resultUri = ContentUris.withAppendedId(uri, longId);
        return resultUri;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return getReadableDatabase().query(getTable(uri), projection, selection, selectionArgs, sortOrder, null, null);

    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return getWritableDatabase().update(getTable(uri), values, selection, selectionArgs);

    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        return super.applyBatch(operations);
    }

    /**
     * return the readable database instance.
     *
     * @return {@link SQLiteDatabase}
     */
    private SQLiteDatabase getReadableDatabase() {
        if (mReadDatabase == null) {
            SQLiteDatabaseHelper dbHelper = new SQLiteDatabaseHelper(getContext());
            mReadDatabase = dbHelper.getReadableDatabase();
        }
        return mReadDatabase;
    }

    /**
     * return the writable database instance.
     *
     * @return {@link SQLiteDatabase}
     */

    private SQLiteDatabase getWritableDatabase() {

        if (mWriteDatabase == null) {
            SQLiteDatabaseHelper dbHelper = new SQLiteDatabaseHelper(getContext());
            mWriteDatabase = dbHelper.getWritableDatabase();
        }
        return mWriteDatabase;
    }

    @Override
    public void shutdown() {
        if (mWriteDatabase != null) {
            mWriteDatabase.close();
        }
        if (mReadDatabase != null) {
            mReadDatabase.close();
        }
    }

    private String getTable(Uri uri) {
        return uri.getQueryParameter(PreferencesContent.KEY_PREFERENCE_NAME);
    }
}
