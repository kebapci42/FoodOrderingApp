import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize Database
        System.out.println("Initializing Database...");
        DatabaseManager.initializeDatabase();

        // 2. Import Data from CSV
        System.out.println("Importing data from CSV...");
        importDataFromCSV("data.csv"); // Assuming data.csv is in project root, and we run from root

        // 3. Retrieve and Display Data from Database
        System.out.println("\n--- Restaurants from Database ---");
        List<Restaurant> restaurants = DatabaseManager.getAllRestaurants();
        
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
            // Skip header
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy);
                
                if (data.length == 4) {
                    String restaurantName = data[0].trim();
                    String foodName = data[1].trim();
                    String foodType = data[2].trim();
                    double price = Double.parseDouble(data[3].trim());

                    // Add Restaurant (DatabaseManager should handle duplicates or we check here)
                    // For simplicity, we'll try to add, and DB manager should ideally handle "INSERT OR IGNORE" or similar
                    // But our current DB manager just inserts. To prevent duplicates in this simple version, 
                    // we might want to check if it exists first, but let's stick to the current simple implementation
                    // and rely on the fact that we are just testing. 
                    // Ideally, DatabaseManager.addRestaurant should check existence.
                    
                    // Let's improve DatabaseManager to return ID or check existence, 
                    // but for now, let's just add. 
                    // NOTE: This will create duplicate restaurants if run multiple times without clearing DB.
                    // We will fix this by checking existence in a real app, but for this task:
                    
                    // A better approach for this script:
                    // 1. Get or Create Restaurant
                    int restaurantId = getOrCreateRestaurant(restaurantName);
                    
                    // 2. Add Food
                    DatabaseManager.addFood(foodName, foodType, price, restaurantId);
                }
            }
            System.out.println("Data import completed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Helper to handle restaurant creation/retrieval
    private static int getOrCreateRestaurant(String name) {
        // This is inefficient (fetching all every time), but simple for this task
        List<Restaurant> restaurants = DatabaseManager.getAllRestaurants();
        for (Restaurant r : restaurants) {
            if (r.getName().equalsIgnoreCase(name)) {
                return r.getId();
            }
        }
        
        // Not found, create it
        DatabaseManager.addRestaurant(name);
        
        // Fetch again to get the ID (since addRestaurant doesn't return ID in current impl)
        restaurants = DatabaseManager.getAllRestaurants();
        for (Restaurant r : restaurants) {
            if (r.getName().equalsIgnoreCase(name)) {
                return r.getId();
            }
        }
        return -1; // Should not happen
    }
}
