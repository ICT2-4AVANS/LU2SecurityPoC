# Code Coverage – Appointment Scheduling Module

| | |
| ----------- | ----------------------------------------------------------- |
| **Norm**    | NEN-7510:2024-2 (A.8.29 Beveiligingstesten)                 |
| **Module**  | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT        |
| **Datum**   | 2026-06-10                                                  |
| **Tooling** | JaCoCo 0.8.12 + GitHub Actions                              |

---

## 1. Doel

Aantoonbaar maken hoe goed de code van de Appointment Scheduling Module door tests wordt geraakt — als onderdeel van het bewijs voor:

- **NEN-7510:2024-2 A.8.29** "Security testing in development" — security-controls (zoals de `AuditLogger`) moeten aantoonbaar getest zijn.
- **NEN-7510:2024-2 A.8.25** "Secure development life cycle" — coverage geeft kwantitatief inzicht in test-discipline.
- De **Definition of Done** uit de security backlog (zie [`docs/security-backlog/security-backlog.md`](../security-backlog/security-backlog.md) § 4): "Er is een (unit-, integratie- of pentest-)test die het gedrag aantoont."

---

## 2. Tooling

- **JaCoCo** (`org.jacoco:jacoco-maven-plugin:0.8.12`) — Java-standaard, integreert met Surefire via `argLine` en draait in `mvn verify`.
- **CI** — `.github/workflows/ci.yml` voert `mvn verify` uit op elke push/PR, print een coverage-samenvatting in de Actions-log en uploadt de volledige HTML/XML/CSV als artifact `coverage-report-jacoco`.
- **Bereik** — `appointmentscheduling-api` module (de servicelaag waar de security-relevante code in zit). De `omod`-laag bevat voornamelijk JSP/Spring-controllers en is buiten scope voor unit-coverage; die hoort bij integratietests (zie roadmap).

---

## 3. Gekozen drempels (coverage gates)

Drempels zijn vastgelegd in `api/pom.xml` via de `jacoco:check` goal en falen de build wanneer ze niet gehaald worden.

| Scope                                                            | Counter | Drempel | Rationale (samengevat)                                          |
| ---------------------------------------------------------------- | ------- | ------: | --------------------------------------------------------------- |
| **Module-breed** (`BUNDLE`)                                      | LINE    | **30 %** | No-regression-baseline voor de legacy-codebase.                  |
| **Security-kritisch pakket** `org.openmrs.module.appointmentscheduling.audit` | LINE    | **90 %** | Nieuwe security-control (`AuditLogger`) moet bewezen getest zijn. |
| Idem                                                             | BRANCH  | **80 %** | Bewijst dat ook foutpaden (anonymous, unknown, denied, failed) doorlopen worden. |

### 3.1 Onderbouwing module-breed (30 %)

De module is een **legacy-codebase** uit OpenMRS 1.9 met decennia aan organisch gegroeide code. Realistisch:

- Bestaande tests dekken vooral de service-laag (`AppointmentServiceTest`, `TimeSlotServiceTest`, etc.); de DAO-, reporting- en task-laag bijna niet.
- Een agressieve drempel (bijv. 80 %) zou de eerste build laten falen en het remediatietraject (zie security backlog) blokkeren zonder beveiligingswinst.
- 30 % is **haalbaar** met de huidige testsuite én **niet-triviaal** — coverage mag niet dalen door PR's die enkel productiecode toevoegen zonder tests ("ratchet" tegen regressie).
- In de **roadmap** (zie § 5) is opgenomen om deze drempel per sprint te verhogen naarmate legacy-code wordt aangeraakt.

### 3.2 Onderbouwing security-kritisch pakket (90 % line + 80 % branch)

De `audit/`-package bevat de `AuditLogger`, de centrale security-control die voortkomt uit:

- **Security backlog SR-08 / SR-09** (audit logging van afspraakwijzigingen en Web UI-acties).
- **Vulnerability V-01 / pentest B-01** (PII-leak in legacy auditlog), waarvan de fix expliciet bewezen moet zijn.

Hiervoor geldt:

- **Branch coverage 80 %** dwingt af dat álle takken (null-user → `anonymous`, null-address → `unknown`, success/denied/failed) door tests doorlopen worden — niet alleen de happy path.
- **Line coverage 90 %** laat een kleine marge voor onbereikbare defensieve code (bijv. een impliciete enum-default), maar eist verder volledige uitvoering.
- Lager dan 90 % zou betekenen dat security-kritieke code ongetest in productie kan belanden — niet acceptabel onder NEN-7510 A.8.29.

### 3.3 Waarom geen 100 %?

100 % is geen kwaliteitsdoel op zich: het stimuleert ontwikkelaars om triviale tests te schrijven (getters, toString, onbereikbare exception-takken) puur om het getal te halen. 90 / 80 dwingt betekenisvolle dekking af zonder die perverse prikkel. Hetzelfde geldt module-breed: het gaat om vooruitgang, niet om een symbolisch getal.

---

## 4. Werking in CI

`.github/workflows/ci.yml` doet bij elke push/PR het volgende:

1. **Build & test** — `mvn --batch-mode verify` (= compile + test + jacoco:report + jacoco:check).
2. **Faal-bij-onder-drempel** — `jacoco:check` faalt de build wanneer een rule niet gehaald wordt. Output bevat dan de regel die faalde, bv:
   ```
   Rule violated for package org.openmrs.module.appointmentscheduling.audit:
     lines covered ratio is 0.85, but expected minimum is 0.90
   ```
3. **Coverage-samenvatting** — stap *"Print coverage summary"* leest `target/site/jacoco/jacoco.csv` en print line/branch percentages per submodule in een GitHub Actions `::group::`.
4. **Artifact** — stap *"Upload coverage report (JaCoCo)"* publiceert de volledige HTML/XML/CSV als artifact `coverage-report-jacoco`, downloadbaar vanuit het run-overzicht. Bewijs voor de auditor.

---

## 5. Roadmap drempel-aanscherping

| Sprint | Module-breed | Audit-pakket            | Trigger                                                               |
| ------ | -----------: | ----------------------- | --------------------------------------------------------------------- |
| 1      | 30 % LINE    | 90 % LINE + 80 % BRANCH | Huidige situatie, baseline.                                           |
| 2      | 40 % LINE    | 90 % / 80 %             | Na implementatie SR-02 (SQLi fix) + bijbehorende DAO-tests.           |
| 3      | 50 % LINE    | 90 % / 80 %             | Na implementatie SR-03 / SR-04 (autorisatie + brute-force) + tests.   |
| 4      | 60 % LINE + 50 % BRANCH | 90 % / 80 %  | Na implementatie SR-08 / SR-09 (audit logging in CRUD-methoden).      |

Elke verhoging gaat samen met de bijbehorende backlog-item, zodat de drempel altijd is afgedwongen door écht geschreven tests, niet door een papieren gate.

---

## 6. Mapping naar NEN-7510:2024-2

| NEN-7510:2024-2 control | Hoe gedekt door dit mechanisme                                                 |
| ----------------------- | ------------------------------------------------------------------------------ |
| **A.8.25** Secure development life cycle | Kwantitatief inzicht in test-discipline per build.                  |
| **A.8.28** Beveiligd coderen             | Coverage afgedwongen op security-kritieke package (audit).          |
| **A.8.29** Beveiligingstesten            | Aantoonbaar bewijs dat security-controls daadwerkelijk geraakt worden door tests; artifact in CI. |

---

## 7. Lokaal draaien

```bash
cd openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling
mvn -pl api -am verify
# Rapport openen
open api/target/site/jacoco/index.html   # macOS
xdg-open api/target/site/jacoco/index.html  # Linux
start api\target\site\jacoco\index.html  # Windows PowerShell
```
