package main.java.com.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Receipt {
    public String id;
    public LocalDateTime datetime;
    public List<ReceiptItem> items;
    public double subtotal;
    public double tax;
    public double total;
    
    public Receipt(List<ReceiptItem> items, double subtotal, double tax, double total) {
        this.id = "RCP-" + System.currentTimeMillis();
        this.datetime = LocalDateTime.now();
        this.items = new ArrayList<>(items);
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
    }
}

