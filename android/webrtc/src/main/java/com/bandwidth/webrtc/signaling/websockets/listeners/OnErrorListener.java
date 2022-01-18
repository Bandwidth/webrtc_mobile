package com.bandwidth.webrtc.signaling.websockets.listeners;

import com.bandwidth.webrtc.signaling.websockets.WebSocketProvider;

public interface OnErrorListener {
    void onError(WebSocketProvider webSocketProvider, Throwable throwable);
}
