package com.bandwidth.webrtc.types;

public class DataChannelPublishMetadata {
    private final String label;
    private final Integer streamId;

    public DataChannelPublishMetadata(String label, Integer streamId) {
        this.label = label;
        this.streamId = streamId;
    }
}
