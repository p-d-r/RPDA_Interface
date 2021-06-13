package com.example.rpda_interface.model.automaton;

import java.util.ArrayList;

public class VisualRpdaStack {

    private ArrayList<String> rpdaStack;

    public VisualRpdaStack() {
        this.rpdaStack = new ArrayList<>();
        rpdaStack.add("          Stack           ");
    }

    public String get(int position) {
        if (position < rpdaStack.size())
            return rpdaStack.get(position);
        else return "";
    }

    public int getItemCount() {
        return rpdaStack.size();
    }

    public void addStackItem(String item) {
        rpdaStack.add(item);
    }

    public void clear() {
        rpdaStack.clear();
    }
}
