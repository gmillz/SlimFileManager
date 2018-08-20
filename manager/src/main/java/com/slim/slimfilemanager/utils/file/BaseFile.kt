package com.slim.slimfilemanager.utils.file

import java.io.File

abstract class BaseFile {

    protected var filePath: String? = null

    abstract val name: String

    abstract val parent: String

    abstract val realPath: String

    abstract val isDirectory: Boolean

    abstract val extension: String

    abstract val realFile: Any

    val isFile: Boolean
        get() = !isDirectory

    abstract fun delete(): Boolean

    abstract fun exists(): Boolean

    abstract fun getPath(): String

    abstract fun getFile(callback: GetFileCallback)

    fun getFile(callback: (File) -> Unit) {
        getFile(callback)
    }

    abstract fun length(): Long

    abstract fun list(): Array<String>

    abstract fun lastModified(): Long

    interface GetFileCallback {
        fun onGetFile(file: File)
    }

    companion object {

        val blankFile: BaseFile
            get() = object : BaseFile() {

                override val name: String
                    get() = ""

                override val parent: String
                    get() = ""

                override val realPath: String
                    get() = ""

                override val isDirectory: Boolean
                    get() = false

                override val extension: String
                    get() = ""

                override val realFile: Any
                    get() = Any()

                override fun delete(): Boolean {
                    return false
                }

                override fun exists(): Boolean {
                    return false
                }

                override fun getPath(): String {
                    return ""
                }

                override fun getFile(callback: GetFileCallback) {}

                override fun length(): Long {
                    return 0
                }

                override fun list(): Array<String> {
                    return emptyArray()
                }

                override fun lastModified(): Long {
                    return 0
                }
            }
    }
}
