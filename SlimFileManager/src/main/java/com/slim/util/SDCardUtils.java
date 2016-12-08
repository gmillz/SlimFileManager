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

package com.slim.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageVolume;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;

public class SDCardUtils {
    private static final String TAG = "SDCard";

    private static final int VOLUME_SDCARD_INDEX = 1;

    private StorageManager mStorageManager = null;
    private StorageVolume mVolume = null;
    private String path = null;
    private static SDCardUtils sSDCard;

    public boolean isWriteable() {
        return mVolume != null && getSDCardStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public boolean exists() {
        return mVolume != null && new File(getDirectory()).exists();
    }

    public String getDirectory() {
        if (mVolume == null) {
            return null;
        }
        if (path == null) {
            path = getPathInternal();
        }
        return path;
    }

    private String getPathInternal() {
        try {
            Method m = mVolume.getClass().getDeclaredMethod("getPath");
            return (String) m.invoke(mVolume);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void initialize(Context context) {
        if (sSDCard == null) {
            sSDCard = new SDCardUtils(context);
        }
    }

    public static synchronized SDCardUtils instance() {
        return sSDCard;
    }

    private String getSDCardStorageState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return mVolume.getState();
        } else {
            return Environment.MEDIA_MOUNTED;
        }
    }

    private SDCardUtils(Context context) {
        try {
            mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method volumeList = mStorageManager.getClass().getDeclaredMethod("getVolumeList");
            final StorageVolume[] volumes = (StorageVolume[]) volumeList.invoke(mStorageManager);
            if (volumes.length > VOLUME_SDCARD_INDEX) {
                mVolume = volumes[VOLUME_SDCARD_INDEX];
            }
        } catch (Exception e) {
            Log.e(TAG, "couldn't talk to MountService", e);
        }
    }

}