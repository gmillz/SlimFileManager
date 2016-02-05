package com.slim.settings;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.ThemeActivity;

import trikita.log.Log;

public class SettingsCategory extends CardView {

    private LinearLayout mContainer;
    private TextView mTitle;

    public SettingsCategory(Context context) {
        super(context);
        setCardBackgroundColor(0xff353535);
        CardView.LayoutParams layoutParams = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(getResources().getDimensionPixelSize(R.dimen.card_margin_sides),
                getResources().getDimensionPixelSize(R.dimen.card_margin_top),
                getResources().getDimensionPixelSize(R.dimen.card_margin_sides),
                getResources().getDimensionPixelSize(R.dimen.card_margin_bottom));
        setLayoutParams(params);
        setElevation(getResources().getDimension(R.dimen.card_elevation));
        setRadius(getResources().getDimension(R.dimen.card_corner_radius));
        int contentPadding = getResources().getDimensionPixelSize(R.dimen.card_content_padding);
        setContentPadding(contentPadding, contentPadding, contentPadding, contentPadding);
        setUseCompatPadding(true);

        mContainer = new LinearLayout(context);
        mContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        mContainer.setOrientation(LinearLayout.VERTICAL);
        super.addView(mContainer);

        mTitle = new TextView(context);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, 0, 0, convertDpToPixel(18f, context));
        mTitle.setLayoutParams(textParams);
        mTitle.setTextSize(18f);
        mTitle.setTextColor(ThemeActivity.getAccentColor());
        mContainer.addView(mTitle, textParams);
    }

    public static int convertDpToPixel(final float dp, final Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * metrics.densityDpi / 160f + 0.5);
    }

    @Override
    public void addView(View view) {
        mContainer.addView(view);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }
}
