package com.example.menureader;

import com.example.menureader.Handling.ImageObject;
import com.example.menureader.Handling.ImageObjectList;
import com.example.menureader.Handling.LocalCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class ImageObjectListTest {
    private ImageObjectList iol;
    private ImageObject[] ioArr;
    private LocalCache cache;
    private static final int SIZE_BYTES = 100;

    @Before
    public void setUp() {
        cache = new LocalCache();
        iol = new ImageObjectList(cache);
        ioArr = TestUtils.createNewImageObjects(3, SIZE_BYTES);
    }

    @Test
    public void testNewListIsEmpty() {
        assertEquals(0, iol.sizeBytes());
        assertEquals(0, iol.size());
    }

    @Test
    public void testContainsReturnsFalseForItemNeverAdded() {
        assertFalse(iol.contains(ioArr[0]));
    }

    @Test
    public void testAddSingleItem() {
        iol.add(ioArr[0]);
        assertEquals(SIZE_BYTES, iol.sizeBytes());
        assertEquals(1, iol.size());
        assertTrue(iol.contains(ioArr[0]));
    }

    @Test
    public void testAddMultipleItems() {
        iol.add(ioArr[0]);
        iol.add(ioArr[1]);
        iol.add(ioArr[2]);
        assertEquals(SIZE_BYTES * 3, iol.sizeBytes());
        assertEquals(3, iol.size());
        assertTrue(iol.contains(ioArr[0]));
        assertTrue(iol.contains(ioArr[1]));
        assertTrue(iol.contains(ioArr[2]));
    }

    @Test
    public void testRemoveSingleItem() {
        iol.add(ioArr[0]);
        iol.remove(ioArr[0]);
        assertEquals(0, iol.sizeBytes());
        assertEquals(0, iol.size());
        assertFalse(iol.contains(ioArr[0]));
    }

    @Test
    public void testRemoveMiddleItem() {
        iol.add(ioArr[0]);
        iol.add(ioArr[1]);
        iol.add(ioArr[2]);
        iol.remove(ioArr[1]);
        assertEquals(SIZE_BYTES * 2, iol.sizeBytes());
        assertTrue(iol.contains(ioArr[0]));
        assertFalse(iol.contains(ioArr[1]));
        assertTrue(iol.contains(ioArr[2]));
    }

    @Test
    public void testRemoveAllItems() {
        iol.add(ioArr[0]);
        iol.add(ioArr[1]);
        iol.add(ioArr[2]);
        iol.remove(ioArr[0]);
        iol.remove(ioArr[1]);
        iol.remove(ioArr[2]);
        assertEquals(0, iol.sizeBytes());
        assertEquals(0, iol.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDuplicate() {
        iol.add(ioArr[0]);
        iol.add(ioArr[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNonexistent() {
        iol.remove(ioArr[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullIsIgnored() {
        iol.add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNull() {
        iol.remove(null);
    }

    @Test
    public void testAddUpdatesCacheSize() {
        iol.add(ioArr[0]);
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
    }

    @Test
    public void testRemoveUpdatesCacheSize() {
        iol.add(ioArr[0]);
        iol.remove(ioArr[0]);
        assertEquals(0, cache.getCurrSizeBytes());
    }
}
