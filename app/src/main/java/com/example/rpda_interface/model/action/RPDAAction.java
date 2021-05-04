package com.example.rpda_interface.model.action;

import com.example.rpda_interface.model.automaton.VisualState;
import com.example.rpda_interface.model.automaton.VisualTransition;

public abstract class RPDAAction {
    protected ActionKind actionKind;
    protected VisualTransition link;

    public RPDAAction(VisualTransition link) {
        this.link = link;

    }

    public VisualState getOriginState() {
        return link.getOrigin();
    }

    public VisualState getActiveState() {
        return link.getTarget();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RPDAAction))
            return false;
        else {
            RPDAAction act = (RPDAAction) o;
            return this.actionKind == act.getActionKind() && this.link.equals(act.getLink());
        }
    }

    public ActionKind getActionKind() {
        return this.actionKind;
    }

    public VisualTransition getLink() {
        return this.link;
    }
}
