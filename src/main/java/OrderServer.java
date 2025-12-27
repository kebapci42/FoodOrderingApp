import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderServer {

    private static final String APP_DIR = System.getProperty("user.home") + java.io.File.separator
            + ".food_ordering_app";
    private static final String DB_URL = "jdbc:sqlite:" + APP_DIR + java.io.File.separator + "food_ordering.db";

    public static void main(String[] args) {
        int port = 6000; // default
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port argument, using default: " + port);
            }
        }

        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("🟢 Order Server started on port " + port);
        System.out.println("   Database: " + DB_URL);
        System.out.println("═══════════════════════════════════════════════════════");

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\n📨 Client connected: " + clientSocket.getInetAddress());

                // Handle each client in a new thread for concurrent support
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String line = in.readLine();

            if ("ORDER".equals(line)) {
                processOrder(in, out);
            } else {
                // Legacy format - just print and acknowledge
                System.out.println("📦 Received (legacy): " + line);
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
                out.println("OK");
            }

            clientSocket.close();

        } catch (Exception e) {
            System.out.println("❌ Error handling client: " + e.getMessage());
        }
    }

    private static void processOrder(BufferedReader in, PrintWriter out) {
        try {
            int restaurantId = -1;
            double totalAmount = 0;
            List<OrderItem> items = new ArrayList<>();

            String line;
            while ((line = in.readLine()) != null) {
                if ("END_ORDER".equals(line)) {
                    break;
                }

                if (line.startsWith("RESTAURANT_ID:")) {
                    restaurantId = Integer.parseInt(line.substring(14));
                } else if (line.startsWith("TOTAL:")) {
                    totalAmount = Double.parseDouble(line.substring(6));
                } else if (line.startsWith("ITEM:")) {
                    // Format: ITEM:name|quantity|price
                    String[] parts = line.substring(5).split("\\|");
                    if (parts.length == 3) {
                        items.add(new OrderItem(
                                parts[0],
                                Integer.parseInt(parts[1]),
                                Double.parseDouble(parts[2])));
                    }
                }
            }

            // Store the order in database
            boolean success = storeOrder(restaurantId, totalAmount, items);

            if (success) {
                System.out.println("\n📦 New Order Stored:");
                System.out.println("   Restaurant ID: " + restaurantId);
                System.out.println("   Total: $" + String.format("%.2f", totalAmount));
                System.out.println("   Items:");
                for (OrderItem item : items) {
                    System.out.println("     • " + item.name + " x" + item.quantity +
                            " ($" + String.format("%.2f", item.price * item.quantity) + ")");
                }
                System.out.println("───────────────────────────────────────────────────────");
                out.println("OK:Order stored successfully");
            } else {
                out.println("ERROR:Failed to store order");
            }

        } catch (Exception e) {
            System.out.println("❌ Error processing order: " + e.getMessage());
            out.println("ERROR:" + e.getMessage());
        }
    }

    private static boolean storeOrder(int restaurantId, double totalAmount, List<OrderItem> items) {
        String insertOrder = "INSERT INTO orders(date, total_amount, restaurant_id) VALUES(?,?,?)";
        String insertOrderItem = "INSERT INTO order_items(order_id, food_name, quantity, price) VALUES(?,?,?,?)";

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateStr = now.format(formatter);

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
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
                    for (OrderItem item : items) {
                        pstmtItem.setInt(1, orderId);
                        pstmtItem.setString(2, item.name);
                        pstmtItem.setInt(3, item.quantity);
                        pstmtItem.setDouble(4, item.price);
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

    // Simple inner class for order items
    private static class OrderItem {
        String name;
        int quantity;
        double price;

        OrderItem(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }
    }
}
