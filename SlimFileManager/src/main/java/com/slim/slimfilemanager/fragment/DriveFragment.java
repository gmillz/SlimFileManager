package com.slim.slimfilemanager.fragment;

import static android.app.Activity.RESULT_OK;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.ParentReference;
import com.slim.slimfilemanager.FileManager;
import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.services.drive.DriveFiles;
import com.slim.slimfilemanager.services.drive.DriveUtils;
import com.slim.slimfilemanager.services.drive.IconCache;
import com.slim.slimfilemanager.services.drive.ListFiles;
import com.slim.slimfilemanager.utils.MimeUtils;
import com.slim.slimfilemanager.utils.PasteTask;
import com.slim.slimfilemanager.utils.file.BaseFile;
import com.slim.slimfilemanager.utils.file.DriveFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class DriveFragment extends BaseBrowserFragment {

    private GoogleAccountCredential mCredential;
    private Drive mDrive;

    private SharedPreferences mPrefs;

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    private static final String PREF_ACCOUNT_NAME = "accountName";

    private static final String[] SCOPES = {
            DriveScopes.DRIVE
    };

    public static final String ROOT_FOLDER = "My Drive";


    private ListFiles.Callback mCallback = new ListFiles.Callback() {
        @Override
        public void filesList(ArrayList<com.google.api.services.drive.model.File> files) {
            if (files == null) return;

            for (com.google.api.services.drive.model.File file : files) {
                DriveFile df = new DriveFile(mDrive, file);
                mFiles.add(df);
                sortFiles();
                mAdapter.notifyItemInserted(mFiles.indexOf(df));
            }
            hideProgress();
        }
    };

    private FileManager.ActivityCallback mActivityCallback = new FileManager.ActivityCallback() {
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case REQUEST_GOOGLE_PLAY_SERVICES:
                    if (requestCode != RESULT_OK) {
                        isGooglePlayServicesAvailable();
                    }
                    break;
                case REQUEST_ACCOUNT_PICKER:
                    if (resultCode == RESULT_OK && data != null
                            && data.getExtras() != null) {
                        String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                        if (accountName != null) {
                            mCredential.setSelectedAccountName(accountName);
                            mPrefs.edit().putString(PREF_ACCOUNT_NAME, accountName).apply();
                        }
                    }
                    break;
                case REQUEST_AUTHORIZATION:
                    if (resultCode != RESULT_OK) {
                        chooseAccount();
                    }
            }

        }
    };

    private void chooseAccount() {
        mActivity.startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        } else if (connectionStatusCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

    private void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode, mActivity, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("TEST", "onActivityCreated");

        mActivity.addActivityCallback(mActivityCallback);

        mPrefs = mContext.getSharedPreferences("drive_prefs", 0);
        mCredential = GoogleAccountCredential.usingOAuth2(mContext, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(mPrefs.getString(PREF_ACCOUNT_NAME, null));

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        mDrive = new Drive.Builder(transport, jsonFactory, mCredential)
                .setApplicationName(mActivity.getApplication().getPackageName())
                .build();

        if (!DriveFiles.isPopulated()) {
            DriveFiles.populate(mDrive, mExecutor);
        }

        if (isGooglePlayServicesAvailable()) {
            onClickFile(ROOT_FOLDER);
        }
    }

    @Override
    public void onClickFile(final String path) {
        //if (TextUtils.isEmpty(path)) return;
        Log.d("TEST", "onClickFile(" + path + ")");
        Log.d("TEST", "rootFolder=" + getRootFolder());
        if (path.equals(getRootFolder())) {
            Log.d("TEST", "path equals " + getRootFolder());
            filesChanged(path);
            setPathText(path);
            return;
        }
        final com.google.api.services.drive.model.File file = DriveFiles.get(path);
        if (file == null || file.getMimeType() == null) {
            Log.d("TEST", "file is null");
            return;
        }
        if (file.getMimeType().equals(DriveFile.FOLDER_TYPE)) {
            mCurrentPath = path;
            filesChanged(path);
            if (path.equals(getRootFolder()) || file.getParents().isEmpty()) {
                setPathText(getRootFolder());
            } else {
                updatePathText(file);
            }
        } else {
            Log.d("TEST", "name=" + file.getTitle());
            showProgressDialog(R.string.downloading, true);
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    File cacheFile = DriveUtils.downloadFileToCache(mDrive, file);
                    hideProgressDialog();
                    fileClicked(cacheFile);
                }
            });
        }
    }

    private void updatePathText(final com.google.api.services.drive.model.File file) {
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... v) {
                DriveFile f = new DriveFile(mDrive, file);
                return getRootFolder() + f.getPath();
            }
            protected void onPostExecute(String path) {
                setPathText(path);
            }
        }.executeOnExecutor(mExecutor);
    }

    @Override
    public void refreshFiles() {
        DriveFiles.populate(mDrive, mExecutor);
        filesChanged(mCurrentPath);
    }

    @Override
    public void filesChanged(String path) {
        super.filesChanged(path);
        Log.d("TEST", "path=" + path);
        if (mCredential.getSelectedAccountName() == null) {
            Log.d("TEST", "choosing account");
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                Log.d("TEST", "here");
                showProgress();
                if (!mFiles.isEmpty()) mFiles.clear();
                mAdapter.notifyDataSetChanged();
                ListFiles listFiles = new ListFiles(mDrive, path, mCallback);
                listFiles.execute();
            } else {
                mPath.setText("No network connection available.");
            }
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public String getTabTitle(String path) {
        return "Drive";
    }

    @Override
    public PasteTask.Callback getPasteCallback() {
        return new PasteTask.Callback() {
            @Override
            public void pasteFiles(final ArrayList<BaseFile> paths, final boolean move) {
                new AsyncTask<Void, Integer, Boolean>() {
                    @Override
                    protected void onPreExecute() {
                        showProgressDialog(move ? R.string.move : R.string.copy, false);
                    }

                    @Override
                    protected Boolean doInBackground(Void... v) {
                        for (final BaseFile f : paths) {
                            f.getFile(new BaseFile.GetFileCallback() {
                                @Override
                                public void onGetFile(File file) {
                                    if (file.exists()) {
                                        com.google.api.services.drive.model.File dFile =
                                                DriveUtils.uploadFile(mDrive, file, getCurrentPath());
                                        DriveFiles.insertFile(dFile);
                                        publishProgress(paths.indexOf(f));
                                    }
                                }
                            });
                        }
                        return true;
                    }

                    @Override
                    protected void onPostExecute(Boolean b) {
                        hideProgressDialog();
                        filesChanged(mCurrentPath);
                    }

                    @Override
                    protected void onProgressUpdate(Integer... progress) {
                        updateProgress(progress[0]);
                    }
                }.executeOnExecutor(mExecutor);
            }
        };
    }

    @Override
    public String getCurrentPath() {
        if (mCurrentPath.equals(getRootFolder())) {
            return DriveFiles.getRootId();
        } else {
            return mCurrentPath;
        }
    }

    @Override
    public BaseFile getFile(final int i) {
        return mFiles.get(i);
    }

    @Override
    public void onDeleteFile(final String path) {
        new AsyncTask<Void, Void, Void>() {
            protected void onPreExecute() {
                showProgressDialog(R.string.delete_dialog_title, true);
            }
            protected Void doInBackground(Void... v) {
                DriveUtils.deleteFile(mDrive, path);
                DriveFiles.removeFile(DriveFiles.get(path));
                return null;
            }
            protected void onPostExecute(Void v) {
                hideProgressDialog();
            }
        }.executeOnExecutor(mExecutor);
    }

    @Override
    public String getDefaultDirectory() {
        return File.separator;
    }

    @Override
    public void backPressed() {
        com.google.api.services.drive.model.File file = DriveFiles.get(mCurrentPath);
        if (file != null && !file.getParents().isEmpty()) {
            ParentReference parent = file.getParents().get(0);
            if (parent != null) {
                onClickFile(parent.getId());
            }
        }
    }

    @Override
    public String getRootFolder() {
        return ROOT_FOLDER;
    }

    @Override
    public void addFile(String f) {
        // TODO implement
    }

    @Override
    public void addNewFile(final String name, final boolean isFolder) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String parentId;
                if (mCurrentPath.equalsIgnoreCase(getRootFolder())) {
                    parentId = DriveFiles.getRootId();
                } else {
                    parentId = mCurrentPath;
                }
                String mimeType;
                if (isFolder) {
                    mimeType = DriveFile.FOLDER_TYPE;
                } else {
                    mimeType = MimeUtils.getMimeType(name);
                }
                com.google.api.services.drive.model.File f =
                        DriveUtils.createFile(mDrive, name, "", parentId, mimeType);
                if (f != null) {
                    DriveFiles.insertFile(f);
                    filesChanged(mCurrentPath);
                } else {
                    toast("Failed.");
                }

            }
        });
    }

    @Override
    public void renameFile(final BaseFile file, final String name) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                DriveUtils.renameFile(mDrive,
                        (com.google.api.services.drive.model.File) file.getRealFile(), name);
            }
        });
    }

    @Override
    public void getIconForFile(ImageView imageView, int position) {
        IconCache.getIconForFile(mContext, mDrive,
                ((com.google.api.services.drive.model.File)
                        mFiles.get(position).getRealFile()), imageView);
    }
}
