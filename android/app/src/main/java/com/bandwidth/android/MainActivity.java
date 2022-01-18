package com.bandwidth.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.datastore.generated.model.Person;
import com.bandwidth.android.app.Conference;
import com.bandwidth.webrtc.RTCBandwidth;
import com.bandwidth.webrtc.RTCBandwidthClient;
import com.bandwidth.webrtc.signaling.ConnectionException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private SurfaceViewRenderer localRenderer;
    private SurfaceViewRenderer remoteRenderer;

    private RTCBandwidth bandwidth;

    VideoCapturer videoCapturer;

    private VideoTrack localVideoTrack;
    private VideoTrack remoteVideoTrack;
    private HashMap<String, VideoTrack> offeredVideoTracks = new HashMap<String, VideoTrack>();

    private EglBase eglBase;

    private Boolean isConnected = false;

    // Push Notification Test Start

    public static final String TAG = MainActivity.class.getSimpleName();

    private static PinpointManager pinpointManager;

    /*
    private String getFirebaseDeviceToken() {
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
     */

    public static PinpointManager getPinpointManager(final Context applicationContext) {
        if (pinpointManager == null) {
            final AWSConfiguration awsConfig = new AWSConfiguration(applicationContext);
            AWSMobileClient.getInstance().initialize(applicationContext, awsConfig, new Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails userStateDetails) {
                    System.out.println("INIT " + userStateDetails.getUserState());
                }

                @Override
                public void onError(Exception e) {
                    System.out.println("INIT " + "Initialization error." + e.getMessage());
                }
            });

            PinpointConfiguration pinpointConfig = new PinpointConfiguration(
                    applicationContext,
                    AWSMobileClient.getInstance(),
                    awsConfig);

            pinpointManager = new PinpointManager(pinpointConfig);

            System.out.println("ABOUT TO GET FCM TOKEN");
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
//                                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                return;
                            }
                            final String token = task.getResult();
                            System.out.println(TAG + " Registering push notifications token: " + token);
                            pinpointManager.getNotificationClient().registerDeviceToken(token);
                        }
                    });
        }
        return pinpointManager;
    }
    // Push Notification Test End

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            System.out.println("initing firebase app");
            FirebaseApp.initializeApp(getApplicationContext());
            // Initialize PinpointManager
            getPinpointManager(getApplicationContext());
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.addPlugin(new AWSDataStorePlugin());


            Amplify.configure(getApplicationContext());
            System.out.println("SIGNING IN");
            Amplify.Auth.signIn(
                    "sayengar-amplify",
                    "raleigh123",
                    result -> System.out.println(result.isSignInComplete() ? "Sign in succeeded" : "Sign in not compelte " + result),
                    error -> System.out.println(error.toString())
            );
        } catch (AmplifyException e) {
            System.out.println("Error initializing / authenticating user with Amplify" + e.getMessage());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO}, 100);

        localRenderer = findViewById(R.id.localSurfaceViewRenderer);
        remoteRenderer = findViewById(R.id.remoteSurfaceViewRenderer);

        eglBase = EglBase.create();

        // Create local video renderer.
        localRenderer.init(eglBase.getEglBaseContext(), null);
        localRenderer.setMirror(true);
        localRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        // Create remote video renderer.
        remoteRenderer.init(eglBase.getEglBaseContext(), null);
        remoteRenderer.setMirror(false);
        remoteRenderer.setEnableHardwareScaler(false);

        bandwidth = new RTCBandwidthClient(getApplicationContext(), eglBase.getEglBaseContext());

        bandwidth.setOnStreamAvailableListener((streamId, mediaTypes, audioTracks, videoTracks, alias) -> {
            if (!videoTracks.isEmpty()) {
                if (!offeredVideoTracks.containsKey(streamId)) {
                    offeredVideoTracks.put(streamId,videoTracks.get(0));
                }
                if (remoteVideoTrack == null) {
                    runOnUiThread(() -> {
                        remoteVideoTrack = videoTracks.get(0);
                        remoteVideoTrack.setEnabled(true);
                        remoteVideoTrack.addSink(remoteRenderer);
                    });
                };
            };
        });

        bandwidth.setOnStreamUnavailableListener(streamId -> {
            offeredVideoTracks.remove(streamId);
            if (offeredVideoTracks.isEmpty()) {
                System.out.println("on Stream NOT available - no more tracks");
                remoteRenderer.clearImage();
                remoteVideoTrack = null;
            } else {
                VideoTrack temp = offeredVideoTracks.entrySet().iterator().next().getValue();
                System.out.println("on Stream NOT available - video track replacement - " +  temp);
                runOnUiThread(() -> {
                    remoteVideoTrack = temp;
                    remoteVideoTrack.setEnabled(true);
                    remoteVideoTrack.addSink(remoteRenderer);
                });
            };
        });

        final Button button = findViewById(R.id.connectButton);
        button.setOnClickListener(view -> {
            if (isConnected) {
                disconnect();
                button.setText("Connect");
            } else {
                connect();
                button.setText("Disconnect");
            }
        });

        connect();
        if(!isConnected) {
            button.setText("Disconnect");
        }

        /*
        try {
            System.out.println("Initialized Amplify with cloud sync");

            Amplify.DataStore.observe(Person.class,
                    started -> System.out.println("Observation began."),
                    change -> System.out.println(change.item().toString()),
                    failure -> System.out.println("Observation failed." + failure),
                    () -> System.out.println("Observation complete.")
            );
//            Person person1 = Person.builder()
//                    .firstName("Amplify")
//                    .lastName("Test")
//                    .clientId("dummyClientId1")
//                    .build();
//
//            Person person2 = Person.builder()
//                    .firstName("Srikant")
//                    .lastName("Ayengar")
//                    .clientId("dummyClientId2")
//                    .build();
//
//            Amplify.DataStore.save(person1,
//                    success -> System.out.println("Saved person " + person1.getFirstName()),
//                    error -> System.out.println("Could not save person to Datastore" + error)
//            );
//
//            Amplify.DataStore.save(person2,
//                    success -> System.out.println("Saved person " + person2.getFirstName()),
//                    error -> System.out.println("Could not save person to Datastore" + error)
//            );

            Amplify.DataStore.query(Person.class,
                    persons -> {
                        while (persons.hasNext()) {
                            Person p = persons.next();

                            System.out.println("==== Persons ====");
                            System.out.println("Name: " + p.getFirstName());
                        }
                    },
                    failure -> System.out.println("Could not query DataStore" + failure)
            );

        } catch (Exception e) {
            System.out.println("Error querying datastore with Amplify" + e.getMessage());
        }
        */
    }

    private void connect() {
        offeredVideoTracks = new HashMap<String, VideoTrack>();
        new Thread((() -> {
            try {
//                registerClient();
                // This device token is Bandwidth WebRTC's device token
                String deviceToken = Conference.getInstance().requestDeviceToken("https://sayengar.ngrok.io/connectionInfo");
//                String deviceToken = "";
//                bandwidth.connect("https://sayengar.ngrok.io", deviceToken, () -> {
                bandwidth.connect(deviceToken, () -> {
                    isConnected = true;

                    bandwidth.publish("hello-world-android", (streamId, mediaTypes, audioSource, audioTrack, videoSource, videoTrack) -> {
                        runOnUiThread(() -> publish(videoSource, videoTrack));
                    });
                });
            } catch (IOException | ConnectionException e) {
                e.printStackTrace();
                System.out.println((e.getMessage()));
            }
        })).start();
    }

    private void disconnect() {
        isConnected = false;

        try {
            videoCapturer.stopCapture();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        localRenderer.clearImage();
        remoteVideoTrack = null;

        bandwidth.disconnect();

        remoteRenderer.clearImage();
    }

    private VideoCapturer createVideoCapturer() {
        CameraEnumerator cameraEnumerator = createCameraEnumerator();

        String[] deviceNames = cameraEnumerator.getDeviceNames();

        for (String deviceName : deviceNames) {
            if (cameraEnumerator.isFrontFacing(deviceName)) {
                return cameraEnumerator.createCapturer(deviceName, null);
            }
        }

        return null;
    }

    private CameraEnumerator createCameraEnumerator() {
        if (Camera2Enumerator.isSupported(getApplicationContext())) {
            return new Camera2Enumerator(getApplicationContext());
        }

        return new Camera1Enumerator(false);
    }

    private void publish(VideoSource videoSource, VideoTrack videoTrack) {
        localVideoTrack = videoTrack;

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());

        videoCapturer = createVideoCapturer();
        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(640, 480, 20);

        System.out.println("video stream attributes" + videoTrack);

        localVideoTrack.setEnabled(true);
        localVideoTrack.addSink(localRenderer);
    }

    /*
    private void registerClient() {
        // get firebase device token first
        String deviceToken = getFirebaseDeviceToken();

        System.out.println("REGISTERING DEVICE");
        try {
            // TODO
            // is notifyType supposed to be FCM or GCM ?
            String json = "{" +
                    "\"action\": \"register\"," +
                    "\"notifyType\": \"GCM\"," +
                    "\"deviceToken\":\"" + deviceToken + "\"" +
                    "}";

            String registerUrl = "https://eys0a9ycb7.execute-api.us-east-1.amazonaws.com/default/webrtcPushNotifier-staging";

            URL url = new URL(registerUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setDoOutput(true );
            connection.connect();

            OutputStream os = connection.getOutputStream();
            os.write(json.getBytes("UTF-8"));

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String output;
            while ((output = bufferedReader.readLine()) != null) {
                stringBuilder.append(output);
            }
            output = stringBuilder.toString();
            System.out.println("GETTING RESP " + output );
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error registering client " + e.getMessage());
        }
    }
     */
}