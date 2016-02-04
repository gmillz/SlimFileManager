package com.slim.slimfilemanager.multichoice;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.RebindReportingHolder;
import android.util.StateSet;
import android.view.View;

import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.ThemeActivity;

public class MultiChoiceViewHolder extends RebindReportingHolder
        implements SelectableHolder {

    private MultiSelector mMultiSelector;

    private boolean mIsSelectable = false;

    private Drawable mSelectionModeBackgroundDrawable;
    private Drawable mDefaultModeBackgroundDrawable;

    private StateListAnimator mSelectionModeAnimator;
    private StateListAnimator mDefaultAnimator;

    public MultiChoiceViewHolder(View view, MultiSelector selector) {
        super(view);

        mMultiSelector = selector;

        setSelectionModeAnimator(getRaiseStateListAnimator(itemView.getContext()));
        setDefaultAnimator(itemView.getStateListAnimator());

        // Default selection mode background drawable is this
        setSelectionModeBackgroundDrawable(
                getAccentStateDrawable(itemView.getContext()));
        setDefaultModeBackgroundDrawable(
                itemView.getBackground());
    }

    private void setSelectionModeAnimator(StateListAnimator animator) {
        mSelectionModeAnimator = animator;
    }

    private void setDefaultAnimator(StateListAnimator animator) {
        mDefaultAnimator = animator;
    }

    private static StateListAnimator getRaiseStateListAnimator(Context context) {
        return AnimatorInflater.loadStateListAnimator(context, R.anim.raise);
    }

    public void setSelectionModeBackgroundDrawable(Drawable selectionModeBackgroundDrawable) {
        mSelectionModeBackgroundDrawable = selectionModeBackgroundDrawable;

        if (mIsSelectable) {
            itemView.setBackground(selectionModeBackgroundDrawable);
        }
    }

    public void setDefaultModeBackgroundDrawable(Drawable defaultModeBackgroundDrawable) {
        mDefaultModeBackgroundDrawable = defaultModeBackgroundDrawable;

        if (!mIsSelectable) {
            itemView.setBackground(mDefaultModeBackgroundDrawable);
        }
    }

    public void setActivated(boolean isActivated) {
        itemView.setActivated(isActivated);
    }

    public void setSelectable(boolean isSelectable) {
        boolean changed = isSelectable != mIsSelectable;
        mIsSelectable = isSelectable;
        if (changed) {
            refresh();
        }
    }

    public int getHolderPosition() {
        return getLayoutPosition();
    }

    private void refresh() {
        Drawable backgroundDrawable = mIsSelectable ? mSelectionModeBackgroundDrawable
                : mDefaultModeBackgroundDrawable;
        itemView.setBackground(backgroundDrawable);
        if (backgroundDrawable != null) {
            backgroundDrawable.jumpToCurrentState();
        }

        StateListAnimator animator = mIsSelectable ? mSelectionModeAnimator : mDefaultAnimator;
        itemView.setStateListAnimator(animator);
        if (animator != null) {
            animator.jumpToCurrentState();
        }
    }

    private Drawable getAccentStateDrawable(Context context) {

        Drawable colorDrawable = new ColorDrawable(ThemeActivity.getAccentColor(context));

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_activated}, colorDrawable);
        stateListDrawable.addState(StateSet.WILD_CARD, null);

        return stateListDrawable;
    }

    @Override
    protected void onRebind() {
        mMultiSelector.bindHolder(this, getAdapterPosition());
    }
}
