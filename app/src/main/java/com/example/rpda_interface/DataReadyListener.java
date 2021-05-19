package com.example.rpda_interface;

import com.example.rpda_interface.model.action.ActionKind;

import java.io.Serializable;

public interface DataReadyListener extends Serializable {
    public void onDataReady();
}
