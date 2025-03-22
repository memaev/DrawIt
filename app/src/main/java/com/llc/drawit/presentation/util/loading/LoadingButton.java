package com.llc.drawit.presentation.util.loading;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.llc.drawit.R;

public class LoadingButton extends LinearLayout {

    private ProgressBar progressBar;
    private TextView buttonTextView;
    private boolean isLoading = false;

    public LoadingButton(Context context) {
        super(context);
        init(context, null, 0);
    }

    public LoadingButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public LoadingButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        View root = LayoutInflater.from(context).inflate(R.layout.button_loading, this, true);
        buttonTextView = root.findViewById(R.id.button_text);
        progressBar = root.findViewById(R.id.button_progress_bar);

        loadAttr(context, attrs, defStyleAttr);
    }

    private void loadAttr(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) return;

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton, defStyleAttr, 0);

        setEnabled(true);
        buttonTextView.setEnabled(true);
        buttonTextView.setTextColor(arr.getColor(R.styleable.LoadingButton_android_textColor, Color.BLACK));
        setText(arr.getString(R.styleable.LoadingButton_android_text));

        setLoading(arr.getBoolean(R.styleable.LoadingButton_isLoading, false));
        progressBar.getIndeterminateDrawable().setColorFilter(arr.getColor(R.styleable.LoadingButton_progressBarColor, Color.BLACK), PorterDuff.Mode.SRC_IN);

        arr.recycle();
    }

    public void setLoading(boolean loading) {
        setClickable(!loading); // Disable click when loading
        isLoading = loading;

        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setText(String text) {
        buttonTextView.setText(text);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        buttonTextView.setEnabled(enabled);
    }
}
