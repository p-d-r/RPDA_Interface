package com.example.rpda_interface.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.rpda_interface.R;
import com.example.rpda_interface.model.automaton.VisualRpdaStack;

public class StackRecyclerAdapter extends RecyclerView.Adapter<StackRecyclerAdapter.ViewHolder>{

    private VisualRpdaStack rpdaStack;
    private MainActivity activity;

    public StackRecyclerAdapter(VisualRpdaStack rpdaStack) {
        this.rpdaStack = rpdaStack;
    }

    public StackRecyclerAdapter(VisualRpdaStack rpdaStack, MainActivity activity) {
        this.rpdaStack = rpdaStack;
        this.activity = activity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemText;
        ImageView itemImage;

        public ViewHolder(View itemView) {
            super(itemView);

            itemText = itemView.findViewById(R.id.stack_item_text);
            itemImage = itemView.findViewById(R.id.stack_item_image);
        }
    }

    @Override
    public StackRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View rpdaStackView = inflater.inflate(R.layout.recycler_stack_item, parent, false);

        // Return a new holder instance
        StackRecyclerAdapter.ViewHolder viewHolder = new StackRecyclerAdapter.ViewHolder(rpdaStackView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(StackRecyclerAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        String item = rpdaStack.get(position);

        TextView itemText = holder.itemText;
        ImageView itemImage = holder.itemImage;
        itemText.setText(item);
        if ("tee gelb".equals(item))
            itemImage.setImageResource(R.mipmap.tee_gelb);
        else if ("tee dunkel".equals(item))
            itemImage.setImageResource(R.mipmap.tee_dunkel);
        else itemImage.setImageDrawable(null);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return rpdaStack.getItemCount();
    }

}
