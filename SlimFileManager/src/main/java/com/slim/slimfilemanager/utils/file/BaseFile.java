package com.slim.slimfilemanager.utils.file;

import java.io.File;

public abstract class BaseFile {

    protected String path;

    public static BaseFile getBlankFile() {
        return new BaseFile() {
            @Override
            public boolean delete() {
                return false;
            }

            @Override
            public boolean exists() {
                return false;
            }

            @Override
            public String getName() {
                return "";
            }

            @Override
            public String getParent() {
                return "";
            }

            @Override
            public String getPath() {
                return "";
            }

            @Override
            public String getRealPath() {
                return "";
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public void getFile(GetFileCallback callback) {
            }

            @Override
            public long length() {
                return 0;
            }

            @Override
            public String[] list() {
                return new String[0];
            }

            @Override
            public String getExtension() {
                return "";
            }

            @Override
            public long lastModified() {
                return 0;
            }

            @Override
            public Object getRealFile() {
                return new Object();
            }
        };
    }

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
