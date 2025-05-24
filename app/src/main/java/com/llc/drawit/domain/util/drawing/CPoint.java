package com.llc.drawit.domain.util.drawing;

import androidx.annotation.NonNull;

public record CPoint(
        float x,
        float y
) {
    @NonNull
    @Override
    public String toString(){
        return this.x + " " + this.y;
    }
}
