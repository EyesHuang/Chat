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

public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mSavebtn;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mStatus = findViewById(R.id.status_input);
        mSavebtn = findViewById(R.id.status_save_btn);

        String status_value = getIntent().getStringExtra(SettingsFragment.EXTRA_STATUS);
        mStatus.getEditText().setText(status_value);

        mToolbar = findViewById(R.id.status_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mSavebtn.setOnClickListener(v -> {
            String status = mStatus.getEditText().getText().toString();
            mStatusDatabase.child("status").setValue(status).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ToastUtil.showToast(StatusActivity.this, "Save New Status");
                    finish();
                } else {
                    Toast.makeText(StatusActivity.this, task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();

                }


            });

        });

    }
}
