package com.bandwidth.android.app;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        System.out.println("URL is " + path);
        URL url = new URL(path);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
//        connection.setDoOutput(true );
//        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
//        connection.setRequestProperty("Accept","*/*");
        connection.connect();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();

        String output;
        while ((output = bufferedReader.readLine()) != null) {
            stringBuilder.append(output);
        }
        output = stringBuilder.toString();

        ParticipantsResponse response = new Gson().fromJson(output, ParticipantsResponse.class);
        System.out.println("Device Token is " + response.getDeviceToken());
        return response.getDeviceToken();
    }
}
