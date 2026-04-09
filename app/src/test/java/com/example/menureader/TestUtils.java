package com.example.menureader;

import android.graphics.Bitmap;
import com.example.menureader.Handling.Controller;
import com.example.menureader.Handling.ImageObject;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class TestUtils {
    private static final AtomicInteger counter = new AtomicInteger(0);
    /**
     * Creates a bitmap of size bytes
     * @param bytes
     */
    public static Bitmap createBitmap(int bytes) {
        /**
         * Each pixel is represented by 4 bytes. For a bitmap to be valid, the number of
         * bytes must be divisible by 4 and greater or equal to 4.
         */
        assert(bytes % 4 == 0 && bytes >= 4);
        Bitmap b = Bitmap.createBitmap(1, bytes / 4, Bitmap.Config.ARGB_8888);
        assertEquals(bytes, b.getByteCount());
        return b;
    }
    public static String createString(int bytes) {
        // Java characters are represented as 2 bytes. Divisible by 2 ensures byte calculations
        // are accurate
        assert(bytes >= Controller.lengthOfStringHeader && bytes % 2 == 0);
        String s = new String(new char[(bytes - Controller.lengthOfStringHeader) / 2]);
        assertEquals(bytes, Controller.getStringSize(s));
        return s;
    }
    public static ImageObject createImageObject(int bytes) {
        assert(bytes > Controller.lengthOfStringHeader + 4);
        return new ImageObject(createString(bytes - 4), createBitmap(4));
    }

    /**
     * Creates unique string object with id
     * @param bytes
     * @param id
     * @return
     */
    public static ImageObject createImageObject(int bytes, String id) {
        assert(bytes >= 4 + Controller.getStringSize(id));
        int availableBytes = bytes - 4;
        char[] def = createString(availableBytes).toCharArray();
        for (int i = 0; i < id.length(); i++) {
            def[i] = id.charAt(i);
        }
        return new ImageObject(new String(def), createBitmap(4));
    }

    /**
     * Creates distinct ImageObjects. Must have sizeBytes big enough to store unique
     * strings
     * @param n
     * @param sizeBytes
     * @return
     */
    public static ImageObject[] createNewImageObjects(int n, int sizeBytes) {
        ImageObject[] ioArr = new ImageObject[n];
        for (int i = 0; i < n; i++) {
            ioArr[i] = TestUtils.createImageObject(sizeBytes, String.valueOf(counter.getAndIncrement()));
        }
        return ioArr;
    }
}
