package com.slim.slimfilemanager.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.text.TextUtils;
import trikita.log.Log;
import android.widget.ImageView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.services.dropbox.DropBoxConstants;
import com.slim.slimfilemanager.services.dropbox.DropboxLoginActivity;
import com.slim.slimfilemanager.services.dropbox.DropboxUtils;
import com.slim.slimfilemanager.services.dropbox.IconCache;
import com.slim.slimfilemanager.services.dropbox.ListFiles;
import com.slim.slimfilemanager.utils.PasteTask;
import com.slim.slimfilemanager.utils.Utils;
import com.slim.slimfilemanager.utils.file.BaseFile;
import com.slim.slimfilemanager.utils.file.DropboxFile;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class DropboxFragment extends BaseBrowserFragment {

    private static final int LOGIN_REQUEST = 10101;

    DropboxAPI<AndroidAuthSession> mAPI;

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
                DropboxFile df = new DropboxFile(mAPI, entry);
                mFiles.add(df);
                sortFiles();
                mAdapter.notifyItemInserted(mFiles.indexOf(df));
            }
            mRecyclerView.scrollToPosition(0);
            hideProgress();
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppKeyPair appKeyPair = new AppKeyPair(
                DropBoxConstants.ACCESS_KEY, DropBoxConstants.ACCESS_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);

        SharedPreferences prefs = mContext.getSharedPreferences(DropBoxConstants.DROPBOX_NAME, 0);
        String token = prefs.getString(DropBoxConstants.ACCESS_TOKEN, null);

        if (token != null) {
            session.setOAuth2AccessToken(token);
        }

        mAPI = new DropboxAPI<>(session);

        if (!mAPI.getSession().isLinked()) {
            startActivityForResult(
                    new Intent(mActivity, DropboxLoginActivity.class), LOGIN_REQUEST);
        } else {
            filesChanged(mCurrentPath);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                filesChanged("/");
            }
        }
    }

    @Override
    public void onClickFile(final String path) {
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
                        showProgressDialog(R.string.downloading, true);
                        File cacheFile = new File(Utils.getCacheDir() + "/"
                                + StringUtils.replace(path, "/", "_"));
                        if (cacheFile.exists()) {
                            if (!cacheFile.delete())
                                Log.e("Unable to delete " + cacheFile.getAbsolutePath());
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

    @Override
    public void filesChanged(String path) {
        super.filesChanged(path);
        setPathText(path);
        showProgress();
        ListFiles listFiles = new ListFiles(mAPI, path, mCallback);
        listFiles.execute();
    }

    @Override
    public String getDefaultDirectory() {
        return "/";
    }

    @Override
    public BaseFile getFile(final int i) {
        AsyncTask<Void, Void, DropboxFile> getFileTask = new AsyncTask<Void, Void, DropboxFile>() {
            @Override
            protected DropboxFile doInBackground(Void... voids) {
                try {
                    String path = mFiles.get(i).getRealPath();
                    DropboxAPI.Entry entry = mAPI.metadata(
                            path, DropBoxConstants.MAX_COUNT, null, true, null);

                    return new DropboxFile(mAPI, entry);
                } catch (DropboxException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }.execute();

        try {
            return getFileTask.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onDeleteFile(final String file) {
        new AsyncTask<Void, Void, Void>() {
            protected void onPreExecute() {
                showProgressDialog(R.string.delete_dialog_title, true);
            }
            protected Void doInBackground(Void... v) {
                showProgressDialog(R.string.delete_dialog_title, true);
                try {
                    mAPI.delete(file);
                } catch (DropboxException e) {
                    toast("Unable to delete " + file);
                }
                hideProgressDialog();
                return null;
            }
            protected void onPostExecute(Void v) {
                hideProgressDialog();
            }
        }.executeOnExecutor(mExecutor);
    }

    @Override
    public String getTabTitle(String path) {
        return "Dropbox";
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

    @Override
    public PasteTask.Callback getPasteCallback() {
        return new PasteTask.Callback() {
            @Override
            public void pasteFiles(final ArrayList<BaseFile> paths, final boolean move) {
                new AsyncTask<Void, Long, Boolean>() {
                    @Override
                    protected void onPreExecute() {
                        showProgressDialog(move ? R.string.move : R.string.copy, false);
                    }

                    @Override
                    protected Boolean doInBackground(Void... v) {
                        for (BaseFile f : paths) {
                            f.getFile(new BaseFile.GetFileCallback() {
                                @Override
                                public void onGetFile(File file) {
                                    String dropboxPath = mCurrentPath
                                            + File.separator + file.getName();
                                    DropboxUtils.uploadFile(mAPI, dropboxPath, file,
                                            new DropboxUtils.Callback() {
                                                @Override
                                                public void updateProgress(int progress) {
                                                    DropboxFragment.this.updateProgress(progress);
                                                }
                                    });
                                }
                            });
                        }
                        return true;
                    }

                    @Override
                    protected void onPostExecute(Boolean b) {
                        hideProgressDialog();
                    }
                }.executeOnExecutor(mExecutor);
            }
        };
    }

    @Override
    public String getRootFolder() {
        return File.separator;
    }

    @Override
    public void backPressed() {
        filesChanged(new File(mCurrentPath).getParent());
    }

    @Override
    public void addFile(String path) {
        // TODO
    }

    @Override
    public void addNewFile(final String name, final boolean isFolder) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isFolder) {
                        mAPI.createFolder(mCurrentPath + File.separator + name);
                    } else {
                        File tempFile = new File(Utils.getCacheDir() + File.separator + "temp"
                                + name);
                        if (!tempFile.getParentFile().exists()) {
                            if (!tempFile.getParentFile().mkdirs()) {
                                Log.e("Failed to create folder " + tempFile.getParent());
                            }
                        }
                        if (tempFile.createNewFile()) {
                            String path = mCurrentPath + File.separator + name;
                            DropboxUtils.uploadFile(mAPI, path, tempFile, null);
                        }
                    }
                } catch (DropboxException|IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void renameFile(final BaseFile file, final String name) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mAPI.move(file.getRealPath(), file.getParent() + "/" + name);
                } catch (DropboxException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void getIconForFile(ImageView imageView, int position) {
        IconCache.getIconForFile(mContext,
                mAPI, ((DropboxAPI.Entry)mFiles.get(position).getRealFile()), imageView);
    }
}
