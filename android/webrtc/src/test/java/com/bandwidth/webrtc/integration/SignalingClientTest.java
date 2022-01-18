package com.bandwidth.webrtc.integration;

import com.bandwidth.webrtc.integration.utils.TestSignalingDelegate;
import com.bandwidth.webrtc.integration.utils.app.Conference;
import com.bandwidth.webrtc.signaling.ConnectionException;
import com.bandwidth.webrtc.signaling.SignalingClient;
import com.bandwidth.webrtc.signaling.SignalingDelegate;
import com.bandwidth.webrtc.signaling.websockets.NeoVisionariesWebSocket;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SignalingClientTest {
    CountDownLatch lock = new CountDownLatch(1);

    final String webRtcServerPath = System.getenv("BANDWIDTH_URL_WEBRTC_SERVER");
    final String conferenceServerPath = System.getenv("BANDWIDTH_URL_WEBRTC_CONFERENCE_SERVER");

    @Test
    public void shouldConnectThenDisconnect() throws IOException, URISyntaxException, InterruptedException, ConnectionException {
        String deviceToken = Conference.getInstance().requestDeviceToken(conferenceServerPath);
        String uniqueId = UUID.randomUUID().toString();

        String path = String.format("%s?token=%s&uniqueId=%s", webRtcServerPath, deviceToken, uniqueId);
        URI uri = new URI(path);

        SignalingDelegate delegate = new TestSignalingDelegate();
        SignalingClient client = new SignalingClient(new NeoVisionariesWebSocket(), delegate);

        AtomicReference<Boolean> hasConnected = new AtomicReference<>(false);
        AtomicReference<Boolean> hasDisconnected = new AtomicReference<>(false);

        client.setOnConnectListener(signaling -> {
            hasConnected.set(true);
            client.disconnect();
        });

        client.setOnDisconnectListener(signaling -> {
            hasDisconnected.set(true);
            lock.countDown();
        });

        client.connect(webRtcServerPath, deviceToken);

        lock.await(5000, TimeUnit.MILLISECONDS);

        Assert.assertTrue(hasConnected.get());
        Assert.assertTrue(hasDisconnected.get());
    }
}
