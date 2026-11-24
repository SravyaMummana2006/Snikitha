package ui;

import dao.DatabaseManager;
import dao.ImportMetadataDao;
import dao.ImportMetadataDaoImpl;
import dao.VulnerabilityDao;
import model.ImportMetadata;
import service.CisaDataImporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

/**
 * Data Ingestion and Historical Audit Management view.
 * Utilizes SwingWorker for non-blocking asynchronous file imports and database population.
 */
public class DataImportView extends JPanel {

    private final VulnerabilityDao vulnerabilityDao;
    private final ImportMetadataDao importMetadataDao;
    private final CisaDataImporter importer;

    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JButton importCsvBtn;
    private JButton importJsonBtn;
    private JButton loadBundledBtn;
    private JButton resetDbBtn;

    public DataImportView(VulnerabilityDao vulnerabilityDao) {
        this.vulnerabilityDao = vulnerabilityDao;
        this.importMetadataDao = new ImportMetadataDaoImpl();
        this.importer = new CisaDataImporter(vulnerabilityDao, importMetadataDao);

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        initComponents();
        loadHistory();
    }

    private void initComponents() {
        // Top Ingestion Controls
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 230, 235), 1, true),
                new TitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), "Dataset Ingestion & Validation Pipeline",
                        TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(33, 37, 41))
        ));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        btnBar.setOpaque(false);

        loadBundledBtn = new JButton("Load Bundled Dataset (Dataset/known_exploited_vulnerabilities.csv)");
        loadBundledBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loadBundledBtn.setBackground(new Color(24, 119, 242));
        loadBundledBtn.setForeground(Color.WHITE);
        loadBundledBtn.addActionListener(e -> loadBundledDataset());

        importCsvBtn = new JButton("Browse CSV File...");
        importCsvBtn.addActionListener(e -> chooseAndImportFile(true));

        importJsonBtn = new JButton("Browse JSON File...");
        importJsonBtn.addActionListener(e -> chooseAndImportFile(false));

        resetDbBtn = new JButton("Purge Database");
        resetDbBtn.setForeground(new Color(229, 57, 53));
        resetDbBtn.addActionListener(e -> resetDatabase());

        btnBar.add(loadBundledBtn);
        btnBar.add(importCsvBtn);
        btnBar.add(importJsonBtn);
        btnBar.add(resetDbBtn);

        topPanel.add(btnBar, BorderLayout.NORTH);

        // Progress Section
        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(new EmptyBorder(0, 15, 12, 15));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(100, 22));

        statusLabel = new JLabel("System Ready. Select an official dataset file to import.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(108, 117, 125));

        progressPanel.add(statusLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        topPanel.add(progressPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Center History Table
        JPanel historyContainer = new JPanel(new BorderLayout(5, 5));
        historyContainer.setOpaque(false);

        JLabel historyTitle = new JLabel("Data Ingestion Audit Log & Import Provenance");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        historyContainer.add(historyTitle, BorderLayout.NORTH);

        String[] cols = {"Import ID", "Timestamp", "Format", "Source File", "Records Read", "Valid Inserted", "Duplicates Skipped", "Duration (ms)"};
        historyTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        historyTable.setRowHeight(26);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i <= 2; i++) historyTable.getColumnModel().getColumn(i).setCellRenderer(center);
        for (int i = 4; i <= 7; i++) historyTable.getColumnModel().getColumn(i).setCellRenderer(center);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new LineBorder(new Color(225, 230, 235), 1, true));
        historyContainer.add(scrollPane, BorderLayout.CENTER);

        add(historyContainer, BorderLayout.CENTER);
    }

    private void loadBundledDataset() {
        File csvFile = Paths.get("Dataset", "known_exploited_vulnerabilities.csv").toFile();
        if (!csvFile.exists()) {
            csvFile = Paths.get("..", "Dataset", "known_exploited_vulnerabilities.csv").toFile();
        }
        if (!csvFile.exists()) {
            JOptionPane.showMessageDialog(this, "Bundled CSV file not found at Dataset/known_exploited_vulnerabilities.csv",
                    "File Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        executeImport(csvFile, true);
    }

    private void chooseAndImportFile(boolean isCsv) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(isCsv ? "Select CISA KEV CSV File" : "Select CISA KEV JSON File");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            executeImport(chooser.getSelectedFile(), isCsv);
        }
    }

    private void executeImport(File file, boolean isCsv) {
        setControlsEnabled(false);
        progressBar.setValue(0);
        statusLabel.setText("Starting import for " + file.getName() + "...");

        SwingWorker<CisaDataImporter.ImportResult, String> worker = new SwingWorker<>() {
            @Override
            protected CisaDataImporter.ImportResult doInBackground() {
                CisaDataImporter.ProgressListener listener = (current, total, status) -> {
                    int pct = total > 0 ? (int) (((double) current / total) * 100) : 0;
                    setProgress(pct);
                    publish(status + " (" + current + "/" + total + ")");
                };

                if (isCsv) {
                    return importer.importCsv(file, listener);
                } else {
                    return importer.importJson(file, listener);
                }
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    statusLabel.setText(chunks.get(chunks.size() - 1));
                    progressBar.setValue(getProgress());
                }
            }

            @Override
            protected void done() {
                setControlsEnabled(true);
                try {
                    CisaDataImporter.ImportResult res = get();
                    progressBar.setValue(100);
                    statusLabel.setText(String.format("Import Completed! %,d valid inserted, %,d duplicates skipped in %d ms.",
                            res.getInsertedCount(), res.getDuplicateCount(), res.getDurationMs()));
                    loadHistory();
                    JOptionPane.showMessageDialog(DataImportView.this,
                            String.format("Dataset import successful!\n\n" +
                                            "Total Read: %,d\n" +
                                            "Successfully Inserted: %,d\n" +
                                            "Duplicates Filtered: %,d\n" +
                                            "Time Taken: %d ms",
                                    res.getTotalRead(), res.getInsertedCount(), res.getDuplicateCount(), res.getDurationMs()),
                            "Import Summary", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    statusLabel.setText("Import failed: " + ex.getMessage());
                    JOptionPane.showMessageDialog(DataImportView.this, "Import Error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        worker.execute();
    }

    public void loadHistory() {
        SwingWorker<List<ImportMetadata>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ImportMetadata> doInBackground() {
                return importMetadataDao.getImportHistory();
            }

            @Override
            protected void done() {
                try {
                    List<ImportMetadata> list = get();
                    historyTableModel.setRowCount(0);
                    for (ImportMetadata m : list) {
                        historyTableModel.addRow(new Object[]{
                                m.getImportId(),
                                m.getImportTimestamp(),
                                m.getSourceFormat(),
                                m.getFileName(),
                                m.getTotalRecords(),
                                m.getValidRecords(),
                                m.getDuplicatesSkipped(),
                                m.getExecutionTimeMs()
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void resetDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to purge all records from the SQLite database?\nThis will clear all vulnerability entries and import history.",
                "Confirm Database Purge", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseManager.getInstance().resetDatabase();
            loadHistory();
            statusLabel.setText("Database purged and vacuumed. All tables cleared.");
            JOptionPane.showMessageDialog(this, "Database successfully purged.", "Notice", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void setControlsEnabled(boolean enabled) {
        importCsvBtn.setEnabled(enabled);
        importJsonBtn.setEnabled(enabled);
        loadBundledBtn.setEnabled(enabled);
        resetDbBtn.setEnabled(enabled);
    }
}
