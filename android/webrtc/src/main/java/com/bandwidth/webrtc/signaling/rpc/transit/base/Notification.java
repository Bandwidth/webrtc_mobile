package com.bandwidth.webrtc.signaling.rpc.transit.base;

public class Notification<T> {
    private String jsonrpc;
    private String method;
    private T params;

    public Notification(String jsonrpc, String method, T params) {
        this.jsonrpc = jsonrpc;
        this.method = method;
        this.params = params;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public T getParams() {
        return params;
    }
}
