package ratelimiter.tokenbucket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class TokenBucketRateLimiter {

    Map<String, TokenBucketConfiguration> configurationPerUser = new ConcurrentHashMap<>();
    Map<String, AtomicReference<BucketStatus>> bucketStatusPerUser = new ConcurrentHashMap<>();

    int totalAdded = 0;

    public boolean process(String userId) {
        TokenBucketConfiguration configuration = configurationPerUser.get(userId);
        if (configuration == null) {
            return false;
        }

        AtomicReference<BucketStatus> bucketStatus = bucketStatusPerUser.get(userId);
        while (true) {
            long currentEpoch = getCurrentEpoch();
            BucketStatus currentBucketStatus = bucketStatus.get();

            long lastRefilledEpochValue = currentBucketStatus.lastUpdatedEpoch;
            double allowedNewRequestsDouble = (currentEpoch - lastRefilledEpochValue)
                    * configuration.refillPerMillisecond;
            long allowedNewRequests = (long) (allowedNewRequestsDouble);

            totalAdded = totalAdded + (int) allowedNewRequests;
            long oldValue = currentBucketStatus.availableCapacity;
            long newValue = oldValue - 1;

            long remainingRequests = Math.min(configuration.bucketCapacity, allowedNewRequests + newValue);
//            System.out.println(allowedNewRequests + " " + allowedNewRequestsDouble + " " + remainingRequests);
            if (remainingRequests < 0) {
                return false;
            }

            if (currentEpoch != currentBucketStatus.lastUpdatedEpoch && allowedNewRequests > 0) {
                System.out.println("Updating epoch");
            }
            BucketStatus newStatus = new BucketStatus(remainingRequests, currentEpoch);
            if (bucketStatus.compareAndSet(currentBucketStatus, newStatus)) {
                return true;
            }
        }
    }

    public long getCurrentEpoch() {
        return System.nanoTime() / 1_000_000;
    }

    public void addConfiguration(String userId, int capacity, int refillPerSecond) {
        configurationPerUser.computeIfAbsent(userId, (key) -> new TokenBucketConfiguration(capacity, refillPerSecond));
        bucketStatusPerUser.computeIfAbsent(userId, (key) -> new AtomicReference<>(
                new BucketStatus(capacity, getCurrentEpoch())
        ));


        //        bucketStatusPerUser.computeIfAbsent(userId,
//                (key) -> new AtomicReference<>(new BucketStatus(capacity, getCurrentEpoch()))
//        );
    }
}
