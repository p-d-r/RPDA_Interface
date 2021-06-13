package com.example.rpda_interface.viewmodel;

import android.content.Context;

import com.example.rpda_interface.model.automaton.VisualConstants;
import com.example.rpda_interface.model.automaton.VisualRPDA;
import com.example.rpda_interface.model.automaton.VisualState;

import java.util.List;


public class RPDAViewModel {

    private VisualRPDA rpda;

    public RPDAViewModel(Context context) {
        rpda = new VisualRPDA(new VisualState(VisualConstants.INITIAL_STATE_POSITION));
    }

    public VisualRPDA getRpda() {
        return this.rpda;
    }

    public void generateNewRpda(String name) {
        rpda = new VisualRPDA(name);
        rpda.name = name;
    }

    public void handleStateAction(int id) {
        rpda.addStatePure(new VisualState(id));
        System.out.println("added state with id " + id );
    }

    public void generateTransitions(List<Integer> origins, List<Integer> targets, List<String> actions, List<String> transitionCriteria, List<String> transitionSamples) {
        for (int i = 0; i < targets.size(); i++) {
            rpda.insertLink(rpda.getState(origins.get(i)), rpda.getState(targets.get(i)), actions.get(i), transitionCriteria.get(i), transitionSamples.get(i));
        }
    }

    public void generateTestRpda() {
        for (int i = 0; i < 26;  i++) {
            rpda.addStatePure(new VisualState(i));
        }

        rpda.insertLink(rpda.getState(0), rpda.getState(1));
        rpda.insertLink(rpda.getState(1), rpda.getState(2));
        rpda.insertLink(rpda.getState(2), rpda.getState(3));
        rpda.insertLink(rpda.getState(3), rpda.getState(4));
        rpda.insertLink(rpda.getState(4), rpda.getState(5));
        rpda.insertLink(rpda.getState(5), rpda.getState(6));
        rpda.insertLink(rpda.getState(6), rpda.getState(7));
        rpda.insertLink(rpda.getState(7), rpda.getState(8));
        rpda.insertLink(rpda.getState(8), rpda.getState(9));
        rpda.insertLink(rpda.getState(9), rpda.getState(10));
        rpda.insertLink(rpda.getState(10), rpda.getState(8));
        rpda.insertLink(rpda.getState(8), rpda.getState(11));
        rpda.insertLink(rpda.getState(11), rpda.getState(12));
        rpda.insertLink(rpda.getState(12), rpda.getState(13));
        rpda.insertLink(rpda.getState(13), rpda.getState(8));
        rpda.insertLink(rpda.getState(8), rpda.getState(14));
        rpda.insertLink(rpda.getState(14), rpda.getState(15));
        rpda.insertLink(rpda.getState(15), rpda.getState(16));
        rpda.insertLink(rpda.getState(16), rpda.getState(3));
        rpda.insertLink(rpda.getState(3), rpda.getState(17));
        rpda.insertLink(rpda.getState(17), rpda.getState(18));
        rpda.insertLink(rpda.getState(18), rpda.getState(19));
        rpda.insertLink(rpda.getState(19), rpda.getState(20));
        rpda.insertLink(rpda.getState(20), rpda.getState(2));
        rpda.insertLink(rpda.getState(2), rpda.getState(21));
        rpda.insertLink(rpda.getState(21), rpda.getState(22));
        rpda.insertLink(rpda.getState(22), rpda.getState(23));
        rpda.insertLink(rpda.getState(23), rpda.getState(24));
        rpda.insertLink(rpda.getState(24), rpda.getState(25));
        rpda.insertLink(rpda.getState(25), rpda.getState(22));
    }

    public void setCurrent(int id) {
        rpda.setCurrentState(rpda.getState(id));
        rpda.getState(id).current = true;
    }

    public void setBranchingState(int id) {
        rpda.getState(id).isBranchingState = true;
    }
}
