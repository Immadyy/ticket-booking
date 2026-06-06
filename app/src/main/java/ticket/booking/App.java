package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.service.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class App {

    public static void main(String[] args) {
        System.out.println("🚂 Welcome to IRCTC Train Booking System 🚂");
        Scanner scanner = new Scanner(System.in);
        int option = 0;
        UserBookingService userBookingService = null;

        // FIXED: Declare trainSelectedForBooking outside the loop to persist between iterations
        Train trainSelectedForBooking = null;

        // Initialize the service
        try {
            userBookingService = new UserBookingService();
            System.out.println("✅ System initialized successfully!");
        } catch (IOException ex) {
            System.out.println("❌ Failed to initialize system: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        while (option != 7) {
            System.out.println("\n" + "=".repeat(50));

            // Show currently selected train if any
            if (trainSelectedForBooking != null) {
                System.out.println("🎯 Currently selected train: " + trainSelectedForBooking.getTrainNo());
            } else {
                System.out.println("🎯 No train selected. Please search and select a train (Option 4)");
            }
            System.out.println("-".repeat(50));

            System.out.println("Please choose an option:");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit the App");
            System.out.println("=".repeat(50));
            System.out.print("Enter your choice: ");

            // Handle invalid input
            try {
                option = scanner.nextInt();
            } catch (Exception e) {
                System.out.println("❌ Invalid input! Please enter a number between 1-7");
                scanner.next(); // Clear the invalid input
                continue;
            }

            switch (option) {
                case 1: // Sign up
                    System.out.println("\n📝 New User Registration");
                    System.out.print("Enter username: ");
                    String nameToSignUp = scanner.next();
                    System.out.print("Enter password: ");
                    String passwordToSignUp = scanner.next();

                    User userToSignup = new User(
                            nameToSignUp,
                            passwordToSignUp,
                            UserServiceUtil.hashPassword(passwordToSignUp),
                            new ArrayList<>(),
                            UUID.randomUUID().toString()
                    );

                    Boolean signUpSuccess = userBookingService.signUp(userToSignup);
                    if (signUpSuccess) {
                        System.out.println("✅ Sign up successful! Please login to continue.");
                    } else {
                        System.out.println("❌ Sign up failed. Please try again.");
                    }
                    break;

                case 2: // Login
                    System.out.println("\n🔐 User Login");
                    System.out.print("Enter username: ");
                    String nameToLogin = scanner.next();
                    System.out.print("Enter password: ");
                    String passwordToLogin = scanner.next();

                    User userToLogin = new User(
                            nameToLogin,
                            passwordToLogin,
                            UserServiceUtil.hashPassword(passwordToLogin),
                            new ArrayList<>(),
                            UUID.randomUUID().toString()
                    );

                    try {
                        userBookingService = new UserBookingService(userToLogin);
                        Boolean loginSuccess = userBookingService.loginUser();
                        if (loginSuccess) {
                            System.out.println("✅ Login successful! Welcome " + nameToLogin + "!");
                        } else {
                            System.out.println("❌ Login failed! Invalid username or password.");
                        }
                    } catch (IOException ex) {
                        System.out.println("❌ Login failed: " + ex.getMessage());
                    }
                    break;

                case 3: // Fetch Bookings
                    System.out.println("\n📋 Your Bookings:");
                    System.out.println("-".repeat(30));
                    userBookingService.fetchBookings();
                    break;

                case 4: // Search Trains
                    System.out.println("\n🔍 Search Trains");
                    System.out.print("Enter source station: ");
                    String source = scanner.next().toLowerCase(); // Convert to lowercase
                    System.out.print("Enter destination station: ");
                    String dest = scanner.next().toLowerCase(); // Convert to lowercase

                    List<Train> trains = userBookingService.getTrains(source, dest);

                    // Check if trains list is empty
                    if (trains == null || trains.isEmpty()) {
                        System.out.println("\n❌ No trains found from " + source + " to " + dest);
                        System.out.println("Available trains run between:");
                        System.out.println("  • Delhi → Mumbai");
                        System.out.println("  • Delhi → Chandigarh");
                        System.out.println("  • Mumbai → Kolkata");
                        break;
                    }

                    // Display available trains
                    System.out.println("\n✅ Found " + trains.size() + " train(s):");
                    System.out.println("-".repeat(60));
                    int index = 1;
                    for (Train t : trains) {
                        System.out.println("\n🚂 Train " + index + ":");
                        System.out.println("   Train ID: " + t.getTrainId());
                        System.out.println("   Train Number: " + t.getTrainNo());
                        System.out.println("   Route and Timings:");
                        if (t.getStationTimes() != null) {
                            for (Map.Entry<String, String> entry : t.getStationTimes().entrySet()) {
                                System.out.println("      📍 " + entry.getKey() + " → " + entry.getValue());
                            }
                        }
                        index++;
                    }

                    // Handle train selection with proper index
                    System.out.print("\nSelect a train (1-" + trains.size() + "): ");
                    int trainChoice;
                    try {
                        trainChoice = scanner.nextInt();
                        // Subtract 1 to convert from 1-based to 0-based index
                        if (trainChoice >= 1 && trainChoice <= trains.size()) {
                            trainSelectedForBooking = trains.get(trainChoice - 1);
                            System.out.println("\n✅ Selected: Train " + trainSelectedForBooking.getTrainNo());
                            System.out.println("💡 You can now book seats using option 5");
                        } else {
                            System.out.println("❌ Invalid selection! Please choose between 1 and " + trains.size());
                            trainSelectedForBooking = null;
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Invalid input! Please enter a number.");
                        scanner.next(); // Clear invalid input
                        trainSelectedForBooking = null;
                    }
                    break;

                case 5: // Book a Seat
                    System.out.println("\n🎫 Book a Seat");

                    // Check if a train was selected
                    if (trainSelectedForBooking == null) {
                        System.out.println("❌ No train selected! Please search and select a train first (Option 4).");
                        break;
                    }

                    // Fetch and display seats
                    List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);
                    if (seats == null || seats.isEmpty()) {
                        System.out.println("❌ Seat information not available for this train.");
                        break;
                    }

                    System.out.println("\n📊 Seat Layout (0=Available, 1=Booked):");
                    System.out.println("-".repeat(30));
                    int rowNum = 0;
                    for (List<Integer> row : seats) {
                        System.out.print("Row " + rowNum + ": ");
                        for (Integer val : row) {
                            String display = (val == 0) ? "⬜" : "⬛";
                            System.out.print(display + " ");
                        }
                        System.out.println();
                        rowNum++;
                    }

                    System.out.println("\n⬜ Available seat | ⬛ Booked seat");

                    // Get seat selection
                    try {
                        System.out.print("\nEnter row number (0-" + (seats.size() - 1) + "): ");
                        int row = scanner.nextInt();
                        System.out.print("Enter column number (0-" + (seats.get(0).size() - 1) + "): ");
                        int col = scanner.nextInt();

                        System.out.println("Booking your seat...");
                        Boolean booked = userBookingService.bookTrainSeat(trainSelectedForBooking, row, col);

                        if (booked != null && booked) {
                            System.out.println("✅ Seat booked successfully! Enjoy your journey! 🎉");
                            // Optionally reset train selection after booking
                            // trainSelectedForBooking = null; // Uncomment if you want to clear after booking
                        } else {
                            System.out.println("❌ Cannot book this seat. It may be already booked or invalid.");
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Invalid seat selection! Please enter valid numbers.");
                        scanner.next(); // Clear invalid input
                    }
                    break;

                case 6: // Cancel my Booking
                    System.out.println("\n❌ Cancel Booking");
                    userBookingService.cancelBooking(null);
                    break;

                case 7: // Exit
                    System.out.println("\n👋 Thank you for using IRCTC Train Booking System!");
                    System.out.println("Have a safe journey! 🚂");
                    break;

                default:
                    System.out.println("\n❌ Invalid option! Please choose between 1 and 7.");
                    break;
            }
        }
        scanner.close();
    }
}