package com.helpshift.kvstore.database;

import android.provider.BaseColumns;

public interface PreferencesColumns extends BaseColumns {
    String COLUMN_NAME_KEY = "preferenceKey";
    String COLUMN_NAME_VALUE = "preferenceValue";
}