package com.gmillz.settingscards;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.util.TypedValue;

@SuppressWarnings("unused")
class SettingsTheme {

    int colorAccent;
    int cardBackground = 0xff353535;
    int pageBackground;
    int primaryTextColor;
    int secondaryTextColor;

    public SettingsTheme(Context context) {
        colorAccent = Color.WHITE;
        pageBackground = Color.WHITE;
        primaryTextColor = Color.WHITE;
        secondaryTextColor = Color.GRAY;
    }

    static int getAttrColor(Context context, @AttrRes int attr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        return value.data;
    }

    static boolean isColorDark(int color) {
        double darkness = 1-(0.299*Color.red(color)
                + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        return darkness > 0.5;
    }

}
