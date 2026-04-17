package main.java.com.services;

import main.java.com.database.DataStore;
import main.java.com.models.CartItem;
import main.java.com.models.Receipt;
import main.java.com.models.ReceiptItem;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    // Processes the cart, reduces stock, and spits out a final Receipt
    public static Receipt processCheckout(List<CartItem> cart, double subtotal, double taxRate) {
        double tax = subtotal * taxRate;
        double total = subtotal + tax;
        List<ReceiptItem> items = new ArrayList<>();

        // 1. Process items and reduce stock safely
        synchronized (DataStore.allProducts) {
            for (CartItem ci : cart) {
                items.add(new ReceiptItem(ci.product, ci.quantity));
                
                // Reduce the actual stock
                ci.product.currentStock -= ci.quantity;
                
                // Log the deduction
                LogService.logStockUpdate("Sold " + ci.quantity + "x of " + ci.product.name);
            }
        }

        // 2. Generate the Receipt object
        Receipt currentReceipt = new Receipt(items, subtotal, tax, total);

        // 3. Save the Receipt to the database
        synchronized (DataStore.receipts) {
            DataStore.receipts.add(currentReceipt);
        }

        // 4. Log the transaction
        LogService.logTransaction(currentReceipt.id, total);

        return currentReceipt;
    }
}
