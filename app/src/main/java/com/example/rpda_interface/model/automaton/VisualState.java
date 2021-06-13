package com.example.rpda_interface.model.automaton;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

public class VisualState implements Comparable
{
    private static int numberOfStates = 0;
    private int id;
    private ArrayList<VisualTransition> transitions;
    private PointF centerPosition;
    public boolean isBranchingState;
    public boolean current;



    public VisualState(PointF centerPosition){
        this.id = numberOfStates;
        numberOfStates++;
        this.centerPosition = centerPosition;
        transitions = new ArrayList<>();
    }

    public void setPosition(PointF centerPosition) {
        this.centerPosition = centerPosition;
    }

    public VisualState(int id) {
        this.id = id;
        transitions = new ArrayList<>();
    }

    public int getId() {
        return this.id;
    }

    public PointF getCenterPosition() {return this.centerPosition;}

    public List<VisualState> getSuccessorStates() {
        ArrayList<VisualState> successorStates = new ArrayList<>();
        for (VisualTransition trans : transitions) {
            successorStates.add(trans.getTarget());
        }
        return successorStates;
    }

    public void addTransition(VisualTransition trans) {
        transitions.add(trans);
    }

    public List<VisualState> printStateAndTransitions(Canvas canvas, Paint mPaint, Paint aPaint, Paint textPaint, Paint branchPaint) {
        int rad = 50;

        float x = this.centerPosition.x;
        float y = this.centerPosition.y;

        canvas.drawText(Integer.toString(id), x - 25, y + 25, textPaint);
        if (current)
            canvas.drawCircle(x, y, rad, aPaint);
        else if (isBranchingState)
            canvas.drawCircle(x, y, rad, branchPaint);
        else
            canvas.drawCircle(x, y, rad, mPaint);


        for (VisualTransition trans : transitions) {
            float targetX = trans.getTarget().getCenterPosition().x;
            float targetY = trans.getTarget().getCenterPosition().y;
            if (trans.getTarget().getId() < id && y == targetY) {
                float length = Math.abs(x - targetX);
                canvas.drawArc(targetX,  y -350-length/30, x, y+350+length/30 ,
                        180, 180, false, mPaint);
            } else {
                PointF p = new PointF(x - targetX,
                        y - trans.getTarget().getCenterPosition().y);

                double len = Math.sqrt((Math.pow((x - trans.getTarget().getCenterPosition().x), 2)
                             + Math.pow(y - trans.getTarget().getCenterPosition().y, 2)));

                PointF trnsMiddle = new PointF((float) ((p.x / len) * len / 4), (float) ((p.y / len) * len / 2));

                p.x = (float) (p.x / len) * rad;
                p.y = (float) (p.y / len) * rad;

                canvas.drawLine(x - p.x, y - p.y,
                        targetX + p.x,
                        targetY + p.y, mPaint);

                if (!isBranchingState) {
                    if (!"move fix".equals(trans.getAction()))
                        canvas.drawText(trans.getAction(), x - trnsMiddle.x, y - trnsMiddle.y, textPaint);
                } else {
                    float newY;
                    if (y == targetY) //first branch: same height as state itself
                        newY = y;
                    else
                        newY = targetY + trnsMiddle.y;

                    canvas.drawText(trans.getAction(), targetX + trnsMiddle.x, newY, textPaint);
                }

                if (trans.getCriterion() != null && !"*".equals(trans.getCriterion()))
                    canvas.drawText(trans.getCriterion(), x - trnsMiddle.x/4, y - trnsMiddle.y/5, textPaint);
            }
        }
        return getSuccessorStates();
    }


    @Override
    public String toString(){
        String desc = "id:" + id;

        for (VisualTransition trans : transitions){
            desc += "\n" + trans.toString();
        }
        return desc;
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof VisualState)) return false;
        else{
            VisualState ob = (VisualState) o;
            return ob.getId() == this.id;
        }

    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof VisualState)) return 1;
        else{
            VisualState ob = (VisualState) o;
            if (ob.getId() == this.id) return 0;
        }

        return 1;
    }
}