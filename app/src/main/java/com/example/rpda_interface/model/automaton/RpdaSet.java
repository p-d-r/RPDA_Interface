package com.example.rpda_interface.model.automaton;

import java.io.Serializable;
import java.util.ArrayList;

public class RpdaSet implements Serializable {

    private ArrayList<String> names;
    private ArrayList<VisualRPDA> rpdas;

    public RpdaSet() {
        names = new ArrayList<>();
    }

    public int getItemCount() {
        return names.size();
    }

    public String get(int position) {
        return names.get(position);
    }

    public void addRpda(String name) {
        names.add(name);
    }
}
