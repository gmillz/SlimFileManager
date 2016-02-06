package com.gmillz.settingscards;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class SettingsHandler {

    public static SharedPreferences get(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences.Editor put(Context context) {
        return get(context).edit();
    }

    protected static void put(Context context, String key, Object o) {
        if (o instanceof Boolean) {
            put(context).putBoolean(key, (Boolean) o).apply();
        } else if (o instanceof String) {
            put(context).putString(key, (String) o).apply();
        } else if (o instanceof Integer) {
            put(context).putInt(key, (Integer) o).apply();
        }
    }

    public static Boolean getBoolean(Context context, String key, Boolean defValue) {
        return get(context).getBoolean(key, defValue);
    }

    public static String getString(Context context, String key, String defValue) {
        try {
            return get(context).getString(key, defValue);
        } catch (ClassCastException e) {
            return String.valueOf(get(context).getInt(key, Integer.valueOf(defValue)));
        }
    }
}
