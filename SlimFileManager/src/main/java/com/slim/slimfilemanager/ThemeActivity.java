package com.slim.slimfilemanager;

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

    private static final String KEY_PRIMARY_COLOR = "primary_color";
    private static final String KEY_ACCENT_COLOR = "accent_color";

    private int mCurrentTheme;
    private static int sAccentColor;
    private static int sPrimaryColor;
    private static int sPrimaryColorDark;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mCurrentTheme = SettingsProvider.getInt(this,
                SettingsProvider.THEME, R.style.AppTheme);
        setTheme(mCurrentTheme);
        loadColors(this);

        super.onCreate(savedInstanceState);
    }

    private void loadTheme() {
        setColors();
    }

    private static void loadColors(Context context) {
        sPrimaryColor = SettingsProvider.getInt(context, KEY_PRIMARY_COLOR, 0);
        if (sPrimaryColor != 0) {
            sPrimaryColorDark = CircleView.shiftColorDown(sPrimaryColor);
        }
        sAccentColor = SettingsProvider.getInt(context, KEY_ACCENT_COLOR, 0);
    }

    private void setColors() {
        setActionBarBackground(sPrimaryColor);
        setStatusBar(sPrimaryColorDark);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTheme();

        if (themeChanged()) {
            recreate();
        }
    }

    public static void setPrimaryColor(Context context, int color) {
        SettingsProvider.putInt(context, KEY_PRIMARY_COLOR, color);
    }

    public static void setAccentColor(Context context, int color) {
        SettingsProvider.putInt(context, KEY_ACCENT_COLOR, color);
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

    public static int getPrimaryDarkColor(Context context) {
        return getAttrColor(context, android.R.attr.colorPrimaryDark);
    }

    public static int getAccentColor() {
        return sAccentColor; //getAttrColor(context, android.R.attr.colorAccent);
    }

    public static int getAttrColor(Context context, @AttrRes int attr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        return value.data;
    }

    private boolean themeChanged() {
        int newTheme = SettingsProvider.getInt(this, SettingsProvider.THEME,
                R.style.AppTheme);
        int newAccent = SettingsProvider.getInt(this, KEY_ACCENT_COLOR,
                getAttrColor(this, android.R.attr.colorAccent));
        return mCurrentTheme != newTheme ||
                sAccentColor != newAccent;
    }

    public static int getBackgroundColor(Context context) {
        return getAttrColor(context, android.R.attr.background);
    }
}
