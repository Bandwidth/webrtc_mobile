package com.bandwidth.webrtc.signaling.rpc.transit.base;

public class Response<T> {
    private String id;
    private String jsonrpc;
    private T result;

    public String getId() {
        return id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public T getResult() {
        return result;
    }
}
