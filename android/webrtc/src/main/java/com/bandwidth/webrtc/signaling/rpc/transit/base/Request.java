package com.bandwidth.webrtc.signaling.rpc.transit.base;

public class Request<T> {
    private String id;
    private String jsonrpc;
    private String method;
    private T params;

    public Request(String id, String jsonrpc, String method, T params) {
        this.id = id;
        this.jsonrpc = jsonrpc;
        this.method = method;
        this.params = params;
    }

    public String getId() {
        return id;
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
