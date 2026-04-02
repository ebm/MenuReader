package com.example.menureader;

import android.util.Log;
import com.google.firebase.components.BuildConfig;

public class LogHandler {
    public static void m(String message) {
        if (BuildConfig.DEBUG) Log.d("Misc", message);
    }
    public static void m(String tag, String message) {
        if (BuildConfig.DEBUG) Log.d(tag, message);
    }
    public static void m(String tag, String message, Throwable e) {
        if (BuildConfig.DEBUG) Log.d(tag, message, e);
    }
}
