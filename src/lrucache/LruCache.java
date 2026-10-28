package lrucache;

import java.util.LinkedHashMap;

public class LruCache {
    private final LinkedHashMap<Integer, Data> cache;
    private final int maximumCapacity;

    public LruCache(int capacity) {
        cache = new LinkedHashMap<>(capacity, 0.75f, true);
        maximumCapacity = capacity;
    }

    public Data get(int key) {
        Data data = cache.get(key);
        if (data == null) {
            return null;
        }

        if (data.expiresAt < getCurrentEpoch()) {
            cache.remove(key);
            return null;
        }

        return data;
    }

    public void put(int key, int value, int ttlInSeconds) {
        long ttlInMillis = convertToMs(ttlInSeconds);
        long expiresAt = getCurrentEpoch() + ttlInMillis;
        Data data = new Data(value, expiresAt);
        cache.put(key, data);
        removeOlderKeyIfRequired();
    }

    private void removeOlderKeyIfRequired() {
        if (cache.size() > maximumCapacity) {
            // least entry is at head and youngest one's are last
            cache.entrySet().stream().findFirst().ifPresent(integerDataEntry -> {
                cache.remove(integerDataEntry.getKey());
            });
        }
    }

    private long getCurrentEpoch() {
        return System.nanoTime() / 1_000_000;
    }

    private long convertToMs(int seconds) {
        return seconds * 1_000L;
    }
}
