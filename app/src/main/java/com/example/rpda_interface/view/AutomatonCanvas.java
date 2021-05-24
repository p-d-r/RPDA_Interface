package com.example.rpda_interface.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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
    private Matrix matrix;
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


        matrix = new Matrix();

        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(8000, 5000);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float maxScale = 1.2f;
        float minScale = 0.9f;
        //if (scaleFactor != maxScale && scaleFactor != minScale) {
            canvas.save();
            float x = (pinchX + scrollX);
            float y = (pinchY + scrollY);
            canvas.scale(scaleFactor, scaleFactor, Math.min(pinchX, 5000), Math.min(pinchY, 5000));
            //canvas.concat(matrix);
            canvas.drawARGB(255, 255, 255, 255);
            printRPDAState(canvas);
            canvas.restore();
        //}

        canvas.drawRect(200,200,7800, 4800, mPaint);
        System.out.println("x: " + pinchX + "   y: " + pinchY + "         scale: " + scaleFactor);
        if (rpda != null) System.out.println(rpda.getLatestCoordinates());
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
        return scaleListener.onTouchEvent(event);
    }



    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            pinchX = detector.getFocusX();
            pinchY = detector.getFocusY();
            System.out.println("begin");
            return true;
        }


        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float maxScale = 1.2f;
            float minScale = 0.9f;
            float tempScale =  detector.getScaleFactor() * scaleFactor;
            scaleFactor = Math.max(minScale, Math.min(tempScale, maxScale));
            if (scaleFactor != maxScale && scaleFactor != minScale)
                matrix.postScale(scaleFactor, scaleFactor, pinchX, pinchY);
            invalidate();
            return true;
        }


        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            System.out.println("end");
        }

    }
}

