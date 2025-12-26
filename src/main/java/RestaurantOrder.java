public class RestaurantOrder {
    private int orderId;
    private String date;
    private double total;
    private String itemsDescription;

    public RestaurantOrder(int orderId, String date, double total, String itemsDescription) {
        this.orderId = orderId;
        this.date = date;
        this.total = total;
        this.itemsDescription = itemsDescription;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getDate() {
        return date;
    }

    public double getTotal() {
        return total;
    }

    public String getItemsDescription() {
        return itemsDescription;
    }
}
