package com.example.rpda_interface.model.automaton;

import android.graphics.PointF;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

public class VisualRPDA
{
    private HashMap<Integer, VisualState> states;
    private int visualHeight;
    private int visualWidth;
    private VisualState currentState;
    private VisualState initialState;
    public String name;


    public VisualRPDA(VisualState initialState ){
        states = new HashMap<>();
        this.initialState = initialState;
        currentState = initialState;
        states.put(currentState.getId(), currentState);
    }

    public VisualRPDA(String name) {
        this.name = name;
        states = new HashMap<>();
    }



    public float getVisualHeight() {
        float maxY = 0;
        for (Map.Entry<Integer, VisualState> state : states.entrySet()) {
            if (state.getValue().getCenterPosition().y > maxY)
                maxY = state.getValue().getCenterPosition().y;
        }

        return maxY+50;
    }


    public float getVisualWidth() {
        float maxX = 0;
        for (Map.Entry<Integer, VisualState> state : states.entrySet()) {
            if (state.getValue().getCenterPosition().x > maxX)
                maxX = state.getValue().getCenterPosition().x;
        }

        return maxX+50;
    }

    public HashMap<Integer, VisualState> getStates() {
        return states;
    }

    public VisualState getState(int id) {
       return states.get(id);
    }

    public VisualState getInitialState() {return initialState;}

    public VisualTransition insertLink(VisualState origin, VisualState target) {
        VisualTransition trans = new VisualTransition(origin, target);
        origin.addTransition(trans);
        return trans;
    }

    public VisualTransition addState(VisualState target) {

        VisualTransition link = null;
        if (states.get(target.getId()) == null) {
            if (states.size() == 0) {
                initialState = target;
                currentState = target;
                target.setPosition(VisualConstants.INITIAL_STATE_POSITION, 0);

                states.put(target.getId(), target);
            } else {
                states.put(target.getId(), target);
                link = insertLink(currentState, target);
                currentState = target;
                }
        }
        return link;
    }

    public void addState(int id) {
        states.put(id, new VisualState(id));
    }

    public void removeState(VisualState state) {
        states.remove(state.getId());
    }

    public void setCurrentState(VisualState state) {
        currentState = state;
    }

    public VisualState getCurrentState() {
        return currentState;
    }

    public float getCurrentX() {
        if (currentState != null)
            return currentState.getCenterPosition().x;
        else return 0;
    }

    public float getCurrentY() {
        return currentState.getCenterPosition().y;
    }

    public void linkState(VisualState target) {
        currentState.addTransition(target);
        currentState = target;
    }


    public int computeIndirectOffset() {
        PriorityQueue<VisualState> successors = new PriorityQueue<>();
        HashSet<Integer> usedIds = new HashSet<>();
        successors.add(currentState);
        usedIds.add(currentState.getId());
        int maxOffset = 0;

        while (!successors.isEmpty()) {
            VisualState successor = successors.poll();
            usedIds.add(successor.getId());

            if (successor.getVerticalOffset() > maxOffset)
                maxOffset = successor.getVerticalOffset();

            for (VisualState testSuccessor : successor.getSuccessorStates()) {
                if (!usedIds.contains(testSuccessor.getId()))
                    successors.add(testSuccessor);
            }
        }

        return maxOffset;
    }
}
