package com.example.myapplication3.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {
    private static final Utils instance = new Utils();

    private Utils() {
    }

    public static Utils getInstance() {
        return instance;
    }

    String CAMERA_NUMBER_PREF = "camera_number";

    public static String getCameraNumber(Context c) {
        SharedPreferences pref;

        pref = c.getSharedPreferences("pref", Activity.MODE_PRIVATE);

        String cameraNumberStr = pref.getString(getInstance().CAMERA_NUMBER_PREF, "");
        return cameraNumberStr;
    }

    public static void saveCameraNumber(String cameraNumberStr, Context c) {
        SharedPreferences pref;
        SharedPreferences.Editor editor;

        pref = c.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();
        editor.putString(getInstance().CAMERA_NUMBER_PREF, cameraNumberStr);
        editor.apply();
    }

    public static JSONObject requestGetHttp(String url_name) {
        JSONObject response = null;
        try {
            URL url = null;
            url = new URL("http://54.180.109.99:8000/" + url_name);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            final StringBuilder sb = new StringBuilder();

            if (conn != null) {
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            conn.getInputStream(), "utf-8"
                    ));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();
                    Log.i("tag", "결과문자열: " + sb.toString());
                    response = new JSONObject(sb.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}


