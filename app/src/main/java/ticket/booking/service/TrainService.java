package ticket.booking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainService {

    private List<Train> trainList;
    private ObjectMapper objectMapper = new ObjectMapper();

    // FIXED: Correct path matching your project structure
    private static final String TRAIN_DB_PATH = "app/src/main/java/ticket/booking/localDb/trains.json";

    public TrainService() throws IOException {
        loadTrainsFromFile();
    }

    private void loadTrainsFromFile() throws IOException {
        File trainsFile = new File(TRAIN_DB_PATH);

        // Ensure parent directories exist
        if (!trainsFile.getParentFile().exists()) {
            trainsFile.getParentFile().mkdirs();
        }

        // If file doesn't exist or is empty, initialize with sample trains
        if (!trainsFile.exists() || trainsFile.length() == 0) {
            trainList = createSampleTrains();
            saveTrainListToFile();
            return;
        }

        try {
            trainList = objectMapper.readValue(trainsFile, new TypeReference<List<Train>>() {});
        } catch (IOException e) {
            System.err.println("Error reading trains file: " + e.getMessage());
            trainList = createSampleTrains();
            saveTrainListToFile();
        }
    }

    private List<Train> createSampleTrains() {
        List<Train> trains = new ArrayList<>();

        // Create sample train 1: Rajdhani Express from Delhi to Mumbai
        Train train1 = new Train();
        train1.setTrainId("TRAIN001");
        train1.setTrainNo("12951");

        // Create stations list
        List<String> stations1 = new ArrayList<>();
        stations1.add("delhi");
        stations1.add("jaipur");
        stations1.add("ahmedabad");
        stations1.add("mumbai");
        train1.setStations(stations1);

        // Create station times
        java.util.Map<String, String> stationTimes1 = new java.util.HashMap<>();
        stationTimes1.put("delhi", "16:00");
        stationTimes1.put("jaipur", "20:30");
        stationTimes1.put("ahmedabad", "04:00");
        stationTimes1.put("mumbai", "08:30");
        train1.setStationTimes(stationTimes1);

        // Create seats (3 rows, 3 columns for simplicity)
        List<List<Integer>> seats1 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                row.add(0); // 0 means available
            }
            seats1.add(row);
        }
        train1.setSeats(seats1);
        trains.add(train1);

        // Create sample train 2: Shatabdi Express from Delhi to Chandigarh
        Train train2 = new Train();
        train2.setTrainId("TRAIN002");
        train2.setTrainNo("12011");

        List<String> stations2 = new ArrayList<>();
        stations2.add("delhi");
        stations2.add("ambala");
        stations2.add("chandigarh");
        train2.setStations(stations2);

        java.util.Map<String, String> stationTimes2 = new java.util.HashMap<>();
        stationTimes2.put("delhi", "07:00");
        stationTimes2.put("ambala", "10:30");
        stationTimes2.put("chandigarh", "11:30");
        train2.setStationTimes(stationTimes2);

        List<List<Integer>> seats2 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                row.add(0);
            }
            seats2.add(row);
        }
        train2.setSeats(seats2);
        trains.add(train2);

        // Create sample train 3: Duronto Express from Mumbai to Kolkata
        Train train3 = new Train();
        train3.setTrainId("TRAIN003");
        train3.setTrainNo("12259");

        List<String> stations3 = new ArrayList<>();
        stations3.add("mumbai");
        stations3.add("pune");
        stations3.add("nagpur");
        stations3.add("kolkata");
        train3.setStations(stations3);

        java.util.Map<String, String> stationTimes3 = new java.util.HashMap<>();
        stationTimes3.put("mumbai", "21:00");
        stationTimes3.put("pune", "23:30");
        stationTimes3.put("nagpur", "06:00");
        stationTimes3.put("kolkata", "18:00");
        train3.setStationTimes(stationTimes3);

        List<List<Integer>> seats3 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                row.add(0);
            }
            seats3.add(row);
        }
        train3.setSeats(seats3);
        trains.add(train3);

        System.out.println("Created " + trains.size() + " sample trains");
        return trains;
    }

    public List<Train> searchTrains(String source, String destination) {
        if (trainList == null || trainList.isEmpty()) {
            System.out.println("No trains available in the database");
            return new ArrayList<>();
        }

        // Convert to lowercase for case-insensitive comparison
        String sourceLower = source.toLowerCase();
        String destLower = destination.toLowerCase();

        List<Train> filteredTrains = trainList.stream()
                .filter(train -> validTrain(train, sourceLower, destLower))
                .collect(Collectors.toList());

        System.out.println("Found " + filteredTrains.size() + " trains from " + source + " to " + destination);
        return filteredTrains;
    }

    public void addTrain(Train newTrain) {
        if (trainList == null) {
            trainList = new ArrayList<>();
        }

        // Check if a train with the same trainId already exists
        Optional<Train> existingTrain = trainList.stream()
                .filter(train -> train.getTrainId() != null && train.getTrainId().equalsIgnoreCase(newTrain.getTrainId()))
                .findFirst();

        if (existingTrain.isPresent()) {
            // If a train with the same trainId exists, update it instead of adding a new one
            updateTrain(newTrain);
        } else {
            // Otherwise, add the new train to the list
            trainList.add(newTrain);
            saveTrainListToFile();
        }
    }

    public void updateTrain(Train updatedTrain) {
        // Find the index of the train with the same trainId
        OptionalInt index = IntStream.range(0, trainList.size())
                .filter(i -> trainList.get(i).getTrainId() != null &&
                        trainList.get(i).getTrainId().equalsIgnoreCase(updatedTrain.getTrainId()))
                .findFirst();

        if (index.isPresent()) {
            // If found, replace the existing train with the updated one
            trainList.set(index.getAsInt(), updatedTrain);
            saveTrainListToFile();
            System.out.println("Train updated successfully");
        } else {
            // If not found, treat it as adding a new train
            addTrain(updatedTrain);
        }
    }

    private void saveTrainListToFile() {
        try {
            File trainsFile = new File(TRAIN_DB_PATH);
            // Ensure parent directories exist
            if (!trainsFile.getParentFile().exists()) {
                trainsFile.getParentFile().mkdirs();
            }
            objectMapper.writeValue(trainsFile, trainList);
            System.out.println("Trains saved successfully to: " + TRAIN_DB_PATH);
        } catch (IOException e) {
            System.err.println("Error saving trains file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validTrain(Train train, String source, String destination) {
        List<String> stationOrder = train.getStations();

        if (stationOrder == null || stationOrder.isEmpty()) {
            return false;
        }

        // Convert station names to lowercase for case-insensitive comparison
        List<String> lowerCaseStations = stationOrder.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        int sourceIndex = lowerCaseStations.indexOf(source);
        int destinationIndex = lowerCaseStations.indexOf(destination);

        boolean isValid = sourceIndex != -1 && destinationIndex != -1 && sourceIndex < destinationIndex;

        if (!isValid) {
            System.out.println("Train " + train.getTrainNo() + " does not run from " + source + " to " + destination);
        }

        return isValid;
    }

    // Helper method to get all trains (for debugging)
    public List<Train> getAllTrains() {
        return trainList;
    }

    // Helper method to check if trains are loaded
    public int getTrainCount() {
        return trainList != null ? trainList.size() : 0;
    }
}