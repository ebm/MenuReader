package com.example.menureader;

import android.view.View;
import android.view.ViewGroup;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Controller {
    public static void applyOffset(View v) {
        ViewCompat.setOnApplyWindowInsetsListener(v, (view, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            // Push content below status bar
            view.setPadding(0, statusBarHeight, 0, 0);

            // Push FAB above nav bar
//            View fab = view.findViewById(R.id.fabCamera);
//            if (fab != null) {
//                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
//                params.bottomMargin = navBarHeight + 24;
//                fab.setLayoutParams(params);
//            }
            view.setPadding(0, statusBarHeight, 0, navBarHeight + 24);

            return insets;
        });
    }
}
