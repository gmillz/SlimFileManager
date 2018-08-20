package com.slim.slimfilemanager.widget

import com.slim.slimfilemanager.fragment.BaseBrowserFragment
import com.slim.slimfilemanager.fragment.BrowserFragment

class TabItem(var path: String, var id: Int) {
    var fragment: BaseBrowserFragment? = null

    init {
        setFragment()
    }

    private fun setFragment() {
        fragment = BrowserFragment.newInstance(path)
    }

    override fun toString(): String {
        var s = path
        s += "<.>"
        s += id
        return s
    }

    companion object {
        val TAB_BROWSER = 1001

        fun fromString(s: String): TabItem {
            val fields = s.split("<.>".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val path = fields[0]
            val id = Integer.parseInt(fields[1])
            return TabItem(path, id)
        }
    }
}
