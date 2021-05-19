package com.example.rpda_interface.model.action;

public enum ActionKind {
    LINK_STATE,
    GENERATE_STATE,
    MOVE_ABS,
    MOVE_REL,
    MOVE_REL_OBJ,
    BRANCH,
    JUMP,
    MOVE,
    GRIPPER,
    EXECUTE,
    QUIT,
    CREATE_SUBTASK,
    SWITCH_SUBTASK,
    NO_ACTION,
    PUSH_POSE,
    PUSH_SUBTASK,
    PUSH_OBJ_ID,
    REQUEST_SET_INFO,
    PUSH_ABSOLUTE_POSE,
    UPDATE_AUTOMATON,
    FETCH_RPDA_SET,
    TAKE_POSE,
    POP,
    SAVE,
    RESTORE,
    AUTOMATON_SAVE,
    AUTOMATON_RESTORE,
    SAMPLE_ADD,
    SAMPLE_SPEC,
    ACTION_LAST		//should never occur
}
