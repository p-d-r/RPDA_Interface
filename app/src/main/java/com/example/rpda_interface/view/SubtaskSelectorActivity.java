package com.example.rpda_interface.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.rpda_interface.R;
import com.example.rpda_interface.SubtaskSelectorAdapter;
import com.example.rpda_interface.model.action.ActionKind;
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
        /*ActionKind action;

        try {
            Intent data = getIntent();
            String actionName = data.getStringExtra("action");
            action = ActionKind.valueOf(actionName);
        } catch(Exception e) {
            System.err.println("activity-communication failed!");
        }
        */
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