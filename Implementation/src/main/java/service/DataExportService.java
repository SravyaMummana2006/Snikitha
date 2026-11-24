package service;

import model.EarlyWarningIndicator;
import model.ForecastResult;
import model.TemporalMetric;
import model.Vulnerability;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating structured CSV exports and formatted analytical reports.
 */
public class DataExportService {

    public static void exportVulnerabilitiesToCsv(List<Vulnerability> list, File targetFile) throws IOException {
        try (FileWriter out = new FileWriter(targetFile, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.builder()
                     .setHeader("CVE ID", "Vendor", "Product", "Vulnerability Name", "Date Added",
                             "Due Date", "Ransomware Campaign", "CWEs", "Short Description")
                     .build())) {

            for (Vulnerability v : list) {
                printer.printRecord(
                        v.getCveId(),
                        v.getVendorProject(),
                        v.getProduct(),
                        v.getVulnerabilityName(),
                        v.getDateAdded(),
                        v.getDueDate(),
                        v.getKnownRansomwareCampaignUse(),
                        v.getCwes(),
                        v.getShortDescription()
                );
            }
        }
    }

    public static void exportTemporalMetricsToCsv(List<TemporalMetric> metrics, File targetFile) throws IOException {
        try (FileWriter out = new FileWriter(targetFile, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.builder()
                     .setHeader("Period (YYYY-MM)", "Total Vulnerabilities", "Ransomware Associated",
                             "Ransomware Ratio", "Unique Vendors", "Unique Products")
                     .build())) {

            for (TemporalMetric m : metrics) {
                printer.printRecord(
                        m.getYearMonthStr(),
                        m.getTotalVulnerabilities(),
                        m.getRansomwareAssociatedCount(),
                        String.format("%.4f", m.getRansomwareRatio()),
                        m.getUniqueVendorsCount(),
                        m.getUniqueProductsCount()
                );
            }
        }
    }

    public static void exportAnalyticalReport(File targetFile,
                                              List<TemporalMetric> metrics,
                                              List<EarlyWarningIndicator> warnings,
                                              List<ForecastResult> forecasts) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.println("================================================================================");
            writer.println("   CYBERPULSE: TEMPORAL INTELLIGENCE & THREAT SURGE ANALYTICAL REPORT");
            writer.println("   Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("   Institution: SRM University AP - Advanced Java Programming (CSE 309)");
            writer.println("================================================================================");
            writer.println();

            writer.println("1. EXECUTIVE SUMMARY & TEMPORAL METRICS");
            writer.println("--------------------------------------------------------------------------------");
            int totalVulns = metrics.stream().mapToInt(TemporalMetric::getTotalVulnerabilities).sum();
            int totalRansom = metrics.stream().mapToInt(TemporalMetric::getRansomwareAssociatedCount).sum();
            writer.println(String.format("Total Catalogued KEV Entries Analyzed : %d", totalVulns));
            writer.println(String.format("Known Ransomware Campaign Exploit Use  : %d (%.2f%%)",
                    totalRansom, (totalVulns > 0 ? (totalRansom * 100.0 / totalVulns) : 0.0)));
            writer.println(String.format("Total Observation Period (Months)      : %d", metrics.size()));
            writer.println();

            writer.println("2. EARLY WARNING SURGE INDICATORS (Threshold >= 1.5x Baseline)");
            writer.println("--------------------------------------------------------------------------------");
            long alertCount = warnings.stream()
                    .filter(w -> w.getAlertLevel() != EarlyWarningIndicator.AlertLevel.NORMAL)
                    .count();
            writer.println("Surge Months Detected: " + alertCount);
            for (EarlyWarningIndicator w : warnings) {
                if (w.getAlertLevel() != EarlyWarningIndicator.AlertLevel.NORMAL) {
                    writer.println(String.format(" - [%s] %s: Count=%d, Baseline=%.1f (Surge Ratio: %.2fx) -> %s",
                            w.getAlertLevel(), w.getYearMonth(), w.getCurrentMonthCount(),
                            w.getBaselineMovingAverage(), w.getSurgeRatio(), w.getRationale()));
                }
            }
            writer.println();

            writer.println("3. TIME-SERIES FORECASTING MODEL COMPARISON");
            writer.println("--------------------------------------------------------------------------------");
            writer.println(String.format("%-25s | %-10s | %-10s | %-15s", "Algorithm", "MAE", "RMSE", "Next Period"));
            writer.println("--------------------------------------------------------------------------------");
            for (ForecastResult f : forecasts) {
                writer.println(String.format("%-25s | %-10.2f | %-10.2f | %-15.1f",
                        f.getModelName(), f.getMae(), f.getRmse(), f.getNextMonthForecast()));
            }
            writer.println();
            writer.println("End of Report.");
        }
    }
}
