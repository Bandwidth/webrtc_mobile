package com.bandwidth.webrtc.signaling.websockets.listeners;

import com.bandwidth.webrtc.signaling.websockets.WebSocketProvider;

public interface OnCloseListener {
    void onClose(WebSocketProvider webSocketProvider);
}
