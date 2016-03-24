package com.slim.slimfilemanager.widget;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.util.AttributeSet;
import android.view.SubMenu;
import android.view.View;

import com.slim.slimfilemanager.R;

public class SlimNavigationView extends NavigationView {

    public SlimNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlimNavigationView(Context context) {
        super(context);
    }

    public void addCollapsibleGroup() {
        View view = View.inflate(getContext(), R.layout.collapsible_header, null);
        SubMenu menu = getMenu().addSubMenu("T");
        menu.setHeaderView(view);
        menu.add("TESTING");
    }


}
