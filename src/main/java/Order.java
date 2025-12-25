public class Order {
    private int id;
    private String date;
    private double totalAmount;
    private String itemsDescription; // Simplified for history view (e.g., "Burger x2, Coke x1")

    public Order(int id, String date, double totalAmount, String itemsDescription) {
        this.id = id;
        this.date = date;
        this.totalAmount = totalAmount;
        this.itemsDescription = itemsDescription;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getItemsDescription() {
        return itemsDescription;
    }
}
