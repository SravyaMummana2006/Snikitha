package ui;

import analytics.ForecastingEngine;
import analytics.TemporalAnalysisEngine;
import dao.VulnerabilityDao;
import model.ForecastResult;
import model.TemporalMetric;
import model.Vulnerability;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import util.ChartUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

/**
 * Predictive forecasting and model comparison interface.
 * Evaluates MAE and RMSE performance across multiple time-series algorithms.
 */
public class ForecastingView extends JPanel {

    private final VulnerabilityDao vulnerabilityDao;
    private final TemporalAnalysisEngine temporalEngine;
    private final ForecastingEngine forecastingEngine;

    private JPanel chartContainer;
    private JTable modelTable;
    private DefaultTableModel tableModel;
    private JLabel bestModelLabel;

    public ForecastingView(VulnerabilityDao vulnerabilityDao) {
        this.vulnerabilityDao = vulnerabilityDao;
        this.temporalEngine = new TemporalAnalysisEngine();
        this.forecastingEngine = new ForecastingEngine();

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        initComponents();
        runForecasting();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Time-Series Vulnerability Addition Forecasting & Model Evaluation");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        bestModelLabel = new JLabel("Evaluating models...");
        bestModelLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bestModelLabel.setForeground(new Color(24, 119, 242));

        header.add(title, BorderLayout.WEST);
        header.add(bestModelLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Center SplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.60);
        splitPane.setBorder(null);

        // Chart Container
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(Color.WHITE);
        chartContainer.setBorder(new LineBorder(new Color(225, 230, 235), 1, true));
        splitPane.setTopComponent(chartContainer);

        // Bottom Table & Academic Note
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setOpaque(false);

        String[] cols = {"Forecasting Technique", "MAE (Mean Abs Error)", "RMSE (Root Mean Sq Error)", "Next-Month Prediction", "Methodology Description"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        modelTable = new JTable(tableModel);
        modelTable.setRowHeight(28);
        modelTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        modelTable.getColumnModel().getColumn(1).setCellRenderer(center);
        modelTable.getColumnModel().getColumn(2).setCellRenderer(center);
        modelTable.getColumnModel().getColumn(3).setCellRenderer(center);

        JScrollPane scrollPane = new JScrollPane(modelTable);
        scrollPane.setBorder(new LineBorder(new Color(225, 230, 235), 1, true));
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        // Methodology note
        JLabel disclaimer = new JLabel("<html><b>Academic Notice:</b> Forecasts estimate future CISA catalog additions based on historical disclosure rates. " +
                "Catalog additions reflect regulatory confirmation, not live attack frequencies or operational threat forecasts.</html>");
        disclaimer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        disclaimer.setForeground(new Color(108, 117, 125));
        bottomPanel.add(disclaimer, BorderLayout.SOUTH);

        splitPane.setBottomComponent(bottomPanel);
        add(splitPane, BorderLayout.CENTER);
    }

    public void runForecasting() {
        SwingWorker<List<ForecastResult>, Void> worker = new SwingWorker<>() {
            private JFreeChart comparisonChart;

            @Override
            protected List<ForecastResult> doInBackground() {
                List<Vulnerability> vulns = vulnerabilityDao.findAll();
                List<TemporalMetric> metrics = temporalEngine.computeMonthlyMetrics(vulns);
                List<ForecastResult> results = forecastingEngine.compareAllModels(metrics);
                comparisonChart = ChartUtils.createForecastComparisonChart(results);
                return results;
            }

            @Override
            protected void done() {
                try {
                    List<ForecastResult> results = get();
                    chartContainer.removeAll();
                    chartContainer.add(new ChartPanel(comparisonChart), BorderLayout.CENTER);
                    chartContainer.revalidate();
                    chartContainer.repaint();

                    tableModel.setRowCount(0);
                    for (ForecastResult res : results) {
                        tableModel.addRow(new Object[]{
                                res.getModelName(),
                                String.format("%.2f", res.getMae()),
                                String.format("%.2f", res.getRmse()),
                                String.format("%.1f additions", res.getNextMonthForecast()),
                                res.getDescription()
                        });
                    }

                    results.stream()
                            .min(Comparator.comparingDouble(ForecastResult::getRmse))
                            .ifPresent(best -> bestModelLabel.setText("★ Recommended Model (Lowest RMSE): " + best.getModelName()));

                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }
}
