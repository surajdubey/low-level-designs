package ratelimiter.tokenbucket;

public class BucketStatus {
    long lastUpdatedEpoch;
    long availableCapacity;

    public BucketStatus(long availableCapacity, long epoch) {
        this.availableCapacity = availableCapacity;
        this.lastUpdatedEpoch = epoch;
    }
}
