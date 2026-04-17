package main.java.com.models;

public class Product {
    public String sku;
    public String name;
    public String category;
    public double price;
    public int currentStock;
    public int initialStock;
    
    public Product(String sku, String name, String category, double price, int currentStock, int initialStock) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.price = price;
        this.currentStock = currentStock;
        this.initialStock = initialStock;
    }
    
    public boolean needsRestock() {
        return currentStock <= (int) Math.ceil(initialStock * 0.1);
    }
    
    public int suggestedOrder() {
        return Math.max(initialStock - currentStock, 0);
    }
}