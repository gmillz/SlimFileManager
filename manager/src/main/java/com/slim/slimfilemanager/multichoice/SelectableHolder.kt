package com.slim.slimfilemanager.multichoice

interface SelectableHolder {

    val holderPosition: Int
    fun setSelectable(selectable: Boolean)

    fun setActivated(activated: Boolean)
}
