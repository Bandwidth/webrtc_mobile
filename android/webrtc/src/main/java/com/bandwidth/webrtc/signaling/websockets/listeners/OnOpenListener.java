package com.bandwidth.webrtc.signaling.websockets.listeners;

import com.bandwidth.webrtc.signaling.websockets.WebSocketProvider;

public interface OnOpenListener {
    void onOpen(WebSocketProvider webSocketProvider);
}
