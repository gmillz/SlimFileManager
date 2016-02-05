package com.slim.slimfilemanager.settings;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.slim.settings.BaseSetting;
import com.slim.settings.CategoryHolder;
import com.slim.settings.ListHolder;
import com.slim.settings.SettingsArray;
import com.slim.settings.SettingsContainer;
import com.slim.settings.SwitchHolder;
import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.ThemeActivity;

import trikita.log.Log;

public class SettingsActivity extends ThemeActivity implements BaseSetting.OnSettingChanged {

    private SettingsArray mSettings = new SettingsArray();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        populateSettings();

        setContentView(R.layout.settings_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.action_settings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SettingsContainer container = (SettingsContainer) findViewById(R.id.settings_container);
        container.setSettings(mSettings);
    }

    public void populateSettings() {
        CategoryHolder categoryHolder = new CategoryHolder(R.string.file_manager);
        mSettings.addSetting(categoryHolder,
                new SwitchHolder(R.string.enable_root_title, 0, SettingsProvider.KEY_ENABLE_ROOT));
        mSettings.addSetting(categoryHolder,
                new SwitchHolder(R.string.use_small_page_indicator,
                        R.string.small_page_indicator_summary, SettingsProvider.SMALL_INDICATOR));

        mSettings.addSetting(categoryHolder,
                new ListHolder(R.string.sort_mode_title, 0, SettingsProvider.SORT_MODE)
                        .setSummaryToValue(true)
                        .setEntries(R.array.sort_mode_entries)
                        .setValues(R.array.sort_mode_values));

        categoryHolder = new CategoryHolder(R.string.text_editor);
        mSettings.addSetting(categoryHolder,
                new SwitchHolder(R.string.use_monospace, 0, SettingsProvider.USE_MONOSPACE));
    }

    @Override
    public void onSettingChanged(BaseSetting setting, Object newValue) {
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
