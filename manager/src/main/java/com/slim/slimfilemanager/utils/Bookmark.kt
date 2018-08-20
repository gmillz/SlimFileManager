package com.slim.slimfilemanager.utils

import android.database.Cursor

class Bookmark {

    var name: String? = null

    var path: String? = null

    var fragmentId: Int = 0

    var menuId: Int = 0

    constructor() {}

    constructor(cursor: Cursor) {
        name = cursor.getString(0)
        path = cursor.getString(1)
    }
}
