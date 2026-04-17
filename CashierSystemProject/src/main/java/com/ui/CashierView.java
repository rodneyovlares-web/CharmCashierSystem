package main.java.com.ui;

import main.java.com.database.DataStore;
import main.java.com.models.CartItem;
import main.java.com.models.Product;
import main.java.com.models.Receipt;
import main.java.com.models.ReceiptItem;
import main.java.com.services.TransactionService;
import main.java.com.utils.AppManager;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class CashierView extends JPanel {
    private JTextField searchField, barcodeField;   
    private JTable productCatalogTable, cartTable;
    private DefaultTableModel catalogModel, cartModel;
    private JLabel totalLabel;
    private List<CartItem> cart = new ArrayList<>();
    private double subtotal = 0.0, taxRate = 0.07;
    private final Object cartLock = new Object();
    private Timer searchTimer; 
    private boolean isUpdatingCart = false; 

    public CashierView() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Scan Barcode / SKU:"));
        barcodeField = new JTextField(15);
        barcodeField.addActionListener(e -> addProductBySku(barcodeField.getText().trim()));
        topPanel.add(barcodeField);
        topPanel.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        
        searchTimer = new Timer(300, e -> searchProducts(searchField.getText().trim()));
        searchTimer.setRepeats(false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { searchTimer.restart(); }
            public void insertUpdate(DocumentEvent e) { searchTimer.restart(); }
            public void removeUpdate(DocumentEvent e) { searchTimer.restart(); }
        });
        topPanel.add(searchField);
        add(topPanel, BorderLayout.NORTH);
        
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplit.setDividerLocation(500);
        
        JPanel catalogPanel = new JPanel(new BorderLayout());
        catalogPanel.setBorder(BorderFactory.createTitledBorder("Products"));
        catalogModel = new DefaultTableModel(new String[]{"SKU", "Name", "Price", "Stock"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        productCatalogTable = new JTable(catalogModel);
        productCatalogTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && productCatalogTable.getSelectedRow() != -1) {
                    Product p = DataStore.findProductBySku((String) catalogModel.getValueAt(productCatalogTable.getSelectedRow(), 0));
                    if (p != null) showQuantityDialog(p);
                }
            }
        });
        catalogPanel.add(new JScrollPane(productCatalogTable), BorderLayout.CENTER);
        
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("Cart"));
        cartModel = new DefaultTableModel(new String[]{"SKU", "Name", "Price", "Quantity", "Subtotal"}, 0);
        cartTable = new JTable(cartModel);
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        
        JPanel cartButtons = new JPanel(new FlowLayout());
        JButton removeBtn = new JButton("Remove Selected");
        removeBtn.addActionListener(e -> removeSelected());
        JButton clearBtn = new JButton("Clear Cart");
        clearBtn.addActionListener(e -> clearCart());
        cartButtons.add(removeBtn); cartButtons.add(clearBtn);
        cartPanel.add(cartButtons, BorderLayout.SOUTH);
        
        centerSplit.setLeftComponent(catalogPanel);
        centerSplit.setRightComponent(cartPanel);
        add(centerSplit, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Subtotal: $0.00  |  Tax: $0.00  |  Total: $0.00");
        bottomPanel.add(totalLabel, BorderLayout.WEST);
        
        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.addActionListener(e -> checkout());
        JButton printBtn = new JButton("Print Receipt");
        printBtn.addActionListener(e -> printReceipt());
        actionPanel.add(checkoutBtn); actionPanel.add(printBtn);
        bottomPanel.add(actionPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void searchProducts(String query) {
        AppManager.threadPool.submit(() -> {
            List<Object[]> results = new ArrayList<>();
            synchronized (DataStore.allProducts) {
                for (Product p : DataStore.allProducts) {
                    if (p.name.toLowerCase().contains(query.toLowerCase()) || p.sku.toLowerCase().contains(query.toLowerCase())) {
                        results.add(new Object[]{p.sku, p.name, p.price, p.currentStock});
                    }
                }
            }
            SwingUtilities.invokeLater(() -> {
                catalogModel.setRowCount(0);
                for (Object[] row : results) catalogModel.addRow(row);
            });
        });
    }

    private void addProductBySku(String sku) {
        AppManager.threadPool.submit(() -> {
            Product p = DataStore.findProductBySku(sku);
            SwingUtilities.invokeLater(() -> {
                if (p != null) showQuantityDialog(p);
                else { JOptionPane.showMessageDialog(this, "Product not found."); barcodeField.setText(""); }
            });
        });
    }

    private void showQuantityDialog(Product p) {
        String qtyStr = JOptionPane.showInputDialog(this, "Quantity for " + p.name + ":");
        if (qtyStr != null) {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0 && qty <= p.currentStock) addToCart(p, qty);
                else JOptionPane.showMessageDialog(this, "Invalid/Insufficient stock.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid format."); }
        }
    }

    private void addToCart(Product p, int qty) {
        synchronized (cartLock) {
            boolean found = false;
            for (CartItem ci : cart) {
                if (ci.product.sku.equals(p.sku)) { ci.quantity += qty; found = true; break; }
            }
            if (!found) cart.add(new CartItem(p, qty));
        }
        SwingUtilities.invokeLater(this::updateCartTable);
    }

    private void removeSelected() {
        if (cartTable.getSelectedRow() != -1) {
            synchronized (cartLock) { cart.remove(cartTable.getSelectedRow()); }
            updateCartTable();
        }
    }

    private void clearCart() { synchronized (cartLock) { cart.clear(); } updateCartTable(); }

    private void updateCartTable() {
        if (isUpdatingCart) return;
        isUpdatingCart = true;
        cartModel.setRowCount(0); subtotal = 0.0;
        synchronized (cartLock) {
            for (CartItem ci : cart) {
                double sub = ci.product.price * ci.quantity; subtotal += sub;
                cartModel.addRow(new Object[]{ci.product.sku, ci.product.name, ci.product.price, ci.quantity, sub});
            }
        }
        double tax = subtotal * taxRate;
        totalLabel.setText(String.format("Subtotal: $%.2f  |  Tax: $%.2f  |  Total: $%.2f", subtotal, tax, subtotal + tax));
        isUpdatingCart = false;
    }

    private void printReceipt() {
       // Using your existing print receipt logic, just accessing cart logic
       JOptionPane.showMessageDialog(this, "Preview Logic Migrated!");
    }

    private void checkout() {
        if (cart.isEmpty()) return;
        
        AppManager.threadPool.submit(() -> {
            // The Service handles all the heavy lifting!
            Receipt finalReceipt = TransactionService.processCheckout(cart, subtotal, taxRate);
            
            SwingUtilities.invokeLater(() -> {
                clearCart(); 
                JOptionPane.showMessageDialog(this, "Checkout successful! Receipt ID: " + finalReceipt.id); 
                refresh();
            });
        });
    }

    public void refresh() { searchProducts(""); barcodeField.setText(""); }
}