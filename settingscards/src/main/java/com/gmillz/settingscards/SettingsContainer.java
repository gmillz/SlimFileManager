package com.gmillz.settingscards;

import android.content.Context;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;

public class SettingsContainer extends ScrollView {

    private LinearLayout mContainer;
    private SettingsTheme mTheme;

    private ArrayList<SettingsCategory> mCategories = new ArrayList<>();
    private ArrayMap<SettingsCategory, ArrayList<SettingBase>> mSettings = new ArrayMap<>();

    public SettingsContainer(Context context) {
        this(context, null);
    }

    public SettingsContainer(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTheme = new SettingsTheme(context);

        mContainer = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 0);
        mContainer.setOrientation(LinearLayout.VERTICAL);
        mContainer.setPadding(0, 0, 0, 0);
        addView(mContainer, params);
    }

    public void setTheme(SettingsTheme theme) {
        mTheme = theme;
    }

    public void recreate() {
        mContainer.removeAllViews();
        setBackgroundColor(mTheme.pageBackground);
        for (SettingsCategory category : mCategories) {
            mContainer.addView(category.getView(getContext()));
            category.setTitleColor(mTheme.colorAccent);
            category.setCardBackgroundColor(mTheme.cardBackground);
            for (SettingBase setting : mSettings.get(category)) {
                category.addView(setting.getView(getContext()));
                setting.setAccentColor(mTheme.colorAccent);
                setting.setTitleColor(mTheme.primaryTextColor);
                setting.setSummaryColor(mTheme.secondaryTextColor);
            }
        }
    }

    public void addCategory(SettingsCategory category) {
        if (!mSettings.containsKey(category)) {
            mCategories.add(category);
            mSettings.put(category, new ArrayList<SettingBase>());
        }
    }

    public void addSetting(SettingsCategory category, SettingBase setting) {
        if (!mSettings.containsKey(category)) {
            addCategory(category);
        }
        mSettings.get(category).add(setting);
    }
}
