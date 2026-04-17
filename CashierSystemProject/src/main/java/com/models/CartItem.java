package main.java.com.models;

public class CartItem {
    public Product product;
    public int quantity;
    
    public CartItem(Product p, int q) { 
        this.product = p; 
        this.quantity = q; 
    }
}
