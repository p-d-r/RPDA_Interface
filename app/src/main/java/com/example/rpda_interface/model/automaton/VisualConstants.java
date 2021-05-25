package com.example.rpda_interface.model.automaton;
import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;

public class VisualConstants {
    public static final PointF INITIAL_STATE_POSITION = new PointF(400, 600);
    public static final float transitionLengthX = 800;
    public static final float transitionOffsetY = 1200;

    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
    public static PointF getPointInDp(Context context, PointF point) {
        return new PointF(convertPixelsToDp(point.x, context), convertPixelsToDp(point.y, context));
    }
}
