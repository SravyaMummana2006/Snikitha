package dao;

import model.ImportMetadata;

import java.util.List;

public interface ImportMetadataDao {

    boolean logImport(ImportMetadata metadata);

    List<ImportMetadata> getImportHistory();

    int getTotalImportsCount();
}
