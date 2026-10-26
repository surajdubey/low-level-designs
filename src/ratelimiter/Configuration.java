package ratelimiter;

public record Configuration(String id, int limit, LIMIT_TYPE limitType) {
}

enum LIMIT_TYPE {
    HOUR,
    MINUTES
}