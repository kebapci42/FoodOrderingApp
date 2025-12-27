import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class DatabaseManager {
    private static final String APP_DIR = System.getProperty("user.home") + java.io.File.separator
            + ".food_ordering_app";
    private static final String DB_URL = "jdbc:sqlite:" + APP_DIR + java.io.File.separator + "food_ordering.db";
    private static int port;

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void initializeDatabase(int portNumber) {
        java.io.File directory = new java.io.File(APP_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        port = portNumber;

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

            // ═══════════════════════════════════════════════════════════════
            // SCHEMA MIGRATION: Add restaurant_id column if missing
            // ═══════════════════════════════════════════════════════════════
            migrateSchema(conn);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Migrate schema to add missing columns
     */
    private static void migrateSchema(Connection conn) {
        try {
            // Check if restaurant_id column exists in orders table
            boolean hasRestaurantId = false;
            ResultSet rs = conn.getMetaData().getColumns(null, null, "orders", "restaurant_id");
            if (rs.next()) {
                hasRestaurantId = true;
            }
            rs.close();

            if (!hasRestaurantId) {
                System.out.println("Migrating schema: Adding restaurant_id to orders table...");
                Statement stmt = conn.createStatement();
                stmt.execute("ALTER TABLE orders ADD COLUMN restaurant_id integer");
                stmt.close();
                System.out.println("Schema migration complete.");
            }
        } catch (SQLException e) {
            // Column might already exist or other issue
            System.out.println("Schema migration note: " + e.getMessage());
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

    // ═══════════════════════════════════════════════════════════════════
    // PLACE ORDER - Now uses server for database operations
    // ═══════════════════════════════════════════════════════════════════
    public static boolean placeOrder(List<BasketItem> items, double totalAmount) {
        if (items.isEmpty())
            return false;

        // Get restaurant_id from first item
        int restaurantId = getRestaurantIdForFood(items.get(0).getFood().getName());

        // Try to send order to server first
        String serverResponse = sendOrderToServer(items, totalAmount, restaurantId);

        if (serverResponse != null && serverResponse.startsWith("OK")) {
            System.out.println("✓ Order processed by server");
            return true;
        }

        // Fallback: Store locally if server unavailable
        System.out.println("⚠ Server unavailable, storing order locally...");
        return placeOrderLocally(items, totalAmount, restaurantId);
    }

    /**
     * Split basket items by restaurant and create separate orders
     * Returns number of orders created, or -1 on failure
     */
    public static int placeOrdersSplitByRestaurant(List<BasketItem> items, double totalAmount) {
        if (items.isEmpty())
            return -1;

        // Group items by restaurant
        java.util.Map<Integer, List<BasketItem>> itemsByRestaurant = new java.util.HashMap<>();

        for (BasketItem item : items) {
            int restaurantId = getRestaurantIdForFood(item.getFood().getName());
            itemsByRestaurant.computeIfAbsent(restaurantId, k -> new ArrayList<>()).add(item);
        }

        int ordersCreated = 0;

        for (java.util.Map.Entry<Integer, List<BasketItem>> entry : itemsByRestaurant.entrySet()) {
            int restaurantId = entry.getKey();
            List<BasketItem> restaurantItems = entry.getValue();

            // Calculate subtotal for this restaurant's items
            double subtotal = restaurantItems.stream()
                    .mapToDouble(BasketItem::getTotalPrice)
                    .sum();

            // Try server first, then fallback to local
            String serverResponse = sendOrderToServer(restaurantItems, subtotal, restaurantId);

            boolean success;
            if (serverResponse != null && serverResponse.startsWith("OK")) {
                success = true;
            } else {
                success = placeOrderLocally(restaurantItems, subtotal, restaurantId);
            }

            if (success) {
                ordersCreated++;
            }
        }

        return ordersCreated;
    }

    /**
     * Store order in local database (fallback when server unavailable)
     */
    private static boolean placeOrderLocally(List<BasketItem> items, double totalAmount, int restaurantId) {
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
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            return false;
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

    // ═══════════════════════════════════════════════════════════════════
    // SERVER COMMUNICATION - Enhanced with response handling
    // ═══════════════════════════════════════════════════════════════════
    private static String sendOrderToServer(List<BasketItem> items, double totalAmount, int restaurantId) {
        String serverIP = "127.0.0.1";

        try (Socket socket = new Socket(serverIP, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send order data in a parseable format
            out.println("ORDER");
            out.println("RESTAURANT_ID:" + restaurantId);
            out.println("TOTAL:" + totalAmount);
            out.println("ITEMS:" + items.size());

            for (BasketItem item : items) {
                out.println("ITEM:" + item.getFood().getName() + "|" +
                        item.getQuantity() + "|" +
                        item.getFood().getPrice());
            }
            out.println("END_ORDER");

            // Wait for server response
            socket.setSoTimeout(5000); // 5 second timeout
            String response = in.readLine();
            return response;

        } catch (Exception e) {
            System.out.println("⚠ Could not send order to server: " + e.getMessage());
            return null;
        }
    }
}
