package org.example;

import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;

public class Cache {
    private final JedisPooled jd;

    public Cache() {
        jd = new JedisPooled("localhost", 6379);
    }

    /**
     * returns an empty set if get() is not in cache
     */
    public Set<String> get(String query) {
        return jd.smembers(query);
    }

    public void add(String query, List<String> urlList) {
        jd.sadd(query, urlList.toArray(new String[0]));
    }

    public void removeQuery(String query) {
        jd.del(query);
    }

    public void removeElement(String query, String url) {
        jd.srem(query, url);
    }
}
