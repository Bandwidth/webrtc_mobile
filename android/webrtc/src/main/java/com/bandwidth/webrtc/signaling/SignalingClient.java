package com.bandwidth.webrtc.signaling;

import com.bandwidth.webrtc.signaling.listeners.OnConnectListener;
import com.bandwidth.webrtc.signaling.listeners.OnDisconnectListener;
import com.bandwidth.webrtc.signaling.rpc.QueueRequest;
import com.bandwidth.webrtc.signaling.rpc.transit.AnswerSdpParams;
import com.bandwidth.webrtc.signaling.rpc.transit.AnswerSdpResult;
import com.bandwidth.webrtc.signaling.rpc.transit.LeaveParams;
import com.bandwidth.webrtc.signaling.rpc.transit.OfferSdpParams;
import com.bandwidth.webrtc.signaling.rpc.transit.OfferSdpResult;
import com.bandwidth.webrtc.signaling.rpc.transit.SdpOfferParams;
import com.bandwidth.webrtc.signaling.rpc.transit.SetMediaPreferencesParams;
import com.bandwidth.webrtc.signaling.rpc.transit.SetMediaPreferencesResult;
import com.bandwidth.webrtc.signaling.rpc.transit.base.Notification;
import com.bandwidth.webrtc.signaling.rpc.transit.base.Request;
import com.bandwidth.webrtc.signaling.rpc.transit.base.Response;
import com.bandwidth.webrtc.signaling.websockets.WebSocketProvider;
import com.bandwidth.webrtc.types.PublishMetadata;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class SignalingClient implements Signaling {
    private final WebSocketProvider webSocketProvider;
    private final SignalingDelegate delegate;

    private final String deviceUniqueId = UUID.randomUUID().toString();

    private final Map<String, QueueRequest> pendingQueueRequests = new HashMap<>();

    private Boolean hasSetMediaPreferences = false;

    private OnConnectListener onConnectListener;
    private OnDisconnectListener onDisconnectListener;

    public SignalingClient(WebSocketProvider webSocketProvider, SignalingDelegate delegate) {
        this.webSocketProvider = webSocketProvider;

        webSocketProvider.setOnOpenListener(onOpenWebSocketProvider -> {
            if (!hasSetMediaPreferences) {
                // Set media preferences once the WebSocket connection has been opened.
                setMediaPreferences("WEBRTC", new Signaling.Adapter() {
                    @Override
                    public void onSetMediaPreferences(Signaling signaling, SetMediaPreferencesResult result) {
                        super.onSetMediaPreferences(signaling, result);

                        hasSetMediaPreferences = true;

                        if (onConnectListener != null) {
                            onConnectListener.onConnect(SignalingClient.this);
                        }
                    }
                });
            } else {
                if (onConnectListener != null) {
                    onConnectListener.onConnect(SignalingClient.this);
                }
            }
        });

        webSocketProvider.setOnCloseListener(onCloseWebSocketProvider -> {
            if (onDisconnectListener != null) {
                onDisconnectListener.onDisconnect(SignalingClient.this);
            }
        });

        webSocketProvider.setOnMessageListener((onMessageWebSocketProvider, message) -> {
            // Determine if we're receiving a response or notification.
            Response response = new Gson().fromJson(message, Response.class);
            Notification notification = new Gson().fromJson(message, Notification.class);

            if (response != null && response.getId() != null) {
                handleResponse(message, response.getId());
            } else if (notification != null) {
                handleNotification(message, notification);
            }

            System.out.println("↓ " + message);
        });

        webSocketProvider.setOnErrorListener((onErrorWebSocketProvider, throwable) -> {

        });

        this.delegate = delegate;
    }

    @Override
    public void setOnConnectListener(OnConnectListener onConnectListener) {
        this.onConnectListener = onConnectListener;
    }

    @Override
    public void setOnDisconnectListener(OnDisconnectListener onDisconnectListener) {
        this.onDisconnectListener = onDisconnectListener;
    }

    @Override
    public void connect(String deviceToken) throws ConnectionException {
        connect("wss://device.webrtc.bandwidth.com", deviceToken);
    }

    @Override
    public void connect(String webSocketUrl, String deviceToken) throws ConnectionException {
        String url = String.format("%s/v3/?token=%s&client=android&sdkVersion=0.1.0-alpha.2&uniqueId=%s", webSocketUrl, deviceToken, deviceUniqueId);
        webSocketProvider.open(url);
    }

    @Override
    public void disconnect() {
        LeaveParams params = new LeaveParams();
        Notification<LeaveParams> notification = new Notification<>("2.0", "leave", params);

        sendNotification(notification);

        hasSetMediaPreferences = false;

        webSocketProvider.close();
    }

    @Override
    public void offerSdp(String sdp, PublishMetadata publishMetadata, Observer observer) {
        OfferSdpParams params = new OfferSdpParams(sdp, publishMetadata);
        Request<OfferSdpParams> request = new Request<>(UUID.randomUUID().toString(), "2.0", "offerSdp", params);

        sendRequest(request, observer);
    }

    @Override
    public void answerSdp(String sdp, Observer observer) {
        AnswerSdpParams params = new AnswerSdpParams(sdp);
        Request<AnswerSdpParams> request = new Request<>(UUID.randomUUID().toString(), "2.0", "answerSdp", params);

        sendRequest(request, observer);
    }

    private void setMediaPreferences(String protocol, Observer observer) {
        SetMediaPreferencesParams params = new SetMediaPreferencesParams(protocol);
        Request<SetMediaPreferencesParams> request = new Request<>(UUID.randomUUID().toString(), "2.0", "setMediaPreferences", params);

        sendRequest(request, observer);
    }

    private void sendRequest(Request request, Observer observer) {
        sendRequest(request, observer, 5000L);
    }

    private void sendRequest(Request request, Observer observer, Long timeout) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                pendingQueueRequests.remove(request.getId());
                this.cancel();
            }
        }, timeout);

        // Keep a reference to our request as we wait for a response.
        pendingQueueRequests.put(request.getId(), new QueueRequest(request.getMethod(), observer, timer));

        String json = new Gson().toJson(request);

        System.out.println("↑ " + json);

        // Send our request to the moon (or signaling server).
        webSocketProvider.sendMessage(json);
    }

    private void sendNotification(Notification notification) {
        String json = new Gson().toJson(notification);

        System.out.println("↑ " + json);

        // Send our notification to the moon (or signaling server).
        webSocketProvider.sendMessage(json);
    }

    private void handleResponse(String message, String id) {
        QueueRequest pendingQueueRequest = pendingQueueRequests.get(id);
        pendingQueueRequest.getTimer().cancel();

        pendingQueueRequests.remove(id);

        switch (pendingQueueRequest.getMethod()) {
            case "setMediaPreferences":
                Type setMediaPreferencesResultType = new TypeToken<Response<SetMediaPreferencesResult>>() { }.getType();
                Response<SetMediaPreferencesResult> setMediaPreferencesResponse = new Gson().fromJson(message, setMediaPreferencesResultType);

                pendingQueueRequest.getObserver().onSetMediaPreferences(this, setMediaPreferencesResponse.getResult());
                break;
            case "offerSdp":
                Type offerSdpResultType = new TypeToken<Response<OfferSdpResult>>() { }.getType();
                Response<OfferSdpResult> offerSdpResponse = new Gson().fromJson(message, offerSdpResultType);

                pendingQueueRequest.getObserver().onOfferSdp(this, offerSdpResponse.getResult());
                break;
            case "answerSdp":
                Type answerSdpResultType = new TypeToken<Response<AnswerSdpResult>>() {}.getType();
                Response<AnswerSdpResult> answerSdpResponse = new Gson().fromJson(message, answerSdpResultType);

                pendingQueueRequest.getObserver().onAnswerSdp(this, answerSdpResponse.getResult());
                break;
        }
    }

    private void handleNotification(String message, Notification notification) {
        if (notification.getMethod().equals("sdpOffer")) {
            Type sdpOfferNotificationType = new TypeToken<Notification<SdpOfferParams>>() { }.getType();
            Notification<SdpOfferParams> sdpOfferNotification = new Gson().fromJson(message, sdpOfferNotificationType);

            delegate.onSdpOffer(this, sdpOfferNotification.getParams());
        }
    }
}
