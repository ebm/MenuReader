package com.example.menureader.Handling;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.menureader.LogHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Persistence {
    private static final String INDEX_FILE = "cache_urls.json";
    private static final String MENU_INDEX_FILE = "menu_list.json";

    public interface OnMenuLoadedListener {
        void onMenuLoaded(Menu menu);
    }

    public static void load(LocalCache cache, AppCompatActivity activity) {
        LogHandler.m("Load attempted");
        File indexFile = new File(activity.getFilesDir(), INDEX_FILE);
        if (!indexFile.exists()) {
            LogHandler.m(INDEX_FILE + " does not exist");
            return;
        }
        try {
            String json = readFile(indexFile);
            if (json.isEmpty()) return;
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
                            cache.addToList(query, imageObject);
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
        LogHandler.m("Save attempted");
        if (cache.getSize() == 0) {
            LogHandler.m("Save failed. Cache is empty");
            return;
        }
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
            writer.close();
        } catch (Exception e) {
            LogHandler.m("Failed to save cache", e);
        }
    }

    public static void loadMenuList(Context context, OnMenuLoadedListener listener) {
        LogHandler.m("loadMenuList attempted");
        File indexFile = new File(context.getFilesDir(), MENU_INDEX_FILE);
        if (!indexFile.exists()) {
            LogHandler.m(MENU_INDEX_FILE + " does not exist");
            return;
        }
        Handler mainHandler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                String json = readFile(indexFile);
                if (json.isEmpty()) return;
                JSONArray menuArray = new JSONArray(json);
                for (int i = 0; i < menuArray.length(); i++) {
                    JSONObject obj = menuArray.getJSONObject(i);
                    String bitmapFile = obj.getString("bitmap");
                    long createdAt = obj.getLong("createdAt");
                    String text = obj.optString("text", "");
                    String name = obj.optString("name", null);
                    List<MenuLine> lines = parseMenuLines(obj.getJSONArray("lines"));

                    File bitmapPath = new File(context.getFilesDir(), bitmapFile);
                    Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath.getAbsolutePath());
                    if (bitmap == null) {
                        LogHandler.m("Failed to decode bitmap: " + bitmapFile);
                        continue;
                    }

                    Menu menu = new Menu(text, bitmap, lines, name, createdAt);
                    mainHandler.post(() -> listener.onMenuLoaded(menu));
                }
            } catch (Exception e) {
                LogHandler.m("Failed to load menu list", e);
            }
        }).start();
    }

    public static void saveMenuList(List<Menu> menuList, Context context) {
        LogHandler.m("saveMenuList attempted");
        if (menuList.isEmpty()) {
            LogHandler.m("saveMenuList: list is empty, nothing to save");
            return;
        }
        try {
            JSONArray menuArray = new JSONArray();
            for (Menu menu : menuList) {
                String bitmapFile = menu.getCreatedAt() + ".jpg";
                File bitmapPath = new File(context.getFilesDir(), bitmapFile);
                boolean bitmapSaved = writeBitmap(menu.getImageBitmap(), bitmapPath);
                if (!bitmapSaved) {
                    LogHandler.m("Skipping menu entry — bitmap write failed: " + bitmapFile);
                    continue;
                }

                JSONArray linesArray = new JSONArray();
                for (MenuLine ml : menu.getMenuList()) {
                    JSONObject lineObj = new JSONObject();
                    lineObj.put("text", ml.getText());
                    if (ml.getLineBounds() != null) {
                        lineObj.put("left", ml.getLineBounds().left);
                        lineObj.put("top", ml.getLineBounds().top);
                        lineObj.put("right", ml.getLineBounds().right);
                        lineObj.put("bottom", ml.getLineBounds().bottom);
                    }
                    linesArray.put(lineObj);
                }

                JSONObject obj = new JSONObject();
                obj.put("bitmap", bitmapFile);
                obj.put("createdAt", menu.getCreatedAt());
                obj.put("text", menu.getText() != null ? menu.getText() : "");
                if (menu.getName() != null) obj.put("name", menu.getName());
                obj.put("lines", linesArray);
                menuArray.put(obj);
            }

            File indexFile = new File(context.getFilesDir(), MENU_INDEX_FILE);
            FileWriter writer = new FileWriter(indexFile);
            writer.write(menuArray.toString());
            writer.close();
        } catch (Exception e) {
            LogHandler.m("Failed to save menu list", e);
        }
    }

    private static List<MenuLine> parseMenuLines(JSONArray linesArray) throws Exception {
        List<MenuLine> lines = new ArrayList<>();
        for (int i = 0; i < linesArray.length(); i++) {
            JSONObject lineObj = linesArray.getJSONObject(i);
            String text = lineObj.getString("text");
            android.graphics.Rect rect = null;
            if (lineObj.has("left")) {
                rect = new android.graphics.Rect(
                        lineObj.getInt("left"),
                        lineObj.getInt("top"),
                        lineObj.getInt("right"),
                        lineObj.getInt("bottom")
                );
            }
            lines.add(new MenuLine(text, rect));
        }
        return lines;
    }

    private static boolean writeBitmap(Bitmap bitmap, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            return true;
        } catch (Exception e) {
            LogHandler.m("Failed to write bitmap to " + file.getName(), e);
            return false;
        }
    }

    private static String readFile(File file) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
