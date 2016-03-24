package com.slim.slimfilemanager.utils;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Bookmark extends RealmObject {

    @Required
    private String name;

    @Required
    private String path;

    private int fragmentId;

    private int menuId;

    public Bookmark() {}

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
