package ratelimiter.tokenbucket;

public class TokenBucketConfiguration {
    int bucketCapacity;
    int refillPerSeconds;

    double refillPerMillisecond;

    public TokenBucketConfiguration(int bucketCapacity, int refillPerSeconds) {
        this.bucketCapacity = bucketCapacity;
        this.refillPerSeconds = refillPerSeconds;
        this.refillPerMillisecond = refillPerSeconds / 1000.0;
    }
}
