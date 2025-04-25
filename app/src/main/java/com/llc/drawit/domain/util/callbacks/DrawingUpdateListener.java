package com.llc.drawit.domain.util.callbacks;

import android.util.Pair;

import com.llc.drawit.domain.util.drawing.CPoint;
import com.llc.drawit.domain.util.drawing.Stroke;

import java.util.ArrayList;

// Callback for drawing update
public interface DrawingUpdateListener {
    void addDrawing(Pair<Stroke, ArrayList<CPoint>> drawing);
}
