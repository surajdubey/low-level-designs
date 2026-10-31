package connectionpool;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static connectionpool.ConnectionPoolManager.Connection;

public class ConnectionPoolDemo {
    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
        System.out.println("Starting connections now");
        ConnectionPoolManager manager = new ConnectionPoolManager(5);

        Deque<Connection> allConnections = new ConcurrentLinkedDeque<Connection>();

        Thread t1 = new Thread(() -> {
            try {

                for (int i = 0; i < 10; i++) {
                    Connection connection = manager.getWorkingConnection();
                    allConnections.add(connection);

                    // doing some comp
//                    Thread.sleep(10);
//                    manager.release(connection);
                    System.out.println("got connection " + connection + " for i = " + i);
                }
            } catch (InterruptedException e) {
                System.out.println("Error in scheduler thread");
            }

        });

        Thread t2 = new Thread(() -> {
            allConnections.iterator().forEachRemaining(connection1 -> {
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                manager.release(connection1);
            });
        });

        scheduledService.scheduleAtFixedRate(t2, 0, 1, TimeUnit.SECONDS);
        t1.start();

        t1.join();

        scheduledService.shutdown();

    }
}
