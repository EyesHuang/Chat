package com.yt.chat.Activity;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.yt.chat.Controller.BaseFragment;
import com.yt.chat.Controller.ProgressDialogUtil;
import com.yt.chat.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends BaseFragment {
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageRef;
    private String current_uid;

    private View mMainView;
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private ImageButton mBtnImage;

    private static final String TAG = SettingsFragment.class.getSimpleName();
    public final static String EXTRA_STATUS = TAG + "_status";
    public final static String EXTRA_NAME = TAG + "_name";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_settings, container, false);

        // Components Initialize
        mDisplayImage = mMainView.findViewById(R.id.settings_image);
        mName = mMainView.findViewById(R.id.settings_name);
        mStatus = mMainView.findViewById(R.id.settings_status);
        mBtnImage = mMainView.findViewById(R.id.settings_image_btn);

        // Draw TextView bottom outline
        mName.setBackgroundResource(R.drawable.outline_background);
        mStatus.setBackgroundResource(R.drawable.outline_background);

        // Set TextView onClick
        mName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name_value = mName.getText().toString();
                Intent settingsIntent = new Intent(getContext(), NameActivity.class);
                settingsIntent.putExtra(EXTRA_NAME, name_value);
                startActivity(settingsIntent);
            }
        });

        mStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = mStatus.getText().toString();
                Intent settingsIntent = new Intent(getContext(), StatusActivity.class);
                settingsIntent.putExtra(EXTRA_STATUS, status_value);
                startActivity(settingsIntent);
            }
        });

        // Set Firebase
        mAuth = FirebaseAuth.getInstance();
        current_uid = "";
        if (mAuth.getCurrentUser() != null) {
            mStorageRef = FirebaseStorage.getInstance().getReference();
            mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
            current_uid = mCurrentUser.getUid();
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.ref_users));
            mUsersDatabase.child(current_uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                    mName.setText(name);
                    mStatus.setText(status);

                    Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(thumb_image).placeholder(R.drawable.avatar)
                                    .into(mDisplayImage);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    ProgressDialogUtil.dismiss();
                }
            });
        }

        // Set Image button onClick
        mBtnImage.setOnClickListener(v -> {
            // start picker to get image for cropping and then use the image in cropping activity
            // show a menu to choose image resource
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(getActivity());

        });

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getResult();
    }

    private void getResult() {
        if (MainActivity.getRequestCode() == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(MainActivity.getCropImageData());

            if (MainActivity.getResultCode() == Activity.RESULT_OK && result != null) {
                Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());
                String current_user_id = mCurrentUser.getUid();
                Bitmap thumb_bitmap = null;

                try {
                    thumb_bitmap = new Compressor(getContext())
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    Log.d(TAG, "Picture has IO problems");
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] thumb_byte = baos.toByteArray();

                StorageReference thumb_pathStorageRef = mStorageRef.child("profile_images")
                        .child("thumb_" + current_user_id + random() + ".jpg");

                UploadTask uploadTask = thumb_pathStorageRef.putBytes(thumb_byte);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskUpload) {
                        if (taskUpload.isSuccessful()) {
                            ProgressDialogUtil.showProgressDialog(getContext());

                            thumb_pathStorageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        final Uri thumb_uri = task.getResult();
                                        Map update_hashMap = new HashMap<>();
                                        update_hashMap.put("thumb_image", thumb_uri.toString());

                                        mUsersDatabase.child(current_uid).updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                if (task.isSuccessful()) {
                                                    ProgressDialogUtil.dismiss();
                                                }
                                            }
                                        });

                                    }


                                }
                            });
                        }
                    }
                });

            } else if (MainActivity.getResultCode() == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, "Something wrong for Cropping Image", error);

            }

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.setRequestCode(0);
        MainActivity.setResultCode(0);
        MainActivity.setCropImageData(null);
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

}
