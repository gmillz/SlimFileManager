package com.slim.slimfilemanager.utils

import android.content.Context
import android.text.TextUtils

import com.slim.slimfilemanager.settings.SettingsProvider
import com.slim.slimfilemanager.utils.file.BaseFile

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

object SortUtils {

    val SORT_MODE_NAME = "sort_mode_name"
    val SORT_MODE_SIZE = "sort_mode_size"
    val SORT_MODE_TYPE = "sort_mode_type"

    private val nameComparator: Comparator<BaseFile>
        get() = Comparator { lhs, rhs -> lhs.name.toLowerCase().compareTo(rhs.name.toLowerCase()) }

    private val sizeComparator: Comparator<BaseFile>
        get() = Comparator { lhs, rhs ->
            if (lhs.isDirectory && rhs.isDirectory) {
                var al = 0
                var bl = 0
                val aList = lhs.list()
                val bList = rhs.list()

                if (aList != null) {
                    al = aList.size
                }

                if (bList != null) {
                    bl = bList.size
                }

                if (al == bl) {
                    return@Comparator lhs.name.toLowerCase().compareTo(rhs.name.toLowerCase())
                }

                return@Comparator if (al < bl) {
                    -1
                } else 1
            }

            if (rhs.isDirectory) {
                return@Comparator -1
            }

            if (lhs.isDirectory) {
                return@Comparator 1
            }

            val len_a = rhs.length()
            val len_b = lhs.length()

            if (len_a == len_b) {
                return@Comparator lhs.name.toLowerCase().compareTo(rhs.name.toLowerCase())
            }

            if (len_a < len_b) {
                -1
            } else 1
        }

    private val typeComparator: Comparator<BaseFile>
        get() = Comparator { lhs, rhs ->
            if (lhs.isDirectory && rhs.isDirectory) {
                return@Comparator lhs.name.toLowerCase().compareTo(rhs.name.toLowerCase())
            }

            if (lhs.isDirectory) {
                return@Comparator -1
            }

            if (rhs.isDirectory) {
                return@Comparator 1
            }

            val ext_a = lhs.extension
            val ext_b = rhs.extension

            if (TextUtils.isEmpty(ext_a) && TextUtils.isEmpty(ext_b)) {
                return@Comparator lhs.name.toLowerCase().compareTo(rhs.name.toLowerCase())
            }

            if (TextUtils.isEmpty(ext_a)) {
                return@Comparator -1
            }

            if (TextUtils.isEmpty(ext_b)) {
                return@Comparator 1
            }

            val res = ext_a.compareTo(ext_b)
            if (res == 0) {
                lhs.name.toLowerCase().compareTo(rhs.name.toLowerCase())
            } else res
        }

    fun sort(context: Context?, files: ArrayList<BaseFile>) {
        if (context != null) {
            val sortMode = SettingsProvider.getString(context,
                    SettingsProvider.SORT_MODE, SORT_MODE_NAME)
            when (sortMode) {
                SORT_MODE_SIZE -> {
                    Collections.sort(files, sizeComparator)
                    return
                }
                SORT_MODE_TYPE -> {
                    Collections.sort(files, typeComparator)
                    return
                }
            }
        }
        Collections.sort(files, nameComparator)
    }
}