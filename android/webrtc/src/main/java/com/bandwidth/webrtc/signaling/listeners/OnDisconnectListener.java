package com.bandwidth.webrtc.signaling.listeners;

import com.bandwidth.webrtc.signaling.Signaling;

public interface OnDisconnectListener {
    void onDisconnect(Signaling signaling);
}
