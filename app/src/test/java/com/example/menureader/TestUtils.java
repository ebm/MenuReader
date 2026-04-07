package com.example.menureader;

import android.graphics.Bitmap;
import com.example.menureader.Handling.Controller;
import com.example.menureader.Handling.ImageObject;

public class TestUtils {
    /**
     * Creates a bitmap of size bytes
     * @param bytes
     */
    public Bitmap createBitmap(int bytes) {
        /**
         * Each pixel is represented by 4 bytes. For a bitmap to be valid, the number of
         * bytes must be divisible by 4 and greater or equal to 4.
         */
        assert(bytes % 4 == 0 && bytes >= 4);
        return Bitmap.createBitmap(1, bytes / 4, Bitmap.Config.ARGB_8888);
    }
    public String createString(int bytes) {
        // Java characters are represented as 2 bytes. Divisible by 2 ensures byte calculations
        // are accurate
        assert(bytes >= Controller.lengthOfStringHeader && bytes % 2 == 0);
        return new String(new char[bytes / 2 - Controller.lengthOfStringHeader]);
    }
    public ImageObject createImageObject(int bytes) {
        assert(bytes > Controller.lengthOfStringHeader + 4);
        return new ImageObject(createString(bytes - 4), createBitmap(4));
    }
}
