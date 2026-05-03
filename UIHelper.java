package com.university.fms.util;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Factory methods for building consistent UI components.
 */
public final class UIHelper {
    private UIHelper() {}

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.FONT_BTN);
        btn.setBackground(UIConstants.PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(UIConstants.BTN_SIZE);
        btn.setOpaque(true);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(UIConstants.PRIMARY_DARK); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(UIConstants.PRIMARY); }
        });
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.FONT_BTN);
        btn.setBackground(UIConstants.DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(UIConstants.BTN_SIZE);
        btn.setOpaque(true);
        return btn;
    }

    public static JButton successButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.FONT_BTN);
        btn.setBackground(UIConstants.SUCCESS);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(UIConstants.BTN_SIZE);
        btn.setOpaque(true);
        return btn;
    }

    public static JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(UIConstants.FONT_INPUT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        f.setPreferredSize(new Dimension(200, UIConstants.FIELD_HEIGHT));
        return f;
    }

    public static JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(UIConstants.FONT_INPUT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        f.setPreferredSize(new Dimension(200, UIConstants.FIELD_HEIGHT));
        return f;
    }

    public static JComboBox<String> styledCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(UIConstants.FONT_INPUT);
        cb.setBackground(UIConstants.BG_WHITE);
        cb.setPreferredSize(new Dimension(200, UIConstants.FIELD_HEIGHT));
        return cb;
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.FONT_HEADING);
        l.setForeground(UIConstants.PRIMARY);
        return l;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.FONT_LABEL);
        l.setForeground(UIConstants.TEXT_MAIN);
        return l;
    }

    public static JTable styledTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(UIConstants.FONT_TABLE);
        table.setRowHeight(28);
        table.setGridColor(UIConstants.BORDER);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(210, 225, 255));
        table.setSelectionForeground(UIConstants.TEXT_MAIN);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setFont(UIConstants.FONT_BTN);
        header.setBackground(UIConstants.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        return table;
    }

    public static JScrollPane scrollPane(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        sp.getViewport().setBackground(UIConstants.BG_WHITE);
        return sp;
    }

    public static JPanel cardPanel(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.BG_WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        if (title != null && !title.isEmpty()) {
            JLabel lbl = heading(title);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            p.add(lbl, BorderLayout.NORTH);
        }
        return p;
    }

    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static String prompt(Component parent, String message) {
        return JOptionPane.showInputDialog(parent, message);
    }

    /** Alternate row colors in a JTable renderer */
    public static void applyAlternatingRows(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? UIConstants.BG_WHITE : UIConstants.ROW_ALT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return this;
            }
        });
    }
}
