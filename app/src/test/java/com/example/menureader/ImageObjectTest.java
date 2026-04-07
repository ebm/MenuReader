package com.example.menureader;

import android.graphics.Bitmap;
import com.example.menureader.Handling.Controller;
import com.example.menureader.Handling.ImageObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

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
    private static final int TOTAL_BYTES = 100;
    private ImageObject io;

    @Before
    public void setUp() {
        io = TestUtils.createImageObject(TOTAL_BYTES, "test1");
    }

    @Test
    public void testConstructorSetsSizeBytes() {
        assertEquals(TOTAL_BYTES, io.getSizeBytes());
    }

    @Test
    public void testSetImageURLUpdatesSizeBytes() {
        String newURL = "newURL";
        int oldURLSize = Controller.getStringSize(io.getImageURL());
        int newURLSize = Controller.getStringSize(newURL);
        int expectedSize = TOTAL_BYTES - oldURLSize + newURLSize;
        io.setImageURL(newURL);
        assertEquals(expectedSize, io.getSizeBytes());
    }

    @Test
    public void testEqualsSameURL() {
        ImageObject other = new ImageObject(io.getImageURL(), TestUtils.createBitmap(8));
        assertEquals(io, other);
    }

    @Test
    public void testEqualsDifferentURL() {
        ImageObject other = TestUtils.createImageObject(TOTAL_BYTES, "test2");
        assertNotEquals(io, other);
    }

    @Test
    public void testEqualsNull() {
        assertNotEquals(null, io);
    }

    @Test
    public void testHashCodeConsistentWithEquals() {
        ImageObject other = new ImageObject(io.getImageURL(), TestUtils.createBitmap(8));
        assertEquals(io.hashCode(), other.hashCode());
    }

}
