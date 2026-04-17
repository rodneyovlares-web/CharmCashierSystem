package main.java.com.ui;

import main.java.com.database.DataStore;
import main.java.com.models.Category;
import main.java.com.models.Product;
import main.java.com.utils.AppManager;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class StockView extends JPanel {
    private DefaultListModel<String> categoryListModel;
    private JList<String> categoryList;
    private JPanel productPanel;
    private JTable productTable;
    private DefaultTableModel productTableModel;
    private String currentCategory;
    private final Object tableLock = new Object();
    
    public StockView() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Categories"));
        categoryListModel = new DefaultListModel<>();
        for (Category cat : DataStore.categories) {
            categoryListModel.addElement(cat.name);
        }
        categoryList = new JList<>(categoryListModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = categoryList.getSelectedValue();
                if (selected != null) {
                    currentCategory = selected;
                    showProductsForCategory(currentCategory);
                }
            }
        });
        leftPanel.add(new JScrollPane(categoryList), BorderLayout.CENTER);
        
        JPanel catButtons = new JPanel(new FlowLayout());
        JButton addCatBtn = new JButton("➕ Add Category");
        addCatBtn.addActionListener(e -> addCategory());
        catButtons.add(addCatBtn);
        leftPanel.add(catButtons, BorderLayout.SOUTH);
        
        add(leftPanel, BorderLayout.WEST);
        
        productPanel = new JPanel(new BorderLayout());
        productPanel.setBorder(BorderFactory.createTitledBorder("Products"));
        
        productTableModel = new DefaultTableModel(new String[]{"SKU", "Name", "Price", "Current Stock", "Initial Stock", "Restock Threshold"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        productTable = new JTable(productTableModel);
        productTable.getTableHeader().setReorderingAllowed(false);
        productPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        
        JPanel productButtons = new JPanel(new FlowLayout());
        JButton addProdBtn = new JButton("➕ Add Product");
        addProdBtn.addActionListener(e -> addProduct());
        JButton updateStockBtn = new JButton("📦 Update Stock");
        updateStockBtn.addActionListener(e -> updateStock());
        productButtons.add(addProdBtn);
        productButtons.add(updateStockBtn);
        productPanel.add(productButtons, BorderLayout.SOUTH);
        
        add(productPanel, BorderLayout.CENTER);
        
        if (categoryListModel.size() > 0) categoryList.setSelectedIndex(0);
    }
    
    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "Enter category name:");
        if (name != null && !name.trim().isEmpty()) {
            AppManager.threadPool.submit(() -> {
                DataStore.categories.add(new Category(name));
                SwingUtilities.invokeLater(() -> {
                    categoryListModel.addElement(name);
                    DataStore.stockUpdateLogs.add("Added category: " + name);
                });
            });
        }
    }
    
    private void showProductsForCategory(String category) {
        AppManager.threadPool.submit(() -> {
            List<Object[]> rows = new ArrayList<>();
            synchronized (tableLock) {
                for (Product p : DataStore.allProducts) {
                    if (p.category.equals(category)) {
                        int threshold = (int) Math.ceil(p.initialStock * 0.1);
                        rows.add(new Object[]{p.sku, p.name, p.price, p.currentStock, p.initialStock, threshold});
                    }
                }
            }
            SwingUtilities.invokeLater(() -> {
                productTableModel.setRowCount(0);
                for (Object[] row : rows) { productTableModel.addRow(row); }
            });
        });
    }
    
    private void addProduct() {
        if (currentCategory == null) {
            JOptionPane.showMessageDialog(this, "Please select a category first.");
            return;
        }
        JTextField skuField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField initStockField = new JTextField();
        Object[] fields = { "SKU:", skuField, "Name:", nameField, "Price:", priceField, "Current Stock:", stockField, "Initial Stock:", initStockField };
        
        if (JOptionPane.showConfirmDialog(this, fields, "Add Product", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Product p = new Product(skuField.getText().trim(), nameField.getText().trim(), currentCategory, 
                        Double.parseDouble(priceField.getText().trim()), Integer.parseInt(stockField.getText().trim()), Integer.parseInt(initStockField.getText().trim()));
                AppManager.threadPool.submit(() -> {
                    synchronized (tableLock) {
                        DataStore.allProducts.add(p);
                        DataStore.stockUpdateLogs.add("Added product: " + p.name + " to " + currentCategory);
                    }
                    SwingUtilities.invokeLater(() -> showProductsForCategory(currentCategory));
                });
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.");
            }
        }
    }
    
    private void updateStock() {
        int row = productTable.getSelectedRow();
        if (row == -1) return;
        Product p = DataStore.findProductBySku((String) productTableModel.getValueAt(row, 0));
        if (p == null) return;
        
        String input = JOptionPane.showInputDialog(this, "Enter new stock:", p.currentStock);
        if (input != null) {
            try {
                int newStock = Integer.parseInt(input);
                AppManager.threadPool.submit(() -> {
                    synchronized (tableLock) {
                        DataStore.stockUpdateLogs.add("Updated stock of " + p.name + " from " + p.currentStock + " to " + newStock);
                        p.currentStock = newStock;
                    }
                    SwingUtilities.invokeLater(() -> showProductsForCategory(currentCategory));
                });
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid number."); }
        }
    }
    
    public void refresh() {
        if (currentCategory != null) showProductsForCategory(currentCategory);
    }
}
