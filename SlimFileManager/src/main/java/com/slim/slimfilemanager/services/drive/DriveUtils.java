package com.slim.slimfilemanager.services.drive;

import android.text.TextUtils;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.slim.slimfilemanager.utils.MimeUtils;
import com.slim.slimfilemanager.utils.Utils;

import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import trikita.log.Log;

public class DriveUtils {

    public static boolean downloadFile(Drive service, File file, java.io.File output) {
        if (TextUtils.isEmpty(file.getDownloadUrl())) {
            return false;
        }

        try {
            HttpResponse resp = service.getRequestFactory().buildGetRequest(
                    new GenericUrl(file.getDownloadUrl())).execute();
            InputStream in = resp.getContent();
            FileOutputStream out = new FileOutputStream(output);
            IOUtils.copy(in, out);
            in.close();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static java.io.File downloadFileToCache(Drive service, File file) {
        final java.io.File cacheFile = new java.io.File(Utils.getCacheDir() + file.getId()
                + "/" + file.getTitle());
        if (cacheFile.exists()) {
            if (!cacheFile.delete()) {
                Log.e("TEMP", "Unable to delete " + cacheFile.getAbsolutePath());
            }
        }
        if (!cacheFile.getParentFile().exists()) {
            if (!cacheFile.getParentFile().mkdirs()) {
                Log.e("TEMP", "Unable to make folder"
                        + cacheFile.getParentFile().getAbsolutePath());
            }
        }
        downloadFile(service, file, cacheFile);
        return cacheFile;
    }

    public static void renameFile(Drive service, File file, String newName) {
        java.io.File content = downloadFileToCache(service, file);
        updateFile(service, file.getId(), newName, file.getDescription(),
                file.getMimeType(), content);
        content.delete();
    }

    public static void updateFile(Drive service, File file, java.io.File localFile) {
        updateFile(service, file.getId(), file.getTitle(), file.getDescription(),
                file.getMimeType(), localFile);
    }

    private static File updateFile(Drive service, String fileId, String newTitle,
                                   String newDescription, String newMimeType,
                                   java.io.File newFile) {
        try {
            // First retrieve the file from the API.
            File file = service.files().get(fileId).execute();

            // File's new metadata.
            file.setTitle(newTitle);
            file.setDescription(newDescription);
            file.setMimeType(newMimeType);

            // File's new content.
            FileContent mediaContent = new FileContent(newMimeType, newFile);

            // Send the request to the API.
            return service.files().update(fileId, file, mediaContent).execute();

        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
            return null;
        }
    }

    public static void deleteFile(Drive service, String fileId) {
        try {
            service.files().delete(fileId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public static void watchFile(final java.io.File file, final File dFile, Drive service) {
        FileObserver observer = new FileObserver(file.getAbsolutePath()) {
            @Override
            public void onEvent(int i, String s) {
                if (i == FileObserver.MODIFY) {
                    try {
                        FileInputStream inputStream = new FileInputStream(file);
                        mAPI.delete(entry.path);
                        mAPI.putFile(entry.path, inputStream, file.length(), null, null);
                        inputStream.close();
                    } catch (IOException|DropboxException e) {
                        // ignore
                    }
                }
            }
        };
        observer.startWatching();
    }*/

    public static File uploadFile(Drive service, java.io.File file, String parentId) {
        return insertFile(service, file.getName(), "", parentId, MimeUtils.getMimeType(file.getName()),
                file.getAbsolutePath());
    }

    public static File createFile(Drive service, String title, String description,
                                  String parentId, String mimeType) {
        // File's metadata
        File body = new File();
        body.setTitle(title);
        body.setDescription(description);
        body.setMimeType(mimeType);

        // Set the parent folder
        if (parentId != null && parentId.length() > 0) {
            body.setParents(Arrays.asList(new ParentReference().setId(parentId)));
        }

        try {
            return service.files().insert(body).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File insertFile(Drive service, String title, String description,
                                  String parentId, String mimeType, String filename) {
        // File's metadata.
        File body = new File();
        body.setTitle(title);
        body.setDescription(description);
        body.setMimeType(mimeType);

        // Set the parent folder.
        if (parentId != null && parentId.length() > 0) {
            body.setParents(
                    Arrays.asList(new ParentReference().setId(parentId)));
        }

        // File's content.
        java.io.File fileContent = new java.io.File(filename);
        FileContent mediaContent = new FileContent(mimeType, fileContent);

        try {
            File file = service.files().insert(body, mediaContent).execute();

            // Uncomment the following line to print the File ID.
            // System.out.println("File ID: " + file.getId());

            return file;
        } catch (IOException e) {
            System.out.println("An error occured: " + e);
            return null;
        }
    }
}
