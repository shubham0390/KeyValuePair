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
import android.support.v4.content.AsyncTaskLoader;

import java.util.Map;

public class PreferenceLoader extends AsyncTaskLoader<Map<String, String>> {

    private Map<String, String> mData;

    public PreferenceLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }else{
            forceLoad();
        }
    }

    @Override
    public Map<String, String> loadInBackground() {
        return (Map<String, String>) SharedPreferencesContext.getInstance(getContext()).getSharedPreference(getContext()).getAll();
    }

    @Override
    public void deliverResult(Map<String, String> data) {
        mData = data;
        super.deliverResult(data);
    }

    @Override
    protected void onReset() {
        super.onReset();
    }
}