package com.yt.chat.Controller;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yt.chat.Activity.ProfileActivity;
import com.yt.chat.Model.Users;
import com.yt.chat.R;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersViewHolder> {
    private List<Users> usersList;
    private Activity activity;

    public UsersAdapter(List<Users> usersList, Activity activity) {
        this.usersList = usersList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity.getApplicationContext())
                .inflate(R.layout.users_single_layout, parent, false);

        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        Users user = usersList.get(position);

        holder.setDisplayName(user.getName());
        holder.setUserImage(user.getImage());
        holder.setUserStatus(user.getStatus());

        holder.getView().setOnClickListener(v -> {
            Intent profileIntent = new Intent(activity, ProfileActivity.class);
            profileIntent.putExtra("user_id", user.getUser_id());
            activity.startActivity(profileIntent);
        });

    }

    @Override
    public int getItemCount() {
        Log.d("UsersActivity", "getItemCount: " + usersList.size());
        return usersList.size();
    }
}
