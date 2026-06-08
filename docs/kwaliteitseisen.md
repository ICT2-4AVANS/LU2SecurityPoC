# Kwaliteitseisen — Appointment Scheduling Module

| | |
|---|---|
| **Project** | LU2 Security PoC |
| **Module** | openmrs-module-appointmentscheduling v2.0.0 |
| **Datum** | 2026-06-08 |

---

## Inleiding

Dit document legt de meetbare kwaliteitseisen vast waaraan de Appointment Scheduling Module moet voldoen. De eisen zijn opgesteld op basis van NEN-7510:2024-2 en de opdracht van LU2. Elke eis is concreet en meetbaar zodat naleving aantoonbaar is.

---

## Security-eisen

### SE-01 — Geen nieuwe HIGH of CRITICAL kwetsbaarheden op main

| Attribuut | Waarde |
|---|---|
| **Eis** | Een pull request naar `main` mag geen nieuwe CodeQL-bevindingen introduceren met ernst `HIGH` of `CRITICAL` |
| **Meetmethode** | CodeQL-workflow slaagt zonder nieuwe `high`/`critical` alerts in de Security-tab |
| **Drempelwaarde** | 0 nieuwe HIGH/CRITICAL bevindingen |
| **Controle** | Automatisch bij elke PR via `.github/workflows/codeql.yml` |

---

### SE-02 — Geen directe afhankelijkheden met bekende HIGH/CRITICAL CVE

| Attribuut | Waarde |
|---|---|
| **Eis** | Directe afhankelijkheden mogen geen bekende kwetsbaarheden bevatten met CVSS-score ≥ 7.0 |
| **Meetmethode** | Dependency Review Action keurt een PR af bij `fail-on-severity: high` |
| **Drempelwaarde** | 0 niet-goedgekeurde HIGH/CRITICAL CVE's in directe dependencies |
| **Controle** | Automatisch bij elke PR via `.github/workflows/dependency-review.yml` |

---

### SE-03 — SBOM aanwezig en up-to-date

| Attribuut | Waarde |
|---|---|
| **Eis** | Bij elke push naar `main` wordt een actuele SBOM gegenereerd in CycloneDX JSON-formaat |
| **Meetmethode** | GitHub Actions artifact `sbom` aanwezig na elke run van de SBOM-workflow |
| **Drempelwaarde** | 100% — elke push naar main levert een downloadbaar SBOM-artifact op |
| **Controle** | Automatisch via `.github/workflows/SBOM.yml` |

---

### SE-04 — Audit-logging voor kritieke acties (A.8.15)

| Attribuut | Waarde |
|---|---|
| **Eis** | Aanmaken, wijzigen en annuleren van afspraken moeten worden gelogd met minimaal: wie, wat, wanneer |
| **Meetmethode** | Code review: controleer aanwezigheid van audit-logstatements in `AppointmentServiceImpl.java` bij alle `save*`, `cancel*` en `purge*` methoden |
| **Drempelwaarde** | 100% van de genoemde methoden bevat een audit-logstatement |
| **Controle** | Handmatig bij code review; in Sprint 3 aangevuld met geautomatiseerde tests |

---

### SE-05 — Toegangsbeveiliging op REST-laag (A.8.3)

| Attribuut | Waarde |
|---|---|
| **Eis** | Alle REST-resource-methoden die medische gegevens schrijven of lezen bevatten een privilege-check |
| **Meetmethode** | Code review: controleer aanwezigheid van `@Authorized`-annotaties op REST-resources |
| **Drempelwaarde** | 0 schrijvende of lezende REST-methoden zonder privilege-check |
| **Controle** | Handmatig bij code review; CodeQL-query als aanvullend bewijs |

---

## Onderhoudbaarheids-eisen

### ME-01 — Cyclomatische complexiteit per methode

| Attribuut | Waarde |
|---|---|
| **Eis** | Geen methode in gewijzigde bestanden mag een cyclomatische complexiteit hoger dan 15 hebben |
| **Meetmethode** | Statische analyse (SonarQube of gelijkwaardig tooling) |
| **Drempelwaarde** | Maximale cyclomatische complexiteit per methode: 15 |
| **Controle** | Handmatig bij code review in Sprint 2/3 |

---

### ME-02 — Code coverage van nieuwe code

| Attribuut | Waarde |
|---|---|
| **Eis** | Nieuwe of gewijzigde code heeft minimaal 70% line coverage |
| **Meetmethode** | JaCoCo coverage-rapport als CI-artifact |
| **Drempelwaarde** | ≥ 70% line coverage op gewijzigde bestanden |
| **Controle** | Automatisch via CI in Sprint 3 |

---

## Overzichtstabel

| ID | Categorie | Eis (samengevat) | Drempelwaarde | Controle |
|---|---|---|---|---|
| SE-01 | Security | Geen nieuwe HIGH/CRITICAL CodeQL-bevindingen bij PR | 0 | Automatisch (CodeQL) |
| SE-02 | Security | Geen HIGH/CRITICAL CVE in directe dependencies bij PR | 0 | Automatisch (Dependency Review) |
| SE-03 | Security | SBOM aanwezig na elke push naar main | 100% runs | Automatisch (SBOM workflow) |
| SE-04 | Security / A.8.15 | Audit-logging bij alle kritieke acties | 100% methoden | Handmatig / Sprint 3 tests |
| SE-05 | Security / A.8.3 | Privilege-check op alle schrijvende REST-methoden | 0 uitzonderingen | Handmatig / CodeQL |
| ME-01 | Onderhoudbaarheid | Cyclomatische complexiteit ≤ 15 per methode | Max 15 | Handmatig |
| ME-02 | Onderhoudbaarheid | ≥ 70% code coverage op nieuwe code | ≥ 70% | Automatisch (Sprint 3) |
