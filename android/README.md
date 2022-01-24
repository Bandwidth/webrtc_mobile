# Bandwidth Android WebRTC Sample App

The Bandwidth Android SDK makes it quick and easy to build an excellent audio and video experience in your Android app. We provide tools to unlock the power of Bandwidth's audio and video networks.

## Installation

### Requirements

* Android
    
## Getting Started

```java
class MainActivity extends AppCompatActivity {
    private RTCBandwidth bandwidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Request camera permissions to allow capturing local video.

        // Setup any local and remote renderers.

        bandwidth = new RTCBandwidthClient(getApplicationContext(), EglBase.create().getEglBaseContext());

        // Called when a remote stream has become available.
        bandwidth.setOnStreamAvailableListener((streamId, mediaTypes, audioTracks, videoTracks, alias) -> {
            runOnUiThread(() -> {
                // Add remote renderers to available video tracks.
            });
        });

        // Called when a remote stream has become unavailable.
        bandwidth.setOnStreamUnavailableListener(streamId -> {
            runOnUiThread(() -> {
                // Clear remote renterers for the corresponding stream.
            });
        });

        // Connect to Bandwidth using the device token returned via Bandwidth's server-side WebRTC APIs.
        bandwidth.connect(deviceToken, () -> {
            // Once connected start publishing media.
            bandwidth.publish("android", (streamId, mediaTypes, audioSource, audioTrack, videoSource, videoTrack) -> {
                runOnUiThread(() -> {
                    // Start capturing local video using the video source.

                    // Add local renderer to the available video track.
                });
            });
        });
    }
}
```

### Flow of sample app

* LoginActivity \
    This is the default launch activity for the app. It allows you to register/sign in using your userId.
    We have used DynamoDB for data persistence and Cognito for authentication. On successful sign in, we
    get the Firebase device token for the device and if necessary, update the database tables
    (DeviceInfo and Person) with this information.

* ListUsersActivity \
    Once logged in, the user is forwarded to the list users activity, which shows all the registered users.
    Each user item is associated with a corresponding "deviceId" (this is primary key for the db table
    that stores the firebase device token).
    Touching a user initiates a BW webrtc call to them. It kicks off the MainActivity, passing in the
    callee's deviceId to it.

* MainActivity \
    This activity is invoked when a) you call someone and b) you are called.

    When you call someone
        This activity is passed the Callee's deviceId as a parameter. The caller's deviceId is stored on
        login. The activity then calls the backend api (see comments above getParticipantToken() in
        MainActivity for detailed explanation) which responds with a Bandwidth webrtc participant token
        for the caller. The participant token is then used to publish the caller's mediastream to the
        bandwidth webrtc session created by the backend.

    When you are called
        MainActivity is also called when a user responds to a push notification. To further elaborate, the
        backend which responds with a participant token to a caller's request to initiate a call, also fires
        a push notification via Amazon Pinpoint to the callee. The notification has the callee's bandwidth
        participant token in it. When the user accepts the call, it will fire off MainActivity and publish
        their mediastream, using the participant token from notification, to the webrtc session.

* DismissActivity \
    Used to dismiss the incoming webrtc call notification.

* PushListenerService \
    This service handles incoming Firebase push notifications. It basically builds the notification
    dialogue and then notifies the callee. Upon accepting the notification, it will fire off the
    MainActivity. This handles both the app being in foreground and background (accepting the notification
    will start off the app).

The bandwidth webrtc SDK can be found under webrtc/ All the UI components can be found under res/

### Yet to be implemented

* Online status for users, synced b/w app and datastore via Amplify
* Syncing all datastore updates, in general, to/from cloud datastore
* Multi party calling
* Don't force logging in everytime
* UI field for email during registrations with Cognito
* UI option to confirm email registrations using OTP. For now, to confirm a registration via amplify CLI, do the following \
    aws cognito-idp admin-set-user-password \
         --user-pool-id <your-user-pool-id> \
        --username YOUR_USER_NAME \
        --password YOUR_PASSWORD \
        --permanent
* Hangups (works on the UI but the participant isn't removed from BW WebRTC session)
* Better ringtone on incoming calls
* Better UI for incoming call notifications

### Setup Android project with Firebase
Read https://firebase.google.com/docs/cloud-messaging/android/first-message?authuser=0#create_a_firebase_project 
on how to setup your Android app with Firebase. You will get a google-services.json file as a result 
that you need to add to your android project under app/

### Get started with integrating your Android app with Amplify
https://docs.amplify.aws/start/getting-started/add-api/q/integration/ios/ provides a pretty nice 
step by step documentation of how to integrate your app with Amplify for Android and iOS. Amplify 
is not necessary for hosting your backend but that is the route chosen for this project. You could 
just as well manage infrastructure for your backend resources yourself.
