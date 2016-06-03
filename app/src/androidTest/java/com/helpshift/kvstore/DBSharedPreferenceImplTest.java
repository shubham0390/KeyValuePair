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
import android.test.ProviderTestCase2;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContentResolver;
import android.util.Log;

import java.util.Map;
import java.util.Set;


public class DBSharedPreferenceImplTest extends ProviderTestCase2<MockSharedPreferenceProvider> {


    private SharedPreferences sharedPreferences;
    private MockSharedPreferenceProvider mProvider;

    /**
     * Constructor.
     */
    public DBSharedPreferenceImplTest() {
        super(MockSharedPreferenceProvider.class, "com.helpshift.kvstore.PreferencesProvider");
    }

    @Override
    protected void setUp() throws Exception {

    }

    @Override
    public void tearDown() throws Exception {
    }

    public void testGetString() {
        setupProvider();
        mProvider.addQueryResult("key1", "value1");
        String value = sharedPreferences.getString("key1", "");
        assertEquals("value1", value);
    }

    public void testSaveValue() {
        setupProvider();
        sharedPreferences.edit().putString("key1", "value1").apply();
        String value = sharedPreferences.getString("key1", "");
        assertEquals("value1", value);
    }

    public void testUpdate() {
        setupProvider();
        sharedPreferences.edit().putString("key1", "value1").apply();
        String value = sharedPreferences.getString("key1", "");
        assertEquals("value1", value);
        sharedPreferences.edit().putString("key1", "value2").apply();
        String value2 = sharedPreferences.getString("key1", "");
        assertEquals("value2", value2);
    }

    private void setupProvider() {
        mProvider = new MockSharedPreferenceProvider();
        MockContentResolver mockContentResolver = new MockContentResolver();
        mockContentResolver.addProvider("com.helpshift.kvstore.PreferencesProvider", mProvider);
        ContextWithMockContentResolver mContext = new ContextWithMockContentResolver(getContext());
        mContext.setContentResolver(mockContentResolver);
        sharedPreferences = SharedPreferencesContext.getInstance().getSharedPreference(mContext);
    }


    public void testWithMultipleThread() {
        setupProvider();
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

    public class ContextWithMockContentResolver extends RenamingDelegatingContext {
        private ContentResolver contentResolver;

        public void setContentResolver(ContentResolver contentResolver) {
            this.contentResolver = contentResolver;
        }

        public ContextWithMockContentResolver(Context targetContext) {
            super(targetContext, "test");
        }

        @Override
        public ContentResolver getContentResolver() {
            return contentResolver;
        }

        @Override
        public Context getApplicationContext() {
            return this;
        }
    }


}
