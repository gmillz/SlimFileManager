package com.slim.slimfilemanager.utils.file;


import android.content.Context;

import com.slim.slimfilemanager.utils.FileUtil;

import java.io.File;

public class BasicFile extends BaseFile {

    private Context mContext;
    private File mFile;

    public BasicFile(Context context, File file) {
        mContext = context;
        mFile = file;
    }

    @Override
    public boolean delete() {
        return FileUtil.deleteFile(mContext, mFile.getAbsolutePath());
    }

    @Override
    public boolean exists() {
        return mFile.exists();
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public String getPath() {
        return mFile.getAbsolutePath();
    }

    @Override
    public boolean isDirectory() {
        return mFile.isDirectory();
    }

    @Override
    public void getFile(GetFileCallback callback) {
        callback.onGetFile(mFile);
    }

    @Override
    public String getRealPath() {
        return getPath();
    }

    @Override
    public String getParent() {
        return mFile.getParent();
    }

    @Override
    public long length() {
        return mFile.length();
    }

    @Override
    public String[] list() {
        return mFile.list();
    }

    @Override
    public String getExtension() {
        return FileUtil.getExtension(getName());
    }

    @Override
    public long lastModified() {
        return mFile.lastModified();
    }

    @Override
    public File getRealFile() {
        return mFile;
    }
}
