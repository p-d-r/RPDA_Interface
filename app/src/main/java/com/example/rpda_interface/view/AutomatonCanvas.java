package com.example.rpda_interface.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.example.rpda_interface.model.automaton.VisualRPDA;
import com.example.rpda_interface.model.automaton.VisualState;
import com.example.rpda_interface.viewmodel.RPDAViewModel;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;


public class AutomatonCanvas extends View {

    private VisualRPDA rpda;
    private Paint mPaint, aPaint, textPaint;
    private int screenWidth, screenHeight;
    private Context context;
    private float scaleFactor = 1;
    private ScaleGestureDetector scaleListener;
    private RPDAViewModel rpdaViewModel;
    float pinchX;
    float pinchY;
    float scrollX;
    float scrollY;


    public AutomatonCanvas(Context context, RPDAViewModel rpdaViewModel) {
        super(context);
        this.context = context;
        this.rpdaViewModel = rpdaViewModel;
        scaleListener = new ScaleGestureDetector(context, new ScaleListener());

        rpda = rpdaViewModel.getRpda();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(5);
        mPaint.setTextSize(50);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(2);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(50);

        aPaint = new Paint();
        aPaint.setAntiAlias(true);
        aPaint.setStyle(Paint.Style.STROKE);
        aPaint.setColor(Color.GREEN);
        aPaint.setStrokeWidth(5);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);


        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(5000, 5000);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //canvas.save();
        float x = (pinchX + scrollX);
        float y = (pinchY + scrollY);
        canvas.scale(scaleFactor, scaleFactor, (pinchX+scrollX), pinchY + scrollY);
        canvas.drawARGB(255, 255, 255, 255);
        printRPDAState(canvas);
        //canvas.restore();
    }


    private void printRPDAState(Canvas canvas) {
        PriorityQueue<VisualState> closure = new PriorityQueue<>();
        closure.add(rpda.getInitialState());
        HashSet<Integer> usedIds = new HashSet<>();
        usedIds.add(0);

        while (!closure.isEmpty()) {
            List<VisualState> newstates = (closure.poll().printStateAndTransitions(canvas, mPaint, aPaint, textPaint));
            for (VisualState staten:newstates) {
                if (!usedIds.contains(staten.getId())) {
                    closure.add(staten);
                    usedIds.add(staten.getId());
                }
            }
        }
    }


    public void updateRpda() {
        rpda = rpdaViewModel.getRpda();
        this.invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleListener.onTouchEvent(event);
        return true;
    }



    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            pinchX = detector.getFocusX();
            pinchY = detector.getFocusY();
            return true;
        }


        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float maxScale = 5;
            scaleFactor *= Math.min(detector.getScaleFactor(), maxScale);
            invalidate();
            return true;
        }

    }
}

