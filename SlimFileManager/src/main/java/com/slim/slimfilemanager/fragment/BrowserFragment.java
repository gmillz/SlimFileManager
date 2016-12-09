package com.slim.slimfilemanager.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.settings.SettingsProvider;
import com.slim.slimfilemanager.utils.FileUtil;
import com.slim.slimfilemanager.utils.IconCache;
import com.slim.slimfilemanager.utils.MimeUtils;
import com.slim.slimfilemanager.utils.PasteTask;
import com.slim.slimfilemanager.utils.RootUtils;
import com.slim.slimfilemanager.utils.Utils;
import com.slim.slimfilemanager.utils.file.BaseFile;
import com.slim.slimfilemanager.utils.file.BasicFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import trikita.log.Log;

public class BrowserFragment extends BaseBrowserFragment {

    public static BaseBrowserFragment newInstance(String path) {
        BaseBrowserFragment fragment = new BrowserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        ACTIONS.add(MENU_CUT);
        ACTIONS.add(MENU_PERMISSIONS);
        ACTIONS.add(MENU_ARCHIVE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        filesChanged(mCurrentPath);
    }

    @Override
    public String getTabTitle(String path) {
        File file = new File(path);
        String title = file.getName();
        if (file.getAbsolutePath().equals("/")) {
            title = "ROOT";
        } else if (file.getAbsolutePath().equals(
                Environment.getExternalStorageDirectory().getAbsolutePath())) {
            title = "SDCARD";
        }
        return title;
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
    public String getDefaultDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    @Override
    public BaseFile getFile(int i) {
        return mFiles.get(i);
    }

    public void filesChanged(String file) {
        super.filesChanged(file);
        if (mExitOnBack) mExitOnBack = false;
        File newPath = new File(file);
        if (!newPath.exists()) {
            return;
        }
        if (!newPath.canRead() && !RootUtils.isRootAvailable()) {
            Toast.makeText(mContext, "Root is required to view folder.", Toast.LENGTH_SHORT).show();
        }
        List<String> files = Utils.listFiles(file);
        if (files == null) {
            return;
        }
        setPathText(new File(file).getAbsolutePath());
        mCurrentPath = file;
        if (!mFiles.isEmpty()) mFiles.clear();
        mAdapter.notifyDataSetChanged();
        for (String s : files) {
            if (mPicking && !TextUtils.isEmpty(mMimeType) && mMimeType.startsWith("image/")
                    && !MimeUtils.isPicture(new File(s).getName())
                    && new File(s).isFile()) continue;
            BasicFile bf = new BasicFile(mContext, new File(s));
            mFiles.add(bf);
            sortFiles();
            mAdapter.notifyItemInserted(mFiles.indexOf(bf));
        }
        mRecyclerView.scrollToPosition(0);
        hideProgress();
    }

    @Override
    public void onDeleteFile(String file) {
        if (FileUtil.deleteFile(mContext, file)) {
            removeFile(file);
        } else {
            Toast.makeText(mContext,
                    "Failed to delete file: "
                            + new File(file).getName(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public PasteTask.Callback getPasteCallback() {
        return new PasteTask.Callback() {
            @Override
            public void pasteFiles(ArrayList<BaseFile> paths, final boolean move) {
                showProgressDialog(move ? R.string.move : R.string.copy, true);
                for (BaseFile f : paths) {
                    f.getFile(new BaseFile.GetFileCallback() {
                        @Override
                        public void onGetFile(File file) {
                            boolean passed;
                            Log.d("TEST", "file=" + file.getAbsolutePath() + " : current=" + mCurrentPath);
                            if (move) {
                                passed = FileUtil.moveFile(
                                        mContext, file.getAbsolutePath(), mCurrentPath);
                            } else {
                                passed = FileUtil.copyFile(
                                        mContext, file.getAbsolutePath(), mCurrentPath);
                            }
                            if (!passed) {
                                toast("Failed to paste file - " + file.getName());
                            }
                        }
                    });
                }
                hideProgressDialog();
            }
        };
    }

    @Override
    public String getRootFolder() {
        Log.d("ROOT=" + Environment.getRootDirectory().getAbsolutePath());
        Log.d("DOWNLOAD-CACHE=" + Environment.getDownloadCacheDirectory().getAbsolutePath());
        Log.d("DATA=" + Environment.getDataDirectory());
        if (SettingsProvider.getBoolean(mContext, SettingsProvider.KEY_ENABLE_ROOT, false)
                && RootUtils.isRootAvailable()) {
            return File.separator;
        } else {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
    }

    @Override
    public void backPressed() {
        filesChanged(new File(mCurrentPath).getParent());
    }

    @Override
    public void getIconForFile(ImageView imageView, int position) {
        IconCache.getIconForFile(mContext, mFiles.get(position).getRealPath(), imageView);
    }

    @Override
    public void addNewFile(String name, boolean isFolder) {
        File newFile = new File(mCurrentPath + File.separator + name);
        if (newFile.exists()) {
            toast(isFolder ? R.string.folder_exists : R.string.file_exists);
            return;
        }

        boolean success = true;
        if (isFolder) {
            if (!newFile.mkdirs()) {
                if (SettingsProvider.getBoolean(mContext, SettingsProvider.KEY_ENABLE_ROOT, false)
                        && RootUtils.isRootAvailable() && !RootUtils.createFolder(newFile)) {
                    success = false;
                }
            }
        } else {
            try {
                if (!newFile.exists()) {
                    if (newFile.getParentFile().canWrite()) {
                        if (!newFile.createNewFile()) {
                            success = false;
                        }
                    } else if (SettingsProvider.getBoolean(mContext,
                            SettingsProvider.KEY_ENABLE_ROOT, false) &&
                            RootUtils.isRootAvailable() && !RootUtils.createFile(newFile)) {
                        success = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!success) {
            toast(isFolder ? R.string.unable_to_create_folder : R.string.unable_to_create_file);
        }
    }

    public void addFile(String path) {
        BasicFile bf = new BasicFile(mContext, new File(path));
        mFiles.add(bf);
        sortFiles();
        mAdapter.notifyItemInserted(mFiles.indexOf(bf));
    }

    @Override
    public void renameFile(BaseFile file, String name) {
        File newFile = new File(file.getParent() + File.separator + name);
        FileUtil.renameFile(mContext, (File) file.getRealFile(), newFile);
        removeFile(file.getRealPath());
        addFile(newFile.getAbsolutePath());
    }
}
