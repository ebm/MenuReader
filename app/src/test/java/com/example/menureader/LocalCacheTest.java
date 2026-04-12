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
        iolArr[0] = cache.putOrGet(query, iolArr[0]);
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
            iolArr[i] = cache.putOrGet(iolArr[i].getQuery(), iolArr[i]);
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
        iolArr[0] = cache.putOrGet(query, iolArr[0]);
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
            iolArr[i] = cache.putOrGet(query + i, iolArr[i]);
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
            iolArr[i] = cache.putOrGet(iolArr[i].getQuery(), iolArr[i]);
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
            iolArr[i] = cache.putOrGet(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        cache.get(iolArr[0].getQuery());
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
            iolArr[i] = cache.putOrGet(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        iolArr[0].add(ioArr[index++]);
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
            iolArr[i] = cache.putOrGet(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
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
            iolArr[i] = cache.putOrGet(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
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
            iolArr[i] = cache.putOrGet(iolArr[i].getQuery(), iolArr[i]);
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
            iolArr[i] = cache.putOrGet(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        cache.get(iolArr[0].getQuery());
        cache.get(iolArr[2].getQuery());
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        assertNotNull(cache.get(iolArr[0].getQuery()));
        assertNull(cache.get(iolArr[1].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
    }

    @Test
    public void testPutOrGetReturnsExistingList() {
        iolArr[0] = new ImageObjectList(query, cache);
        iolArr[0] = cache.putOrGet(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);

        // Try to insert a new list with the same key
        iolArr[1] = new ImageObjectList(query, cache);
        ImageObjectList returned = cache.putOrGet(query, iolArr[1]);

        // Should get back the original, not the new one
        assertSame(iolArr[0], returned);
        assertEquals(1, cache.getSize());
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
    }

    @Test
    public void testPutOrGetReturnedListIsUsable() {
        iolArr[0] = new ImageObjectList(query, cache);
        iolArr[0] = cache.putOrGet(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);

        // Simulate race: another thread tries to insert same query
        iolArr[1] = new ImageObjectList(query, cache);
        ImageObjectList returned = cache.putOrGet(query, iolArr[1]);

        // Adding to the returned list should update the cache correctly
        returned.add(ioArr[1]);
        assertEquals(SIZE_BYTES * 2, cache.getCurrSizeBytes());
        assertEquals(1, cache.getSize());
    }

    @Test
    public void testPutOrGetNewKeyReturnsNewList() {
        iolArr[0] = new ImageObjectList(query, cache);
        ImageObjectList returned = cache.putOrGet(query, iolArr[0]);

        assertSame(iolArr[0], returned);
        assertEquals(1, cache.getSize());
    }

    @Test
    public void testPutOrGetCallerUsesReturnValue() {
        // Simulate disk load inserting first
        iolArr[0] = new ImageObjectList(query, cache);
        iolArr[0] = cache.putOrGet(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);
        iolArr[0].add(ioArr[1]);

        // Simulate user query arriving with same key
        iolArr[1] = new ImageObjectList(query, cache);
        iolArr[1] = cache.putOrGet(query, iolArr[1]);

        // iolArr[1] should now be the same object as iolArr[0]
        assertSame(iolArr[0], iolArr[1]);

        // Adding through iolArr[1] updates the cache correctly
        iolArr[1].add(ioArr[2]);
        assertEquals(SIZE_BYTES * 3, cache.getCurrSizeBytes());
        assertSame(cache.get(query), iolArr[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutOrGetRejectsNullVal() {
        cache.putOrGet(query, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutOrGetRejectsNonEmptyVal() {
        // Insert a list and add an image to make it non-empty
        iolArr[0] = new ImageObjectList(query, cache);
        iolArr[0] = cache.putOrGet(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);

        // Remove from cache but keep the reference
        cache.remove(query);

        // Try to re-insert the non-empty list — should reject
        cache.putOrGet(query, iolArr[0]);
    }

    @Test
    public void testRemoveThenPutOrGetSameKey() {
        iolArr[0] = new ImageObjectList(query, cache);
        iolArr[0] = cache.putOrGet(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);
        iolArr[0].add(ioArr[1]);
        assertEquals(SIZE_BYTES * 2, cache.getCurrSizeBytes());

        cache.remove(query);
        assertEquals(0, cache.getCurrSizeBytes());

        iolArr[1] = new ImageObjectList(query, cache);
        iolArr[1] = cache.putOrGet(query, iolArr[1]);
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
            iolArr[i] = cache.putOrGet(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        cache.remove(iolArr[1].getQuery());
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        assertNull(cache.get(iolArr[0].getQuery()));
        assertNotNull(cache.get(iolArr[2].getQuery()));
        assertEquals(1, cache.getSize());
    }

    @Test
    public void testMiddleNodeRemovalPreservesBacklinks() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            iolArr[i] = new ImageObjectList(query + i, cache);
            iolArr[i] = cache.putOrGet(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        cache.get(iolArr[1].getQuery());
        cache.get(iolArr[2].getQuery());
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
            iolArr[i] = cache.putOrGet(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
            iolArr[i].add(ioArr[index++]);
        }
        iolArr[2].add(ioArr[index++]);
        iolArr[2].add(ioArr[index++]);
        ImageObjectList newList = new ImageObjectList("newquery", cache);
        newList = cache.putOrGet("newquery", newList);
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
            iolArr[i] = cache.putOrGet(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
        }
        cache.remove(iolArr[0].getQuery());
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
            iolArr[i] = cache.putOrGet(iolArr[i].getQuery(), iolArr[i]);
            iolArr[i].add(ioArr[index++]);
        }
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
        iolArr[0] = cache.putOrGet(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);
        cache.remove(query);
        assertNull(cache.get(query));
        assertEquals(0, cache.getCurrSizeBytes());
        assertEquals(0, cache.getSize());
        iolArr[1] = new ImageObjectList(query + "1", cache);
        iolArr[1] = cache.putOrGet(iolArr[1].getQuery(), iolArr[1]);
        iolArr[1].add(ioArr[1]);
        assertNotNull(cache.get(iolArr[1].getQuery()));
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
    }

    @Test
    public void testRemoveNonexistentKeyIsNoOp() {
        iolArr[0] = new ImageObjectList(query, cache);
        iolArr[0] = cache.putOrGet(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);
        cache.remove("doesnotexist");
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
        assertEquals(1, cache.getSize());
        assertNotNull(cache.get(query));
    }

    @Test
    public void testGetOnSingleElementCache() {
        iolArr[0] = new ImageObjectList(query, cache);
        iolArr[0] = cache.putOrGet(query, iolArr[0]);
        iolArr[0].add(ioArr[0]);
        ImageObjectList result = cache.get(query);
        assertNotNull(result);
        assertEquals(iolArr[0], result);
        assertEquals(SIZE_BYTES, cache.getCurrSizeBytes());
        assertEquals(1, cache.getSize());
    }

    @Test
    public void testEvictionEvictsEntireImageObjectList() {
        int index = 0;
        iolArr[0] = new ImageObjectList(query + 0, cache);
        iolArr[0] = cache.putOrGet(iolArr[0].getQuery(), iolArr[0]);
        iolArr[0].add(ioArr[index++]);
        iolArr[0].add(ioArr[index++]);
        iolArr[0].add(ioArr[index++]);
        iolArr[0].add(ioArr[index++]);
        iolArr[0].add(ioArr[index++]);
        iolArr[1] = new ImageObjectList(query + 1, cache);
        iolArr[1] = cache.putOrGet(iolArr[1].getQuery(), iolArr[1]);
        iolArr[1].add(ioArr[index++]);
        iolArr[1].add(ioArr[index++]);
        iolArr[1].add(ioArr[index++]);
        iolArr[1].add(ioArr[index++]);
        iolArr[1].add(ioArr[index++]);
        iolArr[1].add(ioArr[index++]);
        assertNull(cache.get(iolArr[0].getQuery()));
        assertNotNull(cache.get(iolArr[1].getQuery()));
        assertEquals(600, cache.getCurrSizeBytes());
        assertEquals(1, cache.getSize());
    }
    @Test
    public void testConcurrentAdds() throws InterruptedException {
        LocalCache cache = new LocalCache(10_000_000);
        int numThreads = 100;
        int imagesPerThread = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await(); // all threads start at once
                    String query = "query" + threadId;
                    ImageObjectList iol = new ImageObjectList(query, cache);
                    iol = cache.putOrGet(query, iol);
                    for (int i = 0; i < imagesPerThread; i++) {
                        ImageObject io = TestUtils.createNewImageObjects(1, 100)[0];
                        iol.add(io);
                    }
                } catch (Exception e) {
                    fail("Thread " + threadId + " threw: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // release all threads
        doneLatch.await(5, TimeUnit.SECONDS);

        assertEquals(numThreads, cache.getSize());
        assertEquals(numThreads * imagesPerThread * 100, cache.getCurrSizeBytes());
    }

    @Test
    public void testConcurrentReadsAndWrites() throws InterruptedException {
        LocalCache cache = new LocalCache(10_000_000);
        int numQueries = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numQueries * 2);

        // Writer threads
        for (int t = 0; t < numQueries; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    String query = "query" + threadId;
                    ImageObjectList iol = new ImageObjectList(query, cache);
                    iol = cache.putOrGet(query, iol);
                    for (int i = 0; i < 5; i++) {
                        ImageObject io = TestUtils.createNewImageObjects(1, 100)[0];
                        iol.add(io);
                    }
                } catch (Exception e) {
                    fail("Writer " + threadId + " threw: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        // Reader threads
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
                } catch (Exception e) {
                    fail("Reader " + threadId + " threw: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testConcurrentPutOrGetSameKey() throws InterruptedException {
        LocalCache cache = new LocalCache(10_000_000);
        int numThreads = 100;
        String query = "shared_query";
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        ImageObjectList[] results = new ImageObjectList[numThreads];

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    ImageObjectList iol = new ImageObjectList(query, cache);
                    results[threadId] = cache.putOrGet(query, iol);
                } catch (Exception e) {
                    fail("Thread " + threadId + " threw: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);

        // All threads should have gotten back the same instance
        assertEquals(1, cache.getSize());
        for (int i = 1; i < numThreads; i++) {
            assertSame(results[0], results[i]);
        }
    }
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
                        // In testConcurrencyStress
                        for (int i = 0; i < opsPerThread; i++) {
                            String query = "q" + threadId + "_" + i;
                            ImageObjectList iol = new ImageObjectList(query, cache);
                            iol = cache.putOrGet(query, iol);
                            String id = "t" + threadId + "_o" + i;
                            ImageObject io = TestUtils.createImageObject(100, id);
                            iol.add(io);

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

            // Size accounting must be consistent
            assertTrue("Size exceeded capacity on run " + run,
                    cache.getCurrSizeBytes() <= 5000);
            assertTrue("Negative size on run " + run,
                    cache.getCurrSizeBytes() >= 0);
        }
    }

    @Test
    public void testConcurrentAddToSameList() throws InterruptedException {
        for (int run = 0; run < 100; run++) {
            LocalCache cache = new LocalCache(10_000_000);
            String query = "shared";
            ImageObjectList iol = new ImageObjectList(query, cache);
            iol = cache.putOrGet(query, iol);
            final ImageObjectList sharedIol = iol;

            int numThreads = 20;
            int addsPerThread = 50;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(numThreads);
            AtomicReference<Throwable> error = new AtomicReference<>(null);

            for (int t = 0; t < numThreads; t++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        // In testConcurrentAddToSameList
                        for (int i = 0; i < addsPerThread; i++) {
                            String id = "t" + Thread.currentThread().getId() + "_i" + i;
                            ImageObject io = TestUtils.createImageObject(100, id);
                            sharedIol.add(io);
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

            int expectedSize = numThreads * addsPerThread;
            int expectedBytes = expectedSize * 100;
            assertEquals("Wrong count on run " + run, expectedSize, sharedIol.size());
            assertEquals("Wrong list bytes on run " + run, expectedBytes, sharedIol.sizeBytes());
            assertEquals("Wrong cache bytes on run " + run, expectedBytes, cache.getCurrSizeBytes());
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
                            ImageObjectList iol = new ImageObjectList(query, cache);
                            iol = cache.putOrGet(query, iol);
                            // Add multiple images to force eviction
                            for (int j = 0; j < 3; j++) {
                                ImageObject io = TestUtils.createImageObject(100);
                                iol.add(io);
                            }
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

            assertTrue("Size exceeded capacity on run " + run,
                    cache.getCurrSizeBytes() <= 1000);
            assertTrue("Negative size on run " + run,
                    cache.getCurrSizeBytes() >= 0);
            assertTrue("Size/count mismatch on run " + run,
                    cache.getSize() > 0);
        }
    }
}
