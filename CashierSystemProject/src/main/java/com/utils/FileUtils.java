package main.java.com.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class FileUtils {
    public static void saveToFile(String content, String filename) {
        SwingUtilities.invokeLater(() -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(filename));
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                AppManager.threadPool.submit(() -> {
                    try (PrintWriter out = new PrintWriter(fileChooser.getSelectedFile())) {
                        out.print(content);
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(null, "Report saved successfully.")
                        );
                    } catch (IOException ex) {
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(null, "Error saving file: " + ex.getMessage())
                        );
                    }
                });
            }
        });
    }
}