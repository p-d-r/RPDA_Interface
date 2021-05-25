package com.example.rpda_interface.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.example.rpda_interface.LinkInsertedListener;
import com.example.rpda_interface.R;
import com.example.rpda_interface.model.automaton.VisualRPDA;
import com.example.rpda_interface.model.automaton.VisualState;
import com.example.rpda_interface.viewmodel.RPDAViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;


public class AutomatonCanvas extends View {

    private LinkInsertedListener linkInsertedListener;
    private VisualRPDA rpda;
    private Paint mPaint, aPaint, textPaint;
    private int screenWidth, screenHeight;
    private Context context;
    private float scaleFactor = 1;
    private ScaleGestureDetector scaleListener;
    private RPDAViewModel rpdaViewModel;
    private Matrix matrix;
    public int linkByTap = -1;
    private int last_id = -1;
    private int new_id = -1;
    float pinchX;
    float pinchY;
    float scrollX;
    float scrollY;
    float tapX;
    float tapY;


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

        canvas.save();
        float x = (pinchX + scrollX);
        float y = (pinchY + scrollY);
            //canvas.scale(scaleFactor, scaleFactor, Math.min(pinchX, 5000), Math.min(pinchY, 5000));
        canvas.concat(matrix);
        canvas.drawARGB(255, 255, 255, 255);
        printRPDAState(canvas);
        canvas.restore();

        canvas.drawRect(100,100,7900, 4900, mPaint);
        if (rpda != null && rpda.name != null)
            canvas.drawText(rpda.name, 100, 100, mPaint);
        System.out.println("x: " + pinchX + "   y: " + pinchY + "         scale: " + scaleFactor);
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
        rpda.computeStatePositionsDepthFirst(new ArrayList<Integer>(), rpda.getInitialState(), null, 0);
        this.invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean f = scaleListener.onTouchEvent(event);

        if (!scaleListener.isInProgress() && linkByTap != -1 && event.getAction() == MotionEvent.ACTION_DOWN) {
            tapX = event.getX();
            tapY = event.getY();
            int stateId = tapSearchState();

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Link to state " + stateId + "?");
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //rpda.insertLink( rpda.getState(stateId));
                    System.out.println("linked to "  + stateId);
                    linkInsertedListener.onLinkInserted(stateId);
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            linkByTap = -1;
            return true;
        }
        return f;
    }


    public void setLinkInsertedListener(LinkInsertedListener linkInsertedListener) {
        this.linkInsertedListener = linkInsertedListener;
    }

    public int tapSearchState() {
        System.out.println("before: " + tapX + ",      " + tapY);
        Matrix temp = new Matrix();
        float[] pt = {tapX, tapY};
        matrix.invert(temp);
        temp.mapPoints(pt);
        System.out.println("after:" + pt[0] + ",      " + pt[1]);

        int id = rpda.getClosestState(pt[0], pt[1]);


        System.out.println("found state: " + id);
        System.out.println("state-coordinates: (" + rpda.getState(id).getCenterPosition().x +
                                                    rpda.getState(id).getCenterPosition().y + ")");

        return id;
    }


    public void restoreDefaultZoom() {
        matrix.invert(matrix);
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
            float maxScale = 1.025f;
            float minScale = 0.975f;
            float tempScale =  detector.getScaleFactor() * scaleFactor;
            scaleFactor = Math.max(minScale, Math.min(tempScale, maxScale));
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

