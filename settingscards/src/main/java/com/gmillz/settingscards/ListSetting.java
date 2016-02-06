package com.gmillz.settingscards;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Arrays;

public class ListSetting extends SettingBase implements Application.ActivityLifecycleCallbacks {

    String[] mEntries;
    String[] mValues;
    boolean mSetSummaryToValue = false;

    private MaterialDialog mDialog;
    private static Bundle mDialogBundle;

    public ListSetting(@StringRes int title, @StringRes int summary, String key) {
        super(title, summary, key);
    }

    public ListSetting setSummaryToValue(boolean b) {
        mSetSummaryToValue = b;
        return this;
    }

    public ListSetting setEntries(String[] entries) {
        mEntries = entries;
        return this;
    }

    public ListSetting setValues(String[] values) {
        mValues = values;
        return this;
    }

    @Override
    public View getView(final Context context) {
        View v = super.getView(context);
        createDialog(context);

        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(this);

        String value = SettingsHandler.getString(context, mKey, (String) mDefault);
        setSummary(mEntries[Arrays.asList(mValues).indexOf(value)]);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDialog == null) {
                    createDialog(context);
                }
                mDialog.show();
            }
        });
        return v;
    }

    private void createDialog(final Context context) {
        mDialog = new MaterialDialog.Builder(context)
                .items(mEntries)
                .title(getTitle())
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView,
                                            int which, CharSequence text) {
                        String v = mValues[which];
                        if (mOnSettingChanged != null) {
                            mOnSettingChanged.onSettingChanged(
                                    ListSetting.this, mValues[which]);
                        }
                        if (mSetSummaryToValue) {
                            setSummary(text);
                        }
                        SettingsHandler.put(context, mKey, v);
                    }
                }).build();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (mDialogBundle != null) {
            createDialog(activity);
            mDialog.onRestoreInstanceState(mDialogBundle);
            mDialogBundle = null;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (mDialog != null && mDialog.isShowing()) {
            mDialogBundle = mDialog.onSaveInstanceState();
            mDialog.hide();
            mDialog = null;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        mDialog = null;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mDialog = null;
    }
}
