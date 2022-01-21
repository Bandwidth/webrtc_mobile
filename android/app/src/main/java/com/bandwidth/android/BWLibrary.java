package com.bandwidth.android;

// A place to have common functions needed by multiple activities

import android.content.Context;
import android.os.Bundle;

import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.AmplifyConfiguration;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.messaging.FirebaseMessaging;

public class BWLibrary {

    public static int getNotificationId(Bundle extras) {
        int notificationId = extras.getInt("notificationId");
        System.out.println("DismissActivity: notificationId=" + notificationId);
        return notificationId;
    }

    public static String getFirebaseDeviceToken() {
        String deviceToken = null;
        try {
            Task tokenTask = FirebaseMessaging.getInstance().getToken();
            Tasks.await(tokenTask);
            deviceToken = (String)tokenTask.getResult();
            System.out.println("getFirebaseDeviceToken = " + deviceToken);
        } catch(Exception e) {
            System.out.println("Error getting firebaseDeviceToken: " + e.getMessage());
        } finally {
            return deviceToken;
        }
    }

    public static void configureAmplify(Context context) {
            // Initialize Amplify plugins
            try {
                Amplify.addPlugin(new AWSCognitoAuthPlugin());
//                Amplify.addPlugin(new AWSApiPlugin());
                Amplify.addPlugin(new AWSDataStorePlugin());

                Amplify.configure(context);
            } catch (Exception e) {
                // ignore exception if amplify already configured
                System.out.println("EXCEPTION:" + e.getMessage());
            }
    }
}
