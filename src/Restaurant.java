public class Restaurant {
    private int id;
    private String name;
    private Menu menu;

    public Restaurant(int id, String name) {
        this.id = id;
        this.name = name;
        this.menu = new Menu();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    @Override
    public String toString() {
        return String.format("Restaurant: %s (ID: %d)", name, id);
    }
}
