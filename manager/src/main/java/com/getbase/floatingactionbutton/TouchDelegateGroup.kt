package com.getbase.floatingactionbutton

import android.graphics.Rect
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View

import java.util.ArrayList

internal class TouchDelegateGroup(uselessHackyView: View) :
        TouchDelegate(USELESS_HACKY_RECT, uselessHackyView) {
    private val mTouchDelegates = ArrayList<TouchDelegate>()
    private var mCurrentTouchDelegate: TouchDelegate? = null
    private var mEnabled: Boolean = false

    fun addTouchDelegate(touchDelegate: TouchDelegate) {
        mTouchDelegates.add(touchDelegate)
    }

    fun clearTouchDelegates() {
        mTouchDelegates.clear()
        mCurrentTouchDelegate = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!mEnabled) return false

        var delegate: TouchDelegate? = null

        when (event.action) {
            MotionEvent.ACTION_DOWN -> for (i in mTouchDelegates.indices) {
                val touchDelegate = mTouchDelegates[i]
                if (touchDelegate.onTouchEvent(event)) {
                    mCurrentTouchDelegate = touchDelegate
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> delegate = mCurrentTouchDelegate

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                delegate = mCurrentTouchDelegate
                mCurrentTouchDelegate = null
            }
        }

        return delegate != null && delegate.onTouchEvent(event)
    }

    fun setEnabled(enabled: Boolean) {
        mEnabled = enabled
    }

    companion object {
        private val USELESS_HACKY_RECT = Rect()
    }
}