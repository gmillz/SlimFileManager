package com.slim.slimfilemanager.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.slim.slimfilemanager.R;

import java.io.File;

public class PermissionsDialog implements CheckBox.OnCheckedChangeListener {

    private AlertDialog.Builder mBuilder;

    private Permissions mOriginalPermissions;
    private Permissions mPermissions;
    private EditText mOwner;
    private EditText mGroup;
    private CheckBox uRead;
    private CheckBox uWrite;
    private CheckBox uExecute;
    private CheckBox gRead;
    private CheckBox gWrite;
    private CheckBox gExecute;
    private CheckBox oRead;
    private CheckBox oWrite;
    private CheckBox oExecute;
    private View mView;
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

    @Override
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
        mOwner = (EditText) mView.findViewById(R.id.owner);
        mGroup = (EditText) mView.findViewById(R.id.group);

        uRead = (CheckBox) mView.findViewById(R.id.uread);
        uWrite = (CheckBox) mView.findViewById(R.id.uwrite);
        uExecute = (CheckBox) mView.findViewById(R.id.uexecute);
        gRead = (CheckBox) mView.findViewById(R.id.gread);
        gWrite = (CheckBox) mView.findViewById(R.id.gwrite);
        gExecute = (CheckBox) mView.findViewById(R.id.gexecute);
        oRead = (CheckBox) mView.findViewById(R.id.oread);
        oWrite = (CheckBox) mView.findViewById(R.id.owrite);
        oExecute = (CheckBox) mView.findViewById(R.id.oexecute);

        int[] ids = { R.id.uread, R.id.uwrite, R.id.uexecute,
                R.id.gread, R.id.gwrite, R.id.gexecute,
                R.id.oread, R.id.owrite, R.id.oexecute };
        for (@IdRes int id : ids) {
            ((CheckBox) mView.findViewById(id)).setOnCheckedChangeListener(this);
        }
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
