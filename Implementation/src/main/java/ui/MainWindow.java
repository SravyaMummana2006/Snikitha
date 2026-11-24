package ui;

import config.AppConfig;
import dao.DatabaseManager;
import dao.ManualThreatDao;
import dao.ManualThreatDaoImpl;
import dao.VulnerabilityDao;
import dao.VulnerabilityDaoImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Main application window for CyberPulse.
 * Features modern sidebar navigation, dynamic CardLayout view transitions,
 * and high-contrast professional FlatLaf styling.
 */
public class MainWindow extends JFrame {

    private final VulnerabilityDao vulnerabilityDao;
    private final ManualThreatDao manualThreatDao;

    private JPanel contentContainer;
    private CardLayout cardLayout;
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    private DashboardView dashboardView;
    private VulnerabilityTableView tableView;
    private TemporalAnalyticsView temporalView;
    private EarlyWarningView earlyWarningView;
    private ForecastingView forecastingView;
    private ThreatManagementView threatManagementView;
    private DataImportView importView;

    public MainWindow() {
        this.vulnerabilityDao = new VulnerabilityDaoImpl();
        this.manualThreatDao = new ManualThreatDaoImpl();

        setTitle(AppConfig.APP_NAME + " - " + AppConfig.APP_SUBTITLE + " [" + AppConfig.COURSE_CODE + "]");
        setSize(1366, 850);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Top Brand Banner
        JPanel topBanner = new JPanel(new BorderLayout(15, 0));
        topBanner.setBackground(new Color(18, 30, 49)); // Deep professional navy
        topBanner.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel brandTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandTitlePanel.setOpaque(false);

        JLabel logoLabel = new JLabel("⚡ " + AppConfig.APP_NAME.toUpperCase());
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("| " + AppConfig.APP_SUBTITLE + " (" + AppConfig.COURSE_CODE + ")");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(180, 195, 215));

        brandTitlePanel.add(logoLabel);
        brandTitlePanel.add(subtitleLabel);
        topBanner.add(brandTitlePanel, BorderLayout.WEST);

        // Right side info pill
        JPanel statusPill = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statusPill.setOpaque(false);

        JLabel dbBadge = new JLabel("● SQLite Connected");
        dbBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dbBadge.setForeground(new Color(76, 175, 80));

        JLabel studentBadge = new JLabel("Ch. Devi Snikitha (AP25110070820) | Section G");
        studentBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        studentBadge.setForeground(new Color(220, 230, 245));

        statusPill.add(dbBadge);
        statusPill.add(new JLabel(" | ") {{ setForeground(Color.GRAY); }});
        statusPill.add(studentBadge);

        topBanner.add(statusPill, BorderLayout.EAST);
        add(topBanner, BorderLayout.NORTH);

        // 2. Left Sidebar Navigation
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(24, 38, 59));
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(new EmptyBorder(15, 10, 15, 10));

        JLabel navHeader = new JLabel("NAVIGATION MODULES");
        navHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        navHeader.setForeground(new Color(125, 145, 170));
        navHeader.setBorder(new EmptyBorder(5, 10, 10, 5));
        sidebar.add(navHeader);

        addNavButton(sidebar, "Dashboard", "📊  Executive Dashboard");
        addNavButton(sidebar, "Catalog", "📋  Vulnerability Catalog");
        addNavButton(sidebar, "Temporal", "📈  Temporal Analytics");
        addNavButton(sidebar, "EarlyWarning", "⚠️  Early Warning System");
        addNavButton(sidebar, "Forecasting", "🔮  Threat Forecasting");
        addNavButton(sidebar, "Incidents", "🛡️  Incident Management");
        addNavButton(sidebar, "Ingestion", "📥  Data Ingestion & Audit");

        sidebar.add(Box.createVerticalGlue());

        // Academic Meta Card at sidebar bottom
        JPanel metaCard = new JPanel(new GridLayout(3, 1, 2, 2));
        metaCard.setBackground(new Color(32, 48, 73));
        metaCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(45, 65, 95), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel l1 = new JLabel("SRM UNIVERSITY - AP");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l1.setForeground(new Color(255, 193, 7));

        JLabel l2 = new JLabel("Advanced Java (CSE 309)");
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l2.setForeground(Color.LIGHT_GRAY);

        JLabel l3 = new JLabel("Advisor: Gavaskar S");
        l3.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l3.setForeground(Color.WHITE);

        metaCard.add(l1);
        metaCard.add(l2);
        metaCard.add(l3);
        sidebar.add(metaCard);

        add(sidebar, BorderLayout.WEST);

        // 3. Center Content Area (CardLayout)
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);

        dashboardView = new DashboardView(vulnerabilityDao, manualThreatDao);
        tableView = new VulnerabilityTableView(vulnerabilityDao);
        temporalView = new TemporalAnalyticsView(vulnerabilityDao);
        earlyWarningView = new EarlyWarningView(vulnerabilityDao);
        forecastingView = new ForecastingView(vulnerabilityDao);
        threatManagementView = new ThreatManagementView(manualThreatDao);
        importView = new DataImportView(vulnerabilityDao);

        contentContainer.add(dashboardView, "Dashboard");
        contentContainer.add(tableView, "Catalog");
        contentContainer.add(temporalView, "Temporal");
        contentContainer.add(earlyWarningView, "EarlyWarning");
        contentContainer.add(forecastingView, "Forecasting");
        contentContainer.add(threatManagementView, "Incidents");
        contentContainer.add(importView, "Ingestion");

        add(contentContainer, BorderLayout.CENTER);

        // Default to Dashboard
        selectView("Dashboard");
    }

    private void addNavButton(JPanel sidebar, String viewKey, String labelText) {
        JButton btn = new JButton(labelText);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(24, 38, 59));
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        btn.addActionListener(e -> selectView(viewKey));

        navButtons.put(viewKey, btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(4));
    }

    private void selectView(String viewKey) {
        cardLayout.show(contentContainer, viewKey);

        // Highlight active navigation button
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(viewKey)) {
                entry.getValue().setBackground(new Color(24, 119, 242)); // Active royal blue
                entry.getValue().setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                entry.getValue().setBackground(new Color(24, 38, 59));
                entry.getValue().setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
        }

        // Trigger auto-refresh for reactive views
        switch (viewKey) {
            case "Dashboard" -> dashboardView.refreshData();
            case "Catalog" -> {
                tableView.loadVendors();
                tableView.loadData();
            }
            case "Temporal" -> temporalView.loadAnalytics();
            case "EarlyWarning" -> earlyWarningView.loadWarnings();
            case "Forecasting" -> forecastingView.runForecasting();
            case "Incidents" -> threatManagementView.loadRecords();
            case "Ingestion" -> importView.loadHistory();
        }
    }
}
