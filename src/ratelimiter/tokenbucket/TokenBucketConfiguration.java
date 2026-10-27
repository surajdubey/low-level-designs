package ratelimiter.tokenbucket;

public record TokenBucketConfiguration(
        int bucketCapacity,
        int refillPerTimeUnit
) {

}
