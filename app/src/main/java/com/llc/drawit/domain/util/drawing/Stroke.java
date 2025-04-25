package com.llc.drawit.domain.util.drawing;

import android.graphics.Color;
import android.graphics.Path;

public class Stroke {
    public int color;
    public int strokeWidth;
    public Path path;
    public String text;
    public long timestamp;

    public Stroke(String text, long timestamp) {
        this.color = Color.BLACK;
        this.strokeWidth = 3;
        this.path = null;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Stroke(int color, int strokeWidth, Path path, long timestamp) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
        this.text = null;
        this.timestamp = timestamp;
    }

    public Stroke(int color, int strokeWidth, long timestamp) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = null;
        this.text = null;
        this.timestamp = timestamp;
    }
}
