# SRM UNIVERSITY – AP
### DEPARTMENT OF COMPUTER SCIENCE AND ENGINEERING
## CSE 309 – ADVANCED JAVA PROGRAMMING
### B.Tech III Semester | Academic Year: 2026 – 2027

---

# PROJECT REPORT
# **CYBERPULSE: Early Detection of Emerging Cyber Threats Using Temporal Intelligence and Vulnerability Analytics**

**Submitted by:**
- **Ch. Devi Snikitha (Reg. No: AP25110070820)** – *B.Tech CSE, Section G*
- **Jaya Garikapati (Reg. No: AP25110070629)**
- **Siripurapu Vijaya (Reg. No: AP25110070563)**
- **Sruthii Buganna (Reg. No: AP25110071320)**
- **Vemula Sesha Srinivas (Reg. No: AP25110070021)**

**Faculty Guide & Evaluator:**
- **Gavaskar S**, Assistant Professor, Department of CSE, SRM University – AP

---

## CERTIFICATE

This is to certify that the project report entitled **“CYBERPULSE: Early Detection of Emerging Cyber Threats Using Temporal Intelligence and Vulnerability Analytics”** submitted by **Ch. Devi Snikitha (AP25110070820)**, **Jaya Garikapati (AP25110070629)**, **S. Vijaya (AP25110070563)**, **B. Sruthii (AP25110071320)**, and **Vemula Sesha Srinivas (AP25110070021)** of B.Tech III Semester CSE Section G in partial fulfillment of the requirements for the course **CSE 309 – Advanced Java Programming** during the Academic Year 2026–2027 is a bonafide record of original academic and technical work carried out under my supervision.

**Date:** 25 November 2026  
**Signature of Faculty Guide:**  
*(Gavaskar S, Assistant Professor, Department of CSE)*

---

## DECLARATION

We hereby declare that the project entitled **“CYBERPULSE: Early Detection of Emerging Cyber Threats Using Temporal Intelligence and Vulnerability Analytics”** is an original work designed, implemented, and tested by our group. All algorithmic models, relational schemas, graphical user interfaces, and automated test cases were independently developed using Java 21, Java Swing, FlatLaf, SQLite, JDBC, Apache Commons CSV, Google Gson, and JFreeChart.

All external datasets, libraries, and references have been duly cited and acknowledged.

- **Ch. Devi Snikitha (AP25110070820)**
- **Jaya Garikapati (AP25110070629)**
- **Siripurapu Vijaya (AP25110070563)**
- **Sruthii Buganna (AP25110071320)**
- **Vemula Sesha Srinivas (AP25110070021)**

---

## ACKNOWLEDGEMENTS

We express our profound gratitude to our project guide, **Gavaskar S**, Assistant Professor, Department of Computer Science and Engineering, SRM University – AP, for his constructive guidance, technical feedback, and continuous support throughout the semester.

We also thank the **Cybersecurity and Infrastructure Security Agency (CISA)** of the United States Department of Homeland Security for curating and making the official Known Exploited Vulnerabilities (KEV) Catalog publicly accessible under Binding Operational Directive 22-01. Finally, we thank the maintainers of Eclipse Adoptium OpenJDK 21, SQLite JDBC, FlatLaf, Apache Commons CSV, Google Gson, and JFreeChart for providing reliable open-source frameworks.

---

## ABSTRACT

Cybersecurity operations centers (SOCs) and vulnerability management teams are confronted with an ever-expanding volume of Common Vulnerabilities and Exposures (CVEs). Conventional vulnerability management systems treat threat catalogs as static reference dictionaries, focusing exclusively on qualitative severity scores (such as CVSS metrics). Consequently, organizations fail to identify temporal disclosure surges, rapid vendor threat concentrations, or shifts in ransomware exploitation patterns over time.

This project presents **CyberPulse**, an enterprise-grade Java desktop application engineered for temporal threat intelligence, surge anomaly detection, and longitudinal vulnerability forecasting. CyberPulse is built upon the official **CISA Known Exploited Vulnerabilities (KEV) Catalog**, comprising 1,795 authoritative exploited flaw records spanning 61 continuous calendar months (November 2021 through November 2026).

The application architecture incorporates a three-tier Model-View-Controller (MVC) and Data Access Object (DAO) pattern implemented in **Java 21**. It integrates **SQLite** relational persistence via **JDBC**, multithreaded non-blocking asynchronous data ingestion using **SwingWorker**, modern desktop UI styling through **FlatLaf**, and interactive time-series data visualizations powered by **JFreeChart**. 

The analytical core introduces four time-series forecasting models:
1. Moving Average ($k=3$)
2. Historical Cumulative Average
3. Simple Exponential Smoothing (SES, $\alpha=0.3$)
4. Autoregressive First-Difference Drift Model (ARIMA(1,1,0) approximation)

Model accuracy is rigorously evaluated using Mean Absolute Error (MAE) and Root Mean Squared Error (RMSE). An Early Warning surge engine calculates rolling baseline averages and detects statistical surges across 1.5x, 2.0x, and 2.5x thresholds. Unit testing executed with JUnit 5 verified 100% test pass rates across data validation, DAO batch persistence, and analytical engines.

**Keywords:** Advanced Java, Java 21, Swing, FlatLaf, SQLite, JDBC, CISA KEV, Temporal Intelligence, Time-Series Forecasting, Early Warning Surge Detection, JFreeChart.

---

# TABLE OF CONTENTS
1. Project Identification and Title
2. Project Background
3. Problem Statement
4. Objectives and Scope
5. System Requirements Specification
6. System Architecture
7. UML Class Diagram & Design Diagrams
8. Object-Oriented Programming (OOP) Implementation
9. Advanced Java Concepts Implementation
10. Graphical User Interface (GUI) Implementation
11. Database Connectivity & Data Pipeline
12. Analytical Methodology & Algorithmic Formulations
13. Verification, Testing & Empirical Results
14. Application Screenshots & Visual Demonstration
15. Discussion & Academic Interpretation
16. Limitations & Future Enhancements
17. Conclusion
18. References

---

## 1. PROJECT IDENTIFICATION AND TITLE

- **Project Title:** CYBERPULSE: Early Detection of Emerging Cyber Threats Using Temporal Intelligence and Vulnerability Analytics
- **Course Title:** Advanced Java Programming (CSE 309)
- **Academic Semester:** B.Tech Computer Science & Engineering, III Semester (Odd Semester 2026–2027)
- **Class Section:** Section G
- **Institution:** SRM University – AP, Andhra Pradesh, India
- **Selected Dataset:** CISA Known Exploited Vulnerabilities (KEV) Official Catalog (Section G Faculty Source)

---

## 2. PROJECT BACKGROUND

The frequency and sophistication of cyberattacks targeting global enterprise infrastructure have surged exponentially. Attackers weaponize software vulnerabilities within days—or even hours—of public disclosure. In response to widespread exploitation, the United States Cybersecurity and Infrastructure Security Agency (CISA) issued **Binding Operational Directive 22-01 (BOD 22-01)**, establishing the Known Exploited Vulnerabilities (KEV) Catalog. The catalog mandates federal and enterprise remediation of confirmed exploited flaws.

While KEV provides authoritative confirmation of real-world exploitation, security practitioners typically inspect it as a flat spreadsheet or tabular feed. This static consumption misses crucial longitudinal dynamics:
- *When do disclosures spike across specific commercial vendors?*
- *Are threat actors pivoting toward zero-day remote code execution or credential authentication bypasses?*
- *Can time-series moving averages detect emerging threat surges before operational damage occurs?*

**CyberPulse** addresses these questions by applying Java-based temporal intelligence to vulnerability intelligence.

---

## 3. PROBLEM STATEMENT

Existing vulnerability management workflows exhibit three critical deficiencies:
1. **Absence of Temporal Dimensions:** Vulnerabilities are evaluated in isolation without contextual awareness of disclosure velocity or historical baselines.
2. **Lack of Automated Surge Detection:** Sudden clusters of vulnerabilities targeting ubiquitous platforms (e.g., Microsoft Windows, Apple iOS, Cisco IOS XE) go undetected until public exploitation reaches crisis levels.
3. **Inadequate Forensic & Ransomware Correlation:** Security teams struggle to rapidly filter vulnerabilities actively leveraged in ransomware extortion campaigns versus standard proof-of-concept exploits.

---

## 4. OBJECTIVES AND SCOPE

### 4.1 Objectives
1. **Automated Data Ingestion:** Engineer a multithreaded ingestion engine supporting both CISA KEV CSV and JSON formats with comprehensive regex-based validation.
2. **Relational Database Design:** Architect a persistent SQLite database with indexes optimized for chronological queries and composite keyword filtering.
3. **Temporal Intelligence Engine:** Implement Java Stream algorithms to aggregate data across 61 calendar months and calculate Month-over-Month (MoM) growth rates.
4. **Early Warning Surge Detector:** Develop an anomaly detection algorithm that compares current catalogued volumes against trailing moving baselines (1.5x, 2.0x, 2.5x thresholds).
5. **Predictive Forecasting Models:** Implement and compare Moving Average, Historical Average, Simple Exponential Smoothing, and ARIMA(1,1,0) difference models evaluated via MAE and RMSE.
6. **Modern Desktop Interface:** Construct a responsive, polished Java Swing desktop interface with FlatLaf styling, KPI cards, and dynamic JFreeCharts.

### 4.2 Scope
- **Included:** Longitudinal statistical analysis, temporal clustering, early-warning anomaly detection, forecasting, manual threat tracking, and audit logging.
- **Excluded:** Real-time packet sniffing, intrusive network scanning, and automated exploit execution.

---

## 5. SYSTEM REQUIREMENTS SPECIFICATION

### 5.1 Hardware Requirements
- **Processor:** Dual-Core x86-64 / ARM64 processor (Intel Core i3/i5/i7, AMD Ryzen, or Apple Silicon), 2.0 GHz or higher.
- **System Memory (RAM):** Minimum 4 GB RAM (8 GB recommended).
- **Disk Storage:** Minimum 100 MB free hard drive space (includes SQLite database and dataset).
- **Display Resolution:** 1280x720 minimum (1920x1080 recommended).

### 5.2 Software Requirements
- **Operating System:** Windows 10/11 64-bit, macOS 12+, or Ubuntu Linux 22.04+.
- **Java Development Kit (JDK):** OpenJDK 21 (Temurin-21.0.13+9 LTS or Oracle JDK 21).
- **Build System:** Apache Maven 3.9+.
- **Database Engine:** Embedded SQLite 3 (bundled via `sqlite-jdbc`).

---

## 6. SYSTEM ARCHITECTURE

CyberPulse follows a decoupled **Three-Tier Architecture**:
1. **Presentation Tier:** Swing GUI components styled with FlatLaf Light, custom renderers, and JFreeChart panels.
2. **Business Logic & Analytics Tier:** Java Streams, numerical computation engines, validation services, and SwingWorker background threads.
3. **Data Access & Persistence Tier:** DAO interfaces, PreparedStatements, connection pooling, and embedded SQLite database (`cyberpulse.db`).

---

## 7. UML CLASS DIAGRAM & DESIGN DIAGRAMS

The system utilizes clean object-oriented abstraction:
- Domain entities: `Vulnerability`, `ManualThreatRecord`, `TemporalMetric`, `ForecastResult`, `EarlyWarningIndicator`, `VendorRiskMetric`, `ImportMetadata`.
- DAO Contracts: `VulnerabilityDao`, `ManualThreatDao`, `ImportMetadataDao`.
- Services: `CisaDataImporter`, `TemporalAnalysisEngine`, `EarlyWarningEngine`, `ForecastingEngine`, `RansomwareRiskAnalyzer`, `DataExportService`.
- UI Controllers: `MainWindow`, `DashboardView`, `VulnerabilityTableView`, `TemporalAnalyticsView`, `EarlyWarningView`, `ForecastingView`, `ThreatManagementView`, `DataImportView`.

---

## 8. OBJECT-ORIENTED PROGRAMMING (OOP) IMPLEMENTATION

CyberPulse demonstrates core OOP principles without artificial scaffolding:
1. **Encapsulation:** All model entities declare strictly private member variables with accessor and mutator methods, enforcing invariants (e.g., `ValidationUtils.clean()`).
2. **Abstraction:** High-level services interact exclusively through DAO interfaces (`VulnerabilityDao`), hiding JDBC and SQLite dialect specifics.
3. **Inheritance & Polymorphism:** Custom exception hierarchies inherit from `CyberPulseException -> RuntimeException`. JTable renderers override `DefaultTableCellRenderer` to dynamically format cells based on runtime alert levels.
4. **Interfaces:** Implementation of `Comparable<Vulnerability>` and `Comparable<TemporalMetric>` enables natural sorting within Java Collections.

---

## 9. ADVANCED JAVA CONCEPTS IMPLEMENTATION

1. **Multithreading with SwingWorker:** The graphical Event Dispatch Thread (EDT) is never blocked during file I/O or database operations. `SwingWorker<T, V>` coordinates background tasks with real-time UI progress updates.
2. **Java Streams and Lambdas:** Complex multidimensional aggregations leverage Stream pipelines, e.g.:
   ```java
   Map<String, List<Vulnerability>> grouped = vulnerabilities.stream()
       .filter(v -> v.getDateAdded() != null)
       .collect(Collectors.groupingBy(v -> DateUtils.toYearMonthString(v.getDateAdded())));
   ```
3. **Generics & Collections:** Type-safe utilization of `List<T>`, `Map<K, V>`, `Optional<T>`, and `Set<T>` for in-memory deduplication and fast indexing.
4. **Custom Exception Hierarchy:** Differentiated error handling across `DatabaseException`, `DataValidationException`, and `ImportException`.
5. **JDBC Transaction Management:** Batch operations utilize explicit commit and rollback boundaries with `conn.setAutoCommit(false)`.

---

## 10. GRAPHICAL USER INTERFACE (GUI) IMPLEMENTATION

The user interface leverages **FlatLaf 3.5.4** for a clean, modern aesthetic:
- **Responsive Navigation Sidebar:** Fixed 240px navy sidebar (`#18263B`) with active royal blue button highlights (`#1877F2`).
- **Executive KPI Cards:** Metric summaries with colored accent strips showing catalog counts, ransomware numbers, and critical alerts.
- **Searchable JTable:** Fast tabular view supporting instant keyword search, vendor dropdown selection, ransomware filters, and row-double-click detail dialogs.
- **Visual Analytics:** Interactive JFreeCharts with anti-aliasing, styled gridlines, and cross-platform font rendering.

---

## 11. DATABASE CONNECTIVITY & DATA PIPELINE

Relational persistence is handled by **SQLite**:
- Database file: `cyberpulse.db`.
- WAL Mode: `PRAGMA journal_mode = WAL;` enables non-blocking concurrent reads and writes.
- Batch Insertion: `PreparedStatement` batches of 500 rows ensure 1,795 records import in under 10 seconds.
- Deduplication: Enforced by SQLite `cve_id TEXT PRIMARY KEY` combined with in-memory `HashSet<String>` tracking.

---

## 12. ANALYTICAL METHODOLOGY & ALGORITHMIC FORMULATIONS

### 12.1 Early Warning Surge Ratio
$$R_t = \frac{C_t}{\frac{1}{k} \sum_{i=1}^{k} C_{t-i}}$$
Where $C_t$ is the current month catalogued count, and $k=3$ is the trailing baseline window.
- Normal: $R_t < 1.5$
- Elevated: $1.5 \le R_t < 2.0$
- High: $2.0 \le R_t < 2.5$
- Critical: $R_t \ge 2.5$

### 12.2 Forecasting Models
1. **Moving Average ($k=3$):** $\hat{y}_t = \frac{1}{3}(y_{t-1} + y_{t-2} + y_{t-3})$
2. **Historical Average:** $\hat{y}_t = \frac{1}{t-1}\sum_{i=1}^{t-1} y_i$
3. **Simple Exponential Smoothing ($\alpha=0.3$):** $\hat{y}_t = \alpha y_{t-1} + (1-\alpha)\hat{y}_{t-1}$
4. **First-Difference Drift Model (ARIMA(1,1,0)):** $\hat{y}_t = y_{t-1} + \phi(y_{t-1} - y_{t-2})$ with $\phi=0.4$.

### 12.3 Error Evaluation Metrics
$$\text{MAE} = \frac{1}{n}\sum_{t=1}^{n} |y_t - \hat{y}_t|, \quad \text{RMSE} = \sqrt{\frac{1}{n}\sum_{t=1}^{n} (y_t - \hat{y}_t)^2}$$

---

## 13. VERIFICATION, TESTING & EMPIRICAL RESULTS

### 13.1 JUnit 5 Test Suite Results
All unit tests executed with **0 failures, 0 errors**:
- `AnalyticsEngineTest`: 4 tests (Temporal grouping, MoM growth, Early Warning ratios, Forecasting calculations).
- `VulnerabilityDaoTest`: 4 tests (Single insert, batch insert, duplicate handling, parameter filtering, deletion).
- `CisaDataImporterTest`: 3 tests (CSV parsing, JSON parsing, validation filtering).
- **Total Tests: 11 | Passed: 11 | Failed: 0 | Execution Time: 0.601s**.

### 13.2 Empirical Dataset Findings
- **Total Records:** 1,795 confirmed exploited CVEs.
- **Ransomware Campaign Exploits:** 228 CVEs (12.70% of total).
- **Observation Period:** 61 months (November 2021 to November 2026).
- **Peak Addition Month:** March 2022 (with 181 catalog additions).
- **Average Monthly Additions:** 29.4 CVEs/month.
- **Top Vulnerable Vendors:** Microsoft, Apple, Cisco, Google, Oracle.
- **Most Prevalent Weaknesses:** CWE-787 (Out-of-bounds Write), CWE-120 (Buffer Copy), CWE-20 (Improper Input Validation).

---

## 14. APPLICATION SCREENSHOTS & VISUAL DEMONSTRATION

*(Detailed descriptions corresponding to the running application)*
1. **Executive Dashboard (`DashboardView`):** Displays 5 top KPI cards (`1,795` total CVEs, `228` ransomware, `326` vendors, `8` critical surges, `0` active manual threats) and two JFreeCharts.
2. **Vulnerability Catalog (`VulnerabilityTableView`):** Displays sortable JTable with instant search field, vendor dropdown, and double-click detail dialog.
3. **Temporal Analytics (`TemporalAnalyticsView`):** Shows multi-line chronological graph of total vs ransomware additions with MoM growth percentages.
4. **Early Warning System (`EarlyWarningView`):** Highlights critical surge months with red/orange/yellow color-coded badges and baseline ratio metrics.
5. **Threat Forecasting (`ForecastingView`):** Multi-series comparative chart showing Actual vs MA, Hist Avg, SES, and ARIMA models with MAE/RMSE table.
6. **Incident Logging (`ThreatManagementView`):** Interactive analyst form to log local zero-days and security incidents with CRUD capabilities.
7. **Data Ingestion & Audit (`DataImportView`):** Shows asynchronous SwingWorker import progress bar and historical import audit log.

---

## 15. DISCUSSION & ACADEMIC INTERPRETATION

A critical academic insight highlighted in the system and documentation is that **CISA KEV catalog additions represent administrative and regulatory confirmation of exploitation**, rather than direct real-time cyberattack counts. Surges in late 2021 and early 2022 represent the initial backlog ingestion following the establishment of BOD 22-01, whereas subsequent spikes reflect coordinated disclosure cycles and critical zero-day discoveries.

---

## 16. LIMITATIONS & FUTURE ENHANCEMENTS

### Limitations
- Data granularity is constrained by CISA's official disclosure dates rather than raw attacker packet timestamps.
- Time-series models assume historical continuity which can be perturbed by sudden regulatory policy updates.

### Future Enhancements
- Integration with live NIST NVD CVSS v3.1 API feeds.
- Automated EPSS (Exploit Prediction Scoring System) probability score fetching.
- Distributed REST microservice deployment using Spring Boot.

---

## 17. CONCLUSION

The **CyberPulse** project successfully bridges academic Java curriculum requirements with real-world cybersecurity intelligence. By leveraging Java 21, Swing, FlatLaf, SQLite, JDBC, Apache Commons CSV, Google Gson, and JFreeChart, the system transforms raw spreadsheet data into an interactive, demonstrable desktop analytical workstation. The project fulfills all course objectives for CSE 309 and is fully prepared for faculty evaluation and viva defense.

---

## 18. REFERENCES
1. Cybersecurity and Infrastructure Security Agency (CISA), *Known Exploited Vulnerabilities Catalog*, BOD 22-01, 2026.
2. Oracle Corporation, *Java SE 21 Platform Documentation*, 2024.
3. FormDev Software GmbH, *FlatLaf – Modern Open-Source Look and Feel for Java Swing*, 2024.
4. JFree.org, *JFreeChart Developer Guide and API Reference*, Version 1.5.5, 2024.
5. SQLite Consortium, *SQLite Database Engine Documentation*, 2024.
6. Apache Software Foundation, *Commons CSV User Guide*, Version 1.11.0, 2024.
7. Google Inc., *Gson User Guide & JSON Serializer Reference*, 2024.
