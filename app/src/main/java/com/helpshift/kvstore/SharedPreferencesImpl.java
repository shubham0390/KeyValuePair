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

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.helpshift.kvstore.database.PreferencesColumns;
import com.helpshift.kvstore.database.PreferencesContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class SharedPreferencesImpl implements SharedPreferences {

    private static final String SEPARATOR = ":-:";

    private final Map<String, String> mMap = new ConcurrentHashMap<>();

    private final Map<String, String> mModifiedMap = new ConcurrentHashMap<>();

    private final Set<String> mModifiedKeys = Collections.synchronizedSet(new HashSet<String>());


    private static final Object mContent = new Object();

    private Context mContext;

    private final Uri mContentUri;

    private final WeakHashMap<OnSharedPreferenceChangeListener, Object> mListeners = new WeakHashMap<>();

    public SharedPreferencesImpl(Context context, String preferenceName) {
        mContext = context;
        mContentUri = PreferencesContent.BASE_CONTENT_URI.buildUpon().appendPath("preference")
                .appendQueryParameter(PreferencesContent.KEY_PREFERENCE_NAME, preferenceName)
                .build();
        PreferencesContent.createTableQuery(mContext, preferenceName);
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (this) {
            mListeners.put(listener, mContent);
        }
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (this) {
            mListeners.remove(listener);
        }
    }


    @Override
    public Map<String, ?> getAll() {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(mContentUri, PreferencesContent.PREFERENCES_PROJECTION,
                    null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    parseSettingCursor(cursor);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mMap;
    }

    public String getString(String key, String defValue) {
        String value = getValueByKey(key);
        if (value == null) {
            return defValue;
        }
        return value;
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        String value = getValueByKey(key);
        Set<String> stringSet = new HashSet<>();
        if (value == null) {
            return defValues;
        }
        stringSet.addAll(Arrays.asList(value.split(SEPARATOR)));
        return stringSet;
    }


    public boolean getBoolean(String key, boolean defValue) {
        String value = getValueByKey(key);
        if (value == null) {
            return defValue;
        } else {
            try {
                return Boolean.parseBoolean(getValueByKey(key));
            } catch (NumberFormatException e) {
                throw new ClassCastException(value + "can not cast to Boolean");
            }
        }
    }

    @Override
    public boolean contains(String key) {
        return false;
    }

    @Override
    public Editor edit() {
        return new EditorImpl();
    }


    public int getInt(String key, int defValue) {
        String value = getValueByKey(key);
        if (value == null) {
            return defValue;
        } else {
            try {
                return Integer.parseInt(getValueByKey(key));
            } catch (NumberFormatException e) {
                throw new ClassCastException("can not cast to integer");
            }
        }
    }

    @Override
    public long getLong(String key, long defValue) {
        String value = getValueByKey(key);
        if (value == null) {
            return defValue;
        } else {
            try {
                return Long.parseLong(getValueByKey(key));
            } catch (NumberFormatException e) {
                throw new ClassCastException("can not cast to Long");
            }
        }
    }

    @Override
    public float getFloat(String key, float defValue) {
        String value = getValueByKey(key);
        if (value == null) {
            return defValue;
        } else {
            try {
                return Float.parseFloat(getValueByKey(key));
            } catch (NumberFormatException e) {
                throw new ClassCastException("can not cast to Float");
            }
        }
    }


    private String getValueByKey(String key) {
        if (mMap.containsKey(key)) {
            return mMap.get(key);
        } else {
            return getValueFromDB(key);
        }
    }

    private boolean checkIfKeyAlreadyExists(String key) {
        Cursor cursor = mContext.getContentResolver().query(mContentUri,
                PreferencesContent.PREFERENCES_PROJECTION,
                PreferencesContent.SELECTION_VAI_KEY, new String[]{key}, null);
        try {
            if (cursor != null && cursor.moveToNext()) {
                return true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public String getValueFromDB(String key) {
        updateMapWithValueByKey(key);
        return mMap.get(key);
    }

    private void updateMapWithValueByKey(String key) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(mContentUri, PreferencesContent.PREFERENCES_PROJECTION,
                    PreferencesContent.SELECTION_VAI_KEY, new String[]{key}, null);
            if (cursor != null && cursor.moveToNext()) {
                parseSettingCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    private void parseSettingCursor(Cursor cursor) {
        mMap.put(cursor.getString(0), cursor.getString(1));
    }

    public class EditorImpl implements Editor {

        public Editor putBoolean(String key, boolean value) {
            saveSetting(key, String.valueOf(value));
            return this;
        }

        @Override
        public Editor remove(String key) {
            mModifiedMap.put(key, null);
            return this;
        }

        @Override
        public Editor clear() {
            mMap.clear();
            mModifiedMap.clear();
            return this;
        }

        public Editor putInt(String key, int value) {
            saveSetting(key, String.valueOf(value));
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            saveSetting(key, String.valueOf(value));
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            saveSetting(key, String.valueOf(value));
            return this;
        }


        public Editor putString(String key, String value) {
            saveSetting(key, value);
            return this;
        }

        @Override
        public Editor putStringSet(String key, Set<String> values) {
            StringBuilder builder = new StringBuilder();
            for (String s : values) {
                builder.append(s);
                builder.append(SEPARATOR);
            }
            saveSetting(key, String.valueOf(builder.toString()));
            return this;
        }

        private void saveSetting(String key, String value) {
            mModifiedMap.put(key, value);
            mMap.put(key, value);
        }

        @Override
        public boolean commit() {
            throw new UnsupportedOperationException("Commit is not supported");
        }

        @Override
        public void apply() {
            CommitPreferenceAsyncTask asyncTask = new CommitPreferenceAsyncTask();
            asyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    private class CommitPreferenceAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<ContentProviderOperation> mContentProviderOperations = new ArrayList<>();
            for (Map.Entry<String, String> entry : mModifiedMap.entrySet()) {
                ContentProviderOperation.Builder builder;
                /*Add key to modified key */
                mModifiedKeys.add(entry.getKey());
                /*if map contains key mean we have to update or delete*/
                String key = entry.getKey();
                if (checkIfKeyAlreadyExists(key)) {
                    /*If value of any key is missing we are treating it as removed */
                    if (entry.getValue() == null) {
                        Log.d(getClass().getSimpleName(), "Delete operations");
                        builder = ContentProviderOperation.newDelete(mContentUri);
                        mMap.remove(entry.getKey());
                    } else {
                        Log.d(getClass().getSimpleName(), "update operations");
                        builder = ContentProviderOperation.newUpdate(mContentUri);
                        mMap.put(entry.getKey(), entry.getValue());
                    }
                    /*Adding a selection */
                    builder.withSelection(PreferencesColumns.COLUMN_NAME_KEY + " =?",
                            new String[]{entry.getKey()});
                } else {
                    Log.d(getClass().getSimpleName(), "new operations");
                    builder = ContentProviderOperation.newInsert(mContentUri);
                    mMap.put(entry.getKey(), entry.getValue());
                }

                builder.withValue(PreferencesColumns.COLUMN_NAME_KEY, entry.getKey());
                builder.withValue(PreferencesColumns.COLUMN_NAME_VALUE, entry.getValue());
                mContentProviderOperations.add(builder.build());
            }
            /*Committing all setting to database*/
            try {
                ContentProviderResult[] contentProviderResult = mContext.getContentResolver().applyBatch(PreferencesContent
                        .mProviderAuthority, mContentProviderOperations);

                handleProviderResult(contentProviderResult);
            } catch (OperationApplicationException e) {
                Log.d(SharedPreferencesImpl.class.getName(), "Commit failed " + e.getMessage());
            } catch (RemoteException e) {
                Log.d(SharedPreferencesImpl.class.getName(), "Commit failed " + e.getMessage());
            }
            return null;
        }

    }

    private void handleProviderResult(ContentProviderResult[] contentProviderResult) {
        if (contentProviderResult.length <= 0) {
            return;
        }
        int i = 0;
            /*if result does not have count and uri that means database operation has failed*/
        for (ContentProviderResult result : contentProviderResult) {
            if (result == null || result.count == null || result.uri == null) {
                mModifiedKeys.remove(i);
                i++;
            }
        }
        notifyListeners();
        mModifiedMap.clear();
    }

    private void notifyListeners() {
        if (mModifiedKeys.size() == 0) {
            return;
        }
        for (String key : mModifiedKeys) {
            for (OnSharedPreferenceChangeListener listener : mListeners.keySet()) {
                if (listener != null) {
                    listener.onSharedPreferenceChanged(SharedPreferencesImpl.this, key);
                }
            }
        }
        mModifiedKeys.clear();
    }

}
