package com.example.menureader;

import android.graphics.Bitmap;
import com.example.menureader.Handling.ImageObject;
import com.example.menureader.Handling.LocalCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class LocalCacheTest {


    @Test
    public void testAddingImageObjectToCache() {
        LocalCache cache = new LocalCache(1000); // 1000 bytes;

        ImageObject[] ioArr = TestUtils.createNewImageObjects(11, 100);



    }
}
