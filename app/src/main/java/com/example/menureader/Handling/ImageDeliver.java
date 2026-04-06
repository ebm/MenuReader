package com.example.menureader.Handling;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.example.menureader.BuildConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ImageDeliver {
    private static final String ACCESS_KEY = BuildConfig.UNSPLASH_KEY;
    private static final String URL_STRING = "https://api.unsplash.com/search/photos?query=";
    public interface OnImageResultListener {
        void onImageSuccess(Bitmap bitmap);
        void onImageError(Exception e);
    }

    /**
     * Creates a new thread and gets creates a bitmap from imageURL
     *
     * @param imageURL
     * @param activity current activity
     * @param listener
     */
    public static void getBitmapFromUrlThread(String imageURL, Activity activity, OnImageResultListener listener) {
        new Thread(() -> {
            try {
                URL url = new URL(imageURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream input = conn.getInputStream();
                Bitmap bitmap =  BitmapFactory.decodeStream(input);

                activity.runOnUiThread(() -> listener.onImageSuccess(bitmap));
            } catch (Exception e) {
                activity.runOnUiThread(() -> listener.onImageError(e));
            }
        });
    }

    /**
     * Uses an existing thread to get a bitmap from imageURL
     * @param imageURL
     * @return a bitmap
     * @throws Exception outer thread must handle exception
     */
    public static Bitmap getBitmapFromUrlNoThread(String imageURL) throws Exception{
        URL url = new URL(imageURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.connect();
        InputStream input = conn.getInputStream();
        return BitmapFactory.decodeStream(input);
    }

    /**
     * Searches for food a food image on an image server given a query
     * @param food query
     * @param activity activity to get UI thread
     * @param listener
     */
    public static void searchFood(String food, Activity activity, OnImageResultListener listener) {
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

                    Bitmap bitmap = getBitmapFromUrlNoThread(imageURL);
                    activity.runOnUiThread((() -> listener.onImageSuccess(bitmap)));
                } else {
                    activity.runOnUiThread(() -> listener.onImageError(new Exception("Error loading image.")));
                }
            } catch (Exception e) {
                listener.onImageError(e);
            }
        }).start();
    }
}
