# CyberPulse System Architecture & Technical Specifications

## 1. Architectural Overview
**CyberPulse** is engineered as a modular, three-tier desktop analytical system following the Model-View-Controller (MVC) and Data Access Object (DAO) architectural patterns. The system decouples raw data ingestion, relational persistence, numerical algorithms, and modern graphical presentation.

```
+-------------------------------------------------------------------------------+
|                             PRESENTATION TIER (UI)                            |
|  Java Swing + FlatLaf Modern Look & Feel (Light / Dark Arc Design)            |
|  MainWindow (CardLayout) | DashboardView | VulnerabilityTableView             |
|  TemporalAnalyticsView   | EarlyWarningView | ForecastingView                 |
|  ThreatManagementView    | DataImportView    | Interactive JFreeCharts        |
+-------------------------------------------------------------------------------+
                                      |
                                      v
+-------------------------------------------------------------------------------+
|                            SERVICE & ANALYTICS TIER                           |
|  CisaDataImporter (Multithreaded SwingWorker / ProgressListener)             |
|  TemporalAnalysisEngine (Streams, Lambdas, GroupingBy, MoM Growth)            |
|  EarlyWarningEngine (Surge Ratios, 3-Month Moving Baselines, Alert Categorization)|
|  ForecastingEngine (Moving Avg, Hist Avg, SES, ARIMA(1,1,0), MAE/RMSE Metrics)|
|  RansomwareRiskAnalyzer (Vendor Concentration, CWE Weakness Distributions)    |
|  DataExportService (Structured CSV & Formatted Executive Audit Reports)       |
+-------------------------------------------------------------------------------+
                                      |
                                      v
+-------------------------------------------------------------------------------+
|                           DATA ACCESS LAYER (DAO)                             |
|  DatabaseManager (Thread-Safe Connection Management, PRAGMA WAL, Vacuum)     |
|  VulnerabilityDao & VulnerabilityDaoImpl (JDBC PreparedStatements, Batches)  |
|  ManualThreatDao & ManualThreatDaoImpl (Full Incident Logging CRUD)           |
|  ImportMetadataDao & ImportMetadataDaoImpl (Data Provenance & Audit Logging)  |
+-------------------------------------------------------------------------------+
                                      |
                                      v
+-------------------------------------------------------------------------------+
|                               PERSISTENCE TIER                                |
|  Embedded SQLite Relational Database Engine (`cyberpulse.db`)                 |
|  Tables: `cisa_kev`, `manual_threats`, `import_metadata`                      |
|  Indexes: `idx_cisa_date_added`, `idx_cisa_vendor`, `idx_cisa_ransomware`     |
+-------------------------------------------------------------------------------+
```

---

## 2. Package Organization & Separation of Concerns

- `app`: Application initialization, FlatLaf styling setup, and bootstrapper (`CyberPulseApp.java`).
- `config`: Global constant configurations, dynamic cross-platform paths, and metadata (`AppConfig.java`).
- `model`: Strictly encapsulated POJO domain entities implementing `Serializable` and `Comparable` (`Vulnerability`, `ManualThreatRecord`, `TemporalMetric`, `ForecastResult`, `EarlyWarningIndicator`, `VendorRiskMetric`, `ImportMetadata`).
- `dao`: Database connectivity, transaction handling, SQL queries, and CRUD implementations (`DatabaseManager`, `VulnerabilityDao`, `ManualThreatDao`, `ImportMetadataDao`).
- `service`: High-throughput ingestion pipelines (Apache Commons CSV, Google Gson) and export generators (`CisaDataImporter`, `DataExportService`).
- `analytics`: Pure mathematical and analytical algorithms implementing statistical formulas without GUI coupling (`TemporalAnalysisEngine`, `EarlyWarningEngine`, `ForecastingEngine`, `RansomwareRiskAnalyzer`).
- `ui`: Modular Swing views, custom table models, renderers, badge cards, and dialogs (`MainWindow`, `DashboardView`, `VulnerabilityTableView`, etc.).
- `util`: Reusable helper utilities for regex validation, date conversions, and JFreeChart theme styling (`ValidationUtils`, `DateUtils`, `ChartUtils`).
- `exception`: Domain-specific unchecked hierarchy (`CyberPulseException`, `DatabaseException`, `DataValidationException`, `ImportException`).

---

## 3. Multithreading & Asynchronous Processing
To strictly adhere to academic and industry best practices, the graphical Event Dispatch Thread (EDT) is never blocked during file I/O, database batch inserts, or heavy forecasting operations:
1. **SwingWorker Integration:** Background threads execute parsing (`CisaDataImporter.importCsv`) and database queries, while intermediate progress is published to the UI via `process(List<String>)`.
2. **EDT Safety:** UI updates (`done()` callback) execute on the EDT, ensuring thread-safe Swing component mutations.
3. **Responsive Visual Feedback:** Real-time percentage progress bars and animated status indicators notify users of long-running operations.
