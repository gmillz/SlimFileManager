package com.slim.settings;

import android.content.Context;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.slim.slimfilemanager.settings.SettingsProvider;

import java.util.Arrays;

public class ListSetting extends BaseSetting<ListHolder> {

    public ListSetting(final Context context) {
        super(context);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        String value = SettingsProvider.getString(getContext(), mKey, (String) mDefault);
        final String[] entries = getResources().getStringArray(mHolder.entries);
        final String[] values = getResources().getStringArray(mHolder.values);
        setSummary(entries[Arrays.asList(values).indexOf(value)]);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getContext())
                        .items(entries)
                        .title(getTitle())
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView,
                                                    int which, CharSequence text) {
                                String v = values[which];
                                if (mOnSettingChanged != null) {
                                    mOnSettingChanged.onSettingChanged(
                                            ListSetting.this, values[which]);
                                }
                                if (mHolder.setSummaryToValue) {
                                    setSummary(text);
                                }
                                SettingsProvider.put(getContext(), mKey, v);
                            }
                        }).show();
            }
        });
    }
}
