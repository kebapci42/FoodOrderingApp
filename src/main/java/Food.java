public class Food {
    private int id;
    private String name;
    private String type;
    private double price;

    // Constructor
    public Food(String name, String type, double price) {
        this.name = name;
        this.type = type;
        this.price = price;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Method to display food information
    public String getDisplayInfo() {
        return String.format("%s ($%.2f)", name, price);
    }

    @Override
    public String toString() {
        return String.format("%s: %s - $%.2f", type, name, price);
    }
}