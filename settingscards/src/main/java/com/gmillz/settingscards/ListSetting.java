package com.gmillz.settingscards;

import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Arrays;

public class ListSetting extends SettingBase {

    @ArrayRes int mEntries;
    @ArrayRes int mValues;
    boolean mSetSummaryToValue = false;

    public ListSetting(@StringRes int title, @StringRes int summary, String key) {
        super(title, summary, key);
    }

    public ListSetting setSummaryToValue(boolean b) {
        mSetSummaryToValue = b;
        return this;
    }

    public ListSetting setEntries(@ArrayRes int id) {
        mEntries = id;
        return this;
    }

    public ListSetting setValues(@ArrayRes int id) {
        mValues = id;
        return this;
    }

    @Override
    public View getView(final Context context) {
        View v = super.getView(context);

        String value = SettingsHandler.getString(context, mKey, (String) mDefault);
        final String[] entries = context.getResources().getStringArray(mEntries);
        final String[] values = context.getResources().getStringArray(mValues);
        setSummary(entries[Arrays.asList(values).indexOf(value)]);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(context)
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
                                if (mSetSummaryToValue) {
                                    setSummary(text);
                                }
                                SettingsHandler.put(context, mKey, v);
                            }
                        }).show();
            }
        });
        return v;
    }
}
