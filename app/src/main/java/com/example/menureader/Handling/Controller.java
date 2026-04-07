package com.example.menureader.Handling;

import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Controller {
    // No easy way to get exact string lengths in Java. A Java string has 38 bytes of
    // overhead. sizeOfHeaderForStringByes is set to 50 bytes for safety.
    public static int lengthOfStringHeader = 50;
    /**
     * Apply padding based off navigation menu and notification bar
     * @param v
     */
    public static void applyOffset(View v) {
        ViewCompat.setOnApplyWindowInsetsListener(v, (view, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            // Push content below status bar
            view.setPadding(0, statusBarHeight, 0, 0);

            view.setPadding(0, statusBarHeight, 0, navBarHeight + 24);

            return insets;
        });
    }
    public static int getStringSize(String s) {
        // Java uses UTF_16 so each character is 2 bytes.
        int stringBytes = s.length() * 2;
        return stringBytes + lengthOfStringHeader;
    }
}
