package com.example.rpda_interface.model.automaton;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class VisualRPDA
{
    private HashMap<Integer, VisualState> states;
    private VisualState currentState;
    private VisualState initialState;
    public String name;
    private ArrayList<PointF> usedPositions = new ArrayList<>();

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

    public VisualState getState(int id) {
       return states.get(id);
    }
    public VisualState getInitialState() {return initialState;}

    public VisualTransition insertLink(VisualState origin, VisualState target) {
        VisualTransition trans = new VisualTransition(origin, target);
        origin.addTransition(trans);
        return trans;
    }

    public VisualTransition insertLink(VisualState origin, VisualState target, String action, String crit, String sample) {
        VisualTransition trans = new VisualTransition(origin, target, action, crit, sample);
        origin.addTransition(trans);
        return trans;
    }

    public void addStatePure(VisualState state) {
        states.put(state.getId(), state);
        if (states.size() == 1) {
            initialState = state;
        }
    }

    public void setCurrentState(VisualState state) {
        currentState = state;
    }

    public int getClosestState(float x, float y) {
        float minDist = Float.MAX_VALUE;
        int id = -1;
        for (Map.Entry<Integer, VisualState> state : states.entrySet()) {
            if (state.getValue().getCenterPosition() == null)
                continue;
            float tempDistX = x - state.getValue().getCenterPosition().x;
            float tempDistY = y - state.getValue().getCenterPosition().y;
            final double sqrt = Math.sqrt(Math.pow(tempDistX, 2) + Math.pow(tempDistY, 2));
            if (sqrt < minDist) {
                minDist = (float) sqrt;
                id = state.getValue().getId();
            }
        }

        return id;
    }

    public void computeStatePositionsDepthFirst(HashSet<Integer> usedIds, VisualState state, VisualState predecessor, int branchNum) {
         if (!usedIds.contains(state.getId())) {
             usedIds.add(state.getId());
             if (predecessor == null) {
                 state.setPosition(new PointF(VisualConstants.INITIAL_STATE_POSITION.x, VisualConstants.INITIAL_STATE_POSITION.y));
             } else {
                 PointF pos = new PointF(predecessor.getCenterPosition().x + VisualConstants.transitionLengthX,
                                         predecessor.getCenterPosition().y + VisualConstants.transitionOffsetY * branchNum);

                 while (posAlreadyAssigned(pos))
                     pos.y += VisualConstants.transitionOffsetY;

                 state.setPosition(pos);
             }

             for (int i = 0; i < state.getSuccessorStates().size(); i++) {
                 computeStatePositionsDepthFirst(usedIds, state.getSuccessorStates().get(i), state, i);
             }
         }
    }

    private boolean posAlreadyAssigned(PointF pos) {
        for (Map.Entry<Integer, VisualState> state : states.entrySet()) {
            if (state.getValue().getCenterPosition() != null) {
                if (pos.x == state.getValue().getCenterPosition().x && pos.y == state.getValue().getCenterPosition().y)
                    return true;
            }
        }

        return false;
    }
}
