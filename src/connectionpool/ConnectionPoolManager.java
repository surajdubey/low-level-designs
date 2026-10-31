package connectionpool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class ConnectionPoolManager {
    private final Semaphore semaphore;
    private final LinkedBlockingQueue<Connection> linkedBlockingQueue = new LinkedBlockingQueue<>();

    public ConnectionPoolManager(int numberOfConnections) {
        semaphore = new Semaphore(numberOfConnections);
        for (int i = 0; i < numberOfConnections; i++) {
            submitTask();
        }
    }

    private void submitTask() {
        Connection connection = new DatabaseConnection();
        connection.createConnection();
        linkedBlockingQueue.add(connection);
    }

    public Connection getWorkingConnection() throws InterruptedException {

        try {
            semaphore.acquire();

            try {
                Connection connection = linkedBlockingQueue.take();
                return connection;
            } catch (InterruptedException e) {
                semaphore.release();
                throw e;
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    public void release(Connection connection) {
        if (connection == null) {
            return;
        }

        if (!linkedBlockingQueue.contains(connection)) {
            System.out.println(String.format("Connection %s is invalid to be release", connection));
            return;
        }
    /*
            if (!connec.containsKey(connection)) {
                return;
            }

            // either it throws or succeeds
            connectionStatusMap.compute(connection, (key, value) -> {
                if (value == Status.AVAILABLE) {
                    throw new IllegalStateException("Connection is in invalid state for release");
                }
                return Status.AVAILABLE;

            });
    */

        linkedBlockingQueue.add(connection);
        semaphore.release();
    }

    public interface Connection {
        void createConnection();
    }

    private static class DatabaseConnection implements Connection {

        volatile boolean isWorkCompleted = false;

        @Override
        public void createConnection() {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Created connection successfully");
            isWorkCompleted = true;
        }
    }

    private enum Status {
        ACQUIRED,
        AVAILABLE
    }
}
