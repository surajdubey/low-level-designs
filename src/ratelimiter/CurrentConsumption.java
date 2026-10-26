package ratelimiter;

import java.util.concurrent.atomic.AtomicInteger;

public class CurrentConsumption {
    AtomicInteger requestsProcessed;

    public CurrentConsumption() {
        requestsProcessed = new AtomicInteger(0);
    }
}
