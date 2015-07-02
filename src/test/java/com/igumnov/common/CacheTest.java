package com.igumnov.common;


import org.junit.Test;

import javax.security.auth.callback.CallbackHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class CacheTest {
    @Test
    public void testCache() throws ExecutionException, InterruptedException {

        Cache.init(7, 24.0 * 60 * 60);

        //Cache.initWithRedis(24.0 * 60 * 60, "localhost", 6379);

        Object value = Cache.put("a1", "va11", 1.1, "tag1", "tag2");
        Cache.put("a2", "va12", 1.1, "tag1");
        Cache.put("a3", "va13", 1.1);
        Cache.removeByTag("tag2");
        assertEquals(Cache.get("a2"), "va12");
        assertNull(Cache.get("a1"));
        assertEquals(Cache.remove("a3"), "va13");
        assertNull(Cache.get("a3"));
        assertNull(Cache.remove("none"));
        Cache.removeByTag("none");
        assertEquals(Cache.get("a2"), "va12");
        Time.sleepInSeconds(1.3);
        assertNull(Cache.get("a2"));
        Cache.put("a2", "va12", 1.1, "tag1");
        assertEquals(Cache.get("a2"), "va12");
        Time.sleepInSeconds(1.3);
        assertNull(Cache.get("a2"));

        Future<Object> concurensyTest1 = Task.startFunction(() -> {
            try {
                for (int i = 1; i < 10000; ++i) {
                    Cache.put("a" + Number.randomIntByRange(1, 10), "sss", Number.randomDoubleByRange(0.01, 1), "t" + Number.randomIntByRange(1, 5));
                }
            } catch (Exception e) {
                Log.error(e.getMessage(), e);
                return new Boolean(false);
            }
            return new Boolean(true);
        });

        Future<Object> concurensyTest2 = Task.startFunction(() -> {
            try {
                for (int i = 1; i < 10000; ++i) {
                    Cache.get("a" + Number.randomIntByRange(1, 10));
                }
            } catch (Exception e) {
                Log.error(e.getMessage(), e);
                return new Boolean(false);
            }
            return new Boolean(true);
        });

        Future<Object> concurensyTest3 = Task.startFunction(() -> {
            try {
                for (int i = 1; i < 10000; ++i) {
                    Cache.remove("a" + Number.randomIntByRange(1, 10));
                }
            } catch (Exception e) {
                Log.error(e.getMessage(), e);
                return new Boolean(false);
            }
            return new Boolean(true);
        });

        Future<Object> concurensyTest4 = Task.startFunction(() -> {
            try {
                for (int i = 1; i < 10000; ++i) {
                    Cache.removeByTag("t" + Number.randomIntByRange(1, 10));
                }
            } catch (Exception e) {
                Log.error(e.getMessage(), e);
                return new Boolean(false);
            }
            return new Boolean(true);
        });
        assertTrue((Boolean) concurensyTest1.get());
        assertTrue((Boolean) concurensyTest2.get());
        assertTrue((Boolean) concurensyTest3.get());
        assertTrue((Boolean) concurensyTest4.get());

        Cache.put("aa", "aaa");
        Cache.put("aa", "nnn");
        Time.sleepInSeconds(3);
        assertEquals(Cache.get("aa"), "nnn");

    }

}
