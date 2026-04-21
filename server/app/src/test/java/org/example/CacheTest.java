package org.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class CacheTest {
    Cache cache;

    @Before
    public void setUp() {
        cache = new Cache();
        cache.removeQuery("test_query");
    }

    @After
    public void tearDown() {
        cache.removeQuery("test_query");
    }

    @Test
    public void testAddAndGet() {
        List<String> urls = List.of("url1", "url2", "url3");
        cache.add("test_query", urls);

        Set<String> result = cache.get("test_query");
        assertEquals(3, result.size());
        assertTrue(result.containsAll(urls));
    }

    @Test
    public void testCacheMissReturnsEmpty() {
        Set<String> result = cache.get("test_query");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testRemoveElement() {
        cache.add("test_query", List.of("url1", "url2", "url3"));
        cache.removeElement("test_query", "url1");

        Set<String> result = cache.get("test_query");
        assertEquals(2, result.size());
        assertFalse(result.contains("url1"));
    }

    /**
     * Concurrent writes from multiple threads must all succeed without exception.
     * A single Jedis instance shares one socket and will corrupt the protocol stream
     * under concurrent use, causing JedisException or garbled responses.
     * JedisPool hands each thread its own connection, so this test passes only with pooling.
     */
    @Test
    public void testConcurrentWritesDontCorrupt() throws Exception {
        int threadCount = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final String key = "concurrent_key_" + i;
            cache.removeQuery(key);
            futures.add(pool.submit(() -> {
                ready.countDown();
                try { ready.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                try {
                    cache.add(key, List.of("url_a", "url_b", "url_c"));
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            }));
        }

        for (Future<?> f : futures) f.get();
        pool.shutdown();

        // Cleanup
        for (int i = 0; i < threadCount; i++) cache.removeQuery("concurrent_key_" + i);

        assertEquals("Concurrent writes caused errors — Cache needs JedisPool", 0, errors.get());
    }

    /**
     * Concurrent reads and writes on the same key must not produce exceptions or
     * leave the connection in a broken state. Without JedisPool, interleaved
     * reads and writes on one Jedis socket will corrupt the Redis RESP protocol.
     */
    @Test
    public void testConcurrentReadWriteDontCorrupt() throws Exception {
        String key = "rw_concurrent_key";
        cache.removeQuery(key);
        cache.add(key, List.of("seed_url"));

        int threadCount = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final String url = "url_" + i;
            futures.add(pool.submit(() -> {
                ready.countDown();
                try { ready.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                try {
                    cache.add(key, List.of(url));
                    cache.get(key);
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            }));
        }

        for (Future<?> f : futures) f.get();
        pool.shutdown();
        cache.removeQuery(key);

        assertEquals("Concurrent read/write caused errors — Cache needs JedisPool", 0, errors.get());
    }
}
