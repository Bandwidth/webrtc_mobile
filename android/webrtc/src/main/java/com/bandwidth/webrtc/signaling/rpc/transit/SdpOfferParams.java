package com.bandwidth.webrtc.signaling.rpc.transit;

import com.bandwidth.webrtc.types.StreamMetadata;

import java.util.Map;

public class SdpOfferParams {
    private String endpointId;
    private String sdpOffer;
    private Integer sdpRevision;
    private Map<String, StreamMetadata> streamMetadata;

    public String getSdpOffer() {
        return sdpOffer;
    }

    public Map<String, StreamMetadata> getStreamMetadata() {
        return streamMetadata;
    }
}
