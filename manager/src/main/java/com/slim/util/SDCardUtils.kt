/* Copyright (c) 2014, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.slim.util

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.util.Log

import java.io.File
import java.lang.reflect.Method

class SDCardUtils private constructor(context: Context) {
    private var mStorageManager: StorageManager? = null
    private var mVolume: StorageVolume? = null
    private var path: String? = null

    val isWriteable: Boolean
        get() = mVolume != null && sdCardStorageState == Environment.MEDIA_MOUNTED

    val directory: String?
        get() {
            if (mVolume == null) {
                return null
            }
            if (path == null) {
                path = pathInternal
            }
            return path
        }

    private val pathInternal: String?
        get() {
            try {
                val m = mVolume!!.javaClass.getDeclaredMethod("getPath")
                return m.invoke(mVolume) as String
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

        }

    private val sdCardStorageState: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mVolume!!.state
        } else {
            Environment.MEDIA_MOUNTED
        }

    init {
        try {
            mStorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val volumeList = mStorageManager!!.javaClass.getDeclaredMethod("getVolumeList")
            val volumes = volumeList.invoke(mStorageManager) as Array<StorageVolume>
            if (volumes.size > VOLUME_SDCARD_INDEX) {
                mVolume = volumes[VOLUME_SDCARD_INDEX]
            }
        } catch (e: Exception) {
            Log.e(TAG, "couldn't talk to MountService", e)
        }

    }

    fun exists(): Boolean {
        return mVolume != null && File(directory!!).exists()
    }

    companion object {
        private val TAG = "SDCard"

        private val VOLUME_SDCARD_INDEX = 1
        private var sSDCard: SDCardUtils? = null

        fun initialize(context: Context) {
            if (sSDCard == null) {
                sSDCard = SDCardUtils(context)
            }
        }

        @Synchronized fun instance(): SDCardUtils? {
            return sSDCard
        }
    }

}