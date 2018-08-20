package com.slim.slimfilemanager.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.ImageView

import com.slim.slimfilemanager.R

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object IconCache {

    private var mCache: ConcurrentHashMap<String, Any>? = null

    private var mHandler: ImageHandler? = null

    private var mExecutor: ExecutorService? = null

    init {
        mCache = ConcurrentHashMap()
        mHandler = ImageHandler()
        mExecutor = Executors.newFixedThreadPool(6)
    }

    fun getIconForFile(context: Context, file: String, view: ImageView) {
        view.setImageBitmap(null)
        view.setImageDrawable(null)
        if (mCache!!.containsKey(file)) {
            setImage(view, mCache!![file])
            return
        }
        queueImage(context, File(file), view)
    }

    fun getImage(context: Context, file: File): Any? {
        val isPicture = MimeUtils.isPicture(file.name)
        val isVideo = MimeUtils.isVideo(file.name)
        val isApp = MimeUtils.isApp(file.name)

        val `object`: Any?
        val width = context.resources.getDimension(R.dimen.item_height).toInt()

        val path = file.path

        if (isPicture) {
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true

            BitmapFactory.decodeFile(path, o)
            o.inJustDecodeBounds = false

            if (o.outWidth != -1 && o.outHeight != -1) {
                val originalSize = if (o.outHeight > o.outWidth)
                    o.outWidth
                else
                    o.outHeight
                o.inSampleSize = originalSize / width
            }

            `object` = BitmapFactory.decodeFile(path, o)
        } else if (isVideo) {
            `object` = ThumbnailUtils.createVideoThumbnail(path,
                    MediaStore.Video.Thumbnails.MICRO_KIND)
        } else if (isApp) {
            val pm = context.packageManager
            val packageInfo = pm.getPackageArchiveInfo(path,
                    PackageManager.GET_ACTIVITIES)

            if (packageInfo != null) {

                val appInfo = packageInfo.applicationInfo
                appInfo.sourceDir = path
                appInfo.publicSourceDir = path

                return pm.getDrawable(appInfo.packageName, appInfo.icon, appInfo)
            } else {
                return context.getDrawable(
                        android.R.drawable.sym_def_app_icon)
            }
        } else if (file.isDirectory) {
            if (file.list() != null && file.list().size > 0) {
                `object` = context.getDrawable(R.drawable.folder)
            } else {
                `object` = context.getDrawable(R.drawable.empty_folder)
            }
        } else if (MimeUtils.isTextFile(file.name)) {
            `object` = context.getDrawable(R.drawable.text)
        } else {
            `object` = context.getDrawable(R.drawable.file)
        }
        return `object`
    }

    fun queueImage(context: Context, file: File, view: ImageView) {
        mExecutor!!.submit {
            val o = getImage(context, file)
            mCache!![file.path] = o!!
            val message = Message.obtain()
            val data = Bundle()
            data.putString("key", file.path)
            message.data = data
            message.obj = view
            mHandler!!.sendMessage(message)
        }
    }

    fun setImage(view: ImageView, o: Any?) {
        if (o is Drawable) {
            view.setImageDrawable(o as Drawable?)
        } else if (o is Bitmap) {
            view.setImageBitmap(o as Bitmap?)
        }
    }

    fun clearCache() {
        mCache!!.clear()
    }

    private class ImageHandler : Handler() {
        override fun handleMessage(msg: Message) {
            val bundle = msg.data
            if (bundle != null) {
                val key = bundle.getString("key")
                if (!TextUtils.isEmpty(key)) {
                    if (msg.obj != null) {
                        setImage(msg.obj as ImageView, mCache!![key])
                    }
                }
            }
        }
    }
}
