package com.example.menureader;

import android.graphics.Bitmap;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
