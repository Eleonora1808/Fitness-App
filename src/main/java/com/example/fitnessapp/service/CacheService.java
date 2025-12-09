package com.example.fitnessapp.service;

import java.util.Optional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public <T> void put(String cacheName, Object key, T value) {
        cache(cacheName).ifPresent(cache -> cache.put(key, value));
    }

    public <T> T get(String cacheName, Object key, Class<T> type) {
        return cache(cacheName).map(cache -> cache.get(key, type)).orElse(null);
    }

    public void evict(String cacheName, Object key) {
        cache(cacheName).ifPresent(cache -> cache.evictIfPresent(key));
    }

    public void clear(String cacheName) {
        cache(cacheName).ifPresent(Cache::clear);
    }

    private Optional<Cache> cache(String cacheName) {
        return Optional.ofNullable(cacheManager.getCache(cacheName));
    }
}


