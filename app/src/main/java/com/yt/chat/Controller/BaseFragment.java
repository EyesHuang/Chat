package com.yt.chat.Controller;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {
    private Activity activity;
    public Context getContext() {
        if (activity == null) {
            return Chat.getInstance();
        }
        return activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }
}
