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

        PointF coordinates = new PointF();
        coordinates.x = rpda.getCurrentX() + VisualConstants.getTransitionLengthX(context);
        int offset = computeVerticalOffset(context);
        coordinates.y = VisualConstants.INITIAL_STATE_POSITION.y + offset * VisualConstants.getTransitionOffsetY(context);
        undoActions.push(new StateAction(rpda.addState(new VisualState(id, coordinates, offset))));
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
                rpda.setCurrentState(undo.getOriginState());
                rpda.getCurrentState().removeTransition(undo.getLink());
            } else {
                rpda.setCurrentState(undo.getOriginState());
                rpda.getCurrentState().removeTransition(undo.getLink());
            }
        }
    }

    public void redo() {
        if (redoActions.size() > 0) {
            RPDAAction redo = redoActions.pop();
            undoActions.push(redo);

            if (redo.getActionKind() == ActionKind.GENERATE_STATE) {
                rpda.getStates().put(redo.getActiveState().getId(), redo.getActiveState());
                rpda.getState(redo.getOriginState().getId()).addTransition(redo.getActiveState());
                rpda.setCurrentState(redo.getActiveState());
            } else {
                rpda.getState(redo.getOriginState().getId()).addTransition(redo.getLink());
                rpda.setCurrentState(redo.getActiveState());
            }
        }
    }

    private int computeVerticalOffset(Context context) {
        //int currentOffset = rpda.getCurrentState().getVerticalOffset();
        if (rpda.getCurrentState() != null) {
            int branchingStateOffset = rpda.getCurrentState().getNumberOfTransitions();
            int indirectOffset = rpda.computeIndirectOffset();
            if (branchingStateOffset >= indirectOffset)
                return branchingStateOffset;
            else {
                if (rpda.getCurrentState().willChangeToBranchingState())
                    return indirectOffset + 1;
                else return indirectOffset;
            }
        }
        else return 0;
    }
}
