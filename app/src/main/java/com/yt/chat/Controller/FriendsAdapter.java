package com.yt.chat.Controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yt.chat.Activity.ChatActivity;
import com.yt.chat.Activity.ProfileActivity;
import com.yt.chat.Model.Friends;
import com.yt.chat.Model.MessageLinkedHashMap;
import com.yt.chat.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef, mFriendsDatabase, mUserDatabase;
    private String mCurrent_user_id;
    private MessageLinkedHashMap friendsMap;
    private Context context;

    public FriendsAdapter(MessageLinkedHashMap friendsMap, Context context) {
        this.friendsMap = friendsMap;
        this.context = context;

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mFriendsDatabase = mRootRef.child("Friends");
        mUserDatabase = mRootRef.child("Users");
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_single_layout, parent, false);

        return new FriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        Friends friend = (Friends) friendsMap.getValue(position);
        String user_id = friend.getUserId();

        mUserDatabase.child(user_id).orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();
                Log.d("FriendsFragment", "name=" + name + ", status=" + status);

                holder.setName(name);
                holder.setStatus(status);
                holder.setUserImage(image);

                if (dataSnapshot.hasChild("online")) {
                    String userOnline = dataSnapshot.child("online").getValue().toString();
                    holder.setUserOnline(userOnline);
                }

                holder.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence[] options = new CharSequence[]{"Open Profile", "Send Message"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Select Option")
                                .setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                Intent profileIntent = new Intent(context, ProfileActivity.class);
                                                profileIntent.putExtra("user_id", user_id);
                                                context.startActivity(profileIntent);
                                                break;
                                            case 1:
                                                Intent chatIntent = new Intent(context, ChatActivity.class);
                                                chatIntent.putExtra("user_id", user_id);
                                                chatIntent.putExtra("user_name", name);
                                                context.startActivity(chatIntent);
                                                break;
                                        }
                                    }
                                })
                                .show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return friendsMap.size();
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder {
        private View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.friend_single_name);
            userNameView.setText(name);
        }

        public void setStatus(String status) {
            TextView userStatusView = mView.findViewById(R.id.friend_single_status);
            userStatusView.setText(status);
        }

        public void setUserImage(String image) {
            CircleImageView image_ci = mView.findViewById(R.id.friend_single_image);
            Picasso.get().load(image).placeholder(R.drawable.avatar).into(image_ci);
        }

        public void setUserOnline(String online_status) {
            ImageView userOnlineView = mView.findViewById(R.id.friend_single_online_icon);

            if (online_status.equals("true")) {
                userOnlineView.setImageResource(R.drawable.online);
            } else {
                userOnlineView.setImageResource(R.drawable.offline);
            }

        }

        public View getView() {
            return mView;
        }


    }
}
