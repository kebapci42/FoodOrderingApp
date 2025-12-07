# Food Ordering App

A simple Java application to manage restaurants and menus, using SQLite for storage.

## Features
- **Restaurant & Menu Management**: Create restaurants and add food items (Soup, Salad, Main Course, Drink).
- **Data Persistence**: Automatically saves data to a local SQLite database (`~/.food_ordering_app/food_ordering.db`).
- **CSV Import**: Imports initial data from `data.csv`.

## Requirements
- Java 17 or higher
- Maven

## How to Run

1. **Build the project:**
   ```bash
   mvn clean package
   ```

2. **Run the application:**
   ```bash
   java -jar target/FoodOrderingApp-1.0-SNAPSHOT.jar
   ```

## Data Import
To import data, place a `data.csv` file in the project root with the following format:
```csv
RestaurantName,FoodName,FoodType,Price
Gourmet Bistro,Tomato Soup,Soup,5.99
```
