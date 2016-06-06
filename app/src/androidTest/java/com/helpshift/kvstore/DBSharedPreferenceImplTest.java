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

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;

import java.util.Map;
import java.util.Set;


public class DBSharedPreferenceImplTest extends AndroidTestCase {


    private SharedPreferences sharedPreferences;


    public void testGetString() {
        setupPreference();
        sharedPreferences.edit().putString("key1", "value1").apply();
        String value = sharedPreferences.getString("key1", "");
        assertEquals("value1", value);
    }

    public void testSaveValue() {
        setupPreference();
        sharedPreferences.edit().putString("key1", "value1").apply();
        String value = sharedPreferences.getString("key1", "");
        assertEquals("value1", value);
    }

    public void testUpdate() {
        setupPreference();
        sharedPreferences.edit().putString("key1", "value1").apply();
        String value = sharedPreferences.getString("key1", "");
        assertEquals("value1", value);
        sharedPreferences.edit().putString("key1", "value2").apply();
        String value2 = sharedPreferences.getString("key1", "");
        assertEquals("value2", value2);
    }

    private void setupPreference() {
        MockContentResolver mockContentResolver = new MockContentResolver();
        ContextWithMockContentResolver mContext = new ContextWithMockContentResolver(mockContentResolver, getContext());
        sharedPreferences = SharedPreferencesContext.getInstance(getContext()).getSharedPreference(mContext);
    }


    public void testWithMultipleThread() {
        setupPreference();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                for (int i = 0; i < 10; i++) {
                    editor.putString("key" + i, "value" + i);
                }
                editor.apply();
                Map<String, String> map = (Map<String, String>) sharedPreferences.getAll();
                Set<Map.Entry<String, String>> stringSet = map.entrySet();
                for (Map.Entry<String, String> stringStringEntry : stringSet) {
                    Log.d("value", stringStringEntry.getValue());
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                for (int i = 11; i < 20; i++) {
                    editor.putString("key" + i, "value" + i);
                }
                editor.apply();
                Map<String, String> map = (Map<String, String>) sharedPreferences.getAll();
                Set<Map.Entry<String, String>> stringSet = map.entrySet();
                for (Map.Entry<String, String> stringStringEntry : stringSet) {
                    Log.d("value", stringStringEntry.getValue());
                }
            }
        }).start();
    }

    public class ContextWithMockContentResolver extends IsolatedContext {

        public ContextWithMockContentResolver(ContentResolver resolver, Context targetContext) {
            super(resolver, targetContext);
        }


        @Override
        public Context getApplicationContext() {
            return this;
        }
    }


}
