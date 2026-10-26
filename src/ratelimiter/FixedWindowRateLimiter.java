package ratelimiter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedWindowRateLimiter {

    Map<String, Configuration> configurationMap = new ConcurrentHashMap<>();
    Map<String, Map<Long, AtomicInteger>> consumptionPerUser = new ConcurrentHashMap<>();

    public boolean process(String userId) {
        Configuration configuration = configurationMap.get(userId);

        if (configuration == null || configuration.limit() == 0) {
            return false;
        }

        Map<Long, AtomicInteger> userConsumption = consumptionPerUser.get(userId);
        if (userConsumption == null) {
            return false;
        }

        while (true) {
            AtomicInteger consumedRequest = userConsumption.computeIfAbsent(getCurrentEpoch(), (key) -> new AtomicInteger(0));
            int oldValue = consumedRequest.get();
            int newValue = oldValue + 1;
            if (newValue <= configuration.limit()) {
                if (consumedRequest.compareAndSet(oldValue, newValue)) {
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    private long getCurrentEpoch() {
        return Instant.now()
                .truncatedTo(ChronoUnit.MINUTES)
                .toEpochMilli();
    }

    public void addConfiguration(String userId, Configuration configuration) {
        configurationMap.put(userId, configuration);
        consumptionPerUser.computeIfAbsent(userId, key -> new ConcurrentHashMap<>());
    }

    public void deleteAllOlderEntries() {
        consumptionPerUser.forEach((key, userConsumption) ->
                userConsumption.entrySet().removeIf(entry -> entry.getKey() < getCurrentEpoch())
        );
    }
}
