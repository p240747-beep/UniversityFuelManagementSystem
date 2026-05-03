package com.university.fms.ui;

import com.university.fms.model.Person;
import com.university.fms.service.FuelManagementService;
import com.university.fms.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;

/**
 * Login screen — UC-01: User Registration and Login.
 */
public class LoginFrame extends JFrame {
    private final FuelManagementService service;
    private JTextField emailField;
    private JPasswordField passField;
    private JLabel statusLabel;

    public LoginFrame(FuelManagementService service) {
        this.service = service;
        initUI();
    }

    private void initUI() {
        setTitle("University Fuel Management System — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(900, 560);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BG_PANEL);

        // LEFT SIDE — branding
        JPanel left = new JPanel();
        left.setBackground(UIConstants.BG_DARK);
        left.setPreferredSize(new Dimension(380, 0));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(60, 40, 40, 40));

        JLabel logo = new JLabel("⛽");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        logo.setForeground(UIConstants.ACCENT);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("UFMS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("University Fuel Management");
        sub.setFont(UIConstants.FONT_LABEL);
        sub.setForeground(UIConstants.TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        left.add(Box.createVerticalGlue());
        left.add(logo);
        left.add(Box.createVerticalStrut(15));
        left.add(title);
        left.add(Box.createVerticalStrut(8));
        left.add(sub);
        left.add(Box.createVerticalStrut(40));

        // Demo credentials hint
        String[] hints = {
            "admin@uni.edu.pk / admin123  → Administrator",
            "bilal@uni.edu.pk / pass123   → Driver",
            "sara@uni.edu.pk  / pass123   → Fleet Manager",
            "nadia@uni.edu.pk / pass123   → Finance Officer",
            "hamid@uni.edu.pk / pass123   → Pump Operator"
        };
        JPanel hintPanel = new JPanel();
        hintPanel.setBackground(new Color(35, 50, 75));
        hintPanel.setLayout(new BoxLayout(hintPanel, BoxLayout.Y_AXIS));
        hintPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel hintTitle = new JLabel("Demo Accounts:");
        hintTitle.setFont(UIConstants.FONT_SMALL);
        hintTitle.setForeground(UIConstants.ACCENT);
        hintPanel.add(hintTitle);
        hintPanel.add(Box.createVerticalStrut(4));
        for (String h : hints) {
            JLabel hl = new JLabel(h);
            hl.setFont(new Font("Monospaced", Font.PLAIN, 10));
            hl.setForeground(new Color(170, 185, 210));
            hintPanel.add(hl);
        }
        left.add(hintPanel);
        left.add(Box.createVerticalGlue());

        // RIGHT SIDE — login form
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(UIConstants.BG_PANEL);
        right.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.gridx = 0;

        JLabel loginTitle = new JLabel("Sign In");
        loginTitle.setFont(UIConstants.FONT_TITLE);
        loginTitle.setForeground(UIConstants.TEXT_MAIN);
        gbc.gridy = 0;
        right.add(loginTitle, gbc);

        JLabel welcome = UIHelper.label("Welcome back! Please sign in to continue.");
        welcome.setForeground(UIConstants.TEXT_MUTED);
        gbc.gridy = 1;
        right.add(welcome, gbc);

        gbc.insets = new Insets(4, 0, 2, 0);
        gbc.gridy = 2;
        right.add(UIHelper.label("Email Address"), gbc);

        emailField = UIHelper.styledField();
        emailField.setPreferredSize(new Dimension(300, UIConstants.FIELD_HEIGHT));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 8, 0);
        right.add(emailField, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(4, 0, 2, 0);
        right.add(UIHelper.label("Password"), gbc);

        passField = UIHelper.styledPasswordField();
        passField.setPreferredSize(new Dimension(300, UIConstants.FIELD_HEIGHT));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 8, 0);
        right.add(passField, gbc);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.DANGER);
        gbc.gridy = 6;
        right.add(statusLabel, gbc);

        JButton loginBtn = UIHelper.primaryButton("Sign In →");
        loginBtn.setPreferredSize(new Dimension(300, 40));
        gbc.gridy = 7;
        gbc.insets = new Insets(4, 0, 0, 0);
        right.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> doLogin());
        passField.addActionListener(e -> doLogin());
        emailField.addActionListener(e -> passField.requestFocus());

        root.add(left, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword());
        if (email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please enter email and password.");
            return;
        }
        Optional<Person> user = service.login(email, pass);
        if (user.isPresent()) {
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame(service).setVisible(true));
        } else {
            statusLabel.setText("Invalid email or password. Please try again.");
            passField.setText("");
            passField.requestFocus();
        }
    }
}
