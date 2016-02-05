package com.slim.settings;

import android.support.annotation.StringRes;

public class SwitchHolder extends SettingHolder {

    public SwitchHolder(@StringRes int title, @StringRes int summary, String key) {
        super(title, summary, key);

        className = SwitchSetting.class.getName();
    }
}
