import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FoodOrderingGUI extends JFrame {

    private static final Color BG_COLOR = new Color(245, 247, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color PRIMARY = new Color(52, 152, 219);
    private static final Color ACCENT = new Color(46, 204, 113);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private JList<Restaurant> restaurantList;
    private DefaultListModel<Restaurant> restaurantModel;

    private JList<Food> foodList;
    private DefaultListModel<Food> foodModel;

    private JList<String> basketList;
    private DefaultListModel<String> basketModel;
    private List<BasketItem> basketItems;

    private JTextField quantityField;
    private JLabel totalLabel;

    public FoodOrderingGUI() {
        setTitle("Food Ordering App");
        setSize(1200, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(12, 12));

        basketItems = new ArrayList<>();

        initUI();
        loadRestaurants();
    }

    private void initUI() {

        /* ===== TOP TITLE ===== */
        JLabel title = new JLabel("Food Ordering System");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        add(title, BorderLayout.NORTH);

        /* ===== RESTAURANTS ===== */
        restaurantModel = new DefaultListModel<>();
        restaurantList = new JList<>(restaurantModel);
        styleList(restaurantList, PRIMARY);

        restaurantList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadMenuForSelectedRestaurant();
            }
        });

        JPanel leftCard = createCard(new JScrollPane(restaurantList));
        leftCard.setPreferredSize(new Dimension(250, 0));
        add(leftCard, BorderLayout.WEST);

        /* ===== MENU ===== */
        foodModel = new DefaultListModel<>();
        foodList = new JList<>(foodModel);
        styleList(foodList, ACCENT);

        JPanel centerCard = createCard(new JScrollPane(foodList));
        add(centerCard, BorderLayout.CENTER);

        /* ===== BASKET ===== */
        basketModel = new DefaultListModel<>();
        basketList = new JList<>(basketModel);
        styleList(basketList, new Color(231, 76, 60));

        JPanel basketPanel = new JPanel(new BorderLayout());
        basketPanel.setBackground(CARD_COLOR);
        basketPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel basketTitle = new JLabel("🛒 Basket");
        basketTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        basketTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        basketPanel.add(basketTitle, BorderLayout.NORTH);
        basketPanel.add(new JScrollPane(basketList), BorderLayout.CENTER);

        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(ACCENT);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        basketPanel.add(totalLabel, BorderLayout.SOUTH);

        JPanel rightCard = createCard(basketPanel);
        rightCard.setPreferredSize(new Dimension(300, 0));
        add(rightCard, BorderLayout.EAST);

        /* ===== ADD TO BASKET ===== */
        JPanel bottomCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        bottomCard.setBackground(CARD_COLOR);
        bottomCard.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setFont(NORMAL_FONT);

        quantityField = new JTextField("1", 5);
        styleField(quantityField);

        JButton addToBasketButton = new JButton("➕ Add to Basket");
        addToBasketButton.setBackground(ACCENT);
        addToBasketButton.setForeground(Color.WHITE);
        addToBasketButton.setFocusPainted(false);
        addToBasketButton.setFont(NORMAL_FONT);
        addToBasketButton.addActionListener(e -> addToBasket());

        JButton clearBasketButton = new JButton("🗑 Clear Basket");
        clearBasketButton.setBackground(new Color(231, 76, 60));
        clearBasketButton.setForeground(Color.WHITE);
        clearBasketButton.setFocusPainted(false);
        clearBasketButton.setFont(NORMAL_FONT);
        clearBasketButton.addActionListener(e -> clearBasket());

        JButton deleteFoodButton = new JButton("❌ Delete Food");
        deleteFoodButton.setBackground(new Color(192, 57, 43));
        deleteFoodButton.setForeground(Color.WHITE);
        deleteFoodButton.setFocusPainted(false);
        deleteFoodButton.setFont(NORMAL_FONT);
        deleteFoodButton.addActionListener(e -> deleteFood());

        JButton deleteRestaurantButton = new JButton("❌ Delete Restaurant");
        deleteRestaurantButton.setBackground(new Color(142, 68, 173));
        deleteRestaurantButton.setForeground(Color.WHITE);
        deleteRestaurantButton.setFocusPainted(false);
        deleteRestaurantButton.setFont(NORMAL_FONT);
        deleteRestaurantButton.addActionListener(e -> deleteRestaurant());

        bottomCard.add(qtyLabel);
        bottomCard.add(quantityField);
        bottomCard.add(addToBasketButton);
        bottomCard.add(clearBasketButton);
        bottomCard.add(deleteFoodButton);
        bottomCard.add(deleteRestaurantButton);

        add(bottomCard, BorderLayout.SOUTH);
    }

    private JPanel createCard(JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(new Color(230, 230, 230))));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void styleList(JList<?> list, Color selectColor) {
        list.setFont(NORMAL_FONT);
        list.setFixedCellHeight(40);
        list.setSelectionBackground(selectColor);
        list.setSelectionForeground(Color.WHITE);
        list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void styleField(JTextField field) {
        field.setFont(NORMAL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    private void loadRestaurants() {
        restaurantModel.clear();
        List<Restaurant> restaurants = DatabaseManager.getAllRestaurants();
        for (Restaurant r : restaurants) {
            restaurantModel.addElement(r);
        }
    }

    private void loadMenuForSelectedRestaurant() {
        foodModel.clear();
        Restaurant selected = restaurantList.getSelectedValue();
        if (selected == null)
            return;

        for (Food food : selected.getMenu().getFoodItems()) {
            foodModel.addElement(food);
        }
    }

    private void addToBasket() {
        Food selectedFood = foodList.getSelectedValue();
        if (selectedFood == null) {
            JOptionPane.showMessageDialog(this, "Please select a food item first!");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0!");
                return;
            }

            BasketItem item = new BasketItem(selectedFood, quantity);
            basketItems.add(item);
            updateBasketDisplay();
            quantityField.setText("1");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity!");
        }
    }

    private void clearBasket() {
        basketItems.clear();
        updateBasketDisplay();
    }

    private void updateBasketDisplay() {
        basketModel.clear();
        double total = 0.0;

        for (BasketItem item : basketItems) {
            basketModel.addElement(item.toString());
            total += item.getTotalPrice();
        }

        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private void deleteFood() {
        Food selectedFood = foodList.getSelectedValue();
        if (selectedFood == null) {
            JOptionPane.showMessageDialog(this, "Please select a food item to delete!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete '" + selectedFood.getName() + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseManager.deleteFood(selectedFood.getId());
            Restaurant selectedRestaurant = restaurantList.getSelectedValue();
            loadRestaurants();
            if (selectedRestaurant != null) {
                restaurantList.setSelectedValue(selectedRestaurant, true);
            }
        }
    }

    private void deleteRestaurant() {
        Restaurant selectedRestaurant = restaurantList.getSelectedValue();
        if (selectedRestaurant == null) {
            JOptionPane.showMessageDialog(this, "Please select a restaurant to delete!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete '" + selectedRestaurant.getName() + "' and all its food items?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseManager.deleteRestaurant(selectedRestaurant.getId());
            loadRestaurants();
        }
    }

    private static class BasketItem {
        private Food food;
        private int quantity;

        public BasketItem(Food food, int quantity) {
            this.food = food;
            this.quantity = quantity;
        }

        public double getTotalPrice() {
            return food.getPrice() * quantity;
        }

        @Override
        public String toString() {
            return String.format("%dx %s ($%.2f)", quantity, food.getName(), getTotalPrice());
        }
    }
}
