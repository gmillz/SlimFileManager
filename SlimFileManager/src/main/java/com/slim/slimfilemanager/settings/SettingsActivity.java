package com.slim.slimfilemanager.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.afollestad.materialdialogs.color.CircleView;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.gmillz.settingscards.BasicSetting;
import com.gmillz.settingscards.ListSetting;
import com.gmillz.settingscards.SettingBase;
import com.gmillz.settingscards.SettingsCategory;
import com.gmillz.settingscards.SettingsContainer;
import com.gmillz.settingscards.SettingsTheme;
import com.gmillz.settingscards.SwitchSetting;
import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.ThemeActivity;
import com.slim.util.Constant;

import trikita.log.Log;

public class SettingsActivity extends ThemeActivity implements
        SettingBase.OnSettingChanged, ColorChooserDialog.ColorCallback {

    SettingsContainer mSettings;

    private static String mKey = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.action_settings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mSettings = (SettingsContainer) findViewById(R.id.settings_container);
        mSettings.setTheme(new MySettingsTheme(this));
        populateSettings();
    }

    public void populateSettings() {
        SettingsCategory category = new SettingsCategory(R.string.file_manager);
        mSettings.addSetting(category,
                new SwitchSetting(R.string.enable_root_title, 0, SettingsProvider.KEY_ENABLE_ROOT));
        mSettings.addSetting(category,
                new SwitchSetting(R.string.use_small_page_indicator,
                        R.string.small_page_indicator_summary, SettingsProvider.SMALL_INDICATOR));

        mSettings.addSetting(category,
                new ListSetting(R.string.sort_mode_title, 0, SettingsProvider.SORT_MODE)
                        .setSummaryToValue(true)
                        .setEntries(getResources().getStringArray(R.array.sort_mode_entries))
                        .setValues(getResources().getStringArray(R.array.sort_mode_values)));

        category = new SettingsCategory(R.string.text_editor);
        mSettings.addSetting(category,
                new SwitchSetting(R.string.use_monospace, 0, SettingsProvider.USE_MONOSPACE));
        mSettings.addSetting(category,
                new ListSetting(R.string.encoding, 0, SettingsProvider.EDITOR_ENCODING)
                        .setSummaryToValue(true)
                        .setEntries(Constant.ENCODINGS)
                        .setValues(Constant.ENCODINGS)
                        .setDefault(Constant.DEFAULT_ENCODING));
        mSettings.addSetting(category,
                new SwitchSetting(R.string.wrap_content, 0, SettingsProvider.EDITOR_WRAP_CONTENT)
                        .setDefault(true));
        mSettings.addSetting(category,
                new SwitchSetting(R.string.keyboard_suggestions_and_swipe, 0,
                        SettingsProvider.SUGGESTION_ACTIVE)
                        .setDefault(false));
        mSettings.addSetting(category,
                new SwitchSetting(R.string.split_text_if_too_long, 0, SettingsProvider.SPLIT_TEXT)
                        .setDefault(true));
        category = new SettingsCategory(R.string.theme_options);

        String[] entries = new String[] {
                getString(R.string.light),
                getString(R.string.dark)
        };
        String[] values = new String[] {
                Integer.toString(R.style.AppTheme),
                Integer.toString(R.style.AppTheme_Dark)
        };

        mSettings.addSetting(category,
                new ListSetting(R.string.theme, 0, SettingsProvider.THEME)
                        .setEntries(entries)
                        .setValues(values)
                        .setDefault(String.valueOf(R.style.AppTheme)));

        addColorSettings(category);

        mSettings.recreate();
    }

    private void addColorSettings(SettingsCategory category) {
        BasicSetting accentColorSetting = new BasicSetting(R.string.accent_color, 0,
                ThemeActivity.KEY_ACCENT_COLOR);
        accentColorSetting.setSummary(String.valueOf(ThemeActivity.getAccentColor()));

        CircleView circleView = new CircleView(this);
        circleView.setBackgroundColor(ThemeActivity.getAccentColor());
        accentColorSetting.addExtraView(circleView);

        accentColorSetting.setOnSettingClicked(new SettingBase.OnSettingClicked() {
            @Override
            public void onSettingClicked(SettingBase setting, String key) {
                mKey = key;
                new ColorChooserDialog.Builder(SettingsActivity.this, R.string.accent_color)
                        .accentMode(true)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(true)
                        .preselect(ThemeActivity.getAccentColor())
                        .show();
            }
        });

        mSettings.addSetting(category, accentColorSetting);
    }

    @Override
    public void onSettingChanged(SettingBase setting, Object newValue) {
        Log.d(setting.getKey() + " : " + newValue);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        if (mKey.equals(ThemeActivity.KEY_ACCENT_COLOR)) {
            SettingsProvider.putInt(this, mKey, selectedColor);
            //mAccentColor.setBackgroundColor(selectedColor);
            recreate();
        }
        mKey = "";
    }

    public static class MySettingsTheme extends SettingsTheme {

        public MySettingsTheme(Context context) {
            super(context);
            colorAccent = ThemeActivity.getAccentColor();
        }
    }
}
