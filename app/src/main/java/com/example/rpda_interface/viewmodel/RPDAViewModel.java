package com.example.rpda_interface.viewmodel;

import android.content.Context;
import android.graphics.PointF;

import com.example.rpda_interface.model.action.ActionKind;
import com.example.rpda_interface.model.action.LinkAction;
import com.example.rpda_interface.model.action.RPDAAction;
import com.example.rpda_interface.model.action.StateAction;
import com.example.rpda_interface.model.automaton.VisualConstants;
import com.example.rpda_interface.model.automaton.VisualRPDA;
import com.example.rpda_interface.model.automaton.VisualState;
import com.example.rpda_interface.model.automaton.VisualTransition;
import com.example.rpda_interface.repository.RSABaseRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class RPDAViewModel {

    private Stack<RPDAAction> undoActions;
    private Stack<RPDAAction> redoActions;
    private VisualRPDA rpda;
    private Context context;

    public RPDAViewModel(Context context) {
        this.context = context;
        undoActions = new Stack<>();
        redoActions = new Stack<>();
        rpda = new VisualRPDA(new VisualState(VisualConstants.getPointInDp(context,
                                              VisualConstants.INITIAL_STATE_POSITION), 0));
    }

    public VisualRPDA getRpda() {
        return this.rpda;
    }

    public void generateNewRpda(String name) {
        //rpda = new VisualRPDA(new VisualState(VisualConstants.getPointInDp(context,
        //        VisualConstants.INITIAL_STATE_POSITION), 0));
        rpda = new VisualRPDA(name);
        undoActions.clear();
        redoActions.clear();
        rpda.name = name;
    }

    public void handleStateAction(int id) {
        rpda.addStatePure(new VisualState(id));
        System.out.println("added state with id " + id );
       /* PointF coordinates = new PointF();
        coordinates.x = rpda.getCurrentX() + VisualConstants.transitionLengthX;
        int offset = computeVerticalOffset(context);
        coordinates.y = VisualConstants.INITIAL_STATE_POSITION.y + offset * VisualConstants.transitionOffsetY;
        undoActions.push(new StateAction(rpda.addState(new VisualState(id, coordinates, offset))));*/
    }

    public void generateTransitions(HashMap<Integer, Integer> transitions, HashMap<Integer, String> actions) {
        for (Map.Entry<Integer, Integer> transition : transitions.entrySet()) {
            rpda.insertLink(rpda.getState(transition.getKey()), rpda.getState(transition.getValue()), actions.get(transition.getKey()));
            System.out.println("Linked " + transition.getKey() + " to " + transition.getValue());
        }
    }

    public void generateTransitions(List<Integer> origins, List<Integer> targets, List<String> actions) {
        for (int i = 0; i < targets.size(); i++) {
            rpda.insertLink(rpda.getState(origins.get(i)), rpda.getState(targets.get(i)), actions.get(i));
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

    public boolean handleLinkAction(int id) {

        if (!rpda.getStates().containsKey(id))
            return false;

        VisualTransition trans = rpda.insertLink(rpda.getCurrentState(), rpda.getState(id));
        rpda.setCurrentState(trans.getTarget());
        undoActions.push(new LinkAction(trans));
        return true;
    }

    public void undo() {
        if (undoActions.size() > 0) {
            RPDAAction undo = undoActions.pop();
            redoActions.push(undo);

            if (undo.getActionKind() == ActionKind.GENERATE_STATE) {
                rpda.removeState(undo.getActiveState());
            }
            rpda.setCurrentState(undo.getOriginState());
            rpda.getCurrentState().removeTransition(undo.getLink());
        }
    }

    public void redo() {
        if (redoActions.size() > 0) {
            RPDAAction redo = redoActions.pop();
            undoActions.push(redo);

            if (redo.getActionKind() == ActionKind.GENERATE_STATE) {
                rpda.getStates().put(redo.getActiveState().getId(), redo.getActiveState());
                rpda.getState(redo.getOriginState().getId()).addTransition(redo.getActiveState());
            } else {
                rpda.getState(redo.getOriginState().getId()).addTransition(redo.getLink());
            }
            rpda.setCurrentState(redo.getActiveState());
        }
    }
}
