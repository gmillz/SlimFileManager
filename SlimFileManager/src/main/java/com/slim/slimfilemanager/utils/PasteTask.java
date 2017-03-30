package com.slim.slimfilemanager.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.TextView;

import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.utils.file.BaseFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class PasteTask implements View.OnClickListener {

    private Context mContext;
    private boolean mMove;
    private BaseFile mCurrent;
    private AlertDialog mDialog;
    private HashMap<File, BaseFile> mExistingFiles = new HashMap<>();
    private ArrayList<BaseFile> mProcess = new ArrayList<>();
    private Callback mCallback;

    public PasteTask(Context context,
                     boolean shouldDelete, String location, Callback callback) {
        mContext = context;
        mMove = shouldDelete;
        mCallback = callback;

        ArrayList<BaseFile> files = new ArrayList<>();
        files.addAll(SelectedFiles.getFiles());

        for (int i = 0; i < files.size(); i++) {
            BaseFile file = files.get(i);
            if (file.exists()) {
                File newFile = new File(location + File.separator + file.getName());
                if (newFile.exists()) {
                    mExistingFiles.put(newFile, file);
                } else {
                    mProcess.add(file);
                }
            }
        }
        processFiles();
    }

    private void processFiles() {
        if (mExistingFiles.isEmpty()) {
            if (mProcess.isEmpty()) return;
            if (mCallback != null) {
                mCallback.pasteFiles(mProcess, mMove);
            }
        } else {
            File key = mExistingFiles.keySet().iterator().next();
            mCurrent = mExistingFiles.get(key);
            mExistingFiles.remove(key);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            View view = View.inflate(mContext, R.layout.file_exists_dialog, null);

            int[] ids = {R.id.skip, R.id.skip_all, R.id.replace, R.id.replace_all, R.id.cancel};
            for (@IdRes int id : ids) {
                view.findViewById(id).setOnClickListener(this);
            }

            ((TextView) view.findViewById(R.id.source)).setText(mCurrent.getPath());
            ((TextView) view.findViewById(R.id.destination)).setText(key.getPath());

            builder.setView(view);
            mDialog = builder.create();
            mDialog.show();
        }
    }

    @Override
    public void onClick(View view) {
        if (mDialog != null) mDialog.dismiss();

        if (view.getId() == R.id.skip_all) {
            mExistingFiles.clear();
        } else if (view.getId() == R.id.replace) {
            mProcess.add(mCurrent);
        } else if (view.getId() == R.id.replace_all) {
            mProcess.add(mCurrent);
            for (File f : mExistingFiles.keySet()) {
                mProcess.add(mExistingFiles.get(f));
            }
            mExistingFiles.clear();
        } else if (view.getId() == R.id.cancel) {
            mExistingFiles.clear();
            mProcess.clear();
        }
        processFiles();
    }

    public interface Callback {
        void pasteFiles(ArrayList<BaseFile> paths, boolean move);
    }

    public static final class SelectedFiles {
        private static final ArrayList<BaseFile> files = new ArrayList<>();

        public static void addFile(BaseFile file) {
            files.add(file);
        }

        public static void clearAll() {
            files.clear();
        }

        public static boolean isEmpty() {
            return files.isEmpty();
        }

        public static ArrayList<BaseFile> getFiles() {
            return files;
        }
    }
}
