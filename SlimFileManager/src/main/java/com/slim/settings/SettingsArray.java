package com.slim.settings;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingsArray {

    private HashMap<SettingHolder, ArrayList<SettingHolder>> mSettings = new HashMap<>();

    public SettingsArray() {

    }

    public void addCategory(SettingHolder settingHolder) {
        if (!mSettings.containsKey(settingHolder)) {
            mSettings.put(settingHolder, new ArrayList<SettingHolder>());
        }
    }

    public void addSetting(SettingHolder category, SettingHolder setting) {
        if (!mSettings.containsKey(category)) {
            addCategory(category);
        }
        mSettings.get(category).add(setting);
    }

    public HashMap<SettingHolder, ArrayList<SettingHolder>> getSettings() {
        return mSettings;
    }
}
