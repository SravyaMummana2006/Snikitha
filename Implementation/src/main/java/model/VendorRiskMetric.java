package model;

import java.io.Serializable;

/**
 * Encapsulates vendor risk profile and vulnerability concentration metrics.
 */
public class VendorRiskMetric implements Comparable<VendorRiskMetric>, Serializable {

    private String vendorName;
    private int totalCves;
    private int ransomwareCves;
    private String topProduct;
    private double riskScore; // Weighted score: totalCves + (ransomwareCves * 3.0)

    public VendorRiskMetric(String vendorName, int totalCves, int ransomwareCves, String topProduct) {
        this.vendorName = vendorName;
        this.totalCves = totalCves;
        this.ransomwareCves = ransomwareCves;
        this.topProduct = topProduct;
        this.riskScore = totalCves + (ransomwareCves * 3.0);
    }

    public String getVendorName() {
        return vendorName;
    }

    public int getTotalCves() {
        return totalCves;
    }

    public int getRansomwareCves() {
        return ransomwareCves;
    }

    public String getTopProduct() {
        return topProduct;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public double getRansomwarePercentage() {
        if (totalCves == 0) return 0.0;
        return (double) ransomwareCves * 100.0 / totalCves;
    }

    @Override
    public int compareTo(VendorRiskMetric o) {
        if (o == null) return -1;
        // Rank by risk score descending
        return Double.compare(o.riskScore, this.riskScore);
    }

    @Override
    public String toString() {
        return String.format("%s: %d CVEs (%d Ransomware), Score=%.1f",
                vendorName, totalCves, ransomwareCves, riskScore);
    }
}
