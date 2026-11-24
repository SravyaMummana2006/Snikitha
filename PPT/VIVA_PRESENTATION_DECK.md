# CYBERPULSE: Viva Voce & Faculty Evaluation Presentation Deck
### CSE 309 – Advanced Java Programming | End-Semester Evaluation
**SRM University – AP | Department of Computer Science & Engineering**

---

## Slide 1: Title & Group Identification
- **Project Title:** CYBERPULSE: Early Detection of Emerging Cyber Threats Using Temporal Intelligence and Vulnerability Analytics
- **Course:** CSE 309 – Advanced Java Programming
- **Class Section:** B.Tech CSE III Semester, Section G
- **Group Members:**
  - Ch. Devi Snikitha (AP25110070820)
  - Jaya Garikapati (AP25110070629)
  - Siripurapu Vijaya (AP25110070563)
  - Sruthii Buganna (AP25110071320)
  - Vemula Sesha Srinivas (AP25110070021)
- **Faculty Guide:** Gavaskar S, Assistant Professor, Department of CSE

---

## Slide 2: Problem Statement & Motivation
- **The Core Dilemma:** Global organizations face thousands of active software vulnerabilities, yet threat catalogs are consumed as flat, static dictionaries without temporal context.
- **The Gap:** Conventional security operations miss velocity spikes, vendor threat concentration trends, and ransomware weaponization shifts over time.
- **Our Solution:** A Java 21 desktop intelligence workstation that ingests 1,795 confirmed exploited CVEs, analyzes 61 calendar months of temporal activity, detects surges, and models future additions.

---

## Slide 3: Dataset Selection & Provenance
- **Dataset Examined:** Section G Official CISA Known Exploited Vulnerabilities (KEV) Catalog.
- **Total Records:** 1,795 confirmed in-the-wild exploited CVE entries.
- **Time Span:** November 2021 to November 2026 (61 calendar months).
- **Key Dimensions:**
  - Identifiers: `cveID`, `vulnerabilityName`
  - Categorical: `vendorProject`, `product`, `cwes`
  - Temporal: `dateAdded` (ISO-8601), `dueDate`
  - High-Impact Flags: `knownRansomwareCampaignUse` (228 Known entries)

---

## Slide 4: System Architecture & Technologies
- **Language & Runtime:** Java 21 (LTS) on OpenJDK.
- **Desktop Interface:** Java Swing styled with FlatLaf Light modern look & feel.
- **Persistence Layer:** Embedded SQLite 3 database (`cyberpulse.db`) via JDBC with WAL mode.
- **Data Ingestion:** Apache Commons CSV & Google Gson with schema validation.
- **Visualization:** JFreeChart 1.5.5 for interactive time-series line, bar, and pie charts.
- **Testing:** JUnit 5 test suite (11 unit tests, 100% pass rate).

---

## Slide 5: Core Object-Oriented Programming (OOP)
- **Encapsulation:** Strongly typed model entities (`Vulnerability`, `TemporalMetric`, `EarlyWarningIndicator`) protecting internal states.
- **Abstraction:** DAO Pattern (`VulnerabilityDao` interface) completely isolates SQL persistence from business logic.
- **Inheritance & Polymorphism:** Custom exception tree (`CyberPulseException` hierarchy); custom table cell renderers dynamically coloring threat levels.
- **Interfaces:** `Comparable<Vulnerability>` and `Comparable<TemporalMetric>` providing natural chronological ordering in Java Collections.

---

## Slide 6: Advanced Java Requirements
- **Multithreading:** `SwingWorker` manages background file parsing and database writes without freezing the UI Event Dispatch Thread (EDT).
- **Java Streams & Lambdas:** Functional grouping and transformations across 1,795 records:
  - `Collectors.groupingBy()` for monthly temporal bucketing.
  - `Collectors.counting()` for CWE and vendor rankings.
- **Generics & Collections:** Type-safe `List<T>`, `Map<K, V>`, and `Optional<T>`.
- **JDBC Transactions:** Explicit batch processing with rollback guarantees.

---

## Slide 7: Database Design & Schema
- **Embedded SQLite:** Standalone `cyberpulse.db` requiring zero external server configuration.
- **Tables:**
  1. `cisa_kev`: Primary vulnerability catalog (indexed by `date_added`, `vendor_project`, `known_ransomware_campaign_use`).
  2. `manual_threats`: Analyst incident tracking with full CRUD.
  3. `import_metadata`: Ingestion audit log tracking duration, file format, and duplicate counts.
- **Performance:** 1,795 records batch-inserted in under 10 seconds.

---

## Slide 8: Data Ingestion & Validation Pipeline
- **Dual Format Ingestion:** Seamlessly parses both CISA CSV and JSON feeds.
- **Input Validation Rules:**
  - Regex validation: `^CVE-\d{4}-\d{4,}$`
  - Date validation: Strict ISO-8601 parsing (`yyyy-MM-dd`).
  - Text sanitization: Trimming, null-safety guards.
- **Deduplication:** Composite SQLite primary key + in-memory `HashSet` prevents duplicate insertion.

---

## Slide 9: Longitudinal Temporal Analytics
- **Chronological Grouping:** Aggregates records into 61 monthly temporal buckets.
- **Growth Analysis:** Computes Month-over-Month (MoM) expansion and contraction percentages.
- **Key Empirical Findings:**
  - Peak Month: March 2022 (181 additions, post-BOD 22-01 backlog ingestion).
  - Historical Mean: 29.4 vulnerability additions per month.
  - Ransomware Exploit Share: 12.7% (228 out of 1,795 CVEs).

---

## Slide 10: Early Warning Surge Detection System
- **Algorithmic Logic:** Evaluates the surge ratio $R_t$ comparing current month volume against trailing 3-month moving average baseline:
  $$R_t = \frac{C_t}{\text{Baseline}_{3\text{mo}}}$$
- **Threat Severity Tiers:**
  - Normal ($< 1.5\text{x}$)
  - Elevated ($1.5\text{x} - 2.0\text{x}$)
  - High ($2.0\text{x} - 2.5\text{x}$)
  - Critical ($\ge 2.5\text{x}$)
- **Actionable Alerts:** Highlights anomalous spikes targeting enterprise tech stacks.

---

## Slide 11: Time-Series Forecasting Models
- **Four Comparative Techniques Implemented:**
  1. Moving Average ($k=3$ trailing window)
  2. Historical Average (cumulative mean)
  3. Simple Exponential Smoothing (SES, $\alpha=0.3$)
  4. ARIMA(1,1,0) First-Difference Drift Model
- **Evaluation Criteria:**
  - Mean Absolute Error (MAE): $\frac{1}{n}\sum |y - \hat{y}|$
  - Root Mean Squared Error (RMSE): $\sqrt{\frac{1}{n}\sum (y - \hat{y})^2}$
- **Next-Month Prediction:** Walk-forward generation of next period's expected additions.

---

## Slide 12: Application Demonstration & GUI Features
- **Sidebar Navigation:** Smooth CardLayout transitions between 7 operational modules.
- **Interactive Dashboard:** 5 KPI cards + dual live JFreeCharts.
- **Vulnerability Catalog:** Fast keyword search, vendor dropdown, ransomware filters, and row double-click modal details.
- **Incident Logger:** Form for logging local zero-days with toggleable mitigation status.
- **Export Engine:** One-click CSV and formatted executive text report generation.

---

## Slide 13: Testing & Quality Assurance
- **Framework:** JUnit 5 (Jupiter).
- **Test Coverage:**
  - `VulnerabilityDaoTest`: 4 tests (CRUD, batch insert, duplicate skipping, filtering).
  - `CisaDataImporterTest`: 3 tests (CSV/JSON parsing, invalid input filtering, error handling).
  - `AnalyticsEngineTest`: 4 tests (Temporal grouping, surge ratios, forecasting metrics, vendor risks).
- **Outcome:** **11 of 11 Tests Passed (100% Success Rate, 0 Failures)**.

---

## Slide 14: Academic Interpretation & Limitations
- **Data Interpretation:** CISA KEV additions reflect regulatory disclosure confirmation, not real-time network packets.
- **Transparency:** The application clearly displays methodology notices preventing false attribution of attack frequency.
- **Design Philosophy:** Clean desktop experience without artificial dependencies, ensuring reproducible execution on any computer.

---

## Slide 15: Viva Defense Q&A & Conclusion
- **Key Takeaways:**
  - Fully working Java 21 desktop application.
  - Implements all required Advanced Java & OOP concepts meaningfully.
  - Clean Maven build producing a standalone executable JAR (`cyberpulse.jar`).
- **Thank You!** Ready for live demonstration and faculty questions.
