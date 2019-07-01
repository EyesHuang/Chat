package com.yt.chat.Activity;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yt.chat.Controller.BaseFragment;
import com.yt.chat.Model.Conversation;
import com.yt.chat.Model.GetTimeAgo;
import com.yt.chat.R;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends BaseFragment {
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef, mConvDatabase, mMessageDatabase, mUsersDatabase;
    private String mCurrent_user_id;

    private View mMainView;
    private RecyclerView mConvList;
    private FirebaseRecyclerAdapter<Conversation, ConvViewHolder> adapter;
    private final String TAG = ChatsFragment.class.getSimpleName();

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mConvList = mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            mCurrent_user_id = mAuth.getCurrentUser().getUid();
            mRootRef = FirebaseDatabase.getInstance().getReference();
            mConvDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.ref_chat))
                    .child(mCurrent_user_id);
            mConvDatabase.keepSynced(true);

            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.ref_users));
            mUsersDatabase.keepSynced(true);

            mMessageDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.ref_messages))
                    .child(mCurrent_user_id);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);


        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query conversationQuery = mConvDatabase.orderByChild("message_time");
        setFirebaseRecyclerAdapter(conversationQuery);
        mConvList.setAdapter(adapter);
        adapter.startListening();

    }

    private void setFirebaseRecyclerAdapter(Query conversationQuery) {
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Conversation>()
                .setQuery(conversationQuery, new SnapshotParser<Conversation>() {
                    @NonNull
                    @Override
                    public Conversation parseSnapshot(@NonNull DataSnapshot snapshot) {
                        Boolean seen = Boolean.parseBoolean(snapshot.child("seen").getValue().toString());
                        long timestamp = Long.parseLong(snapshot.child("timestamp").getValue().toString());
                        long messageTime;

                        // 1st conversation, and we haven't sent messages
                        if (snapshot.child("message_time").getValue() == null) {
                            messageTime = timestamp;

                            String user_id = snapshot.getKey();
                            Map timeMap = new HashMap();
                            timeMap.put("Chat/" + mCurrent_user_id + "/" + user_id + "/message_time", messageTime);
                            timeMap.put("Chat/" + user_id + "/" + mCurrent_user_id + "/message_time", messageTime);

                            timeMap.put("Chat/" + user_id + "/" + mCurrent_user_id + "/seen", "true");
                            timeMap.put("Chat/" + user_id + "/" + mCurrent_user_id + "/timestamp", timestamp);

                            mRootRef.updateChildren(timeMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    Log.d(TAG, "onComplete: Update");
                                }
                            });

                        } else {
                            messageTime = Long.parseLong(snapshot.child("message_time").getValue().toString());
                        }

                        return new Conversation(seen, timestamp, messageTime);
                    }
                })
                .build();

        adapter = new FirebaseRecyclerAdapter<Conversation, ConvViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ConvViewHolder convViewHolder, int position, @NonNull Conversation conv) {
                final String list_user_id = getRef(position).getKey();
                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        String data = dataSnapshot.child("message").getValue().toString();
                        String type = dataSnapshot.child("type").getValue().toString();
                        convViewHolder.setMessage(data, type, conv.getSeen());
                        convViewHolder.setMessageTime(conv.getMessageTime());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                ChatsFragment.this.bindViewHolderFromUserDatabase(convViewHolder, list_user_id);


            }

            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_single_layout, parent, false);

                return new ConvViewHolder(view);
            }
        };
    }

    private void bindViewHolderFromUserDatabase(@NonNull ConvViewHolder convViewHolder, String list_user_id) {
        mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String userName = dataSnapshot.child("name").getValue().toString();
                String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                convViewHolder.setName(userName);
                convViewHolder.setUserImage(userThumb);
                convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                        chatIntent.putExtra("user_id", list_user_id);
                        chatIntent.putExtra("user_name", userName);
                        startActivity(chatIntent);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private static class ConvViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private final static int MESSAGE_SHOW_LENGTH = 20;

        public ConvViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setMessage(String message, String type, boolean isSeen) {
            TextView userStatusView = mView.findViewById(R.id.chat_single_status);

            int length = message.length();
            if (length > MESSAGE_SHOW_LENGTH) {
                message = message.substring(0, MESSAGE_SHOW_LENGTH) + "...";
            }

            userStatusView.setText(message);

            if (!isSeen) {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

        }

        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.chat_single_name);
            userNameView.setText(name);
        }

        public void setUserImage(String thumb_image) {
            CircleImageView userImageView = mView.findViewById(R.id.chat_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.avatar).into(userImageView);
        }

        public void setMessageTime(long time) {
            TextView messageTimeView = mView.findViewById(R.id.chat_single_time);
            messageTimeView.setText(GetTimeAgo.getTimeAgo(time));
        }


    }

}
