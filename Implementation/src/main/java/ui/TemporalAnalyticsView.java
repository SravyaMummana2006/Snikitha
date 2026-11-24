package ui;

import analytics.TemporalAnalysisEngine;
import dao.VulnerabilityDao;
import model.TemporalMetric;
import model.Vulnerability;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import service.DataExportService;
import util.ChartUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dedicated temporal analysis interface featuring time-series charts,
 * Month-over-Month (MoM) growth metrics, and chronological distributions.
 */
public class TemporalAnalyticsView extends JPanel {

    private final VulnerabilityDao vulnerabilityDao;
    private final TemporalAnalysisEngine engine;

    private JPanel chartContainer;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel totalMonthsLabel;
    private JLabel peakMonthLabel;
    private JLabel meanMonthlyLabel;

    private List<TemporalMetric> currentMetrics = new ArrayList<>();

    public TemporalAnalyticsView(VulnerabilityDao vulnerabilityDao) {
        this.vulnerabilityDao = vulnerabilityDao;
        this.engine = new TemporalAnalysisEngine();

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        initComponents();
        loadAnalytics();
    }

    private void initComponents() {
        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("Temporal Vulnerability Intelligence & Longitudinal Trends");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton exportBtn = new JButton("Export Temporal CSV");
        exportBtn.addActionListener(e -> exportMetrics());

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(exportBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Split: Top Chart, Bottom Table & Insights
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(null);

        // Chart Container
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(Color.WHITE);
        chartContainer.setBorder(new LineBorder(new Color(225, 230, 235), 1, true));
        splitPane.setTopComponent(chartContainer);

        // Bottom Container with Statistics & Table
        JPanel bottomContainer = new JPanel(new BorderLayout(10, 10));
        bottomContainer.setOpaque(false);

        // Quick Stats Cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setOpaque(false);
        totalMonthsLabel = new JLabel("0", SwingConstants.CENTER);
        peakMonthLabel = new JLabel("N/A", SwingConstants.CENTER);
        meanMonthlyLabel = new JLabel("0.0", SwingConstants.CENTER);

        statsPanel.add(createStatCard("Observation Window", totalMonthsLabel, new Color(24, 119, 242)));
        statsPanel.add(createStatCard("Historical Peak Month", peakMonthLabel, new Color(229, 57, 53)));
        statsPanel.add(createStatCard("Average Monthly Additions", meanMonthlyLabel, new Color(67, 160, 71)));

        bottomContainer.add(statsPanel, BorderLayout.NORTH);

        // Metrics Table
        String[] columns = {"Period (YYYY-MM)", "Total Vulnerabilities", "Ransomware Exploited", "Ransomware Share %", "Unique Vendors", "MoM Growth %"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(225, 230, 235), 1, true));
        bottomContainer.add(scrollPane, BorderLayout.CENTER);

        splitPane.setBottomComponent(bottomContainer);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 230, 235), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLbl.setForeground(new Color(108, 117, 125));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(color);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void loadAnalytics() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private JFreeChart trendChart;
            private Map<String, Double> momGrowth;
            private Optional<TemporalMetric> peak;
            private double meanMonthly;

            @Override
            protected Void doInBackground() {
                List<Vulnerability> vulns = vulnerabilityDao.findAll();
                currentMetrics = engine.computeMonthlyMetrics(vulns);
                momGrowth = engine.computeMonthOverMonthGrowth(currentMetrics);
                peak = engine.findPeakMonth(currentMetrics);
                meanMonthly = engine.computeMeanMonthlyAdditions(currentMetrics);
                trendChart = ChartUtils.createTemporalTrendChart(currentMetrics);
                return null;
            }

            @Override
            protected void done() {
                chartContainer.removeAll();
                chartContainer.add(new ChartPanel(trendChart), BorderLayout.CENTER);
                chartContainer.revalidate();
                chartContainer.repaint();

                totalMonthsLabel.setText(currentMetrics.size() + " Calendar Months");
                peak.ifPresentOrElse(
                        p -> peakMonthLabel.setText(p.getYearMonthStr() + " (" + p.getTotalVulnerabilities() + ")"),
                        () -> peakMonthLabel.setText("N/A")
                );
                meanMonthlyLabel.setText(String.format("%.1f CVEs / Month", meanMonthly));

                tableModel.setRowCount(0);
                for (TemporalMetric m : currentMetrics) {
                    Double growth = momGrowth.get(m.getYearMonthStr());
                    String growthStr = growth != null ? String.format("%+.1f%%", growth) : "--";
                    double ratioPct = m.getRansomwareRatio() * 100.0;

                    tableModel.addRow(new Object[]{
                            m.getYearMonthStr(),
                            m.getTotalVulnerabilities(),
                            m.getRansomwareAssociatedCount(),
                            String.format("%.1f%%", ratioPct),
                            m.getUniqueVendorsCount(),
                            growthStr
                    });
                }
            }
        };
        worker.execute();
    }

    private void exportMetrics() {
        if (currentMetrics.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No metrics to export.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("temporal_metrics.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                DataExportService.exportTemporalMetricsToCsv(currentMetrics, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
