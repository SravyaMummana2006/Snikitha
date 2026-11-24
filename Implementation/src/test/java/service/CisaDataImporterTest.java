package service;

import dao.DatabaseManager;
import dao.ImportMetadataDaoImpl;
import dao.VulnerabilityDao;
import dao.VulnerabilityDaoImpl;
import exception.ImportException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class CisaDataImporterTest {

    private File tempDbFile;
    private File tempCsvFile;
    private File tempJsonFile;
    private VulnerabilityDao vulnDao;
    private CisaDataImporter importer;

    @BeforeEach
    void setUp() throws IOException {
        tempDbFile = File.createTempFile("importer_test_", ".db");
        DatabaseManager db = new DatabaseManager("jdbc:sqlite:" + tempDbFile.getAbsolutePath());
        vulnDao = new VulnerabilityDaoImpl(db);
        importer = new CisaDataImporter(vulnDao, new ImportMetadataDaoImpl(db));
    }

    @AfterEach
    void tearDown() {
        if (tempDbFile != null && tempDbFile.exists()) tempDbFile.delete();
        if (tempCsvFile != null && tempCsvFile.exists()) tempCsvFile.delete();
        if (tempJsonFile != null && tempJsonFile.exists()) tempJsonFile.delete();
    }

    @Test
    void testImportValidCsv() throws IOException {
        tempCsvFile = File.createTempFile("test_kev_", ".csv");
        try (FileWriter writer = new FileWriter(tempCsvFile)) {
            writer.write("cveID,vendorProject,product,vulnerabilityName,dateAdded,shortDescription,requiredAction,dueDate,knownRansomwareCampaignUse,notes,cwes\n");
            writer.write("CVE-2024-55555,TestVendor,TestApp,SQL Injection,2024-06-01,Test desc,Apply patch,2024-06-15,Known,Note text,CWE-89\n");
            writer.write("CVE-2024-55556,TestVendor,TestService,Buffer Overflow,2024-06-02,Test desc 2,Apply update,2024-06-16,Unknown,Note text 2,CWE-120\n");
            // Invalid CVE ID row to test validation filtering
            writer.write("INVALID-ID,BadVendor,BadApp,Bad Flaw,2024-06-03,Desc,Action,Date,Unknown,Note,CWE-00\n");
        }

        CisaDataImporter.ImportResult result = importer.importCsv(tempCsvFile, null);
        assertEquals(3, result.getTotalRead());
        assertEquals(2, result.getInsertedCount());
        assertEquals(1, result.getInvalidCount());
        assertEquals(2, vulnDao.getTotalCount());
    }

    @Test
    void testImportValidJson() throws IOException {
        tempJsonFile = File.createTempFile("test_kev_", ".json");
        try (FileWriter writer = new FileWriter(tempJsonFile)) {
            writer.write("""
                {
                  "title": "CISA KEV Feed",
                  "catalogVersion": "2024.1",
                  "count": 2,
                  "vulnerabilities": [
                    {
                      "cveID": "CVE-2024-77771",
                      "vendorProject": "Acme",
                      "product": "Widget",
                      "vulnerabilityName": "Remote Code Execution",
                      "dateAdded": "2024-08-10",
                      "shortDescription": "Widget RCE",
                      "requiredAction": "Patch",
                      "knownRansomwareCampaignUse": "Known",
                      "cwes": ["CWE-787", "CWE-122"]
                    },
                    {
                      "cveID": "CVE-2024-77772",
                      "vendorProject": "BetaCorp",
                      "product": "Portal",
                      "vulnerabilityName": "Authentication Bypass",
                      "dateAdded": "2024-08-12",
                      "shortDescription": "Auth Bypass",
                      "requiredAction": "Upgrade",
                      "knownRansomwareCampaignUse": "Unknown",
                      "cwes": ["CWE-287"]
                    }
                  ]
                }
                """);
        }

        CisaDataImporter.ImportResult result = importer.importJson(tempJsonFile, null);
        assertEquals(2, result.getTotalRead());
        assertEquals(2, result.getInsertedCount());
        assertEquals(0, result.getDuplicateCount());
        assertEquals(2, vulnDao.getTotalCount());
        assertTrue(vulnDao.existsByCveId("CVE-2024-77771"));
    }

    @Test
    void testImportNonExistentFileThrowsException() {
        File nonExistent = new File("non_existent_file_xyz_123.csv");
        assertThrows(ImportException.class, () -> importer.importCsv(nonExistent, null));
    }
}
