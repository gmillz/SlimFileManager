package com.slim.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingsContainer extends ScrollView {

    private LinearLayout mContainer;

    public SettingsContainer(Context context) {
        this(context, null);
    }

    public SettingsContainer(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContainer = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 0);
        //mContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          //      ViewGroup.LayoutParams.WRAP_CONTENT));
        mContainer.setOrientation(LinearLayout.VERTICAL);
        mContainer.setPadding(0, 0, 0, 0);
        addView(mContainer, params);
    }

    @SuppressWarnings("unchecked")
    public void setSettings(SettingsArray set) {
        HashMap<SettingHolder, ArrayList<SettingHolder>> settings = set.getSettings();
        for (SettingHolder category : settings.keySet()) {
            SettingsCategory category1 = new SettingsCategory(getContext());
            category1.setTitle(getContext().getString(category.title));
            for (SettingHolder setting : settings.get(category)) {
                BaseSetting setting1;
                try {
                    Class c = Class.forName(setting.className);
                    setting1 =
                            (BaseSetting) c.getConstructor(Context.class)
                                    .newInstance(getContext());
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                setting1.setHolder(setting);
                setting1.setTitle(setting.title);
                setting1.setSummary(setting.summary);
                setting1.setKey(setting.key);
                if (setting.onSettingClicked != null) {
                    setting1.setOnSettingClicked(setting.onSettingClicked);
                }
                if (setting.onSettingChanged != null) {
                    setting1.setOnSettingChanged(setting.onSettingChanged);
                }
                category1.addView(setting1);
            }
            mContainer.addView(category1);
            addDivider();
        }
    }

    private void addDivider() {
        View v = new View(getContext());
        v.setLayoutParams(new ViewGroup.LayoutParams(0, 10));
        //mContainer.addView(v);
    }
}
