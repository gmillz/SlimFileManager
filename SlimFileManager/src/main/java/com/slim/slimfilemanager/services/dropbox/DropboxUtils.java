package com.slim.slimfilemanager.services.dropbox;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DropboxUtils {

    public static abstract class Callback {
        public void updateProgress(int progress) {}
    }

    public static void uploadFile(DropboxAPI service,
                                  String dropboxPath, File file, final Callback callback) {
        try {
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                DropboxAPI.UploadRequest request =
                        service.putFileOverwriteRequest(
                                dropboxPath, fis, file.length(),
                                new ProgressListener() {
                                    @Override
                                    public long progressInterval() {
                                        return 500;
                                    }

                                    @Override
                                    public void onProgress(
                                            long l, long l1) {
                                        int percent = (int)
                                                (100.0 * (double) l / l1 + 0.5);
                                        if (callback != null) {
                                            callback.updateProgress(percent);
                                        }
                                    }
                                });
                if (request != null) {
                    request.upload();
                }
            }
        } catch (IOException | DropboxException e) {
            // ignore
        }
    }
}
