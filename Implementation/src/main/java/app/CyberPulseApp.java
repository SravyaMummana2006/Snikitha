package app;

import com.formdev.flatlaf.FlatLightLaf;
import config.AppConfig;
import dao.DatabaseManager;
import dao.ImportMetadataDaoImpl;
import dao.VulnerabilityDao;
import dao.VulnerabilityDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.CisaDataImporter;
import ui.MainWindow;

import javax.swing.*;
import java.io.File;
import java.nio.file.Paths;

/**
 * Application Entry Point for CyberPulse.
 * Configures modern FlatLaf UI, initializes SQLite database schema,
 * and performs initial dataset auto-seeding if the catalog is empty.
 */
public class CyberPulseApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(CyberPulseApp.class);

    public static void main(String[] args) {
        // 1. Initialize FlatLaf Look and Feel
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
        } catch (Exception e) {
            LOGGER.warn("FlatLaf could not be initialized, falling back to system L&F: {}", e.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        // 2. Initialize Database & Schema
        LOGGER.info("Starting {} ({})", AppConfig.APP_NAME, AppConfig.COURSE_CODE);
        DatabaseManager dbManager = DatabaseManager.getInstance();

        // 3. Auto-seed initial dataset if database is empty
        VulnerabilityDao vulnDao = new VulnerabilityDaoImpl(dbManager);
        if (vulnDao.getTotalCount() == 0) {
            autoSeedBundledDataset(vulnDao);
        }

        // 4. Launch Main Window on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                MainWindow window = new MainWindow();
                window.setVisible(true);
                LOGGER.info("CyberPulse GUI launched successfully.");
            } catch (Exception ex) {
                LOGGER.error("Fatal error starting GUI: {}", ex.getMessage(), ex);
                JOptionPane.showMessageDialog(null, "Fatal GUI Initialization Error: " + ex.getMessage(),
                        "Launch Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void autoSeedBundledDataset(VulnerabilityDao vulnDao) {
        File csvFile = Paths.get("Dataset", "known_exploited_vulnerabilities.csv").toFile();
        if (!csvFile.exists()) {
            csvFile = Paths.get("..", "Dataset", "known_exploited_vulnerabilities.csv").toFile();
        }

        if (csvFile.exists()) {
            LOGGER.info("Auto-seeding empty database with bundled dataset: {}", csvFile.getAbsolutePath());
            try {
                CisaDataImporter importer = new CisaDataImporter(vulnDao, new ImportMetadataDaoImpl());
                CisaDataImporter.ImportResult result = importer.importCsv(csvFile, null);
                LOGGER.info("Initial seeding complete: {}", result);
            } catch (Exception e) {
                LOGGER.warn("Auto-seeding encountered non-fatal error: {}", e.getMessage());
            }
        } else {
            LOGGER.info("No bundled dataset found at Dataset/known_exploited_vulnerabilities.csv. Database initialized empty.");
        }
    }
}
