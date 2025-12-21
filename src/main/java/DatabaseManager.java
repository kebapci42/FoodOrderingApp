import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String APP_DIR = System.getProperty("user.home") + java.io.File.separator
            + ".food_ordering_app";
    private static final String DB_URL = "jdbc:sqlite:" + APP_DIR + java.io.File.separator + "food_ordering.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            // Ensure the application directory exists
            java.io.File directory = new java.io.File(APP_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // db parameters
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void initializeDatabase() {
        // SQL statement for creating a new table
        String sqlRestaurants = "CREATE TABLE IF NOT EXISTS restaurants (\n"
                + " id integer PRIMARY KEY,\n"
                + " name text NOT NULL\n"
                + ");";

        String sqlFood = "CREATE TABLE IF NOT EXISTS food (\n"
                + " id integer PRIMARY KEY,\n"
                + " name text NOT NULL,\n"
                + " type text NOT NULL,\n"
                + " price real,\n"
                + " restaurant_id integer,\n"
                + " FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)\n"
                + ");";

        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            // create new tables
            stmt.execute(sqlRestaurants);
            stmt.execute(sqlFood);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Method to add a restaurant
    public static void addRestaurant(String name) {
        String sql = "INSERT INTO restaurants(name) VALUES(?)";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Method to add food
    public static void addFood(String name, String type, double price, int restaurantId) {
        String sql = "INSERT INTO food(name, type, price, restaurant_id) VALUES(?,?,?,?)";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, restaurantId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Method to get all restaurants with their menus
    public static List<Restaurant> getAllRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();
        String sql = "SELECT id, name FROM restaurants";

        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Restaurant restaurant = new Restaurant(rs.getInt("id"), rs.getString("name"));
                restaurant.setMenu(getMenuForRestaurant(restaurant.getId()));
                restaurants.add(restaurant);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return restaurants;
    }

    // Helper method to get menu for a restaurant
    private static Menu getMenuForRestaurant(int restaurantId) {
        Menu menu = new Menu();
        String sql = "SELECT id, name, type, price FROM food WHERE restaurant_id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, restaurantId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                double price = rs.getDouble("price");

                Food food = null;
                switch (type) {
                    case "Drink":
                        food = new Drink(name, price);
                        break;
                    case "Soup":
                        food = new Soup(name, price);
                        break;
                    case "Main Course":
                        food = new MainCourse(name, price);
                        break;
                    case "Salad":
                        food = new Salad(name, price);
                        break;
                }

                if (food != null) {
                    food.setId(id);
                    menu.addFood(food);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return menu;
    }

    // Method to delete a food item
    public static void deleteFood(int foodId) {
        String sql = "DELETE FROM food WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, foodId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Method to delete a restaurant (and all its food items)
    public static void deleteRestaurant(int restaurantId) {
        String sqlFood = "DELETE FROM food WHERE restaurant_id = ?";
        String sqlRestaurant = "DELETE FROM restaurants WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sqlFood)) {
            // Delete all food items first
            pstmt.setInt(1, restaurantId);
            pstmt.executeUpdate();

            // Then delete the restaurant
            PreparedStatement pstmt2 = conn.prepareStatement(sqlRestaurant);
            pstmt2.setInt(1, restaurantId);
            pstmt2.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Method to clear all data from database
    public static void clearDatabase() {
        String sqlFood = "DELETE FROM food";
        String sqlRestaurants = "DELETE FROM restaurants";

        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            // Delete food first due to foreign key constraint
            stmt.execute(sqlFood);
            stmt.execute(sqlRestaurants);
            System.out.println("Database cleared successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
