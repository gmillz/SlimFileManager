package com.slim.settings;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BaseSetting<T extends SettingHolder> extends RelativeLayout {

    private TextView mTitle;
    private TextView mSummary;
    private FrameLayout mExtraView;

    protected String mKey;

    protected T mHolder;
    protected Object mDefault;

    protected OnSettingClicked mOnSettingClicked;
    protected OnSettingChanged mOnSettingChanged;

    public BaseSetting(Context context) {
        super(context);
        setupView(context);
    }

    public void setHolder(T holder) {
        mHolder = holder;
    }

    protected void setExtraView(View v) {
        mExtraView.addView(v);
        mExtraView.setVisibility(VISIBLE);
    }

    public void setTitle(@StringRes int title) {
        mTitle.setText(title);
    }

    public CharSequence getTitle() {
        return mTitle.getText();
    }

    public void setSummary(CharSequence summary) {
        mSummary.setText(summary);
        mSummary.setVisibility(View.VISIBLE);
    }

    public void setSummary(@StringRes int summary) {
        if (summary > 0) {
            mSummary.setText(summary);
            mSummary.setVisibility(VISIBLE);
        }
    }

    public void setKey(String key) {
        mKey = key;
    }

    public void setOnSettingClicked(OnSettingClicked onSettingClicked) {
        mOnSettingClicked = onSettingClicked;
    }

    public void setOnSettingChanged(OnSettingChanged onSettingChanged) {
        mOnSettingChanged = onSettingChanged;
    }

    public String getKey() {
        return mKey;
    }

    private void setupView(Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = SettingsCategory.convertDpToPixel(16f, getContext());
        setLayoutParams(params);

        mTitle = new TextView(getContext());
        mTitle.setVisibility(VISIBLE);
        mTitle.setId(View.generateViewId());
        RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        childParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        childParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        mTitle.setTextSize(16f);
        addView(mTitle, childParams);

        mSummary = new TextView(getContext());
        childParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        childParams.addRule(RelativeLayout.BELOW, mTitle.getId());
        childParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        mSummary.setTextSize(12f);
        mSummary.setVisibility(GONE);
        addView(mSummary, childParams);

        mExtraView = new FrameLayout(getContext());
        childParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        childParams.addRule(ALIGN_PARENT_END);
        childParams.addRule(CENTER_VERTICAL);
        childParams.setMarginStart(SettingsCategory.convertDpToPixel(10f, getContext()));
        mExtraView.setVisibility(View.GONE);
        addView(mExtraView, childParams);
    }

    @SuppressWarnings("unused")
    public interface OnSettingClicked {
        void onSettingClicked(BaseSetting setting, String key);
    }

    public interface OnSettingChanged {
        void onSettingChanged(BaseSetting setting, Object newValue);
    }
}
