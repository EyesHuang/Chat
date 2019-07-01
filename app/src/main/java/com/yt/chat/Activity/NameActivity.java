package com.yt.chat.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yt.chat.Controller.ToastUtil;
import com.yt.chat.R;

public class NameActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputLayout mName;
    private Button mSavebtn;

    private DatabaseReference mNameDatabase;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        mName = findViewById(R.id.name_input);
        mSavebtn = findViewById(R.id.name_save_btn);

        String status_value = getIntent().getStringExtra(SettingsFragment.EXTRA_NAME);
        mName.getEditText().setText(status_value);

        mToolbar = findViewById(R.id.name_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Name");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mNameDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mSavebtn.setOnClickListener(v -> {
            String status = mName.getEditText().getText().toString();
            mNameDatabase.child("name").setValue(status).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ToastUtil.showToast(NameActivity.this, "Save New Name");
                    finish();
                } else {
                    Toast.makeText(NameActivity.this, task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();

                }


            });

        });
    }
}
