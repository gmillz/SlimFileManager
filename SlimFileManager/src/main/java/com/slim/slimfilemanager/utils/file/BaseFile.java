package com.slim.slimfilemanager.utils.file;

import java.io.File;

public abstract class BaseFile {
    public abstract boolean moveToFile(BaseFile newFile);
    public abstract boolean copyToFile(BaseFile newFile);
    public abstract boolean delete();
    public abstract boolean exists();
    public abstract String getName();
    public abstract String getParent();
    public abstract String getPath();
    public abstract String getRealPath();
    public abstract boolean isDirectory();
    public abstract File getFile();
    public abstract long length();
    public abstract String[] list();
    public abstract String getExtension();
    public abstract long lastModified();

    public boolean isFile() {
        return !isDirectory();
    }
}
