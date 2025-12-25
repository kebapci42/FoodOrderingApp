import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class FoodOrderingGUI extends JFrame {

    // ═══════════════════════════════════════════════════════════════════
    // THEME STATE
    // ═══════════════════════════════════════════════════════════════════
    private boolean isNightMode = false;

    // ═══════════════════════════════════════════════════════════════════
    // LIGHT THEME COLORS - Warm, hunger-inducing colors
    // ═══════════════════════════════════════════════════════════════════
    private static final Color LIGHT_BG = new Color(255, 250, 240);
    private static final Color LIGHT_CARD = new Color(255, 255, 255);
    private static final Color LIGHT_CARD_ALT = new Color(255, 253, 248);
    private static final Color LIGHT_TEXT = new Color(101, 67, 33);
    private static final Color LIGHT_TEXT_SECONDARY = new Color(150, 140, 130);
    private static final Color LIGHT_TOTAL_BG = new Color(255, 248, 240);

    // ═══════════════════════════════════════════════════════════════════
    // NIGHT THEME COLORS - Dark, cozy restaurant ambiance
    // ═══════════════════════════════════════════════════════════════════
    private static final Color NIGHT_BG = new Color(25, 25, 35);
    private static final Color NIGHT_CARD = new Color(40, 40, 55);
    private static final Color NIGHT_CARD_ALT = new Color(35, 35, 48);
    private static final Color NIGHT_TEXT = new Color(255, 245, 235);
    private static final Color NIGHT_TEXT_SECONDARY = new Color(180, 175, 170);
    private static final Color NIGHT_TOTAL_BG = new Color(45, 45, 60);

    // ═══════════════════════════════════════════════════════════════════
    // SHARED ACCENT COLORS
    // ═══════════════════════════════════════════════════════════════════
    private static final Color APPETIZING_ORANGE = new Color(255, 107, 53);
    private static final Color SPICY_RED = new Color(220, 53, 69);
    private static final Color TOMATO_RED = new Color(255, 99, 71);
    private static final Color OLIVE_GREEN = new Color(107, 142, 35);

    // ═══════════════════════════════════════════════════════════════════
    // CROSS-PLATFORM FONT HANDLING
    // ═══════════════════════════════════════════════════════════════════
    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase();
    private static final boolean IS_MAC = OS_NAME.contains("mac");
    private static final boolean IS_WINDOWS = OS_NAME.contains("win");

    // Get the best available font for the current OS
    private static String getSerifFont() {
        if (IS_MAC) {
            return "Georgia"; // Georgia is available on macOS
        } else if (IS_WINDOWS) {
            return "Georgia";
        }
        return Font.SERIF; // Linux fallback
    }

    private static String getSansSerifFont() {
        if (IS_MAC) {
            // Try SF Pro first, then Helvetica Neue, then system default
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
        return Font.SANS_SERIF; // Linux fallback
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

    // ═══════════════════════════════════════════════════════════════════
    // FONTS - Cross-platform with fallbacks
    // ═══════════════════════════════════════════════════════════════════
    private static final Font HEADER_FONT = new Font(getSerifFont(), Font.BOLD, 26);
    private static final Font TAGLINE_FONT = new Font(getSerifFont(), Font.ITALIC, 14);
    private static final Font SECTION_FONT = new Font(getSansSerifFont(), Font.BOLD, 16);
    private static final Font FOOD_NAME_FONT = new Font(getSansSerifFont(), Font.BOLD, 14);
    private static final Font FOOD_DETAIL_FONT = new Font(getSansSerifFont(), Font.PLAIN, 12);
    private static final Font BUTTON_FONT = new Font(getSansSerifFont(), Font.BOLD, 13);
    private static final Font PRICE_FONT = new Font(getSansSerifFont(), Font.BOLD, 15);

    // ═══════════════════════════════════════════════════════════════════
    // COMPONENTS (stored for theme updates)
    // ═══════════════════════════════════════════════════════════════════
    private JPanel mainContent;
    private JPanel headerPanel;
    private JPanel bottomPanel;
    private JPanel leftPanel;
    private JPanel centerPanel;
    private JPanel rightPanel;
    private JPanel totalPanel;
    private JLabel totalTextLabel;

    private JList<Restaurant> restaurantList;
    private DefaultListModel<Restaurant> restaurantModel;

    private JList<Food> foodList;
    private DefaultListModel<Food> foodModel;

    private JList<String> basketList;
    private DefaultListModel<String> basketModel;
    private List<BasketItem> basketItems;

    private JTextField quantityField;
    private JLabel totalLabel;
    private JLabel itemCountLabel;
    private JLabel qtyLabel;
    private JButton themeToggleBtn;

    // Wallet
    private double walletBalance = 100.00;
    private JLabel walletLabel;

    public FoodOrderingGUI() {
        setTitle("Delicious Bites - Food Ordering");
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout(0, 0));
        basketItems = new ArrayList<>();

        initUI();
        loadRestaurants();
        applyTheme();
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

    private Color getCardAltColor() {
        return isNightMode ? NIGHT_CARD_ALT : LIGHT_CARD_ALT;
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

    private Color getFoodSelectionBg() {
        return isNightMode ? new Color(60, 55, 45) : new Color(255, 245, 235);
    }

    private Color getBorderColor() {
        return isNightMode ? new Color(70, 70, 85) : new Color(240, 235, 228);
    }

    private Color getTotalBgColor() {
        return isNightMode ? NIGHT_TOTAL_BG : LIGHT_TOTAL_BG;
    }

    private void initUI() {
        // ═══════════════════════════════════════════════════════════════
        // HEADER PANEL - Increased height for better text display
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
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 30, 22, 30)); // More bottom padding
        headerPanel.setPreferredSize(new Dimension(0, 100)); // Taller header

        JPanel headerLeft = new JPanel();
        headerLeft.setOpaque(false);
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Delicious Bites");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel taglineLabel = new JLabel("Hungry? Your next meal is just a click away!");
        taglineLabel.setFont(TAGLINE_FONT);
        taglineLabel.setForeground(new Color(255, 255, 255, 220));
        taglineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerLeft.add(titleLabel);
        headerLeft.add(Box.createVerticalStrut(8));
        headerLeft.add(taglineLabel);

        // Theme toggle button in header
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

        // Wallet Label
        walletLabel = new JLabel(String.format("Wallet: $%.2f", walletBalance));
        walletLabel.setFont(SECTION_FONT);
        walletLabel.setForeground(Color.WHITE);
        walletLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        // Order History button
        JButton historyBtn = new JButton("Order History") {
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
        historyBtn.setFont(BUTTON_FONT);
        historyBtn.setPreferredSize(new Dimension(130, 35));
        historyBtn.setFocusPainted(false);
        historyBtn.setBorderPainted(false);
        historyBtn.setContentAreaFilled(false);
        historyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        historyBtn.addActionListener(e -> showOrderHistory());

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        headerRight.setOpaque(false);
        headerRight.add(walletLabel);
        headerRight.add(historyBtn);
        headerRight.add(Box.createHorizontalStrut(10));
        headerRight.add(themeToggleBtn);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ═══════════════════════════════════════════════════════════════
        // MAIN CONTENT PANEL
        // ═══════════════════════════════════════════════════════════════
        mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ═══════════════════════════════════════════════════════════════
        // LEFT PANEL - Restaurants
        // ═══════════════════════════════════════════════════════════════
        restaurantModel = new DefaultListModel<>();
        restaurantList = new JList<>(restaurantModel);
        restaurantList.setCellRenderer(new RestaurantRenderer());
        restaurantList.setFixedCellHeight(55);
        restaurantList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        restaurantList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadMenuForSelectedRestaurant();
            }
        });

        leftPanel = createSectionPanel("Restaurants", new JScrollPane(restaurantList));
        leftPanel.setPreferredSize(new Dimension(240, 0));
        mainContent.add(leftPanel, BorderLayout.WEST);

        // ═══════════════════════════════════════════════════════════════
        // CENTER PANEL - Menu Items
        // ═══════════════════════════════════════════════════════════════
        foodModel = new DefaultListModel<>();
        foodList = new JList<>(foodModel);
        foodList.setCellRenderer(new FoodCardRenderer());
        foodList.setFixedCellHeight(70);
        foodList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane foodScroll = new JScrollPane(foodList);
        foodScroll.setBorder(BorderFactory.createEmptyBorder());

        centerPanel = createSectionPanel("Menu", foodScroll);
        mainContent.add(centerPanel, BorderLayout.CENTER);

        // ═══════════════════════════════════════════════════════════════
        // RIGHT PANEL - Basket
        // ═══════════════════════════════════════════════════════════════
        basketModel = new DefaultListModel<>();
        basketList = new JList<>(basketModel);
        basketList.setFont(FOOD_DETAIL_FONT);
        basketList.setFixedCellHeight(35);
        basketList.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel basketContent = new JPanel(new BorderLayout(0, 10));
        basketContent.setOpaque(false);
        basketContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        itemCountLabel = new JLabel("0 items");
        itemCountLabel.setFont(FOOD_DETAIL_FONT);
        basketContent.add(itemCountLabel, BorderLayout.NORTH);
        basketContent.add(new JScrollPane(basketList), BorderLayout.CENTER);

        // Total panel - now with theme support
        totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font(getSansSerifFont(), Font.BOLD, 22));
        totalLabel.setForeground(OLIVE_GREEN);

        totalTextLabel = new JLabel("Total:");
        totalTextLabel.setFont(SECTION_FONT);

        totalPanel.add(totalTextLabel, BorderLayout.WEST);
        totalPanel.add(totalLabel, BorderLayout.EAST);

        // Checkout Container (Total + Place Order Button)
        JPanel checkoutContainer = new JPanel();
        checkoutContainer.setLayout(new BoxLayout(checkoutContainer, BoxLayout.Y_AXIS));
        checkoutContainer.setOpaque(false);

        JButton placeOrderBtn = createStyledButton("Place Order", new Color(46, 204, 113), Color.WHITE);
        placeOrderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        placeOrderBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        placeOrderBtn.addActionListener(e -> placeOrder());

        checkoutContainer.add(totalPanel);
        checkoutContainer.add(Box.createVerticalStrut(10));
        checkoutContainer.add(placeOrderBtn);

        basketContent.add(checkoutContainer, BorderLayout.SOUTH);

        rightPanel = createSectionPanel("Your Basket", basketContent);
        rightPanel.setPreferredSize(new Dimension(280, 0));
        mainContent.add(rightPanel, BorderLayout.EAST);

        add(mainContent, BorderLayout.CENTER);

        // ═══════════════════════════════════════════════════════════════
        // BOTTOM PANEL - Actions
        // ═══════════════════════════════════════════════════════════════
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 18));

        qtyLabel = new JLabel("Qty:");
        qtyLabel.setFont(SECTION_FONT);

        // Quantity control with +/- buttons
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        quantityPanel.setOpaque(false);

        JButton minusBtn = createQuantityButton("-");
        minusBtn.addActionListener(e -> adjustQuantity(-1));

        quantityField = new JTextField("1", 2);
        quantityField.setFont(FOOD_NAME_FONT);
        quantityField.setHorizontalAlignment(JTextField.CENTER);
        quantityField.setPreferredSize(new Dimension(40, 35));

        JButton plusBtn = createQuantityButton("+");
        plusBtn.addActionListener(e -> adjustQuantity(1));

        quantityPanel.add(minusBtn);
        quantityPanel.add(quantityField);
        quantityPanel.add(plusBtn);

        JButton addButton = createStyledButton("Add to Basket", OLIVE_GREEN, Color.WHITE);
        addButton.addActionListener(e -> addToBasket());

        JButton clearButton = createStyledButton("Clear", SPICY_RED, Color.WHITE);
        clearButton.addActionListener(e -> clearBasket());

        // Delete buttons kept but hidden for potential future use
        // JButton deleteFoodBtn = createStyledButton("Delete Item", new Color(192, 57,
        // 43), Color.WHITE);
        // deleteFoodBtn.addActionListener(e -> deleteFood());
        // JButton deleteRestBtn = createStyledButton("Delete Restaurant", new
        // Color(142, 68, 173), Color.WHITE);
        // deleteRestBtn.addActionListener(e -> deleteRestaurant());

        bottomPanel.add(qtyLabel);
        bottomPanel.add(quantityPanel);
        bottomPanel.add(addButton);
        bottomPanel.add(clearButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ═══════════════════════════════════════════════════════════════════
    // THEME TOGGLE
    // ═══════════════════════════════════════════════════════════════════
    private void toggleTheme() {
        isNightMode = !isNightMode;
        themeToggleBtn.setText(isNightMode ? "Light Mode" : "Night Mode");
        applyTheme();
    }

    private void applyTheme() {
        // Main frame
        getContentPane().setBackground(getBgColor());
        mainContent.setBackground(getBgColor());

        // Update lists
        restaurantList.setBackground(getCardColor());
        restaurantList.setSelectionBackground(getSelectionBg());
        restaurantList.setSelectionForeground(isNightMode ? APPETIZING_ORANGE : APPETIZING_ORANGE);

        foodList.setBackground(getBgColor());
        foodList.setSelectionBackground(getFoodSelectionBg());

        basketList.setBackground(getCardColor());
        basketList.setForeground(getTextColor());
        basketList.setSelectionBackground(isNightMode ? new Color(80, 60, 60) : new Color(255, 230, 230));

        // Total panel - now properly themed
        totalPanel.setBackground(getTotalBgColor());
        totalTextLabel.setForeground(getTextColor());
        itemCountLabel.setForeground(getTextSecondaryColor());

        // Bottom panel
        bottomPanel.setBackground(getCardColor());
        qtyLabel.setForeground(getTextColor());
        quantityField.setBackground(isNightMode ? NIGHT_CARD_ALT : Color.WHITE);
        quantityField.setForeground(getTextColor());
        quantityField.setCaretColor(getTextColor());
        quantityField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBorderColor(), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        // Repaint header for gradient update
        headerPanel.repaint();

        // Force repaint all
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER: Create styled section panel
    // ═══════════════════════════════════════════════════════════════════
    private JPanel createSectionPanel(String title, JComponent content) {
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

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER: Create quantity +/- button
    // ═══════════════════════════════════════════════════════════════════
    private JButton createQuantityButton(String text) {
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
                Color bg = hover ? APPETIZING_ORANGE : (isNightMode ? NIGHT_CARD_ALT : new Color(240, 235, 230));
                g2d.setColor(bg);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2d.setColor(hover ? Color.WHITE : getTextColor());
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        button.setFont(new Font(getSansSerifFont(), Font.BOLD, 16));
        button.setPreferredSize(new Dimension(35, 35));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER: Adjust quantity value
    // ═══════════════════════════════════════════════════════════════════
    private void adjustQuantity(int delta) {
        try {
            int current = Integer.parseInt(quantityField.getText());
            int newValue = Math.max(1, current + delta); // Minimum 1
            quantityField.setText(String.valueOf(newValue));
        } catch (NumberFormatException e) {
            quantityField.setText("1");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER: Create styled button
    // ═══════════════════════════════════════════════════════════════════
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
        button.setPreferredSize(new Dimension(150, 38));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CUSTOM RENDERER: Restaurant List
    // ═══════════════════════════════════════════════════════════════════
    private class RestaurantRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            if (isSelected) {
                panel.setBackground(getSelectionBg());
            } else {
                panel.setBackground(index % 2 == 0 ? getCardColor() : getCardAltColor());
            }

            Restaurant restaurant = (Restaurant) value;

            JLabel nameLabel = new JLabel(restaurant.getName());
            nameLabel.setFont(FOOD_NAME_FONT);
            nameLabel.setForeground(isSelected ? APPETIZING_ORANGE : getTextColor());

            panel.add(nameLabel, BorderLayout.CENTER);

            return panel;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CUSTOM RENDERER: Food Card
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

                    if (isSelected) {
                        g2d.setColor(getFoodSelectionBg());
                    } else {
                        g2d.setColor(getCardColor());
                    }
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
    // DATA LOADING
    // ═══════════════════════════════════════════════════════════════════
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

    // ═══════════════════════════════════════════════════════════════════
    // BASKET OPERATIONS
    // ═══════════════════════════════════════════════════════════════════
    private void addToBasket() {
        Food selectedFood = foodList.getSelectedValue();
        if (selectedFood == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a delicious item first!",
                    "No Item Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Quantity must be at least 1!",
                        "Invalid Quantity",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            BasketItem item = new BasketItem(selectedFood, quantity);
            basketItems.add(item);
            updateBasketDisplay();
            quantityField.setText("1");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid number!",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearBasket() {
        if (basketItems.isEmpty())
            return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Clear all items from your basket?",
                "Clear Basket",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            basketItems.clear();
            updateBasketDisplay();
        }
    }

    private void updateBasketDisplay() {
        basketModel.clear();
        double total = 0.0;

        for (BasketItem item : basketItems) {
            basketModel.addElement(String.format("%dx %s  $%.2f",
                    item.getQuantity(), item.getFood().getName(), item.getTotalPrice()));
            total += item.getTotalPrice();
        }

        totalLabel.setText(String.format("$%.2f", total));
        itemCountLabel.setText(basketItems.size() + (basketItems.size() == 1 ? " item" : " items"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // ORDER OPERATIONS
    // ═══════════════════════════════════════════════════════════════════
    private void placeOrder() {
        if (basketItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Your basket is empty! Add some delicious items first.",
                    "Basket Empty",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total = basketItems.stream().mapToDouble(BasketItem::getTotalPrice).sum();

        if (total > walletBalance) {
            JOptionPane.showMessageDialog(this,
                    String.format("Insufficient funds! You need $%.2f but have only $%.2f.", total, walletBalance),
                    "Budget Exceeded",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Place order for $%.2f? (Wallet: $%.2f)", total, walletBalance),
                "Confirm Order",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseManager.placeOrder(basketItems, total);

            // Deduct from wallet
            walletBalance -= total;
            walletLabel.setText(String.format("Wallet: $%.2f", walletBalance));

            JOptionPane.showMessageDialog(this,
                    String.format("Order placed! Remaining balance: $%.2f", walletBalance),
                    "Order Placed",
                    JOptionPane.INFORMATION_MESSAGE);

            basketItems.clear();
            updateBasketDisplay();
        }
    }

    private void showOrderHistory() {
        JDialog historyDialog = new JDialog(this, "Order History", true);
        historyDialog.setSize(600, 400);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setLayout(new BorderLayout());

        // Table Data
        String[] columnNames = { "Order ID", "Date", "Items", "Total" };
        List<Order> orders = DatabaseManager.getOrderHistory();

        Object[][] data = new Object[orders.size()][4];
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            data[i][0] = order.getId();
            data[i][1] = order.getDate();
            data[i][2] = order.getItemsDescription();
            data[i][3] = String.format("$%.2f", order.getTotalAmount());
        }

        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setFont(FOOD_DETAIL_FONT);
        table.setRowHeight(30);

        // Header styling
        table.getTableHeader().setFont(BUTTON_FONT);
        table.getTableHeader().setBackground(isNightMode ? NIGHT_CARD : LIGHT_CARD);
        table.getTableHeader().setForeground(getTextColor());

        // Table styling
        table.setBackground(isNightMode ? NIGHT_BG : LIGHT_BG);
        table.setForeground(getTextColor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(isNightMode ? NIGHT_BG : LIGHT_BG);

        historyDialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> historyDialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(isNightMode ? NIGHT_CARD : LIGHT_CARD_ALT);
        btnPanel.add(closeBtn);

        historyDialog.add(btnPanel, BorderLayout.SOUTH);
        historyDialog.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════
    // DELETE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════
    private void deleteFood() {
        Food selectedFood = foodList.getSelectedValue();
        if (selectedFood == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a food item to delete!",
                    "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete '" + selectedFood.getName() + "' from the menu?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

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
            JOptionPane.showMessageDialog(this,
                    "Please select a restaurant to delete!",
                    "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete '" + selectedRestaurant.getName() + "' and all its menu items?",
                "Confirm Delete Restaurant",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseManager.deleteRestaurant(selectedRestaurant.getId());
            loadRestaurants();
            foodModel.clear();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // BASKET ITEM CLASS
    // ═══════════════════════════════════════════════════════════════════

}
