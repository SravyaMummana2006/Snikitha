package analytics;

import model.TemporalMetric;
import model.Vulnerability;
import util.DateUtils;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core analytical engine demonstrating advanced Java Streams, Lambdas, and Collectors.
 * Aggregates multidimensional vulnerability records into chronological temporal series.
 */
public class TemporalAnalysisEngine {

    /**
     * Aggregates a list of vulnerabilities into sorted monthly temporal metrics.
     */
    public List<TemporalMetric> computeMonthlyMetrics(List<Vulnerability> vulnerabilities) {
        if (vulnerabilities == null || vulnerabilities.isEmpty()) {
            return Collections.emptyList();
        }

        // Group vulnerabilities by YearMonth using Java 8+ Streams
        Map<String, List<Vulnerability>> groupedByMonth = vulnerabilities.stream()
                .filter(v -> v.getDateAdded() != null)
                .collect(Collectors.groupingBy(v -> DateUtils.toYearMonthString(v.getDateAdded())));

        List<TemporalMetric> result = new ArrayList<>();

        for (Map.Entry<String, List<Vulnerability>> entry : groupedByMonth.entrySet()) {
            String ymStr = entry.getKey();
            List<Vulnerability> list = entry.getValue();

            int total = list.size();
            int ransomwareCount = (int) list.stream()
                    .filter(Vulnerability::isRansomwareAssociated)
                    .count();

            int uniqueVendors = (int) list.stream()
                    .map(Vulnerability::getVendorProject)
                    .filter(v -> v != null && !v.trim().isEmpty())
                    .distinct()
                    .count();

            int uniqueProducts = (int) list.stream()
                    .map(Vulnerability::getProduct)
                    .filter(p -> p != null && !p.trim().isEmpty())
                    .distinct()
                    .count();

            result.add(new TemporalMetric(ymStr, total, ransomwareCount, uniqueVendors, uniqueProducts));
        }

        // Sort chronologically
        Collections.sort(result);
        return result;
    }

    /**
     * Computes Month-over-Month (MoM) percentage change between consecutive months.
     */
    public Map<String, Double> computeMonthOverMonthGrowth(List<TemporalMetric> metrics) {
        Map<String, Double> growthMap = new LinkedHashMap<>();
        if (metrics == null || metrics.size() < 2) {
            return growthMap;
        }

        for (int i = 1; i < metrics.size(); i++) {
            TemporalMetric prev = metrics.get(i - 1);
            TemporalMetric curr = metrics.get(i);

            double prevCount = prev.getTotalVulnerabilities();
            double currCount = curr.getTotalVulnerabilities();

            double rate = prevCount == 0 ? 0.0 : ((currCount - prevCount) / prevCount) * 100.0;
            growthMap.put(curr.getYearMonthStr(), rate);
        }

        return growthMap;
    }

    /**
     * Identifies the historical peak month with maximum additions.
     */
    public Optional<TemporalMetric> findPeakMonth(List<TemporalMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return Optional.empty();
        }
        return metrics.stream()
                .max(Comparator.comparingInt(TemporalMetric::getTotalVulnerabilities));
    }

    /**
     * Calculates the overall monthly mean of additions.
     */
    public double computeMeanMonthlyAdditions(List<TemporalMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return 0.0;
        }
        return metrics.stream()
                .mapToInt(TemporalMetric::getTotalVulnerabilities)
                .average()
                .orElse(0.0);
    }
}
