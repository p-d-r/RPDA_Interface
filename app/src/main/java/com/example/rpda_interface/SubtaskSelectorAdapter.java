package com.example.rpda_interface;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.example.rpda_interface.model.automaton.RpdaSet;
import com.example.rpda_interface.view.SubtaskSelectorActivity;

public class SubtaskSelectorAdapter extends RecyclerView.Adapter<SubtaskSelectorAdapter.ViewHolder>{

    private RpdaSet rpdaSet;
    private SubtaskSelectorActivity activity;

    public SubtaskSelectorAdapter(RpdaSet rpdaSet) {
        this.rpdaSet = rpdaSet;
    }

    public SubtaskSelectorAdapter(RpdaSet rpdaSet, SubtaskSelectorActivity activity) {
        this.rpdaSet = rpdaSet;
        this.activity = activity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public Button rpdaButton;

        public ViewHolder(View itemView) {
            super(itemView);

            rpdaButton = (Button) itemView.findViewById(R.id.rpdaViewButton);
            rpdaButton.setOnClickListener(activity);
        }
    }

    @Override
    public SubtaskSelectorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View rpdaView = inflater.inflate(R.layout.recycler_rpda_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new SubtaskSelectorAdapter.ViewHolder(rpdaView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(SubtaskSelectorAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        String rpda = rpdaSet.get(position);

        Button button = holder.rpdaButton;
        button.setText(rpda);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return rpdaSet.getItemCount();
    }
}
