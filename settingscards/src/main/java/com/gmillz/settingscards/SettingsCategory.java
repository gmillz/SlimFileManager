package com.gmillz.settingscards;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsCategory {

    private CardView mCard;
    private LinearLayout mContainer;
    private TextView mTitle;

    @StringRes int mTitleId;

    public SettingsCategory(@StringRes int titleId) {
        mTitleId = titleId;
    }

    public View getView(Context context) {
        mCard = new CardView(context);
        mCard.setCardBackgroundColor(0xff353535);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(context.getResources().getDimensionPixelSize(R.dimen.card_margin_sides),
                context.getResources().getDimensionPixelSize(R.dimen.card_margin_top),
                context.getResources().getDimensionPixelSize(R.dimen.card_margin_sides),
                context.getResources().getDimensionPixelSize(R.dimen.card_margin_bottom));
        mCard.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCard.setElevation(context.getResources().getDimension(R.dimen.card_elevation));
        }
        mCard.setRadius(context.getResources().getDimension(R.dimen.card_corner_radius));
        int contentPadding =
                context.getResources().getDimensionPixelSize(R.dimen.card_content_padding);
        mCard.setContentPadding(contentPadding, contentPadding, contentPadding, contentPadding);
        mCard.setUseCompatPadding(true);

        mContainer = new LinearLayout(context);
        mContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        mContainer.setOrientation(LinearLayout.VERTICAL);
        mCard.addView(mContainer);

        mTitle = new TextView(context);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, 0, 0, convertDpToPixel(18f, context));
        mTitle.setLayoutParams(textParams);
        mTitle.setTextSize(18f);
        setTitle(mTitleId);
        mContainer.addView(mTitle, textParams);
        return mCard;
    }

    public void setTitleColor(int color) {
        mTitle.setTextColor(color);
    }

    public void setCardBackgroundColor(int color) {
        mCard.setCardBackgroundColor(color);
    }

    public static int convertDpToPixel(final float dp, final Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * metrics.densityDpi / 160f + 0.5);
    }

    public void addView(View view) {
        mContainer.addView(view);
    }

    public void setTitle(@StringRes int id) {
        mTitle.setText(id);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }
}
