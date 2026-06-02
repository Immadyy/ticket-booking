package ticket.booking.entities;

import java.time.ZonedDateTime;

public class Ticket {
    private String ticketID;
    private String userID;
    private String source;
    private String destination;
    private String dateOfTravel;
    private Train train;

    public Ticket(String ticketId, String userId, String source, String destination, String dateOfTravel, Train train){
        this.ticketID = ticketId;
        this.userID = userId;
        this.source = source;
        this.destination = destination;
        this.dateOfTravel = dateOfTravel;
        this.train = train;
    }

    public String getTicketInfo(){
        return String.format("Ticket ID: %s belongs to User %s from %s to %s on %s", ticketID, userID, source, destination, dateOfTravel);
    }

    public String getTicketId(){
        return ticketID;
    }

    public void setTicketId(String ticketId){
        this.ticketID = ticketId;
    }

    public String getSource(){
        return source;
    }

    public void setSource(String source){
        this.source = source;
    }

    public String getUserId(){
        return userID;
    }

    public void setUserId(String userId){
        this.userID = userId;
    }

    public String getDestination(){
        return destination;
    }

    public void setDestination(String destination){
        this.destination = destination;
    }

    public String getDateOfTravel(){
        return dateOfTravel;
    }

    public void setDateOfTravel(String dateOfTravel){
        this.dateOfTravel = dateOfTravel;
    }

    public Train getTrain(){
        return train;
    }

    public void setTrain(Train train){
        this.train = train;
    }

}