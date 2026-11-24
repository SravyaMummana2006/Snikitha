package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain entity representing an analyst-logged incident or threat record.
 * Supports manual threat ingestion alongside automated CISA feeds.
 */
public class ManualThreatRecord implements Serializable {

    private Integer id;
    private String threatName;
    private String threatType; // e.g. "Zero-Day", "Phishing", "Ransomware", "DDoS", "Supply-Chain"
    private String severity;   // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    private LocalDate dateObserved;
    private String affectedSystem;
    private String status;     // "INVESTIGATING", "MITIGATED", "ACTIVE", "CLOSED"
    private String notes;

    public ManualThreatRecord() {
    }

    public ManualThreatRecord(Integer id, String threatName, String threatType, String severity,
                              LocalDate dateObserved, String affectedSystem, String status, String notes) {
        this.id = id;
        this.threatName = threatName;
        this.threatType = threatType;
        this.severity = severity;
        this.dateObserved = dateObserved;
        this.affectedSystem = affectedSystem;
        this.status = status;
        this.notes = notes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getThreatName() {
        return threatName;
    }

    public void setThreatName(String threatName) {
        this.threatName = threatName;
    }

    public String getThreatType() {
        return threatType;
    }

    public void setThreatType(String threatType) {
        this.threatType = threatType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public LocalDate getDateObserved() {
        return dateObserved;
    }

    public void setDateObserved(LocalDate dateObserved) {
        this.dateObserved = dateObserved;
    }

    public String getAffectedSystem() {
        return affectedSystem;
    }

    public void setAffectedSystem(String affectedSystem) {
        this.affectedSystem = affectedSystem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManualThreatRecord that = (ManualThreatRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ManualThreatRecord{" +
                "id=" + id +
                ", threatName='" + threatName + '\'' +
                ", threatType='" + threatType + '\'' +
                ", severity='" + severity + '\'' +
                ", dateObserved=" + dateObserved +
                ", status='" + status + '\'' +
                '}';
    }
}
