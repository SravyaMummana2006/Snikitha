package dao;

import model.ManualThreatRecord;

import java.util.List;
import java.util.Optional;

/**
 * Interface for managing manually submitted threat records.
 */
public interface ManualThreatDao {

    boolean insert(ManualThreatRecord record);

    boolean update(ManualThreatRecord record);

    boolean delete(int id);

    Optional<ManualThreatRecord> findById(int id);

    List<ManualThreatRecord> findAll();

    int getTotalCount();
}
