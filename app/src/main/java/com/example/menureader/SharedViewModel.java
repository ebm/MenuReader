package com.example.menureader;

import android.graphics.Bitmap;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private Bitmap bitmap;
    private Menu menu;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }
}
