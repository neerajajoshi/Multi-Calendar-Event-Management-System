import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import util.PerformanceCache;

/**
 * Tests for PerformanceCache.
 */
public class PerformanceCacheTest {

  private PerformanceCache cache;

  /**
   * Set up test fixtures.
   */
  @Before
  public void setUp() {
    cache = new PerformanceCache(5);
  }

  @Test
  public void testDefaultConstructor() {
    PerformanceCache defaultCache = new PerformanceCache();
    assertNotNull(defaultCache);
  }

  @Test
  public void testGenerateDateQueryKey() {
    LocalDate date = LocalDate.of(2025, 11, 1);
    String key = cache.generateDateQueryKey("TestCal", date);

    assertEquals("TestCal|2025-11-01", key);
  }

  @Test
  public void testGenerateRangeQueryKey() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    String key = cache.generateRangeQueryKey("TestCal", start, end);

    assertEquals("TestCal|2025-11-01T10:00|2025-11-01T11:00", key);
  }

  @Test
  public void testPutAndGet() {
    String key = "test-key";
    List<Object> data = new ArrayList<>();
    data.add("item1");
    data.add("item2");

    cache.put(key, data);
    List<Object> retrieved = cache.get(key);

    assertNotNull(retrieved);
    assertEquals(2, retrieved.size());
    assertEquals("item1", retrieved.get(0));
    assertEquals("item2", retrieved.get(1));
  }

  @Test
  public void testPutNullKey() {
    List<Object> data = new ArrayList<>();
    data.add("item");

    cache.put(null, data);

    assertNull(cache.get(null));
  }

  @Test
  public void testPutNullData() {
    String key = "test-key";

    cache.put(key, null);

    assertNull(cache.get(key));
  }

  @Test
  public void testGetNullKey() {
    List<Object> result = cache.get(null);

    assertNull(result);
  }

  @Test
  public void testGetNonExistentKey() {
    List<Object> result = cache.get("non-existent");

    assertNull(result);
  }

  @Test
  public void testContainsKey() {
    String key = "test-key";
    List<Object> data = new ArrayList<>();
    data.add("item");

    cache.put(key, data);

    assertTrue(cache.containsKey(key));
    assertFalse(cache.containsKey("other-key"));
  }

  @Test
  public void testContainsKeyNull() {
    assertFalse(cache.containsKey(null));
  }

  @Test
  public void testCacheEviction() {
    for (int i = 0; i < 6; i++) {
      List<Object> data = new ArrayList<>();
      data.add("item" + i);
      cache.put("key" + i, data);
    }

    assertNotNull(cache.get("key5"));
    PerformanceCache.CacheStats stats = cache.getStats();
    assertTrue(stats.getCurrentSize() <= 5);
  }

  @Test
  public void testInvalidateCalendar() {
    cache.put("Cal1|2025-11-01", List.of("event1"));
    cache.put("Cal1|2025-11-02", List.of("event2"));
    cache.put("Cal2|2025-11-01", List.of("event3"));

    cache.invalidateCalendar("Cal1");

    assertNull(cache.get("Cal1|2025-11-01"));
    assertNull(cache.get("Cal1|2025-11-02"));
    assertNotNull(cache.get("Cal2|2025-11-01"));
  }

  @Test
  public void testInvalidateCalendarNull() {
    cache.put("Cal1|2025-11-01", List.of("event1"));

    cache.invalidateCalendar(null);

    assertNotNull(cache.get("Cal1|2025-11-01"));
  }

  @Test
  public void testClear() {
    cache.put("key1", List.of("item1"));
    cache.put("key2", List.of("item2"));

    cache.clear();

    assertNull(cache.get("key1"));
    assertNull(cache.get("key2"));
  }

  @Test
  public void testGetStats() {
    cache.put("key1", List.of("item1"));
    cache.put("key2", List.of("item2"));

    PerformanceCache.CacheStats stats = cache.getStats();

    assertNotNull(stats);
    assertEquals(2, stats.getCurrentSize());
    assertEquals(5, stats.getMaxSize());
    assertEquals(0.4, stats.getUtilization(), 0.01);
  }

  @Test
  public void testCacheStatsToString() {
    cache.put("key1", List.of("item1"));
    cache.put("key2", List.of("item2"));

    PerformanceCache.CacheStats stats = cache.getStats();
    String statsString = stats.toString();

    assertTrue(statsString.contains("size=2/5"));
    assertTrue(statsString.contains("utilization=40"));
  }

  @Test
  public void testCacheStatsUtilizationZeroMax() {
    PerformanceCache zeroCache = new PerformanceCache(0);
    PerformanceCache.CacheStats stats = zeroCache.getStats();

    assertEquals(0.0, stats.getUtilization(), 0.01);
  }

  @Test
  public void testDataIsolation() {
    List<Object> originalData = new ArrayList<>();
    originalData.add("item1");

    cache.put("key", originalData);

    originalData.add("item2");

    List<Object> retrieved = cache.get("key");
    assertEquals(1, retrieved.size());

    retrieved.add("item3");

    List<Object> retrieved2 = cache.get("key");
    assertEquals(1, retrieved2.size());
  }

  @Test
  public void testAccessOrderUpdate() {
    cache.put("key1", List.of("item1"));
    cache.put("key2", List.of("item2"));
    cache.put("key3", List.of("item3"));

    cache.get("key1");

    cache.put("key4", List.of("item4"));
    cache.put("key5", List.of("item5"));
    cache.put("key6", List.of("item6"));

    assertNotNull(cache.get("key1"));
  }

  @Test
  public void testRemoveEldestEntryBoundary() {
    for (int i = 0; i < 5; i++) {
      cache.put("key" + i, List.of("item" + i));
    }

    for (int i = 0; i < 5; i++) {
      assertNotNull(cache.get("key" + i));
    }

    cache.put("key5", List.of("item5"));

    PerformanceCache.CacheStats stats = cache.getStats();
    assertTrue(stats.getCurrentSize() <= 5);
  }

  @Test
  public void testGetUpdatesAccessOrder() {
    cache.put("key1", List.of("item1"));
    cache.put("key2", List.of("item2"));

    cache.get("key1");
    cache.get("key1");

    // Add more items
    cache.put("key3", List.of("item3"));
    cache.put("key4", List.of("item4"));
    cache.put("key5", List.of("item5"));

    // Access key1 again
    List<Object> result = cache.get("key1");
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  public void testClearRemovesBothCaches() {
    cache.put("key1", List.of("item1"));
    cache.put("key2", List.of("item2"));

    // Items are present
    assertTrue(cache.containsKey("key1"));
    assertTrue(cache.containsKey("key2"));

    cache.clear();

    // Both caches cleared
    assertFalse(cache.containsKey("key1"));
    assertFalse(cache.containsKey("key2"));
    assertEquals(0, cache.getStats().getCurrentSize());
  }

  @Test
  public void testEvictionWithExactMaxSize() {
    // Create cache with size 3
    PerformanceCache smallCache = new PerformanceCache(3);

    smallCache.put("key1", List.of("item1"));
    smallCache.put("key2", List.of("item2"));
    smallCache.put("key3", List.of("item3"));

    // Cache should be at max size
    assertEquals(3, smallCache.getStats().getCurrentSize());

    // Add one more to trigger eviction
    smallCache.put("key4", List.of("item4"));

    // Cache should still be at or below max size
    assertTrue(smallCache.getStats().getCurrentSize() <= 3);
  }

  @Test
  public void testMultipleGetsUpdateAccessOrder() {
    cache.put("key1", List.of("item1"));
    cache.put("key2", List.of("item2"));
    cache.put("key3", List.of("item3"));

    // Access key1 and key2 multiple times
    cache.get("key1");
    cache.get("key2");
    cache.get("key1");

    // Add more items to fill cache
    cache.put("key4", List.of("item4"));
    cache.put("key5", List.of("item5"));

    // key1 and key2 should still be accessible due to recent access
    assertNotNull(cache.get("key1"));
    assertNotNull(cache.get("key2"));
  }

  @Test
  public void testRemoveEldestEntryBoundaryPrecise() {
    PerformanceCache smallCache = new PerformanceCache(3);

    smallCache.put("key1", List.of("item1"));
    smallCache.put("key2", List.of("item2"));
    smallCache.put("key3", List.of("item3"));

    assertEquals(3, smallCache.getStats().getCurrentSize());
    assertNotNull(smallCache.get("key1"));
    assertNotNull(smallCache.get("key2"));
    assertNotNull(smallCache.get("key3"));
  }

  @Test
  public void testGetUpdatesLruOrder() {
    cache.put("key1", List.of("item1"));
    cache.put("key2", List.of("item2"));
    cache.put("key3", List.of("item3"));
    cache.put("key4", List.of("item4"));
    cache.put("key5", List.of("item5"));

    assertNotNull(cache.get("key1"));

    cache.put("key6", List.of("item6"));
    cache.put("key7", List.of("item7"));

    assertNotNull("key1 should still be in cache after being accessed",
        cache.get("key1"));
  }

  @Test
  public void testClearRemovesAccessOrderTracking() {
    // Fill cache
    cache.put("key1", List.of("item1"));
    cache.put("key2", List.of("item2"));
    cache.put("key3", List.of("item3"));

    // Clear the cache
    cache.clear();

    cache.put("newKey1", List.of("newItem1"));
    cache.put("newKey2", List.of("newItem2"));
    cache.put("newKey3", List.of("newItem3"));
    cache.put("newKey4", List.of("newItem4"));
    cache.put("newKey5", List.of("newItem5"));

    cache.put("newKey6", List.of("newItem6"));

    assertEquals(5, cache.getStats().getCurrentSize());
    assertNotNull(cache.get("newKey6"));
  }

  @Test
  public void testRemoveEldestEntryExactBoundary() {
    PerformanceCache smallCache = new PerformanceCache(3);

    smallCache.put("key1", List.of("item1"));
    smallCache.put("key2", List.of("item2"));
    smallCache.put("key3", List.of("item3"));

    assertEquals(3, smallCache.getStats().getCurrentSize());
    assertNotNull(smallCache.get("key1"));
    assertNotNull(smallCache.get("key2"));
    assertNotNull(smallCache.get("key3"));

    smallCache.get("key1");
    smallCache.get("key2");
    smallCache.get("key3");

    assertNotNull(smallCache.get("key1"));
    assertNotNull(smallCache.get("key2"));
    assertNotNull(smallCache.get("key3"));
  }

  @Test
  public void testGetMustUpdateAccessOrder() {
    cache.put("key1", List.of("item1"));
    cache.put("key2", List.of("item2"));
    cache.put("key3", List.of("item3"));
    cache.put("key4", List.of("item4"));
    cache.put("key5", List.of("item5"));

    assertNotNull(cache.get("key1"));

    cache.put("key6", List.of("item6"));
    cache.put("key7", List.of("item7"));

    assertNotNull("key1 must remain in cache because get() updated its access order",
        cache.get("key1"));

    assertNull("key2 should be evicted as it was least recently used",
        cache.get("key2"));
  }

  @Test
  public void testClearMustRemoveBothMaps() {
    cache.put("key1", List.of("item1"));
    cache.put("key2", List.of("item2"));
    cache.put("key3", List.of("item3"));

    cache.get("key1");
    cache.get("key2");
    cache.get("key3");

    cache.clear();

    assertEquals(0, cache.getStats().getCurrentSize());

    cache.put("newKey1", List.of("newItem1"));
    cache.put("newKey2", List.of("newItem2"));
    cache.put("newKey3", List.of("newItem3"));
    cache.put("newKey4", List.of("newItem4"));
    cache.put("newKey5", List.of("newItem5"));

    assertEquals(5, cache.getStats().getCurrentSize());

    cache.put("newKey6", List.of("newItem6"));

    assertEquals(5, cache.getStats().getCurrentSize());

    assertNotNull(cache.get("newKey6"));
  }
}
