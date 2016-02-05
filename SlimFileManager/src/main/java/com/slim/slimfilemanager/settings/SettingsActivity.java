package com.slim.slimfilemanager.settings;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.gmillz.settingscards.ListSetting;
import com.gmillz.settingscards.SettingBase;
import com.gmillz.settingscards.SettingsCategory;
import com.gmillz.settingscards.SettingsContainer;
import com.gmillz.settingscards.SwitchSetting;
import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.ThemeActivity;

import trikita.log.Log;

public class SettingsActivity extends ThemeActivity implements SettingBase.OnSettingChanged {

    SettingsContainer mSettings;

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
                        .setEntries(R.array.sort_mode_entries)
                        .setValues(R.array.sort_mode_values));

        category = new SettingsCategory(R.string.text_editor);
        mSettings.addSetting(category,
                new SwitchSetting(R.string.use_monospace, 0, SettingsProvider.USE_MONOSPACE));

        mSettings.recreate();
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
}
