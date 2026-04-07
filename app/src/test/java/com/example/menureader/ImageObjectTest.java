package com.example.menureader;

import android.graphics.Bitmap;
import com.example.menureader.Handling.Controller;
import com.example.menureader.Handling.ImageObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class ImageObjectTest {
    @Test
    public void testImageObjectSize() {
        int bytes = 100;
        String s = TestUtils.createString(bytes - 4);
        Bitmap b = TestUtils.createBitmap(4);
        assertEquals(bytes, Controller.getStringSize(s) + b.getByteCount());
        ImageObject io = new ImageObject(s, b);
        assertEquals(bytes, io.getSizeBytes());
    }
}
