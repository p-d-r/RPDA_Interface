package com.example.rpda_interface;

import java.io.Serializable;

public interface DataReadyListener extends Serializable {
    public void onDataReady();
}
