# Security Backlog – OpenMRS Appointment Scheduling Module

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

### 3.2 Pentest-bevindingen (gescoord volgens CIA-criteria)

> **Let op — definitieve nummering:** B-nummers volgen de actuele lijst in `05-pentest-bevindingen.md`. Een eerdere versie van dit document gebruikte een andere mapping (waar B-01 nog "PII in audit log" was). Die mapping is hier gecorrigeerd.

| ID    | Bevinding                                                   | Kans | Onderbouwing kans                                                                       | Impact | Onderbouwing impact (CIA)                                                                                       | Score | Niveau      |
| ----- | ----------------------------------------------------------- | ---: | --------------------------------------------------------------------------------------- | -----: | --------------------------------------------------------------------------------------------------------------- | ----: | ----------- |
| B-03  | IDOR op afspraken                                           |    4 | Triviaal te proberen (`appointmentId` in URL aanpassen)                                 |     5 | Vertrouwelijkheid + integriteit van patiëntafspraken; kroonjuweel "Patiëntafspraken" geraakt                    | **20** | **Kritiek** |
| B-04  | Privilege escalation via directe URL                        |    4 | Realistisch mogelijk wanneer `@Authorized` op alle 13 controllers ontbreekt             |     5 | Gewone gebruiker krijgt beheerrechten → volledige integriteit van afsprakenmodule geraakt                       | **20** | **Kritiek** |
| B-05  | PII gelogd in audit log                                     |    4 | Code-bewijs aanwezig; elke `getAppointmentsForPatientWithLogging`-aanroep produceert PII |     5 | Vier directe PII-velden naar logs → ernstige privacy-impact (AVG bijzondere persoonsgegevens)                   | **20** | **Kritiek** |
| B-02  | Hardcoded credentials in broncode                           |    3 | Plaintext wachtwoord aanwezig in `AppointmentActivator.java` en in volledige git-historie |   5 | Directe schending van NEN-7510 A.8.5; databasetoegang HL7-rapportagesysteem gecompromitteerd                     | **15** | **Hoog**    |
| B-07  | Ontbrekende CSRF-beveiliging en sessie-hardening            |    3 | DWR-laag heeft geen CSRF-token; cookie-flags ontbreken                                  |     5 | Sessiekaping → namens zorgmedewerker afspraken muteren                                                          | **15** | **Hoog**    |
| B-10  | Trust boundary violation — AppointmentBlockListController   |    3 | CodeQL Security Alert #1: input uit `HttpServletRequest` zonder validatie naar sessie    |    5 | Manipulatie van sessiestatus / injectie in interne laag                                                          | **15** | **Hoog**    |
| B-11  | Trust boundary violation — AppointmentBlockCalendarController |  3 | CodeQL Security Alert #2: zelfde patroon, structureel probleem                          |     5 | Idem B-10                                                                                                        | **15** | **Hoog**    |
| B-06  | Ontbrekende logging van auth-events                         |    3 | Gap-analyse: 0 `log.warn`/`log.error` in controllers en service                          |    4 | Forensisch verlies bij incident; misbruik niet reconstrueerbaar                                                   | **12** | **Hoog**    |
| B-08  | PII-lek via debug-logging in DataSetEvaluator               |    3 | Treedt op als loglevel per ongeluk op `DEBUG` staat in productie                         |    4 | Indirect PII-lek (rapportage-parameters) via logbestand                                                          | **12** | **Hoog**    |
| B-01  | SQL Injection in zoekfunctie                                |    2 | DAO-methode `searchAppointmentsByPatientName` is dode code, geen externe entry point     |    5 | Bij bereikbaarheid → volledige database-leak / -manipulatie                                                      | **10** | **Hoog**    |
| B-09  | XSS via eval-achtige DOM-functie (CodeQL)                   |    2 | CodeQL Note-level; 1 instantie in JS-laag; exploitatie vereist gebruikersinput in DOM-flow |   3 | Sessiediefstal / kwaadaardige scripts in browser zorgmedewerker                                                  | **6**  | **Middel**  |

---

## 4. Geprioriteerde Security Backlog

### P0 – Critical (score 16–25, direct oplossen)

---

#### SR-01 – Verwijder PII uit applicatie-/auditlogs

| Veld                  | Waarde                                                                 |
| --------------------- | ---------------------------------------------------------------------- |
| Bron                  | **B-05** + **B-08** (Pentest, ⚠️ Open, bevestigd via code review)        |
| Kans × Impact = Score | **4 × 5 = 20**                                                         |
| Niveau                | **Kritiek**                                                            |
| Prioriteit            | **P0 – Critical**                                                      |
| STRIDE                | Information Disclosure                                                 |
| OWASP                 | A09 – Security Logging and Monitoring Failures                         |
| NEN-7510              | A.8.15 – Logging en monitoring                                         |
| CIA/BIV-impact        | Vertrouwelijkheid (kroonjuweel "Patiëntkoppeling")                     |

**Requirement**
Als beheerder wil ik dat applicatielogs geen direct herleidbare patiëntgegevens bevatten, zodat de module voldoet aan NEN-7510 A.8.15 en de AVG-eis van dataminimalisatie.

**Acceptatiecriteria**

- Het log-statement in `AppointmentServiceImpl.getAppointmentsForPatientWithLogging` (regel ~1427) logt geen `personName`, `birthdate`, `patientIdentifier` of `gender` meer; alleen de interne `patientId`. — **(B-05)**
- Debug-statements in `AppointmentDataSetEvaluator.java` regels 72–75 bevatten geen indirect PII via parameter-mappings. Default loglevel productie = `INFO`. — **(B-08)**
- Grep over de module-codebase op `log\.(info|debug|warn|error).*(getPersonName|getBirthdate|getPatientIdentifier)` levert geen treffers meer op.
- Indien de methode dode code is, wordt deze in plaats van gepatcht **verwijderd**.
- Een testcase verifieert dat een audit-log-regel voor een afspraak geen plain-text PII bevat (regex-check op test-output).

---

### P1 – Must have (score 10–15, maatregel verplicht)

---

#### SR-02 – Bescherming tegen SQL Injection

| Veld                  | Waarde                                          |
| --------------------- | ----------------------------------------------- |
| Bron                  | **T7** (Threat model) + **B-01** (Pentest)      |
| Kans × Impact = Score | **2 × 5 = 10**                                  |
| Niveau                | **Hoog**                                        |
| Prioriteit            | **P1 – Must**                                   |
| STRIDE                | Tampering / Information Disclosure              |
| OWASP                 | A03 – Injection                                 |
| NEN-7510              | A.8.28 – Beveiligd coderen                      |
| CIA/BIV-impact        | Vertrouwelijkheid + Integriteit                 |

**Requirement**
Als beheerder wil ik dat alle database-queries veilig zijn tegen SQL Injection, zodat afspraak- en patiëntdata niet ongeoorloofd uitgelezen of gewijzigd kunnen worden.

**Acceptatiecriteria**

- Alle DAO-queries gebruiken **prepared statements / parameter binding** (Hibernate Criteria / HQL met parameters).
- Input vanuit REST-resources en webcontrollers wordt **gevalideerd** vóór doorgifte aan de servicelaag.
- **Statische analyse** (SonarQube / SpotBugs FSB) draait in CI en faalt op SQLi-findings.
- **sqlmap** op de REST-endpoints geeft geen exploiteerbare bevindingen.

---

#### SR-03 – Autorisatiecontrole op object- en URL-niveau (IDOR + privilege escalation)

| Veld                  | Waarde                                                                                                |
| --------------------- | ----------------------------------------------------------------------------------------------------- |
| Bron                  | **T2** (20) + **T3** (20) + **B-03** (20) + **B-04** (20) + **B-10** (15) + **B-11** (15)             |
| Kans × Impact = Score | **4 × 5 = 20** (hoogste van bronnen)                                                                  |
| Niveau                | **Kritiek**                                                                                           |
| Prioriteit            | **P0 – Critical**                                                                                     |
| STRIDE                | Spoofing / Elevation of Privilege                                                                     |
| OWASP                 | A01 – Broken Access Control                                                                           |
| NEN-7510              | A.8.3 – Toegangsbeveiliging                                                                           |
| CIA/BIV-impact        | Vertrouwelijkheid + Integriteit (kroonjuweel "Patiëntafspraken")                                      |

**Requirement**
Als zorgmedewerker wil ik dat alleen geautoriseerde gebruikers afspraken en beheerfuncties kunnen benaderen, zodat een aanvaller niet via een aangepaste URL of object-ID afspraken van een andere patiënt kan inzien of beheerfuncties kan uitvoeren.

**Acceptatiecriteria**

- Elke webcontroller-methode heeft een passende **`@Authorized(...)`-annotatie**.
- In de servicelaag (`AppointmentServiceImpl`) wordt vóór het retourneren van een `Appointment` gecontroleerd of de huidige gebruiker rechten heeft op die patiënt; zo niet → `APIAuthenticationException`.
- **TC-02 (B-03)**: aanpassen van `appointmentId` in de URL door een gewone gebruiker resulteert in HTTP 403/redirect.
- **TC-03 (B-04)**: directe URL-toegang tot `/openmrs/admin/` of `module/appointmentscheduling/appointmentType/list.form` als gewone gebruiker resulteert in HTTP 403/redirect.
- Input uit `HttpServletRequest` wordt in `AppointmentBlockListController` (regel 151) en `AppointmentBlockCalendarController` (regel 150) gevalideerd vóór doorgifte aan de sessie — CodeQL trust-boundary alerts gesloten. **(B-10 + B-11)**
- B-03, B-04, B-10 en B-11 in `05-pentest-bevindingen.md` krijgen status ✅ Opgelost.

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

**Requirement**
Als beheerder wil ik dat de loginpagina beschermd is tegen brute-force-aanvallen en dat alleen sterk geauthenticeerde gebruikers toegang krijgen tot de module.

**Acceptatiecriteria**

- OpenMRS Global Property `security.loginAttemptsAllowed` staat op een veilige waarde (≤ 5) en account lockout is actief.
- Wachtwoorden voldoen aan een wachtwoordbeleid (minimaal 12 tekens, complexiteit).
- Anonieme toegang tot module-endpoints wordt geweigerd (HTTP 401).
- **MFA** is beschikbaar voor accounts met beheerrechten.

---

#### SR-11 – Geen hardcoded credentials in broncode

| Veld                  | Waarde                                                  |
| --------------------- | ------------------------------------------------------- |
| Bron                  | **B-02** (Pentest, ⚠️ Open, bevestigd via code review)   |
| Kans × Impact = Score | **3 × 5 = 15**                                          |
| Niveau                | **Hoog**                                                |
| Prioriteit            | **P1 – Must**                                           |
| STRIDE                | Information Disclosure                                  |
| OWASP                 | A07 – Identification and Authentication Failures        |
| NEN-7510              | A.8.5 – Authenticatie + A.8.24 – Sleutelbeheer          |
| CIA/BIV-impact        | Vertrouwelijkheid                                       |

**Requirement**
Als beheerder wil ik dat geen enkele credential in de broncode of git-historie staat, zodat geen onbevoegde via repo-toegang databasecredentials kan bemachtigen.

**Acceptatiecriteria**

- `AppointmentActivator.java` bevat geen plaintext wachtwoord meer; credentials worden gelezen uit OpenMRS Global Properties of environment variables.
- Git-historie is geschoond (`git filter-repo` of BFG) zodat het wachtwoord `Appt@Export2021!` nergens meer voorkomt.
- Wachtwoord van het HL7-rapportagesysteem is **geroteerd**.
- Een SAST/secret-scan (Gitleaks of TruffleHog) in CI detecteert geen nieuwe hardcoded secrets.
- B-02 in `05-pentest-bevindingen.md` krijgt status ✅ Opgelost.

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

**Requirement**
Als beheerder wil ik dat de Appointment Scheduling API en de onderliggende database beschermd zijn tegen overbelasting, zodat afspraken altijd binnen aanvaardbare tijd gepland en bekeken kunnen worden.

**Acceptatiecriteria**

- Per sessie / IP geldt een **rate limit** op REST-endpoints, configureerbaar via OpenMRS global properties.
- Lijsten van afspraken zijn verplicht **gepagineerd** (max. page size afgedwongen server-side).
- Langlopende of zware queries hebben een **time-out** en gebruiken **database-indexen** op `appointment.start_date`, `appointment.provider_id` en `time_slot`.
- Een **load test** (JMeter / k6) toont aan dat de API onder verwachte piekbelasting beschikbaar blijft.

---

#### SR-06 – Beveiliging van browsersessies tegen kaping en misbruik

| Veld                  | Waarde                                                  |
| --------------------- | ------------------------------------------------------- |
| Bron                  | **T1** (Threat, 15) + **B-07** (Pentest, 15)            |
| Kans × Impact = Score | **3 × 5 = 15** (hoogste van bronnen)                    |
| Niveau                | **Hoog**                                                |
| Prioriteit            | **P1 – Must**                                           |
| STRIDE                | Spoofing                                                |
| OWASP                 | A07 – Identification and Authentication Failures        |
| NEN-7510              | A.8.5 – Authenticatie                                   |
| CIA/BIV-impact        | Vertrouwelijkheid                                       |

**Requirement**
Als gebruiker wil ik dat mijn sessie niet kan worden gekaapt of hergebruikt door derden, zodat onbevoegden niet via mijn browser bij afspraakgegevens kunnen komen.

**Acceptatiecriteria**

- Session cookies hebben de flags **`HttpOnly`**, **`Secure`** en **`SameSite=Strict`** (of minimaal `Lax`).
- Na **inactiviteitstime-out** (bv. 15 min.) wordt de sessie automatisch beëindigd.
- Bij login wordt het **session ID geregenereerd** (bescherming tegen session fixation).
- **CSRF-bescherming** via token per form / mutating REST call.
- Beveiligingsheaders: `Content-Security-Policy`, `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`.

---

#### SR-07 – Beveiligd transport van credentials en gegevens

| Veld                  | Waarde                                                  |
| --------------------- | ------------------------------------------------------- |
| Bron                  | **T1** (Threat model) – compenserend bij sessiekaping   |
| Kans × Impact = Score | **3 × 5 = 15**                                          |
| Niveau                | **Hoog**                                                |
| Prioriteit            | **P1 – Must**                                           |
| STRIDE                | Information Disclosure                                  |
| OWASP                 | A02 – Cryptographic Failures                            |
| NEN-7510              | A.8.24 – Gebruik van cryptografie                       |
| CIA/BIV-impact        | Vertrouwelijkheid                                       |

**Requirement**
Als gebruiker wil ik dat mijn inloggegevens en afspraakgegevens versleuteld over het netwerk worden verstuurd, zodat ze niet kunnen worden onderschept.

**Acceptatiecriteria**

- Alle verkeer naar OpenMRS verloopt via **HTTPS (TLS 1.2 of hoger)**; HTTP-verkeer wordt geredirect.
- **HSTS** geactiveerd met max-age ≥ 6 maanden.
- Wachtwoorden worden **nooit in URL of GET-parameters** verstuurd.
- Wachtwoorden in de database opgeslagen met een **sterke hash + salt** (bv. bcrypt/PBKDF2).
- Een **TLS-scan** (testssl.sh, SSL Labs) toont geen zwakke ciphers.

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

**Requirement**
Als gebruiker wil ik dat gebruikersinput nooit als JavaScript wordt uitgevoerd in mijn browser, zodat aanvallers geen scripts kunnen injecteren via afsprakennamen of andere invoer.

**Acceptatiecriteria**

- Eval-achtige DOM-functie (`innerHTML`, `eval`, `document.write`) in de JS-laag van de module is vervangen door veilige equivalenten (`textContent`, DOM-builder, sanitizer).
- CodeQL Note "Call to eval-like DOM function" is gesloten op `dev`.
- Een `Content-Security-Policy` header blokkeert inline scripts en `eval`.
- B-09 in `05-pentest-bevindingen.md` krijgt status ✅ Opgelost.

---

---

#### SR-08 – Audit logging van afspraakwijzigingen (zonder PII)

| Veld                  | Waarde                                                                       |
| --------------------- | ---------------------------------------------------------------------------- |
| Bron                  | **T5** (Threat, 12) + **B-06** (Pentest, 12)                                 |
| Kans × Impact = Score | **3 × 4 = 12** (hoogste van bronnen)                                         |
| Niveau                | **Hoog**                                                                     |
| Prioriteit            | **P1 – Must**                                                                |
| STRIDE                | Repudiation                                                                  |
| OWASP                 | A09 – Security Logging and Monitoring Failures                               |
| NEN-7510              | A.8.15 – Logging en monitoring                                               |
| CIA/BIV-impact        | Integriteit                                                                  |

**Requirement**
Als beheerder wil ik dat elke wijziging aan een afspraak herleidbaar is naar een gebruiker en moment, zodat ik kan verantwoorden wie wat heeft gedaan – zonder PII in de log zelf op te slaan (zie SR-01).

**Acceptatiecriteria**

- Elke create / update / cancel / delete op `Appointment` of `AppointmentStatusHistory` schrijft een record met **gebruiker-ID, tijdstip, actie, object-ID en oude/nieuwe waarden** (geen PII).
- Logs zijn **alleen-lezen** voor reguliere gebruikers.
- Logs zijn raadpleegbaar door beheerders.
- Bewaartermijn **minimaal 12 maanden** (configureerbaar).

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

**Requirement**
Als beheerder wil ik dat ook acties via de OpenMRS Web UI (niet alleen via de REST API) worden gelogd, zodat alle handelingen op afspraken controleerbaar zijn, ongeacht het kanaal.

**Acceptatiecriteria**

- Webcontrollers loggen **gebruiker-ID, tijdstip en actie** (gelinkt aan SR-08).
- Logs bevatten geen gevoelige gegevens (geen wachtwoorden, geen PII – zie SR-01).
- Logs worden centraal verzameld en zijn doorzoekbaar.
- Steekproef: voor 10 willekeurige UI-acties is in de logs traceerbaar welke gebruiker dit deed.

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

**Requirement**
Als zorgmedewerker wil ik dat de Appointment Scheduling Module beschikbaar blijft, en bij uitval snel herstelt, zodat ik patiëntafspraken altijd kan plannen en bekijken.

**Acceptatiecriteria**

- Onverwachte excepties worden afgevangen, gelogd en resulteren in een nette foutmelding.
- Een **health check / monitoring** signaleert problemen met de module of databaseverbinding.
- Er is een **back-up- en recovery-procedure** voor de OpenMRS Database; RTO en RPO zijn vastgelegd in de NFR's.
- Bij **deploy** wordt de module getest met een smoke test (CRUD op een afspraak werkt) vóór release.

---

## 5. Samenvatting backlog (gesorteerd op risicoscore)

| ID    | Requirement                                                | Bron(nen)                                  | Score | Niveau      | Prio  |
| ----- | ---------------------------------------------------------- | ------------------------------------------ | ----: | ----------- | ----- |
| SR-03 | Autorisatiecontrole (IDOR + privesc + trust boundary)      | **T2 + T3 + B-03 + B-04 + B-10 + B-11**   | **20** | **Kritiek** | **P0** |
| SR-01 | Verwijder PII uit applicatie-/auditlogs                    | **B-05 + B-08**                            | **20** | **Kritiek** | **P0** |
| SR-04 | Sterke authenticatie + brute-force bescherming             | T1                                         |    15 | Hoog        | P1    |
| SR-11 | Geen hardcoded credentials in broncode                     | B-02                                       |    15 | Hoog        | P1    |
| SR-06 | Sessiebeveiliging (CSRF, cookies, headers)                 | T1 + B-07                                  |    15 | Hoog        | P1    |
| SR-07 | Beveiligd transport (TLS, HSTS, password hashing)          | T1                                         |    15 | Hoog        | P1    |
| SR-08 | Audit logging van afspraakwijzigingen (zonder PII)         | T5 + B-06                                  |    12 | Hoog        | P1    |
| SR-09 | Audit logging van acties via de Web UI                     | T5                                         |    12 | Hoog        | P1    |
| SR-05 | Rate limiting & resource throttling                        | T6                                         |    12 | Hoog        | P1    |
| SR-10 | Beschikbaarheid en herstelvermogen                         | T6                                         |    12 | Hoog        | P1    |
| SR-02 | Bescherming tegen SQL Injection                            | T7 + B-01                                  |    10 | Hoog        | P1    |
| SR-12 | Bescherming tegen XSS in de webclient                      | B-09                                       |     6 | Middel      | P2    |

---

## 6. Definition of Done (per security requirement)

Een security requirement uit deze backlog is **done** wanneer:

1. De implementatie voldoet aan alle acceptatiecriteria.
2. Er een **(unit-, integratie- of pentest-)test** is die het gedrag aantoont.
3. De wijziging is **code-reviewed** met expliciete aandacht voor security.
4. Documentatie (CIA-analyse / threat model / pentestbevindingen / NFR) is bijgewerkt — pentestbevindingen krijgen status ✅ Opgelost.
5. Er geen nieuwe Hoog/Kritieke bevindingen zijn in de **statische analyse** of **dependency scan**.
