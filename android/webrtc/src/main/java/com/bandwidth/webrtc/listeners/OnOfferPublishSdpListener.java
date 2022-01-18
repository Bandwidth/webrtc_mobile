package com.bandwidth.webrtc.listeners;

import com.bandwidth.webrtc.signaling.rpc.transit.OfferSdpResult;

public interface OnOfferPublishSdpListener {
    void onOfferPublishSdp(OfferSdpResult result);
}
