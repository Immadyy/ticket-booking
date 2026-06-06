package ticket.booking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class UserBookingService {

    private ObjectMapper objectMapper = new ObjectMapper();
    private List<User> userList;
    private User user;
    private final String USER_FILE_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    // Constructor for login (with user)
    public UserBookingService(User user) throws IOException {
        this.user = user;
        loadUserListFromFile();
    }

    // Constructor for signup (without user)
    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    private void loadUserListFromFile() throws IOException {
        File usersFile = new File(USER_FILE_PATH);

        // Ensure parent directories exist
        if (!usersFile.getParentFile().exists()) {
            usersFile.getParentFile().mkdirs();
        }

        // If file doesn't exist or is empty, initialize empty list
        if (!usersFile.exists() || usersFile.length() == 0) {
            userList = new ArrayList<>();
            saveUserListToFile(); // Save empty list to create the file
            return;
        }

        try {
            userList = objectMapper.readValue(usersFile, new TypeReference<List<User>>() {});
            System.out.println("✅ Loaded " + userList.size() + " users from database");
        } catch (IOException e) {
            System.err.println("❌ Error reading users file: " + e.getMessage());
            userList = new ArrayList<>();
            // Create a backup of corrupted file
            File backup = new File(USER_FILE_PATH + ".backup");
            usersFile.renameTo(backup);
            System.out.println("⚠️ Corrupted file backed up as: " + backup.getName());
        }
    }

    private void saveUserListToFile() throws IOException {
        File usersFile = new File(USER_FILE_PATH);
        // Ensure parent directories exist
        if (!usersFile.getParentFile().exists()) {
            usersFile.getParentFile().mkdirs();
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(usersFile, userList);
        System.out.println("💾 Saved " + userList.size() + " users to database");
    }

    public Boolean loginUser() {
        if (userList == null || userList.isEmpty()) {
            System.out.println("❌ No users registered yet. Please sign up first.");
            return false;
        }

        Optional<User> foundUser = userList.stream()
                .filter(user1 -> user1.getName().equals(user.getName())
                        && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword()))
                .findFirst();

        if (foundUser.isPresent()) {
            // Update the current user with the stored user data (including tickets)
            User storedUser = foundUser.get();
            this.user.setUserId(storedUser.getUserId());
            this.user.setHashedPassword(storedUser.getHashedPassword());
            this.user.setTicketsBooked(storedUser.getTicketsBooked());
            System.out.println("✅ Welcome back " + user.getName() + "!");
            return true;
        } else {
            System.out.println("❌ Invalid username or password");
            return false;
        }
    }

    public Boolean signUp(User user1) {
        try {
            // Check if user already exists
            boolean userExists = userList.stream()
                    .anyMatch(existingUser -> existingUser.getName().equals(user1.getName()));

            if (userExists) {
                System.out.println("❌ Username already exists! Please choose a different username.");
                return false;
            }

            userList.add(user1);
            saveUserListToFile();
            System.out.println("✅ User " + user1.getName() + " registered successfully!");
            return true;
        } catch (IOException ex) {
            System.err.println("❌ Error during sign up: " + ex.getMessage());
            return false;
        }
    }

    public void fetchBookings() {
        if (user == null) {
            System.out.println("❌ Please login first to view bookings");
            return;
        }

        Optional<User> userFetched = userList.stream()
                .filter(user1 -> user1.getName().equals(user.getName())
                        && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword()))
                .findFirst();

        if (userFetched.isPresent()) {
            List<Ticket> tickets = userFetched.get().getTicketsBooked();
            if (tickets == null || tickets.isEmpty()) {
                System.out.println("📭 No bookings found. Please book a ticket first.");
            } else {
                System.out.println("📋 You have " + tickets.size() + " booking(s):");
                System.out.println("-".repeat(40));
                userFetched.get().printTickets();
            }
        } else {
            System.out.println("❌ User not found. Please login again.");
        }
    }

    // FIXED: Completely rewritten cancelBooking method
    public Boolean cancelBooking(String ticketId) {
        if (user == null) {
            System.out.println("❌ Please login first to cancel booking");
            return false;
        }

        Scanner scanner = new Scanner(System.in);

        // If ticketId is not provided, ask for it
        if (ticketId == null || ticketId.isEmpty()) {
            System.out.print("Enter the ticket ID to cancel: ");
            ticketId = scanner.nextLine().trim();
        }

        if (ticketId == null || ticketId.isEmpty()) {
            System.out.println("❌ Ticket ID cannot be empty");
            return false;
        }

        // Find the user in the list
        Optional<User> userFromList = userList.stream()
                .filter(u -> u.getName().equals(user.getName()))
                .findFirst();

        if (!userFromList.isPresent()) {
            System.out.println("❌ User not found");
            return false;
        }

        User foundUser = userFromList.get();
        List<Ticket> tickets = foundUser.getTicketsBooked();

        if (tickets == null || tickets.isEmpty()) {
            System.out.println("❌ You have no bookings to cancel");
            return false;
        }

        // Find and remove the ticket
        Iterator<Ticket> iterator = tickets.iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            Ticket ticket = iterator.next();
            if (ticket.getTicketId().equals(ticketId)) {
                iterator.remove();
                removed = true;
                break;
            }
        }

        if (removed) {
            try {
                // Update the user's tickets
                foundUser.setTicketsBooked(tickets);
                // Update the user in the list
                int index = userList.indexOf(foundUser);
                if (index != -1) {
                    userList.set(index, foundUser);
                }
                // Save to file
                saveUserListToFile();
                // Update current user object
                this.user.setTicketsBooked(tickets);
                System.out.println("✅ Ticket " + ticketId + " cancelled successfully!");
                System.out.println("💵 Refund will be processed within 5-7 business days");
                return true;
            } catch (IOException e) {
                System.err.println("❌ Error saving cancellation: " + e.getMessage());
                return false;
            }
        } else {
            System.out.println("❌ No ticket found with ID: " + ticketId);
            return false;
        }
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            List<Train> trains = trainService.searchTrains(source, destination);
            return trains;
        } catch (IOException ex) {
            System.err.println("❌ Error searching trains: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        if (train == null) {
            System.out.println("❌ No train selected");
            return new ArrayList<>();
        }

        List<List<Integer>> seats = train.getSeats();
        if (seats == null || seats.isEmpty()) {
            System.out.println("❌ Seat information not available");
            return new ArrayList<>();
        }

        return seats;
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        if (train == null) {
            System.out.println("❌ No train selected for booking");
            return false;
        }

        try {
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();

            // Validate row and seat indices
            if (seats == null || seats.isEmpty()) {
                System.out.println("❌ Seat layout not available for this train");
                return false;
            }

            if (row < 0 || row >= seats.size()) {
                System.out.println("❌ Invalid row number. Please choose between 0 and " + (seats.size() - 1));
                return false;
            }

            if (seat < 0 || seat >= seats.get(row).size()) {
                System.out.println("❌ Invalid seat number. Please choose between 0 and " + (seats.get(row).size() - 1));
                return false;
            }

            // Check if seat is available
            if (seats.get(row).get(seat) == 0) {
                // Book the seat
                seats.get(row).set(seat, 1);
                train.setSeats(seats);
                trainService.addTrain(train);

                // Create a ticket for the booking
                Ticket newTicket = new Ticket(
                        UUID.randomUUID().toString(),
                        user.getUserId(),
                        "Station", // You should get this from the journey
                        "Station", // You should get this from the journey
                        new Date(System.currentTimeMillis()).toString(),
                        train
                );

                // Add ticket to user's bookings
                if (user.getTicketsBooked() == null) {
                    user.setTicketsBooked(new ArrayList<>());
                }
                user.getTicketsBooked().add(newTicket);

                // Update user in the list and save
                updateUserInFile();

                System.out.println("✅ Seat " + row + "," + seat + " booked successfully!");
                System.out.println("🎫 Ticket ID: " + newTicket.getTicketId());
                System.out.println("📧 Ticket details sent to your registered email");
                return true;
            } else {
                System.out.println("❌ Seat " + row + "," + seat + " is already booked");
                return false;
            }
        } catch (IOException ex) {
            System.err.println("❌ Error booking seat: " + ex.getMessage());
            return false;
        }
    }

    // Helper method to update user in file after booking
    private void updateUserInFile() throws IOException {
        Optional<User> userFromList = userList.stream()
                .filter(u -> u.getUserId().equals(user.getUserId()))
                .findFirst();

        if (userFromList.isPresent()) {
            int index = userList.indexOf(userFromList.get());
            userList.set(index, user);
            saveUserListToFile();
        }
    }

    // Helper method to get all users (for debugging)
    public List<User> getAllUsers() {
        return userList;
    }

    // Helper method to get user count
    public int getUserCount() {
        return userList != null ? userList.size() : 0;
    }
}