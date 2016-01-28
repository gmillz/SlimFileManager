package com.slim.slimfilemanager.fragment;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.FileObserver;
import android.text.TextUtils;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.slim.slimfilemanager.dropbox.DropBoxConstants;
import com.slim.slimfilemanager.dropbox.ListFiles;
import com.slim.slimfilemanager.utils.Utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DropboxFragment extends BaseBrowserFragment {

    DropboxAPI<AndroidAuthSession> mAPI;
    Executor mExecutor = Executors.newSingleThreadExecutor();

    private final ListFiles.Callback mCallback = new ListFiles.Callback() {
        @Override
        public void filesListed(ArrayList<DropboxAPI.Entry> entries) {

            if (entries == null) {
                return;
            }
            //if (mPath != null) mPath.setText(mCurrentPath);
            //mCurrentPath = ;
            if (!mFiles.isEmpty()) mFiles.clear();
            mAdapter.notifyDataSetChanged();
            for (DropboxAPI.Entry entry : entries) {
                Log.d("TEST", "ENTRY=" + entry.path + " : " + entry.fileName() + " : " + entry.parentPath());
                Item item = new Item();
                item.name = entry.fileName();
                item.path = entry.path;
                mFiles.add(item);
                sortFiles();
                mAdapter.notifyItemInserted(mFiles.indexOf(item));
            }
            mRecyclerView.scrollToPosition(0);
        }
    };

    private ProgressDialog mProgressDialog;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle("Downloading");
        mProgressDialog.setMessage("Please wait...");

        AppKeyPair appKeyPair = new AppKeyPair(
                DropBoxConstants.ACCESS_KEY, DropBoxConstants.ACCESS_SECRET);
        mAPI = new DropboxAPI<>(new AndroidAuthSession(appKeyPair));

        SharedPreferences prefs = mContext.getSharedPreferences(DropBoxConstants.DROPBOX_NAME, 0);
        String key = prefs.getString(DropBoxConstants.ACCESS_KEY, null);
        String secret = prefs.getString(DropBoxConstants.ACCESS_SECRET, null);

        if (key != null && secret != null) {
            AccessTokenPair accessToken = new AccessTokenPair(key, secret);
            mAPI.getSession().setAccessTokenPair(accessToken);
        } else {
            mAPI.getSession().startOAuth2Authentication(mContext);
        }

        filesChanged("/");
    }

    @Override
    public void onClickFile(final String path) {
        Log.d("TEST", "path=" + path);
        if (TextUtils.isEmpty(path)) {
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DropboxAPI.Entry entry = mAPI.metadata(path, 1000, null, true, null);

                    if (entry.isDir) {
                        mCurrentPath = path;
                        filesChanged(path);
                    } else {
                        showProgressDialog();
                        File cacheFile = new File(Utils.getCacheDir() + "/"
                                + StringUtils.replace(path, "/", "_"));
                        if (cacheFile.exists()) {
                            cacheFile.delete();
                        }
                        FileOutputStream outputStream = new FileOutputStream(cacheFile);
                        mAPI.getFile(path, entry.rev, outputStream, null);
                        outputStream.close();
                        watchFile(cacheFile, entry);
                        hideProgressDialog();
                        fileClicked(cacheFile);
                    }
                } catch (IOException|DropboxException e) {
                    // ignore
                }
            }
        });
    }

    private void showProgressDialog() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.show();
            }
        });
    }

    private void hideProgressDialog() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.hide();
            }
        });
    }

    @Override
    public void filesChanged(String path) {
        ListFiles listFiles = new ListFiles(mAPI, path, mCallback);
        listFiles.execute();
    }

    @Override
    public String getFilePath(int i) {
        String filePath;
        try {
            String path = mFiles.get(i).path;
            DropboxAPI.Entry entry = mAPI.metadata(path, 1000, null, true, null);
            File cacheFile = new File(Utils.getCacheDir() + "/"
                    + StringUtils.replace(path, "/", "_"));
            if (cacheFile.exists()) {
                cacheFile.delete();
            }
            FileOutputStream outputStream = new FileOutputStream(cacheFile);
            mAPI.getFile(path, entry.rev, outputStream, null);
            outputStream.close();
            filePath = cacheFile.getAbsolutePath();
        } catch (IOException|DropboxException e) {
            filePath = null;
        }
        return filePath;
    }

    private void watchFile(final File file, final DropboxAPI.Entry entry) {
        FileObserver observer = new FileObserver(file.getAbsolutePath()) {
            @Override
            public void onEvent(int i, String s) {
                if (i == FileObserver.MODIFY) {
                    try {
                        FileInputStream inputStream = new FileInputStream(file);
                        mAPI.delete(entry.path);
                        mAPI.putFile(entry.path, inputStream, file.length(), null, null);
                        inputStream.close();
                    } catch (IOException|DropboxException e) {
                        // ignore
                    }
                }
            }
        };
        observer.startWatching();
    }
}
