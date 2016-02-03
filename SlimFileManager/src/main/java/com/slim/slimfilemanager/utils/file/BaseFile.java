package com.slim.slimfilemanager.utils.file;

import java.io.File;

public abstract class BaseFile {

    protected String path;

    public abstract boolean delete();
    public abstract boolean exists();
    public abstract String getName();
    public abstract String getParent();
    public abstract String getPath();
    public abstract String getRealPath();
    public abstract boolean isDirectory();
    public abstract void getFile(GetFileCallback callback);
    public abstract long length();
    public abstract String[] list();
    public abstract String getExtension();
    public abstract long lastModified();

    public abstract Object getRealFile();

    public boolean isFile() {
        return !isDirectory();
    }

    public interface GetFileCallback {
        void onGetFile(File file);
    }
}
