/*
 * Copyright (C) 2014 Vlad Mihalachi
 *
 * This file is part of Turbo Editor.
 *
 * Turbo Editor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Turbo Editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.slim.turboeditor.util;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.utils.FileUtil;
import com.slim.turboeditor.activity.MainActivity;
import com.slim.util.MediaStoreUtils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SaveFileTask extends AsyncTask<Void, Void, Void> {

    private final MainActivity activity;
    private final File mFile;
    private final Uri mUri;
    private final String newContent;
    private final String encoding;
    private String message;
    private String positiveMessage;
    private SaveFileInterface mCompletionHandler;

    public SaveFileTask(MainActivity activity, File file, Uri uri, String newContent,
                        String encoding, SaveFileInterface completionHandler) {
        this.activity = activity;
        mFile = file;
        mUri = uri;
        this.newContent = newContent;
        this.encoding = encoding;
        mCompletionHandler = completionHandler;

        execute();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        positiveMessage = String.format(
                activity.getString(R.string.file_saved_with_success),
                mFile != null ? mFile.getName() : mUri != null ?
                        MediaStoreUtils.getFileNameFromUri(activity, mUri) : "");
        message = positiveMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void doInBackground(final Void... voids) {
        try {
            if (mUri != null) {
                Log.d("TEST-SAVE", "mUri=" + mUri.toString());
                writeUri(mUri, newContent, encoding);
                return null;
            }
            String filePath = mFile.getAbsolutePath();
            // if the uri has no path
            if (TextUtils.isEmpty(filePath)) {
                writeUri(Uri.fromFile(mFile), newContent, encoding);
            } else {
                FileUtil.writeFile(activity, newContent, mFile, encoding);
            }
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
        }
        return null;
    }

    private void writeUri(Uri uri, String newContent, String encoding) throws IOException {
        //ParcelFileDescriptor pfd = activity.getContentResolver().openFileDescriptor(uri, "w");
        //FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
        //fileOutputStream.write(newContent.getBytes(Charset.forName(encoding)));
        //fileOutputStream.close();
        //pfd.close();

        Log.d("TEST", "content=" + newContent);
        Log.d("TEST", "encoding=" + encoding);

        OutputStream stream = activity.getContentResolver().openOutputStream(uri);
        InputStream is = IOUtils.toInputStream(newContent, encoding);
        IOUtils.copy(is, stream);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(stream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(final Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();

        /*android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Clip",message);
        clipboard.setPrimaryClip(clip);*/

        if (mCompletionHandler != null)
            mCompletionHandler.fileSaved(message.equals(positiveMessage));
    }

    public interface SaveFileInterface {
        void fileSaved(Boolean success);
    }
}