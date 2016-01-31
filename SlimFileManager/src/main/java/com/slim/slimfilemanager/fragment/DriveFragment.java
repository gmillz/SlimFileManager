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
import android.text.TextUtils;
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
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.slim.slimfilemanager.FileManager;
import com.slim.slimfilemanager.services.drive.IconCache;
import com.slim.slimfilemanager.services.drive.ListFiles;
import com.slim.slimfilemanager.utils.PasteTask;
import com.slim.slimfilemanager.utils.file.BaseFile;
import com.slim.slimfilemanager.utils.file.DriveFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DriveFragment extends BaseBrowserFragment {

    private GoogleAccountCredential mCredential;
    private Drive mDrive;

    private SharedPreferences mPrefs;

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    private static final String PREF_ACCOUNT_NAME = "accountName";

    private static final String[] SCOPES = { DriveScopes.DRIVE };

    public static final String ROOT_FOLDER = "My Drive";
    public String mRootFolderId;


    private ListFiles.Callback mCallback = new ListFiles.Callback() {
        @Override
        public void filesList(ArrayList<com.google.api.services.drive.model.File> files) {
            if (files == null || files.isEmpty()) return;

            mAdapter.notifyDataSetChanged();

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

        if (isGooglePlayServicesAvailable()) {
            filesChanged(ROOT_FOLDER);
        }

        setRootFolderId();
    }

    @Override
    public void onClickFile(final String path) {
        if (TextUtils.isEmpty(path)) return;
        Log.d("TEST", "onClickFile(" + path + ")");
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (path.equals(getRootFolder())) {
                        filesChanged(path);
                        return;
                    }
                    com.google.api.services.drive.model.File file =
                            mDrive.files().get(path).execute();
                    if (file.getMimeType().equals(DriveFile.FOLDER_TYPE)) {
                        filesChanged(path);
                    } else {
                        Log.d("TEST", "name=" + file.getTitle());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
                showProgress();
                if (!mFiles.isEmpty()) mFiles.clear();
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
            public void pasteFiles(ArrayList<String> paths, boolean move) {

            }
        };
    }

    @Override
    public BaseFile getFile(int i) {
        return null;
    }

    @Override
    public void onDeleteFile(String path) {
    }

    @Override
    public String getDefaultDirectory() {
        return File.separator;
    }

    @Override
    public void backPressed() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("TEST", "mCurrentPath=" + mCurrentPath);
                    com.google.api.services.drive.model.File file =
                            mDrive.files().get(mCurrentPath).execute();
                    if (!file.getParents().isEmpty()) {
                        ParentReference parent = file.getParents().get(0);
                        if (parent != null) {
                            filesChanged(parent.getId());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public String getRootFolder() {
        return mRootFolderId;
    }

    private void setRootFolderId() {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... v) {
                try {
                    FileList result = mDrive.files().list().setMaxResults(10000).execute();
                    if (!result.isEmpty()) {
                        for (com.google.api.services.drive.model.File file : result.getItems()) {
                            List<ParentReference> parents = file.getParents();
                            for (ParentReference parent : parents) {
                                if (parent.getIsRoot()) {
                                    mRootFolderId = parent.getId();
                                    return null;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(mExecutor);
    }

    @Override
    public void addFile(String f) {
        // TODO implement
    }

    @Override
    public void getIconForFile(ImageView imageView, int position) {
        IconCache.getIconForFile(mContext, mDrive,
                ((DriveFile) mFiles.get(position)).getDriveFile(), imageView);
    }
}
