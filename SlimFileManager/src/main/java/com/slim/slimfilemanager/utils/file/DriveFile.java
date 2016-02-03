package com.slim.slimfilemanager.utils.file;

import android.os.AsyncTask;

import com.google.api.services.drive.Drive;
import com.slim.slimfilemanager.services.drive.DriveFiles;
import com.slim.slimfilemanager.services.drive.DriveUtils;
import com.slim.slimfilemanager.utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class DriveFile extends BaseFile {

    private com.google.api.services.drive.model.File mFile;
    private Drive mDrive;

    public static final String FOLDER_TYPE = "application/vnd.google-apps.folder";

    public DriveFile(Drive drive, com.google.api.services.drive.model.File file) {
        mFile = file;
        mDrive = drive;
    }

    @Override
    public boolean delete() {
        AsyncTask<Void, Void, Void> deleteTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                DriveUtils.deleteFile(mDrive, mFile.getId());
                return null;
            }
        }.execute();
        try {
            deleteTask.get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public String getName() {
        return mFile.getTitle();
    }

    @Override
    public String getParent() {
        return mFile.getParents().get(0).getId();
    }

    @Override
    public String getPath() {
        return DriveFiles.getPath(mFile);
    }

    @Override
    public String getRealPath() {
        return  mFile.getId();
    }

    @Override
    public boolean isDirectory() {
        return mFile.getMimeType().equals(FOLDER_TYPE);
    }

    @Override
    public void getFile(final GetFileCallback callback) {
        new AsyncTask<Void, Void, File>() {
            @Override
            protected File doInBackground(Void... voids) {
                File cacheFile = new File(Utils.getCacheDir() + mFile.getId()
                        + "/" + mFile.getTitle());
                DriveUtils.downloadFile(mDrive, mFile, cacheFile);
                return cacheFile;
            }

            @Override
            protected void onPostExecute(File file) {
                super.onPostExecute(file);
                callback.onGetFile(file);
            }
        }.execute();
    }

    @Override
    public long length() {
        return mFile == null ? 0 : mFile.size();
    }

    @Override
    public String[] list() {
        ArrayList<com.google.api.services.drive.model.File> files =
                DriveFiles.getAll(mFile.getId());
        String[] f = new String[files.size()];
        for (com.google.api.services.drive.model.File fi : files) {
            f[files.indexOf(fi)] = fi.getTitle();
        }
        return f;
    }

    @Override
    public String getExtension() {
        return mFile.getFileExtension();
    }

    @Override
    public long lastModified() {
        return mFile.getModifiedDate().getValue();
    }

    @Override
    public com.google.api.services.drive.model.File getRealFile() {
        return mFile;
    }
}
