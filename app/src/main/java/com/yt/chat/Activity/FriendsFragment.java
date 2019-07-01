package com.yt.chat.Activity;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yt.chat.Controller.BaseFragment;
import com.yt.chat.Controller.FriendsAdapter;
import com.yt.chat.Model.Friends;
import com.yt.chat.Model.MessageLinkedHashMap;
import com.yt.chat.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends BaseFragment {
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef, mFriendsDatabase, mUsersDatabase;
    private String mCurrent_user_id;

    private View mMainView;
    private RecyclerView mFriendsList;
    private FriendsAdapter adapter;
    private MessageLinkedHashMap friendsMap = new MessageLinkedHashMap();

    private final String TAG = FriendsFragment.class.getSimpleName();

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = mMainView.findViewById(R.id.friends_list);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            mCurrent_user_id = mAuth.getCurrentUser().getUid();
            mRootRef = FirebaseDatabase.getInstance().getReference();
            mFriendsDatabase = mRootRef.child(getString(R.string.ref_friends)).child(mCurrent_user_id);
            mFriendsDatabase.keepSynced(true);

            mUsersDatabase = mRootRef.child(getString(R.string.ref_users));
            mUsersDatabase.keepSynced(true);

            adapter = new FriendsAdapter(friendsMap, getContext());
            mFriendsList.setHasFixedSize(true);
            mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
            mFriendsList.setAdapter(adapter);
        }

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        friendsMap.clear();
        adapter.notifyDataSetChanged();
        friendDatabaseListening();

        adapter.notifyDataSetChanged();

        mUsersDatabase.orderByChild("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void friendDatabaseListening() {
        mFriendsDatabase.orderByChild("name").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded: user_id=" + dataSnapshot.getKey());
                String user_id = dataSnapshot.getKey();
                String name = dataSnapshot.child("name").getValue().toString();
                friendsMap.put(dataSnapshot.getKey(), new Friends(user_id, name));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildChanged: user_id=" + dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved: user_id=" + dataSnapshot.getKey());
                friendsMap.remove(dataSnapshot.getKey());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildMoved: user_id=" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: ");
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.notifyDataSetChanged();
    }
}
