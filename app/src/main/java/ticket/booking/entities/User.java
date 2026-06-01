package ticket.booking.entities;

import java.util.List;

public class User {
    private String userID;
    private String name;
    private String password;
    private String hashedPassword;
    private List<Ticket> ticketsBooked;
}
