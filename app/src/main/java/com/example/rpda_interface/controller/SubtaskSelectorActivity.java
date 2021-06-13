package com.example.rpda_interface.controller;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.rpda_interface.R;
import com.example.rpda_interface.model.ActionKind;
import com.example.rpda_interface.model.automaton.RpdaSet;

public class SubtaskSelectorActivity extends Activity implements View.OnClickListener {

    RpdaSet rpdaSet;
    ActionKind actionKind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtask_selector);

        RecyclerView rvRpdaSet = (RecyclerView) findViewById(R.id.subtask_recycler_view);

        rpdaSet = (RpdaSet) getIntent().getSerializableExtra("rpdaSet");
        actionKind = ActionKind.valueOf(getIntent().getStringExtra("action_name"));

        SubtaskSelectorAdapter adapter = new SubtaskSelectorAdapter(rpdaSet, this);
        rvRpdaSet.setAdapter(adapter);
        rvRpdaSet.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onClick(View v) {
        Button clicked = (Button) v;
        Intent intent = new Intent();
        intent.putExtra("resName", clicked.getText());
        intent.putExtra("action_name", actionKind.toString());
        setResult(1, intent);
        finish();
    }
}