package com.bandwidth.webrtc.types;

public class StreamPublishMetadata {
    private final String alias;

    public StreamPublishMetadata(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }
}
