import java.util.ArrayList;
import java.util.List;

public class Menu {
    private List<Food> foodItems;

    public Menu() {
        this.foodItems = new ArrayList<>();
    }

    public void addFood(Food food) {
        foodItems.add(food);
    }

    public void removeFood(Food food) {
        foodItems.remove(food);
    }
    
    public void removeFood(int index) {
        if (index >= 0 && index < foodItems.size()) {
            foodItems.remove(index);
        }
    }

    public List<Food> getFoodItems() {
        return new ArrayList<>(foodItems); // Return a copy to protect internal list
    }
    
    public Food getFood(int index) {
        if (index >= 0 && index < foodItems.size()) {
            return foodItems.get(index);
        }
        return null;
    }

    public int getSize() {
        return foodItems.size();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Menu:\n");
        for (Food food : foodItems) {
            sb.append("# ").append(food.toString()).append("\n");
        }
        return sb.toString();
    }
}
