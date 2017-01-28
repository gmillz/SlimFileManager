package com.slim.slimfilemanager.widget;

import com.slim.slimfilemanager.fragment.BaseBrowserFragment;
import com.slim.slimfilemanager.fragment.BrowserFragment;

public class TabItem {
    public static final int TAB_BROWSER = 1001;
    public BaseBrowserFragment fragment;
    public String path;
    public int id;

    public TabItem(String p, int id) {
        path = p;
        this.id = id;
        setFragment();
    }

    public static TabItem fromString(String s) {
        String[] fields = s.split("<.>");
        String path = fields[0];
        int id = Integer.parseInt(fields[1]);
        return new TabItem(path, id);
    }

    private void setFragment() {
        fragment = BrowserFragment.newInstance(path);
    }

    public String toString() {
        String s = path;
        s += "<.>";
        s += id;
        return s;
    }
}
