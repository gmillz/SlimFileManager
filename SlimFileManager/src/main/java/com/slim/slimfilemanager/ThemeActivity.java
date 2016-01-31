package com.slim.slimfilemanager;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;

import com.slim.slimfilemanager.settings.SettingsProvider;

public class ThemeActivity extends AppCompatActivity {

    private int mCurrentTheme;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mCurrentTheme = SettingsProvider.getInt(this,
                SettingsProvider.THEME, R.style.AppTheme);

        setTheme(mCurrentTheme);

        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();

        int newTheme = SettingsProvider.getInt(this, SettingsProvider.THEME,
                R.style.AppTheme);

        if (mCurrentTheme != newTheme) {
            recreate();
        }
    }

    public static int getPrimaryDarkColor(Context context) {
        return getAttrColor(context, android.R.attr.colorPrimaryDark);
    }

    public static int getAccentColor(Context context) {
        return getAttrColor(context, android.R.attr.colorAccent);
    }

    public static int getAttrColor(Context context, @AttrRes int attr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        return value.data;
    }
}
