package main.java.com.ui;

import main.java.com.database.DataStore;
import main.java.com.models.Receipt;
import main.java.com.utils.AppManager;
import main.java.com.utils.FileUtils;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class LogView extends JPanel {
    private DefaultTableModel transactionModel, stockUpdateModel;
    
    public LogView() {
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel transPanel = new JPanel(new BorderLayout());
        transactionModel = new DefaultTableModel(new String[]{"Receipt ID", "Date", "Items", "Total"}, 0);
        transPanel.add(new JScrollPane(new JTable(transactionModel)), BorderLayout.CENTER);
        
        JPanel stockPanel = new JPanel(new BorderLayout());
        stockUpdateModel = new DefaultTableModel(new String[]{"Action"}, 0);
        stockPanel.add(new JScrollPane(new JTable(stockUpdateModel)), BorderLayout.CENTER);
        
        tabbedPane.addTab("Transaction History", transPanel);
        tabbedPane.addTab("Stock Updates", stockPanel);
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    public void refresh() {
        AppManager.threadPool.submit(() -> {
            List<Object[]> tRows = new ArrayList<>(), sRows = new ArrayList<>();
            synchronized (DataStore.receipts) {
                for (Receipt r : DataStore.receipts) tRows.add(new Object[]{r.id, r.datetime.toString(), r.items.size(), r.total});
            }
            synchronized (DataStore.stockUpdateLogs) {
                for (String log : DataStore.stockUpdateLogs) sRows.add(new Object[]{log});
            }
            SwingUtilities.invokeLater(() -> {
                transactionModel.setRowCount(0); stockUpdateModel.setRowCount(0);
                for (Object[] r : tRows) transactionModel.addRow(r);
                for (Object[] r : sRows) stockUpdateModel.addRow(r);
            });
        });
    }
}
