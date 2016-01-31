package com.slim.slimfilemanager.utils.file;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.slim.slimfilemanager.utils.FileUtil;
import com.slim.slimfilemanager.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DropboxFile extends BaseFile {

    private DropboxAPI<AndroidAuthSession> mAPI;
    private DropboxAPI.Entry mEntry;
    private String mRemotePath;
    private boolean mExists = true;
    private Context mContext;

    public DropboxFile(Context context,
                       DropboxAPI<AndroidAuthSession> api, DropboxAPI.Entry entry) {
        mAPI = api;
        mEntry = entry;
        mExists = !mEntry.isDeleted;
        mRemotePath = entry.path;
        mContext = context;
    }

    @Override
    public boolean copyToFile(final BaseFile newFile) {
        AsyncTask<Void, Void, Void> copyTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    FileOutputStream outputStream = new FileOutputStream(newFile.getFile());
                    mAPI.getFile(mEntry.path, mEntry.rev, outputStream, null);
                    outputStream.close();
                } catch (IOException|DropboxException e) {
                    // ignore
                }
                return null;
            }
        }.execute();
        try {
            copyTask.get();
            return newFile.exists();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean moveToFile(BaseFile newFile) {
        copyToFile(newFile);
        delete();
        return newFile.exists();
    }

    @Override
    public boolean delete() {
        AsyncTask<Void, Void, Void> deleteTask = new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... v) {
                try {
                    mAPI.delete(mRemotePath);
                } catch (DropboxException e) {
                    // ignore
                }
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
        return mExists;
    }

    @Override
    public String getName() {
        return mEntry.fileName();
    }

    @Override
    public String getPath() {
        return "Dropbox" + mEntry.path;
    }

    @Override
    public String getRealPath() {
        return mEntry.path;
    }

    @Override
    public boolean isDirectory() {
        return mEntry.isDir;
    }

    @Override
    public File getFile() {
        File cacheFile = new File(Utils.getCacheDir() + File.separator + mEntry.fileName());
        copyToFile(new BasicFile(mContext, cacheFile));
        return cacheFile;
    }

    @Override
    public String getParent() {
        return mEntry.parentPath();
    }

    @Override
    public long length() {
        return mEntry.bytes;
    }

    @Override
    public String[] list() {
        if (mEntry.contents == null || mEntry.contents.isEmpty()) return new String[0];
        String[] contents = new String[mEntry.contents.size()];
        for (int i = 0; i < mEntry.contents.size(); i++) {
            contents[i] = mEntry.contents.get(i).fileName();
        }
        return contents;
    }

    @Override
    public String getExtension() {
        return FileUtil.getExtension(getName());
    }

    @Override
    public long lastModified() {
        return 0; // Long.valueOf(mEntry.modified);
    }

    public DropboxAPI.Entry getEntry() {
        return mEntry;
    }
}
