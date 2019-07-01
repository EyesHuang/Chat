package com.yt.chat.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yt.chat.Controller.SectionsPagerAdapter;
import com.yt.chat.R;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private Toolbar mToolbar;
    private ViewPager mViewPager;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    private static int request_code, result_code;
    private static Intent crop_image_data;

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.ref_users))
                    .child(mAuth.getCurrentUser().getUid());
        }

        // Toolbar setup
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chat");

        // tabs setup
        mViewPager = findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFragment(new FriendsFragment(), getString(R.string.fragment_page_one));
        mSectionsPagerAdapter.addFragment(new ChatsFragment(), getString(R.string.fragment_page_two));
        mSectionsPagerAdapter.addFragment(new SettingsFragment(), getString(R.string.fragment_page_three));
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.getTabAt(0).setIcon(R.drawable.tab_selector_one);
        mTabLayout.getTabAt(1).setIcon(R.drawable.tab_selector_two);
        mTabLayout.getTabAt(2).setIcon(R.drawable.tab_selector_three);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Test", "onStart: " + TAG);
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendToStart();
        } else {
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("name")) {
                        mUserDatabase.child("online").setValue("true");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
            request_code = 0;
            result_code = RESULT_CANCELED;
            crop_image_data = null;
        }
    }

    private void sendToStart() {
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.main_logout_btn:
                mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
                FirebaseAuth.getInstance().signOut();
                sendToStart();
                break;
            case R.id.main_request_btn:
                Intent requestIntent = new Intent(MainActivity.this, RequestActivity.class);
                startActivity(requestIntent);
                break;
            case R.id.main_all_btn:
                Intent allIntent = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(allIntent);
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            request_code = requestCode;
            crop_image_data = data;

            if (resultCode == RESULT_OK) {
                result_code = resultCode;
            }
        }
    }

    public static int getRequestCode() {
        return request_code;
    }

    public static int getResultCode() {
        return result_code;
    }

    public static Intent getCropImageData() {
        return crop_image_data;
    }

    public static void setRequestCode(int request_code) {
        MainActivity.request_code = request_code;
    }

    public static void setResultCode(int result_code) {
        MainActivity.result_code = result_code;
    }

    public static void setCropImageData(Intent crop_image_data) {
        MainActivity.crop_image_data = crop_image_data;
    }
}


