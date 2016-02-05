package com.slim.settings;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.slim.slimfilemanager.settings.SettingsProvider;

public class SwitchSetting extends BaseSetting<SwitchHolder> {

    private SwitchCompat mSwitch;

    public SwitchSetting(final Context context) {
        super(context);

        mSwitch = new SwitchCompat(context);
        setExtraView(mSwitch);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwitch.setChecked(!mSwitch.isChecked());
            }
        });

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mOnSettingChanged != null) {
                    mOnSettingChanged.onSettingChanged(SwitchSetting.this, b);
                }
                SettingsProvider.put(context, mKey, b);
            }
        });
    }
}
