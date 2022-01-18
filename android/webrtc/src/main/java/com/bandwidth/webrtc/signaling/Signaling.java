package com.bandwidth.webrtc.signaling;

import com.bandwidth.webrtc.signaling.listeners.OnConnectListener;
import com.bandwidth.webrtc.signaling.listeners.OnDisconnectListener;
import com.bandwidth.webrtc.signaling.rpc.transit.AnswerSdpResult;
import com.bandwidth.webrtc.signaling.rpc.transit.OfferSdpResult;
import com.bandwidth.webrtc.signaling.rpc.transit.SetMediaPreferencesResult;
import com.bandwidth.webrtc.types.PublishMetadata;

import java.net.URI;

public interface Signaling {
    void setOnConnectListener(OnConnectListener listener);
    void setOnDisconnectListener(OnDisconnectListener listener);

    void connect(String deviceToken) throws ConnectionException;
    void connect(String webSocketUrl, String deviceToken) throws ConnectionException;
    void disconnect();
    void offerSdp(String sdp, PublishMetadata publishMetadata, Observer observer);
    void answerSdp(String sdp, Observer observer);

    interface Observer {
        void onOfferSdp(Signaling signaling, OfferSdpResult result);
        void onAnswerSdp(Signaling signaling, AnswerSdpResult result);
        void onSetMediaPreferences(Signaling signaling, SetMediaPreferencesResult result);
    }

    class Adapter implements Observer {
        @Override
        public void onOfferSdp(Signaling signaling, OfferSdpResult result) {

        }

        @Override
        public void onAnswerSdp(Signaling signaling, AnswerSdpResult result) {

        }

        @Override
        public void onSetMediaPreferences(Signaling signaling, SetMediaPreferencesResult result) {

        }
    }
}
