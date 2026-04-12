package com.example.menureader;

import android.graphics.Bitmap;
import com.example.menureader.Handling.ImageObject;
import com.example.menureader.Handling.ImageObjectList;
import com.example.menureader.Handling.LocalCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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

    // ==================== Basic Operations ====================

    @Test
    public void testNewCacheEmpty() {
        assertEquals(0, cache.getCurrSizeBytes());
        assertNull(cache.get("Does not exist"));
    }

    @Test
    public void testPutAndGetSingle() {
        iolArr[0] = new ImageObjectList(query);
        iolArr[0].add(ioArr[0]);
        cache.put(query, iolArr[0]);
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
        assertNotNull(cache.get(query));
    }

    @Test
    public void testPutMultiple() {
        int index = 0;
        int totalSize = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            totalSize += SIZE_BYTES * 2;
            assertEquals(totalSize, cache.getCurrSizeBytes());
        }
        for (int i = 0; i < 3; i++) {
            assertEquals(iolArr[i], cache.get(iolArr[i].getQuery()));
        }
    }

    @Test
    public void testPutUpdatesExistingEntry() {
        iolArr[0] = new ImageObjectList(query);
        iolArr[0].add(ioArr[0]);
        cache.put(query, iolArr[0]);
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());

        iolArr[1] = new ImageObjectList(query);
        iolArr[1].add(ioArr[1]);
        iolArr[1].add(ioArr[2]);
        cache.put(query, iolArr[1]);

        assertEquals(SIZE_BYTES * 2, cache.getCurrSizeBytes());
        assertEquals(1, cache.getSize());
        assertSame(iolArr[1], cache.get(query));
    }

    @Test
    public void testPutSameListAfterModification() {
        iolArr[0] = new ImageObjectList(query);
        iolArr[0].add(ioArr[0]);
        cache.put(query, iolArr[0]);
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());

        iolArr[0].add(ioArr[1]);
        cache.put(query, iolArr[0]);
        assertEquals(SIZE_BYTES * 2, cache.getCurrSizeBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutRejectsNull() {
        cache.put(query, null);
    }

    // ==================== Removal ====================

    @Test
    public void testRemove() {
        iolArr[0] = new ImageObjectList(query);
        iolArr[0].add(ioArr[0]);
        cache.put(query, iolArr[0]);
        cache.remove(query);
        assertNull(cache.get(query));
        assertEquals(0, cache.getCurrSizeBytes());
    }

    @Test
    public void testRemoveMultiple() {
        int index = 0;
        int totalSize = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            cache.put(query + i, iolArr[i]);
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
    public void testRemoveHead() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            iolArr[i].add(ioArr[index++]);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
        }
        cache.remove(iolArr[0].getQuery());
        assertNull(cache.get(iolArr[0].getQuery()));
        assertNotNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
        assertEquals(SIZE_BYTES * 2, cache.getCurrSizeBytes());
    }

    @Test
    public void testRemoveTail() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            iolArr[i].add(ioArr[index++]);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
        }
        cache.remove(iolArr[2].getQuery());
        assertNull(cache.get(iolArr[2].getQuery()));
        assertNotNull(cache.get(iolArr[0].getQuery()));
        assertNotNull(cache.get(iolArr[1].getQuery()));
        assertEquals(SIZE_BYTES * 2, cache.getCurrSizeBytes());
    }

    @Test
    public void testRemoveOnlyElement() {
        iolArr[0] = new ImageObjectList(query);
        iolArr[0].add(ioArr[0]);
        cache.put(query, iolArr[0]);
        cache.remove(query);
        assertEquals(0, cache.getCurrSizeBytes());
        assertEquals(0, cache.getSize());

        iolArr[1] = new ImageObjectList(query + "1");
        iolArr[1].add(ioArr[1]);
        cache.put(iolArr[1].getQuery(), iolArr[1]);
        assertNotNull(cache.get(iolArr[1].getQuery()));
    }

    @Test
    public void testRemoveNonexistentKeyIsNoOp() {
        iolArr[0] = new ImageObjectList(query);
        iolArr[0].add(ioArr[0]);
        cache.put(query, iolArr[0]);
        cache.remove("doesnotexist");
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
        assertEquals(1, cache.getSize());
    }

    @Test
    public void testRemoveThenPutSameKey() {
        iolArr[0] = new ImageObjectList(query);
        iolArr[0].add(ioArr[0]);
        iolArr[0].add(ioArr[1]);
        cache.put(query, iolArr[0]);

        cache.remove(query);
        assertEquals(0, cache.getCurrSizeBytes());

        iolArr[1] = new ImageObjectList(query);
        iolArr[1].add(ioArr[2]);
        cache.put(query, iolArr[1]);

        assertSame(iolArr[1], cache.get(query));
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
        assertEquals(1, cache.getSize());
    }

    // ==================== LRU Promotion ====================

    @Test
    public void testGetPromotesEntry() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
        }
        // 900 bytes. Access iolArr[0] to promote it
        cache.get(iolArr[0].getQuery());

        // Add 200 bytes — should evict iolArr[1], not iolArr[0]
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        cache.put(iolArr[2].getQuery(), iolArr[2]);

        assertNotNull(cache.get(iolArr[0].getQuery()));
        assertNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
    }

    @Test
    public void testPutPromotesExistingEntry() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
        }
        // 900 bytes. Update iolArr[0] to promote it
        iolArr[0].add(ioArr[index++]);
        cache.put(iolArr[0].getQuery(), iolArr[0]);

        // Add more to iolArr[2] to force eviction
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        cache.put(iolArr[2].getQuery(), iolArr[2]);

        assertNotNull(cache.get(iolArr[0].getQuery()));
        assertNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
    }

    @Test
    public void testEvictionOrderAfterMultipleGets() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
        }
        cache.get(iolArr[0].getQuery());
        cache.get(iolArr[2].getQuery());

        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        cache.put(iolArr[2].getQuery(), iolArr[2]);

        assertNotNull(cache.get(iolArr[0].getQuery()));
        assertNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
    }

    @Test
    public void testGetOnSingleElementCache() {
        iolArr[0] = new ImageObjectList(query);
        iolArr[0].add(ioArr[0]);
        cache.put(query, iolArr[0]);

        ImageObjectList result = cache.get(query);
        assertNotNull(result);
        assertSame(iolArr[0], result);
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
        assertEquals(1, cache.getSize());
    }

    // ==================== Eviction ====================

    @Test
    public void testEviction() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
        }
        assertNull(cache.get(iolArr[0].getQuery()));
        assertNotNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
    }

    @Test
    public void testEvictionRemovesLeastRecentlyUsed() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
        }
        // 900 bytes. Add 200 to iolArr[2] and re-put
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        cache.put(iolArr[2].getQuery(), iolArr[2]);

        assertNull(cache.get(iolArr[0].getQuery()));
        assertNotNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
    }

    @Test
    public void testEvictionClearsEnoughSpace() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
        }
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        cache.put(iolArr[2].getQuery(), iolArr[2]);

        assertNull(cache.get(iolArr[0].getQuery()));
        assertNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
        assertTrue(cache.getCurrSizeBytes() <= 1000);
    }

    @Test
    public void testEvictionEvictsEntireImageObjectList() {
        int index = 0;
        iolArr[0] = new ImageObjectList(query + 0);
        for (int i = 0; i < 5; i++) iolArr[0].add(ioArr[index++]);
        cache.put(iolArr[0].getQuery(), iolArr[0]);

        iolArr[1] = new ImageObjectList(query + 1);
        for (int i = 0; i < 6; i++) iolArr[1].add(ioArr[index++]);
        cache.put(iolArr[1].getQuery(), iolArr[1]);

        assertNull(cache.get(iolArr[0].getQuery()));
        assertNotNull(cache.get(iolArr[1].getQuery()));
        assertEquals(600, cache.getCurrSizeBytes());
        assertEquals(1, cache.getSize());
    }

    @Test
    public void testCacheSizeNeverExceedsMax() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            for (int j = 0; j < 4; j++) {
                iolArr[i].add(ioArr[index++]);
            }
            cache.put(iolArr[i].getQuery(), iolArr[i]);
            assertTrue(cache.getCurrSizeBytes() <= 1000);
        }
    }

    // ==================== Linked List Integrity ====================

    @Test
    public void testRemoveUnlinksFromLinkedList() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
        }
        cache.remove(iolArr[1].getQuery());

        for (int i = 0; i < 5; i++) iolArr[2].add(ioArr[index++]);
        cache.put(iolArr[2].getQuery(), iolArr[2]);

        assertNull(cache.get(iolArr[0].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
        assertEquals(1, cache.getSize());
    }

    @Test
    public void testMiddleNodeRemovalPreservesBacklinks() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
        }
        cache.get(iolArr[1].getQuery());
        cache.get(iolArr[2].getQuery());

        for (int i = 0; i < 5; i++) iolArr[2].add(ioArr[index++]);
        cache.put(iolArr[2].getQuery(), iolArr[2]);

        assertNull(cache.get(iolArr[0].getQuery()));
        assertNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
    }

    @Test
    public void testEvictionThenInsertionKeepsListConsistent() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            cache.put(iolArr[i].getQuery(), iolArr[i]);
        }
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        cache.put(iolArr[2].getQuery(), iolArr[2]);

        ImageObjectList newList = new ImageObjectList("newquery");
        newList.add(ioArr[index++]);
        cache.put("newquery", newList);

        assertNotNull(cache.get("newquery"));
        assertNull(cache.get(iolArr[0].getQuery()));
        int expectedSize = iolArr[1].sizeBytes() + iolArr[2].sizeBytes() + newList.sizeBytes();
        assertEquals(expectedSize, cache.getCurrSizeBytes());
    }

    // ==================== Concurrency - Correctness ====================

    @Test
    public void testConcurrentPuts() throws InterruptedException {
        LocalCache cache = new LocalCache(10_000_000);
        int numThreads = 100;
        int imagesPerThread = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        AtomicReference<Throwable> error = new AtomicReference<>(null);

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    String query = "query" + threadId;
                    ImageObjectList iol = new ImageObjectList(query);
                    for (int i = 0; i < imagesPerThread; i++) {
                        String id = "t" + threadId + "_i" + i;
                        ImageObject io = TestUtils.createImageObject(100, id);
                        iol.add(io);
                    }
                    cache.put(query, iol);
                } catch (Throwable e) {
                    error.compareAndSet(null, e);
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);

        assertNull("Threw: " + error.get(), error.get());
        assertEquals(numThreads, cache.getSize());
        assertEquals(numThreads * imagesPerThread * 100, cache.getCurrSizeBytes());
    }

    @Test
    public void testConcurrentReadsAndWrites() throws InterruptedException {
        LocalCache cache = new LocalCache(10_000_000);
        int numQueries = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numQueries * 2);
        AtomicReference<Throwable> error = new AtomicReference<>(null);

        for (int t = 0; t < numQueries; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    String query = "query" + threadId;
                    ImageObjectList iol = new ImageObjectList(query);
                    for (int i = 0; i < 5; i++) {
                        String id = "t" + threadId + "_i" + i;
                        iol.add(TestUtils.createImageObject(100, id));
                    }
                    cache.put(query, iol);
                } catch (Throwable e) {
                    error.compareAndSet(null, e);
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        for (int t = 0; t < numQueries; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < 20; i++) {
                        cache.get("query" + (threadId % numQueries));
                        cache.getSize();
                        cache.getCurrSizeBytes();
                    }
                } catch (Throwable e) {
                    error.compareAndSet(null, e);
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS));
        assertNull("Threw: " + error.get(), error.get());
    }

    // ==================== Concurrency - Stress ====================

    @Test
    public void testConcurrencyStress() throws InterruptedException {
        for (int run = 0; run < 50; run++) {
            LocalCache cache = new LocalCache(5000);
            int numThreads = 20;
            int opsPerThread = 100;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(numThreads);
            AtomicReference<Throwable> error = new AtomicReference<>(null);

            for (int t = 0; t < numThreads; t++) {
                final int threadId = t;
                new Thread(() -> {
                    try {
                        startLatch.await();
                        for (int i = 0; i < opsPerThread; i++) {
                            String query = "q" + threadId + "_" + i;
                            ImageObjectList iol = new ImageObjectList(query);
                            String id = "t" + threadId + "_o" + i;
                            iol.add(TestUtils.createImageObject(100, id));
                            cache.put(query, iol);

                            cache.get("q" + ((threadId + 1) % numThreads) + "_" + i);
                            cache.getSize();
                            cache.getCurrSizeBytes();
                        }
                    } catch (Throwable e) {
                        error.compareAndSet(null, e);
                    } finally {
                        doneLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            assertTrue("Timed out on run " + run, doneLatch.await(10, TimeUnit.SECONDS));
            assertNull("Run " + run + " threw: " + error.get(), error.get());
            assertTrue("Size exceeded capacity on run " + run, cache.getCurrSizeBytes() <= 5000);
            assertTrue("Negative size on run " + run, cache.getCurrSizeBytes() >= 0);
        }
    }

    @Test
    public void testConcurrentEvictionConsistency() throws InterruptedException {
        for (int run = 0; run < 50; run++) {
            LocalCache cache = new LocalCache(1000);
            int numThreads = 10;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(numThreads);
            AtomicReference<Throwable> error = new AtomicReference<>(null);

            for (int t = 0; t < numThreads; t++) {
                final int threadId = t;
                new Thread(() -> {
                    try {
                        startLatch.await();
                        for (int i = 0; i < 20; i++) {
                            String query = "t" + threadId + "_r" + i;
                            ImageObjectList iol = new ImageObjectList(query);
                            for (int j = 0; j < 3; j++) {
                                String id = "t" + threadId + "_r" + i + "_j" + j;
                                iol.add(TestUtils.createImageObject(100, id));
                            }
                            cache.put(query, iol);
                        }
                    } catch (Throwable e) {
                        error.compareAndSet(null, e);
                    } finally {
                        doneLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            assertTrue("Timed out on run " + run, doneLatch.await(10, TimeUnit.SECONDS));
            assertNull("Run " + run + " threw: " + error.get(), error.get());
            assertTrue("Size exceeded capacity on run " + run, cache.getCurrSizeBytes() <= 1000);
            assertTrue("Negative size on run " + run, cache.getCurrSizeBytes() >= 0);
            assertTrue("Size/count mismatch on run " + run, cache.getSize() > 0);
        }
    }
}