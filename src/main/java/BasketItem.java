public class BasketItem {
    private Food food;
    private int quantity;

    public BasketItem(Food food, int quantity) {
        this.food = food;
        this.quantity = quantity;
    }

    public Food getFood() {
        return food;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
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
