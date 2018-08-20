/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.slim.slimfilemanager.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Build
import android.support.annotation.ColorInt
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

import com.slim.slimfilemanager.R

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT

/**
 * This widget implements the dynamic action bar tab behavior that can change
 * across different configurations or circumstances.
 */
class TabPageIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        HorizontalScrollView(context, attrs), ViewPager.OnPageChangeListener {
    private val mTabLayout: IndicatorLinearLayout
    private var mTabSelector: Runnable? = null
    private var mSelectedColor: Int = 0
    private var mBackgroundColor: Int = 0
    private var mViewPager: ViewPager? = null
    private val mTabClickListener = OnClickListener { view ->
        val tabView = view as TabView
        val newSelected = tabView.index
        mViewPager!!.currentItem = newSelected
    }
    private var mMaxTabWidth: Int = 0
    private var mSelectedTabIndex: Int = 0

    init {
        isHorizontalScrollBarEnabled = false

        val type = context.obtainStyledAttributes(attrs, R.styleable.PageIndicator)

        mSelectedColor = type.getColor(R.styleable.PageIndicator_selectedColor, Color.TRANSPARENT)
        mBackgroundColor = type.getColor(R.styleable.PageIndicator_defaultColor, Color.TRANSPARENT)

        type.recycle()

        mTabLayout = IndicatorLinearLayout(context, R.attr.tabPageIndicatorStyle)
        addView(mTabLayout, ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT))
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val lockedExpanded = widthMode == View.MeasureSpec.EXACTLY
        isFillViewport = lockedExpanded

        val childCount = mTabLayout.childCount
        if (childCount > 1 && (widthMode == View.MeasureSpec.EXACTLY || widthMode == View.MeasureSpec.AT_MOST)) {
            if (childCount > 2) {
                mMaxTabWidth = (View.MeasureSpec.getSize(widthMeasureSpec) * 0.4f).toInt()
            } else {
                mMaxTabWidth = View.MeasureSpec.getSize(widthMeasureSpec) / 2
            }
        } else {
            mMaxTabWidth = -1
        }

        val oldWidth = measuredWidth
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val newWidth = measuredWidth

        if (lockedExpanded && oldWidth != newWidth) {
            // Recenter the tab display if we're at a new (scrollable) size.
            setCurrentItem(mSelectedTabIndex)
        }
    }

    fun setSelectedColor(@ColorInt color: Int) {
        mSelectedColor = color
        requestLayout()
    }

    fun setUnselectedColor(@ColorInt color: Int) {
        mBackgroundColor = color
        requestLayout()
    }

    fun setTabTitle(title: String, position: Int) {
        val tv = mTabLayout.getChildAt(position) as TabView?
        tv?.setText(title.toUpperCase())
    }

    private fun animateToTab(position: Int) {
        val tabView = mTabLayout.getChildAt(position)
        if (mTabSelector != null) {
            removeCallbacks(mTabSelector)
        }
        mTabSelector = Runnable {
            val scrollPos = tabView.left - (width - tabView.width) / 2
            smoothScrollTo(scrollPos, 0)
            mTabSelector = null
        }
        post(mTabSelector)
    }

    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mTabSelector != null) {
            // Re-post the selector we saved
            post(mTabSelector)
        }
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mTabSelector != null) {
            removeCallbacks(mTabSelector)
        }
    }

    private fun addTab(index: Int, text: CharSequence, iconResId: Int) {
        val tabView = TabView(context)
        tabView.index = index
        tabView.isFocusable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tabView.setTextAppearance(R.style.TextAppearance_TabPageIndicator)
        }
        tabView.setOnClickListener(mTabClickListener)
        tabView.text = text
        tabView.gravity = Gravity.CENTER

        if (iconResId != 0) {
            tabView.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0)
        }

        mTabLayout.addView(tabView, LinearLayout.LayoutParams(0, MATCH_PARENT, 1f))
    }

    override fun onPageScrollStateChanged(arg0: Int) {}

    override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}

    override fun onPageSelected(arg0: Int) {
        setCurrentItem(arg0)
    }

    fun setViewPager(view: ViewPager) {
        if (mViewPager === view) {
            return
        }
        if (mViewPager != null) {
            mViewPager!!.removeOnPageChangeListener(this)
        }
        val adapter = view.adapter ?: throw IllegalStateException(
                "ViewPager does not have adapter instance.")
        mViewPager = view
        mViewPager!!.addOnPageChangeListener(this)
        notifyDataSetChanged()
    }

    fun notifyDataSetChanged() {
        mTabLayout.removeAllViews()
        val adapter = mViewPager!!.adapter
        var iconAdapter: IconPagerAdapter? = null
        if (adapter is IconPagerAdapter) {
            iconAdapter = adapter
        }
        val count = adapter!!.count
        for (i in 0 until count) {
            var title = adapter.getPageTitle(i)
            if (title == null) {
                title = EMPTY_TITLE
            }
            var iconResId = 0
            if (iconAdapter != null) {
                iconResId = iconAdapter.getIconResId(i)
            }
            addTab(i, title, iconResId)
        }
        if (mSelectedTabIndex > count) {
            mSelectedTabIndex = count - 1
        }
        setCurrentItem(mSelectedTabIndex)
        requestLayout()
    }

    fun setCurrentItem(item: Int) {
        if (mViewPager == null) {
            throw IllegalStateException("ViewPager has not been bound.")
        }
        mSelectedTabIndex = item
        mViewPager!!.currentItem = item

        val tabCount = mTabLayout.childCount
        for (i in 0 until tabCount) {
            val child = mTabLayout.getChildAt(i)
            val isSelected = i == item
            child.isSelected = isSelected
            if (isSelected) {
                animateToTab(item)
                child.setBackgroundColor(mSelectedColor)
            } else {
                child.setBackgroundColor(mBackgroundColor)
            }
        }
    }

    interface IconPagerAdapter {
        fun getIconResId(id: Int): Int
    }

    private inner class TabView(context: Context) : TextView(context, null) {
        var index: Int = 0

        public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)

            // Re-measure if we went beyond our maximum size.
            if (mMaxTabWidth > 0 && measuredWidth > mMaxTabWidth) {
                super.onMeasure(
                        View.MeasureSpec.makeMeasureSpec(mMaxTabWidth, View.MeasureSpec.EXACTLY),
                        heightMeasureSpec)
            }
        }
    }

    companion object {

        /**
         * Title text used when no title is provided by the adapter.
         */
        private val EMPTY_TITLE = ""
    }
}
