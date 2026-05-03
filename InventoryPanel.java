package com.university.fms.ui;

import com.university.fms.model.FuelInventory;
import com.university.fms.service.FuelManagementService;
import com.university.fms.util.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Inventory management panel — UC-06: Fuel Inventory Tracking.
 * Also covers FR20: Fuel Price Management.
 */
public class InventoryPanel extends JPanel {
    private final FuelManagementService service;
    private final MainFrame mainFrame;

    private JLabel stockValue, capacityValue, priceValue, pctValue;
    private JPanel barPanel;
    private JTextArea logArea;

    public InventoryPanel(FuelManagementService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;
        setBackground(UIConstants.BG_PANEL);
        setLayout(new BorderLayout(0, UIConstants.GAP));
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.heading("🏭  Fuel Inventory Management"), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 15, 0));
        center.setBackground(UIConstants.BG_PANEL);

        // Left: Stats card
        JPanel statsCard = UIHelper.cardPanel("Current Inventory Status");
        JPanel stats = new JPanel();
        stats.setBackground(UIConstants.BG_WHITE);
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));

        stockValue    = makeStatLabel("--");
        capacityValue = makeStatLabel("--");
        priceValue    = makeStatLabel("--");
        pctValue      = makeStatLabel("--");

        stats.add(statRow("Current Stock:", stockValue));
        stats.add(Box.createVerticalStrut(8));
        stats.add(statRow("Total Capacity:", capacityValue));
        stats.add(Box.createVerticalStrut(8));
        stats.add(statRow("Price per Litre:", priceValue));
        stats.add(Box.createVerticalStrut(8));
        stats.add(statRow("Stock Level:", pctValue));
        stats.add(Box.createVerticalStrut(15));

        barPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                FuelInventory inv = service.getInventory();
                int w = (int) (getWidth() * inv.getStockPercentage() / 100.0);
                g2.setColor(UIConstants.BORDER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                Color c = inv.isLowStock() ? UIConstants.DANGER :
                    inv.getStockPercentage() < 50 ? UIConstants.WARNING : UIConstants.SUCCESS;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, Math.max(w, 10), getHeight(), 10, 10);
            }
        };
        barPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        barPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        barPanel.setPreferredSize(new Dimension(100, 24));
        stats.add(barPanel);
        stats.add(Box.createVerticalStrut(20));

        // Action buttons
        JButton replenishBtn = UIHelper.primaryButton("+ Replenish Stock");
        JButton priceBtn = UIHelper.successButton("Update Price");
        replenishBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        priceBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        replenishBtn.addActionListener(e -> showReplenishDialog());
        priceBtn.addActionListener(e -> showPriceDialog());
        stats.add(replenishBtn);
        stats.add(Box.createVerticalStrut(8));
        stats.add(priceBtn);

        statsCard.add(stats, BorderLayout.CENTER);

        // Right: Audit log
        JPanel logCard = UIHelper.cardPanel("Inventory Audit Log");
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(248, 249, 252));
        JScrollPane logScroll = UIHelper.scrollPane(logArea);
        logCard.add(logScroll, BorderLayout.CENTER);

        center.add(statsCard);
        center.add(logCard);
        add(center, BorderLayout.CENTER);

        refresh();
    }

    private JPanel statRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(UIConstants.BG_WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(UIHelper.label(label), BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JLabel makeStatLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(UIConstants.PRIMARY);
        return l;
    }

    private void refresh() {
        FuelInventory inv = service.getInventory();
        stockValue.setText(String.format("%.0f L", inv.getCurrentStock()));
        capacityValue.setText(String.format("%.0f L", inv.getTotalCapacity()));
        priceValue.setText(String.format("PKR %.2f", inv.getPricePerLitre()));
        pctValue.setText(String.format("%.1f%%", inv.getStockPercentage()));
        pctValue.setForeground(inv.isLowStock() ? UIConstants.DANGER :
            inv.getStockPercentage() < 50 ? UIConstants.WARNING : UIConstants.SUCCESS);
        barPanel.repaint();

        List<String> logs = inv.getAuditLog();
        StringBuilder sb = new StringBuilder();
        for (int i = logs.size() - 1; i >= 0; i--) sb.append(logs.get(i)).append("\n");
        logArea.setText(sb.toString());
        logArea.setCaretPosition(0);
    }

    private void showReplenishDialog() {
        JTextField litresField = UIHelper.styledField();
        JTextField vendorField = UIHelper.styledField();
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.add(UIHelper.label("Litres to Add:"));
        panel.add(litresField);
        panel.add(UIHelper.label("Vendor Name:"));
        panel.add(vendorField);
        int res = JOptionPane.showConfirmDialog(this, panel, "Replenish Fuel Stock",
            JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                double litres = Double.parseDouble(litresField.getText().trim());
                String vendor = vendorField.getText().trim();
                if (vendor.isEmpty()) { UIHelper.showError(this, "Vendor name is required."); return; }
                service.replenishFuel(litres, vendor);
                UIHelper.showSuccess(this, String.format("Stock replenished by %.0f L.", litres));
                refresh();
            } catch (NumberFormatException ex) {
                UIHelper.showError(this, "Please enter a valid number.");
            } catch (Exception ex) {
                UIHelper.showError(this, ex.getMessage());
            }
        }
    }

    private void showPriceDialog() {
        String input = UIHelper.prompt(this, "Enter new price per litre (PKR):");
        if (input == null || input.trim().isEmpty()) return;
        try {
            double price = Double.parseDouble(input.trim());
            service.updateFuelPrice(price);
            UIHelper.showSuccess(this, "Price updated to PKR " + price + "/L");
            refresh();
        } catch (NumberFormatException ex) {
            UIHelper.showError(this, "Please enter a valid number.");
        } catch (Exception ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }
}
