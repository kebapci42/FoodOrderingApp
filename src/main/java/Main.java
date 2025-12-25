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

        // Cache restaurant IDs to minimize DB queries
        java.util.Map<String, Integer> restaurantCache = new java.util.HashMap<>();
        for (Restaurant r : DatabaseManager.getAllRestaurants()) {
            restaurantCache.put(r.getName().toLowerCase(), r.getId());
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy);

                if (data.length == 4) {
                    String restaurantName = data[0].trim();
                    String foodName = data[1].trim();
                    String foodType = data[2].trim();
                    String priceStr = data[3].trim();

                    if (restaurantName.isEmpty() || foodName.isEmpty() || foodType.isEmpty() || priceStr.isEmpty()) {
                        System.err.println("Skipping incomplete line: " + line);
                        continue;
                    }

                    try {
                        double price = Double.parseDouble(priceStr);

                        int restaurantId;
                        String cacheKey = restaurantName.toLowerCase();

                        if (restaurantCache.containsKey(cacheKey)) {
                            restaurantId = restaurantCache.get(cacheKey);
                        } else {
                            restaurantId = DatabaseManager.addRestaurant(restaurantName);
                            restaurantCache.put(cacheKey, restaurantId);
                        }

                        DatabaseManager.addFood(foodName, foodType, price, restaurantId);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping line with invalid price: " + line);
                    }
                }
            }
            System.out.println("Data import completed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
