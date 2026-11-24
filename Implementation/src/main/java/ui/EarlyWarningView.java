package ui;

import analytics.EarlyWarningEngine;
import analytics.TemporalAnalysisEngine;
import dao.VulnerabilityDao;
import model.EarlyWarningIndicator;
import model.EarlyWarningIndicator.AlertLevel;
import model.TemporalMetric;
import model.Vulnerability;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Early Warning surge monitoring interface with configurable baselines,
 * surge ratio calculations, and threat severity color-coding.
 */
public class EarlyWarningView extends JPanel {

    private final VulnerabilityDao vulnerabilityDao;
    private final TemporalAnalysisEngine temporalEngine;

    private JComboBox<Integer> windowCombo;
    private JComboBox<String> levelFilterCombo;
    private JTable table;
    private DefaultTableModel tableModel;

    private JLabel criticalBadge;
    private JLabel highBadge;
    private JLabel elevatedBadge;
    private JLabel normalBadge;

    private List<EarlyWarningIndicator> allIndicators = new ArrayList<>();

    public EarlyWarningView(VulnerabilityDao vulnerabilityDao) {
        this.vulnerabilityDao = vulnerabilityDao;
        this.temporalEngine = new TemporalAnalysisEngine();

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        initComponents();
        loadWarnings();
    }

    private void initComponents() {
        // Top Filter Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(new Color(225, 230, 235), 1, true));

        windowCombo = new JComboBox<>(new Integer[]{3, 6, 12});
        windowCombo.addActionListener(e -> loadWarnings());

        levelFilterCombo = new JComboBox<>(new String[]{"All Levels", "Surges Only (≥1.5x)", "Critical (≥2.5x)"});
        levelFilterCombo.addActionListener(e -> filterTableDisplay());

        topBar.add(new JLabel("Trailing Baseline Window:"));
        topBar.add(windowCombo);
        topBar.add(new JLabel("months"));
        topBar.add(Box.createHorizontalStrut(15));
        topBar.add(new JLabel("Severity Filter:"));
        topBar.add(levelFilterCombo);

        add(topBar, BorderLayout.NORTH);

        // Center Panel: Summary Badges + Data Table
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // Surge Badges
        JPanel badgesPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        badgesPanel.setOpaque(false);

        criticalBadge = new JLabel("0", SwingConstants.CENTER);
        highBadge = new JLabel("0", SwingConstants.CENTER);
        elevatedBadge = new JLabel("0", SwingConstants.CENTER);
        normalBadge = new JLabel("0", SwingConstants.CENTER);

        badgesPanel.add(createBadgePanel("Critical Surges (≥2.5x)", criticalBadge, new Color(229, 57, 53)));
        badgesPanel.add(createBadgePanel("High Surges (≥2.0x)", highBadge, new Color(251, 140, 0)));
        badgesPanel.add(createBadgePanel("Elevated Surges (≥1.5x)", elevatedBadge, new Color(255, 179, 0)));
        badgesPanel.add(createBadgePanel("Normal Months (<1.5x)", normalBadge, new Color(67, 160, 71)));

        centerPanel.add(badgesPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Month (YYYY-MM)", "Monthly Count", "Baseline Avg", "Surge Ratio", "Severity Level", "Detection Rationale"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Center align columns 0-4
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i <= 3; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        // Custom severity renderer with background badge colors
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (value != null) {
                    String str = value.toString();
                    switch (str) {
                        case "CRITICAL" -> {
                            c.setBackground(new Color(255, 235, 238));
                            c.setForeground(new Color(198, 40, 40));
                        }
                        case "HIGH" -> {
                            c.setBackground(new Color(255, 243, 224));
                            c.setForeground(new Color(230, 81, 0));
                        }
                        case "ELEVATED" -> {
                            c.setBackground(new Color(255, 248, 225));
                            c.setForeground(new Color(245, 127, 23));
                        }
                        default -> {
                            c.setBackground(Color.WHITE);
                            c.setForeground(new Color(46, 125, 50));
                        }
                    }
                }
                return c;
            }
        });

        table.getColumnModel().getColumn(5).setPreferredWidth(350);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(225, 230, 235), 1, true));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createBadgePanel(String title, JLabel valueLabel, Color color) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 230, 235), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLbl.setForeground(new Color(108, 117, 125));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);

        p.add(titleLbl, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    public void loadWarnings() {
        Integer window = (Integer) windowCombo.getSelectedItem();
        int winSize = window != null ? window : 3;
        EarlyWarningEngine engine = new EarlyWarningEngine(winSize);

        SwingWorker<List<EarlyWarningIndicator>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<EarlyWarningIndicator> doInBackground() {
                List<Vulnerability> vulns = vulnerabilityDao.findAll();
                List<TemporalMetric> metrics = temporalEngine.computeMonthlyMetrics(vulns);
                return engine.evaluateSurges(metrics);
            }

            @Override
            protected void done() {
                try {
                    allIndicators = get();
                    int crit = 0, high = 0, elev = 0, norm = 0;
                    for (EarlyWarningIndicator ind : allIndicators) {
                        switch (ind.getAlertLevel()) {
                            case CRITICAL -> crit++;
                            case HIGH -> high++;
                            case ELEVATED -> elev++;
                            case NORMAL -> norm++;
                        }
                    }
                    criticalBadge.setText(String.valueOf(crit));
                    highBadge.setText(String.valueOf(high));
                    elevatedBadge.setText(String.valueOf(elev));
                    normalBadge.setText(String.valueOf(norm));

                    filterTableDisplay();
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void filterTableDisplay() {
        String filter = (String) levelFilterCombo.getSelectedItem();
        List<EarlyWarningIndicator> filtered = allIndicators.stream()
                .filter(ind -> {
                    if ("Critical (≥2.5x)".equals(filter)) {
                        return ind.getAlertLevel() == AlertLevel.CRITICAL;
                    } else if ("Surges Only (≥1.5x)".equals(filter)) {
                        return ind.getAlertLevel() != AlertLevel.NORMAL;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        tableModel.setRowCount(0);
        for (EarlyWarningIndicator ind : filtered) {
            tableModel.addRow(new Object[]{
                    ind.getYearMonth(),
                    ind.getCurrentMonthCount(),
                    String.format("%.1f", ind.getBaselineMovingAverage()),
                    String.format("%.2fx", ind.getSurgeRatio()),
                    ind.getAlertLevel().name(),
                    ind.getRationale()
            });
        }
    }
}
