package bookmyshow;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BookingManager {

    private final Map<String, Event> events = new HashMap<>();

    // if bookings are canceled, we will not track it here for simplicity
    private final Map<String, ConcurrentLinkedDeque<Booking>> bookingsForEvents = new ConcurrentHashMap<>();

    private final Map<String, AtomicInteger> consumedCapacityPerEvent = new ConcurrentHashMap<>();


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
                bookingsForEvents.get(event.id).offer(booking);
                return booking;
            }
        }
        throw new Exception("Can not book tickets, tickets over for user " + user.id);

    }

    public void createEvent(Event event) {
        bookingsForEvents.put(event.id, new ConcurrentLinkedDeque<>());
    }

    public Event getEventDetails(String eventId) {
        return new Event();
    }
}
