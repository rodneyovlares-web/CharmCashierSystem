package main.java.com.services;

import main.java.com.database.DataStore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogService {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logStockUpdate(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        synchronized (DataStore.stockUpdateLogs) {
            DataStore.stockUpdateLogs.add("[" + timestamp + "] " + message);
        }
    }
    
    public static void logTransaction(String receiptId, double total) {
        String timestamp = LocalDateTime.now().format(formatter);
        synchronized (DataStore.stockUpdateLogs) {
             // We save it to the same log list for now, or you can create a specific transaction log list
            DataStore.stockUpdateLogs.add("[" + timestamp + "] TRANSACTION: " + receiptId + " | Total: $" + total);
        }
    }
}
