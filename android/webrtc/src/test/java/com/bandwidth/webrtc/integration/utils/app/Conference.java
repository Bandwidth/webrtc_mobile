package com.bandwidth.webrtc.integration.utils.app;

import com.bandwidth.webrtc.integration.SignalingClientTest;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Conference {
    private static Conference instance;

    private Conference() {

    }

    public static Conference getInstance() {
        if (instance == null) {
            instance = new Conference();
        }
        return instance;
    }


    public String requestDeviceToken(String path) throws IOException {
        URL url = new URL(path);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.connect();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();

        String output;
        while ((output = bufferedReader.readLine()) != null) {
            stringBuilder.append(output);
        }
        output = stringBuilder.toString();

        ParticipantsResponse response = new Gson().fromJson(output, ParticipantsResponse.class);
        return response.getDeviceToken();
    }
}
