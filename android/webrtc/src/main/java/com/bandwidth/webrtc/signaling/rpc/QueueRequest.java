package com.bandwidth.webrtc.signaling.rpc;

import com.bandwidth.webrtc.signaling.Signaling;

import java.util.Timer;

public class QueueRequest {
    private String method;
    private Signaling.Observer observer;
    private Timer timer;

    public QueueRequest(String method, Signaling.Observer observer, Timer timer) {
        this.method = method;
        this.observer = observer;
        this.timer = timer;
    }

    public String getMethod() {
        return method;
    }

    public Signaling.Observer getObserver() {
        return observer;
    }

    public Timer getTimer() {
        return timer;
    }
}
