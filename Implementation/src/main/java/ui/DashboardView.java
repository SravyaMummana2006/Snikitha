package ui;

import analytics.EarlyWarningEngine;
import analytics.RansomwareRiskAnalyzer;
import analytics.TemporalAnalysisEngine;
import dao.ManualThreatDao;
import dao.VulnerabilityDao;
import model.EarlyWarningIndicator;
import model.TemporalMetric;
import model.Vulnerability;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import util.ChartUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Modern analytical overview dashboard with KPI metric cards,
 * interactive mini-charts, and quick summaries.
 */
public class DashboardView extends JPanel {

    private final VulnerabilityDao vulnerabilityDao;
    private final ManualThreatDao manualThreatDao;
    private final TemporalAnalysisEngine temporalEngine;
    private final EarlyWarningEngine warningEngine;
    private final RansomwareRiskAnalyzer riskAnalyzer;

    // KPI Card Labels
    private JLabel totalKevLabel;
    private JLabel ransomwareLabel;
    private JLabel vendorsCountLabel;
    private JLabel surgeCountLabel;
    private JLabel manualThreatsLabel;

    // Chart Containers
    private JPanel trendChartContainer;
    private JPanel cweChartContainer;

    public DashboardView(VulnerabilityDao vulnerabilityDao, ManualThreatDao manualThreatDao) {
        this.vulnerabilityDao = vulnerabilityDao;
        this.manualThreatDao = manualThreatDao;
        this.temporalEngine = new TemporalAnalysisEngine();
        this.warningEngine = new EarlyWarningEngine(3);
        this.riskAnalyzer = new RansomwareRiskAnalyzer();

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 247, 250));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // 1. Header with title and refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Executive Threat Intelligence Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));

        JButton refreshBtn = new JButton("Refresh Dashboard");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> refreshData());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content with KPIs and Charts
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // KPI Section
        JPanel kpiGrid = new JPanel(new GridLayout(1, 5, 15, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        kpiGrid.setPreferredSize(new Dimension(1000, 110));

        totalKevLabel = new JLabel("0", SwingConstants.CENTER);
        ransomwareLabel = new JLabel("0", SwingConstants.CENTER);
        vendorsCountLabel = new JLabel("0", SwingConstants.CENTER);
        surgeCountLabel = new JLabel("0", SwingConstants.CENTER);
        manualThreatsLabel = new JLabel("0", SwingConstants.CENTER);

        kpiGrid.add(createKpiCard("Total KEV Catalogued", totalKevLabel, new Color(24, 119, 242)));
        kpiGrid.add(createKpiCard("Ransomware Exploited", ransomwareLabel, new Color(229, 57, 53)));
        kpiGrid.add(createKpiCard("Targeted Vendors", vendorsCountLabel, new Color(142, 36, 170)));
        kpiGrid.add(createKpiCard("Critical Surges Detected", surgeCountLabel, new Color(251, 140, 0)));
        kpiGrid.add(createKpiCard("Active Manual Threats", manualThreatsLabel, new Color(67, 160, 71)));

        contentPanel.add(kpiGrid);
        contentPanel.add(Box.createVerticalStrut(20));

        // Charts Section
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 15, 0));
        chartsGrid.setOpaque(false);
        chartsGrid.setPreferredSize(new Dimension(1000, 450));

        trendChartContainer = new JPanel(new BorderLayout());
        trendChartContainer.setBackground(Color.WHITE);
        trendChartContainer.setBorder(new LineBorder(new Color(225, 230, 235), 1, true));

        cweChartContainer = new JPanel(new BorderLayout());
        cweChartContainer.setBackground(Color.WHITE);
        cweChartContainer.setBorder(new LineBorder(new Color(225, 230, 235), 1, true));

        chartsGrid.add(trendChartContainer);
        chartsGrid.add(cweChartContainer);

        contentPanel.add(chartsGrid);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 230, 235), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(108, 117, 125));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(accentColor);

        // Accent bottom strip
        JPanel strip = new JPanel();
        strip.setBackground(accentColor);
        strip.setPreferredSize(new Dimension(0, 4));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(strip, BorderLayout.SOUTH);

        return card;
    }

    public void refreshData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private int totalKev;
            private int ransomwareCount;
            private int vendorsCount;
            private int surgeCount;
            private int manualThreatCount;
            private JFreeChart trendChart;
            private JFreeChart cweChart;

            @Override
            protected Void doInBackground() {
                totalKev = vulnerabilityDao.getTotalCount();
                ransomwareCount = vulnerabilityDao.getRansomwareCount();
                vendorsCount = vulnerabilityDao.getDistinctVendors().size();
                manualThreatCount = manualThreatDao.getTotalCount();

                List<Vulnerability> all = vulnerabilityDao.findAll();
                List<TemporalMetric> metrics = temporalEngine.computeMonthlyMetrics(all);
                List<EarlyWarningIndicator> warnings = warningEngine.evaluateSurges(metrics);

                surgeCount = (int) warnings.stream()
                        .filter(w -> w.getAlertLevel() == EarlyWarningIndicator.AlertLevel.CRITICAL ||
                                w.getAlertLevel() == EarlyWarningIndicator.AlertLevel.HIGH)
                        .count();

                trendChart = ChartUtils.createTemporalTrendChart(metrics);
                Map<String, Integer> topCwes = riskAnalyzer.computeTopCwes(all, 6);
                cweChart = ChartUtils.createCwePieChart(topCwes);

                return null;
            }

            @Override
            protected void done() {
                totalKevLabel.setText(String.format("%,d", totalKev));
                ransomwareLabel.setText(String.format("%,d", ransomwareCount));
                vendorsCountLabel.setText(String.format("%,d", vendorsCount));
                surgeCountLabel.setText(String.valueOf(surgeCount));
                manualThreatsLabel.setText(String.valueOf(manualThreatCount));

                trendChartContainer.removeAll();
                trendChartContainer.add(new ChartPanel(trendChart), BorderLayout.CENTER);
                trendChartContainer.revalidate();
                trendChartContainer.repaint();

                cweChartContainer.removeAll();
                cweChartContainer.add(new ChartPanel(cweChart), BorderLayout.CENTER);
                cweChartContainer.revalidate();
                cweChartContainer.repaint();
            }
        };
        worker.execute();
    }
}
