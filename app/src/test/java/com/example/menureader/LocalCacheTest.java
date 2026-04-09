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
        assertNull(cache.get("Does not exist"));
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
    @Test(expected = IllegalArgumentException.class)
    public void testPutDuplicateKeyThrows() {
        iolArr[0] = new ImageObjectList(query, cache);
        cache.put(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);

        iolArr[1] = new ImageObjectList(query, cache);

        cache.put(query, iolArr[1]);
    }

    @Test
    public void testPutOrGetReturnedListIsUsable() {
        iolArr[0] = new ImageObjectList(query, cache);
        cache.put(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);
        iolArr[0].add(ioArr[1]);
        assertEquals(SIZE_BYTES * 2, cache.getCurrSizeBytes());

        cache.remove(query);
        assertEquals(0, cache.getCurrSizeBytes());

        iolArr[1] = new ImageObjectList(query, cache);
        cache.put(query, iolArr[1]);
        iolArr[1].add(ioArr[2]);

        assertEquals(iolArr[1], cache.get(query));
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
        assertEquals(1, cache.getSize());
    }

    @Test
    public void testRemoveUnlinksFromLinkedList() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        // 900 bytes total, 300 each

        cache.remove(iolArr[1].getQuery());
        // 600 bytes: iolArr[0]=300, iolArr[2]=300

        // Add 500 bytes to iolArr[2] to force eviction of iolArr[0]
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        // Now: iolArr[0]=300 + iolArr[2]=800 = 1100 > 1000, evicts iolArr[0]

        assertNull(cache.get(iolArr[0].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
        assertEquals(1, cache.getSize());
    }

    @Test
    public void testMiddleNodeRemovalPreservesBacklinks() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        // Order in list: 0 -> 1 -> 2

        cache.get(iolArr[1].getQuery());
        // Order should now be: 0 -> 2 -> 1

        cache.get(iolArr[2].getQuery());
        // Order should now be: 0 -> 1 -> 2

        // Force eviction — should evict iolArr[0] first, then iolArr[1]
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);

        assertNull(cache.get(iolArr[0].getQuery()));
        assertNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
    }

    @Test
    public void testEvictionThenInsertionKeepsListConsistent() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        // 900 bytes. Force eviction of iolArr[0]
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);

        // Add a brand new entry — should work without crashing
        ImageObjectList newList = new ImageObjectList("newquery", cache);
        cache.put("newquery", newList);
        newList.add(ioArr[index++]);

        assertNotNull(cache.get("newquery"));
        assertNull(cache.get(iolArr[0].getQuery()));
        int expectedSize = iolArr[1].sizeBytes() + iolArr[2].sizeBytes() + newList.sizeBytes();
        assertEquals(expectedSize, cache.getCurrSizeBytes());
    }
    @Test
    public void testRemoveHead() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
        }
        // List: 0 -> 1 -> 2
        cache.remove(iolArr[0].getQuery());

        // Verify head was cleanly removed and rest still works
        assertNull(cache.get(iolArr[0].getQuery()));
        assertNotNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
        assertEquals(SIZE_BYTES * 2, cache.getCurrSizeBytes());
        assertEquals(2, cache.getSize());
    }

    @Test
    public void testRemoveTail() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
        }
        // List: 0 -> 1 -> 2
        cache.remove(iolArr[2].getQuery());

        assertNull(cache.get(iolArr[2].getQuery()));
        assertNotNull(cache.get(iolArr[0].getQuery()));
        assertNotNull(cache.get(iolArr[1].getQuery()));
        assertEquals(SIZE_BYTES * 2, cache.getCurrSizeBytes());
        assertEquals(2, cache.getSize());
    }

    @Test
    public void testRemoveOnlyElement() {
        iolArr[0] = new ImageObjectList(query, cache);
        cache.put(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);

        cache.remove(query);

        assertNull(cache.get(query));
        assertEquals(0, cache.getCurrSizeBytes());
        assertEquals(0, cache.getSize());

        // Verify cache is usable after emptying
        iolArr[1] = new ImageObjectList(query + "1", cache);
        cache.put(iolArr[1].getQuery(), iolArr[1]);
        iolArr[1].add(ioArr[1]);
        assertNotNull(cache.get(iolArr[1].getQuery()));
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
    }

    @Test
    public void testRemoveNonexistentKeyIsNoOp() {
        iolArr[0] = new ImageObjectList(query, cache);
        cache.put(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);

        cache.remove("doesnotexist");

        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
        assertEquals(1, cache.getSize());
        assertNotNull(cache.get(query));
    }

    @Test
    public void testGetOnSingleElementCache() {
        iolArr[0] = new ImageObjectList(query, cache);
        cache.put(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);

        // get() calls removeNode then insertNodeAtTail on the only node
        // This exercises the head==tail edge case
        ImageObjectList result = cache.get(query);

        assertNotNull(result);
        assertEquals(iolArr[0], result);
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
        assertEquals(1, cache.getSize());
    }

    @Test
    public void testEvictionEvictsEntireImageObjectList() {
        // Verify that when a node is evicted, all its bytes are subtracted
        int index = 0;
        iolArr[0] = new ImageObjectList(query + 0, cache);
        cache.put(iolArr[0].getQuery(), iolArr[0]);
        iolArr[0].add(ioArr[index++]);
        iolArr[0].add(ioArr[index++]);
        iolArr[0].add(ioArr[index++]);
        iolArr[0].add(ioArr[index++]);
        iolArr[0].add(ioArr[index++]);
        // iolArr[0] = 500 bytes

        iolArr[1] = new ImageObjectList(query + 1, cache);
        cache.put(iolArr[1].getQuery(), iolArr[1]);
        iolArr[1].add(ioArr[index++]);
        iolArr[1].add(ioArr[index++]);
        iolArr[1].add(ioArr[index++]);
        iolArr[1].add(ioArr[index++]);
        iolArr[1].add(ioArr[index++]);
        iolArr[1].add(ioArr[index++]);
        // iolArr[1] = 600 bytes, total = 1100, evicts iolArr[0] (500)

        assertNull(cache.get(iolArr[0].getQuery()));
        assertNotNull(cache.get(iolArr[1].getQuery()));
        assertEquals(600, cache.getCurrSizeBytes());
        assertEquals(1, cache.getSize());
    }

}
