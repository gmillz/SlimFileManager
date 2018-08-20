package com.slim.slimfilemanager.utils

import android.text.TextUtils
import android.util.Log

import org.apache.commons.io.IOUtils

import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.regex.Pattern

object RootUtils {

    internal val sEscape = Pattern.compile("([\"\'`\\\\])")

    val isRootAvailable: Boolean
        get() {
            var output: CommandOutput? = runCommand("id")
            if (output != null && TextUtils.isEmpty(output.error) && output.exitCode == 0) {
                return output.output != null && output.output!!.contains("uid=0")
            } else {
                output = runCommand("echo _TEST_")
                return output != null && output.output!!.contains("_TEST_")
            }
        }

    fun listFiles(path: String, showHidden: Boolean): ArrayList<String>? {
        if (!isRootAvailable) return null
        val mDirContent = ArrayList<String>()
        val output = runCommand("ls -a \"$path\"\n") ?: return mDirContent
        val split =
                output.output!!.split("\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        for (line in split) {
            if (!showHidden) {
                if (line.get(0) != '.')
                    mDirContent.add("$path/$line")
            } else {
                mDirContent.add("$path/$line")
            }
        }
        return mDirContent
    }

    fun runCommand(cmd: String): CommandOutput {
        val output = CommandOutput()
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(
                    process.outputStream)
            os.writeBytes(cmd + "\n")
            os.writeBytes("exit\n")
            os.flush()

            output.exitCode = process.waitFor()
            output.output = IOUtils.toString(process.inputStream)
            output.error = IOUtils.toString(process.errorStream)

            if (output.exitCode != 0 || !TextUtils.isEmpty(output.error)) {
                Log.e("Root Error, cmd: $cmd", "error: " + output.error!!)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return output
    }

    fun copyFile(old: String, newDir: String): Boolean {
        if (!isRootAvailable) return false
        try {
            var remounted = false
            if (!File(newDir).canWrite()) {
                remounted = true
                remountSystem("rw")
            }
            runCommand("cp -fr $old $newDir")
            if (remounted) {
                remountSystem("ro")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    fun moveFile(old: String, newDir: String): Boolean {
        if (!isRootAvailable) return false
        try {
            remountSystem("rw")
            runCommand("mv -f $old $newDir")
            remountSystem("ro")
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return File(newDir).exists()
    }

    fun remountSystem(mountType: String): Boolean {
        if (!isRootAvailable) return false
        val output = runCommand("mount -o remount,$mountType /system \n")
        return output != null && output.exitCode == 0 && TextUtils.isEmpty(output.error)
    }

    fun deleteFile(path: String): Boolean {
        if (!isRootAvailable) return false
        try {
            remountSystem("rw")

            if (File(path).isDirectory) {
                runCommand("rm -rf '$path'\n")
            } else {
                runCommand("rm -rf '$path'\n")
            }

            remountSystem("ro")
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return !File(path).exists()
    }

    fun createFile(file: File): Boolean {
        if (!isRootAvailable) return false
        remountSystem("rw")
        runCommand("touch " + file.absolutePath)
        remountSystem("ro")
        return true
    }

    fun createFolder(folder: File): Boolean {
        if (!isRootAvailable) return false
        remountSystem("rw")
        runCommand("mkdir " + folder.path)
        remountSystem("ro")
        return true
    }

    fun writeFile(file: File, content: String) {
        if (!isRootAvailable) return
        var redirect = ">"
        val input = content.trim { it <= ' ' }.split("\n".toRegex()).dropLastWhile({ it.isEmpty() })
                .toTypedArray()
        remountSystem("rw")
        for (line in input) {
            val l = sEscape.matcher(line).replaceAll("\\\\$1")
            runCommand("echo '" + l + "' " + redirect + " '" + file.absolutePath + "' ")
            redirect = ">>"
        }
    }

    fun readFile(file: File): String? {
        val out = runCommand("cat " + file.absolutePath)
        return out?.output
    }

    fun renameFile(oldFile: File, newFile: File) {
        remountSystem("rw")
        runCommand("mv " + oldFile.absolutePath + " " + newFile.absolutePath + "\n")
        remountSystem("ro")
    }

    class CommandOutput {
        internal var output: String? = null
        internal var error: String? = null
        internal var exitCode: Int = 0
    }
}
