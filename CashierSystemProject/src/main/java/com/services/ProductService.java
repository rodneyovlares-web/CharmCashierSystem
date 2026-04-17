package main.java.com.services;

import main.java.com.database.DataStore;
import main.java.com.models.Product;
import java.util.List;
import java.util.stream.Collectors;

public class ProductService {
    
    // Logic for the search bar in the Cashier view
    public static List<Product> searchProducts(String query) {
        String lowerQuery = query.toLowerCase();
        synchronized (DataStore.allProducts) {
            return DataStore.allProducts.stream()
                .filter(p -> p.name.toLowerCase().contains(lowerQuery) || p.sku.toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
        }
    }

    // Logic for updating stock and automatically logging it
    public static void updateStock(Product product, int newStock) {
        synchronized (DataStore.allProducts) {
            LogService.logStockUpdate("Updated stock of " + product.name + " from " + product.currentStock + " to " + newStock);
            product.currentStock = newStock;
        }
    }
    
    // Checks if a barcode scanned matches an exact item
    public static Product findBySku(String sku) {
        return DataStore.findProductBySku(sku);
    }
}