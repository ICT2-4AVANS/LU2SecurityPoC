# Risk Assessment Report – OpenMRS Appointment Scheduling Module

| | |
| ----------- | ----------------------------------------------------------- |
| **Norm**    | NEN-7510:2024-2                                             |
| **Module**  | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT        |
| **Datum**   | 2026-06-10                                                  |
| **Auteur**  | Security PoC – LU2                                          |
| **Status**  | Concept                                                     |

---

## 1. Inleiding en scope

Dit Risk Assessment Report bundelt de uitkomsten van:

- **CIA/BIV-analyse** – [`docs/CIA/CIA-analyse.md`](../CIA/CIA-analyse.md)
- **Threat model** – [`docs/threadmodel/Threat-model.md`](../threadmodel/Threat-model.md)
- **Gap-analyse NEN-7510 (algemeen)** – [`docs/auditreport/01-gap-analyse.md`](../auditreport/01-gap-analyse.md)
- **Gap-analyse logging (A.8.15)** – [`docs/auditreport/02gapanalyselogging.md`](../auditreport/02gapanalyselogging.md)
- **Pipeline-compliance** – [`docs/auditreport/02-pipeline-compliance.md`](../auditreport/02-pipeline-compliance.md)
- **Pentest-bevindingen** – [`docs/auditreport/05-pentest-bevindingen.md`](../auditreport/05-pentest-bevindingen.md)
- **Security backlog** – [`docs/security-backlog/security-backlog.md`](./security-backlog.md)

Doel: één geïntegreerd risicobeeld, gekoppeld aan **NEN-7510:2024-2** controls, met een onderbouwde **kostenraming** voor de remediatie.

Gehanteerde scoringschaal: **Risico = Kans × Impact** met schaal 1–5 (zie CIA-analyse § 4–5).

---

## 2. Verwerkte gevoelige gegevens (met referenties)

### 2.1 Datacategorieën

| # | Datacategorie         | Voorbeelden (velden / klassen)                                                         | AVG-classificatie                | Codebewijs                                                                                                       | CIA-impact                      |
| - | --------------------- | -------------------------------------------------------------------------------------- | -------------------------------- | ---------------------------------------------------------------------------------------------------------------- | ------------------------------- |
| 1 | Direct identificerende patiëntgegevens | `Patient.personName` (voor- en achternaam), `birthdate`, `gender`, `patientIdentifier` | Persoonsgegeven (AVG art. 4)     | `AppointmentServiceImpl.java` regel ~1427 (gelekte velden); CIA-analyse § 3 kroonjuweel "Patiëntkoppeling"        | Vertrouwelijkheid               |
| 2 | Patiëntafspraken      | `Appointment` (patient, timeSlot, status, reason, type, comment)                       | Gezondheidsgegeven (AVG art. 9 — bijzondere persoonsgegevens) | Domeinklasse `Appointment`; CIA-analyse § 3 kroonjuweel "Patiëntafspraken"                                       | Vertrouwelijkheid + Integriteit |
| 3 | Afspraaktypen         | `AppointmentType` (bv. "HIV consult", "Oncologie")                                     | Gezondheidsgegeven (indirect)    | CIA-analyse § 3 kroonjuweel "Afspraaktypes"                                                                       | Vertrouwelijkheid               |
| 4 | Zorgverlenerroosters  | `ProviderSchedule`, `Provider`                                                         | Persoonsgegeven medewerker       | CIA-analyse § 3 kroonjuweel "Zorgverlenerroosters"                                                                | Integriteit + Beschikbaarheid   |
| 5 | Tijd- en planningsdata | `TimeSlot`, `AppointmentBlock`                                                         | Operationele data                | CIA-analyse § 3 kroonjuweel "Tijdsloten"                                                                          | Integriteit + Beschikbaarheid   |
| 6 | Locatiegegevens       | `Location` (waar de afspraak plaatsvindt)                                              | Operationele data                | CIA-analyse § 3 kroonjuweel "Locatiegegevens"                                                                     | Integriteit                     |
| 7 | Afspraakstatussen     | `AppointmentStatusHistory` (Scheduled, Completed, Missed, Cancelled)                   | Gezondheidsgegeven (indirect)    | CIA-analyse § 3 kroonjuweel "Afspraakstatussen"                                                                   | Integriteit                     |
| 8 | Authenticatie-gerelateerde data | OpenMRS `User`, HL7-koppelingsaccount                                                   | Credentials                      | `AppointmentActivator.java` regel 78–82 (hardcoded HL7 credentials, gap-analyse § A.8.5)                          | Vertrouwelijkheid               |

### 2.2 Datastromen

| Bron                  | Doel                  | Kanaal              | Gevoeligheid | Referentie                                          |
| --------------------- | --------------------- | ------------------- | ------------ | --------------------------------------------------- |
| Browser zorgmedewerker | OpenMRS Web UI        | HTTP(S) + cookies   | Credentials, PII | Threat model § 5 (Level 1 DFD)                      |
| Web UI / REST         | Appointment Scheduling API | In-process        | Patiëntdata  | Threat model § 5                                    |
| API                   | OpenMRS Database      | JDBC                | Patiëntdata  | Threat model § 5 + gap-analyse § A.8.3              |
| Module                | Applicatielog (Log4j2) | File appender      | PII (huidige bug) | `AppointmentServiceImpl.java:1427`, gap-A.8.15      |
| Module → HL7 exportserver | HL7 reporting server | JDBC (hardcoded)   | Patiëntdata + credentials | `AppointmentActivator.java:78–82`                  |

---

## 3. Geïdentificeerde kwetsbaarheden (uit scans + analyses)

| ID    | Categorie / vulnerability                                | Bron                                       | Bewijs                                                            | Status         |
| ----- | -------------------------------------------------------- | ------------------------------------------ | ----------------------------------------------------------------- | -------------- |
| V-01  | PII in audit/application log                             | Pentest B-01 + gap-A.8.15                  | `AppointmentServiceImpl.java:1422–1432`                            | ⚠️ Bevestigd   |
| V-02  | SQL Injection in DAO                                     | Gap-analyse A.8.3                           | `HibernateAppointmentDAO.java:315–319` (HQL-concatenatie)         | ⚠️ Bevestigd   |
| V-03  | Hardcoded credentials in broncode                        | Gap-analyse A.8.5                           | `AppointmentActivator.java:78–82` (HL7-credentials + JDBC URL)    | ⚠️ Bevestigd   |
| V-04  | Geen audit-logging op CRUD-events                        | Gap-A.8.15 § 5.1                            | 23 mutatie-methoden zonder log                                    | ⚠️ Bevestigd   |
| V-05  | Geen brute-force-bescherming op login                    | Pentest B-02 + Threat T1                    | Default OpenMRS lockout-policy niet actief                        | 🔄 Te verifiëren |
| V-06  | IDOR op afspraken                                        | Pentest B-03 + Threat T1                    | `appointmentId` direct in URL                                     | 🔄 Te verifiëren |
| V-07  | Privilege escalation via directe URL                     | Pentest B-04 + Threat T1                    | Mogelijk ontbreken `@Authorized` op web-controllers               | 🔄 Te verifiëren |
| V-08  | Sessiekaping / CSRF / ontbrekende secure cookies         | Threat T4                                   | Geen documentatie van cookie-flags, geen CSRF-token in DWR        | ⚠️ Bevestigd (configgap) |
| V-09  | Zwakke credential transit / TLS-config                   | Threat T8                                   | Geen afgedwongen HTTPS / HSTS in `docker-compose.prod.yml`        | ⚠️ Bevestigd (configgap) |
| V-10  | Overbelasting API/database (geen rate limit, geen paging) | Threat T6                                   | Geen rate-limit-configuratie; bulk-methoden zonder paging         | ⚠️ Bevestigd   |
| V-11  | Geen herstelvermogen (geen healthcheck, geen exception handling-strategie) | Threat T3                  | Geen `/health`-endpoint in module                                 | ⚠️ Bevestigd   |
| V-12  | Mogelijke PII-lek via debug-logging                      | Gap-A.8.15 § 3.2                            | `AppointmentDataSetEvaluator.java:72–95` (debug van parameters)   | ⚠️ Bevestigd   |
| V-13  | Geen logging van `@Authorized`-afwijzingen / DWR-anoniem | Gap-A.8.15 § 5.3                            | Silent-return in `DWRAppointmentService.java:66/88/138`           | ⚠️ Bevestigd   |
| V-14  | Geen logging op scheduled tasks                          | Gap-A.8.15 § 5.4                            | `CleanOpenAppointmentsTask` zonder logregel                       | ⚠️ Bevestigd   |
| V-15  | Geen logbeveiliging (integriteit, retentie, NTP, encryptie) | Gap-A.8.15 § 5.5                          | Niet gedocumenteerd                                               | ⚠️ Bevestigd   |
| V-16  | Geen monitoring / SIEM / alerting                        | Gap-A.8.15 § 5.6                            | Niet ingericht                                                    | ⚠️ Bevestigd   |

---

## 4. Risicobeoordeling per vulnerability

Scoring volgens CIA-analyse (Kans × Impact, schaal 1–5).

| ID    | Vulnerability                                  | Kans | Impact | Score | Niveau      | Backlog-item |
| ----- | ---------------------------------------------- | ---: | -----: | ----: | ----------- | ------------ |
| V-01  | PII in log                                     |    4 |      5 |   20  | **Kritiek** | SR-01        |
| V-03  | Hardcoded credentials                          |    5 |      4 |   20  | **Kritiek** | (nieuw) SR-11 |
| V-02  | SQL Injection                                  |    3 |      5 |   15  | Hoog        | SR-02        |
| V-06  | IDOR                                           |    3 |      5 |   15  | Hoog        | SR-03        |
| V-07  | Privilege escalation via URL                   |    3 |      5 |   15  | Hoog        | SR-03        |
| V-05  | Geen brute-force-bescherming                   |    3 |      5 |   15  | Hoog        | SR-04        |
| V-10  | Overbelasting API/database                     |    3 |      4 |   12  | Hoog        | SR-05        |
| V-08  | Sessiekaping / CSRF / cookies                  |    2 |      5 |   10  | Hoog        | SR-06        |
| V-09  | Zwakke credential transit                      |    2 |      5 |   10  | Hoog        | SR-07        |
| V-04  | Geen audit-logging op CRUD                     |    3 |      3 |    9  | Middel      | SR-08, SR-09 |
| V-13  | Geen logging autorisatiefouten                 |    3 |      3 |    9  | Middel      | SR-09        |
| V-12  | PII-lek via debug-logging                      |    2 |      4 |    8  | Middel      | (nieuw) SR-12 |
| V-11  | Geen herstelvermogen                           |    2 |      4 |    8  | Middel      | SR-10        |
| V-14  | Geen logging scheduled tasks                   |    2 |      3 |    6  | Middel      | SR-08        |
| V-15  | Geen logbeveiliging gedocumenteerd             |    2 |      3 |    6  | Middel      | (nieuw) SR-13 |
| V-16  | Geen monitoring/SIEM                           |    2 |      3 |    6  | Middel      | (nieuw) SR-14 |

> **Toevoegingen t.o.v. backlog:** V-03, V-12, V-15 en V-16 worden opgenomen als nieuwe items SR-11 t/m SR-14 in een volgende update van de backlog. V-03 is een **Kritiek** risico (score 20) en moet naast SR-01 direct worden opgepakt.

---

## 5. Mitigatie per vulnerability + NEN-7510:2024-2 maatregel

| ID    | Vulnerability                          | Mitigatie (technisch)                                                                                                   | NEN-7510:2024-2 control                                              |
| ----- | -------------------------------------- | ----------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------- |
| V-01  | PII in log                             | Verwijder `getAppointmentsForPatientWithLogging` of strip PII; log alleen `patientId`. Voeg regex-CI-check op PII toe.   | **A.8.15** Logging en monitoring                                     |
| V-02  | SQL Injection                          | Vervang HQL-concatenatie door **parameter binding**; SAST (CodeQL, Snyk) blokkeert merge bij Hoog/Kritiek.               | **A.8.28** Beveiligd coderen; **A.8.29** Beveiligingstesten          |
| V-03  | Hardcoded credentials                  | Verwijder credentials uit code; gebruik OpenMRS Global Properties of secret-store; **git-historie schonen** (BFG/`filter-repo`); roteer wachtwoord. | **A.8.5** Authenticatie; **A.8.24** Cryptografie / sleutelbeheer     |
| V-04  | Geen audit-logging op CRUD             | Voeg AOP-aspect of explicit `audit(...)`-call toe in `saveAppointment`, `voidAppointment`, `purgeAppointment`, etc.; gebruik evt. `openmrs-module-auditlog`. | **A.8.15** Logging en monitoring                                     |
| V-05  | Geen brute-force-bescherming           | Activeer `security.loginAttemptsAllowed ≤ 5`; voeg responsvertraging / account-lockout toe; MFA voor beheerders.        | **A.8.5** Authenticatie                                              |
| V-06  | IDOR                                   | Eigenaarcontrole in servicelaag (`appointment.getPatient().equals(currentUser.getPerson())` of via privilege).            | **A.8.3** Toegangsbeveiliging                                        |
| V-07  | Privilege escalation via URL           | `@Authorized(...)` op alle web-/REST-controllers; defense-in-depth ook op DAO.                                          | **A.8.3** Toegangsbeveiliging                                        |
| V-08  | Sessiekaping / CSRF                    | Cookies: `HttpOnly`, `Secure`, `SameSite=Strict`; session-ID-rotatie bij login; CSRF-token; `X-Frame-Options`, `CSP`.   | **A.8.5** Authenticatie; **A.8.23** Webfiltering / browserbeveiliging |
| V-09  | Zwakke credential transit              | HTTPS afdwingen (HTTP → 301); **HSTS** ≥ 6 mnd; TLS 1.2+; testssl.sh-scan in CI.                                          | **A.8.24** Cryptografie; **A.5.14** Beveiliging informatieoverdracht |
| V-10  | Overbelasting API/database             | Rate limiting per IP/sessie; verplichte server-side paginatie; DB-indexen op `start_date`, `provider_id`, `time_slot`; load test. | **A.8.6** Capaciteitsbeheer                                          |
| V-11  | Geen herstelvermogen                   | Health-endpoint (`/health`), centrale exception-handler met nette foutmelding (geen stack trace), back-up + recovery-procedure. | **A.8.14** Redundantie van informatieverwerking; **A.5.30** ICT-gereedheid voor bedrijfscontinuïteit |
| V-12  | PII-lek via debug-logging              | Pas `AppointmentDataSetEvaluator`-debug aan zodat parameter-mappings nooit PII bevatten; default log-level `INFO` in productie. | **A.8.15** Logging en monitoring                                     |
| V-13  | Geen logging autorisatiefouten         | AOP-interceptor logt `@Authorized`-afwijzingen; DWR logt `Context.isAuthenticated() == false` als WARN.                 | **A.8.15** Logging en monitoring; **A.8.16** Monitoring activiteiten |
| V-14  | Geen logging scheduled tasks           | `CleanOpenAppointmentsTask` logt start/eind, aantal records geraakt, status.                                            | **A.8.15** Logging en monitoring                                     |
| V-15  | Geen logbeveiliging                    | Append-only/WORM-opslag, toegang beperkt tot security-officers, retentiebeleid ≥ 12 mnd, NTP, encryptie at rest.        | **A.8.15** Logging en monitoring; **A.5.33** Bescherming van records |
| V-16  | Geen monitoring/SIEM                   | Log-forwarding naar SIEM (ELK/Splunk); alerting op auth-failures, bulk-reads, SQL-fouten; periodieke logreview.         | **A.8.16** Monitoringactiviteiten; **A.5.25** Beoordeling van beveiligingsgebeurtenissen |

---

## 6. Kostenraming remediatie

### 6.1 Aannames & uurtarieven (incl. omgevingen / overhead)

| Rol                          | Uurtarief (€) | Toelichting                          |
| ---------------------------- | ------------: | ------------------------------------ |
| Security architect           |           130 | Ontwerp, NEN-7510-mapping, review    |
| Medior Java-developer        |            85 | Implementatie binnen module          |
| Junior Java-developer        |            60 | Aanvullende refactoring / tests      |
| DevOps / SRE                 |            95 | Pipeline, TLS, secrets, SIEM-koppeling |
| Pentester (extern)           |           120 | Verificatietest na fix               |
| Projectmanager / scrum master |           100 | Coördinatie (5% van directe uren)    |

Inschatting is **±25%**. Eenmalige kosten; geen jaarlijkse licenties meegerekend tenzij vermeld.

### 6.2 Inschatting per requirement / vulnerability

| ID  | Requirement / vulnerability                            | Rol(len)                  | Uren | Subtotaal (€) | Doorlooptijd |
| --- | ------------------------------------------------------ | ------------------------- | ---: | ------------: | ------------ |
| SR-01 / V-01 | PII uit logs verwijderen + regex-CI-check                | Medior dev + Sec architect | 8 + 2 | **940**         | 1 dag        |
| SR-11 / V-03 | Hardcoded credentials verwijderen + git-historie + roteren | Medior dev + DevOps + Sec arch. | 8 + 8 + 4 | **1.960**       | 2 dagen      |
| SR-02 / V-02 | SQL Injection fixen (parameter binding) + SAST-config    | Medior dev + DevOps        | 12 + 4 | **1.400**       | 2 dagen      |
| SR-03 / V-06+V-07 | IDOR + URL-autorisatie afdekken                      | Medior dev + Sec architect | 20 + 6 | **2.480**       | 3–4 dagen    |
| SR-04 / V-05 | Brute force + MFA-toggle activeren / documenteren        | Medior dev + DevOps        | 8 + 8 | **1.440**       | 2 dagen      |
| SR-05 / V-10 | Rate limiting, paginatie, DB-indexen, load test          | Medior dev + DevOps        | 24 + 16 | **3.560**       | 1 week       |
| SR-06 / V-08 | Sessie/CSRF/cookies/CSP harden                           | Medior dev + DevOps        | 16 + 8 | **2.120**       | 3 dagen      |
| SR-07 / V-09 | HTTPS/HSTS afdwingen + TLS-scan                          | DevOps                    | 12   | **1.140**       | 2 dagen      |
| SR-08 + SR-09 / V-04+V-14 | Audit-logging CRUD + web UI + tasks (AOP/auditlog) | Medior dev + Sec architect | 40 + 8 | **4.440**       | 1,5 week     |
| SR-12 / V-12 | Debug-logging schoonmaken                                | Junior dev                | 6    | **360**         | 0,5 dag      |
| SR-13 / V-15 | Logbeveiliging: WORM/retentie/NTP/encryptie + beleidsdoc | DevOps + Sec architect    | 16 + 8 | **2.560**       | 3–4 dagen    |
| SR-14 / V-16 | SIEM-forwarding + alerting + logreview-procedure         | DevOps + Sec architect    | 24 + 8 | **3.320**       | 1 week       |
| SR-10 / V-11 | Healthcheck, exception-handler, back-up/recovery-doc     | Medior dev + DevOps       | 12 + 8 | **1.780**       | 2–3 dagen    |
| **Verificatie-pentest (B-02/03/04 + regressie)** | Extern pentester            |                            | 24    | **2.880**       | 3 dagen      |
| **Subtotaal directe uren**                       |                            |                            |     | **30.380**       |              |
| Projectmanagement (5%)                            | PM                         |                            | ≈ 15  | **1.500**        | doorlopend   |
| Onvoorzien (15%)                                  |                            |                            |     | **4.780**        |              |
| **TOTAAL geschatte kosten remediatie**            |                            |                            |     | **≈ € 36.660**   |              |

### 6.3 Doorlooptijd

Bij **1 medior dev (full-time) + 0,5 FTE DevOps + 0,2 FTE security architect** is de totale doorlooptijd **8–10 weken** (≈ 2 sprints van 3 weken voor P0/P1 + 1 sprint voor P2 + pentest-verificatie).

| Sprint | Focus                                                            | Inhoud                                                  |
| ------ | ---------------------------------------------------------------- | ------------------------------------------------------- |
| Sprint 1 (week 1–3) | **P0 Kritiek**: V-01, V-03 + start SQLi (V-02)        | SR-01, SR-11, SR-02                                     |
| Sprint 2 (week 4–6) | **P1 Hoog**: AuthN/AuthZ + transport + DoS            | SR-03, SR-04, SR-05, SR-06, SR-07                       |
| Sprint 3 (week 7–9) | **P2 Middel**: logging, monitoring, beschikbaarheid   | SR-08, SR-09, SR-10, SR-12, SR-13, SR-14                |
| Week 10            | **Verificatie-pentest** + restpunten                                |                                                         |

### 6.4 Recurring / jaarlijkse kosten (indicatief, buiten remediatiebudget)

| Item                                  | Kosten (€/jaar) | Toelichting                                     |
| ------------------------------------- | --------------: | ----------------------------------------------- |
| SIEM-licentie (ELK self-hosted / Splunk light) | 3.000 – 12.000 | Sterk afhankelijk van event-volume              |
| Snyk / Sonar-licentie                 | 2.500 – 6.000   | SAST/SCA op meer dan open-source-only           |
| Externe pentest (jaarlijks)           | 6.000 – 10.000  | Conform A.8.29 — herhaal min. 1×/jaar           |
| Security-officer (0,1 FTE) voor logreview | ~ 12.000        | Periodieke logreview-procedure (A.8.16)         |

---

## 7. Conclusie en aanbevelingen

1. **Twee Kritieke risico's** (V-01 PII-log en V-03 hardcoded credentials, beide score 20) moeten **direct** worden opgepakt — beide bevestigd in code en buiten de norm van NEN-7510 A.8.5 / A.8.15.
2. De **Hoge risico's** zijn voornamelijk gerelateerd aan **A.8.3 Toegangsbeveiliging** (SQLi, IDOR, URL-escalatie) en **A.8.5 Authenticatie** (brute force, sessies, transport). Alle zijn afdekbaar binnen 1–2 sprints.
3. De **logging- en monitoring-gap (A.8.15 / A.8.16)** is structureel en vereist zowel code- als procesveranderingen; deze loopt door tot Sprint 3 en heeft een doorlopende kostencomponent (SIEM, logreview).
4. **Totale eenmalige remediatie ≈ € 36.660** (±25%) met een **doorlooptijd van 8–10 weken**.
5. Aanbevolen om de security backlog (SR-01 t/m SR-14) als Definition of Done-trigger voor de auditrapportage te gebruiken en de **verificatie-pentest** als formele afsluiting van het remediatietraject.
