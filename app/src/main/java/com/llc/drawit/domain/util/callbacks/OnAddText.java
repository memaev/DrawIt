package com.llc.drawit.domain.util.callbacks;

import com.llc.drawit.domain.util.drawing.CPoint;

/**
 * Callback that's being called when text was added to the screen
 */
@FunctionalInterface
public interface OnAddText {
    void invoke (CPoint pos);
}
