package ui;

import dao.ManualThreatDao;
import model.ManualThreatRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Analyst incident logging and manual threat record management interface.
 * Implements complete CRUD capabilities for locally observed threats.
 */
public class ThreatManagementView extends JPanel {

    private final ManualThreatDao manualThreatDao;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<ManualThreatRecord> currentRecords = new ArrayList<>();

    // Form inputs
    private JTextField nameField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> severityCombo;
    private JTextField systemField;
    private JComboBox<String> statusCombo;
    private JTextField notesField;

    public ThreatManagementView(ManualThreatDao manualThreatDao) {
        this.manualThreatDao = manualThreatDao;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        initComponents();
        loadRecords();
    }

    private void initComponents() {
        // North Panel: Form
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 230, 235), 1, true),
                new TitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), "Log New Threat Incident / Investigation",
                        TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(33, 37, 41))
        ));

        JPanel formGrid = new JPanel(new GridLayout(2, 6, 10, 8));
        formGrid.setOpaque(false);
        formGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        nameField = new JTextField();
        typeCombo = new JComboBox<>(new String[]{"Zero-Day", "Ransomware", "Phishing Campaign", "Supply-Chain Exploit", "DDoS Attack", "Credential Stuffing"});
        severityCombo = new JComboBox<>(new String[]{"CRITICAL", "HIGH", "MEDIUM", "LOW"});
        systemField = new JTextField();
        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INVESTIGATING", "MITIGATED", "CLOSED"});
        notesField = new JTextField();

        formGrid.add(new JLabel("Threat Title:"));
        formGrid.add(new JLabel("Threat Type:"));
        formGrid.add(new JLabel("Severity:"));
        formGrid.add(new JLabel("Affected Asset:"));
        formGrid.add(new JLabel("Status:"));
        formGrid.add(new JLabel("Remediation Notes:"));

        formGrid.add(nameField);
        formGrid.add(typeCombo);
        formGrid.add(severityCombo);
        formGrid.add(systemField);
        formGrid.add(statusCombo);
        formGrid.add(notesField);

        formContainer.add(formGrid, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnBar.setOpaque(false);
        JButton saveBtn = new JButton("Add Threat Record");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.addActionListener(e -> saveThreat());
        btnBar.add(saveBtn);

        formContainer.add(btnBar, BorderLayout.SOUTH);
        add(formContainer, BorderLayout.NORTH);

        // Center Panel: Table with action buttons
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        String[] cols = {"ID", "Threat Name", "Type", "Severity", "Date Observed", "Affected Asset", "Status", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);
        table.getColumnModel().getColumn(4).setCellRenderer(center);
        table.getColumnModel().getColumn(6).setCellRenderer(center);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(225, 230, 235), 1, true));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Table Bottom Actions
        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        tableActions.setOpaque(false);

        JButton mitigateBtn = new JButton("Toggle Mitigated");
        mitigateBtn.addActionListener(e -> toggleMitigated());

        JButton deleteBtn = new JButton("Delete Record");
        deleteBtn.addActionListener(e -> deleteSelected());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadRecords());

        tableActions.add(mitigateBtn);
        tableActions.add(deleteBtn);
        tableActions.add(refreshBtn);

        centerPanel.add(tableActions, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void saveThreat() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Threat Title.", "Validation Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ManualThreatRecord r = new ManualThreatRecord();
        r.setThreatName(name);
        r.setThreatType((String) typeCombo.getSelectedItem());
        r.setSeverity((String) severityCombo.getSelectedItem());
        r.setDateObserved(LocalDate.now());
        r.setAffectedSystem(systemField.getText().trim().isEmpty() ? "Internal Infrastructure" : systemField.getText().trim());
        r.setStatus((String) statusCombo.getSelectedItem());
        r.setNotes(notesField.getText().trim());

        try {
            manualThreatDao.insert(r);
            nameField.setText("");
            systemField.setText("");
            notesField.setText("");
            loadRecords();
            JOptionPane.showMessageDialog(this, "Incident recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadRecords() {
        SwingWorker<List<ManualThreatRecord>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ManualThreatRecord> doInBackground() {
                return manualThreatDao.findAll();
            }

            @Override
            protected void done() {
                try {
                    currentRecords = get();
                    tableModel.setRowCount(0);
                    for (ManualThreatRecord r : currentRecords) {
                        tableModel.addRow(new Object[]{
                                r.getId(),
                                r.getThreatName(),
                                r.getThreatType(),
                                r.getSeverity(),
                                r.getDateObserved(),
                                r.getAffectedSystem(),
                                r.getStatus(),
                                r.getNotes()
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void toggleMitigated() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a threat record from the table.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ManualThreatRecord r = currentRecords.get(row);
        r.setStatus("MITIGATED".equalsIgnoreCase(r.getStatus()) ? "ACTIVE" : "MITIGATED");
        manualThreatDao.update(r);
        loadRecords();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a threat record to delete.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            manualThreatDao.delete(currentRecords.get(row).getId());
            loadRecords();
        }
    }
}
