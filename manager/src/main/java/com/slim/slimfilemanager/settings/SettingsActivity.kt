package com.slim.slimfilemanager.settings

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View

import com.gmillz.settingscards.ListSetting
import com.gmillz.settingscards.SettingBase
import com.gmillz.settingscards.SettingsCategory
import com.gmillz.settingscards.SettingsContainer
import com.gmillz.settingscards.SettingsTheme
import com.gmillz.settingscards.SwitchSetting
import com.slim.slimfilemanager.R
import com.slim.slimfilemanager.ThemeActivity
import com.slim.slimfilemanager.utils.RootUtils
import com.slim.util.Constant
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : ThemeActivity(), SettingBase.OnSettingChanged {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.settings_activity)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        toolbar.setTitle(R.string.action_settings)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        settings_container.setTheme(MySettingsTheme(this))
        populateSettings()
    }

    fun populateSettings() {
        var category = SettingsCategory(R.string.file_manager)
        if (RootUtils.isRootAvailable) {
            settings_container.addSetting(category,
                    SwitchSetting(R.string.enable_root_title, 0, SettingsProvider.KEY_ENABLE_ROOT))
        }
        settings_container.addSetting(category,
                SwitchSetting(R.string.use_small_page_indicator,
                        R.string.small_page_indicator_summary, SettingsProvider.SMALL_INDICATOR))

        settings_container.addSetting(category,
                ListSetting(R.string.sort_mode_title, 0, SettingsProvider.SORT_MODE)
                        .setSummaryToValue(true)
                        .setEntries(resources.getStringArray(R.array.sort_mode_entries))
                        .setValues(resources.getStringArray(R.array.sort_mode_values))
                        .setDefault("sort_mode_name"))

        category = SettingsCategory(R.string.theme_options)

        val entries = arrayOf(getString(R.string.light), getString(R.string.dark))
        val values =
                arrayOf(Integer.toString(R.style.AppTheme), Integer.toString(R.style.AppTheme_Dark))

        settings_container.addSetting(category,
                ListSetting(R.string.theme, 0, SettingsProvider.THEME)
                        .setEntries(entries)
                        .setValues(values)
                        .setSummaryToValue(true)
                        .setOnSettingChanged(this)
                        .setDefault(R.style.AppTheme.toString()))

        settings_container.recreate()
    }

    override fun onSettingChanged(setting: SettingBase, newValue: Any) {
        if (setting.key == SettingsProvider.THEME) {
            recreate()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private class MySettingsTheme internal constructor(context: Context) : SettingsTheme(context) {
        init {
            colorAccent = ThemeActivity.getAccentColor(context)
        }
    }
}
