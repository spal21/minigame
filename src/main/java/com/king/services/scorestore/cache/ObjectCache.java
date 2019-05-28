package com.king.services.scorestore.cache;

public interface ObjectCache<K, V> {
    V get(K key);

    V put(K key, V value);
}
