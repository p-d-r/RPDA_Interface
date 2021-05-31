package com.example.rpda_interface.model.automaton;

public class VisualTransition
{
    private VisualState origin;
    private VisualState target;
    private String action;
    private static int numberOfTransitions;
    private int id;

    VisualTransition(VisualState origin, VisualState target) {
        this.id = numberOfTransitions;
        numberOfTransitions++;
        this.origin = origin;
        this.target = target;
        this.action = "no info";
    }


    VisualTransition(VisualState origin, VisualState target, String action) {
        this.id = numberOfTransitions;
        numberOfTransitions++;
        this.origin = origin;
        this.target = target;
        this.action = action;
    }


    public VisualState getOrigin() {
        return origin;
    }

    public VisualState getTarget() {
        return target;
    }

    public String getAction() { return action; }
}
