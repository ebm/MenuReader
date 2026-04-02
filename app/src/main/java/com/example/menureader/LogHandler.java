package com.example.menureader;

import android.util.Log;

public class LogHandler {
    private static final boolean debug = true;
    public static void m(String message) {
        if (debug) Log.v("ebm", message);
    }
    public static void m(String message, Throwable e) {
        if (debug) {
            if (e == null) {
                Log.v("ebm", message);
            } else {
                Log.v("ebm", message, e);
            }
        }
    }
}
