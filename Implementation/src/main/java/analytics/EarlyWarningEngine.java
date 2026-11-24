package analytics;

import model.EarlyWarningIndicator;
import model.EarlyWarningIndicator.AlertLevel;
import model.TemporalMetric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Early Warning surge detection module.
 * Identifies sudden spikes in threat cataloguing relative to a trailing moving baseline.
 */
public class EarlyWarningEngine {

    private final int baselineWindowSize;

    public EarlyWarningEngine() {
        this(3); // Default 3-month trailing baseline window
    }

    public EarlyWarningEngine(int baselineWindowSize) {
        this.baselineWindowSize = Math.max(1, baselineWindowSize);
    }

    /**
     * Evaluates early warning indicators for all available historical months.
     */
    public List<EarlyWarningIndicator> evaluateSurges(List<TemporalMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return Collections.emptyList();
        }

        List<EarlyWarningIndicator> indicators = new ArrayList<>();

        for (int i = 0; i < metrics.size(); i++) {
            TemporalMetric current = metrics.get(i);
            int currentCount = current.getTotalVulnerabilities();

            if (i < baselineWindowSize) {
                // Not enough prior history for a full moving baseline; compute expanding average
                double expandingSum = 0;
                for (int j = 0; j < i; j++) {
                    expandingSum += metrics.get(j).getTotalVulnerabilities();
                }
                double baseline = i == 0 ? currentCount : (expandingSum / i);
                double ratio = baseline > 0 ? (currentCount / baseline) : 1.0;
                AlertLevel level = determineLevel(ratio);
                String rationale = String.format("Baseline expanding average (first %d months): %.1f", i, baseline);
                indicators.add(new EarlyWarningIndicator(current.getYearMonthStr(), currentCount, baseline, ratio, level, rationale));
            } else {
                // Trailing moving window average
                double windowSum = 0;
                for (int j = i - baselineWindowSize; j < i; j++) {
                    windowSum += metrics.get(j).getTotalVulnerabilities();
                }
                double baseline = windowSum / baselineWindowSize;
                double ratio = baseline > 0 ? (currentCount / baseline) : 1.0;
                AlertLevel level = determineLevel(ratio);

                String rationale;
                if (level == AlertLevel.CRITICAL) {
                    rationale = String.format("CRITICAL SURGE: %.1fx above %d-month baseline (%.1f)", ratio, baselineWindowSize, baseline);
                } else if (level == AlertLevel.HIGH) {
                    rationale = String.format("HIGH SURGE: %.1fx above %d-month baseline (%.1f)", ratio, baselineWindowSize, baseline);
                } else if (level == AlertLevel.ELEVATED) {
                    rationale = String.format("ELEVATED ACTIVITY: %.1fx above %d-month baseline (%.1f)", ratio, baselineWindowSize, baseline);
                } else {
                    rationale = String.format("Normal activity within baseline bounds (%.1fx of %.1f)", ratio, baseline);
                }

                indicators.add(new EarlyWarningIndicator(current.getYearMonthStr(), currentCount, baseline, ratio, level, rationale));
            }
        }

        return indicators;
    }

    private AlertLevel determineLevel(double ratio) {
        if (ratio >= 2.5) {
            return AlertLevel.CRITICAL;
        } else if (ratio >= 2.0) {
            return AlertLevel.HIGH;
        } else if (ratio >= 1.5) {
            return AlertLevel.ELEVATED;
        } else {
            return AlertLevel.NORMAL;
        }
    }
}
