package bookmyshow;

import java.time.Instant;

public class Event {
    String id;
    String name;
    String description;
    Instant createdOn;
    Instant eventDateAndTime;

    int totalTickets;
    volatile int availableTickets;

}
