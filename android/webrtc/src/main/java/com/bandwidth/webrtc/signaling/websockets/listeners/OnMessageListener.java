package com.bandwidth.webrtc.signaling.websockets.listeners;

import com.bandwidth.webrtc.signaling.websockets.WebSocketProvider;

public interface OnMessageListener {
    void onMessage(WebSocketProvider webSocketProvider, String message);
}
