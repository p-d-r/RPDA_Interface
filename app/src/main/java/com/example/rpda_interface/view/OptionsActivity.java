package com.example.rpda_interface.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.rpda_interface.R;


public class OptionsActivity extends Activity {
    private EditText ipv4_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        ipv4_editor = findViewById(R.id.ipv4_address);
    }

    public void takeIp(View v) {
        Intent intent = new Intent();
        String ip = ipv4_editor.getText().toString();
        intent.putExtra("target_ip", ip);
        setResult(0, intent);
        finish();
    }
}
