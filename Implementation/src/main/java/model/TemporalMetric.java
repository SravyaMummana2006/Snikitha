package model;

import java.io.Serializable;
import java.time.YearMonth;

/**
 * Monthly aggregate temporal metrics derived from CISA KEV data.
 */
public class TemporalMetric implements Comparable<TemporalMetric>, Serializable {

    private String yearMonthStr; // e.g., "2024-03"
    private YearMonth yearMonth;
    private int totalVulnerabilities;
    private int ransomwareAssociatedCount;
    private int uniqueVendorsCount;
    private int uniqueProductsCount;

    public TemporalMetric(String yearMonthStr, int totalVulnerabilities, int ransomwareAssociatedCount,
                          int uniqueVendorsCount, int uniqueProductsCount) {
        this.yearMonthStr = yearMonthStr;
        this.yearMonth = YearMonth.parse(yearMonthStr);
        this.totalVulnerabilities = totalVulnerabilities;
        this.ransomwareAssociatedCount = ransomwareAssociatedCount;
        this.uniqueVendorsCount = uniqueVendorsCount;
        this.uniqueProductsCount = uniqueProductsCount;
    }

    public String getYearMonthStr() {
        return yearMonthStr;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public int getTotalVulnerabilities() {
        return totalVulnerabilities;
    }

    public int getRansomwareAssociatedCount() {
        return ransomwareAssociatedCount;
    }

    public int getUniqueVendorsCount() {
        return uniqueVendorsCount;
    }

    public int getUniqueProductsCount() {
        return uniqueProductsCount;
    }

    public double getRansomwareRatio() {
        if (totalVulnerabilities == 0) return 0.0;
        return (double) ransomwareAssociatedCount / totalVulnerabilities;
    }

    @Override
    public int compareTo(TemporalMetric other) {
        if (other == null) return 1;
        return this.yearMonth.compareTo(other.yearMonth);
    }

    @Override
    public String toString() {
        return "TemporalMetric{" +
                "yearMonth='" + yearMonthStr + '\'' +
                ", total=" + totalVulnerabilities +
                ", ransomware=" + ransomwareAssociatedCount +
                ", vendors=" + uniqueVendorsCount +
                '}';
    }
}
