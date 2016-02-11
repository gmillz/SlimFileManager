package com.slim.slimfilemanager;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;

import com.afollestad.materialdialogs.color.CircleView;
import com.slim.slimfilemanager.settings.SettingsProvider;

public class ThemeActivity extends AppCompatActivity {

    public static final String KEY_PRIMARY_COLOR = "primary_color";
    public static final String KEY_ACCENT_COLOR = "accent_color";

    protected int mCurrentTheme;
    protected int mAccentColor;
    protected int mPrimaryColor;
    protected int mPrimaryColorDark;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mCurrentTheme = SettingsProvider.getInt(this,
                SettingsProvider.THEME, R.style.AppTheme);
        setTheme(mCurrentTheme);
        loadColors(this);

        super.onCreate(savedInstanceState);
    }

    private void loadTheme() {
        int newTheme = SettingsProvider.getInt(this, SettingsProvider.THEME,
                R.style.AppTheme);
        int newAccent = SettingsProvider.getInt(this, KEY_ACCENT_COLOR,
                getAttrColor(this, android.R.attr.colorAccent));
        int newPrimary = SettingsProvider.getInt(this, KEY_PRIMARY_COLOR,
                getAttrColor(this, android.R.attr.colorPrimary));

        setColors();

        if (newPrimary != mPrimaryColor
                || newAccent != mAccentColor || newTheme != mCurrentTheme) {
            recreate();
        }
    }

    private void loadColors(Context context) {
        mPrimaryColor = SettingsProvider.getInt(context, KEY_PRIMARY_COLOR, 0);
        mPrimaryColorDark = CircleView.shiftColorDown(mPrimaryColor);
        if (mPrimaryColor == 0) {
            mPrimaryColor = getAttrColor(context, android.R.attr.colorPrimary);
            mPrimaryColorDark = getAttrColor(context, android.R.attr.colorPrimaryDark);
        }
        mAccentColor = SettingsProvider.getInt(context, KEY_ACCENT_COLOR,
                getAttrColor(context, android.R.attr.colorAccent));
    }

    private void setColors() {
        setActionBarBackground(mPrimaryColor);
        setStatusBar(mPrimaryColorDark);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTheme();

        ActivityManager.TaskDescription td =
                new ActivityManager.TaskDescription(null, null, mPrimaryColor);
        setTaskDescription(td);
    }

    private void setActionBarBackground(int color) {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setBackgroundDrawable(new ColorDrawable(color));
        }
    }

    private void setStatusBar(int color) {
        getWindow().setStatusBarColor(CircleView.shiftColorDown(color));
    }

    public static int getAccentColor(Context context) {
        return SettingsProvider.getInt(context, KEY_ACCENT_COLOR,
                getAttrColor(context, android.R.attr.colorAccent));
    }

    public static int getPrimaryColor(Context context) {
        return SettingsProvider.getInt(context, KEY_PRIMARY_COLOR,
                getAttrColor(context, android.R.attr.colorPrimary));
    }

    public static int getAttrColor(Context context, @AttrRes int attr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        return value.data;
    }
}
