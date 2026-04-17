package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageDeliver {
    private final String ACCESS_KEY = "";
    private final String URL_STRING = "https://api.unsplash.com/search/photos?query=";

    public interface OnQueryResultListener {
        void onQuerySuccess(JSONArray ja);

        void onQueryError(Exception e);
    }

    public final String query;
    public final int totalImageCount;
    public final OnQueryResultListener oqrl;

    public ImageDeliver(String query, int totalImageCount, OnQueryResultListener oqrl) {
        this.query = query;
        this.totalImageCount = totalImageCount;
        this.oqrl = oqrl;
    }

    public void searchCache() {

    }

    public void queryUnsplash() {
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
                oqrl.onQuerySuccess(res);
            } catch (Exception e) {
                e.printStackTrace();
                oqrl.onQueryError(e);
            }
        }).start();
    }
}
