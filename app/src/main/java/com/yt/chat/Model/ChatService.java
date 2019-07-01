package com.yt.chat.Model;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.yt.chat.Activity.ProfileActivity;
import com.yt.chat.R;

public class ChatService extends FirebaseMessagingService {

    private final String TAG = ChatService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "onNewToken: Refreshed token. " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Context context = getApplicationContext();
        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String from_user_id = remoteMessage.getData().get("from_user_id");
        Log.d(TAG, "onMessageReceived: user_id:" + from_user_id);
        Log.d(TAG, "onMessageReceived: click_action" + click_action);


        Intent intent = new Intent(this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("user_id", from_user_id);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "onMessageReceived: Your SDK version > O");
            CharSequence name = "This is Title";
            String description = "This is Content";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("NEW_SDK", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            // Construct Notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "NEW_SDK")
                    .setSmallIcon(R.drawable.chat_icon)
                    .setContentTitle(notification_title)
                    .setContentText(notification_message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            // Launch Notification
            int mNotificationId = (int) System.currentTimeMillis();
            notificationManager.notify(mNotificationId, builder.build());
            return;

        } else {
            // Construct Notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(notification_title)
                    .setContentText(notification_message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            // Launch Notification
            int mNotificationId = (int) System.currentTimeMillis();
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.notify(mNotificationId, builder.build());
        }

        Log.d(TAG, "onMessageReceived: title:" + remoteMessage.getNotification().getTitle());
        Log.d(TAG, "onMessageReceived: body:" + remoteMessage.getNotification().getBody());

        FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance();

        instanceId.getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {

                if (!task.isSuccessful()) {
                    Log.d(TAG, "getInstanceId failed. " + task.getException());
                    return;
                }

                String token = task.getResult().getToken();
                String id = instanceId.getId();
                Log.d(TAG, "Current Token: " + token);
                Log.d(TAG, "ID: " + id);

            }
        });
    }


}
