package config;

import java.io.File;
import java.nio.file.Paths;

/**
 * Global configuration constants for the CyberPulse application.
 */
public final class AppConfig {

    public static final String APP_NAME = "CyberPulse";
    public static final String APP_SUBTITLE = "Early Threat Detection & Temporal Vulnerability Intelligence";
    public static final String APP_VERSION = "v1.0.0";
    public static final String COURSE_CODE = "CSE 309 - Advanced Java Programming";
    public static final String INSTITUTION = "SRM University - AP";

    // Dynamic, platform-independent SQLite database path in user home or current project folder
    public static final String DB_NAME = "cyberpulse.db";
    public static final String DB_URL = "jdbc:sqlite:" + getDatabasePath();

    // Default CISA KEV JSON endpoint
    public static final String CISA_KEV_FEED_URL = "https://www.cisa.gov/sites/default/files/feeds/known_exploited_vulnerabilities.json";

    private AppConfig() {
        // Prevent instantiation
    }

    private static String getDatabasePath() {
        // Store in current working directory / project directory
        return Paths.get(System.getProperty("user.dir"), DB_NAME).toAbsolutePath().toString();
    }
}
