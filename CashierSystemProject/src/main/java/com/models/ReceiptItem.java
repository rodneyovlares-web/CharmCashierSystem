package main.java.com.models;

public class ReceiptItem {
    public Product product;
    public int quantity;
    public double priceAtTime;
    
    public ReceiptItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.priceAtTime = product.price;
    }
    
    public double subtotal() {
        return priceAtTime * quantity;
    }
}