package com.slim.settings;

import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;

public class ListHolder extends SettingHolder {

    public boolean setSummaryToValue;

    @ArrayRes int entries;
    @ArrayRes int values;

    public ListHolder(@StringRes int title, @StringRes int summary, String key) {
        super(title, summary, key);

        className = ListSetting.class.getName();
    }

    public ListHolder setSummaryToValue(boolean b) {
        this.setSummaryToValue = b;
        return this;
    }

    public ListHolder setEntries(@ArrayRes int id) {
        entries = id;
        return this;
    }

    public ListHolder setValues(@ArrayRes int id) {
        values = id;
        return this;
    }
}
