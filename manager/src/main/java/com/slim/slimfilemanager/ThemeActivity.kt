package com.slim.slimfilemanager

import android.app.ActivityManager
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.annotation.AttrRes
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue

import com.slim.slimfilemanager.settings.SettingsProvider

open class ThemeActivity : AppCompatActivity() {

    protected var mCurrentTheme: Int = 0
    protected var mAccentColor: Int = 0
    protected var mPrimaryColor: Int = 0
    protected var mPrimaryColorDark: Int = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        mCurrentTheme = SettingsProvider.getInt(this,
                SettingsProvider.THEME, R.style.AppTheme)
        setTheme(mCurrentTheme)
        loadColors(this)

        super.onCreate(savedInstanceState)
    }

    private fun loadTheme() {
        val newTheme = SettingsProvider.getInt(this, SettingsProvider.THEME,
                R.style.AppTheme)
        val newAccent = SettingsProvider.getInt(this, KEY_ACCENT_COLOR,
                getAttrColor(this, android.R.attr.colorAccent))
        val newPrimary = SettingsProvider.getInt(this, KEY_PRIMARY_COLOR,
                getAttrColor(this, android.R.attr.colorPrimary))

        setColors()

        if (newPrimary != mPrimaryColor
                || newAccent != mAccentColor || newTheme != mCurrentTheme) {
            recreate()
        }
    }

    private fun loadColors(context: Context) {
        mPrimaryColor = getAttrColor(context, android.R.attr.colorPrimary)
        mPrimaryColorDark = getAttrColor(context, android.R.attr.colorPrimaryDark)
        mAccentColor = SettingsProvider.getInt(context, KEY_ACCENT_COLOR,
                getAttrColor(context, android.R.attr.colorAccent))
    }

    private fun setColors() {
        setActionBarBackground(mPrimaryColor)
        setStatusBar(mPrimaryColorDark)
    }

    public override fun onResume() {
        super.onResume()
        loadTheme()

        val td = ActivityManager.TaskDescription(null, null, mPrimaryColor)
        setTaskDescription(td)
    }

    private fun setActionBarBackground(color: Int) {
        val bar = supportActionBar
        bar?.setBackgroundDrawable(ColorDrawable(color))
    }

    private fun setStatusBar(color: Int) {
        //getWindow().setStatusBarColor(CircleView.shiftColorDown(color));
    }

    companion object {

        val KEY_PRIMARY_COLOR = "primary_color"
        val KEY_ACCENT_COLOR = "accent_color"

        fun getAccentColor(context: Context): Int {
            return SettingsProvider.getInt(context, KEY_ACCENT_COLOR,
                    getAttrColor(context, android.R.attr.colorAccent))
        }

        fun getPrimaryColor(context: Context): Int {
            return SettingsProvider.getInt(context, KEY_PRIMARY_COLOR,
                    getAttrColor(context, android.R.attr.colorPrimary))
        }

        fun getAttrColor(context: Context, @AttrRes attr: Int): Int {
            val value = TypedValue()
            context.theme.resolveAttribute(attr, value, true)
            return value.data
        }
    }
}
