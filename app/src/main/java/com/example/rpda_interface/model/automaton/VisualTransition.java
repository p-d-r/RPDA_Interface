package com.example.rpda_interface.model.automaton;

public class VisualTransition
{
    private VisualState origin;
    private VisualState target;
    private String action;
    private String crit;
    private static int numberOfTransitions;
    private int id;

    VisualTransition(VisualState origin, VisualState target) {
        this.id = numberOfTransitions;
        numberOfTransitions++;
        this.origin = origin;
        this.target = target;
        this.action = "no info";
    }

    VisualTransition(VisualState origin, VisualState target, String action, String symbol, String sample) {
        this.id = numberOfTransitions;
        numberOfTransitions++;
        this.origin = origin;
        this.target = target;
        this.action = action;
        crit = "";
        if ("2".equals(sample))
            crit += "tee_dunkel";
        else if ("3".equals(sample))
            crit += "tee_gelb";
        else
            crit += "*";
        if (symbol != null && !"".equals(symbol)) {
            if ("Base-Class".equals(symbol))
                crit += ":" + "*";
            else
                crit += ":" + symbol;
        }
    }

    public VisualState getTarget() {
        return target;
    }

    public String getAction() { return action; }

    public String getCriterion() { return crit; }
}
