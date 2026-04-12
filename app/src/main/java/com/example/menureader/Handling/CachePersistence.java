package com.example.menureader.Handling;

import android.app.Activity;
import android.content.Context;
import com.example.menureader.LogHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class CachePersistence {
    private static final String INDEX_FILE = "cache_urls.json";

    public static void load(LocalCache cache, Activity activity) {
        File indexFile = new File(activity.getFilesDir(), INDEX_FILE);
        if (!indexFile.exists()) {
            LogHandler.m(INDEX_FILE + " does not exist");
            return;
        }
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(indexFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String json = sb.toString();
            JSONObject index = new JSONObject(json);
            Iterator<String> queries = index.keys();
            while (queries.hasNext()) {
                String query = queries.next();
                JSONArray urls = index.getJSONArray(query);

                ImageObjectList iol = new ImageObjectList(query);
                cache.put(query, iol);
                for (int i = 0; i < urls.length(); i++) {
                    String url = urls.getString(i);
                    new ImageObject(url, activity, new ImageObject.OnImageObjectSuccess() {
                        @Override
                        public void onImageCreation(ImageObject imageObject) {
                            iol.add(imageObject);
                        }

                        @Override
                        public void onImageFailure(Exception e) {
                            LogHandler.m("Failed to load" + url, e);
                        }
                    });
                }
            }
        } catch (Exception e) {
            LogHandler.m("Failed to load cache", e);
        }
    }

    public static void save(LocalCache cache, Context context) {
        File indexFile = new File(context.getFilesDir(), INDEX_FILE);
        try {
            JSONObject index = new JSONObject();
            List<Map.Entry<String, ImageObjectList>> list = cache.listOfCacheObjects();
            for (Map.Entry<String, ImageObjectList> entry : list) {
                JSONArray urls = new JSONArray();
                for (ImageObject io : entry.getValue().getImageObjects()) {
                    urls.put(io.getImageURL());
                }
                index.put(entry.getKey(), urls);
            }
            FileWriter writer = new FileWriter(indexFile);
            writer.write(index.toString());
        } catch (Exception e) {
            LogHandler.m("Failed to save cache", e);
        }
    }
}
