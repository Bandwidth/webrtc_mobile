package com.bandwidth.webrtc.listeners;

import org.webrtc.AudioTrack;
import org.webrtc.VideoTrack;

import java.util.List;

public interface OnStreamAvailableListener {
    void onStreamAvailable(String streamId, List<String> mediaTypes, List<AudioTrack> audioTracks, List<VideoTrack> videoTracks, String alias);
}
