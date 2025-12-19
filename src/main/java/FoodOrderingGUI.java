import javax.swing.*;
import java.awt.*;
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

    private JTextField foodNameField;
    private JTextField foodPriceField;
    private JComboBox<String> foodTypeBox;

    public FoodOrderingGUI() {
        setTitle("🍽 Food Ordering App");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(12, 12));

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

        /* ===== ADD FOOD ===== */
        JPanel bottomCard = new JPanel(new GridBagLayout());
        bottomCard.setBackground(CARD_COLOR);
        bottomCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        foodNameField = new JTextField();
        foodPriceField = new JTextField();
        foodTypeBox = new JComboBox<>(new String[]{"Drink", "Soup", "Main Course", "Salad"});

        styleField(foodNameField);
        styleField(foodPriceField);
        foodTypeBox.setFont(NORMAL_FONT);

        JButton addFoodButton = new JButton("➕ Add Food");
        addFoodButton.setBackground(PRIMARY);
        addFoodButton.setForeground(Color.WHITE);
        addFoodButton.setFocusPainted(false);
        addFoodButton.addActionListener(e -> addFood());

        gbc.gridx = 0; gbc.gridy = 0;
        bottomCard.add(new JLabel("Food Name"), gbc);
        gbc.gridx = 1;
        bottomCard.add(new JLabel("Type"), gbc);
        gbc.gridx = 2;
        bottomCard.add(new JLabel("Price"), gbc);

        gbc.gridy = 1; gbc.gridx = 0;
        bottomCard.add(foodNameField, gbc);
        gbc.gridx = 1;
        bottomCard.add(foodTypeBox, gbc);
        gbc.gridx = 2;
        bottomCard.add(foodPriceField, gbc);
        gbc.gridx = 3;
        bottomCard.add(addFoodButton, gbc);

        add(bottomCard, BorderLayout.SOUTH);
    }

    private JPanel createCard(JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(new Color(230, 230, 230))
        ));
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
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
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
        if (selected == null) return;

        for (Food food : selected.getMenu().getFoodItems()) {
            foodModel.addElement(food);
        }
    }

    private void addFood() {
        Restaurant selected = restaurantList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a restaurant first!");
            return;
        }

        try {
            double price = Double.parseDouble(foodPriceField.getText());
            DatabaseManager.addFood(
                    foodNameField.getText(),
                    (String) foodTypeBox.getSelectedItem(),
                    price,
                    selected.getId()
            );
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price!");
            return;
        }

        loadRestaurants();
        restaurantList.setSelectedValue(selected, true);
        foodNameField.setText("");
        foodPriceField.setText("");
    }
}
