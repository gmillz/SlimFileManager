package com.slim.settings;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

public abstract class SettingHolder {

    public String className;

    @StringRes
    public int title;

    @StringRes
    public int summary;

    public String key;

    @Nullable
    public BaseSetting.OnSettingClicked onSettingClicked;

    @Nullable
    public BaseSetting.OnSettingChanged onSettingChanged;

    public SettingHolder(@StringRes int title, @StringRes int summary, String key) {
        this.title = title;
        this.summary = summary;
        this.key = key;
    }

    public SettingHolder setOnSettingClickListener(BaseSetting.OnSettingClicked onSettingClicked) {
        this.onSettingClicked = onSettingClicked;
        return this;
    }

    public SettingHolder setOnSettingChangeListener(BaseSetting.OnSettingChanged onSettingChanged) {
        this.onSettingChanged = onSettingChanged;
        return this;
    }
}
