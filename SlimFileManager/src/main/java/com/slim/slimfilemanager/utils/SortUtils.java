package com.slim.slimfilemanager.utils;

import android.content.Context;
import android.text.TextUtils;

import com.slim.slimfilemanager.settings.SettingsProvider;
import com.slim.slimfilemanager.utils.file.BaseFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SortUtils {

    public static final String SORT_MODE_NAME = "sort_mode_name";
    public static final String SORT_MODE_SIZE = "sort_mode_size";
    public static final String SORT_MODE_TYPE = "sort_mode_type";

    public static void sort(Context context, ArrayList<BaseFile> files) {
        if (context != null) {
            String sortMode = SettingsProvider.getString(context,
                    SettingsProvider.SORT_MODE, SORT_MODE_NAME);
            switch (sortMode) {
                case SORT_MODE_SIZE:
                    Collections.sort(files, getSizeComparator());
                    return;
                case SORT_MODE_TYPE:
                    Collections.sort(files, getTypeComparator());
                    return;
            }
        }
        Collections.sort(files, getNameComparator());
    }

    private static Comparator<BaseFile> getNameComparator() {
        return new Comparator<BaseFile>() {
            @Override
            public int compare(BaseFile lhs, BaseFile rhs) {
                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
            }
        };
    }

    private static Comparator<BaseFile> getSizeComparator() {
        return new Comparator<BaseFile>() {
            @Override
            public int compare(BaseFile lhs, BaseFile rhs) {

                if (lhs.isDirectory() && rhs.isDirectory()) {
                    int al = 0, bl = 0;
                    String[] aList = lhs.list();
                    String[] bList = rhs.list();

                    if (aList != null) {
                        al = aList.length;
                    }

                    if (bList != null) {
                        bl = bList.length;
                    }

                    if (al == bl) {
                        return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
                    }

                    if (al < bl) {
                        return -1;
                    }
                    return 1;
                }

                if (rhs.isDirectory()) {
                    return -1;
                }

                if (lhs.isDirectory()) {
                    return 1;
                }

                final long len_a = rhs.length();
                final long len_b = lhs.length();

                if (len_a == len_b) {
                    return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
                }

                if (len_a < len_b) {
                    return -1;
                }

                return 1;
            }
        };
    }

    private static Comparator<BaseFile> getTypeComparator() {
        return new Comparator<BaseFile>() {
            @Override
            public int compare(BaseFile lhs, BaseFile rhs) {

                if (lhs.isDirectory() && rhs.isDirectory()) {
                    return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
                }

                if (lhs.isDirectory()) {
                    return -1;
                }

                if (rhs.isDirectory()) {
                    return 1;
                }

                final String ext_a = lhs.getExtension();
                final String ext_b = rhs.getExtension();

                if (TextUtils.isEmpty(ext_a) && TextUtils.isEmpty(ext_b)) {
                    return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
                }

                if (TextUtils.isEmpty(ext_a)) {
                    return -1;
                }

                if (TextUtils.isEmpty(ext_b)) {
                    return 1;
                }

                final int res = ext_a.compareTo(ext_b);
                if (res == 0) {
                    return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
                }
                return res;
            }
        };
    }
}