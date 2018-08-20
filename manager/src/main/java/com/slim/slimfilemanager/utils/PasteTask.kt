package com.slim.slimfilemanager.utils

import android.app.AlertDialog
import android.content.Context
import android.support.annotation.IdRes
import android.view.View
import android.widget.TextView

import com.slim.slimfilemanager.R
import com.slim.slimfilemanager.utils.file.BaseFile

import java.io.File
import java.util.ArrayList
import java.util.HashMap

class PasteTask(private val mContext: Context,
                private val mMove: Boolean, location: String, private val mCallback: Callback?) :
        View.OnClickListener {
    private var mCurrent: BaseFile? = null
    private var mDialog: AlertDialog? = null
    private val mExistingFiles = HashMap<File, BaseFile>()
    private val mProcess = ArrayList<BaseFile>()

    init {

        val files = ArrayList<BaseFile>()
        files.addAll(SelectedFiles.files)

        for (i in files.indices) {
            val file = files[i]
            if (file.exists()) {
                val newFile = File(location + File.separator + file.name)
                if (newFile.exists()) {
                    mExistingFiles[newFile] = file
                } else {
                    mProcess.add(file)
                }
            }
        }
        processFiles()
    }

    private fun processFiles() {
        if (mExistingFiles.isEmpty()) {
            if (mProcess.isEmpty()) return
            mCallback?.pasteFiles(mProcess, mMove)
        } else {
            val key = mExistingFiles.keys.iterator().next()
            mCurrent = mExistingFiles[key]
            mExistingFiles.remove(key)
            val builder = AlertDialog.Builder(mContext)
            val view = View.inflate(mContext, R.layout.file_exists_dialog, null)

            val ids = intArrayOf(R.id.skip, R.id.skip_all, R.id.replace, R.id.replace_all,
                    R.id.cancel)
            for (id in ids) {
                view.findViewById<View>(id).setOnClickListener(this)
            }

            (view.findViewById<View>(R.id.source) as TextView).text = mCurrent!!.getPath()
            (view.findViewById<View>(R.id.destination) as TextView).text = key.path

            builder.setView(view)
            mDialog = builder.create()
            mDialog!!.show()
        }
    }

    override fun onClick(view: View) {
        if (mDialog != null) mDialog!!.dismiss()

        if (view.id == R.id.skip_all) {
            mExistingFiles.clear()
        } else if (view.id == R.id.replace) {
            mProcess.add(mCurrent!!)
        } else if (view.id == R.id.replace_all) {
            mProcess.add(mCurrent!!)
            for (f in mExistingFiles.keys) {
                mProcess.add(mExistingFiles[f]!!)
            }
            mExistingFiles.clear()
        } else if (view.id == R.id.cancel) {
            mExistingFiles.clear()
            mProcess.clear()
        }
        processFiles()
    }

    interface Callback {
        fun pasteFiles(paths: ArrayList<BaseFile>, move: Boolean)
    }

    object SelectedFiles {
        val files = ArrayList<BaseFile>()

        val isEmpty: Boolean
            get() = files.isEmpty()

        fun addFile(file: BaseFile) {
            files.add(file)
        }

        fun clearAll() {
            files.clear()
        }
    }
}
