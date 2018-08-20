package com.slim.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

object MediaStoreUtils {

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return ""
        try {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            return cursor.getString(nameIndex)
        } finally {
            cursor.close()
        }
    }
}
