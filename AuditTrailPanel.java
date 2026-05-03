package com.university.fms.ui;

import com.university.fms.model.AuditEntry;
import com.university.fms.service.FuelManagementService;
import com.university.fms.util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Audit Trail panel — UC-13: View Audit Trail.
 * Accessible only by Administrator.
 */
public class AuditTrailPanel extends JPanel {
    private final FuelManagementService service;
    private final MainFrame mainFrame;
    private DefaultTableModel tableModel;

    public AuditTrailPanel(FuelManagementService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;
        setBackground(UIConstants.BG_PANEL);
        setLayout(new BorderLayout(0, UIConstants.GAP));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_PANEL);
        header.add(UIHelper.heading("🔍  System Audit Trail"), BorderLayout.WEST);
        JButton refreshBtn = UIHelper.successButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"Entry ID", "Timestamp", "User", "Action", "Details"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UIHelper.styledTable(cols);
        table.setModel(tableModel);
        UIHelper.applyAlternatingRows(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(400);
        add(UIHelper.scrollPane(table), BorderLayout.CENTER);

        JLabel info = UIHelper.label("Showing all system activity. Most recent entries at top.");
        info.setForeground(UIConstants.TEXT_MUTED);
        add(info, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        tableModel.setRowCount(0);
        List<AuditEntry> entries = service.getAuditTrail();
        for (int i = entries.size() - 1; i >= 0; i--) {
            AuditEntry e = entries.get(i);
            tableModel.addRow(new Object[]{
                e.getEntryID(), e.getTimestampStr(), e.getUserName(),
                e.getAction(), e.getDetails()
            });
        }
    }
}
