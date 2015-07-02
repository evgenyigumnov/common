package com.igumnov.common.cache;

import com.igumnov.common.Strings;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class RedisCache implements CacheInterface {


    private static JedisPool jedisPool;
    private static double cacheDefaultTTL;


    public void init(double defaultTTL, String host, int port) {

        cacheDefaultTTL = defaultTTL;
        jedisPool = new JedisPool(new JedisPoolConfig(), host, port);

    }

    @Override
    public Object put(String key, Object value, double ttl, String... tag) {

        try (Jedis jedis = jedisPool.getResource()) {
            try {
                jedis.set(key, Strings.serializeObject((Serializable) value));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (ttl != 0) {
                jedis.expireAt(key, System.currentTimeMillis() / 1000L + (long) (ttl));
            }
            if (tag != null) {
                for (String t : tag) {
                    jedis.sadd(t, key);
                }
            }

            return value;
        }

    }

    @Override
    public Object get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            String val = jedis.get(key);
            try {
                if (val != null) {
                    return Strings.deserializeObject(val);
                } else {
                    return null;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void removeByTag(String tag) {
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> keys = jedis.smembers(tag);
            String[] strs = new String[keys.size()];
            int i = 0;
            for (String key : keys) {
                jedis.del(key);
                strs[i] = key;
                i++;
            }
            if (i > 0) {
                jedis.srem(tag, strs);
            }
        }
    }

    @Override
    public Object remove(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            String ret = jedis.get(key);
            jedis.del(key);
            if (ret != null) {
                try {
                    return Strings.deserializeObject(ret);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }
    }

    @Override
    public Object put(String key, Object value, String... tag) {
        return put(key, value, cacheDefaultTTL, tag);
    }
}
