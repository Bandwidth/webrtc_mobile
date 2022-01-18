package com.bandwidth.webrtc.signaling.websockets;

import com.bandwidth.webrtc.signaling.ConnectionException;
import com.bandwidth.webrtc.signaling.websockets.listeners.OnCloseListener;
import com.bandwidth.webrtc.signaling.websockets.listeners.OnErrorListener;
import com.bandwidth.webrtc.signaling.websockets.listeners.OnMessageListener;
import com.bandwidth.webrtc.signaling.websockets.listeners.OnOpenListener;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class NeoVisionariesWebSocket implements WebSocketProvider {
    private WebSocket webSocket;

    private OnOpenListener onOpenListener;
    private OnCloseListener onCloseListener;
    private OnMessageListener onMessageListener;
    private OnErrorListener onErrorListener;

    public NeoVisionariesWebSocket() {

    }

    @Override
    public void setOnOpenListener(OnOpenListener onOpenListener) {
        this.onOpenListener = onOpenListener;
    }

    @Override
    public void setOnCloseListener(OnCloseListener onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    @Override
    public void setOnMessageListener(OnMessageListener onMessageListener) {
        this.onMessageListener = onMessageListener;
    }

    @Override
    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    @Override
    public void open(String uri) throws ConnectionException {
        try {
            webSocket = new WebSocketFactory().createSocket(uri);
            webSocket.addListener(new WebSocketAdapter() {
                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    super.onConnected(websocket, headers);

                    if (onOpenListener != null) {
                        onOpenListener.onOpen(NeoVisionariesWebSocket.this);
                    }
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);

                    if (onCloseListener != null) {
                        onCloseListener.onClose(NeoVisionariesWebSocket.this);
                    }
                }

                @Override
                public void onTextMessage(WebSocket websocket, String message) throws Exception {
                    super.onTextMessage(websocket, message);

                    if (onMessageListener != null) {
                        onMessageListener.onMessage(NeoVisionariesWebSocket.this, message);
                    }
                }

                @Override
                public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                    super.onError(websocket, cause);

                    if (onErrorListener != null) {
                        onErrorListener.onError(NeoVisionariesWebSocket.this, cause);
                    }
                }
            });
            webSocket.connect();
        } catch (IOException | WebSocketException e) {
            throw new ConnectionException("Could not connect to signaling server.", e);
        }
    }

    @Override
    public void close() {
        webSocket.sendClose();
    }

    @Override
    public void sendMessage(String message) {
        webSocket.sendText(message);
    }
}
