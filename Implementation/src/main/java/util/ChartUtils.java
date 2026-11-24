package util;

import model.ForecastResult;
import model.TemporalMetric;
import model.VendorRiskMetric;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Chart generator utility for rendering polished, modern JFreeCharts
 * integrated seamlessly with the FlatLaf theme.
 */
public final class ChartUtils {

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font AXIS_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Color GRID_COLOR = new Color(220, 224, 230);
    private static final Color PRIMARY_BLUE = new Color(24, 119, 242);
    private static final Color DANGER_RED = new Color(229, 57, 53);
    private static final Color AMBER_ORANGE = new Color(251, 140, 0);
    private static final Color EMERALD_GREEN = new Color(67, 160, 71);
    private static final Color PURPLE_ACCENT = new Color(142, 36, 170);

    private ChartUtils() {
    }

    /**
     * Creates a multi-line chart for Monthly Vulnerabilities and Ransomware Trend.
     */
    public static JFreeChart createTemporalTrendChart(List<TemporalMetric> metrics) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (TemporalMetric m : metrics) {
            dataset.addValue(m.getTotalVulnerabilities(), "Total Vulnerabilities", m.getYearMonthStr());
            dataset.addValue(m.getRansomwareAssociatedCount(), "Ransomware Associated", m.getYearMonthStr());
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Monthly Catalogued Vulnerabilities & Ransomware Trend",
                "Month (YYYY-MM)",
                "Additions Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        applyLineChartStyling(chart, PRIMARY_BLUE, DANGER_RED);
        return chart;
    }

    /**
     * Creates a Bar Chart for Top Vulnerable Vendors.
     */
    public static JFreeChart createVendorRiskBarChart(List<VendorRiskMetric> vendors) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (VendorRiskMetric v : vendors) {
            dataset.addValue(v.getTotalCves(), "Total CVEs", v.getVendorName());
            dataset.addValue(v.getRansomwareCves(), "Ransomware Exploited", v.getVendorName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Top Exploited Vendors & Ransomware Distribution",
                "Vendor / Project",
                "CVE Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        applyBarChartStyling(chart, PRIMARY_BLUE, DANGER_RED);
        return chart;
    }

    /**
     * Creates a Pie Chart for Top CWE Weaknesses.
     */
    public static JFreeChart createCwePieChart(Map<String, Integer> topCwes) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Map.Entry<String, Integer> entry : topCwes.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Distribution of Primary Common Weakness Enumerations (CWEs)",
                dataset,
                true,
                true,
                false
        );

        chart.getTitle().setFont(TITLE_FONT);
        chart.setBackgroundPaint(Color.WHITE);

        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(AXIS_FONT);
        plot.setSimpleLabels(true);

        return chart;
    }

    /**
     * Creates a Comparative Forecast Line Chart.
     */
    public static JFreeChart createForecastComparisonChart(List<ForecastResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        if (!results.isEmpty()) {
            ForecastResult first = results.get(0);
            List<String> periods = first.getPeriods();
            List<Double> actuals = first.getActualValues();

            // Add actual historical series
            for (int i = 0; i < periods.size(); i++) {
                dataset.addValue(actuals.get(i), "Actual Additions", periods.get(i));
            }

            // Add each model's forecast series
            for (ForecastResult res : results) {
                List<Double> forecasts = res.getForecastValues();
                for (int i = 0; i < periods.size(); i++) {
                    dataset.addValue(forecasts.get(i), res.getModelName(), periods.get(i));
                }
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Time-Series Forecasting Model Comparison vs Actual Historicals",
                "Period (YYYY-MM)",
                "Additions Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        applyLineChartStyling(chart, Color.BLACK, PRIMARY_BLUE, EMERALD_GREEN, AMBER_ORANGE, PURPLE_ACCENT);
        return chart;
    }

    private static void applyLineChartStyling(JFreeChart chart, Color... seriesColors) {
        chart.getTitle().setFont(TITLE_FONT);
        chart.setBackgroundPaint(Color.WHITE);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setDomainGridlinePaint(GRID_COLOR);
        plot.setOutlineVisible(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(AXIS_FONT);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        for (int i = 0; i < seriesColors.length; i++) {
            renderer.setSeriesPaint(i, seriesColors[i]);
            renderer.setSeriesStroke(i, new BasicStroke(2.2f));
            renderer.setSeriesShapesVisible(i, true);
        }
    }

    private static void applyBarChartStyling(JFreeChart chart, Color primary, Color secondary) {
        chart.getTitle().setFont(TITLE_FONT);
        chart.setBackgroundPaint(Color.WHITE);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setOutlineVisible(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(AXIS_FONT);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, primary);
        renderer.setSeriesPaint(1, secondary);
        renderer.setDrawBarOutline(false);
        renderer.setItemMargin(0.05);
    }
}
