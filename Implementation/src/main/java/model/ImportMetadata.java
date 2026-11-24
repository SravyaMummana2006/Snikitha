package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Tracks historical data import audit metadata.
 */
public class ImportMetadata implements Serializable {

    private Integer importId;
    private LocalDateTime importTimestamp;
    private String sourceFormat; // "CSV" or "JSON"
    private String fileName;
    private int totalRecords;
    private int validRecords;
    private int duplicatesSkipped;
    private long executionTimeMs;

    public ImportMetadata() {
    }

    public ImportMetadata(Integer importId, LocalDateTime importTimestamp, String sourceFormat,
                          String fileName, int totalRecords, int validRecords,
                          int duplicatesSkipped, long executionTimeMs) {
        this.importId = importId;
        this.importTimestamp = importTimestamp;
        this.sourceFormat = sourceFormat;
        this.fileName = fileName;
        this.totalRecords = totalRecords;
        this.validRecords = validRecords;
        this.duplicatesSkipped = duplicatesSkipped;
        this.executionTimeMs = executionTimeMs;
    }

    public Integer getImportId() {
        return importId;
    }

    public void setImportId(Integer importId) {
        this.importId = importId;
    }

    public LocalDateTime getImportTimestamp() {
        return importTimestamp;
    }

    public void setImportTimestamp(LocalDateTime importTimestamp) {
        this.importTimestamp = importTimestamp;
    }

    public String getSourceFormat() {
        return sourceFormat;
    }

    public void setSourceFormat(String sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getValidRecords() {
        return validRecords;
    }

    public void setValidRecords(int validRecords) {
        this.validRecords = validRecords;
    }

    public int getDuplicatesSkipped() {
        return duplicatesSkipped;
    }

    public void setDuplicatesSkipped(int duplicatesSkipped) {
        this.duplicatesSkipped = duplicatesSkipped;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    @Override
    public String toString() {
        return String.format("Import #%d [%s]: %d valid, %d dups skipped (%d ms)",
                importId, sourceFormat, validRecords, duplicatesSkipped, executionTimeMs);
    }
}
