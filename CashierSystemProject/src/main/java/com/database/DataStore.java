package main.java.com.database;

import main.java.com.models.Category;
import main.java.com.models.Product;
import main.java.com.models.Receipt;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    public static List<Category> categories = new ArrayList<>();
    public static List<Product> allProducts = new ArrayList<>();
    public static List<Receipt> receipts = new ArrayList<>();
    public static List<String> stockUpdateLogs = new ArrayList<>();
    public static List<String> restockLogs = new ArrayList<>();
    
    // Preload sample data
    static {
        categories.add(new Category("Bar"));
        categories.add(new Category("Food"));
        categories.add(new Category("Wine"));
        categories.add(new Category("Soup"));
        categories.add(new Category("Pizzas"));
        categories.add(new Category("Fish"));
        
        allProducts.add(new Product("1001", "Super Delicious Pizza", "Pizzas", 12.99, 15, 20));
        allProducts.add(new Product("1002", "Super Delicious Chicken", "Food", 9.99, 8, 15));
        allProducts.add(new Product("1003", "Super Delicious Burger", "Food", 8.49, 2, 15));
        allProducts.add(new Product("1004", "Super Delicious Chips", "Food", 3.99, 20, 30));
        allProducts.add(new Product("1005", "Red Wine", "Wine", 15.99, 7, 20));
        allProducts.add(new Product("1006", "Fish Fillet", "Fish", 11.50, 1, 10));
    }
    
    public static Product findProductBySku(String sku) {
        synchronized (allProducts) {
            for (Product p : allProducts) {
                if (p.sku.equals(sku)) return p;
            }
        }
        return null;
    }
}
