package com.slim.slimfilemanager.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.content.FileProvider
import android.widget.Toast

import com.slim.slimfilemanager.R

import java.io.File
import java.math.BigInteger
import java.util.ArrayList

object Utils {

    val ONE_KB: Long = 1024
    val ONE_KB_BI = BigInteger.valueOf(ONE_KB)
    val ONE_MB_BI = ONE_KB_BI.multiply(ONE_KB_BI)
    val ONE_GB_BI = ONE_KB_BI.multiply(ONE_MB_BI)
    val ONE_TB_BI = ONE_KB_BI.multiply(ONE_GB_BI)
    val ONE_PB_BI = ONE_KB_BI.multiply(ONE_TB_BI)
    val ONE_EB_BI = ONE_KB_BI.multiply(ONE_PB_BI)

    val cacheDir: String?
        get() {
            val cache =
                    File(Environment.getExternalStorageDirectory().toString() + "/.file_manager_cache")
            if (!cache.exists()) {
                if (!cache.mkdirs()) return null
            }
            return cache.absolutePath
        }

    fun listFiles(path: String): ArrayList<String>? {
        var mDirContent: ArrayList<String>? = ArrayList()

        if (!mDirContent!!.isEmpty())
            mDirContent.clear()

        val file = File(path)

        if (file.exists() && file.canRead()) {
            val list = file.list() ?: return null

            for (aList in list) {
                mDirContent.add("$path/$aList")
            }
        } else {
            mDirContent = RootUtils.listFiles(file.absolutePath, true)
        }

        return mDirContent
    }

    fun onClickFile(context: Context, fileString: String) {
        val file = File(fileString)
        val mime = getMimeType(file)
        val i = Intent(Intent.ACTION_VIEW)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            i.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val uri = FileProvider.getUriForFile(context, "com.slim.slimfilemanager.fileprovider",
                    file)
            if (mime != null) {
                i.setDataAndType(uri, mime)
            } else {
                i.setDataAndType(uri, "*/*")
            }
        } else {
            if (mime != null) {
                i.setDataAndType(Uri.fromFile(file), mime)
            } else {
                i.setDataAndType(Uri.fromFile(file), "*/*")
            }
        }

        if (context.packageManager.queryIntentActivities(i, 0).isEmpty()) {
            Toast.makeText(context, R.string.cant_open_file, Toast.LENGTH_SHORT).show()
            return
        }

        try {
            context.startActivity(i)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getMimeType(file: File): String? {
        return if (file.isDirectory) {
            null
        } else MimeUtils.getMimeType(file.name)
    }

    fun displaySize(bytes: Long): String {
        val displaySize: String

        val size = BigInteger.valueOf(bytes)

        if (size.divide(ONE_EB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_EB_BI).toString() + " EB"
        } else if (size.divide(ONE_PB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_PB_BI).toString() + " PB"
        } else if (size.divide(ONE_TB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_TB_BI).toString() + " TB"
        } else if (size.divide(ONE_GB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_GB_BI).toString() + " GB"
        } else if (size.divide(ONE_MB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_MB_BI).toString() + " MB"
        } else if (size.divide(ONE_KB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_KB_BI).toString() + " KB"
        } else {
            displaySize = size.toString() + " bytes"
        }
        return displaySize
    }

    fun darkenColor(color: Int): Int {
        val r = Color.red(color)
        val b = Color.blue(color)
        val g = Color.green(color)

        return Color.rgb((r * .9).toInt(), (g * .9).toInt(), (b * .9).toInt())
    }

    fun convertToARGB(color: Int): String {
        var alpha = Integer.toHexString(Color.alpha(color))
        var red = Integer.toHexString(Color.red(color))
        var green = Integer.toHexString(Color.green(color))
        var blue = Integer.toHexString(Color.blue(color))

        if (alpha.length == 1) {
            alpha = "0$alpha"
        }
        if (red.length == 1) {
            red = "0$red"
        }
        if (green.length == 1) {
            green = "0$green"
        }
        if (blue.length == 1) {
            blue = "0$blue"
        }

        return "#$alpha$red$blue$green"
    }
}
