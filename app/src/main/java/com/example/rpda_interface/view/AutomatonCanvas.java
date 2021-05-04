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
    private Paint mPaint;
    private int screenWidth, screenHeight;
    private Context context;
    private float scaleFactor = 1;
    private ScaleGestureDetector scaleListener;


    public AutomatonCanvas(Context context, RPDAViewModel rpdaViewModel) {
        super(context);
        this.context = context;
        scaleListener = new ScaleGestureDetector(context, new ScaleListener());

        rpda = rpdaViewModel.getRPDA();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(5);

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

        //setMeasuredDimension(rpda.getVisualHeight(), rpda.getVisualWidth());
        setMeasuredDimension(3000, 2000);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);
        canvas.drawARGB(255, 255, 255, 255);
        printRPDAState(canvas, rpda.getState(0), 200, screenHeight/2);
        canvas.restore();
    }

    private void printRPDAState(Canvas canvas, VisualState state, int x, int y) {
        PriorityQueue<VisualState> closure = new PriorityQueue<>();
        closure.add(rpda.getInitialState());
        HashSet<Integer> usedIds = new HashSet<>();
        usedIds.add(0);

        while (!closure.isEmpty()) {
            List<VisualState> newstates = (closure.poll().printStateAndTransitions(canvas, mPaint));
            for (VisualState staten:newstates) {
                if (!usedIds.contains(staten.getId())) {
                    closure.add(staten);
                    usedIds.add(staten.getId());
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleListener.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            invalidate();
            return true;
        }
    }

}

