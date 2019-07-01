package com.yt.chat.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.yt.chat.R;

public class StartActivity extends AppCompatActivity {
    private Button mRegBtn;
    private Button mLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mRegBtn = findViewById(R.id.start_reg_btn);
        mRegBtn.setOnClickListener(v -> {
            Intent reg_intent = new Intent(StartActivity.this, RegisterActivity.class);
            startActivity(reg_intent);
        });

        mLoginBtn = findViewById(R.id.start_login_btn);
        mLoginBtn.setOnClickListener(v -> {
            Intent login_intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(login_intent);
        });

    }
}
