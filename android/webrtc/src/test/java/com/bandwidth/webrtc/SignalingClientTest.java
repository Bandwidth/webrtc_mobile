package com.bandwidth.webrtc;

import com.bandwidth.webrtc.signaling.Signaling;
import com.bandwidth.webrtc.signaling.SignalingClient;
import com.bandwidth.webrtc.signaling.SignalingDelegate;
import com.bandwidth.webrtc.signaling.rpc.transit.AnswerSdpResult;
import com.bandwidth.webrtc.signaling.rpc.transit.OfferSdpResult;
import com.bandwidth.webrtc.signaling.websockets.WebSocketProvider;
import com.bandwidth.webrtc.types.DataChannelPublishMetadata;
import com.bandwidth.webrtc.types.PublishMetadata;
import com.bandwidth.webrtc.types.StreamPublishMetadata;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SignalingClientTest {
    @Test
    public void shouldAnswerSdp() {
        WebSocketProvider mockedWebSocketProvider = mock(WebSocketProvider.class);
        SignalingDelegate mockedSignalingDelegate = mock(SignalingDelegate.class);

        Signaling signaling = new SignalingClient(mockedWebSocketProvider, mockedSignalingDelegate);
        signaling.answerSdp("sdp-123", new Signaling.Adapter() {
            @Override
            public void onAnswerSdp(Signaling signaling, AnswerSdpResult result) {
                super.onAnswerSdp(signaling, result);
            }
        });

        // Pattern matching for an sdp offer, required due to each request having a unique id.
        String pattern = "^\\{\"id\":\"[\\w\\d-]+\",\"jsonrpc\":\"2.0\",\"method\":\"answerSdp\",\"params\":\\{\"sdpAnswer\":\"sdp-123\"\\}\\}$";

        verify(mockedWebSocketProvider, times(1)).sendMessage(matches(pattern));
    }

    @Test
    public void shouldOfferSdpWithEmptyMediaStreamsAndDataChannels() {
        WebSocketProvider mockedWebSocketProvider = mock(WebSocketProvider.class);
        SignalingDelegate mockedSignalingDelegate = mock(SignalingDelegate.class);

        Map<String, StreamPublishMetadata> mediaStreams = new HashMap<>();
        Map<String, DataChannelPublishMetadata> dataChannels = new HashMap<>();

        PublishMetadata publishMetadata = new PublishMetadata(mediaStreams, dataChannels);

        Signaling signaling = new SignalingClient(mockedWebSocketProvider, mockedSignalingDelegate);
        signaling.offerSdp("sdp-123", publishMetadata, new Signaling.Adapter() {
            @Override
            public void onOfferSdp(Signaling signaling, OfferSdpResult result) {
                super.onOfferSdp(signaling, result);
            }
        });

        // Pattern matching for an sdp offer, required due to each request having a unique id.
        String pattern = "^\\{\"id\":\"[\\w\\d-]+\",\"jsonrpc\":\"2.0\",\"method\":\"offerSdp\",\"params\":\\{\"sdpOffer\":\"sdp-123\",\"mediaMetadata\":\\{\"mediaStreams\":\\{\\},\"dataChannels\":\\{\\}\\}\\}\\}$";

        verify(mockedWebSocketProvider, times(1)).sendMessage(matches(pattern));
    }
}
