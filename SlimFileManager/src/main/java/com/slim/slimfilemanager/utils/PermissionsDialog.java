package com.slim.slimfilemanager.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.slim.slimfilemanager.R;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class PermissionsDialog {

    AlertDialog.Builder mBuilder;

    Permissions mOriginalPermissions;
    Permissions mPermissions;

    private View mView;

    @Bind(R.id.owner) EditText mOwner;
    @Bind(R.id.group) EditText mGroup;
    @Bind(R.id.uread) CheckBox uRead;
    @Bind(R.id.uwrite) CheckBox uWrite;
    @Bind(R.id.uexecute) CheckBox uExecute;
    @Bind(R.id.gread) CheckBox gRead;
    @Bind(R.id.gwrite) CheckBox gWrite;
    @Bind(R.id.gexecute) CheckBox gExecute;
    @Bind(R.id.oread) CheckBox oRead;
    @Bind(R.id.owrite) CheckBox oWrite;
    @Bind(R.id.oexecute) CheckBox oExecute;

    private File mFile;
    private Context mContext;

    public PermissionsDialog(Context context, String file) {
        mContext = context;
        mFile = new File(file);

        mOriginalPermissions = new Permissions();
        mOriginalPermissions.loadFromFile(mContext, mFile);

        mPermissions = mOriginalPermissions;

        init();
    }

    @OnCheckedChanged({R.id.uread, R.id.uwrite, R.id.uexecute,
            R.id.gread, R.id.gwrite, R.id.gexecute,
            R.id.oread, R.id.owrite, R.id.oexecute})
    @SuppressWarnings("unused")
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        if (view == uRead) {
            mPermissions.userRead = isChecked;
        } else if (view == uWrite) {
            mPermissions.userWrite = isChecked;
        } else if (view == uExecute) {
            mPermissions.userExecute = isChecked;
        } else if (view == gRead) {
            mPermissions.groupRead = isChecked;
        } else if (view == gWrite) {
            mPermissions.groupWrite = isChecked;
        } else if (view == gExecute) {
            mPermissions.groupExecute = isChecked;
        } else if (view == oRead) {
            mPermissions.otherRead = isChecked;
        } else if (view == oWrite) {
            mPermissions.otherWrite = isChecked;
        } else if (view == oExecute) {
            mPermissions.otherExecute = isChecked;
        }
    }

    private void init() {
        initViews();
        initBuilder();
        initValues();
    }

    private void initValues() {
        mOwner.setText(mOriginalPermissions.owner);
        mGroup.setText(mOriginalPermissions.group);

        uRead.setChecked(mOriginalPermissions.userRead);
        uWrite.setChecked(mOriginalPermissions.userWrite);
        uExecute.setChecked(mOriginalPermissions.userExecute);
        gRead.setChecked(mOriginalPermissions.groupRead);
        gWrite.setChecked(mOriginalPermissions.groupWrite);
        gExecute.setChecked(mOriginalPermissions.groupExecute);
        oRead.setChecked(mOriginalPermissions.otherRead);
        oWrite.setChecked(mOriginalPermissions.otherWrite);
        oExecute.setChecked(mOriginalPermissions.otherExecute);
    }

    private void initViews() {
        mView = View.inflate(mContext, R.layout.permissions, null);
        ButterKnife.bind(this, mView);
    }

    private void initBuilder() {
        mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setTitle(mFile.getName());
        mBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mBuilder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mPermissions.equals(mOriginalPermissions))
                FileUtil.applyPermissions(mContext, mFile, mPermissions);
                FileUtil.changeGroupOwner(mContext, mFile, mPermissions.owner, mPermissions.group);
                dialog.dismiss();
            }
        });
        mBuilder.setView(mView);
    }

    public Dialog getDialog() {
        return mBuilder.create();
    }
}
