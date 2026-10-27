package ratelimiter.tokenbucket;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class TokenBucketRateLimiter {

    Map<String, TokenBucketConfiguration> configurationPerUser = new ConcurrentHashMap<>();
    Map<String, AtomicReference<BucketStatus>> bucketStatusPerUser = new ConcurrentHashMap<>();

    public boolean process(String userId) {
        TokenBucketConfiguration configuration = configurationPerUser.get(userId);
        if (configuration == null) {
            return false;
        }

        AtomicReference<BucketStatus> bucketStatus = bucketStatusPerUser.get(userId);

        while (true) {
            long currentEpoch = getCurrentEpoch();
            BucketStatus currentBucketStatus = bucketStatus.get();
            long oldValue = currentBucketStatus.availableCapacity;
            long newValue = oldValue - 1;

            long lastRefilledEpochValue = currentBucketStatus.lastUpdatedEpoch;
            double allowedRequestsDouble = (currentEpoch - lastRefilledEpochValue)
                    * configuration.refillPerMillisecond;
            long allowedNewRequests = (long) (allowedRequestsDouble);

            long remainingRequests = Math.min(configuration.bucketCapacity, allowedNewRequests + newValue);
            if (remainingRequests <= 0) {
                return false;
            }

            BucketStatus newStatus = new BucketStatus(remainingRequests, currentEpoch);
            if (bucketStatus.compareAndSet(currentBucketStatus, newStatus)) {
                return true;
            }
        }
    }

    private long getCurrentEpoch() {
        return System.nanoTime()/1_000_000;
    }

    public void addConfiguration(String userId, int capacity, int refillPerSecond) {
        configurationPerUser.computeIfAbsent(userId, (key) -> new TokenBucketConfiguration(capacity, refillPerSecond));
        bucketStatusPerUser.computeIfAbsent(userId,
                (key) -> new AtomicReference<>(new BucketStatus(capacity, getCurrentEpoch()))
        );
    }
}
