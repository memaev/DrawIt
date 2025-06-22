package com.llc.drawit.presentation.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector; // Added
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.llc.drawit.domain.util.drawing.CPoint;
import com.llc.drawit.domain.util.drawing.DrawingInstrument;
import com.llc.drawit.domain.util.callbacks.DrawingUpdateListener;
import com.llc.drawit.domain.util.callbacks.OnAddText;
import com.llc.drawit.domain.util.drawing.Stroke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;

import lombok.Getter;

public class DrawView extends View {

    private static final int BRUSH_SIZE = 5;
    private static final int ERASE_SIZE = 15;
    private static final int COLOR_PEN = Color.RED;
    private static final float TOUCH_TOLERANCE = 4;
    private static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final float CANVAS_SCALE_FACTOR = 2.0f; // how much the largeBitmap is bigger than the actual screen size

    private float mX, mY;
    private Paint paint;
    private Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);
    private Paint textPaint = new Paint();
    private Path currentDrawingPath;
    private Stroke currentStroke;


    private DrawingUpdateListener drawingUpdateListener;
    private LinkedHashMap<Stroke, ArrayList<CPoint>> paths = new LinkedHashMap<>();
    @Getter
    private DrawingInstrument currentInstrument = DrawingInstrument.PEN;
    private int currentColor;
    private OnAddText onAddTextListener;

    // Offset of the top left side of the view from top left side of largeBitmap
    private float customXOffset = 0f;
    private float customYOffset = 0f;

    private Canvas largeCanvas;
    private Bitmap largeBitmap;
    private int canvasWidth;
    private int canvasHeight;

    private static final int INVALID_POINTER_ID = -1; // default const for unassigned pointer id
    private int activePointerId = INVALID_POINTER_ID; // For 1-finger drawing/panning
    private boolean isDrawing = false; // True if actively drawing with one finger

    // For 2-finger manual pan (if ScaleGestureDetector doesn't handle all pan)
    private boolean isTwoFingerPanning = false;
    private int panPointerId1 = INVALID_POINTER_ID;
    private int panPointerId2 = INVALID_POINTER_ID;
    private float lastPanFocalX_screen, lastPanFocalY_screen;


    private final ScaleGestureDetector scaleGestureDetector; // for pinch-zoom gestures detection
    private float currentScaleFactor = 1.0f; // current zoom
    private static final float MIN_SCALE_FACTOR = 0.25f; // can zoom out to minimum 1/4th size
    private static final float MAX_SCALE_FACTOR = 4.0f;  // can zoom in to maximum 4x size


    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(COLOR_PEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setXfermode(null);
        paint.setAlpha(0xff);

        textPaint.setTextSize(70);
        textPaint.setColor(Color.BLACK);

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void init(OnAddText onAddTextListener, DisplayMetrics metrics, LiveData<LinkedHashMap<Stroke, ArrayList<CPoint>>> drawings, LifecycleOwner lifecycleOwner) {
        this.onAddTextListener = onAddTextListener;
        initializeBitmap(metrics);

        drawings.observe(lifecycleOwner, data -> {
            if (data == null) return;
            paths.clear();
            paths.putAll(data);
            redrawLargeBitmapContent();
            invalidate();
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0 && largeBitmap == null) {
            if (canvasWidth == 0 || canvasHeight == 0) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                initializeBitmap(metrics);
            }
            // Ensure scroll and scale are valid after size change
            clampScroll();
            invalidate();
        }
    }

    private void initializeBitmap(DisplayMetrics metrics) {
        canvasWidth = (int) (metrics.widthPixels * CANVAS_SCALE_FACTOR);
        canvasHeight = (int) (metrics.heightPixels * CANVAS_SCALE_FACTOR);

        if (largeBitmap != null) {
            largeBitmap.recycle();
        }
        largeBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        largeCanvas = new Canvas(largeBitmap);
        largeCanvas.drawColor(DEFAULT_BG_COLOR);

        currentColor = COLOR_PEN;
        // After initializing, ensure scroll is clamped, especially if scale factor isn't 1.
        clampScroll();
        invalidate();
    }

    private void redrawLargeBitmapContent() {
        if (largeCanvas == null || largeBitmap == null) return;
        largeCanvas.drawColor(DEFAULT_BG_COLOR);
        paths.forEach((stroke, points) -> {
            if (stroke.text != null && !points.isEmpty()) {
                CPoint pos = points.get(0);
                textPaint.setColor(stroke.color);
                largeCanvas.drawText(stroke.text, pos.x(), pos.y(), textPaint);
            } else {
                Path pathForStroke = stroke.path;
                if (pathForStroke == null && !points.isEmpty()) {
                    pathForStroke = buildPathFromPoints(points);
                    stroke.path = pathForStroke;
                }
                if (pathForStroke != null) {
                    paint.setColor(stroke.color);
                    paint.setStrokeWidth((stroke.color == Color.WHITE) ? ERASE_SIZE : stroke.strokeWidth);
                    paint.setMaskFilter(null);
                    largeCanvas.drawPath(pathForStroke, paint);
                }
            }
        });
    }

    private void cancelCurrentDrawing() {
        if (isDrawing && currentStroke != null) {
            paths.remove(currentStroke); // Remove from our map of strokes
            // Need to revert largeCanvas to state before this stroke was drawn
            redrawLargeBitmapContent(); // Simplest way to revert
        }
        currentDrawingPath = null;
        currentStroke = null;
        isDrawing = false;
    }


    public void setCurrentColor(int color) {
        this.currentColor = color;
        if (currentInstrument == DrawingInstrument.TEXT) {
            textPaint.setColor(color);
        }
    }

    public void setCurrentInstrument(DrawingInstrument drawingInstrument) {
        this.currentInstrument = drawingInstrument;
        if (drawingInstrument == DrawingInstrument.TEXT) {
            textPaint.setColor(this.currentColor);
        }
    }

    public void addText(CPoint screenPos, String text) {
        if (largeCanvas == null) return;
        // Convert screen position to canvas position considering scroll and scale
        float canvasX = customXOffset + (screenPos.x() / currentScaleFactor);
        float canvasY = customYOffset + (screenPos.y() / currentScaleFactor);
        CPoint canvasPos = new CPoint(canvasX, canvasY);

        Stroke stroke = new Stroke(text, System.currentTimeMillis());
        stroke.color = this.currentColor;
        ArrayList<CPoint> pointList = new ArrayList<>();
        pointList.add(canvasPos);
        paths.put(stroke, pointList);

        textPaint.setColor(stroke.color);
        largeCanvas.drawText(text, canvasPos.x(), canvasPos.y(), textPaint);
        invalidate();

        Executors.newSingleThreadExecutor().execute(() -> {
            if (drawingUpdateListener != null) {
                drawingUpdateListener.addDrawing(new Pair<>(stroke, pointList));
            }
        });
    }

    public void addListener(DrawingUpdateListener drawingUpdateListener) {
        this.drawingUpdateListener = drawingUpdateListener;
    }

    private Path buildPathFromPoints(ArrayList<CPoint> points) {
        Path newPath = new Path();
        if (points.isEmpty()) return newPath;
        CPoint firstPoint = points.get(0);
        newPath.moveTo(firstPoint.x(), firstPoint.y());
        for (int i = 1; i < points.size(); ++i) {
            CPoint p = points.get(i);
            newPath.lineTo(p.x(), p.y());
        }
        return newPath;
    }

    @Override
    protected void onDraw(@NonNull Canvas viewCanvas) {
        super.onDraw(viewCanvas);
        if (largeBitmap == null || largeCanvas == null || getWidth() == 0 || getHeight() == 0) {
            viewCanvas.drawColor(DEFAULT_BG_COLOR);
            return;
        }

        viewCanvas.save();

        // Define the source rectangle from largeBitmap (what part to show)
        float viewPortWidthOnCanvas = getWidth() / currentScaleFactor;
        float viewPortHeightOnCanvas = getHeight() / currentScaleFactor;

        Rect srcRect = new Rect(
                (int) customXOffset,
                (int) customYOffset,
                (int) (customXOffset + viewPortWidthOnCanvas),
                (int) (customYOffset + viewPortHeightOnCanvas)
        );

        // Define the destination rectangle on the view's canvas (entire view)
        Rect destRect = new Rect(0, 0, getWidth(), getHeight());

        viewCanvas.drawBitmap(largeBitmap, srcRect, destRect, bitmapPaint);
        viewCanvas.restore();
    }

    private void touchStart(float screenX, float screenY) {
        // Convert screen to canvas coords
        float canvasX = customXOffset + (screenX / currentScaleFactor);
        float canvasY = customYOffset + (screenY / currentScaleFactor);

        currentDrawingPath = new Path();
        int strokeColor = (currentInstrument == DrawingInstrument.ERASE) ? Color.WHITE : currentColor;
        int strokeWidth = (currentInstrument == DrawingInstrument.ERASE) ? ERASE_SIZE : BRUSH_SIZE;
        currentStroke = new Stroke(strokeColor, strokeWidth, currentDrawingPath, System.currentTimeMillis());

        paths.put(currentStroke, new ArrayList<>()); // Add to map
        paths.get(currentStroke).add(new CPoint(canvasX, canvasY)); // Add first point

        currentDrawingPath.moveTo(canvasX, canvasY);
        mX = canvasX; // Store canvas coordinates
        mY = canvasY;
        isDrawing = true;
    }

    private void touchMove(float screenX, float screenY) {
        if (!isDrawing || currentDrawingPath == null || currentStroke == null) return;

        float canvasX = customXOffset + (screenX / currentScaleFactor);
        float canvasY = customYOffset + (screenY / currentScaleFactor);

        float dx = Math.abs(canvasX - mX);
        float dy = Math.abs(canvasY - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            currentDrawingPath.quadTo(mX, mY, (canvasX + mX) / 2, (canvasY + mY) / 2);

            // Draw the new segment onto largeCanvas
            paint.setColor(currentStroke.color);
            paint.setStrokeWidth((currentStroke.color == Color.WHITE) ? ERASE_SIZE : currentStroke.strokeWidth);
            largeCanvas.drawPath(currentDrawingPath, paint); // This draws the entire path so far.
            // For efficiency, could draw only new segments.

            mX = canvasX;
            mY = canvasY;
            paths.get(currentStroke).add(new CPoint(canvasX, canvasY)); // Add point to stroke's list
            invalidate(); // Request redraw of the view
        }
    }

    private void touchUp() {
        if (!isDrawing || currentDrawingPath == null || currentStroke == null) return;
        // Path already drawn on largeCanvas during touchMove

        Executors.newSingleThreadExecutor().execute(() -> {
            if (drawingUpdateListener != null && paths.containsKey(currentStroke)) {
                drawingUpdateListener.addDrawing(new Pair<>(currentStroke, paths.get(currentStroke)));
            }
        });
        // Don't nullify currentDrawingPath/Stroke here, reset in ACTION_UP/CANCEL
        isDrawing = false;
    }

    private void clampScroll() {
        if (getWidth() == 0 || getHeight() == 0 || largeBitmap == null) return;

        float effectiveViewportWidth = getWidth() / currentScaleFactor;
        float effectiveViewportHeight = getHeight() / currentScaleFactor;

        float maxScrollX = Math.max(0, canvasWidth - effectiveViewportWidth);
        float maxScrollY = Math.max(0, canvasHeight - effectiveViewportHeight);

        customXOffset = Math.max(0, Math.min(customXOffset, maxScrollX));
        customYOffset = Math.max(0, Math.min(customYOffset, maxScrollY));

        // If viewport is larger than canvas (zoomed out too much), center it
        if (effectiveViewportWidth >= canvasWidth) {
            customXOffset = (canvasWidth - effectiveViewportWidth) / 2f;
        }
        if (effectiveViewportHeight >= canvasHeight) {
            customYOffset = (canvasHeight - effectiveViewportHeight) / 2f;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (largeBitmap == null) return false;

        // Let the ScaleGestureDetector inspect all events.
        scaleGestureDetector.onTouchEvent(event);

        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        // If scaling, don't process other touch events for drawing/manual panning
        if (scaleGestureDetector.isInProgress()) {
            isDrawing = false; // Ensure drawing is stopped
            activePointerId = INVALID_POINTER_ID;
            isTwoFingerPanning = false; // Scaler handles focal point adjustment
            return true; // Event handled by scaler
        }
        // If consumedByScaler is true but not inProgress (e.g. onScaleEnd)

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = pointerId;
                isDrawing = false; // Will be set true in touchStart if applicable
                isTwoFingerPanning = false;

                if (currentInstrument == DrawingInstrument.TEXT) {
                    if (onAddTextListener != null) {
                        onAddTextListener.invoke(new CPoint(event.getX(), event.getY()));
                    }
                } else {
                    touchStart(event.getX(), event.getY());
                }
                invalidate();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                // Second finger down
                if (isDrawing) { // If 1-finger drawing was in progress, cancel it
                    cancelCurrentDrawing();
                }
                activePointerId = INVALID_POINTER_ID; // No single active drawing pointer
                isTwoFingerPanning = true;
                panPointerId1 = event.getPointerId(0);
                panPointerId2 = event.getPointerId(1); // This is the new pointer
                // Calculate initial focal point for manual panning
                if (event.getPointerCount() >= 2) {
                    float x0 = event.getX(event.findPointerIndex(panPointerId1));
                    float y0 = event.getY(event.findPointerIndex(panPointerId1));
                    float x1 = event.getX(event.findPointerIndex(panPointerId2));
                    float y1 = event.getY(event.findPointerIndex(panPointerId2));
                    lastPanFocalX_screen = (x0 + x1) / 2f;
                    lastPanFocalY_screen = (y0 + y1) / 2f;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDrawing && activePointerId != INVALID_POINTER_ID) {
                    int currentActiveIdx = event.findPointerIndex(activePointerId);
                    if (currentActiveIdx != -1) {
                        touchMove(event.getX(currentActiveIdx), event.getY(currentActiveIdx));
                    }
                } else if (isTwoFingerPanning && event.getPointerCount() >= 2 && panPointerId1 != INVALID_POINTER_ID && panPointerId2 != INVALID_POINTER_ID) {
                    int idx1 = event.findPointerIndex(panPointerId1);
                    int idx2 = event.findPointerIndex(panPointerId2);
                    if (idx1 != -1 && idx2 != -1) {
                        float currentFocalX = (event.getX(idx1) + event.getX(idx2)) / 2f;
                        float currentFocalY = (event.getY(idx1) + event.getY(idx2)) / 2f;

                        float deltaX_screen = currentFocalX - lastPanFocalX_screen;
                        float deltaY_screen = currentFocalY - lastPanFocalY_screen;

                        // Convert screen pan delta to canvas pan delta (scaled)
                        customXOffset -= deltaX_screen / currentScaleFactor;
                        customYOffset -= deltaY_screen / currentScaleFactor;

                        clampScroll();
                        invalidate();

                        lastPanFocalX_screen = currentFocalX;
                        lastPanFocalY_screen = currentFocalY;
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                int upPointerId = event.getPointerId(pointerIndex);
                if (isTwoFingerPanning && (upPointerId == panPointerId1 || upPointerId == panPointerId2)) {
                    isTwoFingerPanning = false;
                    // Transition to 1-finger mode if a finger remains
                    if (upPointerId == panPointerId1) activePointerId = panPointerId2;
                    else activePointerId = panPointerId1;

                    // If transitioning to 1-finger draw, reset mX, mY based on remaining finger
                    // For simplicity, we just stop panning. New 1-finger draw will start on next ACTION_DOWN/MOVE.
                    int remainingPointerIndex = event.findPointerIndex(activePointerId);
                    if(remainingPointerIndex != -1 && currentInstrument != DrawingInstrument.TEXT){
                        isDrawing = false; // Require a new touch down or move to start drawing
                    }

                    panPointerId1 = INVALID_POINTER_ID;
                    panPointerId2 = INVALID_POINTER_ID;

                } else if (upPointerId == activePointerId) { // A non-primary drawing finger lifted (rare for 1-finger draw)
                    if (isDrawing) touchUp();
                    activePointerId = INVALID_POINTER_ID; // No active drawing pointer
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isDrawing && activePointerId == pointerId) {
                    touchUp();
                }
                activePointerId = INVALID_POINTER_ID;
                isDrawing = false;
                isTwoFingerPanning = false;
                panPointerId1 = INVALID_POINTER_ID;
                panPointerId2 = INVALID_POINTER_ID;
                invalidate();
                break;
        }
        return true; // We are handling all touch events
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float focusCanvasX_atScaleStart, focusCanvasY_atScaleStart;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            // Cancel any ongoing 1-finger drawing or manual 2-finger pan
            if (isDrawing) {
                cancelCurrentDrawing();
            }
            isDrawing = false;
            isTwoFingerPanning = false;
            activePointerId = INVALID_POINTER_ID;


            float screenFocalX = detector.getFocusX();
            float screenFocalY = detector.getFocusY();

            // Point on the largeCanvas that is under the fingers
            focusCanvasX_atScaleStart = customXOffset + (screenFocalX / currentScaleFactor);
            focusCanvasY_atScaleStart = customYOffset + (screenFocalY / currentScaleFactor);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            currentScaleFactor *= detector.getScaleFactor();
            currentScaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(currentScaleFactor, MAX_SCALE_FACTOR));

            // Adjust scroll to keep the original canvas focal point under the new screen focal point
            float screenFocalX = detector.getFocusX();
            float screenFocalY = detector.getFocusY();

            customXOffset = focusCanvasX_atScaleStart - (screenFocalX / currentScaleFactor);
            customYOffset = focusCanvasY_atScaleStart - (screenFocalY / currentScaleFactor);

            clampScroll();
            invalidate();
            return true;
        }
    }
}