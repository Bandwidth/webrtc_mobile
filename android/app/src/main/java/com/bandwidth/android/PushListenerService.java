package com.bandwidth.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Date;
import java.util.Locale;

public class PushListenerService extends FirebaseMessagingService {
    public static final String TAG = PushListenerService.class.getSimpleName();

    // Intent action used in local broadcast
    public static final String ACTION_PUSH_NOTIFICATION = "push-notification";
    // Intent keys
    public static final String INTENT_SNS_NOTIFICATION_FROM = "from";
    public static final String INTENT_SNS_NOTIFICATION_DATA = "data";

    public static final String CHANNEL_ID = "BWChannel";
    public static final String CHANNEL_NAME = "WebRTC Mobile Sample";

    public static final int ACCEPT_CALL = 101;
    public static final int DECLINE_CALL = 102;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Log.d(TAG, "Registering push notifications token: " + token);
        MainActivity.getPinpointManager(getApplicationContext()).getNotificationClient().registerDeviceToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String notificationTitle = remoteMessage.getData().get("pinpoint.notification.title");
        String notificationBody = remoteMessage.getData().get("pinpoint.notification.body");
        Log.d(TAG, "Message: " + remoteMessage.getData());

        try {


            int notificationId = getNotificationId();
            Intent fullScreenIntent = new Intent(this, ListUsersActivity.class);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, ACCEPT_CALL,
                    fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent acceptCallIntent = PendingIntent.getActivity(this, ACCEPT_CALL,
                    fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent declineCallIntent = PendingIntent.getActivity(this, DECLINE_CALL,
                    fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT); // TODO

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            createNotificationChannel(manager, CHANNEL_ID, CHANNEL_NAME);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher_background) // TODO change icon
                            // Notification Title
                            .setContentTitle(notificationTitle)
                            // Notification Text
                            .setContentText(notificationBody)
                            // Disable ability to swipe off the notification
                            .setOngoing(true)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_CALL)

                            // Use a full-screen intent only for the highest-priority alerts where you
                            // have an associated activity that you would like to launch after the user
                            // interacts with the notification. Also, if your app targets Android 10
                            // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
                            // order for the platform to invoke this notification.
                            .setFullScreenIntent(fullScreenPendingIntent, true);


            notificationBuilder.addAction(
                    NotificationCompat.Action.Builder(
                            IconCompat.createWithResource(
                                    getApplicationContext(), R.drawable.icon_accept_call // TODO
                            ),
                            getString(R.string.accept_call), // TODO
                            acceptCallIntent
                    )
            )
            manager.notify( notificationId, notificationBuilder.build());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION HERE: " + e.getMessage());
        }
    }

    private void broadcast(final String from, final HashMap<String, String> dataMap) {
        Intent intent = new Intent(ACTION_PUSH_NOTIFICATION);
        intent.putExtra(INTENT_SNS_NOTIFICATION_FROM, from);
        intent.putExtra(INTENT_SNS_NOTIFICATION_DATA, dataMap);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void createNotificationChannel(NotificationManager manager, String channelId, String channelName) {

        // The user-visible description of the channel.
//        String description = getString(R.string.channel_description);

        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel bwChannel = new NotificationChannel(channelId, channelName,importance);

        // Configure the notification channel.
        bwChannel.setDescription("Incoming call from BW WebRTC Mobile Sample App");

        bwChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        bwChannel.setLightColor(Color.RED);

        bwChannel.enableVibration(true);
        bwChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        manager.createNotificationChannel(bwChannel);
    }

    /**
     * Helper method to extract push message from bundle.
     *
     * @param data bundle
     * @return message string from push notification
     */
    public static String getMessage(Bundle data) {
        return ((HashMap) data.get("data")).toString();
    }

    // Notification ids have to be unique; use a timestamp for id to guarantee this
    public int getNotificationId(){
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        return id;
    }
}