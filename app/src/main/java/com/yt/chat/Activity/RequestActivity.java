package com.yt.chat.Activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yt.chat.Controller.RequestFriendAdapter;
import com.yt.chat.Model.RequestFriend;
import com.yt.chat.R;

import java.util.ArrayList;
import java.util.List;

public class RequestActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private String mCurrentUserId;

    private Toolbar mToolbar;
    private RecyclerView mRequestRecycler, mReceivedRecycler;
    private List<RequestFriend> requestFriends = new ArrayList<>();
    private List<RequestFriend> receivedFriends = new ArrayList<>();
    private RequestFriendAdapter requestAdapter, receivedAdapter;

    private final String TAG = RequestActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        // Toolbar setup
        mToolbar = findViewById(R.id.request_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friend Request");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRequestRecycler = findViewById(R.id.request_recycler_requested);
        mReceivedRecycler = findViewById(R.id.request_recycler_received);

        requestAdapter = new RequestFriendAdapter(requestFriends, RequestActivity.this);
        receivedAdapter = new RequestFriendAdapter(receivedFriends, RequestActivity.this);

        mRequestRecycler.setHasFixedSize(true);
        mRequestRecycler.setAdapter(requestAdapter);
        mRequestRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRequestRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mReceivedRecycler.setHasFixedSize(true);
        mReceivedRecycler.setAdapter(receivedAdapter);
        mReceivedRecycler.setLayoutManager(new LinearLayoutManager(this));
        mReceivedRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mCurrentUserId = mAuth.getCurrentUser().getUid();
            mRootRef = FirebaseDatabase.getInstance().getReference();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        requestFriends.clear();
        receivedFriends.clear();
        receivedAdapter.notifyDataSetChanged();
        requestAdapter.notifyDataSetChanged();

        friendRequestDataListening();
    }

    private void friendRequestDataListening() {
        mRootRef.child(getString(R.string.ref_friend_req)).child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String type = ds.child(getString(R.string.request_type)).getValue().toString();
                    String user_id = ds.getKey();

                    if (type.equals(getString(R.string.received))) {

                        mRootRef.child(getString(R.string.ref_users)).child(user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String name = dataSnapshot.child("name").getValue().toString();
                                String image = dataSnapshot.child("thumb_image").getValue().toString();
                                Log.d(TAG, "Received onDataChange: name=" + name + ", image=" + image);
                                receivedFriends.add(new RequestFriend(name, image, user_id));
                                receivedAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    } else if (type.equals(getString(R.string.sent))) {

                        mRootRef.child(getString(R.string.ref_users)).child(user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String name = dataSnapshot.child("name").getValue().toString();
                                String image = dataSnapshot.child("thumb_image").getValue().toString();
                                Log.d(TAG, "Request onDataChange: name=" + name + ", image=" + image);
                                requestFriends.add(new RequestFriend(name, image, user_id));
                                requestAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        receivedAdapter.notifyDataSetChanged();
        requestAdapter.notifyDataSetChanged();
    }
}
