package com.slim.slimfilemanager.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import java.util.ArrayList

class BookmarkHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    val bookmarks: List<Bookmark>
        get() {
            val bookmarks = ArrayList<Bookmark>()

            val cursor = writableDatabase.query(BOOKMARK_TABLE_NAME,
                    BOOKMARK_TABLE_COLUMNS, null, null, null, null, null)

            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                val bookmark = Bookmark(cursor)
                if (bookmark != null) bookmarks.add(bookmark)
                cursor.moveToNext()
            }
            cursor.close()
            return bookmarks
        }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DATABASE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // nothing yet
    }

    fun addBookmark(bookmark: Bookmark) {
        val values = ContentValues()
        values.put(COLUMN_NAME, bookmark.name)
        values.put(COLUMN_PATH, bookmark.path)

        writableDatabase.insert(BOOKMARK_TABLE_NAME, null, values)
    }

    fun deleteBookmark() {

    }

    companion object {

        private val BOOKMARK_TABLE_NAME = "bookmarks"
        private val COLUMN_NAME = "name"
        private val COLUMN_PATH = "path"
        private val COLUMN_ID = "_id"
        private val BOOKMARK_TABLE_COLUMNS = arrayOf(COLUMN_NAME, COLUMN_PATH)

        private val DATABASE_NAME = "bookmarks.db"
        private val DATABASE_VERSION = 1

        private val DATABASE_CREATE = ("create table " + BOOKMARK_TABLE_NAME
                + "(" + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_NAME + " text not null, "
                + COLUMN_PATH + " text not null"
                + ");")
    }
}
