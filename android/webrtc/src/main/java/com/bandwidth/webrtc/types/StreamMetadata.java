package com.bandwidth.webrtc.types;

import java.util.List;

public class StreamMetadata {
    private String endpointId;
    private List<String> mediaTypes;
    private String alias;
    private String participantId;

    public List<String> getMediaTypes() {
        return mediaTypes;
    }

    public String getAlias() {
        return alias;
    }

    public String getParticipantId() {
        return participantId;
    }
}
