package com.example.rpda_interface.model.automaton;

import com.example.rpda_interface.model.action.ActionKind;

public class VisualTransition
{
    private VisualState origin;
    private VisualState target;
    private ActionKind action;
    private static int numberOfTransitions;
    private int id;

    VisualTransition(VisualState origin, VisualState target) {
        this.id = numberOfTransitions;
        numberOfTransitions++;
        this.origin = origin;
        this.target = target;
    }



    public VisualState getOrigin() {
        return origin;
    }

    public VisualState getTarget() {
        return target;
    }
}
