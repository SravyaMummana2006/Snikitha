# SQLite Relational Database Schema & Data Dictionary

## 1. Database Specifications
- **RDBMS Engine:** SQLite 3 (Driver: `org.xerial:sqlite-jdbc:3.46.1.3`).
- **Target File:** `cyberpulse.db` (Located dynamically in project execution directory).
- **Concurrency & WAL Mode:** Configured with `PRAGMA journal_mode = WAL;` and `PRAGMA synchronous = NORMAL;` for high-throughput write performance.

---

## 2. Table Specifications

### Table 1: `cisa_kev` (Official Vulnerability Catalog)
Stores all validated CISA Known Exploited Vulnerabilities records.

```sql
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

CREATE INDEX IF NOT EXISTS idx_cisa_date_added ON cisa_kev (date_added);
CREATE INDEX IF NOT EXISTS idx_cisa_vendor ON cisa_kev (vendor_project);
CREATE INDEX IF NOT EXISTS idx_cisa_ransomware ON cisa_kev (known_ransomware_campaign_use);
```

### Table 2: `manual_threats` (Analyst Incident Registry)
Stores internally observed threats, zero-days, and security incident investigations.

```sql
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
```

### Table 3: `import_metadata` (Ingestion Provenance & Audit Trail)
Audits every dataset ingestion event for reproducibility.

```sql
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
```
