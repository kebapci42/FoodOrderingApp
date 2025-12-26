import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;
import java.net.Socket;

public class DatabaseManager {
    private static final String APP_DIR = System.getProperty("user.home") + java.io.File.separator
            + ".food_ordering_app";
    private static final String DB_URL = "jdbc:sqlite:" + APP_DIR + java.io.File.separator + "food_ordering.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void initializeDatabase() {
        java.io.File directory = new java.io.File(APP_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

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
            stmt.execute(sqlRestaurants);
            stmt.execute(sqlFood);

            // Create orders table with restaurant_id
            String sqlOrders = "CREATE TABLE IF NOT EXISTS orders (\n"
                    + " id integer PRIMARY KEY,\n"
                    + " date text NOT NULL,\n"
                    + " total_amount real,\n"
                    + " restaurant_id integer,\n"
                    + " FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)\n"
                    + ");";
            stmt.execute(sqlOrders);

            String sqlOrderItems = "CREATE TABLE IF NOT EXISTS order_items (\n"
                    + " id integer PRIMARY KEY,\n"
                    + " order_id integer,\n"
                    + " food_name text,\n"
                    + " quantity integer,\n"
                    + " price real,\n"
                    + " FOREIGN KEY (order_id) REFERENCES orders (id)\n"
                    + ");";
            stmt.execute(sqlOrderItems);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static int addRestaurant(String name) {
        String sql = "INSERT INTO restaurants(name) VALUES(?)";
        int id = -1;

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return id;
    }

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

    // NEW: Update food item
    public static void updateFood(int foodId, String name, String type, double price) {
        String sql = "UPDATE food SET name = ?, type = ?, price = ? WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, foodId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

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

    // NEW: Get restaurant by ID
    public static Restaurant getRestaurantById(int id) {
        String sql = "SELECT id, name FROM restaurants WHERE id = ?";
        Restaurant restaurant = null;

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                restaurant = new Restaurant(rs.getInt("id"), rs.getString("name"));
                restaurant.setMenu(getMenuForRestaurant(id));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return restaurant;
    }

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

                Food food = new Food(name, type, price);
                food.setId(id);
                menu.addFood(food);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return menu;
    }

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

    public static void deleteRestaurant(int restaurantId) {
        String sqlFood = "DELETE FROM food WHERE restaurant_id = ?";
        String sqlRestaurant = "DELETE FROM restaurants WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sqlFood)) {
            pstmt.setInt(1, restaurantId);
            pstmt.executeUpdate();

            PreparedStatement pstmt2 = conn.prepareStatement(sqlRestaurant);
            pstmt2.setInt(1, restaurantId);
            pstmt2.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // MODIFIED: placeOrder now tracks restaurant_id
    public static void placeOrder(List<BasketItem> items, double totalAmount) {
        if (items.isEmpty())
            return;

        // Determine restaurant_id from first item
        int restaurantId = getRestaurantIdForFood(items.get(0).getFood().getName());

        String insertOrder = "INSERT INTO orders(date, total_amount, restaurant_id) VALUES(?,?,?)";
        String insertOrderItem = "INSERT INTO order_items(order_id, food_name, quantity, price) VALUES(?,?,?,?)";

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateStr = now.format(formatter);

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            int orderId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, dateStr);
                pstmt.setDouble(2, totalAmount);
                pstmt.setInt(3, restaurantId);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    }
                }
            }

            if (orderId != -1) {
                try (PreparedStatement pstmtItem = conn.prepareStatement(insertOrderItem)) {
                    for (BasketItem item : items) {
                        pstmtItem.setInt(1, orderId);
                        pstmtItem.setString(2, item.getFood().getName());
                        pstmtItem.setInt(3, item.getQuantity());
                        pstmtItem.setDouble(4, item.getFood().getPrice());
                        pstmtItem.addBatch();
                    }
                    pstmtItem.executeBatch();
                }
                conn.commit();
                sendOrderToServer(items, totalAmount);

            } else {
                conn.rollback();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    // NEW: Get restaurant_id for a food item by name
    private static int getRestaurantIdForFood(String foodName) {
        String sql = "SELECT restaurant_id FROM food WHERE name = ? LIMIT 1";
        int restaurantId = -1;

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, foodName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                restaurantId = rs.getInt("restaurant_id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return restaurantId;
    }

    public static Food getFoodByName(String foodName) {
        String sql = "SELECT id, name, type, price FROM food WHERE name = ?";
        Food food = null;

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, foodName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                food = new Food(rs.getString("name"), rs.getString("type"), rs.getDouble("price"));
                food.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return food;
    }

    public static List<BasketItem> getOrderItems(int orderId) {
        List<BasketItem> items = new ArrayList<>();
        String sql = "SELECT food_name, quantity FROM order_items WHERE order_id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String foodName = rs.getString("food_name");
                int quantity = rs.getInt("quantity");

                Food food = getFoodByName(foodName);
                if (food != null) {
                    items.add(new BasketItem(food, quantity));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return items;
    }

    public static List<Order> getOrderHistory() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id, date, total_amount FROM orders ORDER BY date DESC";

        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("date");
                double total = rs.getDouble("total_amount");

                String itemsDesc = getOrderItemsDescription(id);

                orders.add(new Order(id, date, total, itemsDesc));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return orders;
    }

    private static String getOrderItemsDescription(int orderId) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT food_name, quantity FROM order_items WHERE order_id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append(rs.getString("food_name"))
                        .append(" x")
                        .append(rs.getInt("quantity"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return sb.toString();
    }

    // NEW: Get orders for a specific restaurant
    public static List<RestaurantOrder> getOrdersForRestaurant(int restaurantId) {
        List<RestaurantOrder> orders = new ArrayList<>();
        String sql = "SELECT id, date, total_amount FROM orders WHERE restaurant_id = ? ORDER BY date DESC";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, restaurantId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("date");
                double total = rs.getDouble("total_amount");
                String itemsDesc = getOrderItemsDescription(id);

                orders.add(new RestaurantOrder(id, date, total, itemsDesc));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return orders;
    }

    public static void clearDatabase() {
        String sqlFood = "DELETE FROM food";
        String sqlRestaurants = "DELETE FROM restaurants";
        String sqlOrderItems = "DELETE FROM order_items";
        String sqlOrders = "DELETE FROM orders";

        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sqlOrderItems);
            stmt.execute(sqlFood);
            stmt.execute(sqlOrders);
            stmt.execute(sqlRestaurants);
            System.out.println("Database cleared successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void sendOrderToServer(List<BasketItem> items, double totalAmount) {
        String serverIP = "127.0.0.1"; // localhost
        int port = 6000;

        try (Socket socket = new Socket(serverIP, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("Total Amount: $" + totalAmount);

            for (BasketItem item : items) {
                out.println(
                        item.getFood().getName() +
                                " x" + item.getQuantity() +
                                " ($" + item.getTotalPrice() + ")");
            }

        } catch (Exception e) {
            System.out.println("⚠ Could not send order to server");
        }
    }

}
