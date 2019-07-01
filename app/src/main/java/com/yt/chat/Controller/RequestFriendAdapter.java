package com.yt.chat.Controller;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.yt.chat.Model.RequestFriend;
import com.yt.chat.Activity.ProfileActivity;
import com.yt.chat.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFriendAdapter extends RecyclerView.Adapter<RequestFriendAdapter.RequestFriendViewHolder> {
    private List<RequestFriend> mFriendList;
    private Activity activity;

    public RequestFriendAdapter(List<RequestFriend> mFriendList, Activity activity) {
        this.mFriendList = mFriendList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RequestFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.request_single_layout, parent, false);

        return new RequestFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestFriendViewHolder holder, int position) {
        RequestFriend friend = mFriendList.get(position);
        holder.bind(friend);
        holder.mView.setOnClickListener(v -> {
            Intent intent = new Intent(activity.getApplicationContext(), ProfileActivity.class);
            intent.putExtra("user_id", friend.getUserId());
            activity.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return mFriendList.size();
    }

    public class RequestFriendViewHolder extends RecyclerView.ViewHolder {
        private TextView mName;
        private CircleImageView mImage;
        private View mView;

        public RequestFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mName = mView.findViewById(R.id.request_single_name);
            mImage = mView.findViewById(R.id.request_single_image);
        }

        public void bind(RequestFriend friend) {
            mName.setText(friend.getName());
            Picasso.get().load(friend.getImage()).placeholder(R.drawable.avatar).into(mImage);
        }

        public View getView() {
            return mView;
        }
    }
}
