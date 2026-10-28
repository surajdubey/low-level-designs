package connectionpool;

import java.util.*;
import java.util.concurrent.*;

public class ConnectionPoolManager {
    private final Semaphore semaphore;
    private final Map<Connection, Status> connectionStatusMap = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Connection> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    public ConnectionPoolManager(int numberOfConnections) {
        semaphore = new Semaphore(numberOfConnections);
        for (int i = 0; i < numberOfConnections; i++) {
            submitTask();
        }
    }

    private void submitTask() {
        Connection connection = new DatabaseConnection();
        connection.createConnection();
        connectionStatusMap.put(connection, Status.AVAILABLE);
        concurrentLinkedQueue.add(connection);
    }

    public Connection getWorkingConnection() throws InterruptedException {
        semaphore.acquire();
        Connection connection = concurrentLinkedQueue.remove();
        connectionStatusMap.put(connection, Status.ACQUIRED);
        return connection;
    }

    public void release(Connection connection) {
        if (connection == null) {
            return;
        }

        if (!connectionStatusMap.containsKey(connection)) {
            return;
        }

        Status newStatus = connectionStatusMap.compute(connection, (key, value) -> {
            if (value == Status.ACQUIRED) {
                return Status.AVAILABLE;
            }

            return Status.ANOTHER_STATE;
        });
        if (newStatus == Status.AVAILABLE) {
            concurrentLinkedQueue.add(connection);
            semaphore.release();
        }
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
        AVAILABLE,
        ANOTHER_STATE
    }
}
