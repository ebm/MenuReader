package com.example.menureader.Handling;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.menureader.BuildConfig;
import com.example.menureader.Front.SharedViewModel;
import com.example.menureader.LogHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageDeliver {
    private final String ACCESS_KEY = BuildConfig.UNSPLASH_KEY;
    private final String URL_STRING = "https://api.unsplash.com/search/photos?query=";

    public interface OnImageResultListener {
        void onImageSuccess(Bitmap bitmap);

        void onImageError(Exception e);
    }

    public final int totalImageCount = 3;
    public final AtomicInteger imageFound = new AtomicInteger(0);
    public final AtomicInteger imageFailed = new AtomicInteger(0);
    private final String query;
    private final FragmentActivity activity;
    private final OnImageResultListener listener;
    private ImageObjectList iol;
    private final LocalCache cache;

    public ImageDeliver(String query, FragmentActivity activity, OnImageResultListener listener) {
        this.query = query;
        this.activity = activity;
        this.listener = listener;

        SharedViewModel svm = new ViewModelProvider(activity).get(SharedViewModel.class);
        cache = svm.getCache();

        if (!cacheFound()) {
            LogHandler.m("Cache miss");
            searchFood();
        }
    }

    public boolean cacheFound() {
        iol = cache.get(query);
        if (iol == null) return false;
        LogHandler.m("Cache Hit!");
        for (ImageObject io : iol.getImageObjects()) {
            listener.onImageSuccess(io.getBitmap());
        }
        return true;
    }

    /**
     * Creates a new thread and gets creates a bitmap from imageURL
     *
     * @param imageURL
     * @param activity current activity
     * @param listener
     * @param thread   whether to use a thread
     */
    public static void getBitmapFromURL(String imageURL, Activity activity,
                                        boolean thread, OnImageResultListener listener) {
        if (!thread) {
            try {
                Bitmap bitmap = getBitmapFromUrlNoThread(imageURL);
                listener.onImageSuccess(bitmap);
            } catch (Exception e) {
                listener.onImageError(e);
            }
            return;
        }
        new Thread(() -> {
            try {
                Bitmap bitmap = getBitmapFromUrlNoThread(imageURL);
                activity.runOnUiThread(() -> listener.onImageSuccess(bitmap));
            } catch (Exception e) {
                activity.runOnUiThread(() -> listener.onImageError(e));
            }
        }).start();
    }

    /**
     * Uses an existing thread to get a bitmap from imageURL
     *
     * @param imageURL
     * @return a bitmap
     * @throws Exception outer thread must handle exception
     */
    public static Bitmap getBitmapFromUrlNoThread(String imageURL) throws Exception {
        URL url = new URL(imageURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.connect();
        InputStream input = conn.getInputStream();
        return BitmapFactory.decodeStream(input);
    }

    /**
     * Searches for food a food image on an image server given a query
     */
    private void searchFood() {
        new Thread(() -> {
            try {
                String encoded = URLEncoder.encode(query, "UTF-8");
                URL url = new URL(URL_STRING + encoded + "&per_page=" + totalImageCount);
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

                handleResults(res);
            } catch (Exception e) {
                listener.onImageError(e);
            }
        }).start();
    }

    private void handleResults(JSONArray res) throws JSONException {
        LogHandler.m("Found " + res.length() + " result(s)");
        if (res.length() > 0) {
            iol = new ImageObjectList(query);
            cache.put(query, iol);
            for (int i = 0; i < res.length(); i++) {
                String imageURL = res.getJSONObject(i).getJSONObject("urls").getString("small");
                LogHandler.m("Image " + i + " trying url=" + imageURL);
                new ImageObject(imageURL, activity, new ImageObject.OnImageObjectSuccess() {
                    @Override
                    public void onImageCreation(ImageObject imageObject) {
                        LogHandler.m("Found image");
                        cache.addToList(query, imageObject);
                        imageFound.incrementAndGet();
                        activity.runOnUiThread(() -> listener.onImageSuccess(imageObject.getBitmap()));
                    }

                    @Override
                    public void onImageFailure(Exception e) {
                        LogHandler.m("Failed to find image", e);
                        int failed = imageFailed.incrementAndGet();
                        if (imageFound.get() + failed == totalImageCount && imageFound.get() == 0) {
                            activity.runOnUiThread(() -> listener.onImageError(new Exception("Error loading image.")));
                        }
                    }
                });
            }
        } else {
            activity.runOnUiThread(() -> listener.onImageError(new Exception("Error loading image.")));
        }
    }
}
