package analytics;

import model.VendorRiskMetric;
import model.Vulnerability;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytical service for evaluating vendor threat concentration,
 * ransomware exploit campaigns, and Common Weakness Enumeration (CWE) prevalence.
 */
public class RansomwareRiskAnalyzer {

    /**
     * Computes top vendors ranked by calculated composite risk score.
     */
    public List<VendorRiskMetric> computeTopVendorRisks(List<Vulnerability> vulnerabilities, int limit) {
        if (vulnerabilities == null || vulnerabilities.isEmpty()) {
            return Collections.emptyList();
        }

        // Group by vendor
        Map<String, List<Vulnerability>> vendorGroups = vulnerabilities.stream()
                .filter(v -> v.getVendorProject() != null && !v.getVendorProject().trim().isEmpty())
                .collect(Collectors.groupingBy(v -> v.getVendorProject().trim()));

        List<VendorRiskMetric> metrics = new ArrayList<>();

        for (Map.Entry<String, List<Vulnerability>> entry : vendorGroups.entrySet()) {
            String vendor = entry.getKey();
            List<Vulnerability> vulns = entry.getValue();

            int total = vulns.size();
            int ransomware = (int) vulns.stream().filter(Vulnerability::isRansomwareAssociated).count();

            // Find top vulnerable product for this vendor
            String topProduct = vulns.stream()
                    .map(Vulnerability::getProduct)
                    .filter(p -> p != null && !p.trim().isEmpty())
                    .collect(Collectors.groupingBy(p -> p, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

            metrics.add(new VendorRiskMetric(vendor, total, ransomware, topProduct));
        }

        // Sort descending by risk score
        Collections.sort(metrics);

        return metrics.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Aggregates frequency distribution of Common Weakness Enumerations (CWEs).
     */
    public Map<String, Integer> computeTopCwes(List<Vulnerability> vulnerabilities, int limit) {
        if (vulnerabilities == null || vulnerabilities.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Integer> cweCounts = new HashMap<>();

        for (Vulnerability v : vulnerabilities) {
            String cwes = v.getCwes();
            if (cwes != null && !cwes.trim().isEmpty()) {
                String[] parts = cwes.split("[,;\\s]+");
                for (String part : parts) {
                    String clean = part.trim().toUpperCase();
                    if (clean.startsWith("CWE-")) {
                        cweCounts.put(clean, cweCounts.getOrDefault(clean, 0) + 1);
                    }
                }
            }
        }

        return cweCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
