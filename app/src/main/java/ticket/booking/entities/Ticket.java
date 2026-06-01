package ticket.booking.entities;

import java.time.ZonedDateTime;

public class Ticket {
    private String ticketID;
    private String userID;
    private String source;
    private String destination;
    private ZonedDateTime dateOfTravel;
    private Train train;
}
