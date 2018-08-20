package com.slim.slimfilemanager.utils

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Environment

import java.io.File

class BackgroundUtils(internal var mContext: Context, internal var mFile: String,
                      internal var mId: Int) : AsyncTask<Void, Void, String>() {
    internal lateinit var mDialog: ProgressDialog

    override fun onPreExecute() {
        when (mId) {
            UNZIP_FILE -> mDialog =
                    ProgressDialog.show(mContext, "Unzipping", "Please wait...", true, false)
            ZIP_FILE -> mDialog =
                    ProgressDialog.show(mContext, "Zipping", "Please wait...", true, false)
            UNTAR_FILE -> mDialog =
                    ProgressDialog.show(mContext, "Untarring", "Please wait...", true, false)
            TAR_FILE, TAR_COMPRESS -> mDialog =
                    ProgressDialog.show(mContext, "Tarring", "Please wait...", true, false)
        }
    }

    override fun doInBackground(vararg v: Void): String? {
        when (mId) {
            UNZIP_FILE -> return ArchiveUtils.extractZipFiles(mFile, EXTRACTED_LOCATION)
            ZIP_FILE -> return ArchiveUtils.createZipFile(mFile,
                    PasteTask.SelectedFiles.files)
            UNTAR_FILE -> return ArchiveUtils.unTar(mContext, mFile, EXTRACTED_LOCATION)
            TAR_FILE -> return ArchiveUtils.createTar(mFile, PasteTask.SelectedFiles.files)
            TAR_COMPRESS -> return ArchiveUtils.createTarGZ(mFile, PasteTask.SelectedFiles.files)
        }
        return null
    }

    override fun onPostExecute(v: String) {
        mDialog.dismiss()

    }

    companion object {

        val UNZIP_FILE = 1001001
        val ZIP_FILE = 1001002

        val UNTAR_FILE = 1001003
        val TAR_FILE = 1001004
        val TAR_COMPRESS = 1001005

        val EXTRACTED_LOCATION = (Environment.getExternalStorageDirectory().toString()
                + File.separator + "Slim" + File.separator + "Extracted")
        val ARCHIVE_LOCATION = (Environment.getExternalStorageDirectory().toString()
                + File.separator + "Slim" + File.separator + "Archived")

        init {
            if (!File(EXTRACTED_LOCATION).exists()) {
                File(EXTRACTED_LOCATION).mkdirs()
            }
            if (!File(ARCHIVE_LOCATION).exists()) {
                File(ARCHIVE_LOCATION).mkdirs()
            }
        }
    }
}
