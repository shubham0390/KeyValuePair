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

package com.helpshift.kvstore;

import android.annotation.TargetApi;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.test.mock.MockContentProvider;
import android.util.Log;

import com.helpshift.kvstore.database.PreferencesContent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MockSharedPreferenceProvider extends MockContentProvider {

    private Set<Entry> entries = new HashSet<>();

    public MockSharedPreferenceProvider() {
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return super.delete(uri, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        return super.getType(uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String key = values.getAsString(PreferencesContent.COLUMN_NAME_KEY);
        String value = values.getAsString(PreferencesContent.COLUMN_NAME_VALUE);
        entries.add(new Entry(key, value));
        return super.insert(uri, values);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(getClass().getSimpleName(), "Starting update");
        for (Entry entry : entries) {
            if (entry.key.equals(selectionArgs[0])) {
                Log.d(getClass().getSimpleName(), "Starting update-item found");
                entry.value = values.getAsString(PreferencesContent.COLUMN_NAME_VALUE);
                return 1;
            }
        }
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        for (Entry entry : entries) {
            if (entry.key.equals(selectionArgs[0])) {
                MatrixCursor matrixCursor = new MatrixCursor(PreferencesContent.PREFERENCES_PROJECTION);
                matrixCursor.addRow(new String[]{entry.key, entry.value});
                return matrixCursor;
            }
        }
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        return super.bulkInsert(uri, values);
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) {
        ContentProviderResult[] backRefs = new ContentProviderResult[operations.size()];
        for (int i = 0; i < operations.size(); i++) {
            ContentProviderOperation operation = operations.get(i);
            try {
                operation.apply(this, backRefs, i);
            } catch (OperationApplicationException e) {
                Log.e(getClass().getSimpleName(), e.getMessage());
            }
        }
        return backRefs;
    }

    public void addQueryResult(String key, String value) {
        entries.add(new Entry(key, value));
    }


    class Entry {
        String key;
        String value;

        public Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

}