package com.slim.slimfilemanager.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.slim.slimfilemanager.utils.FileUtil;
import com.slim.slimfilemanager.utils.MimeUtils;
import com.slim.slimfilemanager.utils.RootUtils;
import com.slim.slimfilemanager.utils.Utils;

import java.io.File;
import java.util.List;

public class BrowserFragment extends BaseBrowserFragment {

    public static BaseBrowserFragment newInstance(String path) {
        BaseBrowserFragment fragment = new BrowserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    public void onClickFile(String file) {
        if (TextUtils.isEmpty(file)) {
            return;
        }
        File f = new File(file);
        if (f.exists()) {
            if (f.isDirectory()) {
                filesChanged(file);
            } else {
                fileClicked(new File(file));
            }
        }
    }

    @Override
    public String getFilePath(int i) {
        return mFiles.get(i).path;
    }

    public void filesChanged(String file) {
        if (mExitOnBack) mExitOnBack = false;
        File newPath = new File(file);
        if (!newPath.exists()) {
            return;
        }
        mActivity.setTabTitle(this, newPath);
        if (!newPath.canRead() && !RootUtils.isRootAvailable()) {
            Toast.makeText(mContext, "Root is required to view folder.", Toast.LENGTH_SHORT).show();
        }
        List<String> files = Utils.listFiles(file);
        if (files == null) {
            return;
        }
        if (mPath != null) mPath.setText(new File(file).getAbsolutePath());
        mCurrentPath = file;
        if (!mFiles.isEmpty()) mFiles.clear();
        mAdapter.notifyDataSetChanged();
        for (String s : files) {
            if (mPicking && !TextUtils.isEmpty(mMimeType) && mMimeType.startsWith("image/")
                    && !MimeUtils.isPicture(new File(s)) && new File(s).isFile()) continue;
            Item item = new Item();
            item.name = new File(s).getName();
            item.path = s;
            mFiles.add(item);
            sortFiles();
            mAdapter.notifyItemInserted(mFiles.indexOf(item));
        }
        mRecyclerView.scrollToPosition(0);
    }
}
