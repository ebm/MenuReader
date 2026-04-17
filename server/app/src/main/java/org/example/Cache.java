package org.example;

import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;

public class Cache {
    private final Jedis jd;

    public Cache() {
        jd = new Jedis("redis://localhost:6379");
    }

    /**
     * returns an empty set if get() is not in cache
     */
    public Set<String> get(String query) {
        return jd.smembers(query);
    }

    public void put(String query, List<String> urlList) {
        jd.del(query);
        jd.sadd(query, urlList.toArray(new String[0]));
    }

    public void addToList(String query, List<String> urlList) {
        jd.sadd(query, urlList.toArray(new String[0]));
    }

    public void removeQuery(String query) {
        jd.del(query);
    }

    public void removeElement(String query, String url) {
        jd.srem(query, url);
    }
}
