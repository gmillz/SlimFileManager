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

package com.slim.slimfilemanager.settings

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils

import com.slim.slimfilemanager.widget.TabItem

import java.util.ArrayList
import java.util.Arrays

object SettingsProvider {

    // File Manager
    val KEY_ENABLE_ROOT = "enable_root"
    val THEME = "app_theme"
    val SORT_MODE = "sort_mode"
    val SMALL_INDICATOR = "small_indicator"

    // Text Editor
    val USE_MONOSPACE = "use_monospace"
    val EDITOR_WRAP_CONTENT = "editor_wrap_content"
    val SUGGESTION_ACTIVE = "suggestion_active"
    val EDITOR_ENCODING = "editor_encoding"
    val FONT_SIZE = "font_size"
    val SPLIT_TEXT = "page_system_active"

    operator fun get(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun put(context: Context): SharedPreferences.Editor {
        return get(context).edit()
    }

    fun put(context: Context, key: String, o: Any) {
        if (o is Boolean) {
            put(context).putBoolean(key, o).apply()
        } else if (o is String) {
            put(context).putString(key, o).apply()
        } else if (o is Int) {
            put(context).putInt(key, o).apply()
        }
    }

    fun getString(context: Context, key: String, defValue: String): String? {
        return get(context).getString(key, defValue)
    }

    fun putString(context: Context, key: String, value: String) {
        put(context).putString(key, value).apply()
    }

    fun getBoolean(context: Context, key: String, defValue: Boolean): Boolean {
        return get(context).getBoolean(key, defValue)
    }

    fun putBoolean(context: Context, key: String, b: Boolean) {
        put(context).putBoolean(key, b).apply()
    }

    fun getInt(context: Context, key: String, defValue: Int): Int {
        var i: Int
        try {
            i = get(context).getInt(key, defValue)
        } catch (e: Exception) {
            i = Integer.parseInt(get(context).getString(key, Integer.toString(defValue))!!)
        }

        return i
    }

    fun putInt(context: Context, key: String, value: Int) {
        put(context).putInt(key, value).commit()
    }

    fun getTabList(context: Context, key: String,
                   def: ArrayList<TabItem>): ArrayList<TabItem> {
        val items = ArrayList<TabItem>()
        val array = ArrayList(
                Arrays.asList(*TextUtils.split(get(context).getString(key, ""), "‚‗‚")))
        for (s in array) {
            items.add(TabItem.fromString(s))
        }
        if (array.isEmpty()) {
            items.addAll(def)
        }
        return items
    }

    fun putTabList(context: Context, key: String, tabs: ArrayList<TabItem>) {
        val items = ArrayList<String>()
        for (item in tabs) {
            items.add(item.toString())
        }
        put(context).putString(key, TextUtils.join("‚‗‚", items)).apply()
    }

    fun remove(context: Context, key: String) {
        put(context).remove(key).apply()
    }
}