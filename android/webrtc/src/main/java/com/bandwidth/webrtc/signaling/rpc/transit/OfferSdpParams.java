package com.bandwidth.webrtc.signaling.rpc.transit;

import com.bandwidth.webrtc.types.PublishMetadata;

public class OfferSdpParams {
    private final String sdpOffer;
    private final PublishMetadata mediaMetadata;

    public OfferSdpParams(String sdpOffer, PublishMetadata mediaMetadata) {
        this.sdpOffer = sdpOffer;
        this.mediaMetadata = mediaMetadata;
    }
}
