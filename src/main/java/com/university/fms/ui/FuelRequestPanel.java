package com.university.fms.ui;

import com.university.fms.model.*;
import com.university.fms.service.FuelManagementService;
import com.university.fms.util.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Fuel Request panel — UC-03: Submit Fuel Request, UC-04: Approve/Reject.
 */
public class FuelRequestPanel extends JPanel {
    private final FuelManagementService service;
    private final MainFrame mainFrame;
    private JTable table;
    private DefaultTableModel tableModel;

    public FuelRequestPanel(FuelManagementService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;
        setBackground(UIConstants.BG_PANEL);
        setLayout(new BorderLayout(0, UIConstants.GAP));
        buildUI();
    }

    private void buildUI() {
        Person user = service.getCurrentUser();

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_PANEL);
        header.add(UIHelper.heading("📋  Fuel Requests"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(UIConstants.BG_PANEL);

        // Only drivers and fleet managers can submit
        if (user.getRole().equals("DRIVER") || user.getRole().equals("FLEET_MANAGER")
            || user.getRole().equals("ADMIN")) {
            JButton submitBtn = UIHelper.primaryButton("+ New Request");
            submitBtn.addActionListener(e -> showSubmitDialog());
            btnPanel.add(submitBtn);
        }
        JButton refreshBtn = UIHelper.successButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refreshTable());
        btnPanel.add(refreshBtn);
        header.add(btnPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Request ID", "Requester", "Vehicle", "Litres", "Purpose", "Status", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(cols);
        table.setModel(tableModel);
        UIHelper.applyAlternatingRows(table);
        applyStatusRenderer();

        JScrollPane sp = UIHelper.scrollPane(table);
        add(sp, BorderLayout.CENTER);

        // Action buttons
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionBar.setBackground(UIConstants.BG_PANEL);

        if (user.getRole().equals("ADMIN") || user.getRole().equals("FLEET_MANAGER")) {
            JButton approveBtn = UIHelper.successButton("✔ Approve");
            JButton rejectBtn  = UIHelper.dangerButton("✘ Reject");
            approveBtn.addActionListener(e -> approveSelected());
            rejectBtn.addActionListener(e -> rejectSelected());
            actionBar.add(approveBtn);
            actionBar.add(rejectBtn);
        }
        add(actionBar, BorderLayout.SOUTH);

        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        Person user = service.getCurrentUser();
        List<FuelRequest> requests;

        if (user.getRole().equals("DRIVER")) {
            requests = service.getRequestsByUser(user.getPersonID());
        } else {
            requests = service.getAllRequests();
        }

        for (FuelRequest r : requests) {
            tableModel.addRow(new Object[]{
                r.getRequestID(), r.getRequesterName(), r.getVehiclePlate(),
                String.format("%.1f L", r.getRequestedLitres()),
                r.getPurpose(), r.getStatus().name(), r.getRequestDateStr()
            });
        }
    }

    private void applyStatusRenderer() {
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String status = v != null ? v.toString() : "";
                if (!sel) {
                    switch (status) {
                        case "PENDING":   setBackground(UIConstants.PENDING_CLR); break;
                        case "APPROVED":  setBackground(UIConstants.APPROVED_CLR); break;
                        case "REJECTED":  setBackground(UIConstants.REJECTED_CLR); break;
                        case "DISPENSED": setBackground(new Color(220, 240, 255)); break;
                        default:          setBackground(UIConstants.BG_WHITE);
                    }
                }
                setFont(UIConstants.FONT_BTN);
                setHorizontalAlignment(CENTER);
                return this;
            }
        });
    }

    private void showSubmitDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Submit Fuel Request",
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 360);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        List<Vehicle> vehicles = service.getAllVehicles();
        String[] vOptions = vehicles.stream()
            .filter(v -> v.getStatus() == Vehicle.VehicleStatus.ACTIVE)
            .map(v -> v.getVehicleID() + " — " + v.getPlateNumber() + " (" + v.getModel() + ")")
            .toArray(String[]::new);

        JComboBox<String> vehicleCb = UIHelper.styledCombo(vOptions);
        vehicleCb.setPreferredSize(new Dimension(280, UIConstants.FIELD_HEIGHT));
        JTextField litresField = UIHelper.styledField();
        litresField.setPreferredSize(new Dimension(280, UIConstants.FIELD_HEIGHT));
        JTextField purposeField = UIHelper.styledField();
        purposeField.setPreferredSize(new Dimension(280, UIConstants.FIELD_HEIGHT));

        int row = 0;
        addFormRow(form, gbc, row++, "Vehicle:", vehicleCb);
        addFormRow(form, gbc, row++, "Litres Requested:", litresField);
        addFormRow(form, gbc, row++, "Purpose:", purposeField);

        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(UIConstants.BG_WHITE);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dlg.dispose());
        JButton submit = UIHelper.primaryButton("Submit Request");
        submit.addActionListener(e -> {
            try {
                String vSel = (String) vehicleCb.getSelectedItem();
                if (vSel == null || vehicles.isEmpty()) {
                    UIHelper.showError(dlg, "No active vehicles available.");
                    return;
                }
                String vehicleID = vSel.split(" — ")[0];
                double litres = Double.parseDouble(litresField.getText().trim());
                String purpose = purposeField.getText().trim();
                if (purpose.isEmpty()) { UIHelper.showError(dlg, "Purpose is required."); return; }
                service.submitFuelRequest(vehicleID, litres, purpose);
                UIHelper.showSuccess(dlg, "Fuel request submitted successfully!");
                dlg.dispose();
                refreshTable();
            } catch (NumberFormatException ex) {
                UIHelper.showError(dlg, "Please enter a valid number for litres.");
            } catch (Exception ex) {
                UIHelper.showError(dlg, ex.getMessage());
            }
        });
        btnRow.add(cancel);
        btnRow.add(submit);
        form.add(btnRow, gbc);

        dlg.setContentPane(form);
        dlg.setVisible(true);
    }

    private void approveSelected() {
        int sel = table.getSelectedRow();
        if (sel < 0) { UIHelper.showError(this, "Please select a request to approve."); return; }
        String reqID = (String) tableModel.getValueAt(sel, 0);
        String status = (String) tableModel.getValueAt(sel, 5);
        if (!status.equals("PENDING")) { UIHelper.showError(this, "Only PENDING requests can be approved."); return; }
        if (UIHelper.confirm(this, "Approve request " + reqID + "?")) {
            try {
                service.approveRequest(reqID);
                UIHelper.showSuccess(this, "Request approved. Fuel token generated.");
                refreshTable();
            } catch (Exception e) {
                UIHelper.showError(this, e.getMessage());
            }
        }
    }

    private void rejectSelected() {
        int sel = table.getSelectedRow();
        if (sel < 0) { UIHelper.showError(this, "Please select a request to reject."); return; }
        String reqID = (String) tableModel.getValueAt(sel, 0);
        String status = (String) tableModel.getValueAt(sel, 5);
        if (!status.equals("PENDING")) { UIHelper.showError(this, "Only PENDING requests can be rejected."); return; }
        String reason = UIHelper.prompt(this, "Enter rejection reason:");
        if (reason == null || reason.trim().isEmpty()) return;
        try {
            service.rejectRequest(reqID, reason);
            UIHelper.showSuccess(this, "Request rejected.");
            refreshTable();
        } catch (Exception e) {
            UIHelper.showError(this, e.getMessage());
        }
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        form.add(UIHelper.label(label), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, gbc);
    }
}
