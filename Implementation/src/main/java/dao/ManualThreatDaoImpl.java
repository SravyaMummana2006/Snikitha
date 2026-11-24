package dao;

import exception.DatabaseException;
import model.ManualThreatRecord;
import util.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManualThreatDaoImpl implements ManualThreatDao {

    private final DatabaseManager databaseManager;

    public ManualThreatDaoImpl() {
        this(DatabaseManager.getInstance());
    }

    public ManualThreatDaoImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean insert(ManualThreatRecord record) {
        String sql = """
            INSERT INTO manual_threats (threat_name, threat_type, severity, date_observed, affected_system, status, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?);
            """;
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, record.getThreatName());
            ps.setString(2, record.getThreatType());
            ps.setString(3, record.getSeverity());
            ps.setString(4, record.getDateObserved() != null ? record.getDateObserved().toString() : null);
            ps.setString(5, record.getAffectedSystem());
            ps.setString(6, record.getStatus());
            ps.setString(7, record.getNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        record.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating manual threat record: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean update(ManualThreatRecord record) {
        String sql = """
            UPDATE manual_threats SET threat_name = ?, threat_type = ?, severity = ?,
                                      date_observed = ?, affected_system = ?, status = ?, notes = ?
            WHERE id = ?;
            """;
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getThreatName());
            ps.setString(2, record.getThreatType());
            ps.setString(3, record.getSeverity());
            ps.setString(4, record.getDateObserved() != null ? record.getDateObserved().toString() : null);
            ps.setString(5, record.getAffectedSystem());
            ps.setString(6, record.getStatus());
            ps.setString(7, record.getNotes());
            ps.setInt(8, record.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating manual threat record: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM manual_threats WHERE id = ?;";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting manual threat record: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ManualThreatRecord> findById(int id) {
        String sql = "SELECT * FROM manual_threats WHERE id = ?;";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding manual threat: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<ManualThreatRecord> findAll() {
        String sql = "SELECT * FROM manual_threats ORDER BY date_observed DESC, id DESC;";
        List<ManualThreatRecord> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error listing manual threats: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM manual_threats;";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting manual threats: " + e.getMessage(), e);
        }
        return 0;
    }

    private ManualThreatRecord mapRow(ResultSet rs) throws SQLException {
        ManualThreatRecord r = new ManualThreatRecord();
        r.setId(rs.getInt("id"));
        r.setThreatName(rs.getString("threat_name"));
        r.setThreatType(rs.getString("threat_type"));
        r.setSeverity(rs.getString("severity"));
        r.setDateObserved(DateUtils.parseIsoDate(rs.getString("date_observed")));
        r.setAffectedSystem(rs.getString("affected_system"));
        r.setStatus(rs.getString("status"));
        r.setNotes(rs.getString("notes"));
        return r;
    }
}
