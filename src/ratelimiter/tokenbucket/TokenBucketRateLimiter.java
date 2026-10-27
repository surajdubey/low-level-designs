package ratelimiter.tokenbucket;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TokenBucketRateLimiter {

    Map<String, TokenBucketConfiguration> configurationPerUser = new ConcurrentHashMap<>();
//    Map<String, AtomicInteger> requestProcessedPerUser = new ConcurrentHashMap<>();

    Map<String, AtomicInteger> availableTokensPerUser = new ConcurrentHashMap<>();
    Map<String, AtomicLong> lastRefillTimePerUser = new ConcurrentHashMap<>();

    public boolean process(String userId) {
        TokenBucketConfiguration configuration = configurationPerUser.get(userId);
        if (configuration == null) {
            return false;
        }

        AtomicInteger availableTokens = availableTokensPerUser.get(userId);
        AtomicLong lastRefilledEpoch = lastRefillTimePerUser.get(userId);
        lastRefilledEpoch.compareAndSet(0, getCurrentEpoch() - 1_000);

        while (true) {
            long currentEpoch = getCurrentEpoch();
            int oldValue = availableTokens.get();
            int newValue = oldValue - 1;

            long lastRefilledEpochValue = lastRefilledEpoch.get();
            long allowedNewTokens = (currentEpoch - lastRefilledEpochValue)
                    * configuration.refillPerTimeUnit();

            long allowedRequests = Math.min(configuration.bucketCapacity(), allowedNewTokens + newValue);
            if (newValue <= 0) {
                return false;
            }

            if (availableTokens.compareAndSet(oldValue, newValue)) {
                lastRefilledEpoch.compareAndSet(lastRefilledEpochValue, currentEpoch);
                return true;
            }
        }
    }

    private long getCurrentEpoch() {
        return Instant.now()
                .truncatedTo(ChronoUnit.SECONDS)
                .toEpochMilli();
    }

    public void addConfiguration(String userId) {
//        requestProcessedPerUser.putIfAbsent(userId, new AtomicInteger(0));
        availableTokensPerUser.putIfAbsent(userId, new AtomicInteger(0));
        lastRefillTimePerUser.putIfAbsent(userId, new AtomicLong(0));
    }
}
