package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple caching mechanism for performance optimization.
 * Design rationale:
 * - Caches frequently accessed data to reduce computation
 * - Thread-safe using ConcurrentHashMap
 * - LRU-style eviction to prevent memory leaks
 * - Configurable cache sizes for different data types
 */
public class PerformanceCache {
  
  private static final int DEFAULT_CACHE_SIZE = 100;
  
  // Cache for date-based event queries
  private final Map<String, List<Object>> dateQueryCache;
  private final LinkedHashMap<String, Long> cacheAccessOrder;
  private final int maxCacheSize;

  /**
   * This is performance cache.
   */
  public PerformanceCache() {
    this(DEFAULT_CACHE_SIZE);
  }

  /**
   * This is Performance cache.
   */
  public PerformanceCache(int maxSize) {
    this.maxCacheSize = maxSize;
    this.dateQueryCache = new ConcurrentHashMap<>();
    this.cacheAccessOrder = new LinkedHashMap<String, Long>() {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
        return size() > maxCacheSize;
      }
    };
  }
  
  /**
   * Generates a cache key for date-based queries.
   */
  public String generateDateQueryKey(String calendarName, LocalDate date) {
    return calendarName + "|" + date.toString();
  }
  
  /**
   * Generates a cache key for range-based queries.
   */
  public String generateRangeQueryKey(String calendarName, LocalDateTime start, LocalDateTime end) {
    return calendarName + "|" + start.toString() + "|" + end.toString();
  }
  
  /**
   * Puts data in the cache.
   */
  public void put(String key, List<Object> data) {
    if (key != null && data != null) {
      dateQueryCache.put(key, new ArrayList<>(data));
      updateAccessOrder(key);
      evictIfNecessary();
    }
  }
  
  /**
   * Gets data from the cache.
   */
  public List<Object> get(String key) {
    if (key == null) {
      return null;
    }
    
    List<Object> data = dateQueryCache.get(key);
    if (data != null) {
      updateAccessOrder(key);
      return new ArrayList<>(data);
    }
    return null;
  }
  
  /**
   * Checks if the cache contains a key.
   */
  public boolean containsKey(String key) {
    return key != null && dateQueryCache.containsKey(key);
  }
  
  /**
   * Invalidates cache entries for a specific calendar.
   */
  public void invalidateCalendar(String calendarName) {
    if (calendarName == null) {
      return;
    }
    
    Set<String> keysToRemove = new HashSet<>();
    for (String key : dateQueryCache.keySet()) {
      if (key.startsWith(calendarName + "|")) {
        keysToRemove.add(key);
      }
    }
    
    for (String key : keysToRemove) {
      dateQueryCache.remove(key);
      cacheAccessOrder.remove(key);
    }
  }
  
  /**
   * Clears all cache entries.
   */
  public void clear() {
    dateQueryCache.clear();
    cacheAccessOrder.clear();
  }
  
  /**
   * Gets cache statistics.
   */
  public CacheStats getStats() {
    return new CacheStats(dateQueryCache.size(), maxCacheSize);
  }
  
  /**
   * Updates the access order for LRU eviction.
   */
  private void updateAccessOrder(String key) {
    synchronized (cacheAccessOrder) {
      cacheAccessOrder.put(key, System.currentTimeMillis());
    }
  }
  
  /**
   * Evicts old entries if cache is full.
   */
  private void evictIfNecessary() {
    if (dateQueryCache.size() > maxCacheSize) {
      synchronized (cacheAccessOrder) {
        String oldestKey = cacheAccessOrder.entrySet().iterator().next().getKey();
        dateQueryCache.remove(oldestKey);
        cacheAccessOrder.remove(oldestKey);
      }
    }
  }
  
  /**
   * Cache statistics holder.
   */
  public static class CacheStats {
    private final int currentSize;
    private final int maxSize;

    /**
     * This is cache status.
     */
    public CacheStats(int currentSize, int maxSize) {
      this.currentSize = currentSize;
      this.maxSize = maxSize;
    }
    
    public int getCurrentSize() {
      return currentSize;
    }
    
    public int getMaxSize() {
      return maxSize;
    }
    
    public double getUtilization() {
      return maxSize > 0 ? (double) currentSize / maxSize : 0.0;
    }
    
    @Override
    public String toString() {
      return String.format("CacheStats{size=%d/%d, utilization=%.1f%%}", 
                          currentSize, maxSize, getUtilization() * 100);
    }
  }
}