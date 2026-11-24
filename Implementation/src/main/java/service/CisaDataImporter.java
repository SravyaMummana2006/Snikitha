package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.ImportMetadataDao;
import dao.ImportMetadataDaoImpl;
import dao.VulnerabilityDao;
import dao.VulnerabilityDaoImpl;
import exception.ImportException;
import model.ImportMetadata;
import model.Vulnerability;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DateUtils;
import util.ValidationUtils;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * High-performance data ingestion service supporting both CSV and JSON feeds.
 * Demonstrates Apache Commons CSV, Google Gson, validation pipelines, and multithreaded callbacks.
 */
public class CisaDataImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CisaDataImporter.class);

    private final VulnerabilityDao vulnerabilityDao;
    private final ImportMetadataDao importMetadataDao;

    public interface ProgressListener {
        void onProgress(int processed, int total, String status);
    }

    public static class ImportResult {
        private final int totalRead;
        private final int insertedCount;
        private final int duplicateCount;
        private final int invalidCount;
        private final long durationMs;

        public ImportResult(int totalRead, int insertedCount, int duplicateCount, int invalidCount, long durationMs) {
            this.totalRead = totalRead;
            this.insertedCount = insertedCount;
            this.duplicateCount = duplicateCount;
            this.invalidCount = invalidCount;
            this.durationMs = durationMs;
        }

        public int getTotalRead() {
            return totalRead;
        }

        public int getInsertedCount() {
            return insertedCount;
        }

        public int getDuplicateCount() {
            return duplicateCount;
        }

        public int getInvalidCount() {
            return invalidCount;
        }

        public long getDurationMs() {
            return durationMs;
        }

        @Override
        public String toString() {
            return String.format("Import Complete: %d read, %d inserted, %d duplicates skipped, %d invalid (%d ms)",
                    totalRead, insertedCount, duplicateCount, invalidCount, durationMs);
        }
    }

    public CisaDataImporter() {
        this(new VulnerabilityDaoImpl(), new ImportMetadataDaoImpl());
    }

    public CisaDataImporter(VulnerabilityDao vulnerabilityDao, ImportMetadataDao importMetadataDao) {
        this.vulnerabilityDao = vulnerabilityDao;
        this.importMetadataDao = importMetadataDao;
    }

    /**
     * Imports vulnerabilities from a CSV file.
     */
    public ImportResult importCsv(File file, ProgressListener listener) {
        if (file == null || !file.exists()) {
            throw new ImportException("CSV file does not exist: " + (file != null ? file.getAbsolutePath() : "null"));
        }

        long startTime = System.currentTimeMillis();
        int totalRead = 0;
        int duplicateCount = 0;
        int invalidCount = 0;

        List<Vulnerability> validList = new ArrayList<>();
        Set<String> seenInBatch = new HashSet<>();

        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            List<CSVRecord> records = csvParser.getRecords();
            int total = records.size();

            for (int i = 0; i < total; i++) {
                CSVRecord record = records.get(i);
                totalRead++;

                String cveId = record.isMapped("cveID") ? record.get("cveID") : record.get(0);
                if (!ValidationUtils.isValidCveId(cveId)) {
                    invalidCount++;
                    continue;
                }

                cveId = cveId.trim().toUpperCase();

                if (seenInBatch.contains(cveId) || vulnerabilityDao.existsByCveId(cveId)) {
                    duplicateCount++;
                    continue;
                }

                String vendor = record.isMapped("vendorProject") ? record.get("vendorProject") : (record.size() > 1 ? record.get(1) : "");
                String product = record.isMapped("product") ? record.get("product") : (record.size() > 2 ? record.get(2) : "");
                String vulnName = record.isMapped("vulnerabilityName") ? record.get("vulnerabilityName") : (record.size() > 3 ? record.get(3) : "");
                String dateAddedStr = record.isMapped("dateAdded") ? record.get("dateAdded") : (record.size() > 4 ? record.get(4) : "");
                String shortDesc = record.isMapped("shortDescription") ? record.get("shortDescription") : (record.size() > 5 ? record.get(5) : "");
                String requiredAction = record.isMapped("requiredAction") ? record.get("requiredAction") : (record.size() > 6 ? record.get(6) : "");
                String dueDateStr = record.isMapped("dueDate") ? record.get("dueDate") : (record.size() > 7 ? record.get(7) : "");
                String ransomware = record.isMapped("knownRansomwareCampaignUse") ? record.get("knownRansomwareCampaignUse") : (record.size() > 8 ? record.get(8) : "Unknown");
                String notes = record.isMapped("notes") ? record.get("notes") : (record.size() > 10 ? record.get(10) : "");
                String cwes = record.isMapped("cwes") ? record.get("cwes") : (record.size() > 11 ? record.get(11) : "");

                if (!ValidationUtils.isValidIsoDate(dateAddedStr)) {
                    invalidCount++;
                    continue;
                }

                Vulnerability v = new Vulnerability();
                v.setCveId(cveId);
                v.setVendorProject(ValidationUtils.clean(vendor));
                v.setProduct(ValidationUtils.clean(product));
                v.setVulnerabilityName(ValidationUtils.clean(vulnName));
                v.setDateAdded(DateUtils.parseIsoDate(dateAddedStr));
                v.setShortDescription(ValidationUtils.clean(shortDesc));
                v.setRequiredAction(ValidationUtils.clean(requiredAction));
                v.setDueDate(DateUtils.parseIsoDate(dueDateStr));
                v.setKnownRansomwareCampaignUse(ValidationUtils.clean(ransomware));
                v.setNotes(ValidationUtils.clean(notes));
                v.setCwes(ValidationUtils.clean(cwes));

                validList.add(v);
                seenInBatch.add(cveId);

                if (listener != null && (i % 100 == 0 || i == total - 1)) {
                    listener.onProgress(i + 1, total, "Parsing CSV records...");
                }
            }

            if (listener != null) {
                listener.onProgress(total, total, "Saving to SQLite database...");
            }

            int inserted = vulnerabilityDao.insertBatch(validList);
            long duration = System.currentTimeMillis() - startTime;

            // Log import audit metadata
            ImportMetadata meta = new ImportMetadata(
                    null, LocalDateTime.now(), "CSV", file.getName(),
                    totalRead, inserted, duplicateCount, duration
            );
            importMetadataDao.logImport(meta);

            LOGGER.info("CSV Import finished: {}", meta);
            return new ImportResult(totalRead, inserted, duplicateCount, invalidCount, duration);

        } catch (Exception e) {
            LOGGER.error("Failed to parse CSV file: {}", e.getMessage(), e);
            throw new ImportException("CSV parsing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Imports vulnerabilities from a JSON file.
     */
    public ImportResult importJson(File file, ProgressListener listener) {
        if (file == null || !file.exists()) {
            throw new ImportException("JSON file does not exist: " + (file != null ? file.getAbsolutePath() : "null"));
        }

        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            return parseAndInsertJson(reader, file.getName(), listener);
        } catch (Exception e) {
            LOGGER.error("Failed to parse JSON file: {}", e.getMessage(), e);
            throw new ImportException("JSON parsing failed: " + e.getMessage(), e);
        }
    }

    private ImportResult parseAndInsertJson(Reader reader, String sourceLabel, ProgressListener listener) {
        long startTime = System.currentTimeMillis();
        int totalRead = 0;
        int duplicateCount = 0;
        int invalidCount = 0;

        List<Vulnerability> validList = new ArrayList<>();
        Set<String> seenInBatch = new HashSet<>();

        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonArray vulnArray = root.has("vulnerabilities") ? root.getAsJsonArray("vulnerabilities") : new JsonArray();
        int total = vulnArray.size();

        for (int i = 0; i < total; i++) {
            totalRead++;
            JsonElement el = vulnArray.get(i);
            if (!el.isJsonObject()) {
                invalidCount++;
                continue;
            }

            JsonObject obj = el.getAsJsonObject();
            String cveId = obj.has("cveID") ? obj.get("cveID").getAsString() : "";

            if (!ValidationUtils.isValidCveId(cveId)) {
                invalidCount++;
                continue;
            }

            cveId = cveId.trim().toUpperCase();
            if (seenInBatch.contains(cveId) || vulnerabilityDao.existsByCveId(cveId)) {
                duplicateCount++;
                continue;
            }

            String dateAdded = obj.has("dateAdded") ? obj.get("dateAdded").getAsString() : "";
            if (!ValidationUtils.isValidIsoDate(dateAdded)) {
                invalidCount++;
                continue;
            }

            Vulnerability v = new Vulnerability();
            v.setCveId(cveId);
            v.setVendorProject(obj.has("vendorProject") ? obj.get("vendorProject").getAsString() : "");
            v.setProduct(obj.has("product") ? obj.get("product").getAsString() : "");
            v.setVulnerabilityName(obj.has("vulnerabilityName") ? obj.get("vulnerabilityName").getAsString() : "");
            v.setDateAdded(DateUtils.parseIsoDate(dateAdded));
            v.setShortDescription(obj.has("shortDescription") ? obj.get("shortDescription").getAsString() : "");
            v.setRequiredAction(obj.has("requiredAction") ? obj.get("requiredAction").getAsString() : "");
            if (obj.has("dueDate")) {
                v.setDueDate(DateUtils.parseIsoDate(obj.get("dueDate").getAsString()));
            }
            v.setKnownRansomwareCampaignUse(obj.has("knownRansomwareCampaignUse") ? obj.get("knownRansomwareCampaignUse").getAsString() : "Unknown");
            v.setNotes(obj.has("notes") ? obj.get("notes").getAsString() : "");

            if (obj.has("cwes") && obj.get("cwes").isJsonArray()) {
                JsonArray cweArr = obj.getAsJsonArray("cwes");
                List<String> cweList = new ArrayList<>();
                for (JsonElement cweEl : cweArr) {
                    cweList.add(cweEl.getAsString());
                }
                v.setCwes(String.join(", ", cweList));
            }

            validList.add(v);
            seenInBatch.add(cveId);

            if (listener != null && (i % 100 == 0 || i == total - 1)) {
                listener.onProgress(i + 1, total, "Parsing JSON vulnerabilities...");
            }
        }

        if (listener != null) {
            listener.onProgress(total, total, "Inserting batch into SQLite...");
        }

        int inserted = vulnerabilityDao.insertBatch(validList);
        long duration = System.currentTimeMillis() - startTime;

        ImportMetadata meta = new ImportMetadata(
                null, LocalDateTime.now(), "JSON", sourceLabel,
                totalRead, inserted, duplicateCount, duration
        );
        importMetadataDao.logImport(meta);

        return new ImportResult(totalRead, inserted, duplicateCount, invalidCount, duration);
    }
}
