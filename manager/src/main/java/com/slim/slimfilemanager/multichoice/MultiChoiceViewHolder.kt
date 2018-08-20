package com.slim.slimfilemanager.multichoice

import android.animation.AnimatorInflater
import android.animation.StateListAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.support.v7.widget.RebindReportingHolder
import android.util.StateSet
import android.view.View

import com.slim.slimfilemanager.R
import com.slim.slimfilemanager.ThemeActivity

open class MultiChoiceViewHolder(view: View, private val mMultiSelector: MultiSelector) :
        RebindReportingHolder(view), SelectableHolder {

    private var mIsSelectable = false

    private var mSelectionModeBackgroundDrawable: Drawable? = null
    private var mDefaultModeBackgroundDrawable: Drawable? = null

    private var mSelectionModeAnimator: StateListAnimator? = null
    private var mDefaultAnimator: StateListAnimator? = null

    override val holderPosition: Int
        get() = layoutPosition

    init {

        setSelectionModeAnimator(getRaiseStateListAnimator(itemView.context))
        if (itemView.stateListAnimator != null) {
            setDefaultAnimator(itemView.stateListAnimator)
        }

        // Default selection mode background drawable is this
        setSelectionModeBackgroundDrawable(
                getAccentStateDrawable(view.context))
        if (itemView.background != null) {
            setDefaultModeBackgroundDrawable(itemView.background)
        }
    }

    private fun getRaiseStateListAnimator(context: Context): StateListAnimator {
        return AnimatorInflater.loadStateListAnimator(context, R.anim.raise)
    }

    private fun setSelectionModeAnimator(animator: StateListAnimator) {
        mSelectionModeAnimator = animator
    }

    private fun setDefaultAnimator(animator: StateListAnimator) {
        mDefaultAnimator = animator
    }

    fun setSelectionModeBackgroundDrawable(selectionModeBackgroundDrawable: Drawable) {
        mSelectionModeBackgroundDrawable = selectionModeBackgroundDrawable

        if (mIsSelectable) {
            itemView.background = selectionModeBackgroundDrawable
        }
    }

    fun setDefaultModeBackgroundDrawable(defaultModeBackgroundDrawable: Drawable) {
        mDefaultModeBackgroundDrawable = defaultModeBackgroundDrawable

        if (!mIsSelectable) {
            itemView.background = mDefaultModeBackgroundDrawable
        }
    }

    override fun setActivated(isActivated: Boolean) {
        itemView.isActivated = isActivated
    }

    override fun setSelectable(isSelectable: Boolean) {
        val changed = isSelectable != mIsSelectable
        mIsSelectable = isSelectable
        if (changed) {
            refresh()
        }
    }

    private fun refresh() {
        val backgroundDrawable = if (mIsSelectable)
            mSelectionModeBackgroundDrawable
        else
            mDefaultModeBackgroundDrawable
        itemView.background = backgroundDrawable
        backgroundDrawable?.jumpToCurrentState()

        val animator = if (mIsSelectable) mSelectionModeAnimator else mDefaultAnimator
        itemView.stateListAnimator = animator
        animator?.jumpToCurrentState()
    }

    private fun getAccentStateDrawable(context: Context): Drawable {

        val colorDrawable = ColorDrawable(ThemeActivity.getAccentColor(context))

        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(android.R.attr.state_activated), colorDrawable)
        stateListDrawable.addState(StateSet.WILD_CARD, null)

        return stateListDrawable
    }

    override fun onRebind() {
        mMultiSelector.bindHolder(this, adapterPosition)
    }
}
