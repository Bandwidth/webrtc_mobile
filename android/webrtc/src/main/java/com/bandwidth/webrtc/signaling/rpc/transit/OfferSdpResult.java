package com.bandwidth.webrtc.signaling.rpc.transit;

import com.bandwidth.webrtc.types.StreamMetadata;

import java.util.Map;

public class OfferSdpResult {
    private String endpointId;
    private String sdpAnswer;
    private Map<String, StreamMetadata> streamMetadata;

    public String getEndpointId() {
        return endpointId;
    }

    public String getSdpAnswer() {
        return sdpAnswer;
    }

    public Map<String, StreamMetadata> getStreamMetadata() {
        return streamMetadata;
    }
}
