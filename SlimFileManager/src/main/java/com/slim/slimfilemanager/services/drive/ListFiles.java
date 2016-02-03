package com.slim.slimfilemanager.services.drive;

import android.os.AsyncTask;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.util.ArrayList;

public class ListFiles extends AsyncTask<Void, Void, Void> {

    Drive mDrive;
    String mPath;
    Callback mCallback;

    private ArrayList<File> mFiles = new ArrayList<>();

    public interface Callback {
        void filesList(ArrayList<File> files);
    }

    public ListFiles(Drive drive, String path, Callback callback) {
        mDrive = drive;
        mPath = path;
        mCallback = callback;
    }

    @Override
    protected Void doInBackground(Void... v) {
        while (!DriveFiles.isPopulated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        if (DriveFiles.contains(mPath)) {
            ArrayList<File> files = DriveFiles.getAll(mPath);
            mFiles.addAll(files);
        }
        return null;
    }

    @Override
    public void onPostExecute(Void v) {
        if (mCallback != null) {
            mCallback.filesList(mFiles);
        }
    }
}
