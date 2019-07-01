package com.yt.chat.Controller;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.yt.chat.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersViewHolder extends RecyclerView.ViewHolder {
    private View mView;

    public UsersViewHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setDisplayName(String name) {
        TextView userName_tv = mView.findViewById(R.id.user_single_name);
        userName_tv.setText(name);
    }

    public void setUserStatus(String status) {
        TextView status_tv = mView.findViewById(R.id.user_single_status);
        status_tv.setText(status);
    }

    public void setUserImage(String image) {
        CircleImageView image_ci = mView.findViewById(R.id.user_single_image);
        Picasso.get().load(image).placeholder(R.drawable.avatar).into(image_ci);
    }

    public View getView() {
        return mView;
    }
}
