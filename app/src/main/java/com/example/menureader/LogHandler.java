package com.example.menureader;

import android.util.Log;

public class LogHandler {
    public static final boolean debug = true;
    public static void m(String message) {
        m(message, null);
    }
    public static void m(String message, Throwable e) {
        if (debug) {
//            String caller = new Throwable().getStackTrace()[2].getClassName();
            StackTraceElement frame = new Throwable().getStackTrace()[2];
            String className = frame.getClassName();
            String methodName = frame.getMethodName();
            String shortName = className.substring(className.lastIndexOf('.') + 1);
            if (e == null) {
                Log.v("ebm_" + shortName, message + " | " + methodName);
            } else {
                Log.v("ebm_" + shortName, message + " | " + methodName, e);
            }
        }
    }
}
