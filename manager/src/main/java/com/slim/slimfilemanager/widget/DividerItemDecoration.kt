package com.slim.slimfilemanager.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

class DividerItemDecoration(context: Context, attrs: AttributeSet) : RecyclerView.ItemDecoration() {

    private val mDivider: Drawable?
    private var mShowFirstDivider = false
    private var mShowLastDivider = false

    init {
        val a = context
                .obtainStyledAttributes(attrs, intArrayOf(android.R.attr.listDivider))
        mDivider = a.getDrawable(0)
        a.recycle()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        if (mDivider == null) {
            return
        }
        if (parent.getChildLayoutPosition(view) < 1) {
            return
        }

        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
            outRect.top = mDivider.intrinsicHeight
        } else {
            outRect.left = mDivider.intrinsicWidth
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (mDivider == null) {
            super.onDrawOver(c, parent, state)
            return
        }

        // Initialization needed to avoid compiler warning
        var left = 0
        var right = 0
        var top = 0
        var bottom = 0
        val size: Int
        val orientation = getOrientation(parent)
        val childCount = parent.childCount

        if (orientation == LinearLayoutManager.VERTICAL) {
            size = mDivider.intrinsicHeight
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
        } else { //horizontal
            size = mDivider.intrinsicWidth
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
        }

        for (i in (if (mShowFirstDivider) 0 else 1) until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            if (orientation == LinearLayoutManager.VERTICAL) {
                top = child.top - params.topMargin
                bottom = top + size
            } else { //horizontal
                left = child.left - params.leftMargin
                right = left + size
            }
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }

        // show last divider
        if (mShowLastDivider && childCount > 0) {
            val child = parent.getChildAt(childCount - 1)
            val params = child.layoutParams as RecyclerView.LayoutParams
            if (orientation == LinearLayoutManager.VERTICAL) {
                top = child.bottom + params.bottomMargin
                bottom = top + size
            } else { // horizontal
                left = child.right + params.rightMargin
                right = left + size
            }
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }

    private fun getOrientation(parent: RecyclerView): Int {
        if (parent.layoutManager is LinearLayoutManager) {
            val layoutManager = parent.layoutManager as LinearLayoutManager?
            return layoutManager!!.orientation
        } else {
            throw IllegalStateException(
                    "DividerItemDecoration can only be used with a LinearLayoutManager.")
        }
    }

    fun setShowFirstDivider(show: Boolean) {
        mShowFirstDivider = show
    }

    fun setShowLastDivider(show: Boolean) {
        mShowLastDivider = show
    }
}
