# CYBERPULSE: Early Detection of Emerging Cyber Threats Using Temporal Intelligence and Vulnerability Analytics

**Advanced Java Programming (CSE 309) — End-Semester Project**  
**SRM University – AP | Department of Computer Science & Engineering**  
**Academic Year:** 2026–2027 (Odd Semester) | **Class Section:** B.Tech CSE Section G  

### Project Team Members:
- **Ch. Devi Snikitha (AP25110070820)**
- **Jaya Garikapati (AP25110070629)**
- **Siripurapu Vijaya (AP25110070563)**
- **Sruthii Buganna (AP25110071320)**
- **Vemula Sesha Srinivas (AP25110070021)**

**Faculty Guide:** Gavaskar S, Assistant Professor, Department of CSE

---

## 1. Quick Start & Execution

### Option A: One-Click Execution (Windows)
Double-click the bundled batch script in the root project folder:
```cmd
run.bat
```

### Option B: Command Line (Java 21)
Run the pre-compiled executable shaded JAR:
```cmd
java -jar "Implementation\target\cyberpulse-temporal-intelligence-1.0.0.jar"
```

### Option C: Rebuild from Source (Maven)
```cmd
cd Implementation
mvn clean package
java -jar target\cyberpulse-temporal-intelligence-1.0.0.jar
```

---

## 2. Project Overview & Faculty Dataset Selection

### Dataset Selection: CISA Known Exploited Vulnerabilities (KEV) Catalog (Section G)
The application is built upon the official **CISA Known Exploited Vulnerabilities (KEV) Catalog** designated for Section G:
- **Total Records:** 1,795 confirmed in-the-wild exploited CVE entries.
- **Time Window:** November 2021 to November 2026 (61 continuous calendar months).
- **Ransomware Campaign Exploits:** 228 CVEs (12.7%).
- **Vendors & Products:** 326 unique vendors and 687 distinct software products.
- **Files Stored In:** `Dataset/known_exploited_vulnerabilities.csv` and `Dataset/known_exploited_vulnerabilities.json`.

---

## 3. Technology Stack & Dependencies

| Layer | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java (OpenJDK) | 21 LTS | Core programming language |
| **GUI Framework** | Java Swing + FlatLaf | 3.5.4 | Modern, polished desktop look and feel |
| **Database** | SQLite + JDBC | 3.46.1.3 | Embedded zero-configuration relational persistence |
| **Visualizations** | JFreeChart | 1.5.5 | Time-series line graphs, risk bar charts, CWE pie charts |
| **Data Parsing** | Apache Commons CSV | 1.11.0 | High-performance CSV ingestion & validation |
| **JSON Processing** | Google Gson | 2.11.0 | Direct parsing of CISA KEV JSON feeds |
| **Unit Testing** | JUnit 5 (Jupiter) | 5.10.3 | Automated testing for DAOs, parsers, and engines |
| **Build System** | Apache Maven | 3.9+ | Build lifecycle, dependency management & shaded JAR packaging |

---

## 4. Key Architectural Modules & Features

1. **Executive Dashboard (`DashboardView`):**
   - 5 KPI summary cards: Total KEVs (`1,795`), Ransomware Exploits (`228`), Targeted Vendors (`326`), Critical Surges (`8`), Manual Threats (`0`).
   - Live JFreeChart time-series trend chart and CWE distribution donut chart.
2. **Vulnerability Catalog (`VulnerabilityTableView`):**
   - Multi-column search, vendor filtering dropdown, and ransomware status filters.
   - Non-editable `JTable` with alternating row colors and center-aligned dates.
   - Row double-click modal displaying full remediation instructions and NIST references.
   - One-click CSV export of filtered records.
3. **Temporal Analytics (`TemporalAnalyticsView`):**
   - 61-month longitudinal aggregation using Java Streams and Lambdas.
   - Calculation of Month-over-Month (MoM) expansion and contraction percentages.
   - Identification of historical peak month (March 2022: 181 additions) and average monthly additions (29.4/month).
4. **Early Warning Surge Detection (`EarlyWarningView`):**
   - Calculates moving baseline averages over trailing windows (3, 6, 12 months).
   - Flags sudden surges using statistical thresholds: Normal (<1.5x), Elevated (1.5x-2.0x), High (2.0x-2.5x), and Critical (≥2.5x).
5. **Time-Series Threat Forecasting (`ForecastingView`):**
   - Compares 4 predictive algorithms: Moving Average, Historical Average, Simple Exponential Smoothing (SES), and ARIMA(1,1,0) Drift Model.
   - Displays Mean Absolute Error (MAE) and Root Mean Squared Error (RMSE) performance metrics.
6. **Incident Logging & Threat Management (`ThreatManagementView`):**
   - Form for security analysts to log local incidents and zero-day observations.
   - Full CRUD capabilities: create, update mitigation status, delete, and refresh.
7. **Data Ingestion & Provenance Audit (`DataImportView`):**
   - Asynchronous non-blocking file imports via `SwingWorker` with dynamic `JProgressBar`.
   - Historical audit log table recording timestamp, source format, valid records, duplicates skipped, and execution duration in milliseconds.

---

## 5. Automated Test Suite (JUnit 5)

Run all unit tests via Maven:
```cmd
cd Implementation
mvn test
```

### Verified Test Results:
- `AnalyticsEngineTest`: 4 tests passed (Temporal grouping, MoM growth, Early Warning ratios, Forecasting calculations).
- `VulnerabilityDaoTest`: 4 tests passed (Single insert, batch insert, duplicate skipping, parameter filtering, deletion).
- `CisaDataImporterTest`: 3 tests passed (CSV parsing, JSON parsing, validation filtering).
- **Total: 11 tests run, 0 failures, 0 errors, 100% success rate.**

---

## 6. Directory Structure

```
├── Dataset/
│   ├── known_exploited_vulnerabilities.csv
│   ├── known_exploited_vulnerabilities.json
│   └── DATASET_DOCUMENTATION.md
├── Implementation/
│   ├── pom.xml
│   ├── src/main/java/
│   │   ├── app/ (CyberPulseApp.java)
│   │   ├── config/ (AppConfig.java)
│   │   ├── model/ (Vulnerability, TemporalMetric, etc.)
│   │   ├── dao/ (DatabaseManager, VulnerabilityDao, etc.)
│   │   ├── service/ (CisaDataImporter, DataExportService)
│   │   ├── analytics/ (TemporalAnalysisEngine, ForecastingEngine, etc.)
│   │   ├── ui/ (MainWindow, DashboardView, etc.)
│   │   ├── util/ (ValidationUtils, DateUtils, ChartUtils)
│   │   └── exception/ (CyberPulseException hierarchy)
│   ├── src/test/java/
│   └── target/
│       └── cyberpulse-temporal-intelligence-1.0.0.jar (Executable Fat JAR)
├── Documentation/
│   ├── SYSTEM_ARCHITECTURE.md
│   ├── UML_DESIGN_DIAGRAMS.md
│   └── DATABASE_SCHEMA.md
├── Report/
│   └── CSE309_END_SEMESTER_PROJECT_REPORT.md (Comprehensive Academic Report)
├── PPT/
│   └── VIVA_PRESENTATION_DECK.md (15-Slide Faculty Defense Presentation)
├── cyberpulse.db (Populated SQLite Database)
├── run.bat (One-Click Application Launcher)
└── README.md (This File)
```
