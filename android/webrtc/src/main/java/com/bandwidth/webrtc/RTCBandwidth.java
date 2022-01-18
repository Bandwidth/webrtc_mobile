package com.bandwidth.webrtc;

import com.bandwidth.webrtc.listeners.OnConnectListener;
import com.bandwidth.webrtc.listeners.OnPublishListener;
import com.bandwidth.webrtc.listeners.OnStreamAvailableListener;
import com.bandwidth.webrtc.listeners.OnStreamUnavailableListener;
import com.bandwidth.webrtc.listeners.OnUnpublishListener;
import com.bandwidth.webrtc.signaling.ConnectionException;

import java.net.URI;
import java.util.List;

public interface RTCBandwidth {
    void connect(String deviceToken, OnConnectListener onConnectListener) throws ConnectionException;
    void connect(String webSocketUrl, String deviceToken, OnConnectListener onConnectListener) throws ConnectionException;
    void disconnect();

    void publish(String alias, OnPublishListener onPublishListener);
    void unpublish(List<String> streamIds, OnUnpublishListener onUnpublishListener);

    void setOnStreamAvailableListener(OnStreamAvailableListener onStreamAvailableListener);
    void setOnStreamUnavailableListener(OnStreamUnavailableListener onStreamUnavailableListener);
}
