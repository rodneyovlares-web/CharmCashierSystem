package main.java.com.ui;

import main.java.com.database.DataStore;
import main.java.com.models.Product;
import main.java.com.utils.AppManager;
import main.java.com.utils.FileUtils;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ReminderView extends JPanel {
    private JTable reminderTable;
    private DefaultTableModel reminderModel;
    
    public ReminderView() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        reminderModel = new DefaultTableModel(new String[]{"SKU", "Name", "Category", "Current Stock", "Initial Stock", "Order"}, 0);
        reminderTable = new JTable(reminderModel);
        add(new JScrollPane(reminderTable), BorderLayout.CENTER);
        
        JButton genReportBtn = new JButton("Generate Restock Report");
        genReportBtn.addActionListener(e -> generateReport());
        JPanel p = new JPanel(); p.add(genReportBtn);
        add(p, BorderLayout.SOUTH);
        
        refresh();
    }
    
    public void refresh() {
        AppManager.threadPool.submit(() -> {
            List<Object[]> rows = new ArrayList<>();
            synchronized (DataStore.allProducts) {
                for (Product p : DataStore.allProducts) {
                    if (p.needsRestock()) rows.add(new Object[]{p.sku, p.name, p.category, p.currentStock, p.initialStock, p.suggestedOrder()});
                }
            }
            SwingUtilities.invokeLater(() -> {
                reminderModel.setRowCount(0);
                for (Object[] r : rows) reminderModel.addRow(r);
            });
        });
    }
    
    private void generateReport() {
        AppManager.threadPool.submit(() -> {
            StringBuilder sb = new StringBuilder("CHARM mini grocery - Restock Report\n");
            synchronized (DataStore.allProducts) {
                for (Product p : DataStore.allProducts) {
                    if (p.needsRestock()) sb.append(p.name).append(" needs ").append(p.suggestedOrder()).append("\n");
                }
            }
            FileUtils.saveToFile(sb.toString(), "restock_report.txt");
        });
    }
}