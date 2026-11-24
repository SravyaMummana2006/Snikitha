package dao;

import config.AppConfig;
import exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Thread-safe SQLite Connection Manager and Database Lifecycle Controller.
 * Implements schema generation, indexes, and transactional connection handling.
 */
public class DatabaseManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
    private static volatile DatabaseManager instance;
    private final String connectionUrl;

    public DatabaseManager() {
        this(AppConfig.DB_URL);
    }

    public DatabaseManager(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        initializeSchema();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Obtains a fresh connection to the SQLite database.
     */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(connectionUrl);
        // Enable foreign keys and WAL mode for high read/write concurrency
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA journal_mode = WAL;");
            stmt.execute("PRAGMA synchronous = NORMAL;");
        }
        return conn;
    }

    /**
     * Initializes all relational tables and optimized query indexes.
     */
    public void initializeSchema() {
        String createCisaTableSql = """
            CREATE TABLE IF NOT EXISTS cisa_kev (
                cve_id TEXT PRIMARY KEY,
                vendor_project TEXT NOT NULL,
                product TEXT NOT NULL,
                vulnerability_name TEXT,
                date_added TEXT NOT NULL,
                short_description TEXT,
                required_action TEXT,
                due_date TEXT,
                known_ransomware_campaign_use TEXT,
                forensic_triage TEXT,
                notes TEXT,
                cwes TEXT
            );
            """;

        String createCisaIndexes = """
            CREATE INDEX IF NOT EXISTS idx_cisa_date_added ON cisa_kev (date_added);
            CREATE INDEX IF NOT EXISTS idx_cisa_vendor ON cisa_kev (vendor_project);
            CREATE INDEX IF NOT EXISTS idx_cisa_ransomware ON cisa_kev (known_ransomware_campaign_use);
            """;

        String createManualThreatsTableSql = """
            CREATE TABLE IF NOT EXISTS manual_threats (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                threat_name TEXT NOT NULL,
                threat_type TEXT NOT NULL,
                severity TEXT NOT NULL,
                date_observed TEXT NOT NULL,
                affected_system TEXT NOT NULL,
                status TEXT NOT NULL,
                notes TEXT
            );
            """;

        String createImportMetadataTableSql = """
            CREATE TABLE IF NOT EXISTS import_metadata (
                import_id INTEGER PRIMARY KEY AUTOINCREMENT,
                import_timestamp TEXT NOT NULL,
                source_format TEXT NOT NULL,
                file_name TEXT NOT NULL,
                total_records INTEGER NOT NULL,
                valid_records INTEGER NOT NULL,
                duplicates_skipped INTEGER NOT NULL,
                execution_time_ms INTEGER NOT NULL
            );
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createCisaTableSql);
            stmt.execute(createCisaIndexes);
            stmt.execute(createManualThreatsTableSql);
            stmt.execute(createImportMetadataTableSql);
            LOGGER.info("Database schema initialized successfully at {}", connectionUrl);
        } catch (SQLException e) {
            LOGGER.error("Fatal error creating database tables", e);
            throw new DatabaseException("Failed to initialize database schema: " + e.getMessage(), e);
        }
    }

    /**
     * Resets all tables in the database (used in testing or explicit user reset).
     */
    public void resetDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM cisa_kev;");
            stmt.execute("DELETE FROM manual_threats;");
            stmt.execute("DELETE FROM import_metadata;");
            stmt.execute("VACUUM;");
            LOGGER.info("Database wiped and vacuumed successfully.");
        } catch (SQLException e) {
            throw new DatabaseException("Failed to reset database: " + e.getMessage(), e);
        }
    }
}
