package com.co.dinofox.bubblebuilder.adapters;

/**
 * Semplice interfaccia per gestire lo SwipeToRemove e Drag&Drop delle schede nelle recycler.
 */
public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}