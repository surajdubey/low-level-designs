package ratelimiter.tokenbucket;

public class TokenBucketRateLimiterDemo {
    public static void main(String[] args) throws InterruptedException {

        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter();
        rateLimiter.addConfiguration("user1", 500, 1000);
        rateLimiter.addConfiguration("user2", 100, 100);
        rateLimiter.addConfiguration("user3", 100, 100);

        Thread t1 = new Thread(() -> {
            long startTime = rateLimiter.getCurrentEpoch();
            int failed = 0;
            System.out.println("Time at start " + startTime);
            for (int i = 1; i <= 2000; i++) {
                boolean processed = rateLimiter.process("user1");
                if (!processed) {
                    failed++;
//                    System.out.println("failed to process " + i);
                }
            }
//            System.out.println("Consumed ms " + (rateLimiter.getCurrentEpoch() - startTime));
            System.out.println("failed " + failed + " new tokens " + rateLimiter.totalAdded);
        });

        t1.start();
        t1.join();
    }
}
