package com.example.rpda_interface.model;

public enum ActionKind {
    MOVE_ABS,
    MOVE_REL,
    MOVE_REL_OBJ,
    BRANCH,
    JUMP,
    BRANCH_TOPMOST_SYMBOL,
    BRANCH_STIMULUS_AND_TOPMOST,
    GRIPPER,
    CSTATE_CHANGED,
    STOP,
    GRAVCOMP,
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
    INSERT_LINK,
    SCAN_FOR_OBJ,
    RESET_SUBTASK,
    RESTART,
    POP,
}
