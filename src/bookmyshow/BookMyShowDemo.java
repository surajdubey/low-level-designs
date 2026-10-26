package bookmyshow;

import java.util.ArrayList;
import java.util.List;

public class BookMyShowDemo {
    public static void main(String[] args) {
        System.out.println("Hello World");

        final BookingManagerUsingAtomicCAS bookingManager = new BookingManagerUsingAtomicCAS();

        Event event = new Event();
        event.id = "Test event";
        event.totalTickets = 3;

        bookingManager.createEvent(event);

        List<Thread> allThreads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int finalI = i;
            Thread t1 = new Thread(() -> {
                for (int j = 0; j < 5; j++) {
                    try {
                        User user = new User();
                        user.id = "user" + j + "thread" + finalI;
                        Booking booking = bookingManager.performBooking(event, user);
                        System.out.println("Available tickets are " + event.availableTickets);
                        System.out.println("Booking completed " + booking);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            });

            allThreads.add(t1);
        }

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
    }
}
