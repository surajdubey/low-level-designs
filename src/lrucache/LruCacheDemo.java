package lrucache;

public class LruCacheDemo {
    private static final LruCache lruCache = new LruCache(2);

    public static void main(String[] args) {
        lruCache.put(1, 1, 100);

        lruCache.put(2, 2, 10);
        print(1);
        lruCache.put(3, 3, 10);
        print(1);
        print(2);
        print(3);

    }

    private static void print(int key) {
        Data data1 = lruCache.get(key);
        if (data1 == null) {
            System.out.println("No value found for " + key);
        } else {
            System.out.println(String.format("Value for key %s is %s", key, data1.value));
        }
    }
}
