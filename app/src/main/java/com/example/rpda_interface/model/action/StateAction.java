package com.example.rpda_interface.model.action;

import com.example.rpda_interface.model.automaton.VisualTransition;

public class StateAction extends RPDAAction {

    public StateAction(VisualTransition link) {
        super(link);
        this.actionKind = ActionKind.GENERATE_STATE;
    }
}
