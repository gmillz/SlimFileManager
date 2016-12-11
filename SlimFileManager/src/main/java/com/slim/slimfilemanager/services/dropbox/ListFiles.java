package com.slim.slimfilemanager.services.dropbox;

import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

import java.util.ArrayList;

public class ListFiles extends AsyncTask<Void, Void, ArrayList<Entry>> {

    private DropboxAPI dropboxApi;
    private String path;
    private Callback callback;

    public ListFiles(DropboxAPI dropboxApi, String path, Callback callback) {
        this.dropboxApi = dropboxApi;
        this.path = path;
        this.callback = callback;
    }

    @Override
    protected ArrayList<Entry> doInBackground(Void... params) {
        ArrayList<Entry> files = new ArrayList<>();
        if (dropboxApi != null) {
            try {
                Entry directory = dropboxApi.metadata(path, 1000, null, true, null);
                for (Entry entry : directory.contents) {
                    files.add(entry);
                }
            } catch (DropboxException e) {
                e.printStackTrace();
            }
        }
        return files;
    }

    @Override
    protected void onPostExecute(ArrayList<Entry> result) {
        if (callback != null) {
            callback.filesListed(result);
        }
    }

    public interface Callback {
        void filesListed(ArrayList<Entry> entries);
    }
}
