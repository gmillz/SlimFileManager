package com.slim.slimfilemanager.multichoice

import android.util.SparseArray
import android.util.SparseBooleanArray

import java.lang.ref.WeakReference
import java.util.ArrayList

class MultiSelector {

    private val mSelections = SparseBooleanArray()
    private val mTracker = WeakHolderTracker()

    private var mIsSelectable: Boolean = false

    val selectedPositions: List<Int>
        get() {
            val positions = ArrayList<Int>()

            for (i in 0 until mSelections.size()) {
                if (mSelections.valueAt(i)) {
                    positions.add(mSelections.keyAt(i))
                }
            }
            return positions
        }

    private fun refreshHolder(holder: SelectableHolder?) {
        if (holder == null) {
            return
        }
        holder.setSelectable(mIsSelectable)

        val isActivated = mSelections.get(holder.holderPosition)
        holder.setActivated(isActivated)
    }

    fun bindHolder(holder: MultiChoiceViewHolder, position: Int) {
        mTracker.bindHolder(holder, position)
        refreshHolder(holder)
    }

    fun setSelectable(isSelectable: Boolean) {
        mIsSelectable = isSelectable
        refreshAllHolders()
    }

    private fun refreshAllHolders() {
        for (holder in mTracker.trackedHolders) {
            refreshHolder(holder)
        }
    }

    fun isSelected(position: Int): Boolean {
        return mSelections.get(position)
    }

    fun setSelected(position: Int, isSelected: Boolean) {
        mSelections.put(position, isSelected)
        refreshHolder(mTracker.getHolder(position))
    }

    fun clearSelections() {
        mSelections.clear()
        refreshAllHolders()
    }

    fun setSelected(holder: SelectableHolder, isSelected: Boolean) {
        setSelected(holder.holderPosition, isSelected)
    }

    fun tapSelection(holder: SelectableHolder): Boolean {
        return tapSelection(holder.holderPosition)
    }

    private fun tapSelection(position: Int): Boolean {
        if (mIsSelectable) {
            val isSelected = isSelected(position)
            setSelected(position, !isSelected)
            return true
        }
        return false

    }

    internal inner class WeakHolderTracker {
        private val mHoldersByPosition = SparseArray<WeakReference<SelectableHolder>>()

        val trackedHolders: List<SelectableHolder>
            get() {
                val holders = ArrayList<SelectableHolder>()

                for (i in 0 until mHoldersByPosition.size()) {
                    val key = mHoldersByPosition.keyAt(i)
                    val holder = getHolder(key)

                    if (holder != null) {
                        holders.add(holder)
                    }
                }

                return holders
            }

        fun getHolder(position: Int): SelectableHolder? {
            val holderRef = mHoldersByPosition.get(position) ?: return null

            val holder = holderRef.get()
            if (holder == null || holder.holderPosition != position) {
                mHoldersByPosition.remove(position)
                return null
            }

            return holder
        }

        fun bindHolder(holder: SelectableHolder, position: Int) {
            mHoldersByPosition.put(position, WeakReference(holder))
        }
    }
}
