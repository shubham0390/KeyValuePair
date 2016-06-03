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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PreferenceAdapter extends ArrayAdapter {
    private List<String> keyList = new ArrayList<>();
    private Map<String, String> map;

    public PreferenceAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void setData(Map<String, String> data) {
        map = data;
        keyList = new ArrayList<>(data.keySet());
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PreferenceViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_item, parent, false);
            viewHolder = new PreferenceViewHolder(convertView);
        } else {
            viewHolder = (PreferenceViewHolder) convertView.getTag();
        }
        convertView.setTag(viewHolder);
        String key = keyList.get(position);
        String value = map.get(key);
        viewHolder.bindData(key, value);

        return convertView;
    }

    @Override
    public int getCount() {
        return keyList.size();
    }

    class PreferenceViewHolder {
        TextView mKeyTextView;
        TextView mValueTextView;

        public PreferenceViewHolder(View convertView) {
            mKeyTextView = (TextView) convertView.findViewById(R.id.keyTextView);
            mValueTextView = (TextView) convertView.findViewById(R.id.valueTextView);
        }

        public void bindData(String key, String value) {
            mKeyTextView.setText(getContext().getString(R.string.string_key, key));
            mValueTextView.setText(getContext().getString(R.string.string_value, value));
        }
    }
}