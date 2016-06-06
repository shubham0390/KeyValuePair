
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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Map<String, String>> {

    EditText mKeyEditText;
    EditText mValueEditText;
    Button mButton;
    ListView mListView;
    PreferenceAdapter mPreferenceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mKeyEditText = (EditText) findViewById(R.id.keyEditText);
        mValueEditText = (EditText) findViewById(R.id.valueEditText);
        mButton = (Button) findViewById(R.id.button);
        mListView = (ListView) findViewById(R.id.list);
        mButton.setOnClickListener(mButtonClickListener);
        mPreferenceAdapter = new PreferenceAdapter(this, R.layout.layout_list_item);
        mListView.setAdapter(mPreferenceAdapter);
        getSupportLoaderManager().restartLoader(1, null, this).forceLoad();
        SharedPreferencesContext.getInstance(getApplicationContext()).getSharedPreference(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        getSupportLoaderManager().restartLoader(1, null, MainActivity.this).forceLoad();
                    }
                });
    }


    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String key = mKeyEditText.getText().toString();
            String value = mValueEditText.getText().toString();
            if (TextUtils.isEmpty(key)) {
                mKeyEditText.setError(getString(R.string.error_message_empty_field));
                return;
            }
            if (TextUtils.isEmpty(value)) {
                mValueEditText.setError(getString(R.string.error_message_empty_field));
                return;
            }
            SharedPreferences sharedPreferences = SharedPreferencesContext.getInstance(getApplicationContext()).getSharedPreference(getApplicationContext());
            sharedPreferences.edit().putString(key, value).apply();
        }
    };

    @Override
    public Loader<Map<String, String>> onCreateLoader(int id, Bundle args) {
        return new PreferenceLoader(getApplicationContext());
    }

    @Override
    public void onLoadFinished(Loader<Map<String, String>> loader, Map<String, String> data) {
        Log.d(getClass().getSimpleName(), "Received Data");
        if (data != null)
            mPreferenceAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<Map<String, String>> loader) {
        Log.d(getClass().getSimpleName(), "Reset Loader");
    }


}
