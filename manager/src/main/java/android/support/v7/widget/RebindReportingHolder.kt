package android.support.v7.widget

import android.view.View

/**
 * ViewHolder with a callback for when it is rebound. Please use judiciously.
 */
abstract class RebindReportingHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private fun isRelevantFlagSet(flag: Int): Boolean {
        for (value in intArrayOf(RecyclerView.ViewHolder.FLAG_BOUND,
                RecyclerView.ViewHolder.FLAG_UPDATE,
                RecyclerView.ViewHolder.FLAG_RETURNED_FROM_SCRAP)) {
            if (flag and value == value) {
                return true
            }
        }

        return false
    }

    /**
     * Called when the ViewHolder is rebound to another item.
     */
    protected abstract fun onRebind()

    internal override fun setFlags(flags: Int, mask: Int) {
        super.setFlags(flags, mask)
        val setFlags = mask and flags
        checkFlags(setFlags)
    }

    internal override fun addFlags(flags: Int) {
        super.addFlags(flags)
        checkFlags(flags)
    }

    private fun checkFlags(setFlags: Int) {
        if (isRelevantFlagSet(setFlags)) {
            onRebind()
        }
    }

    internal override fun offsetPosition(offset: Int, applyToPreLayout: Boolean) {
        super.offsetPosition(offset, applyToPreLayout)
        onRebind()
    }
}
