package com.slim.slimfilemanager.services.dropbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ImageView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.utils.MimeUtils;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IconCache {

    private static final DropboxAPI.ThumbSize SIZE = DropboxAPI.ThumbSize.ICON_64x64;
    private static final DropboxAPI.ThumbFormat FORMAT = DropboxAPI.ThumbFormat.PNG;

    private static ConcurrentHashMap<String, Object> mCache;

    private static ImageHandler mHandler;

    private static ExecutorService mExecutor;

    static {
        mCache = new ConcurrentHashMap<>();
        mHandler = new ImageHandler();
        mExecutor = Executors.newFixedThreadPool(6);
    }

    public static void getIconForFile(Context context,
                                      DropboxAPI api, DropboxAPI.Entry entry, ImageView view) {
        view.setImageBitmap(null);
        view.setImageDrawable(null);
        if (mCache.containsKey(entry.path)) {
            setImage(view, mCache.get(entry.path));
            return;
        }
        queueImage(context, api, entry, view);
    }

    public static Object getImage(Context context, DropboxAPI api, DropboxAPI.Entry entry) {

        Object object = null;

        if (entry.thumbExists) {
            try {
                InputStream stream = api.getThumbnailStream(entry.path, SIZE, FORMAT);
                object = BitmapFactory.decodeStream(stream);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
        } else if (entry.isDir) {
            object = context.getDrawable(R.drawable.folder);
        } else if (MimeUtils.isTextFile(entry.fileName())) {
            object = context.getDrawable(R.drawable.text);
        } else {
            object = context.getDrawable(R.drawable.file);
        }

        return object;
    }

    public static void queueImage(final Context context, final DropboxAPI api,
                                  final DropboxAPI.Entry entry, final ImageView view) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                final Object o = getImage(context, api, entry);
                mCache.put(entry.path, o);
                Message message = Message.obtain();
                Bundle data = new Bundle();
                data.putString("key", entry.path);
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
}
