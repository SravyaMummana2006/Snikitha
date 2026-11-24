package model;

import java.io.Serializable;

/**
 * Encapsulates an early warning surge detection signal for a specific month.
 */
public class EarlyWarningIndicator implements Serializable {

    public enum AlertLevel {
        NORMAL,     // < 1.5x baseline
        ELEVATED,   // >= 1.5x baseline
        HIGH,       // >= 2.0x baseline
        CRITICAL    // >= 2.5x baseline
    }

    private String yearMonth;
    private int currentMonthCount;
    private double baselineMovingAverage;
    private double surgeRatio;
    private AlertLevel alertLevel;
    private String rationale;

    public EarlyWarningIndicator(String yearMonth, int currentMonthCount, double baselineMovingAverage,
                                 double surgeRatio, AlertLevel alertLevel, String rationale) {
        this.yearMonth = yearMonth;
        this.currentMonthCount = currentMonthCount;
        this.baselineMovingAverage = baselineMovingAverage;
        this.surgeRatio = surgeRatio;
        this.alertLevel = alertLevel;
        this.rationale = rationale;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public int getCurrentMonthCount() {
        return currentMonthCount;
    }

    public double getBaselineMovingAverage() {
        return baselineMovingAverage;
    }

    public double getSurgeRatio() {
        return surgeRatio;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public String getRationale() {
        return rationale;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: Count=%d, Baseline=%.1f, Ratio=%.2fx",
                alertLevel, yearMonth, currentMonthCount, baselineMovingAverage, surgeRatio);
    }
}
