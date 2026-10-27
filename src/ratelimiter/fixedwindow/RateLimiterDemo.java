package ratelimiter.fixedwindow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RateLimiterDemo {

    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        String userId = "id1";
        Configuration configuration = new Configuration(
                userId, 26, LIMIT_TYPE.MINUTES
        );

        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter();
        rateLimiter.addConfiguration(userId, configuration);

        List<Thread> allThreads = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            int finalI = i;
            Thread t = new Thread(() -> {
                for (int j = 0; j < 20; j++) {
                    if (rateLimiter.process(userId)) {
                        System.out.println(finalI + " was allowed");
                    } else {
                        System.out.println(finalI + " was rejected");
                    }
                }
            });

            allThreads.add(t);
        }


        scheduledExecutorService.scheduleAtFixedRate(rateLimiter::deleteAllOlderEntries,
                0,
                1,
                TimeUnit.SECONDS
        );

        for (Thread t : allThreads) {
            t.start();
        }

        for (Thread t : allThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        scheduledExecutorService.shutdown();
    }
}
