package bookmyshow;

public record Booking(
        String id,
        String ticketId,
        String eventId,
        String userId,
        String status

) {
}
