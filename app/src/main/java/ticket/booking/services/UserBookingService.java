package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserBookingService {

    private final User user;
    private List<User> userList;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String USERS_FILE_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService(User user1) throws IOException {
        this.user = user1;
        loadUserListFromFile();
    }

    public void loadUserListFromFile() throws IOException {
        userList = objectMapper.readValue(USERS_FILE_PATH, new TypeReference<List<User>>() {});
    }

    public  boolean loginUser() {
        Optional<User> foundUser = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        return foundUser.isPresent();
    }

    public boolean signUp(User user1) {
        try {
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        }catch (IOException ex){
            return Boolean.FALSE;
        }
    }

    public void saveUserListToFile() throws IOException{
        File userFile = new File(USERS_FILE_PATH);
        objectMapper.writeValue(userFile, userList);
    }

    public void fetchBookings(){
        Optional<User> userFetched = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        userFetched.ifPresent(User::printTickets);
    }

    public Boolean cancelBooking(String ticketID){
        Scanner s = new Scanner(System.in);
        System.out.println("Enter the ticket id to cancel");
        ticketID = s.next();

        if (ticketID == null || ticketID.isEmpty()){
            System.out.println("Ticket ID cannot be null or empty.");
            return Boolean.FALSE;
        }

        if (user.getTicketsBooked() == null){
            System.out.println("User has no booked tickets");
            return Boolean.FALSE;
        }

        String finalTicketID = ticketID;
        Boolean removed = user.getTicketsBooked().removeIf(ticket ->
                (ticket.getTicketId() != null) && ticket.getTicketId().equals(finalTicketID));

        if (removed) {
            System.out.println("Ticket with ID " + ticketID + " has been canceled.");
            return Boolean.TRUE;
        }else{
            System.out.println("No ticket found with ID " + ticketID);
            return Boolean.FALSE;
        }
    }
}
