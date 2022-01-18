package com.bandwidth.webrtc.listeners;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.List;

public interface OnPublishListener {
    void onPublish(String streamId, List<String> mediaTypes, AudioSource audioSource, AudioTrack audioTrack, VideoSource videoSource, VideoTrack videoTrack);
}
