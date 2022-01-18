package com.bandwidth.webrtc.types;

import java.util.Map;

public class PublishMetadata {
    Map<String, StreamPublishMetadata> mediaStreams;
    Map<String, DataChannelPublishMetadata> dataChannels;

    public PublishMetadata(Map<String, StreamPublishMetadata> mediaStreams, Map<String, DataChannelPublishMetadata> dataChannels) {
        this.mediaStreams = mediaStreams;
        this.dataChannels = dataChannels;
    }
}
