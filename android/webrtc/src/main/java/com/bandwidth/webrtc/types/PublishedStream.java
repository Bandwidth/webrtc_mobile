package com.bandwidth.webrtc.types;

import org.webrtc.MediaStream;

public class PublishedStream {
    private MediaStream mediaStream;
    private StreamPublishMetadata metadata;

    public PublishedStream(MediaStream mediaStream, StreamPublishMetadata metadata) {
        this.mediaStream = mediaStream;
        this.metadata = metadata;
    }

    public MediaStream getMediaStream() {
        return mediaStream;
    }

    public StreamPublishMetadata getMetadata() {
        return metadata;
    }
}
