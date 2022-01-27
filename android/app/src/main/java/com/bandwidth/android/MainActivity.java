package com.bandwidth.android;

import android.Manifest;
import android.app.NotificationManager;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.api.rest.RestResponse;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.query.Where;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.datastore.generated.model.DeviceInfo;
import com.bandwidth.android.ui.login.LoginActivity;
import com.bandwidth.webrtc.RTCBandwidth;
import com.bandwidth.webrtc.RTCBandwidthClient;
import com.bandwidth.webrtc.signaling.ConnectionException;
import com.google.firebase.FirebaseApp;

import org.json.JSONObject;
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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;

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

    public static final String TAG = MainActivity.class.getSimpleName();

    private String theCallerId = null;
    private String theCalleeId = null;

    // this function is called when a user calls another user
    // gets the DeviceInfo table's id using Firebase token
    private void getDeviceIdAndConnect(String calleeId) {
        new Thread((() -> {
            if(LoginActivity.deviceId != null) {
                // on login, we set the deviceId for the user in the java class
                getParticipantTokenAndConnect(LoginActivity.deviceId, calleeId);
                // connect(LoginActivity.deviceId, calleeId);
            }

            // if for some reason, the deviceId for caller is not set already, fetch it from the db
            String deviceToken = BWLibrary.getFirebaseDeviceToken();

            Amplify.DataStore.query(DeviceInfo.class, Where.matches(DeviceInfo.DEVICE_TOKEN.eq(deviceToken)),
                    devices -> {
                        if(devices.hasNext()) {
                            System.out.println(TAG + ": Found deviceId for token " + deviceToken);
                            String callerId = devices.next().getId();
                            getParticipantTokenAndConnect(callerId, calleeId);
                            // connect(LoginActivity.deviceId, calleeId);
                        }
                        // TODO error handling for when deviceId is not found for the deviceToken
                    },
                    failure -> System.out.println(TAG + ": getDeviceIdFromDatastore query failed: " + failure.toString())
            );
        })).start();
    }

    // this function is used when a user accepts an incoming call
    private void joinSession(String pToken) {
        new Thread((() -> {
            System.out.println(TAG  + ": in joinSession()");
            connectToSession(pToken);
        })).start();
    }

    private void configureAmplify() {
        try {
            Amplify.addPlugin(new AWSDataStorePlugin());
            Amplify.configure(getApplicationContext());

        } catch (AmplifyException e) {
            // TODO error handling
            System.out.println(TAG + ": Error initializing user with Amplify" + e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(getApplicationContext());

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
            disconnect();
        });

        Bundle extras = getIntent().getExtras();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(BWLibrary.getNotificationId(getIntent().getExtras()));

        String pToken = getPTokenFromRequest(extras);

        // pToken is set in extras for incoming calls
        if(pToken != null) {
            // We are the callee
            // this is an incoming call and pToken is BW participant token we need to use
            // to join the call
            joinSession(pToken);
        } else {
            // We are the caller; get the callee's client ID and
            //  initiate webrtc call
            getDeviceIdAndConnect(getCalleeId(extras));
        }
//        if(!isConnected) {
//            button.setText("Disconnect");
//        }

    }

    private String getCalleeId(Bundle extras) {
        String calleeId = null;
        if(extras != null) {
            calleeId = extras.getString("calleeId");
        }
        return calleeId;
    }

    // This function is utilized when this activity is called as a result of answering
    // an incoming notification
    private String getPTokenFromRequest(Bundle extras) {
        System.out.println(TAG + ": in getPTokenFromRequest");
        String pToken = null;
        if(extras != null && extras.getString("pToken") != null &&
                !extras.getString("pToken").equals("")) {
            pToken = extras.getString("pToken");
        }
        System.out.println(TAG + ": pToken=" + pToken);
        return pToken;
    }

    /* This function is called when a CALLER initiates a call and is looking
       for a Bandwidth webrtc participantToken in response

       This function calls the sample app's backend on AWS Amplify and the BACKEND
       does several things

     1. API hits Bandwidth's webrtc server and creates a webrtc session
     2. It then creates a webrtc participant for the caller and the callee
     3. It returns the caller's participantToken as a response to this API call
     4. Separately, it will fire off a Push Notification for the callee with their participantToken

        We will eventually use this participantToken for the CALLER, in our Bandwidth's SDK call to
        publish CALLER's mediastream to the webrtc session that was just created.
     */
    private void getParticipantTokenAndConnect(String callerId, String calleeId) {
        try {
            System.out.println("Using AWSApiPlugin...");
            System.out.println("in getParticipantToken: " + callerId + ":::" + calleeId);

            theCallerId = callerId;
            theCalleeId = calleeId;

            String json = "{" +
                    "\"action\": \"initiateCall\"," +
                    "\"callerId\": \"" + callerId + "\"," +
                    "\"calleeId\":\"" + calleeId + "\"" +
                    "}";

            RestOptions options = RestOptions.builder()
                    .addPath("/api")
                    .addBody(json.getBytes())
                    .build();

            Amplify.API.post(options,
                    response -> onTokenReceipt(response),
                    error -> System.out.println("getParticipantTokenAndConnect: POST failed." + error.toString())
            );
        } catch (Exception e) {
            System.out.println("getParticipantTokenAndConnect: Exception trying AWSApiPlugin: " + e.getMessage());
        }
    }

    private void onTokenReceipt(RestResponse response) {
        try {
            System.out.println("Resp from API call: " + response.getData().asJSONObject());
            String bwParticipantToken = (String)response.getData().asJSONObject().get("token");

            connectToSession(bwParticipantToken);
        } catch (Exception e) {
            System.out.println("Error fetching bwParticipantToken from API: "+ e.getMessage());
        }
    }

    // TODO remove this and connect() when email verification from new Cognito users is removed
    private String getParticipantToken(String callerId, String calleeId) {
        try {
            System.out.println("in getParticipantToken: " + callerId + ":::" + calleeId);
            String json = "{" +
                    "\"action\": \"initiateCall\"," +
                    "\"callerId\": \"" + callerId + "\"," +
                    "\"calleeId\":\"" + calleeId + "\"" +
                    "}";

            URL url = new URL(BWLibrary.LAMBDA_URL);
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
            System.out.println(TAG + ":GETTING RESP " + output );
            JSONObject resp = new JSONObject(output);

            return (String)resp.get("token");
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(TAG + ": Error getting participant token " + e.getMessage());
            return null;
        }
    }

    // This function is called when a caller initiates a call
    private void connect(String callerId, String calleeId) {
        // get a Bandwidth participant token for the caller
        String bwParticipantToken = getParticipantToken(callerId, calleeId);

        connectToSession(bwParticipantToken);
    }

    // This function publishes a mediastream using Bandwidth's SDK and the Bandwidth participantToken
    // that has already been added to a webrtc session by the backend
    private void connectToSession(String pToken) {
        offeredVideoTracks = new HashMap<String, VideoTrack>();

        System.out.println("in connectToSession: " + pToken);
        try {
            bandwidth.connect(pToken, () -> {
                bandwidth.publish("hello-world-android-callee", (streamId, mediaTypes, audioSource, audioTrack, videoSource, videoTrack) -> {
                    runOnUiThread(() -> publish(videoSource, videoTrack));
                });
            });
        } catch (ConnectionException e) {
            e.printStackTrace();
            System.out.println("Error publishing mediastream to bandwidth: " + e.getMessage());
        }
    }

    private void disconnect() {

        try {
            videoCapturer.stopCapture();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        localRenderer.clearImage();
        remoteVideoTrack = null;

        bandwidth.disconnect();

        remoteRenderer.clearImage();

        if(theCallerId == null && theCalleeId == null) {
            finishAndRemoveTask();
            return;
        }
        if(theCallerId != null) {
            clearParticipantTokenFromDb(theCallerId);
            theCallerId = null;
        }

        if(theCalleeId != null) {
            clearParticipantTokenFromDb(theCalleeId);
            theCalleeId = null;
        }
    }

    // clears the participantToken field from db
    // this is specific to sample app's backend implementation; in a prod world, you would end
    // the bandwidth webrtc session
    private void clearParticipantTokenFromDb(String clientId) {
        try {
            System.out.println("clearParticipantTokenFromDb() for " + clientId);


            String json = "{" +
                    "\"action\": \"endCall\"," +
                    "\"clientId\": \"" + clientId + "\"" +
                    "}";

            RestOptions options = RestOptions.builder()
                    .addPath("/api")
                    .addBody(json.getBytes())
                    .build();

            Amplify.API.post(options,
                    response -> BWLibrary.showUsers(MainActivity.this),
                    error -> System.out.println("POST failed for clearParticipantToken." + error.toString())
            );
        } catch (Exception e) {

        }
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

        localVideoTrack.setEnabled(true);
        localVideoTrack.addSink(localRenderer);
    }
}