package com.gmillz.settingscards;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

public class BasicSetting extends SettingBase {

    private View mExtraView;

    public BasicSetting(@StringRes int title, @StringRes int summary, String key) {
        super(title, summary, key);
    }

    public void addExtraView(View v) {
        mExtraView = v;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(75, 75);
        params.gravity = Gravity.CENTER;
        mExtraView.setLayoutParams(params);
    }

    @Override
    public View getView(Context context) {
        View v = super.getView(context);
        if (mExtraView != null) {
            setExtraView(mExtraView);
        }
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnSettingClicked != null) {
                    mOnSettingClicked.onSettingClicked(BasicSetting.this, mKey);
                }
            }
        });
        return v;
    }
}
