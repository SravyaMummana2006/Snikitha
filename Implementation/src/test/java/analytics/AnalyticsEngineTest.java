package analytics;

import model.EarlyWarningIndicator;
import model.ForecastResult;
import model.TemporalMetric;
import model.VendorRiskMetric;
import model.Vulnerability;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsEngineTest {

    @Test
    void testTemporalAnalysisEngine() {
        TemporalAnalysisEngine engine = new TemporalAnalysisEngine();

        Vulnerability v1 = new Vulnerability("CVE-2024-001", "VendorA", "Prod1", "Flaw", LocalDate.of(2024, 1, 5), null, null, null, "Known", null, null, null);
        Vulnerability v2 = new Vulnerability("CVE-2024-002", "VendorA", "Prod2", "Flaw", LocalDate.of(2024, 1, 20), null, null, null, "Unknown", null, null, null);
        Vulnerability v3 = new Vulnerability("CVE-2024-003", "VendorB", "Prod3", "Flaw", LocalDate.of(2024, 2, 10), null, null, null, "Known", null, null, null);

        List<TemporalMetric> metrics = engine.computeMonthlyMetrics(Arrays.asList(v1, v2, v3));

        assertEquals(2, metrics.size());
        assertEquals("2024-01", metrics.get(0).getYearMonthStr());
        assertEquals(2, metrics.get(0).getTotalVulnerabilities());
        assertEquals(1, metrics.get(0).getRansomwareAssociatedCount());
        assertEquals(1, metrics.get(0).getUniqueVendorsCount());

        assertEquals("2024-02", metrics.get(1).getYearMonthStr());
        assertEquals(1, metrics.get(1).getTotalVulnerabilities());
        assertEquals(1, metrics.get(1).getRansomwareAssociatedCount());

        Map<String, Double> mom = engine.computeMonthOverMonthGrowth(metrics);
        assertTrue(mom.containsKey("2024-02"));
        assertEquals(-50.0, mom.get("2024-02"), 0.001); // (1 - 2)/2 * 100 = -50%
    }

    @Test
    void testEarlyWarningEngine() {
        EarlyWarningEngine engine = new EarlyWarningEngine(3);

        List<TemporalMetric> metrics = new ArrayList<>();
        metrics.add(new TemporalMetric("2024-01", 10, 2, 5, 8));
        metrics.add(new TemporalMetric("2024-02", 10, 3, 5, 8));
        metrics.add(new TemporalMetric("2024-03", 10, 1, 5, 8));
        // Baseline for 4th month = (10+10+10)/3 = 10.0
        // If 4th month has 30 -> ratio = 3.0x -> CRITICAL (>= 2.5x)
        metrics.add(new TemporalMetric("2024-04", 30, 8, 12, 20));

        List<EarlyWarningIndicator> indicators = engine.evaluateSurges(metrics);
        assertEquals(4, indicators.size());

        EarlyWarningIndicator fourth = indicators.get(3);
        assertEquals("2024-04", fourth.getYearMonth());
        assertEquals(10.0, fourth.getBaselineMovingAverage(), 0.01);
        assertEquals(3.0, fourth.getSurgeRatio(), 0.01);
        assertEquals(EarlyWarningIndicator.AlertLevel.CRITICAL, fourth.getAlertLevel());
    }

    @Test
    void testForecastingEngine() {
        ForecastingEngine engine = new ForecastingEngine();

        List<TemporalMetric> metrics = new ArrayList<>();
        metrics.add(new TemporalMetric("2024-01", 10, 1, 2, 2));
        metrics.add(new TemporalMetric("2024-02", 12, 1, 2, 2));
        metrics.add(new TemporalMetric("2024-03", 14, 2, 3, 3));
        metrics.add(new TemporalMetric("2024-04", 16, 2, 3, 3));
        metrics.add(new TemporalMetric("2024-05", 18, 3, 4, 4));

        List<ForecastResult> results = engine.compareAllModels(metrics);
        assertEquals(4, results.size());

        for (ForecastResult res : results) {
            assertNotNull(res.getModelName());
            assertTrue(res.getMae() >= 0.0);
            assertTrue(res.getRmse() >= 0.0);
            assertTrue(res.getNextMonthForecast() > 0.0);
        }
    }

    @Test
    void testRansomwareRiskAnalyzer() {
        RansomwareRiskAnalyzer analyzer = new RansomwareRiskAnalyzer();

        Vulnerability v1 = new Vulnerability("CVE-2024-101", "Microsoft", "Windows", "Flaw 1", LocalDate.now(), null, null, null, "Known", null, null, "CWE-787");
        Vulnerability v2 = new Vulnerability("CVE-2024-102", "Microsoft", "Windows", "Flaw 2", LocalDate.now(), null, null, null, "Unknown", null, null, "CWE-787");
        Vulnerability v3 = new Vulnerability("CVE-2024-103", "Apple", "macOS", "Flaw 3", LocalDate.now(), null, null, null, "Known", null, null, "CWE-120");

        List<VendorRiskMetric> vendorMetrics = analyzer.computeTopVendorRisks(Arrays.asList(v1, v2, v3), 5);
        assertEquals(2, vendorMetrics.size());
        assertEquals("Microsoft", vendorMetrics.get(0).getVendorName());
        assertEquals(2, vendorMetrics.get(0).getTotalCves());
        assertEquals(1, vendorMetrics.get(0).getRansomwareCves());

        Map<String, Integer> topCwes = analyzer.computeTopCwes(Arrays.asList(v1, v2, v3), 5);
        assertEquals(2, topCwes.get("CWE-787"));
        assertEquals(1, topCwes.get("CWE-120"));
    }
}
