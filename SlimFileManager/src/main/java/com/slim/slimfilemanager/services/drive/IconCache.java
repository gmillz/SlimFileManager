package com.slim.slimfilemanager.services.drive;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.utils.Utils;
import com.slim.slimfilemanager.utils.file.DriveFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IconCache {

    private static ConcurrentHashMap<String, Object> mCache;

    private static ImageHandler mHandler;

    private static ExecutorService mExecutor;

    static {
        mCache = new ConcurrentHashMap<>();
        mHandler = new ImageHandler();
        mExecutor = Executors.newFixedThreadPool(6);
    }

    public static void getIconForFile(Context context, Drive drive, File file, ImageView view) {
        view.setImageBitmap(null);
        view.setImageDrawable(null);
        if (mCache.containsKey(file.getId())) {
            setImage(view, mCache.get(file.getId()));
            return;
        }
        queueImage(context, drive, file, view);
    }

    public static Object getImage(Context context, Drive drive, File file) {

        Object object;
        String iconLink = file.getIconLink();
        if (!TextUtils.isEmpty(iconLink)) {
            try {
                InputStream stream = new URL(iconLink).openStream();
                object = BitmapFactory.decodeStream(stream);
            } catch (IOException e) {
                object = null;
                e.printStackTrace();
            }
        } else if (file.getMimeType().equals(DriveFile.FOLDER_TYPE)) {
            object = context.getDrawable(R.drawable.folder);
        } else {
            object = context.getDrawable(R.drawable.file);
        }

        return object;
    }

    private static class ImageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            if (bundle != null) {
                String key = bundle.getString("key");
                if (!TextUtils.isEmpty(key)) {
                    if (msg.obj != null) {
                        setImage((ImageView) msg.obj, mCache.get(key));
                    }
                }
            }
        }
    }

    public static void queueImage(final Context context, final Drive drive,
            final File file, final ImageView view) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                final Object o = getImage(context, drive, file);
                mCache.put(file.getId(), o);
                Message message = Message.obtain();
                Bundle data = new Bundle();
                data.putString("key", file.getId());
                message.setData(data);
                message.obj = view;
                mHandler.sendMessage(message);
            }
        });
    }

    public static void setImage(ImageView view, Object o) {
        if (o instanceof Drawable) {
            view.setImageDrawable((Drawable) o);
        } else if (o instanceof Bitmap) {
            view.setImageBitmap((Bitmap) o);
        }
    }

    public static void clearCache() {
        mCache.clear();
    }
}
