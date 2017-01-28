package com.gmillz.settingscards;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

@SuppressWarnings("unused")
public class SettingsTheme {

    protected int colorAccent;
    public int cardBackground = 0xffffffff;
    public int pageBackground;
    public int primaryTextColor;
    public int secondaryTextColor;

    public SettingsTheme(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorAccent = getAttrColor(context, android.R.attr.colorAccent);
        } else {
            colorAccent = getAttrColor(context, R.attr.colorAccent);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cardBackground = getAttrColor(context, android.R.attr.colorBackgroundFloating);
        } else {
            cardBackground = getAttrColor(context, android.R.attr.colorBackground);
        }
        pageBackground = getAttrColor(context, android.R.attr.colorBackground);
        primaryTextColor = getAttrColor(context, android.R.attr.textColorPrimary);
        secondaryTextColor = getAttrColor(context, android.R.attr.textColorSecondary);
    }

    private static int getAttrColor(Context context, @AttrRes int attr) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(attr, tv, true);
        return ContextCompat.getColor(context, tv.resourceId);
    }

    static boolean isColorDark(int color) {
        double darkness = 1-(0.299*Color.red(color)
                + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        return darkness > 0.5;
    }

}
