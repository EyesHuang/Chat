package com.yt.chat.Activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yt.chat.Controller.UsersAdapter;
import com.yt.chat.Model.Users;
import com.yt.chat.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsersActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private FirebaseUser mCurrentUser;

    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private UsersAdapter adapter;
    private List<Users> usersList = new ArrayList<>();

    private final static String TAG = UsersActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        // Set Toolbar
        mToolbar = findViewById(R.id.users_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            return;
        }
        mCurrentUser = mAuth.getCurrentUser();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.ref_users));

        // Set RecyclerView
        adapter = new UsersAdapter(usersList, UsersActivity.this);
        mUsersList = findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        mUsersList.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        usersList.clear();
        userDataListening();
    }

    private void userDataListening() {
        mUsersDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    String user_id = ds.getKey();
                    if (!user_id.equals(mCurrentUser.getUid())) {
                        String name = ds.child("name").getValue().toString();
                        String image = ds.child("thumb_image").getValue().toString();
                        String status = ds.child("status").getValue().toString();

                        usersList.add(new Users(name, image, status, user_id));
                        adapter.notifyDataSetChanged();
                    }

                }

                Collections.sort(usersList, Users::compareTo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
