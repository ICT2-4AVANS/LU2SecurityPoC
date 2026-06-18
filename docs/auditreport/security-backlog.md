# Security Backlog – OpenMRS Appointment Scheduling Module

| | |
|---|---|
| **Norm** | NEN-7510:2024-2 |
| **Module** | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT |
| **Datum** | 2026-06-18 |
| **Status** | ✅ Afgerond — 8/11 opgelost, 2 geaccepteerd, 1 open (B-08 — gepland voor productie-oplevering) |

---

## 1. Inleiding

Dit document bevat de **geprioriteerde security requirements** (security backlog) voor de OpenMRS Appointment Scheduling Module. De backlog is samengesteld uit twee bronnen:

1. **Threat model** – [`docs/threadmodel/Threat-model.md`](../threadmodel/Threat-model.md) – 8 geselecteerde threats (T1–T8) op basis van STRIDE.
2. **Pentest-bevindingen** – [`docs/auditreport/05-pentest-bevindingen.md`](../auditreport/05-pentest-bevindingen.md) – bevindingen B-01 t/m B-11 (OWASP Testing Guide v4 + NEN-7510:2024-2 + CodeQL SAST + Dependabot security alerts).

De **prioritering** is volledig gebaseerd op de **risicocriteria uit de CIA/BIV-analyse** ([`docs/CIA/CIA-analyse.md`](../CIA/CIA-analyse.md), § 4 en § 5).

---

## 2. Gehanteerde risicocriteria (uit CIA-analyse)

```text
Risico = Kans × Impact
```

### Kansscore (1–5)

| Score | Kans      | Betekenis                              |
| ----: | --------- | -------------------------------------- |
|     1 | Zeer laag | Bijna onmogelijk                       |
|     2 | Laag      | Kan gebeuren, maar niet waarschijnlijk |
|     3 | Middel    | Realistisch mogelijk                   |
|     4 | Hoog      | Waarschijnlijk                         |
|     5 | Zeer hoog | Komt waarschijnlijk vaak voor          |

### Impactscore (1–5)

| Score | Impact    | Betekenis                                                            |
| ----: | --------- | -------------------------------------------------------------------- |
|     1 | Zeer laag | Nauwelijks effect                                                    |
|     2 | Laag      | Kleine verstoring                                                    |
|     3 | Middel    | Merkbare verstoring of beperkte datablootstelling                    |
|     4 | Hoog      | Gevoelige gegevens of belangrijk zorgproces geraakt                  |
|     5 | Zeer hoog | Ernstige privacy-impact, verkeerde zorgplanning of langdurige uitval |

### Risicoschaal & backlog-prioriteit

| Risicoscore | Niveau      | Backlog-prioriteit | Actie                                 |
| ----------: | ----------- | ------------------ | ------------------------------------- |
|         1–4 | Laag        | **P3 – Could**     | Accepteren                            |
|         5–9 | Middel      | **P2 – Should**    | Monitoren en waar mogelijk verbeteren |
|       10–15 | Hoog        | **P1 – Must**      | Maatregel verplicht                   |
|       16–25 | Kritiek     | **P0 – Critical**  | Direct oplossen of mitigeren          |

### Naast Kans × Impact: CVSS v3.1

De **pentest-bevindingen** zijn óók gescoord volgens **CVSS v3.1** (officiële NIST-calculator). CVSS meet de technische ernst van de kwetsbaarheid; Kans × Impact meet het organisatie-risico in onze context. Beide scores zijn opgenomen in `05-pentest-bevindingen.md` voor volledigheid.

---

## 3. Risicoscores per bron

### 3.1 Threat-model-threats (overgenomen uit Threat-model.md § 8–9, definitieve nummering)

| ID | Threat                                     | Kans | Impact | Score | Niveau  |
| -- | ------------------------------------------ | ---: | -----: | ----: | ------- |
| T1 | Account- of sessiemisbruik                 |    3 |      5 |    15 | Hoog    |
| T2 | Onbevoegde inzage in patiëntafspraken      |    4 |      5 |    20 | Kritiek |
| T3 | Onbevoegd wijzigen/annuleren van afspraken |    4 |      5 |    20 | Kritiek |
| T4 | Niet-admin wijzigt module-instellingen     |    3 |      5 |    15 | Hoog    |
| T5 | Onvoldoende audit trail                    |    3 |      4 |    12 | Hoog    |
| T6 | Overbelasting van API/database             |    3 |      4 |    12 | Hoog    |
| T7 | Injection via zoek/filterinput             |    2 |      5 |    10 | Hoog    |
| T8 | Kwetsbare dependency of module deployment  |    2 |      4 |     8 | Middel  |

### 3.2 Pentest-bevindingen (gescoord volgens CIA-criteria én CVSS)

| ID    | Bevinding                                                     | Kans | Impact | Score | Niveau  | CVSS | Status sprint 4 |
| ----- | ------------------------------------------------------------- | ---: | -----: | ----: | ------- | ---: | --- |
| B-03  | IDOR op afspraken                                             |    4 |     5 | **20** | Kritiek | 8.1 | ✅ Opgelost ([PR #74](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/74)) |
| B-04  | Privilege escalation via directe URL                          |    4 |     5 | **20** | Kritiek | 8.8 | ✅ Opgelost ([PR #52](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/52)) |
| B-05  | PII gelogd in audit log                                       |    4 |     5 | **20** | Kritiek | 7.5 | ✅ Opgelost ([PR #75](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/75)) |
| B-02  | Hardcoded credentials in broncode                             |    3 |     5 | **15** | Hoog    | 9.1 | ✅ Opgelost ([PR #51](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/51)) |
| B-07  | Ontbrekende CSRF-beveiliging en sessie-hardening              |    3 |     5 | **15** | Hoog    | 8.1 | ✅ Opgelost ([PR #55](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/55)) |
| B-10  | Trust boundary violation — AppointmentBlockListController     |    3 |     5 | **15** | Hoog    | 7.5 | ✅ Opgelost ([PR #57](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/57)) |
| B-11  | Trust boundary violation — AppointmentBlockCalendarController |    3 |     5 | **15** | Hoog    | 7.5 | ✅ Opgelost ([PR #57](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/57)) |
| B-06  | Ontbrekende logging van auth-events                           |    3 |     4 | **12** | Hoog    | 5.3 | ✅ Geaccepteerd — platformverantwoordelijkheid |
| B-08  | PII-lek via debug-logging in DataSetEvaluator                 |    3 |     4 | **12** | Hoog    | 5.3 | ⚠️ Open — gepland voor productie-oplevering |
| B-01  | SQL Injection in zoekfunctie                                  |    2 |     5 | **10** | Hoog    | 9.0 | ✅ Opgelost ([PR #49](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/49)) |
| B-09  | XSS via eval-achtige DOM-functie                              |    2 |     3 |  **6** | Middel  | 6.1 | ✅ Geaccepteerd — Medium, latent risico |

---

## 4. Geprioriteerde Security Backlog

### P0 – Critical (score 16–25, direct oplossen)

---

#### SR-01 – Verwijder PII uit applicatie-/auditlogs

| Veld                  | Waarde                                                                 |
| --------------------- | ---------------------------------------------------------------------- |
| Bron                  | **B-05** (Pentest, ✅ Opgelost PR #75) + **B-08** (⚠️ Open)              |
| Kans × Impact = Score | **4 × 5 = 20**                                                         |
| Niveau                | **Kritiek**                                                            |
| Prioriteit            | **P0 – Critical**                                                      |
| STRIDE                | Information Disclosure                                                 |
| OWASP                 | A09 – Security Logging and Monitoring Failures                         |
| NEN-7510              | A.8.15 – Logging en monitoring                                         |
| CIA/BIV-impact        | Vertrouwelijkheid (kroonjuweel "Patiëntkoppeling")                     |
| **Status sprint 4**   | ✅ B-05 opgelost; ⚠️ B-08 gepland voor volgende sprint                  |

**Acceptatiecriteria**

- Het log-statement in `AppointmentServiceImpl.getAppointmentsForPatientWithLogging` (regel ~1427) logt geen `personName`, `birthdate`, `patientIdentifier` of `gender` meer; alleen de interne `patientId`. — **(B-05 ✅)**
- Debug-statements in `AppointmentDataSetEvaluator.java` regels 72–75 bevatten geen indirect PII via parameter-mappings. Default loglevel productie = `INFO`. — **(B-08 ⚠️ open)**
- Een testcase verifieert dat een audit-log-regel voor een afspraak geen plain-text PII bevat.

---

#### SR-03 – Autorisatiecontrole op object- en URL-niveau (IDOR + privesc + trust boundary)

| Veld                  | Waarde                                                                                                |
| --------------------- | ----------------------------------------------------------------------------------------------------- |
| Bron                  | **T2 + T3 + B-03 + B-04 + B-10 + B-11**                                                                |
| Kans × Impact = Score | **4 × 5 = 20** (hoogste van bronnen)                                                                  |
| Niveau                | **Kritiek**                                                                                           |
| Prioriteit            | **P0 – Critical**                                                                                     |
| STRIDE                | Spoofing / Elevation of Privilege                                                                     |
| OWASP                 | A01 – Broken Access Control                                                                           |
| NEN-7510              | A.8.3 – Toegangsbeveiliging                                                                           |
| CIA/BIV-impact        | Vertrouwelijkheid + Integriteit (kroonjuweel "Patiëntafspraken")                                      |
| **Status sprint 4**   | ✅ Volledig opgelost (PR #74, PR #52, PR #57)                                                          |

**Acceptatiecriteria**

- Elke webcontroller-methode heeft een passende **`@Authorized(...)`-annotatie**. — **(B-04 ✅ PR #52)**
- In de servicelaag wordt vóór het retourneren van een `Appointment` gecontroleerd of de gebruiker rechten heeft op die patiënt. — **(B-03 ✅ PR #74)**
- Input uit `HttpServletRequest` wordt in `AppointmentBlockListController` (regel 151) en `AppointmentBlockCalendarController` (regel 150) gevalideerd. — **(B-10 + B-11 ✅ PR #57)**

---

### P1 – Must have (score 10–15, maatregel verplicht)

---

#### SR-11 – Geen hardcoded credentials in broncode

| Veld                  | Waarde                                                  |
| --------------------- | ------------------------------------------------------- |
| Bron                  | **B-02** (Pentest, ✅ Opgelost PR #51)                   |
| Kans × Impact = Score | **3 × 5 = 15**                                          |
| Niveau                | **Hoog**                                                |
| Prioriteit            | **P1 – Must**                                           |
| STRIDE                | Information Disclosure                                  |
| OWASP                 | A07 – Identification and Authentication Failures        |
| NEN-7510              | A.8.5 – Authenticatie + A.8.24 – Sleutelbeheer          |
| CIA/BIV-impact        | Vertrouwelijkheid                                       |
| **Status sprint 4**   | ✅ Opgelost (PR #51) — git-historieschoning openstaand   |

**Acceptatiecriteria**

- `AppointmentActivator.java` bevat geen plaintext wachtwoord meer; credentials worden gelezen uit OpenMRS Global Properties. **(✅ PR #51)**
- Git-historie geschoond zodat het wachtwoord `Appt@Export2021!` nergens meer voorkomt. **(⚠️ openstaand)**
- Wachtwoord van het HL7-rapportagesysteem is geroteerd. **(buiten scope module)**

---

#### SR-06 – Beveiliging van browsersessies tegen kaping en misbruik

| Veld                  | Waarde                                                  |
| --------------------- | ------------------------------------------------------- |
| Bron                  | **T1** (Threat, 15) + **B-07** (Pentest, ✅ PR #55)      |
| Kans × Impact = Score | **3 × 5 = 15**                                          |
| Niveau                | **Hoog**                                                |
| Prioriteit            | **P1 – Must**                                           |
| STRIDE                | Spoofing                                                |
| OWASP                 | A07 – Identification and Authentication Failures        |
| NEN-7510              | A.8.5 – Authenticatie                                   |
| CIA/BIV-impact        | Vertrouwelijkheid                                       |
| **Status sprint 4**   | ✅ Opgelost (PR #55 + PR #64)                            |

**Acceptatiecriteria**

- CSRF-bescherming actief in DWR-laag. **(✅ PR #55)**
- Session cookies hebben de flags `HttpOnly`, `Secure` en `SameSite=Strict` (of minimaal `Lax`).
- Bij login wordt het session ID geregenereerd.

---

#### SR-04 – Sterke authenticatie en brute-force bescherming

| Veld                  | Waarde                                                  |
| --------------------- | ------------------------------------------------------- |
| Bron                  | **T1** (Threat model)                                   |
| Kans × Impact = Score | **3 × 5 = 15**                                          |
| Niveau                | **Hoog**                                                |
| Prioriteit            | **P1 – Must**                                           |
| STRIDE                | Spoofing                                                |
| OWASP                 | A07 – Identification and Authentication Failures        |
| NEN-7510              | A.8.5 – Authenticatie                                   |
| CIA/BIV-impact        | Vertrouwelijkheid                                       |
| **Status sprint 4**   | ⏸️ Niet in scope — platformverantwoordelijkheid          |

---

#### SR-07 – Beveiligd transport van credentials en gegevens

| Veld                  | Waarde                                                  |
| --------------------- | ------------------------------------------------------- |
| Bron                  | **T1** (compenserend bij sessiekaping)                  |
| Kans × Impact = Score | **3 × 5 = 15**                                          |
| Niveau                | **Hoog**                                                |
| Prioriteit            | **P1 – Must**                                           |
| STRIDE                | Information Disclosure                                  |
| OWASP                 | A02 – Cryptographic Failures                            |
| NEN-7510              | A.8.24 – Gebruik van cryptografie                       |
| CIA/BIV-impact        | Vertrouwelijkheid                                       |
| **Status sprint 4**   | ⏸️ Niet in scope — deployment-/infraconfiguratie         |

---

#### SR-08 – Audit logging van afspraakwijzigingen (zonder PII)

| Veld                  | Waarde                                                                       |
| --------------------- | ---------------------------------------------------------------------------- |
| Bron                  | **T5** (Threat, 12) + **B-06** (Geaccepteerd)                                |
| Kans × Impact = Score | **3 × 4 = 12**                                                               |
| Niveau                | **Hoog**                                                                     |
| Prioriteit            | **P1 – Must**                                                                |
| STRIDE                | Repudiation                                                                  |
| OWASP                 | A09 – Security Logging and Monitoring Failures                               |
| NEN-7510              | A.8.15 – Logging en monitoring                                               |
| CIA/BIV-impact        | Integriteit                                                                  |
| **Status sprint 4**   | ⚠️ Gedeeltelijk — `AuditLogger.java` toegevoegd voor module-CRUD; login-events platformverantwoordelijkheid (B-06 geaccepteerd) |

**Acceptatiecriteria**

- Elke create / update / cancel / delete op `Appointment` schrijft een record met gebruiker-ID, tijdstip, actie, object-ID (geen PII). **(✅ `AuditLogger.java`)**
- Logs zijn alleen-lezen voor reguliere gebruikers.
- Bewaartermijn minimaal 12 maanden.

---

#### SR-09 – Audit logging van acties via de OpenMRS Web UI

| Veld                  | Waarde                                                  |
| --------------------- | ------------------------------------------------------- |
| Bron                  | **T5** (Threat model)                                   |
| Kans × Impact = Score | **3 × 4 = 12**                                          |
| Niveau                | **Hoog**                                                |
| Prioriteit            | **P1 – Must**                                           |
| STRIDE                | Repudiation                                             |
| OWASP                 | A09 – Security Logging and Monitoring Failures          |
| NEN-7510              | A.8.15 – Logging en monitoring                          |
| CIA/BIV-impact        | Integriteit                                             |
| **Status sprint 4**   | ⏸️ Niet in scope — gepland voor volgende sprint          |

---

#### SR-05 – Rate limiting & resource throttling op API en database

| Veld                  | Waarde                                          |
| --------------------- | ----------------------------------------------- |
| Bron                  | **T6** (Threat model)                           |
| Kans × Impact = Score | **3 × 4 = 12**                                  |
| Niveau                | **Hoog**                                        |
| Prioriteit            | **P1 – Must**                                   |
| STRIDE                | Denial of Service                               |
| OWASP                 | A04 – Insecure Design                           |
| NEN-7510              | A.8.6 – Capaciteitsbeheer                       |
| CIA/BIV-impact        | Beschikbaarheid                                 |
| **Status sprint 4**   | ⏸️ Niet in scope — deployment/infra              |

---

#### SR-10 – Beschikbaarheid en herstelvermogen van de module

| Veld                  | Waarde                                                |
| --------------------- | ----------------------------------------------------- |
| Bron                  | **T6** (Threat model)                                 |
| Kans × Impact = Score | **3 × 4 = 12**                                        |
| Niveau                | **Hoog**                                              |
| Prioriteit            | **P1 – Must**                                         |
| STRIDE                | Denial of Service                                     |
| OWASP                 | A04 – Insecure Design                                 |
| NEN-7510              | A.8.14 – Redundantie van informatieverwerking         |
| CIA/BIV-impact        | Beschikbaarheid                                       |
| **Status sprint 4**   | ⏸️ Niet in scope — deployment/infra                    |

---

#### SR-02 – Bescherming tegen SQL Injection

| Veld                  | Waarde                                          |
| --------------------- | ----------------------------------------------- |
| Bron                  | **T7** (Threat, 10) + **B-01** (✅ Opgelost PR #49) |
| Kans × Impact = Score | **2 × 5 = 10**                                  |
| Niveau                | **Hoog**                                        |
| Prioriteit            | **P1 – Must**                                   |
| STRIDE                | Tampering / Information Disclosure              |
| OWASP                 | A03 – Injection                                 |
| NEN-7510              | A.8.28 – Beveiligd coderen                      |
| CIA/BIV-impact        | Vertrouwelijkheid + Integriteit                 |
| **Status sprint 4**   | ✅ Opgelost (PR #49)                             |

**Acceptatiecriteria**

- Alle DAO-queries gebruiken prepared statements / parameter binding. **(✅ PR #49)**
- Statische analyse (CodeQL / Snyk) draait in CI en faalt op SQLi-findings.

---

### P2 – Should have (score 5–9, monitoren en verbeteren)

---

#### SR-12 – Bescherming tegen XSS in de webclient

| Veld                  | Waarde                                                  |
| --------------------- | ------------------------------------------------------- |
| Bron                  | **B-09** (Pentest / CodeQL SAST)                        |
| Kans × Impact = Score | **2 × 3 = 6**                                           |
| Niveau                | **Middel**                                              |
| Prioriteit            | **P2 – Should**                                         |
| STRIDE                | Tampering                                               |
| OWASP                 | A03 – Injection (XSS)                                   |
| NEN-7510              | A.8.28 – Beveiligd coderen                              |
| CIA/BIV-impact        | Vertrouwelijkheid + Integriteit                         |
| **Status sprint 4**   | ✅ Geaccepteerd — latent risico, scope volgende sprint   |

**Acceptatiecriteria**

- Eval-achtige DOM-functie in de JS-laag is vervangen door veilige equivalenten (`textContent`, sanitizer).
- Content-Security-Policy header blokkeert inline scripts en `eval`.
- CodeQL Note "Call to eval-like DOM function" is gesloten op `dev`.

---

## 5. Samenvatting backlog (gesorteerd op risicoscore)

| ID    | Requirement                                                | Bron(nen)                                  | Score | Niveau      | Prio  | Sprint 4 status |
| ----- | ---------------------------------------------------------- | ------------------------------------------ | ----: | ----------- | ----- | --- |
| SR-03 | Autorisatiecontrole (IDOR + privesc + trust boundary)      | T2 + T3 + B-03 + B-04 + B-10 + B-11        | **20** | **Kritiek** | **P0** | ✅ Opgelost (#74, #52, #57) |
| SR-01 | Verwijder PII uit applicatie-/auditlogs                    | B-05 + B-08                                | **20** | **Kritiek** | **P0** | ⚠️ B-05 ✅ (#75); B-08 open |
| SR-04 | Sterke authenticatie + brute-force bescherming             | T1                                         |    15 | Hoog        | P1    | ⏸️ Platformverantwoordelijkheid |
| SR-11 | Geen hardcoded credentials in broncode                     | B-02                                       |    15 | Hoog        | P1    | ✅ Opgelost (#51) |
| SR-06 | Sessiebeveiliging (CSRF, cookies, headers)                 | T1 + B-07                                  |    15 | Hoog        | P1    | ✅ Opgelost (#55, #64) |
| SR-07 | Beveiligd transport (TLS, HSTS, password hashing)          | T1                                         |    15 | Hoog        | P1    | ⏸️ Deployment-infra |
| SR-08 | Audit logging van afspraakwijzigingen (zonder PII)         | T5 + B-06                                  |    12 | Hoog        | P1    | ⚠️ AuditLogger toegevoegd; B-06 geaccepteerd |
| SR-09 | Audit logging van acties via de Web UI                     | T5                                         |    12 | Hoog        | P1    | ⏸️ Volgende sprint |
| SR-05 | Rate limiting & resource throttling                        | T6                                         |    12 | Hoog        | P1    | ⏸️ Deployment-infra |
| SR-10 | Beschikbaarheid en herstelvermogen                         | T6                                         |    12 | Hoog        | P1    | ⏸️ Deployment-infra |
| SR-02 | Bescherming tegen SQL Injection                            | T7 + B-01                                  |    10 | Hoog        | P1    | ✅ Opgelost (#49) |
| SR-12 | Bescherming tegen XSS in de webclient                      | B-09                                       |     6 | Middel      | P2    | ✅ Geaccepteerd |

---

## 6. Scope sprint 4 — verantwoording

Door beperkte sprint-capaciteit is gekozen om binnen sprint 4 **alleen de bevindingen met CVSS-ernst Critical (≥ 9.0) en High (≥ 7.0)** op te lossen. Dit is een standaard pentest-aanpak: prioriteer de bevindingen met direct exploiteerbare impact.

### Wat is opgelost (8 bevindingen)

| Bevinding | PR | Eigenaar |
|---|---|---|
| B-01 SQL Injection | [#49](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/49) | Nick |
| B-02 Hardcoded credentials | [#51](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/51) | Amine |
| B-03 IDOR | [#74](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/74) | Rami |
| B-04 Privilege escalation | [#52](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/52) | Nick |
| B-05 PII in logs | [#75](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/75) | Rami |
| B-07 CSRF / sessie-hardening | [#55](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/55) + [#64](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/64) | Amine |
| B-10 Trust boundary List | [#57](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/57) | Nick |
| B-11 Trust boundary Calendar | [#57](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/57) | Nick |

### Wat is bewust geaccepteerd (2 bevindingen)

| Bevinding | CVSS | Onderbouwing |
|---|---|---|
| B-06 Ontbrekende auth-logging | 5.3 Medium | Platformverantwoordelijkheid OpenMRS-core; module-laag kan login-events niet zelfstandig loggen. Compenserend: `AuditLogger.java` voor module-CRUD-events. |
| B-09 XSS eval-DOM | 6.1 Medium | CodeQL Note-level; 1 instantie; exploitatie vereist specifieke input-flow. Latent risico — gepland voor volgende sprint. |

### Wat is open (1 bevinding)

| Bevinding | CVSS | Onderbouwing |
|---|---|---|
| B-08 PII via debug-logging | 5.3 Medium | Latent — treedt alleen op bij DEBUG-loglevel in productie. Geplande mitigatie: default loglevel productie afdwingen op `INFO`. Gepland voor productie-oplevering. |

---

## 7. Definition of Done (per security requirement)

Een security requirement uit deze backlog is **done** wanneer:

1. De implementatie voldoet aan alle acceptatiecriteria.
2. Er een **(unit-, integratie- of pentest-)test** is die het gedrag aantoont.
3. De wijziging is **code-reviewed** met expliciete aandacht voor security.
4. Documentatie (CIA-analyse / threat model / pentestbevindingen / NFR) is bijgewerkt — pentestbevindingen krijgen status ✅ Opgelost.
5. Er geen nieuwe Hoog/Kritieke bevindingen zijn in de **statische analyse** of **dependency scan**.
