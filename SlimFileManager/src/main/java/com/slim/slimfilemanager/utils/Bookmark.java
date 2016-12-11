package com.slim.slimfilemanager.utils;

import android.database.Cursor;

public class Bookmark {

    private String name;

    private String path;

    private int fragmentId;

    private int menuId;

    public Bookmark() {
    }

    public Bookmark(Cursor cursor) {
        setName(cursor.getString(0));
        setPath(cursor.getString(1));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getFragmentId() {
        return fragmentId;
    }

    public void setFragmentId(int fragmentId) {
        this.fragmentId = fragmentId;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }
}
