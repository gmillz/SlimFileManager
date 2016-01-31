package com.slim.slimfilemanager.services.drive;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.slim.slimfilemanager.fragment.DriveFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        try {
            FileList result = mDrive.files().list().setMaxResults(Integer.MAX_VALUE).execute();
            List<File> files = result.getItems();
            List<String> names = new ArrayList<>();
            for (File file : files) {
                Log.d("TEST", "name=" + file.getTitle());
                List<ParentReference> parents = file.getParents();
                Log.d("TEST", "id=" + file.getId());
                for (ParentReference parent : parents) {
                    Log.d("TEST", "parent=" + parent.getId());
                    if ((mPath.equals(DriveFragment.ROOT_FOLDER)
                            && parent.getIsRoot()) || mPath.equals(parent.getId())) {
                        Log.d("TEST", "name1=" + file.getTitle());
                        if (!names.contains(file.getTitle())) {
                            names.add(file.getTitle());
                            mFiles.add(file);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
