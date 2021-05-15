package com.example.rpda_interface.model.automaton;
import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;

public class VisualConstants {
    public static final PointF INITIAL_STATE_POSITION = new PointF(400, 1000);
    private static final float transitionLengthX = 1350;
    private static final float transitionOffsetY = 1500;

    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float getTransitionLengthX(Context context) {return convertPixelsToDp(transitionLengthX, context);}
    public static float getTransitionOffsetY(Context context) {return convertPixelsToDp(transitionOffsetY, context);}
    public static PointF getPointInDp(Context context, PointF point) {
        return new PointF(convertPixelsToDp(point.x, context), convertPixelsToDp(point.y, context));
    }
}
