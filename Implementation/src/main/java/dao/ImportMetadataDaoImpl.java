package dao;

import exception.DatabaseException;
import model.ImportMetadata;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ImportMetadataDaoImpl implements ImportMetadataDao {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DatabaseManager databaseManager;

    public ImportMetadataDaoImpl() {
        this(DatabaseManager.getInstance());
    }

    public ImportMetadataDaoImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean logImport(ImportMetadata metadata) {
        String sql = """
            INSERT INTO import_metadata (import_timestamp, source_format, file_name, total_records, valid_records, duplicates_skipped, execution_time_ms)
            VALUES (?, ?, ?, ?, ?, ?, ?);
            """;
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, metadata.getImportTimestamp().format(FORMATTER));
            ps.setString(2, metadata.getSourceFormat());
            ps.setString(3, metadata.getFileName());
            ps.setInt(4, metadata.getTotalRecords());
            ps.setInt(5, metadata.getValidRecords());
            ps.setInt(6, metadata.getDuplicatesSkipped());
            ps.setLong(7, metadata.getExecutionTimeMs());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        metadata.setImportId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error logging import metadata: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public List<ImportMetadata> getImportHistory() {
        String sql = "SELECT * FROM import_metadata ORDER BY import_timestamp DESC, import_id DESC;";
        List<ImportMetadata> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ImportMetadata m = new ImportMetadata();
                m.setImportId(rs.getInt("import_id"));
                m.setImportTimestamp(LocalDateTime.parse(rs.getString("import_timestamp"), FORMATTER));
                m.setSourceFormat(rs.getString("source_format"));
                m.setFileName(rs.getString("file_name"));
                m.setTotalRecords(rs.getInt("total_records"));
                m.setValidRecords(rs.getInt("valid_records"));
                m.setDuplicatesSkipped(rs.getInt("duplicates_skipped"));
                m.setExecutionTimeMs(rs.getLong("execution_time_ms"));
                list.add(m);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving import history: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int getTotalImportsCount() {
        String sql = "SELECT COUNT(*) FROM import_metadata;";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting imports: " + e.getMessage(), e);
        }
        return 0;
    }
}
