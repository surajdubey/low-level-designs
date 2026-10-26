package bookmyshow;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BookingManager {

    private final Map<String, Event> events = new HashMap<>();

    // if bookings are canceled, we will not track it here for simplicity
    private final Map<String, List<Booking>> bookingsForEvents = new ConcurrentHashMap<>();

    private final Map<String, LinkedBlockingQueue<Booking>> bookingForEventsUsingQueue =
            new ConcurrentHashMap<>();

    private final Map<String, AtomicInteger> consumedCapacityPerEvent = new ConcurrentHashMap<>();

//    private final Object lock = new Object();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


    // book ticket for a provided event
    // if error occurs, this method returns exception
    // otherwise valid Booking instance is returned
    public Booking performBooking(Event event, User user) throws Exception {
        // creating these instances ahead of time:
        Ticket ticket = new Ticket();
        ticket.id = UUID.randomUUID().toString();

        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                ticket.id,
                event.id,
                user.id,
                "COMPLETED"
        );


        consumedCapacityPerEvent.computeIfAbsent(event.id, (key) -> new AtomicInteger(0));
        AtomicInteger consumedCapacity = consumedCapacityPerEvent.get(event.id);

        if (consumedCapacity.get() < event.totalTickets) {
            if (consumedCapacity.addAndGet(1) <= event.totalTickets) {
                // reservation can go through
                bookingsForEvents.get(event.id).add(booking);
                return booking;
            }
        }
        throw new Exception("Can not book tickets, tickets over for user " + user.id);

    }
/*
    // book ticket for a provided event
    // if error occurs, this method returns exception
    // otherwise valid Booking instance is returned
    public Booking performBooking(Event event, User user) throws Exception {
        // creating these instances ahead of time:
        Ticket ticket = new Ticket();
        ticket.id = UUID.randomUUID().toString();

        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                ticket.id,
                event.id,
                user.id,
                "COMPLETED"
        );


        bookingsForEvents.computeIfAbsent(event.id, (key) -> new ArrayList<>());
        List<Booking> allBookings = bookingsForEvents.get(event.id);
        synchronized (allBookings) {
            if (event.totalTickets > allBookings.size()) {
                // we can go ahead and book tickets now
                allBookings.add(booking);

                // ideally this should be done async in separate non-blocking thread, but
                // doing it here for simplicity
                event.availableTickets = event.totalTickets - allBookings.size();
                return booking;
            }
            throw new Exception("Can not book tickets, tickets over for user " + user.id);
        }
    }
*/

/*
    // book ticket for a provided event
    // if error occurs, this method returns exception
    // otherwise valid Booking instance is returned
    public Booking performBooking(Event event, User user) throws Exception {
        // creating these instances ahead of time:
        Ticket ticket = new Ticket();
        ticket.id = UUID.randomUUID().toString();

        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                ticket.id,
                event.id,
                user.id,
                "COMPLETED"
        );


        bookingForEventsUsingQueue.computeIfAbsent(event.id,
                (key) -> new LinkedBlockingQueue<Booking>(event.totalTickets)
        );
        LinkedBlockingQueue<Booking> allBookings = bookingForEventsUsingQueue.get(event.id);
        boolean wasSuccessful = allBookings.offer(booking);

        if (wasSuccessful) {
            // ideally this should be done async in separate non-blocking thread, but
            // doing it here for simplicity
            event.availableTickets = event.totalTickets - allBookings.size();
            return booking;
        }

        throw new Exception("Can not book tickets, tickets over for user " + user.id);

    }*/

    public void createEvent(Event event) {
        bookingsForEvents.put(event.id, new ArrayList<>());
    }

    public Event getEventDetails(String eventId) {
        return new Event();
    }
}
