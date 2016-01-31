package com.slim.slimfilemanager.utils.file;

import com.google.api.services.drive.Drive;

import java.io.File;

public class DriveFile extends BaseFile {

    private com.google.api.services.drive.model.File mFile;
    private Drive mDrive;

    public static final String FOLDER_TYPE = "application/vnd.google-apps.folder";

    public DriveFile(Drive drive, com.google.api.services.drive.model.File file) {
        mFile = file;
        mDrive = drive;
    }

    @Override
    public boolean moveToFile(BaseFile newFile) {
        return false;
    }

    @Override
    public boolean copyToFile(BaseFile newFile) {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
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
        return mFile.getId();
    }

    @Override
    public String getRealPath() {
        return getPath();
    }

    @Override
    public boolean isDirectory() {
        return mFile.getMimeType().equals(FOLDER_TYPE);
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public long length() {
        return mFile == null ? 0 : mFile.size();
    }

    @Override
    public String[] list() {
        return new String[0];
    }

    @Override
    public String getExtension() {
        return mFile.getFileExtension();
    }

    @Override
    public long lastModified() {
        return mFile.getModifiedDate().getValue();
    }

    public com.google.api.services.drive.model.File getDriveFile() {
        return mFile;
    }
}
