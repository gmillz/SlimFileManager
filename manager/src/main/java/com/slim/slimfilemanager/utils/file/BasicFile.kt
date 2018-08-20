package com.slim.slimfilemanager.utils.file

import android.content.Context

import com.slim.slimfilemanager.utils.FileUtil

import java.io.File

class BasicFile(private val mContext: Context, override val realFile: File) : BaseFile() {

    override val name: String
        get() = realFile.name

    override val isDirectory: Boolean
        get() = realFile.isDirectory

    override val realPath: String
        get() = getPath()

    override val parent: String
        get() = realFile.parent

    override val extension: String
        get() = FileUtil.getExtension(name)

    override fun delete(): Boolean {
        return FileUtil.deleteFile(mContext, realFile.absolutePath)
    }

    override fun exists(): Boolean {
        return realFile.exists()
    }

    override fun getPath(): String {
        return realFile.absolutePath
    }

    override fun getFile(callback: BaseFile.GetFileCallback) {
        callback.onGetFile(realFile)
    }

    override fun length(): Long {
        return FileUtil.getFileSize(realFile)
    }

    override fun list(): Array<String> {
        return realFile.list()
    }

    override fun lastModified(): Long {
        return realFile.lastModified()
    }
}
