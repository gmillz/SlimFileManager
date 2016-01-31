package com.slim.slimfilemanager.widget;

import com.slim.slimfilemanager.fragment.BaseBrowserFragment;
import com.slim.slimfilemanager.fragment.BrowserFragment;
import com.slim.slimfilemanager.fragment.DriveFragment;
import com.slim.slimfilemanager.fragment.DropboxFragment;

public class TabItem {
    public BaseBrowserFragment fragment;
    public String path;
    public int id;

    public static final int TAB_BROWSER = 1001;
    public static final int TAB_DROPBOX = 1002;
    public static final int TAB_DRIVE   = 1003;

    public TabItem(String p, int id) {
        path = p;
        this.id = id;
        setFragment();
    }

    private void setFragment() {
        if (id == TAB_DROPBOX) {
            fragment = new DropboxFragment();
        } else if (id == TAB_DRIVE) {
            fragment = new DriveFragment();
        } else {
            fragment = BrowserFragment.newInstance(path);
        }
    }

    public static TabItem fromString(String s) {
        String[] fields = s.split("<.>");
        String path = fields[0];
        int id = Integer.parseInt(fields[1]);
        return new TabItem(path, id);
    }

    public String toString() {
        String s = path;
        s += "<.>";
        s += id;
        return s;
    }
}
