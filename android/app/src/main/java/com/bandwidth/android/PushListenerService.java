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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Date;
import java.util.Locale;

public class PushListenerService extends FirebaseMessagingService {
    public static final String TAG = PushListenerService.class.getSimpleName();

    public static final String CHANNEL_ID = "BWChannel";
    public static final String CHANNEL_NAME = "WebRTC Mobile Sample";

    public static final int ACCEPT_CALL = 101;
    public static final int DECLINE_CALL = 102;

    @Override
    public void onNewToken(String token) {
        // NOTE
        // This function is fired when a new firebase token is received for a device;
        // This is not utilized for the sample app. Anytime a device token changes, you should
        // prompt the user to enter their re
        super.onNewToken(token);

        Log.d(TAG, "NEW push notifications token: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message: " + remoteMessage.getData());

        String notificationTitle = remoteMessage.getData().get("pinpoint.notification.title");
        String notificationBody = remoteMessage.getData().get("pinpoint.notification.body");

        // TODO we can do better than parsing out the Bandwidth participantToken
        //      from the notification data. We can have the app point deepLink
        //      to the MainActivity with the participant token passed in as
        //      a query string parameter. See https://developer.android.com/guide/navigation/navigation-deep-link
        //      for details
        URL deepLink = null;
        try {
            deepLink = new URL(remoteMessage.getData().get("pinpoint.deeplink"));
        } catch (MalformedURLException e) {
            System.out.println(TAG + ": found malformed URL in notification " + e.getMessage());
        }

        String pToken = getBWPTokenFromNotification(deepLink);

        try {
            // Notification ids need to be unique; use timestamp for ids
            int notificationId = getNotificationId();

            // Create the notification channel
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            createNotificationChannel(manager, CHANNEL_ID, CHANNEL_NAME);

            // Create an intent that is supposed to be launched
            // when receiving a push notification
            // TODO full screen intents not working consistently with Android 10+
            Intent fullScreenIntent = new Intent(this, MainActivity.class);
            Intent dismissIntent = new Intent(this, DismissActivity.class);

            fullScreenIntent.putExtra("pToken", pToken);
            fullScreenIntent.putExtra("notificationId", notificationId);
            dismissIntent.putExtra("notificationId", notificationId);

            // Define separate intents for accept and decline intents
            // (there are buttons for these in the notification)
            PendingIntent acceptCallIntent = PendingIntent.getActivity(this, ACCEPT_CALL,
                    fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent declineCallIntent = PendingIntent.getActivity(this, DECLINE_CALL,
                    dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Build the notification itself, setting priority, icon, title, text, etc
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_launcher_background) // TODO change icon
                            // Notification Title
                            .setContentTitle(notificationTitle)
                            // Notification Text
                            .setContentText(notificationBody)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_CALL)
                            .setAutoCancel(true)
                            .addAction(R.drawable.common_full_open_on_phone, "Accept", acceptCallIntent)
                            .addAction(R.drawable.common_full_open_on_phone, "Decline", declineCallIntent)

                            // Use a full-screen intent only for the highest-priority alerts where you
                            // have an associated activity that you would like to launch after the user
                            // interacts with the notification. Also, if your app targets Android 10
                            // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
                            // order for the platform to invoke this notification.
                            .setFullScreenIntent(acceptCallIntent, true);


            // Actually send the notification
            manager.notify( notificationId, notificationBuilder.build());

        } catch (Exception e) {
            // TODO error handling
            e.printStackTrace();
            System.out.println("EXCEPTION HERE: " + e.getMessage());
        }
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

//        bwChannel.enableVibration(true);
//        bwChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

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

    private String getBWPTokenFromNotification(URL deepLink) {
        String qs = null;
        String pToken = null;
        try {
            qs = deepLink.getQuery();
            System.out.println(TAG + ": QUERY : " + qs);

            for(String param : qs.split("&")) {
                String name = param.split("=")[0];
                String value = param.split("=")[1];

                if(name.equals("tok")) {
                    pToken = value;
                    break;
                }
            }
        } catch (NullPointerException e) {
            // TODO error handling
            System.out.println(TAG + ": no deeplink found in the notification");
        } finally {
            return pToken;
        }
    }
}