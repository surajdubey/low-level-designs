package ratelimiter.tokenbucket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class TokenBucketRateLimiter {

    Map<String, TokenBucketConfiguration> configurationPerUser = new ConcurrentHashMap<>();
    Map<String, AtomicReference<BucketStatus>> bucketStatusPerUser = new ConcurrentHashMap<>();

    int totalAdded = 0;

    private final static long SCALING_FACTOR = 1_00_000;

    public boolean process(String userId) {
        TokenBucketConfiguration configuration = configurationPerUser.get(userId);
        if (configuration == null) {
            return false;
        }

        AtomicReference<BucketStatus> bucketStatus = bucketStatusPerUser.computeIfAbsent(userId,
                (key) -> new AtomicReference<>(new BucketStatus((configuration.bucketCapacity * SCALING_FACTOR), getCurrentEpoch()))
        );
        while (true) {
            long currentEpoch = getCurrentEpoch();
            BucketStatus currentBucketStatus = bucketStatus.get();

            long lastRefilledEpochValue = currentBucketStatus.lastUpdatedEpoch;
            double allowedNewRequestsDouble = (currentEpoch - lastRefilledEpochValue)
                    * configuration.refillPerMillisecond * SCALING_FACTOR;
            long allowedNewRequests = (long) (allowedNewRequestsDouble);

            // added for debugging
            totalAdded = totalAdded + (int) allowedNewRequests;

            long oldValue = currentBucketStatus.availableCapacity;
            long scaledBucketCapacity = configuration.bucketCapacity * SCALING_FACTOR;
            long scaledRemainingRequests = Math.min(scaledBucketCapacity, allowedNewRequests + oldValue);

            long scaledNewRequest = 1 * SCALING_FACTOR;

            if (scaledRemainingRequests < scaledNewRequest) {
//                System.out.println("scaledRemainingRequests is " + scaledRemainingRequests);
                return false;
            }
//            long newValue = oldValue - 1;
            long newValue = scaledRemainingRequests - scaledNewRequest;

//            if (currentEpoch != currentBucketStatus.lastUpdatedEpoch && allowedNewRequests > 0) {
////                System.out.println("Updating epoch");
//            }
            BucketStatus newStatus = new BucketStatus(newValue, currentEpoch);
            if (bucketStatus.compareAndSet(currentBucketStatus, newStatus)) {
                return true;
            }
        }
    }

    public long getCurrentEpoch() {
        return System.nanoTime() / 1_000_000;
    }

    public void addConfiguration(String userId, int capacity, int refillPerSecond) {
        long scaledCapacity = capacity * SCALING_FACTOR;
        configurationPerUser.computeIfAbsent(userId, (key) -> new TokenBucketConfiguration(capacity, refillPerSecond));
        bucketStatusPerUser.computeIfAbsent(userId, (key) -> new AtomicReference<>(
                new BucketStatus(scaledCapacity, getCurrentEpoch())
        ));
    }
}
