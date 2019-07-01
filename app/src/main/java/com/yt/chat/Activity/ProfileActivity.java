package com.yt.chat.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yt.chat.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private String mCurrentUserId;

    private CircleImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn, mDeclineBtn;

    private String mCurrent_state;
    private String currentName, userName;
    private String currentStatus, userStatus;
    private int friendCount = 0;
    private final String TAG = ProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set Component
        mProfileImage = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.profile_displayName);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriendsCount = findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = findViewById(R.id.profile_decline_btn);

        String user_id = getIntent().getStringExtra("user_id");

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mCurrentUserId = mAuth.getCurrentUser().getUid();
            mRootRef = FirebaseDatabase.getInstance().getReference();
            mFriendReqDatabase = mRootRef.child(getString(R.string.ref_friend_req));
            mFriendDatabase = mRootRef.child(getString(R.string.ref_friends));
            mUsersDatabase = mRootRef.child(getString(R.string.ref_users)).child(user_id);
            mUsersDatabase.keepSynced(true);

            mCurrent_state = getString(R.string.not_friends);
            countTotalFriends(user_id);
            userDataListening(user_id);

            // Set Button OnClickListener
            sendReqBtnOnClickListener(user_id);
            declineBtnOnClickListener(user_id);
        }

    }

    private void declineBtnOnClickListener(String user_id) {
        mDeclineBtn.setOnClickListener(v -> {
            if (mCurrent_state.equals(getString(R.string.req_received))) {
                Map declineMap = new HashMap();
                declineMap.put(getString(R.string.ref_friend_req) + "/" + mCurrentUserId + "/" + user_id, null);
                declineMap.put(getString(R.string.ref_friend_req) + "/" + user_id + "/" + mCurrentUserId, null);

                mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Toast.makeText(ProfileActivity.this, databaseError.getMessage(),
                                    Toast.LENGTH_LONG);
                        } else {
                            mCurrent_state = getString(R.string.not_friends);
                            mProfileSendReqBtn.setText(getString(R.string.send_friend_request));

                            mDeclineBtn.setVisibility(View.INVISIBLE);
                            mDeclineBtn.setEnabled(false);
                        }

                        mProfileSendReqBtn.setEnabled(true);
                    }
                });


            }

        });
    }

    private void sendReqBtnOnClickListener(String user_id) {
        mProfileSendReqBtn.setOnClickListener(v -> {

            mProfileSendReqBtn.setEnabled(false);

            /******************State: Not Friends******************/
            reqBtnStatusNotFriend(user_id);

            //******************State: Cancel Request******************/
            reqBtnStatusReqSent(user_id);

            //******************State: Request Received******************/
            reqBtnStatusReqReceived(user_id);

            //******************State: Un-Friends******************/
            reqBtnStatusFriends(user_id);


        });
    }

    private void reqBtnStatusFriends(String user_id) {
        if (mCurrent_state.equals(getString(R.string.friends))) {
            Map unfriendMap = new HashMap();
            unfriendMap.put(getString(R.string.ref_friends) + "/" + mCurrentUserId + "/" + user_id, null);
            unfriendMap.put(getString(R.string.ref_friends) + "/" + user_id + "/" + mCurrentUserId, null);

            mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Toast.makeText(ProfileActivity.this, databaseError.getMessage(),
                                Toast.LENGTH_LONG);
                    } else {
                        mCurrent_state = getString(R.string.not_friends);
                        mProfileSendReqBtn.setText(getString(R.string.send_friend_request));

                        mDeclineBtn.setVisibility(View.INVISIBLE);
                        mDeclineBtn.setEnabled(false);
                    }

                    mProfileSendReqBtn.setEnabled(true);
                }
            });


        }
    }

    private void reqBtnStatusReqReceived(String user_id) {
        if (mCurrent_state.equals(getString(R.string.req_received))) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String currentDate = sdf.format(new Date());

            mRootRef.child(getString(R.string.ref_users)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.getKey().equals(mCurrentUserId)) {
                            currentName = ds.child("name").getValue().toString();
                            currentStatus = ds.child("status").getValue().toString();
                        } else if (ds.getKey().equals(user_id)) {
                            userName = ds.child("name").getValue().toString();
                            userStatus = ds.child("status").getValue().toString();
                        }

                    }

                    Map friendsMap = new HashMap();
                    friendsMap.put(getString(R.string.ref_friends) + "/" + mCurrentUserId + "/" + user_id + "/date",
                            currentDate);
                    friendsMap.put(getString(R.string.ref_friends) + "/" + user_id + "/" + mCurrentUserId + "/date",
                            currentDate);

                    friendsMap.put(getString(R.string.ref_friends) + "/" + mCurrentUserId + "/" + user_id + "/name",
                            userName);
                    friendsMap.put(getString(R.string.ref_friends) + "/" + user_id + "/" + mCurrentUserId + "/name",
                            currentName);

                    friendsMap.put(getString(R.string.ref_friends) + "/" + mCurrentUserId + "/" + user_id + "/status",
                            userStatus);
                    friendsMap.put(getString(R.string.ref_friends) + "/" + user_id + "/" + mCurrentUserId + "/status",
                            currentStatus);

                    friendsMap.put(getString(R.string.ref_friend_req) + "/" + mCurrentUserId + "/" + user_id,
                            null);
                    friendsMap.put(getString(R.string.ref_friend_req) + "/" + user_id + "/" + mCurrentUserId,
                            null);


                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(ProfileActivity.this, databaseError.getMessage(),
                                        Toast.LENGTH_LONG);
                            } else {
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = getString(R.string.friends);
                                mProfileSendReqBtn.setText(getString(R.string.un_friend));
                                mProfileSendReqBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                                        R.drawable.button_unfriend, null));

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
    }

    private void reqBtnStatusReqSent(String user_id) {
        if (mCurrent_state.equals(getString(R.string.req_sent))) {
            mFriendReqDatabase.child(mCurrentUserId).child(user_id).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrentUserId)
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = getString(R.string.not_friends);
                                    mProfileSendReqBtn.setText(getString(R.string.send_friend_request));

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                }
                            });

                        }
                    });
        }
    }

    private void reqBtnStatusNotFriend(String user_id) {
        if (mCurrent_state.equals(getString(R.string.not_friends))) {
            DatabaseReference newNotificationRef = mRootRef.child(getString(R.string.ref_notifications))
                    .child(user_id).push();
            String newNotificationId = newNotificationRef.getKey();

            HashMap<String, String> notificationData = new HashMap<>();
            notificationData.put("from", mCurrentUserId);
            notificationData.put("type", "request");

            Map requestMap = new HashMap();
            requestMap.put(getString(R.string.ref_friend_req) + "/" + mCurrentUserId
                            + "/" + user_id + "/" + getString(R.string.request_type),
                    getString(R.string.sent));

            requestMap.put(getString(R.string.ref_friend_req) + "/" + user_id + "/" + mCurrentUserId
                            + "/" + getString(R.string.request_type),
                    getString(R.string.received));

            requestMap.put(getString(R.string.ref_notifications) + "/" + user_id + "/" + newNotificationId,
                    notificationData);

            mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if (databaseError != null) {
                        Toast.makeText(ProfileActivity.this, databaseError.getMessage(),
                                Toast.LENGTH_LONG);
                    } else {
                        mProfileSendReqBtn.setEnabled(true);
                        mCurrent_state = getString(R.string.req_sent);
                        mProfileSendReqBtn.setText(getString(R.string.cancel_friend_request));

                        mDeclineBtn.setVisibility(View.INVISIBLE);
                        mDeclineBtn.setEnabled(false);

                        Toast.makeText(ProfileActivity.this,
                                "Sent Friend Request Successfully", Toast.LENGTH_LONG).show();
                    }

                }
            });


        }
    }

    private void userDataListening(String user_id) {
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                if (user_id.equals(mCurrentUserId)) {
                    mProfileSendReqBtn.setVisibility(View.INVISIBLE);
                    mProfileSendReqBtn.setEnabled(false);
                }

                mProfileName.setText(userName);
                mProfileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.avatar).into(mProfileImage);

                /*****************Friends List / Request Feature*****************/
                checkFriendRequest(user_id);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkFriendRequest(String user_id) {
        mFriendReqDatabase.child(mCurrentUserId)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child(getString(R.string.request_type)).getValue().toString();

                            if (req_type.equals(getString(R.string.received))) {
                                mCurrent_state = getString(R.string.req_received);
                                mProfileSendReqBtn.setText(getString(R.string.accept_friend_request));

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if (req_type.equals(req_type.equals(getString(R.string.sent)))) {
                                mCurrent_state = getString(R.string.req_sent);
                                mProfileSendReqBtn.setText(getString(R.string.cancel_friend_request));

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }

                        } else {
                            checkWhetherAreFriends(user_id);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void checkWhetherAreFriends(String user_id) {
        mFriendDatabase.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user_id)) {
                    mCurrent_state = getString(R.string.friends);
                    mProfileSendReqBtn.setText(getString(R.string.un_friend));
                    mProfileSendReqBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                            R.drawable.button_unfriend, null));
                    Log.d(TAG, "We are friends");

                    mDeclineBtn.setVisibility(View.INVISIBLE);
                    mDeclineBtn.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void countTotalFriends(String user_id) {
        mFriendDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    friendCount++;
                }

                mProfileFriendsCount.setText("Total friends: " + friendCount);
                friendCount = 0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
