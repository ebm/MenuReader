package com.example.menureader;

import android.graphics.Bitmap;
import com.example.menureader.Handling.ImageObject;
import com.example.menureader.Handling.ImageObjectList;
import com.example.menureader.Handling.LocalCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class LocalCacheTest {
    private LocalCache cache;
    private ImageObjectList[] iolArr;
    private ImageObject[] ioArr;
    private String query;
    private final int SIZE_BYTES = 100;

    @Before
    public void setup() {
        cache = new LocalCache(1000);
        query = "placeholder";
        ioArr = TestUtils.createNewImageObjects(20, SIZE_BYTES);
        iolArr = new ImageObjectList[3];
    }

    @Test
    public void testNewCacheEmpty() {
        assertEquals(0, cache.getCurrSizeBytes());
    }
    @Test
    public void testGetReturnsNullForEmptyCache() {
        assertNull(cache.get(ioArr[0].getImageURL()));
    }
    @Test
    public void testAddSingleImageObjectToCache() {
        iolArr[0] = new ImageObjectList(query, cache);
        cache.put(query, iolArr[0]);
        assertEquals(0, cache.getCurrSizeBytes());
        iolArr[0].add(ioArr[0]);
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
        assertNotNull(cache.get(query));
    }
    @Test
    public void testAddMultipleImageObjectsToCache() {
        int index = 0;
        int totalSize = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            assertEquals(totalSize, cache.getCurrSizeBytes());
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            totalSize += SIZE_BYTES * 2;
            assertEquals(totalSize, cache.getCurrSizeBytes());
        }
        for (int i = 0; i < 3; i++) {
            assertEquals(iolArr[i], cache.get(iolArr[i].getQuery()));
        }
    }
    @Test
    public void testAddRemoveImageObjectToCache() {
        iolArr[0] = new ImageObjectList(query, cache);
        cache.put(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);
        cache.remove(query);
        assertNull(cache.get(query));
        assertEquals(0, cache.getCurrSizeBytes());
    }
    @Test
    public void testRemoveMultipleImageObjectsFromCache() {
        int index = 0;
        int totalSize = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(query + i, iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            totalSize += SIZE_BYTES * 2;
        }
        for (int i = 0; i < 3; i++) {
            cache.remove(iolArr[i].getQuery());
            totalSize -= iolArr[i].sizeBytes();
            assertNull(cache.get(iolArr[i].getQuery()));
            assertEquals(totalSize, cache.getCurrSizeBytes());
        }
    }
    @Test
    public void testEvictionFromCache() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        assertNull(cache.get(iolArr[0].getQuery()));
        assertEquals(iolArr[1], cache.get(iolArr[1].getQuery()));
        assertEquals(iolArr[2], cache.get(iolArr[2].getQuery()));
    }
    @Test
    public void testGetMovesToFrontPreventsEviction() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        // 900 bytes used. Access iolArr[0] to make it recent
        cache.get(iolArr[0].getQuery());

        // Add 200 more bytes to iolArr[2], pushing total over 1000
        // Should evict iolArr[1] (least recently used), not iolArr[0]
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);

        assertNotNull(cache.get(iolArr[0].getQuery()));
        assertNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
    }

    @Test
    public void testAddToListMovesToFrontPreventsEviction() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        // 900 bytes. Adding to iolArr[0] makes it recent
        iolArr[0].add(ioArr[index++]);

        // Add more to iolArr[2] to force eviction
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);

        assertNotNull(cache.get(iolArr[0].getQuery()));
        assertNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
    }

    @Test
    public void testEvictionRemovesLeastRecentlyUsed() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        // 900 bytes. No access to any — iolArr[0] is oldest
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);

        assertNull(cache.get(iolArr[0].getQuery()));
        assertNotNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
    }

    @Test
    public void testEvictionClearsEnoughSpace() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        // 900 bytes. Add 500 more — need to evict both iolArr[0] and iolArr[1]
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);

        assertNull(cache.get(iolArr[0].getQuery()));
        assertNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
        assertTrue(cache.getCurrSizeBytes() <= 1000);
    }

    @Test
    public void testPutExistingKeyUpdatesValue() {
        iolArr[0] = new ImageObjectList(query, cache);
        cache.put(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);

        iolArr[1] = new ImageObjectList(query, cache);
        cache.put(query, iolArr[1]);
        iolArr[1].add(ioArr[1]);

        assertEquals(iolArr[1], cache.get(query));
    }

    @Test
    public void testCacheSizeNeverExceedsMax() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            for (int j = 0; j < 4; j++) {
                iolArr[i].add(ioArr[index++]);
                assertTrue(cache.getCurrSizeBytes() <= 1000);
            }
        }
    }

    @Test
    public void testGetNonexistentKeyReturnsNull() {
        assertNull(cache.get("doesNotExist"));
    }

    @Test
    public void testEvictionOrderAfterMultipleGets() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        // 900 bytes. Access in order: 0, 2 — makes 1 the least recent
        cache.get(iolArr[0].getQuery());
        cache.get(iolArr[2].getQuery());

        // Force eviction
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);

        assertNotNull(cache.get(iolArr[0].getQuery()));
        assertNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
    }
}
