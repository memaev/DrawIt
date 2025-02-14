package com.llc.drawit.domain.util.drawing;

import androidx.annotation.NonNull;

public class CPoint {
    public float x;
    public float y;

    public CPoint (float x, float y){
        this.x = x;
        this.y = y;
    }

    @NonNull
    @Override
    public String toString(){
        return this.x + " " + this.y;
    }
}
