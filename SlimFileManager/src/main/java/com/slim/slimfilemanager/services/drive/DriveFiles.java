package com.slim.slimfilemanager.services.drive;

import android.os.AsyncTask;
import android.util.ArrayMap;
import android.util.Log;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

public class DriveFiles extends HashMap<File, ArrayList<File>> {


    private ArrayMap<String, File> mIndex = new ArrayMap<>();
    private static DriveFiles INSTANCE;
    private static boolean sPopulated = false;

    private File mRootFile;

    static {
        if (INSTANCE == null) {
            INSTANCE = new DriveFiles();
        }
    }

    public static void populate(final Drive drive, Executor executor) {
        sPopulated = false;
        AsyncTask<Void, Void, Void> pop = new AsyncTask<Void, Void, Void>() {
            protected void onPreExecute() {
                INSTANCE.clear();
                INSTANCE.mIndex.clear();
            }
            protected Void doInBackground(Void... v) {
                Log.d("TEST", "TESTING");
                List<File> result = new ArrayList<>();
                try {
                    Drive.Files.List request = drive.files().list().setMaxResults(1000);

                    do {
                        try {
                            FileList fileList = request.execute();
                            result.addAll(fileList.getItems());
                            request.setPageToken(fileList.getNextPageToken());
                        } catch (IOException e) {
                            e.printStackTrace();
                            request.setPageToken(null);
                        }
                    } while (request.getPageToken() != null &&
                            request.getPageToken().length() > 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (File file : result) {
                    List<ParentReference> parents = file.getParents();
                    for (ParentReference parent : parents) {
                        Log.d("TEST", "file=" + file.getTitle());
                        if (!contains(parent.getId())) {
                            try {
                                File parentFile = drive.files().get(parent.getId()).execute();
                                if (parent.getIsRoot() && INSTANCE.mRootFile == null) {
                                    INSTANCE.mRootFile = parentFile;
                                }
                                put(parent.getId(), parentFile, file);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (!containsId(file.getId())) {
                                put(parent.getId(), file);
                            }
                        }
                    }
                }
                return null;
            }
            protected void onPostExecute(Void v) {
                sPopulated = true;
            }
        };
        if (executor == null) {
            pop.execute();
        } else {
            pop.executeOnExecutor(executor);
        }
    }

    public static boolean isPopulated() {
        return sPopulated;
    }

    public static void put(String index, File parentFile, File file) {
        ArrayList<File> files;
        if (INSTANCE.containsKey(parentFile)) {
            files = INSTANCE.get(parentFile);
        } else {
            files = new ArrayList<>();
        }
        if (!files.contains(file)) {
            files.add(file);
        }
        INSTANCE.put(parentFile, files);
        if (!INSTANCE.mIndex.containsValue(parentFile)) {
            INSTANCE.mIndex.put(index, parentFile);
        }
    }

    public static void put(String index, File file) {
        File parentFile = INSTANCE.mIndex.get(index);
        put(index, parentFile, file);
    }

    /*public Pair<File, File> get(String index) {
        File parentFile = mIndex.get(index);
        File file = get(parentFile);
        return new Pair<>(parentFile, file);
    }*/

    public static boolean containsId(String id) {
        for (ArrayList<File> arrays : INSTANCE.values()) {
            for (File file : arrays) {
                if (file.getId().equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean contains(String index) {
        if (!INSTANCE.mIndex.containsKey(index)) {
            for (File file : INSTANCE.mIndex.values()) {
                if (file.getTitle().equals(index)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public static ArrayList<File> getAll(String index) {
        ArrayList<File> files = new ArrayList<>();
        if (INSTANCE.mIndex.containsKey(index)) {
            File parentFile = INSTANCE.mIndex.get(index);
            if (INSTANCE.containsKey(parentFile)) {
                return INSTANCE.get(parentFile);
            }
        } else {
            if (contains(index)) {
                for (File parent : INSTANCE.mIndex.values()) {
                    if (parent.getTitle().equals(index)) {
                        if (INSTANCE.containsKey(parent)) {
                            return INSTANCE.get(parent);
                        }
                    }
                }
            }
        }
        return files;
    }

    public static String getPath(File file) {
        StringBuilder path = new StringBuilder();
        for (File f : INSTANCE.keySet()) {
            if (INSTANCE.get(f).contains(file)) {
                path.append(getPath(f));
                path.append("/");
                path.append(file.getTitle());
            }
        }
        return path.toString();
    }

    public static File get(String id) {
        for (File parent : INSTANCE.mIndex.values()) {
            if (parent.getId().equals(id)) {
                return parent;
            }
            for (File file : INSTANCE.get(parent)) {
                if (file.getId().equals(id)) {
                    return file;
                }
            }
        }
        return null;
    }

    public static String getRootId() {
        return INSTANCE.mRootFile.getId();
    }

    public static void insertFile(File file) {
        String parentId = file.getParents().get(0).getId();
        INSTANCE.get(INSTANCE.mIndex.get(parentId)).add(file);
    }
}
