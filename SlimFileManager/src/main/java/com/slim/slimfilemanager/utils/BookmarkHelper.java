package com.slim.slimfilemanager.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class BookmarkHelper extends SQLiteOpenHelper {

    private static final String BOOKMARK_TABLE_NAME = "bookmarks";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PATH = "path";
    private static final String COLUMN_ID = "_id";
    private static final String[] BOOKMARK_TABLE_COLUMNS = {
            COLUMN_NAME,
            COLUMN_PATH
    };

    private static final String DATABASE_NAME = "bookmarks.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "create table " + BOOKMARK_TABLE_NAME
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_PATH + " text not null"
            + ");";


    public BookmarkHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nothing yet
    }

    public void addBookmark(Bookmark bookmark) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, bookmark.getName());
        values.put(COLUMN_PATH, bookmark.getPath());

        getWritableDatabase().insert(BOOKMARK_TABLE_NAME, null, values);
    }

    public void deleteBookmark() {

    }

    public List<Bookmark> getBookmarks() {
        List<Bookmark> bookmarks = new ArrayList<>();

        Cursor cursor = getWritableDatabase().query(BOOKMARK_TABLE_NAME,
                BOOKMARK_TABLE_COLUMNS, null, null, null, null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Bookmark bookmark = new Bookmark(cursor);
            if (bookmark != null) bookmarks.add(bookmark);
            cursor.moveToNext();
        }
        cursor.close();
        return bookmarks;
    }
}
