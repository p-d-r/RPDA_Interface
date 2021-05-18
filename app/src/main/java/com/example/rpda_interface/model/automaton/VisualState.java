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
    private boolean isBranchingState;
    private int verticalOffset;



    public VisualState(PointF centerPosition, int verticalOffset){
        this.id = numberOfStates;
        numberOfStates++;
        this.centerPosition = centerPosition;
        this.verticalOffset = verticalOffset;
        transitions = new ArrayList<>();
    }

    public VisualState(int id, PointF centerPosition, int verticalOffset){
        this.id = id;
        this.centerPosition = centerPosition;
        this.verticalOffset = verticalOffset;
        transitions = new ArrayList<>();
    }

    public void setPosition(PointF centerPosition, int verticalOffset) {
        this.centerPosition = centerPosition;
        this.verticalOffset = verticalOffset;
    }

    public VisualState(int id) {
        this.id = id;
        transitions = new ArrayList<>();
    }

    public int getVerticalOffset() {
        return verticalOffset;
    }

    public int getId() {
        return this.id;
    }

    public PointF getCenterPosition() {return this.centerPosition;}

    public boolean isBranchingState() {return isBranchingState;}

    public List<VisualState> getSuccessorStates() {
        ArrayList<VisualState> successorStates = new ArrayList<>();
        for (VisualTransition trans : transitions) {
            successorStates.add(trans.getTarget());
        }
        return successorStates;
    }

    public int getNumberOfTransitions() {
        return transitions.size();
    }

    public ArrayList<VisualTransition> getTransitions() {
        return transitions;
    }

    public void addTransition(VisualTransition trans) {
        transitions.add(trans);
        if (transitions.size() > 1)
            isBranchingState = true;
    }

    public void addTransition(VisualState target) {
        transitions.add(new VisualTransition(this, target));
        if (transitions.size() > 1)
            isBranchingState = true;
    }

    public void removeTransition(VisualTransition trans) {
        transitions.remove(trans);
    }

    public void removeTransition(VisualState target) {
        for (VisualTransition trans : transitions) {
            if (trans.getTarget().equals(target)) {
                transitions.remove(trans);
            }
        }
    }


    public boolean willChangeToBranchingState() {
        return transitions.size() >= 1;
    }

    public List<VisualState> printStateAndTransitions(Canvas canvas, Paint statePaint) {
        canvas.drawCircle(this.centerPosition.x, this.centerPosition.y, 75, statePaint);

        for (VisualTransition trans : transitions) {
            canvas.drawLine(this.centerPosition.x, this.centerPosition.y, trans.getTarget().getCenterPosition().x, trans.getTarget().getCenterPosition().y, statePaint);
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