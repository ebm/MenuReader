package com.example.menureader;

import com.example.menureader.BuildConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ImageDeliver {
    private static final String ACCESS_KEY = BuildConfig.UNSPLASH_KEY;
    private static final String URL_STRING = "http://api.unsplash.com/search/photos?query=";
    public interface OnImageResultListener {
        void onImageURL(String url);
        void onImageError(Exception e);
    }
    public static void searchFood(String food, OnImageResultListener listener) {
        new Thread(() -> {
            try {
                String encoded = URLEncoder.encode(food, "UTF-8");
                URL url = new URL(URL_STRING + encoded + "&per_page=1");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Client-ID " + ACCESS_KEY);
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray res = json.getJSONArray("results");
                if (res.length() > 0) {
                    String imageURL = res.getJSONObject(0).getJSONObject("urls").getString("small");
                    listener.onImageURL(imageURL);
                } else {
                    listener.onImageError(new Exception("No images found"));
                }
            } catch (Exception e) {
                listener.onImageError(e);
            }
        }).start();
    }
}
