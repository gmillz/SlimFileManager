package com.slim.slimfilemanager.utils

import android.content.Context
import android.os.Build
import android.support.v4.provider.DocumentFile
import android.text.TextUtils
import android.util.Log

import com.slim.slimfilemanager.settings.SettingsProvider

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils

import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream

object FileUtil {

    fun copyFile(context: Context, f: String, fol: String): Boolean {
        val file = File(f)
        val folder = File(fol)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var fis: FileInputStream? = null
            var os: OutputStream? = null
            try {
                fis = FileInputStream(file)
                val target = DocumentFile.fromFile(folder)
                os = context.contentResolver.openOutputStream(target.uri)
                IOUtils.copy(fis, os)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fis?.close()
                    if (os != null) {
                        os.flush()
                        os.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }

        if (file.exists()) {
            if (!folder.exists()) {
                if (!mkdir(context, folder)) return false
            }
            try {
                if (file.isDirectory) {
                    FileUtils.copyDirectoryToDirectory(file, folder)
                } else if (file.isFile) {
                    FileUtils.copyFileToDirectory(file, folder)
                }
                return true
            } catch (e: IOException) {
                return (SettingsProvider.getBoolean(context, SettingsProvider.KEY_ENABLE_ROOT,
                        false)
                        && RootUtils.isRootAvailable && RootUtils.copyFile(f, fol))
            }

        } else {
            return false
        }
    }

    fun moveFile(context: Context, source: String, destination: String): Boolean {
        if (TextUtils.isEmpty(source) || TextUtils.isEmpty(destination)) {
            return false
        }
        val file = File(source)
        val folder = File(destination)

        try {
            FileUtils.moveFileToDirectory(file, folder, true)
            return true
        } catch (e: IOException) {
            return (SettingsProvider.getBoolean(context, SettingsProvider.KEY_ENABLE_ROOT, false)
                    && RootUtils.isRootAvailable && RootUtils.moveFile(source, destination))
        }

    }

    fun deleteFile(context: Context, path: String): Boolean {
        try {
            FileUtils.forceDelete(File(path))
            return true
        } catch (e: IOException) {
            return (SettingsProvider.getBoolean(context, SettingsProvider.KEY_ENABLE_ROOT, false)
                    && RootUtils.isRootAvailable && RootUtils.deleteFile(path))
        }

    }

    fun getFileProperties(context: Context, file: File): Array<String>? {
        var info: Array<String>? = null

        val out: RootUtils.CommandOutput?
        if (SettingsProvider.getBoolean(context, SettingsProvider.KEY_ENABLE_ROOT,
                        false) && RootUtils.isRootAvailable) {
            out = RootUtils.runCommand("ls -l " + file.absolutePath)
        } else {
            out = runCommand("ls -l " + file.absolutePath)
        }
        if (out == null) return null
        if (TextUtils.isEmpty(out.error) && out.exitCode == 0) {
            info = getAttrs(out.output!!)
        }
        return info
    }

    private fun getAttrs(string: String): Array<String> {
        if (string.length < 44) {
            throw IllegalArgumentException("Bad ls -l output: $string")
        }
        val chars = string.toCharArray()

        val results = emptyArray<String>()
        var ind = 0
        val current = StringBuilder()

        Loop@ for (i in chars.indices) {
            when (chars[i]) {
                ' ', '\t' -> if (current.length != 0) {
                    results[ind] = current.toString()
                    ind++
                    current.setLength(0)
                    if (ind == 10) {
                        results[ind] = string.substring(i).trim({ it <= ' ' })
                        break@Loop
                    }
                }

                else -> current.append(chars[i])
            }
        }

        return results
    }

    fun runCommand(cmd: String): RootUtils.CommandOutput {
        val output = RootUtils.CommandOutput()
        try {
            val process = Runtime.getRuntime().exec("sh")
            val os = DataOutputStream(
                    process.outputStream)
            os.writeBytes(cmd + "\n")
            os.writeBytes("exit\n")
            os.flush()

            output.exitCode = process.waitFor()
            output.output = IOUtils.toString(process.inputStream)
            output.error = IOUtils.toString(process.errorStream)

            if (output.exitCode != 0 || !TextUtils.isEmpty(output.error)) {
                Log.e("Shell Error, cmd: $cmd", "error: " + output.error!!)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return output
    }

    fun changeGroupOwner(context: Context, file: File, owner: String, group: String): Boolean {
        try {
            var useRoot = false
            if (!file.canWrite() && SettingsProvider.getBoolean(context,
                            SettingsProvider.KEY_ENABLE_ROOT, false)
                    && RootUtils.isRootAvailable) {
                useRoot = true
                RootUtils.remountSystem("rw")
            }

            if (useRoot) {
                RootUtils.runCommand("chown " + owner + "." + group + " "
                        + file.absolutePath)
            } else {
                runCommand("chown " + owner + "." + group + " "
                        + file.absolutePath)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun applyPermissions(context: Context, file: File, permissions: Permissions): Boolean {
        try {
            var useSu = false
            if (!file.canWrite() && SettingsProvider.getBoolean(context,
                            SettingsProvider.KEY_ENABLE_ROOT, false)
                    && RootUtils.isRootAvailable) {
                useSu = true
                RootUtils.remountSystem("rw")
            }

            if (useSu) {
                RootUtils.runCommand("chmod " + permissions.octalPermissions + " "
                        + file.absolutePath)
            } else {
                runCommand("chmod " + permissions.octalPermissions + " "
                        + file.absolutePath)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun mkdir(context: Context, dir: File): Boolean {
        if (dir.exists()) {
            return false
        }
        if (dir.mkdirs()) {
            return true
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val document = DocumentFile.fromFile(dir.parentFile)
                if (document.exists()) {
                    if (document.createDirectory(dir.absolutePath)!!.exists()) {
                        return true
                    }
                }
            }
        }

        return (SettingsProvider.getBoolean(context,
                SettingsProvider.KEY_ENABLE_ROOT, false) && RootUtils.isRootAvailable
                && RootUtils.createFolder(dir))
    }

    fun getExtension(fileName: String): String {
        return FilenameUtils.getExtension(fileName)
    }

    fun removeExtension(s: String): String {
        return FilenameUtils.removeExtension(File(s).name)
    }

    fun writeFile(context: Context, content: String, file: File, encoding: String) {
        if (file.canWrite()) {
            try {
                FileUtils.write(file, content, encoding)
            } catch (e: IOException) {
                // ignore
            }

        } else if (SettingsProvider.getBoolean(context, SettingsProvider.KEY_ENABLE_ROOT, false)) {
            RootUtils.writeFile(file, content)
        }
    }

    fun renameFile(context: Context, oldFile: File, newFile: File) {
        if (oldFile.renameTo(newFile)) {
            return
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val document = DocumentFile.fromFile(oldFile)
                if (document.renameTo(newFile.absolutePath)) {
                    return
                }
            }
        }
        if (SettingsProvider.getBoolean(context, SettingsProvider.KEY_ENABLE_ROOT, false)) {
            RootUtils.renameFile(oldFile, newFile)
        }
    }

    fun getFileSize(file: File): Long {
        return file.length()
    }
}
