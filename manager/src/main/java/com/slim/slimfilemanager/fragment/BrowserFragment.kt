package com.slim.slimfilemanager.fragment

import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.widget.ImageView
import android.widget.Toast

import com.slim.slimfilemanager.R
import com.slim.slimfilemanager.settings.SettingsProvider
import com.slim.slimfilemanager.utils.FileUtil
import com.slim.slimfilemanager.utils.IconCache
import com.slim.slimfilemanager.utils.MimeUtils
import com.slim.slimfilemanager.utils.PasteTask
import com.slim.slimfilemanager.utils.RootUtils
import com.slim.slimfilemanager.utils.Utils
import com.slim.slimfilemanager.utils.file.BaseFile
import com.slim.slimfilemanager.utils.file.BasicFile
import com.slim.util.Constant

import org.apache.commons.io.FileUtils

import java.io.File
import java.io.IOException
import java.util.ArrayList

class BrowserFragment : BaseBrowserFragment() {

    override val defaultDirectory: String
        get() = Environment.getExternalStorageDirectory().absolutePath

    override val pasteCallback: PasteTask.Callback
        get() = object : PasteTask.Callback {
            override fun pasteFiles(paths: ArrayList<BaseFile>, move: Boolean) {
                showProgressDialog(if (move) R.string.move else R.string.copy, true)
                for (f in paths) {
                    f.getFile { file ->
                        val passed: Boolean
                        if (move) {
                            passed = FileUtil.moveFile(
                                    mContext, file.absolutePath, currentPath)
                        } else {
                            passed = FileUtil.copyFile(
                                    mContext, file.absolutePath, currentPath)
                        }
                        if (!passed) {
                            toast("Failed to paste file - " + file.name)
                        }
                    }
                }
                hideProgressDialog()
            }
        }

    override val rootFolder: String
        get() = if (SettingsProvider.getBoolean(mContext, SettingsProvider.KEY_ENABLE_ROOT,
                        false) && RootUtils.isRootAvailable) {
            File.separator
        } else {
            Environment.getExternalStorageDirectory().absolutePath
        }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        ACTIONS.add(BaseBrowserFragment.MENU_CUT)
        ACTIONS.add(BaseBrowserFragment.MENU_PERMISSIONS)
        ACTIONS.add(BaseBrowserFragment.MENU_ARCHIVE)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        filesChanged(currentPath)
    }

    override fun getTabTitle(path: String): String {
        if (TextUtils.isEmpty(path)) return ""
        val file = File(path)
        var title = file.name
        if (file.absolutePath == "/") {
            title = "ROOT"
        } else if (file.absolutePath == Environment.getExternalStorageDirectory().absolutePath) {
            title = "SDCARD"
        }
        return title
    }

    override fun onClickFile(file: String) {
        if (TextUtils.isEmpty(file)) {
            return
        }
        val f = File(file)
        if (f.exists()) {
            if (f.isDirectory) {
                filesChanged(file)
            } else {
                fileClicked(File(file))
            }
        }
    }

    override fun getFile(i: Int): BaseFile {
        return mFiles[i]
    }

    override fun filesChanged(file: String) {
        if (TextUtils.isEmpty(file)) return
        super.filesChanged(file)
        if (mExitOnBack) mExitOnBack = false
        val newPath = File(file)
        if (!newPath.exists()) {
            return
        }
        if (!newPath.canRead() && !RootUtils.isRootAvailable) {
            Toast.makeText(mContext, "Root is required to view folder.", Toast.LENGTH_SHORT).show()
        }
        val files = Utils.listFiles(file) ?: return
        setPathText(File(file).absolutePath)
        currentPath = file
        if (!mFiles.isEmpty()) mFiles.clear()
        mAdapter!!.notifyDataSetChanged()
        for (s in files) {
            if (mPicking && !TextUtils.isEmpty(mMimeType) && mMimeType.startsWith("image/")
                    && !MimeUtils.isPicture(File(s).name)
                    && File(s).isFile)
                continue
            val bf = BasicFile(mContext, File(s))
            mFiles.add(bf)
            sortFiles()
            mAdapter!!.notifyItemInserted(mFiles.indexOf(bf))
        }
        mRecyclerView.scrollToPosition(0)
        hideProgress()
    }

    override fun onDeleteFile(file: String) {
        if (FileUtil.deleteFile(mContext, file)) {
            removeFile(file)
        } else {
            Toast.makeText(mContext,
                    "Failed to delete file: " + File(file).name,
                    Toast.LENGTH_SHORT).show()
        }
    }

    override fun backPressed() {
        if (TextUtils.isEmpty(currentPath)) return
        filesChanged(File(currentPath).parent)
    }

    override fun getIconForFile(imageView: ImageView, position: Int) {
        IconCache.getIconForFile(mContext, mFiles[position].realPath, imageView)
    }

    override fun addNewFile(name: String, isFolder: Boolean) {
        val newFile = File(currentPath + File.separator + name)
        if (newFile.exists()) {
            toast(if (isFolder) R.string.folder_exists else R.string.file_exists)
            return
        }

        var success = true
        if (isFolder) {
            if (!newFile.mkdirs()) {
                if (SettingsProvider.getBoolean(mContext, SettingsProvider.KEY_ENABLE_ROOT, false)
                        && RootUtils.isRootAvailable && !RootUtils.createFolder(newFile)) {
                    success = false
                }
            }
        } else {
            try {
                if (!newFile.exists()) {
                    if (newFile.parentFile.canWrite()) {
                        FileUtils.writeStringToFile(newFile, "",
                                SettingsProvider.getString(mContext,
                                        SettingsProvider.EDITOR_ENCODING,
                                        Constant.DEFAULT_ENCODING))
                        if (!newFile.exists()) {
                            success = false
                        }
                    } else if (SettingsProvider.getBoolean(mContext,
                                    SettingsProvider.KEY_ENABLE_ROOT, false) &&
                            RootUtils.isRootAvailable && !RootUtils.createFile(newFile)) {
                        success = false
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        if (success) {
            filesChanged(currentPath)
        } else {
            toast(if (isFolder) R.string.unable_to_create_folder else R.string.unable_to_create_file)
        }
    }

    override fun addFile(path: String) {
        val bf = BasicFile(mContext, File(path))
        mFiles.add(bf)
        sortFiles()
        mAdapter!!.notifyItemInserted(mFiles.indexOf(bf))
    }

    override fun renameFile(file: BaseFile, name: String) {
        val newFile = File(file.parent + File.separator + name)
        FileUtil.renameFile(mContext, file.realFile as File, newFile)
        removeFile(file.realPath)
        addFile(newFile.absolutePath)
    }

    companion object {

        fun newInstance(path: String): BaseBrowserFragment {
            val fragment = BrowserFragment()
            val args = Bundle()
            args.putString(BaseBrowserFragment.ARG_PATH, path)
            fragment.arguments = args
            return fragment
        }
    }
}
