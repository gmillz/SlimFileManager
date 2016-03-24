/*
 * Copyright (C) 2015 The SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.slim.slimfilemanager.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.slim.slimfilemanager.widget.TabItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsProvider {

    // File Manager
    public static final String KEY_ENABLE_ROOT = "enable_root";
    public static final String THEME = "app_theme";
    public static final String SORT_MODE = "sort_mode";
    public static final String SMALL_INDICATOR = "small_indicator";

    // Text Editor
    public static final String USE_MONOSPACE = "use_monospace";
    public static final String EDITOR_WRAP_CONTENT = "editor_wrap_content";
    public static final String SUGGESTION_ACTIVE = "suggestion_active";
    public static final String EDITOR_ENCODING = "editor_encoding";
    public static final String FONT_SIZE = "font_size";
    public static final String SPLIT_TEXT = "page_system_active";

    public static SharedPreferences get(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences.Editor put(Context context) {
        return get(context).edit();
    }

    public static void put(Context context, String key, Object o) {
        if (o instanceof Boolean) {
            put(context).putBoolean(key, (Boolean) o).apply();
        } else if (o instanceof String) {
            put(context).putString(key, (String) o).apply();
        } else if (o instanceof Integer) {
            put(context).putInt(key, (Integer) o).apply();
        }
    }

    public static String getString(Context context, String key, String defValue) {
        return get(context).getString(key, defValue);
    }

    public static void putString(Context context, String key, String value) {
        put(context).putString(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return get(context).getBoolean(key, defValue);
    }

    public static void putBoolean(Context context, String key, boolean b) {
        put(context).putBoolean(key, b).apply();
    }

    public static int getInt(Context context, String key, int defValue) {
        int i;
        try {
            i = get(context).getInt(key, defValue);
        } catch (Exception e) {
            i = Integer.parseInt(get(context).getString(key, Integer.toString(defValue)));
        }
        return i;
    }

    public static void putInt(Context context, String key, int value) {
        put(context).putInt(key, value).commit();
    }

    public static ArrayList<TabItem> getTabList(Context context, String key,
                                                ArrayList<TabItem> def) {
        ArrayList<TabItem> items = new ArrayList<>();
        ArrayList<String> array = new ArrayList<>(
                Arrays.asList(TextUtils.split(get(context).getString(key, ""), "‚‗‚")));
        for (String s : array) {
            items.add(TabItem.fromString(s));
        }
        if (array.isEmpty()) {
            items.addAll(def);
        }
        return items;
    }

    public static void putTabList(Context context, String key, ArrayList<TabItem> tabs) {
        List<String> items = new ArrayList<>();
        for (TabItem item : tabs) {
            items.add(item.toString());
        }
        put(context).putString(key, TextUtils.join("‚‗‚", items)).apply();
    }

    public static void remove(Context context, String key) {
        put(context).remove(key).apply();
    }
}