package com.gmillz.settingscards;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingBase {

    @StringRes
    public int mTitleId;

    @StringRes
    public int mSummaryId;

    public String mKey;

    private RelativeLayout mLayout;
    private TextView mTitle;
    private TextView mSummary;
    private FrameLayout mExtraView;

    protected Object mDefault;

    protected OnSettingClicked mOnSettingClicked;
    protected OnSettingChanged mOnSettingChanged;

    public SettingBase(@StringRes int title, @StringRes int summary, String key) {
        mTitleId = title;
        mSummaryId = summary;
        mKey = key;
    }

    public View getView(Context context) {
        mLayout = new RelativeLayout(context);
        setupView(context);
        return mLayout;
    }

    protected void setExtraView(View v) {
        mExtraView.addView(v);
        mExtraView.setVisibility(View.VISIBLE);
    }

    public SettingBase setDefault(Object object) {
        mDefault = object;
        return this;
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
            mSummary.setVisibility(View.VISIBLE);
        }
    }

    public void setTitleColor(int color) {
        mTitle.setTextColor(color);
    }

    public void setSummaryColor(int color) {
        mSummary.setTextColor(color);
    }

    @SuppressWarnings("unused")
    public void setOnSettingClicked(OnSettingClicked onSettingClicked) {
        mOnSettingClicked = onSettingClicked;
    }

    @SuppressWarnings("unused")
    public void setOnSettingChanged(OnSettingChanged onSettingChanged) {
        mOnSettingChanged = onSettingChanged;
    }

    public String getKey() {
        return mKey;
    }

    private void setupView(Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = SettingsCategory.convertDpToPixel(16f, context);
        mLayout.setLayoutParams(params);

        mTitle = new TextView(context);
        mTitle.setId(View.generateViewId());
        setTitle(mTitleId);
        RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        childParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        childParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        mTitle.setTextSize(16f);
        mLayout.addView(mTitle, childParams);

        mSummary = new TextView(context);
        childParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        childParams.addRule(RelativeLayout.BELOW, mTitle.getId());
        childParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        mSummary.setTextSize(12f);
        mSummary.setVisibility(View.GONE);
        setSummary(mSummaryId);
        mLayout.addView(mSummary, childParams);

        mExtraView = new FrameLayout(context);
        childParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        childParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        childParams.addRule(RelativeLayout.CENTER_VERTICAL);
        childParams.setMarginStart(SettingsCategory.convertDpToPixel(10f, context));
        mExtraView.setVisibility(View.GONE);
        mLayout.addView(mExtraView, childParams);
    }

    @SuppressWarnings("unused")
    public interface OnSettingClicked {
        void onSettingClicked(SettingBase setting, String key);
    }

    public interface OnSettingChanged {
        void onSettingChanged(SettingBase setting, Object newValue);
    }
}
