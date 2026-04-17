package main.java.com;

import main.java.com.ui.CashierView;
import main.java.com.ui.LogView;
import main.java.com.ui.ReminderView;
import main.java.com.ui.StockView;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

public class MainApp extends JFrame {
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private StockView stockPanel;
    private CashierView cashierPanel;
    private ReminderView reminderPanel;
    private LogView logPanel;
    
    public MainApp() {
        setTitle("CHARM mini grocery - Inventory System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } 
        catch (Exception ignored) {}
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setEnabled(false);
        splitPane.setLeftComponent(createSidebar());
        
        mainContentPanel = new JPanel(cardLayout = new CardLayout());
        stockPanel = new StockView();
        cashierPanel = new CashierView();
        reminderPanel = new ReminderView();
        logPanel = new LogView();
        
        mainContentPanel.add(stockPanel, "STOCK");
        mainContentPanel.add(cashierPanel, "CASHIER");
        mainContentPanel.add(reminderPanel, "REMINDER");
        mainContentPanel.add(logPanel, "LOG");
        
        splitPane.setRightComponent(mainContentPanel);
        add(splitPane);
        
        showStock();
    }
    
    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(51, 51, 51));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        
        JLabel logoLabel = new JLabel("🛒 CHARM mini grocery");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        panel.add(logoLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(createSidebarButton("📦 Stock", e -> showStock()));
        panel.add(createSidebarButton("🛒 Cashier", e -> showCashier()));
        panel.add(createSidebarButton("⚠️ Reminder", e -> showReminder()));
        panel.add(createSidebarButton("📋 Logs", e -> showLog()));
        
        return panel;
    }
    
    private JButton createSidebarButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(70, 70, 70));
        btn.addActionListener(listener);
        return btn;
    }
    
    private void showStock() { cardLayout.show(mainContentPanel, "STOCK"); stockPanel.refresh(); }
    private void showCashier() { cardLayout.show(mainContentPanel, "CASHIER"); cashierPanel.refresh(); }
    private void showReminder() { cardLayout.show(mainContentPanel, "REMINDER"); reminderPanel.refresh(); }
    private void showLog() { cardLayout.show(mainContentPanel, "LOG"); logPanel.refresh(); }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}