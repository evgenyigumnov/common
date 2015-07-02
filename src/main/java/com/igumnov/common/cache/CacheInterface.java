package com.igumnov.common.cache;

import java.util.HashSet;
import java.util.LinkedList;

public interface CacheInterface {

    public Object put(String key, Object value, double ttl, String... tag);
    public Object get(String key);
    public void removeByTag(String tag);
    public Object remove(String key);
    public Object put(String key, Object value, String... tag);



}
