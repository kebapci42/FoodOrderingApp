import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing Database...");
        DatabaseManager.initializeDatabase();

        System.out.println("Clearing existing data...");
        DatabaseManager.clearDatabase();

        System.out.println("Importing data from CSV...");
        importDataFromCSV("data.csv");

        System.out.println("\n--- Restaurants from Database ---");
        List<Restaurant> restaurants = DatabaseManager.getAllRestaurants();

        SwingUtilities.invokeLater(() -> {
            new FoodOrderingGUI().setVisible(true);
        });

        if (restaurants.isEmpty()) {
            System.out.println("No restaurants found in database.");
        } else {
            for (Restaurant r : restaurants) {
                System.out.println(r);
                System.out.println(r.getMenu());
                System.out.println("-----------------------------");
            }
        }
    }

    private static void importDataFromCSV(String csvFile) {
        String line;
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy);

                if (data.length == 4) {
                    String restaurantName = data[0].trim();
                    String foodName = data[1].trim();
                    String foodType = data[2].trim();
                    double price = Double.parseDouble(data[3].trim());

                    int restaurantId = getOrCreateRestaurant(restaurantName);
                    DatabaseManager.addFood(foodName, foodType, price, restaurantId);
                }
            }
            System.out.println("Data import completed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getOrCreateRestaurant(String name) {
        List<Restaurant> restaurants = DatabaseManager.getAllRestaurants();
        for (Restaurant r : restaurants) {
            if (r.getName().equalsIgnoreCase(name)) {
                return r.getId();
            }
        }

        DatabaseManager.addRestaurant(name);

        restaurants = DatabaseManager.getAllRestaurants();
        for (Restaurant r : restaurants) {
            if (r.getName().equalsIgnoreCase(name)) {
                return r.getId();
            }
        }
        return -1;
    }
}
