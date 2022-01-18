package com.bandwidth.webrtc.signaling;

import com.bandwidth.webrtc.signaling.rpc.transit.SdpOfferParams;

public interface SignalingDelegate {
    void onSdpOffer(Signaling signaling, SdpOfferParams params);
}
