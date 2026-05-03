package com.university.fms.ui;

import com.university.fms.model.*;
import com.university.fms.service.FuelManagementService;
import com.university.fms.util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Fuel Dispensing panel — UC-05: Record Fuel Dispensing.
 * Only accessible by Pump Operator and Administrator.
 */
public class FuelDispensingPanel extends JPanel {
    private final FuelManagementService service;
    private final MainFrame mainFrame;
    private DefaultTableModel tableModel;
    private JLabel stockLabel;

    public FuelDispensingPanel(FuelManagementService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;
        setBackground(UIConstants.BG_PANEL);
        setLayout(new BorderLayout(0, UIConstants.GAP));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_PANEL);
        header.add(UIHelper.heading("⛽  Fuel Dispensing"), BorderLayout.WEST);

        stockLabel = new JLabel();
        stockLabel.setFont(UIConstants.FONT_LABEL);
        updateStockLabel();
        header.add(stockLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Show only APPROVED requests
        String[] cols = {"Request ID", "Requester", "Vehicle", "Litres", "Purpose", "Token", "Approved Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UIHelper.styledTable(cols);
        table.setModel(tableModel);
        UIHelper.applyAlternatingRows(table);
        add(UIHelper.scrollPane(table), BorderLayout.CENTER);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionBar.setBackground(UIConstants.BG_PANEL);
        JButton dispenseBtn = UIHelper.primaryButton("⛽ Dispense Fuel");
        JButton refreshBtn = UIHelper.successButton("↻ Refresh");
        dispenseBtn.addActionListener(e -> dispenseSelected(table));
        refreshBtn.addActionListener(e -> { refresh(); updateStockLabel(); });
        actionBar.add(dispenseBtn);
        actionBar.add(refreshBtn);
        add(actionBar, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        tableModel.setRowCount(0);
        List<FuelRequest> approved = service.getRequestsByStatus(FuelRequest.RequestStatus.APPROVED);
        for (FuelRequest r : approved) {
            tableModel.addRow(new Object[]{
                r.getRequestID(), r.getRequesterName(), r.getVehiclePlate(),
                String.format("%.1f L", r.getRequestedLitres()),
                r.getPurpose(),
                r.getFuelToken() != null ? r.getFuelToken() : "-",
                r.getProcessedDateStr()
            });
        }
    }

    private void dispenseSelected(JTable table) {
        int sel = table.getSelectedRow();
        if (sel < 0) { UIHelper.showError(this, "Please select a request to dispense."); return; }
        String reqID = (String) tableModel.getValueAt(sel, 0);
        double litres = Double.parseDouble(((String) tableModel.getValueAt(sel, 3)).replace(" L", ""));
        if (UIHelper.confirm(this, "Dispense " + litres + "L for request " + reqID + "?")) {
            try {
                service.dispenseFuel(reqID);
                UIHelper.showSuccess(this, "Fuel dispensed successfully!");
                refresh();
                updateStockLabel();
            } catch (Exception e) {
                UIHelper.showError(this, e.getMessage());
            }
        }
    }

    private void updateStockLabel() {
        FuelInventory inv = service.getInventory();
        stockLabel.setText(String.format("Current Stock: %.0f L  |  Price: PKR %.2f/L",
            inv.getCurrentStock(), inv.getPricePerLitre()));
        stockLabel.setForeground(inv.isLowStock() ? UIConstants.DANGER : UIConstants.SUCCESS);
    }
}
