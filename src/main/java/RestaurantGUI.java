import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;

public class RestaurantGUI extends JFrame {

    // ═══════════════════════════════════════════════════════════════════
    // THEME STATE
    // ═══════════════════════════════════════════════════════════════════
    private boolean isNightMode = false;

    // ═══════════════════════════════════════════════════════════════════
    // LIGHT THEME COLORS
    // ═══════════════════════════════════════════════════════════════════
    private static final Color LIGHT_BG = new Color(255, 250, 240);
    private static final Color LIGHT_CARD = new Color(255, 255, 255);
    private static final Color LIGHT_TEXT = new Color(101, 67, 33);
    private static final Color LIGHT_TEXT_SECONDARY = new Color(150, 140, 130);

    // ═══════════════════════════════════════════════════════════════════
    // NIGHT THEME COLORS
    // ═══════════════════════════════════════════════════════════════════
    private static final Color NIGHT_BG = new Color(25, 25, 35);
    private static final Color NIGHT_CARD = new Color(40, 40, 55);
    private static final Color NIGHT_TEXT = new Color(255, 245, 235);
    private static final Color NIGHT_TEXT_SECONDARY = new Color(180, 175, 170);

    // ═══════════════════════════════════════════════════════════════════
    // ACCENT COLORS
    // ═══════════════════════════════════════════════════════════════════
    private static final Color APPETIZING_ORANGE = new Color(255, 107, 53);
    private static final Color SPICY_RED = new Color(220, 53, 69);
    private static final Color TOMATO_RED = new Color(255, 99, 71);
    private static final Color OLIVE_GREEN = new Color(107, 142, 35);

    // ═══════════════════════════════════════════════════════════════════
    // CROSS-PLATFORM FONTS
    // ═══════════════════════════════════════════════════════════════════
    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase();
    private static final boolean IS_MAC = OS_NAME.contains("mac");
    private static final boolean IS_WINDOWS = OS_NAME.contains("win");

    private static String getSerifFont() {
        if (IS_MAC || IS_WINDOWS) {
            return "Georgia";
        }
        return Font.SERIF;
    }

    private static String getSansSerifFont() {
        if (IS_MAC) {
            String[] macFonts = { ".SF NS Text", "SF Pro Text", "Helvetica Neue", "Helvetica" };
            for (String fontName : macFonts) {
                if (isFontAvailable(fontName)) {
                    return fontName;
                }
            }
            return Font.SANS_SERIF;
        } else if (IS_WINDOWS) {
            return "Segoe UI";
        }
        return Font.SANS_SERIF;
    }

    private static boolean isFontAvailable(String fontName) {
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String available : availableFonts) {
            if (available.equalsIgnoreCase(fontName)) {
                return true;
            }
        }
        return false;
    }

    private static final Font HEADER_FONT = new Font(getSerifFont(), Font.BOLD, 26);
    private static final Font TAGLINE_FONT = new Font(getSerifFont(), Font.ITALIC, 14);
    private static final Font SECTION_FONT = new Font(getSansSerifFont(), Font.BOLD, 16);
    private static final Font FOOD_NAME_FONT = new Font(getSansSerifFont(), Font.BOLD, 14);
    private static final Font FOOD_DETAIL_FONT = new Font(getSansSerifFont(), Font.PLAIN, 12);
    private static final Font BUTTON_FONT = new Font(getSansSerifFont(), Font.BOLD, 13);
    private static final Font PRICE_FONT = new Font(getSansSerifFont(), Font.BOLD, 15);

    // ═══════════════════════════════════════════════════════════════════
    // COMPONENTS
    // ═══════════════════════════════════════════════════════════════════
    private JPanel mainContent;
    private JPanel headerPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JButton themeToggleBtn;
    private JLabel restaurantNameLabel;

    private Restaurant currentRestaurant;
    private JList<Food> menuList;
    private DefaultListModel<Food> menuModel;
    private JPanel ordersPanel;
    private JLabel orderCountLabel;

    private Timer autoRefreshTimer;

    public RestaurantGUI() {
        setTitle("Delicious Bites - Restaurant Manager");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        selectRestaurant();
        if (currentRestaurant == null) {
            dispose();
            return;
        }

        initUI();
        loadMenu();
        loadOrders();
        applyTheme();
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(10000, e -> loadOrders()); // 10 seconds
        autoRefreshTimer.start();
    }

    @Override
    public void dispose() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
        super.dispose();
    }

    // ═══════════════════════════════════════════════════════════════════
    // THEME HELPERS
    // ═══════════════════════════════════════════════════════════════════
    private Color getBgColor() {
        return isNightMode ? NIGHT_BG : LIGHT_BG;
    }

    private Color getCardColor() {
        return isNightMode ? NIGHT_CARD : LIGHT_CARD;
    }

    private Color getTextColor() {
        return isNightMode ? NIGHT_TEXT : LIGHT_TEXT;
    }

    private Color getTextSecondaryColor() {
        return isNightMode ? NIGHT_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY;
    }

    private Color getSelectionBg() {
        return isNightMode ? new Color(70, 60, 50) : new Color(255, 237, 225);
    }

    private Color getBorderColor() {
        return isNightMode ? new Color(70, 70, 85) : new Color(240, 235, 228);
    }

    private void selectRestaurant() {
        List<Restaurant> restaurants = DatabaseManager.getAllRestaurants();
        if (restaurants.isEmpty()) {
            showThemedMessage("No restaurants in database!", "Error");
            return;
        }

        currentRestaurant = showRestaurantSelectionDialog(restaurants, "Select your restaurant:");
    }

    private void initUI() {
        // ═══════════════════════════════════════════════════════════════
        // HEADER
        // ═══════════════════════════════════════════════════════════════
        headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color startColor = isNightMode ? new Color(180, 70, 40) : APPETIZING_ORANGE;
                Color endColor = isNightMode ? new Color(150, 40, 50) : TOMATO_RED;
                GradientPaint gradient = new GradientPaint(0, 0, startColor, getWidth(), 0, endColor);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 30, 22, 30));
        headerPanel.setPreferredSize(new Dimension(0, 100));

        JPanel headerLeft = new JPanel();
        headerLeft.setOpaque(false);
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));

        restaurantNameLabel = new JLabel(currentRestaurant.getName() + " - Manager");
        restaurantNameLabel.setFont(HEADER_FONT);
        restaurantNameLabel.setForeground(Color.WHITE);
        restaurantNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel taglineLabel = new JLabel("Manage your menu and view incoming orders");
        taglineLabel.setFont(TAGLINE_FONT);
        taglineLabel.setForeground(new Color(255, 255, 255, 220));
        taglineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerLeft.add(restaurantNameLabel);
        headerLeft.add(Box.createVerticalStrut(8));
        headerLeft.add(taglineLabel);

        themeToggleBtn = new JButton("Night Mode") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        themeToggleBtn.setFont(BUTTON_FONT);
        themeToggleBtn.setPreferredSize(new Dimension(120, 35));
        themeToggleBtn.setFocusPainted(false);
        themeToggleBtn.setBorderPainted(false);
        themeToggleBtn.setContentAreaFilled(false);
        themeToggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeToggleBtn.addActionListener(e -> toggleTheme());

        JButton switchRestaurantBtn = new JButton("Switch Restaurant") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        switchRestaurantBtn.setFont(BUTTON_FONT);
        switchRestaurantBtn.setPreferredSize(new Dimension(150, 35));
        switchRestaurantBtn.setFocusPainted(false);
        switchRestaurantBtn.setBorderPainted(false);
        switchRestaurantBtn.setContentAreaFilled(false);
        switchRestaurantBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        switchRestaurantBtn.addActionListener(e -> switchRestaurant());

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);
        headerRight.add(switchRestaurantBtn);
        headerRight.add(themeToggleBtn);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // ═══════════════════════════════════════════════════════════════
        // MAIN CONTENT
        // ═══════════════════════════════════════════════════════════════
        mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ═══════════════════════════════════════════════════════════════
        // LEFT PANEL - Menu Management
        // ═══════════════════════════════════════════════════════════════
        menuModel = new DefaultListModel<>();
        menuList = new JList<>(menuModel);
        menuList.setCellRenderer(new FoodCardRenderer());
        menuList.setFixedCellHeight(70);
        menuList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane menuScroll = new JScrollPane(menuList);
        menuScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel menuContent = new JPanel(new BorderLayout(0, 10));
        menuContent.setOpaque(false);
        menuContent.add(menuScroll, BorderLayout.CENTER);

        // Menu action buttons
        JPanel menuActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        menuActions.setOpaque(false);

        JButton addFoodBtn = createStyledButton("Add Item", OLIVE_GREEN, Color.WHITE);
        addFoodBtn.addActionListener(e -> addFood());

        JButton editFoodBtn = createStyledButton("Edit Item", APPETIZING_ORANGE, Color.WHITE);
        editFoodBtn.addActionListener(e -> editFood());

        JButton deleteFoodBtn = createStyledButton("Delete Item", SPICY_RED, Color.WHITE);
        deleteFoodBtn.addActionListener(e -> deleteFood());

        menuActions.add(addFoodBtn);
        menuActions.add(editFoodBtn);
        menuActions.add(deleteFoodBtn);

        menuContent.add(menuActions, BorderLayout.SOUTH);

        leftPanel = createSectionPanel("Your Menu", menuContent, null);
        leftPanel.setPreferredSize(new Dimension(520, 0));
        mainContent.add(leftPanel, BorderLayout.CENTER);

        // ═══════════════════════════════════════════════════════════════
        // RIGHT PANEL - Orders
        // ═══════════════════════════════════════════════════════════════
        ordersPanel = new JPanel();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
        ordersPanel.setOpaque(false);

        JPanel ordersWrapper = new JPanel(new BorderLayout());
        ordersWrapper.setOpaque(false);
        ordersWrapper.add(ordersPanel, BorderLayout.NORTH);

        JScrollPane ordersScroll = new JScrollPane(ordersWrapper);
        ordersScroll.setBorder(BorderFactory.createEmptyBorder());
        ordersScroll.setOpaque(false);
        ordersScroll.getViewport().setOpaque(false);
        ordersScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel ordersContent = new JPanel(new BorderLayout(0, 10));
        ordersContent.setOpaque(false);
        ordersContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        orderCountLabel = new JLabel("0 orders");
        orderCountLabel.setFont(FOOD_DETAIL_FONT);
        ordersContent.add(orderCountLabel, BorderLayout.NORTH);
        ordersContent.add(ordersScroll, BorderLayout.CENTER);

        rightPanel = createSectionPanel("Incoming Orders", ordersContent, null);
        rightPanel.setPreferredSize(new Dimension(380, 0));
        mainContent.add(rightPanel, BorderLayout.EAST);

        add(mainContent, BorderLayout.CENTER);

        // Start timer
        startAutoRefresh();
    }

    // ═══════════════════════════════════════════════════════════════════
    // THEME
    // ═══════════════════════════════════════════════════════════════════
    private void toggleTheme() {
        isNightMode = !isNightMode;
        themeToggleBtn.setText(isNightMode ? "Light Mode" : "Night Mode");
        applyTheme();
    }

    private void applyTheme() {
        getContentPane().setBackground(getBgColor());
        mainContent.setBackground(getBgColor());
        menuList.setBackground(getBgColor());
        menuList.setSelectionBackground(getSelectionBg());
        orderCountLabel.setForeground(getTextSecondaryColor());

        loadOrders(); // Refresh order cards with new theme
        headerPanel.repaint();
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    // ═══════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ═══════════════════════════════════════════════════════════════════
    private JPanel createSectionPanel(String title, JComponent content, JComponent headerAction) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getCardColor());
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.setColor(getBorderColor());
                g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SECTION_FONT);
        titleLabel.setForeground(getTextColor());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 12, 0));

        if (headerAction != null) {
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.add(titleLabel, BorderLayout.WEST);
            header.add(headerAction, BorderLayout.EAST);
            panel.add(header, BorderLayout.NORTH);
        } else {
            panel.add(titleLabel, BorderLayout.NORTH);
        }

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hover ? bgColor.brighter() : bgColor;
                g2d.setColor(bg);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2d.setColor(fgColor);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(new Dimension(130, 38));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CUSTOM RENDERER
    // ═══════════════════════════════════════════════════════════════════
    private class FoodCardRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            JPanel card = new JPanel(new BorderLayout(12, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(isSelected ? getSelectionBg() : getCardColor());
                    g2d.fill(new RoundRectangle2D.Float(5, 3, getWidth() - 10, getHeight() - 6, 12, 12));
                    g2d.setColor(isSelected ? APPETIZING_ORANGE : getBorderColor());
                    g2d.draw(new RoundRectangle2D.Float(5, 3, getWidth() - 10, getHeight() - 6, 12, 12));
                }
            };
            card.setOpaque(false);
            card.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

            Food food = (Food) value;

            JPanel detailsPanel = new JPanel();
            detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
            detailsPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(food.getName());
            nameLabel.setFont(FOOD_NAME_FONT);
            nameLabel.setForeground(getTextColor());
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel typeLabel = new JLabel(food.getType());
            typeLabel.setFont(FOOD_DETAIL_FONT);
            typeLabel.setForeground(getTextSecondaryColor());
            typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            detailsPanel.add(nameLabel);
            detailsPanel.add(Box.createVerticalStrut(4));
            detailsPanel.add(typeLabel);

            JLabel priceLabel = new JLabel(String.format("$%.2f", food.getPrice()));
            priceLabel.setFont(PRICE_FONT);
            priceLabel.setForeground(OLIVE_GREEN);

            card.add(detailsPanel, BorderLayout.CENTER);
            card.add(priceLabel, BorderLayout.EAST);

            return card;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // DATA OPERATIONS
    // ═══════════════════════════════════════════════════════════════════
    private void loadMenu() {
        menuModel.clear();
        currentRestaurant = DatabaseManager.getRestaurantById(currentRestaurant.getId());
        if (currentRestaurant != null) {
            for (Food food : currentRestaurant.getMenu().getFoodItems()) {
                menuModel.addElement(food);
            }
        }
    }

    private void loadOrders() {
        ordersPanel.removeAll();
        List<RestaurantOrder> orders = DatabaseManager.getOrdersForRestaurant(currentRestaurant.getId());

        int totalOrders = orders.size();
        for (int i = 0; i < orders.size(); i++) {
            // orders are DESC (newest first), so reverse the numbering
            int displayNumber = totalOrders - i;
            ordersPanel.add(createOrderCard(orders.get(i), displayNumber));
            ordersPanel.add(Box.createVerticalStrut(10));
        }

        orderCountLabel.setText(orders.size() + (orders.size() == 1 ? " order" : " orders"));
        ordersPanel.revalidate();
        ordersPanel.repaint();
    }

    private JPanel createOrderCard(RestaurantOrder order, int displayNumber) {
        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setOpaque(true);
        card.setBackground(getCardColor());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBorderColor(), 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 120));

        // Header: Order ID and Date
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel orderIdLabel = new JLabel("Order #" + displayNumber);
        orderIdLabel.setFont(new Font(getSansSerifFont(), Font.BOLD, 14));
        orderIdLabel.setForeground(APPETIZING_ORANGE);

        JLabel dateLabel = new JLabel(order.getDate());
        dateLabel.setFont(FOOD_DETAIL_FONT);
        dateLabel.setForeground(getTextSecondaryColor());

        headerPanel.add(orderIdLabel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        // Items list
        JTextArea itemsArea = new JTextArea(order.getItemsDescription());
        itemsArea.setFont(FOOD_DETAIL_FONT);
        itemsArea.setForeground(getTextColor());
        itemsArea.setBackground(getCardColor());
        itemsArea.setEditable(false);
        itemsArea.setLineWrap(true);
        itemsArea.setWrapStyleWord(true);
        itemsArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // Total
        JLabel totalLabel = new JLabel(String.format("Total: $%.2f", order.getTotal()));
        totalLabel.setFont(PRICE_FONT);
        totalLabel.setForeground(OLIVE_GREEN);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(itemsArea, BorderLayout.CENTER);
        card.add(totalLabel, BorderLayout.SOUTH);

        return card;
    }

    private void addFood() {
        JDialog dialog = createFoodInputDialog("Add New Food Item", null);
        dialog.setVisible(true);
    }

    private void editFood() {
        Food selected = menuList.getSelectedValue();
        if (selected == null) {
            showThemedMessage("Please select a food item to edit!", "No Selection");
            return;
        }

        JDialog dialog = createFoodInputDialog("Edit Food Item", selected);
        dialog.setVisible(true);
    }

    private void deleteFood() {
        Food selected = menuList.getSelectedValue();
        if (selected == null) {
            showThemedMessage("Please select a food item to delete!", "No Selection");
            return;
        }

        showThemedConfirmation(
                "Delete '" + selected.getName() + "' from the menu?",
                "Confirm Delete",
                () -> {
                    DatabaseManager.deleteFood(selected.getId());
                    loadMenu();
                    showThemedMessage("Food item deleted!", "Success");
                });
    }

    private void switchRestaurant() {
        List<Restaurant> restaurants = DatabaseManager.getAllRestaurants();
        if (restaurants.isEmpty()) {
            showThemedMessage("No restaurants in database!", "Error");
            return;
        }

        Restaurant selected = showRestaurantSelectionDialog(restaurants, "Select restaurant to switch to:");

        if (selected != null && selected.getId() != currentRestaurant.getId()) {
            currentRestaurant = selected;
            restaurantNameLabel.setText(currentRestaurant.getName() + " - Manager");
            loadMenu();
            loadOrders();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // THEMED DIALOGS
    // ═══════════════════════════════════════════════════════════════════
    private Restaurant showRestaurantSelectionDialog(List<Restaurant> restaurants, String message) {
        JDialog dialog = new JDialog(this, "Restaurant Selection", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // Set dialog background
        dialog.getContentPane().setBackground(getBgColor());

        // Message label
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(SECTION_FONT);
        messageLabel.setForeground(getTextColor());
        messageLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        // Restaurant list
        DefaultListModel<Restaurant> listModel = new DefaultListModel<>();
        for (Restaurant r : restaurants) {
            listModel.addElement(r);
        }

        JList<Restaurant> restaurantList = new JList<>(listModel);
        restaurantList.setFont(FOOD_NAME_FONT);
        restaurantList.setBackground(getCardColor());
        restaurantList.setForeground(getTextColor());
        restaurantList.setSelectionBackground(getSelectionBg());
        restaurantList.setSelectionForeground(APPETIZING_ORANGE);
        restaurantList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        if (currentRestaurant != null) {
            restaurantList.setSelectedValue(currentRestaurant, true);
        } else {
            restaurantList.setSelectedIndex(0);
        }

        JScrollPane scrollPane = new JScrollPane(restaurantList);
        scrollPane.setBorder(BorderFactory.createLineBorder(getBorderColor()));
        scrollPane.getViewport().setBackground(getBgColor());

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(getBgColor());
        listPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        listPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(getCardColor());

        final Restaurant[] selectedRestaurant = { null };

        JButton selectBtn = createStyledButton("Select", OLIVE_GREEN, Color.WHITE);
        selectBtn.addActionListener(e -> {
            selectedRestaurant[0] = restaurantList.getSelectedValue();
            dialog.dispose();
        });

        JButton cancelBtn = createStyledButton("Cancel", SPICY_RED, Color.WHITE);
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(selectBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(messageLabel, BorderLayout.NORTH);
        dialog.add(listPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
        return selectedRestaurant[0];
    }

    private void showThemedMessage(String message, String title) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(350, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(getBgColor());

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(FOOD_NAME_FONT);
        messageLabel.setForeground(getTextColor());
        messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(getBgColor());

        JButton okBtn = createStyledButton("OK", OLIVE_GREEN, Color.WHITE);
        okBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(okBtn);

        dialog.add(messageLabel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showThemedConfirmation(String message, String title, Runnable onConfirm) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(400, 180);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(getBgColor());

        JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        messageLabel.setFont(FOOD_NAME_FONT);
        messageLabel.setForeground(getTextColor());
        messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(getBgColor());

        JButton yesBtn = createStyledButton("Yes", OLIVE_GREEN, Color.WHITE);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            onConfirm.run();
        });

        JButton noBtn = createStyledButton("No", SPICY_RED, Color.WHITE);
        noBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(yesBtn);
        buttonPanel.add(noBtn);

        dialog.add(messageLabel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JDialog createFoodInputDialog(String title, Food existingFood) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(getBgColor());

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBackground(getBgColor());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(FOOD_DETAIL_FONT);
        nameLabel.setForeground(getTextColor());

        JTextField nameField = new JTextField(20);
        nameField.setFont(FOOD_DETAIL_FONT);
        nameField.setBackground(getCardColor());
        nameField.setForeground(getTextColor());
        nameField.setCaretColor(getTextColor());
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBorderColor()),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(FOOD_DETAIL_FONT);
        typeLabel.setForeground(getTextColor());

        JTextField typeField = new JTextField(20);
        typeField.setFont(FOOD_DETAIL_FONT);
        typeField.setBackground(getCardColor());
        typeField.setForeground(getTextColor());
        typeField.setCaretColor(getTextColor());
        typeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBorderColor()),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JLabel priceLabel = new JLabel("Price:");
        priceLabel.setFont(FOOD_DETAIL_FONT);
        priceLabel.setForeground(getTextColor());

        JTextField priceField = new JTextField(20);
        priceField.setFont(FOOD_DETAIL_FONT);
        priceField.setBackground(getCardColor());
        priceField.setForeground(getTextColor());
        priceField.setCaretColor(getTextColor());
        priceField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBorderColor()),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        if (existingFood != null) {
            nameField.setText(existingFood.getName());
            typeField.setText(existingFood.getType());
            priceField.setText(String.valueOf(existingFood.getPrice()));
        }

        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(typeLabel);
        inputPanel.add(typeField);
        inputPanel.add(priceLabel);
        inputPanel.add(priceField);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(getBgColor());

        JButton saveBtn = createStyledButton("Save", OLIVE_GREEN, Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String type = typeField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());

                if (name.isEmpty() || type.isEmpty()) {
                    showThemedMessage("Name and type cannot be empty!", "Error");
                    return;
                }

                if (existingFood == null) {
                    DatabaseManager.addFood(name, type, price, currentRestaurant.getId());
                    showThemedMessage("Food item added successfully!", "Success");
                } else {
                    DatabaseManager.updateFood(existingFood.getId(), name, type, price);
                    showThemedMessage("Food item updated successfully!", "Success");
                }

                loadMenu();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                showThemedMessage("Invalid price format!", "Error");
            }
        });

        JButton cancelBtn = createStyledButton("Cancel", SPICY_RED, Color.WHITE);
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        return dialog;
    }
}
