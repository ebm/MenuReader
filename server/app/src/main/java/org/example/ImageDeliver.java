package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

public class ImageDeliver {
    private static final String ACCESS_KEY = System.getenv("UNSPLASH_KEY");
    private static final String URL_STRING = "https://api.unsplash.com/search/photos?query=";
    private static final Cache cache = new Cache();

    public static List<String> search(String query, int totalImageCount) {
        System.out.println("Searching for query: " + query + ", " + totalImageCount);
        Set<String> cached = cache.get(query);
        System.out.println("Cached size: " + cached.size());
        if (cached.size() < totalImageCount) {
            System.out.println("Cache miss");
            return queryUnsplash(query, totalImageCount);
        }
        System.out.println("Cache hit");
        List<String> res = new ArrayList<>();
        int count = 0;
        for (String s : cached) {
            if (count++ == totalImageCount) {
                break;
            }
            res.add(s);
        }
        return res;
    }

    public static List<String> queryUnsplash(String query, int totalImageCount) {
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
            JSONArray resArr = json.getJSONArray("results");

            List<String> res = new ArrayList<>();
            for (int i = 0; i < resArr.length(); i++) {
                String urlString = resArr.getJSONObject(i).getJSONObject("urls").getString("small");
                res.add(urlString);
            }
            cache.add(query, res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
