package com.slim.slimfilemanager.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View

import com.slim.slimfilemanager.R

class PageIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                              defStyle: Int = 0) : View(context, attrs, defStyle),
                                                                   ViewPager.OnPageChangeListener {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    internal var mViewPager: ViewPager? = null
    internal var mCurrentPage: Int = 0
    internal var mPositionOffset: Float = 0.toFloat()
    internal var mScrollState: Int = 0

    internal var mFadeDelay = 100
    internal var mFadeBy = 0xFF / (mFadeDelay / FADE_FRAME_MS)
    internal var mFades = false

    private val mFadeRunnable = object : Runnable {
        override fun run() {
            if (!mFades) return

            val alpha = Math.max(mPaint.alpha - mFadeBy, 0)
            mPaint.alpha = alpha
            invalidate()
            if (alpha > 0) {
                postDelayed(this, FADE_FRAME_MS.toLong())
            }
        }
    }

    init {

        val a = context.obtainStyledAttributes(attrs,
                R.styleable.PageIndicator, defStyle, 0)

        setSelectedColor(a.getColor(
                R.styleable.PageIndicator_selectedColor, Color.WHITE))
        a.recycle()
    }

    fun setViewPager(vp: ViewPager) {
        if (mViewPager === vp) {
            return
        }
        if (mViewPager != null) {
            mViewPager!!.removeOnPageChangeListener(this)
        }
        mViewPager = vp
        mViewPager!!.addOnPageChangeListener(this)
    }

    fun setSelectedColor(@ColorInt color: Int) {
        mPaint.color = color
        requestLayout()
    }

    fun setUnselectedColor(@ColorInt color: Int) {
        setBackgroundColor(color)
        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mViewPager == null) {
            return
        }
        val count = mViewPager!!.adapter!!.count
        if (count == 0) {
            return
        }

        if (mCurrentPage >= count) {
            return
        }

        val paddingLeft = paddingLeft
        val pageWidth = (width - paddingLeft - paddingRight) / (1f * count)
        val left = paddingLeft + pageWidth * (mCurrentPage + mPositionOffset)
        val right = left + pageWidth
        val top = paddingTop.toFloat()
        val bottom = (height - paddingBottom).toFloat()

        canvas.drawRect(left, top, right, bottom, mPaint)
    }

    override fun onPageScrolled(position: Int,
                                positionOffset: Float, positionOffsetPixels: Int) {
        mCurrentPage = position
        mPositionOffset = positionOffset
        if (mFades) {
            if (positionOffsetPixels > 0) {
                removeCallbacks(mFadeRunnable)
                mPaint.alpha = 0xFF
            } else if (mScrollState != ViewPager.SCROLL_STATE_DRAGGING) {
                postDelayed(mFadeRunnable, mFadeDelay.toLong())
            }
        }
        invalidate()
    }

    override fun onPageSelected(position: Int) {

    }

    override fun onPageScrollStateChanged(state: Int) {
        mScrollState = state
    }

    companion object {

        private val FADE_FRAME_MS = 30
    }
}
