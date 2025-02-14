package com.llc.drawit.domain.util.callbacks;

import com.llc.drawit.domain.util.drawing.CPoint;

// коллбэк для добавления текста на доску
public interface OnAddText {
    void invoke (CPoint pos);
}
