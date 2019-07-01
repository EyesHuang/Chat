package com.yt.chat.Controller;

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
import com.yt.chat.Model.GetTimeAgo;
import com.yt.chat.Model.MessageLinkedHashMap;
import com.yt.chat.Model.Messages;
import com.yt.chat.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter {
    private MessageLinkedHashMap mMessageMap;
    private DatabaseReference mUsersDatabase;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_TIME = 3;

    public MessageAdapter(MessageLinkedHashMap mMessageMap) {
        this.mMessageMap = mMessageMap;
    }

    // Inflates the appropriate layout according to the ViewType.
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        } else if (viewType == VIEW_TYPE_TIME) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.time_label, parent, false);
            return new TimeLabelHolder(view);
        }

        return null;
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        super.getItemViewType(position);
        Object object = mMessageMap.getEntry(position).getValue();

        if (object.getClass().equals(Messages.class)) {
            String from_user = ((Messages)object).getFrom();
            String current_user;
            FirebaseAuth mAuth = FirebaseAuth.getInstance();

            if (mAuth.getCurrentUser() != null) {
                current_user = mAuth.getCurrentUser().getUid();
                if (current_user.equals(from_user)) {
                    return VIEW_TYPE_MESSAGE_SENT;
                } else {
                    return VIEW_TYPE_MESSAGE_RECEIVED;
                }

            } else {
                return 0;
            }
        } else if (object.getClass().equals(String.class)) {
            return VIEW_TYPE_TIME;
        } else {
            return 0;
        }

    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Object object = mMessageMap.getEntry(position).getValue();
        if (object.getClass().equals(Messages.class)) {
            Messages message = (Messages) object;
            String from_user = message.getFrom();

            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MESSAGE_SENT:
                    ((SentMessageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    ((ReceivedMessageHolder) holder).bind(message);
                    mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
                    mUsersDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String image = dataSnapshot.child("thumb_image").getValue().toString();
                            Picasso.get().load(image)
                                    .placeholder(R.drawable.avatar).into(((ReceivedMessageHolder) holder).profileImage);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            }

        } else if (object.getClass().equals(String.class)) {
            ((TimeLabelHolder)holder).bind((String) object);
        }

    }

    @Override
    public int getItemCount() {
        return mMessageMap.size();
    }


    public class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageText, timeText;
        private CircleImageView profileImage;

        public ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.received_text_message_body);
            timeText = itemView.findViewById(R.id.received_text_message_time);
            profileImage = itemView.findViewById(R.id.received_image_message_profile);
        }

        public void bind(Messages message) {
            messageText.setText(message.getMessage());
            timeText.setText(GetTimeAgo.getMessageTime(message.getTime()));
        }

        public ImageView getProfileImage() {
            return profileImage;
        }
    }

    public class SentMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageText, timeText;

        public SentMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.sent_text_message_body);
            timeText = itemView.findViewById(R.id.sent_text_message_time);
        }

        public void bind(Messages message) {
            messageText.setText(message.getMessage());
            timeText.setText(GetTimeAgo.getMessageTime(message.getTime()));
        }
    }

    public class TimeLabelHolder extends RecyclerView.ViewHolder {
        private TextView timeText;

        public TimeLabelHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.time_label_time);
        }

        public void bind(String time) {
            timeText.setText(time);
        }
    }
}
