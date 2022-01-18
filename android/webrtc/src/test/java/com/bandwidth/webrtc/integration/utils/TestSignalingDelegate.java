package com.bandwidth.webrtc.integration.utils;

import com.bandwidth.webrtc.signaling.Signaling;
import com.bandwidth.webrtc.signaling.SignalingDelegate;
import com.bandwidth.webrtc.signaling.rpc.transit.SdpOfferParams;

public class TestSignalingDelegate implements SignalingDelegate {
    @Override
    public void onSdpOffer(Signaling signaling, SdpOfferParams params) {

    }
}
