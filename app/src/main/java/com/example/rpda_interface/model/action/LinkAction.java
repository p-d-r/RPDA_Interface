package com.example.rpda_interface.model.action;

import com.example.rpda_interface.model.automaton.VisualTransition;


public class LinkAction extends RPDAAction {
    public LinkAction(VisualTransition link) {
        super(link);
        this.actionKind = ActionKind.LINK_STATE;
    }
}
