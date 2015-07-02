package com.igumnov.common;

import com.igumnov.common.cache.CacheInterface;
import com.igumnov.common.cache.MemoryCache;
import com.igumnov.common.cache.RedisCache;

public class Cache {


    private static CacheInterface cache;

    public static void init(int size, double defaultTTL) {

        MemoryCache mc = new MemoryCache();
        mc.init(size,defaultTTL);
        cache = mc;
    }

    public static void initWithRedis(double defaultTTL, String host, int port) {
        RedisCache rc = new RedisCache();
        rc.init(defaultTTL,host,port);
        cache = rc;
    }

    public static Object put(String key, Object value, double ttl, String... tag) {
           return  cache.put(key, value, ttl, tag);
    }

    public static Object get(String key) {
        return cache.get(key);
    }

    public static void removeByTag(String tag) {
        cache.removeByTag(tag);
    }

    public static Object remove(String key) {
        return cache.remove(key);
    }


    public static Object put(String key, Object value, String... tag) {
        return put(key, value, 0, tag);
    }


}
