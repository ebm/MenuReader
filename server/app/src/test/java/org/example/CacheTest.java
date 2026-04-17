package org.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

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
}
