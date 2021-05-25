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

    public void insertLink(VisualState target) {
        currentState.addTransition(target);
        currentState = target;
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

    public void addStatePure(VisualState state) {
        states.put(state.getId(), state);
        if (states.size() == 1) {
            initialState = state;
        }
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

    public String getLatestCoordinates() {
        return "Position:  x:  " + states.get(states.size()-1).getCenterPosition().x + "     y: " + states.get(states.size()-1).getCenterPosition().y;
    }


    public int getClosestState(float x, float y) {
        float minDist = Float.MAX_VALUE;
        int id = -1;
        for (Map.Entry<Integer, VisualState> state : states.entrySet()) {
            float tempDistX = x - state.getValue().getCenterPosition().x;
            float tempDistY = y - state.getValue().getCenterPosition().y;
            if (Math.sqrt(Math.pow(tempDistX, 2) + Math.pow(tempDistY, 2)) < minDist) {
                minDist = (float) Math.sqrt(Math.pow(tempDistX, 2) + Math.pow(tempDistY, 2));
                id = state.getValue().getId();
            }
        }
        return id;
    }


    public void computeStateCoordinates() {
        PriorityQueue<VisualState> successors = new PriorityQueue<>();
        HashSet<Integer> usedIds = new HashSet<>();
        usedIds.add(initialState.getId());
        successors.add(initialState);
        int maxOffset = 0;

        initialState.setPosition(VisualConstants.INITIAL_STATE_POSITION, 0);
        while (!successors.isEmpty()) {
            VisualState currentState = successors.poll();
            List<VisualState> successorStates = currentState.getSuccessorStates();
            for (int i = 0; i < successorStates.size(); i++) {
                if (!usedIds.contains(successorStates.get(i).getId())) {
                    successors.add(successorStates.get(i));
                    PointF newP = new PointF(currentState.getCenterPosition().x, currentState.getCenterPosition().y);
                    newP.x+=VisualConstants.transitionLengthX;
                    while(!addPositionIfNotPresent(newP))
                        newP.y += VisualConstants.transitionOffsetY;
                    successorStates.get(i).setPosition(newP, 0);
                    usedIds.add(successorStates.get(i).getId());
                }
            }
        }
    }

    public void computeStatePositionsDepthFirst(List<Integer> usedIds, VisualState state, VisualState predecessor, int branchNum) {
        boolean contained = false;
         for (int i : usedIds) {
             if (i == state.getId())
                 contained = true;
         }
         if (!contained) {
             usedIds.add(state.getId());
             if (predecessor == null) {
                 state.setPosition(new PointF(VisualConstants.INITIAL_STATE_POSITION.x, VisualConstants.INITIAL_STATE_POSITION.y), 0);
             } else {
                 state.setPosition(new PointF(predecessor.getCenterPosition().x + VisualConstants.transitionLengthX,
                         predecessor.getCenterPosition().y + VisualConstants.transitionOffsetY * branchNum), 0);
             }

             for (int i = 0; i < state.getSuccessorStates().size(); i++) {
                 computeStatePositionsDepthFirst(usedIds, state.getSuccessorStates().get(i), state, i);
             }
         }
    }

    private boolean addPositionIfNotPresent(PointF pNew) {
        for (PointF p : usedPositions) {
            if (p.x == pNew.x && p.y == pNew.y) {
                return false;
            }
        }
        usedPositions.add(pNew);
        return true;
    }
}
