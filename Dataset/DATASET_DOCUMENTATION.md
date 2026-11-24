# Dataset Documentation: CISA Known Exploited Vulnerabilities (KEV) Catalog

## 1. Provenance & Official Source
- **Originating Body:** Cybersecurity and Infrastructure Security Agency (CISA), United States Department of Homeland Security.
- **Authority Directive:** Binding Operational Directive 22-01 (BOD 22-01) - *Reducing the Significant Risk of Known Exploited Vulnerabilities*.
- **Dataset Identification:** Section G Faculty-Designated Source.
- **Official CSV Distribution:** `https://www.cisa.gov/sites/default/files/csv/known_exploited_vulnerabilities.csv`
- **Official JSON Distribution:** `https://www.cisa.gov/sites/default/files/feeds/known_exploited_vulnerabilities.json`
- **Catalog Snapshot Date:** November 2026.
- **Total Catalogued Records:** 1,795 confirmed exploited CVE records.
- **Chronological Span:** November 3, 2021 to November 24, 2026 (61 calendar months).

---

## 2. Subject Matter & Analytical Purpose
The CISA KEV Catalog serves as the authoritative, globally recognized repository of software and hardware security vulnerabilities that have been confirmed by cybersecurity analysts and federal authorities to be actively exploited by malicious threat actors in the wild.

In traditional cybersecurity management, catalogs are viewed statically as vulnerability dictionaries. In **CyberPulse**, the dataset is leveraged for **temporal intelligence**, longitudinal analysis, early-warning surge detection, and time-series addition forecasting.

---

## 3. Data Dictionary & Attribute Specifications

| Field Name | Relational Column | Data Type | Constraint | Description |
| :--- | :--- | :--- | :--- | :--- |
| `cveID` | `cve_id` | Text (VARCHAR) | `PRIMARY KEY` | Standard Common Vulnerabilities and Exposures identifier (e.g., `CVE-2024-38063`). Format: `^CVE-\d{4}-\d{4,}$`. |
| `vendorProject` | `vendor_project` | Text (VARCHAR) | `NOT NULL` | The commercial vendor, organization, or open-source foundation (e.g., Microsoft, Cisco, Apple, Google, Apache). |
| `product` | `product` | Text (VARCHAR) | `NOT NULL` | The specific software application, firmware, or hardware operating system (e.g., Windows, iOS, IOS XE, Chrome). |
| `vulnerabilityName` | `vulnerability_name` | Text | Nullable | Descriptive technical title assigned to the vulnerability. |
| `dateAdded` | `date_added` | Text (ISO-8601) | `NOT NULL` | The exact calendar date (`YYYY-MM-DD`) the flaw was officially catalogued into KEV. Used as the primary temporal index. |
| `shortDescription` | `short_description` | Text | Nullable | Executive explanation of flaw mechanics, attack vector, and consequences. |
| `requiredAction` | `required_action` | Text | Nullable | Specific remediation mandate issued by CISA (e.g., apply vendor updates or discontinue product). |
| `dueDate` | `due_date` | Text (ISO-8601) | Nullable | Mandatory remediation compliance deadline for federal executive agencies. |
| `knownRansomwareCampaignUse` | `known_ransomware_campaign_use` | Text (Categorical)| Nullable | Indicates whether the vulnerability is actively leveraged in extortion/ransomware campaigns (`Known` vs `Unknown`). |
| `forensicTriage` | `forensic_triage` | Text (Categorical)| Nullable | Indicates if specialized forensic triage guidance is required under BOD 24-01 / BOD 26-04 (`Yes` vs `No`). |
| `notes` | `notes` | Text (URLs) | Nullable | Semicolon-delimited references, vendor advisories, and NIST NVD links. |
| `cwes` | `cwes` | Text (Delimited)| Nullable | Associated Common Weakness Enumeration categories (e.g., `CWE-787, CWE-120`). |

---

## 4. Data Quality & Cleaning Summary
1. **Uniqueness:** The dataset enforces strict uniqueness on `cveID`. Zero duplicate primary keys exist in the official catalog.
2. **Missing Value Handling:**
   - `cveID`, `vendorProject`, `product`, and `dateAdded` have 100% completeness (0% missing).
   - `notes` and `dueDate` contain occasional empty strings or trailing whitespace which are sanitized using `ValidationUtils.clean()`.
3. **Temporal Ingestion:** All date fields conform to the ISO-8601 standard (`YYYY-MM-DD`). Parsing is verified using `java.time.LocalDate`.
4. **Data Integrity Audit:**
   - Total rows: 1,795.
   - Known Ransomware associated entries: 228 (12.7%).
   - Unique targeted vendor projects: 326.
   - Unique affected products: 687.
