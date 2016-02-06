package com.gmillz.settingscards;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

public class SwitchSetting extends SettingBase {

    private SwitchCompat mSwitch;

    public SwitchSetting(@StringRes int title, @StringRes int summary, String key) {
        super(title, summary, key);
    }

    @Override
    public View getView(final Context context) {
        View v = super.getView(context);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwitch.setChecked(!mSwitch.isChecked());
            }
        });

        mSwitch = new SwitchCompat(context);

        if (mDefault == null) mDefault = false;
        mSwitch.setChecked(SettingsHandler.getBoolean(context, mKey, (Boolean) mDefault));

        setExtraView(mSwitch);

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mOnSettingChanged != null) {
                    mOnSettingChanged.onSettingChanged(SwitchSetting.this, b);
                }
                SettingsHandler.put(context, mKey, b);
            }
        });

        return v;
    }
}
