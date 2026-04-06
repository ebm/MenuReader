package com.example.menureader.Handling;

import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Controller {
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
}
