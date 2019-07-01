package com.yt.chat.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yt.chat.R;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;
    private FirebaseAuth mAuth;
    private final String TAG = RegisterActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //  Component setup
        mDisplayName = findViewById(R.id.reg_display_name);
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_password);

        //  Toolbar setup
        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //  Button setup listener
        mCreateBtn = findViewById(R.id.reg_create_btn);
        mCreateBtn.setOnClickListener(v -> {
            String display_name = mDisplayName.getEditText().getText().toString().trim();
            String email = mEmail.getEditText().getText().toString().trim();
            String password = mPassword.getEditText().getText().toString();

            if (!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                register_user(display_name, email, password);
            } else {
                Toast.makeText(RegisterActivity.this, "Please input your information",
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    private void register_user(String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        storeDatabase(display_name);
                    } else {
                        new AlertDialog.Builder(RegisterActivity.this)
                                .setTitle("Sign Up")
                                .setMessage(task.getException().getMessage())
                                .setPositiveButton("OK", null)
                                .show();


                        // If sign in fails, display a message to the user.
                        /*Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                Toast.LENGTH_LONG).show();*/
                    }
                });
    }

    private void storeDatabase(String display_name) {
        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = current_user.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(uid);

        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("name", display_name);
        userMap.put("status", "Hi, I am using Chat App");
        userMap.put("image", "default");
        userMap.put("thumb_image", "default");

        mDatabase.setValue(userMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        new AlertDialog.Builder(RegisterActivity.this)
                                .setTitle("Sign Up")
                                .setMessage("Account created")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    setResult(Activity.RESULT_OK);
                                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                }).show();
                    } else {
                        new AlertDialog.Builder(RegisterActivity.this)
                                .setTitle("Sign Up")
                                .setMessage(task.getException().getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
        );
    }
}
