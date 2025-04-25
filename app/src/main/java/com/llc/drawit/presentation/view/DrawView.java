package com.llc.drawit.presentation.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.MotionEvent;
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

public class DrawView extends View {

    public int BRUSH_SIZE = 5;
    public int ERASE_SIZE = 15;
    public static final int COLOR_PEN = Color.RED;
    public static final float TOUCH_TOLERANCE = 4;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;

    private float mX, mY;
    private Paint paint;
    private Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);
    private Paint textPaint = new Paint();
    private Path path;
    private Stroke currentStroke;
    private Canvas canvas;
    private Bitmap bitmap;

    private DrawingUpdateListener drawingUpdateListener;

    private LinkedHashMap<Stroke, ArrayList<CPoint>> paths = new LinkedHashMap<>();

    private DrawingInstrument currentInstrument = DrawingInstrument.PEN; //by default, the instrument is a pen (drawing)
    private int currentColor;

    private OnAddText onAddTextListener;

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

        DisplayMetrics defaultMetrics = new DisplayMetrics();
        defaultMetrics.widthPixels = getResources().getDisplayMetrics().widthPixels;
        defaultMetrics.heightPixels = getResources().getDisplayMetrics().heightPixels;

        textPaint.setTextSize(70); //the size of the text that can be inserted on the board
        textPaint.setColor(Color.BLACK);

        // Initialize with an empty paths list, replace with actual paths if available
        init(defaultMetrics);
    }

    public void init(DisplayMetrics metrics) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        currentColor = COLOR_PEN;
    }

    public void init(OnAddText onAddTextListener, DisplayMetrics metrics, LiveData<LinkedHashMap<Stroke, ArrayList<CPoint>>> drawings, LifecycleOwner lifecycleOwner) {
        this.onAddTextListener = onAddTextListener;

        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        currentColor = COLOR_PEN;

        drawings.observe(lifecycleOwner, data -> {
            if (data.isEmpty()){
                return;
            }
            paths = data;
            invalidate();
        });
    }

    public void setCurrentColor(int color) {
        this.currentColor = color;
    }

    public void setCurrentInstrument(DrawingInstrument drawingInstrument) {
        this.currentInstrument = drawingInstrument;
        if (drawingInstrument == DrawingInstrument.ERASE)
            this.currentColor = Color.WHITE;
    }

    public DrawingInstrument getCurrentInstrument() { return this.currentInstrument; }

    public void addText(CPoint pos, String text) {
        Stroke stroke = new Stroke(text, System.currentTimeMillis());
        ArrayList<CPoint> point = new ArrayList<>();
        point.add(pos);
        paths.put(stroke, point);
        invalidate();

        Executors.newSingleThreadExecutor().execute(() -> {
            if (drawingUpdateListener==null) return;
            drawingUpdateListener.addDrawing(new Pair<>(stroke, point));
        });
    }

    public void addListener(DrawingUpdateListener drawingUpdateListener) {
        this.drawingUpdateListener = drawingUpdateListener;
    }

    private Path buildPath(ArrayList<CPoint> points){
        Path path = new Path();
        if (points.isEmpty()) return path;

        float cX = points.get(0).x;
        float cY = points.get(0).y;
        path.moveTo(cX, cY);

        for (int i=1; i<(points.size()); ++i){
            float x = points.get(i).x;
            float y = points.get(i).y;
            path.lineTo(x, y);
        }

        return path;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.drawColor(DEFAULT_BG_COLOR);

        paths.forEach((stroke, points) -> {
            //determine what we should display: text or line (drawing)
            if (stroke.text != null) {
                CPoint pos = points.get(0);
                canvas.drawText(stroke.text, pos.x, pos.y, textPaint);
            } else {
                if (stroke.path == null) {
                    stroke.path = buildPath(points);
                }
                paint.setColor(stroke.color);
                if (stroke.color == Color.WHITE){
                    paint.setStrokeWidth(ERASE_SIZE);
                } else {
                    paint.setStrokeWidth(stroke.strokeWidth);
                }
                paint.setMaskFilter(null);

                canvas.drawPath(stroke.path, paint);
            }
        });

        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y) {
        path = new Path();

        if (currentInstrument == DrawingInstrument.ERASE)
            currentStroke = new Stroke(currentColor, ERASE_SIZE, path, System.currentTimeMillis());
        else
            currentStroke = new Stroke(currentColor, BRUSH_SIZE, path, System.currentTimeMillis());
        paths.put(currentStroke, new ArrayList<>());

        path.reset();
        path.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        if (path == null) return;
        float dx = Math.abs(x-mX);
        float dy = Math.abs(y-mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            path.quadTo(mX, mY, (x+mX)/2, (y+mY)/2);
            mX = x;
            mY = y;

            paths.get(currentStroke).add(new CPoint(x, y));
        }
    }

    private void touchUp() {
        if (path == null) return;
        path.lineTo(mX, mY);

        Executors.newSingleThreadExecutor().execute(() -> {
            if (drawingUpdateListener==null) return;
            drawingUpdateListener.addDrawing(new Pair<>(currentStroke, paths.get(currentStroke)));
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //determine whether the user wants to add text or draw a picture
                if (currentInstrument == DrawingInstrument.TEXT) {
                    if (onAddTextListener == null) return false;
                    onAddTextListener.invoke(new CPoint(x, y));
                    return true;
                }
                touchStart(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }
}
