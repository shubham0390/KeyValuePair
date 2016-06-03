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

import android.content.Context;
import android.content.SharedPreferences;

import com.helpshift.kvstore.database.PreferencesContent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SharedPreferencesContext {

    private static final SharedPreferencesContext mPreferencesContext = new SharedPreferencesContext();

    private Map<String, SharedPreferences> mSharedPreferences = new ConcurrentHashMap<>();

    /**
     * Return singleton instance of {@link SharedPreferencesContext}.
     *
     * @return {@link SharedPreferencesContext}.
     */
    public static SharedPreferencesContext getInstance() {
        return mPreferencesContext;
    }

    /**
     * Retrieve and hold the contents of the preferences  'preferenceName', returning
     * a SharedPreferences through which you can retrieve and modify its
     * values.  Only one instance of the SharedPreferences object is returned
     * to any callers for the same name, meaning they will see each other's
     * edits as soon as they are made.
     *
     * @param preferenceName Desired preferences . If a preferences  by this name
     *                       does not exist, it will be created when you retrieve an
     *                       editor (SharedPreferences.edit()) and then commit changes (Editor.commit()).
     * @param context        - application context {@link Context}
     * @return The single {@link SharedPreferences} instance that can be used
     * to retrieve and modify the preference values.
     */
    public synchronized SharedPreferences getSharedPreference(Context context, String preferenceName) {
        if (mSharedPreferences.containsKey(preferenceName)) {
            return mSharedPreferences.get(preferenceName);
        } else {
            return getPreference(context, preferenceName);
        }
    }

    public synchronized SharedPreferences getSharedPreference(Context context) {
        if (mSharedPreferences.containsKey(PreferencesContent.TABLE_NAME)) {
            return mSharedPreferences.get(PreferencesContent.TABLE_NAME);
        } else {
            return getPreference(context, PreferencesContent.TABLE_NAME);
        }
    }

    private SharedPreferences getPreference(Context context, String preferenceName) {
        SharedPreferences sharedPreferences = null;
        synchronized (this) {
            PreferencesContent.setProviderAuthority(context.getPackageName());
            sharedPreferences = new SharedPreferencesImpl(context, preferenceName);
            mSharedPreferences.put(preferenceName, sharedPreferences);
        }
        return sharedPreferences;
    }
}
