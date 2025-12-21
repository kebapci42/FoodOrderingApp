public abstract class Food {
    private int id;
    private String name;
    private double price;

    // Constructor
    public Food(String name, double price) {
        this.name = name;
        this.price = price;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public void setPrice(double price) {
        this.price = price;
    }

    // Abstract method to be overridden by subclasses
    public abstract String getType();

    // Method to display food information
    public String getDisplayInfo() {
        return String.format("%s ($%.2f)", name, price);
    }

    @Override
    public String toString() {
        return String.format("%s: %s - $%.2f", getType(), name, price);
    }
}

// Drink subclass
class Drink extends Food {

    public Drink(String name, double price) {
        super(name, price);
    }

    @Override
    public String getType() {
        return "Drink";
    }
}

// Soup subclass
class Soup extends Food {

    public Soup(String name, double price) {
        super(name, price);
    }

    @Override
    public String getType() {
        return "Soup";
    }
}

// MainCourse subclass
class MainCourse extends Food {

    public MainCourse(String name, double price) {
        super(name, price);
    }

    @Override
    public String getType() {
        return "Main Course";
    }
}

// Salad subclass
class Salad extends Food {

    public Salad(String name, double price) {
        super(name, price);
    }

    @Override
    public String getType() {
        return "Salad";
    }
}