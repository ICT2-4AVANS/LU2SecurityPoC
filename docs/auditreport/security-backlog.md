# Security Backlog – OpenMRS Appointment Scheduling Module

## 1. Inleiding

Dit document bevat de **geprioriteerde security requirements** (security backlog) voor de OpenMRS Appointment Scheduling Module. De backlog is samengesteld uit twee bronnen:

1. **Threat model** – [`docs/threadmodel/Threat-model.md`](../threadmodel/Threat-model.md) – 8 geselecteerde threats (T1–T8) op basis van STRIDE.
2. **Pentest-bevindingen** – [`docs/auditreport/05-pentest-bevindingen.md`](../auditreport/05-pentest-bevindingen.md) – bevindingen B-01 t/m B-04 (OWASP Testing Guide v4 + NEN-7510:2024-2).

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

### 3.1 Threat-model-threats (overgenomen uit Threat-model.md § 7)

| ID | Threat                                                        | Kans | Impact | Score | Niveau |
| -- | ------------------------------------------------------------- | ---: | -----: | ----: | ------ |
| T1 | Spoofing zorgmedewerker / arts / beheerder                    |    2 |      5 |    10 | Hoog   |
| T2 | Wijzigingen aan afspraken zijn niet goed herleidbaar          |    3 |      3 |     9 | Middel |
| T3 | Appointment Scheduling Module crasht of stopt                 |    2 |      4 |     8 | Middel |
| T4 | Browser-/sessiemisbruik                                       |    2 |      5 |    10 | Hoog   |
| T5 | SQL Injection richting OpenMRS Database                       |    3 |      5 |    15 | Hoog   |
| T6 | API of database raakt overbelast                              |    3 |      4 |    12 | Hoog   |
| T7 | Acties via OpenMRS Web UI niet herleidbaar                    |    3 |      3 |     9 | Middel |
| T8 | Weak Credential Transit                                       |    2 |      5 |    10 | Hoog   |

### 3.2 Pentest-bevindingen (gescoord volgens CIA-criteria)

| ID    | Bevinding                                  | Kans | Onderbouwing kans                                                                     | Impact | Onderbouwing impact (CIA)                                                                                       | Score | Niveau      |
| ----- | ------------------------------------------ | ---: | ------------------------------------------------------------------------------------- | -----: | --------------------------------------------------------------------------------------------------------------- | ----: | ----------- |
| B-01  | PII in audit log (BEVESTIGD)               |    4 | Code-bewijs aanwezig; elke `getAppointmentsForPatientWithLogging`-aanroep produceert PII | 5      | Vier directe PII-velden naar logs → ernstige privacy-impact (AVG bijzondere persoonsgegevens)                   | **20** | **Kritiek** |
| B-02  | Geen brute-force-bescherming op login      |    3 | Realistisch mogelijk; OpenMRS lockout is niet standaard aan                          | 5      | Account-overname → toegang tot patiëntafspraken                                                                  | **15** | **Hoog**    |
| B-03  | IDOR op afspraken                          |    3 | Triviaal te proberen (ID in URL aanpassen)                                            | 5      | Vertrouwelijkheid + integriteit van patiëntafspraken; kroonjuweel "Patiëntafspraken" geraakt                    | **15** | **Hoog**    |
| B-04  | Privilege escalation via directe URL       |    3 | Realistisch mogelijk wanneer `@Authorized` op controllers ontbreekt                  | 5      | Gewone gebruiker krijgt beheerrechten → volledige integriteit van afsprakenmodule geraakt                       | **15** | **Hoog**    |

---

## 4. Geprioriteerde Security Backlog

### P0 – Critical (score 16–25, direct oplossen)

---

#### SR-01 – Verwijder PII uit applicatie-/auditlogs

| Veld                  | Waarde                                                                 |
| --------------------- | ---------------------------------------------------------------------- |
| Bron                  | **B-01** (Pentest, ⚠️ Open, bevestigd via code review)                  |
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

- Het log-statement in `AppointmentServiceImpl.getAppointmentsForPatientWithLogging` (regel ~1427) logt geen `personName`, `birthdate`, `patientIdentifier` of `gender` meer; alleen de interne `patientId`.
- Grep over de module-codebase op `log\.(info|debug|warn|error).*(getPersonName|getBirthdate|getPatientIdentifier)` levert geen treffers meer op.
- Indien de methode dode code is, wordt deze in plaats van gepatcht **verwijderd**.
- Een testcase verifieert dat een audit-log-regel voor een afspraak geen plain-text PII bevat (regex-check op test-output).

---

### P1 – Must have (score 10–15, maatregel verplicht)

---

#### SR-02 – Bescherming tegen SQL Injection

| Veld                  | Waarde                                          |
| --------------------- | ----------------------------------------------- |
| Bron                  | **T5** (Threat model)                           |
| Kans × Impact = Score | **3 × 5 = 15**                                  |
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
| Bron                  | **T1** (Threat, 10) + **B-03** (Pentest, 15) + **B-04** (Pentest, 15)                                  |
| Kans × Impact = Score | **3 × 5 = 15** (hoogste van bronnen)                                                                  |
| Niveau                | **Hoog**                                                                                              |
| Prioriteit            | **P1 – Must**                                                                                         |
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
- B-03 en B-04 in `05-pentest-bevindingen.md` krijgen status ✅ Opgelost.

---

#### SR-04 – Sterke authenticatie en brute-force bescherming

| Veld                  | Waarde                                                  |
| --------------------- | ------------------------------------------------------- |
| Bron                  | **T1** (Threat, 10) + **B-02** (Pentest, 15)            |
| Kans × Impact = Score | **3 × 5 = 15** (hoogste van bronnen)                    |
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
- **TC-01 (B-02)**: na 25 mislukte logins is het account geblokkeerd of het responstijdgedrag toont rate limiting; resultaat vastgelegd in B-02.

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
| Bron                  | **T4** (Threat model)                                   |
| Kans × Impact = Score | **2 × 5 = 10**                                          |
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
| Bron                  | **T8** (Threat model)                                   |
| Kans × Impact = Score | **2 × 5 = 10**                                          |
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

#### SR-08 – Audit logging van afspraakwijzigingen (zonder PII)

| Veld                  | Waarde                                                                       |
| --------------------- | ---------------------------------------------------------------------------- |
| Bron                  | **T2** (Threat model) – consistent met SR-01                                 |
| Kans × Impact = Score | **3 × 3 = 9**                                                                |
| Niveau                | **Middel**                                                                   |
| Prioriteit            | **P2 – Should**                                                              |
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
| Bron                  | **T7** (Threat model)                                   |
| Kans × Impact = Score | **3 × 3 = 9**                                           |
| Niveau                | **Middel**                                              |
| Prioriteit            | **P2 – Should**                                         |
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
| Bron                  | **T3** (Threat model)                                 |
| Kans × Impact = Score | **2 × 4 = 8**                                         |
| Niveau                | **Middel**                                            |
| Prioriteit            | **P2 – Should**                                       |
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

| ID    | Requirement                                                | Bron(nen)            | Score | Niveau      | Prio  |
| ----- | ---------------------------------------------------------- | -------------------- | ----: | ----------- | ----- |
| SR-01 | Verwijder PII uit applicatie-/auditlogs                    | B-01                 | **20** | **Kritiek** | **P0** |
| SR-02 | Bescherming tegen SQL Injection                            | T5                   | 15    | Hoog        | P1    |
| SR-03 | Autorisatiecontrole (IDOR + privilege escalation)          | T1 + B-03 + B-04     | 15    | Hoog        | P1    |
| SR-04 | Sterke authenticatie + brute-force bescherming             | T1 + B-02            | 15    | Hoog        | P1    |
| SR-05 | Rate limiting & resource throttling                        | T6                   | 12    | Hoog        | P1    |
| SR-06 | Sessiebeveiliging (CSRF, cookies, headers)                 | T4                   | 10    | Hoog        | P1    |
| SR-07 | Beveiligd transport (TLS, HSTS, password hashing)          | T8                   | 10    | Hoog        | P1    |
| SR-08 | Audit logging van afspraakwijzigingen (zonder PII)         | T2                   |  9    | Middel      | P2    |
| SR-09 | Audit logging van acties via de Web UI                     | T7                   |  9    | Middel      | P2    |
| SR-10 | Beschikbaarheid en herstelvermogen                         | T3                   |  8    | Middel      | P2    |

---

## 6. Definition of Done (per security requirement)

Een security requirement uit deze backlog is **done** wanneer:

1. De implementatie voldoet aan alle acceptatiecriteria.
2. Er een **(unit-, integratie- of pentest-)test** is die het gedrag aantoont.
3. De wijziging is **code-reviewed** met expliciete aandacht voor security.
4. Documentatie (CIA-analyse / threat model / pentestbevindingen / NFR) is bijgewerkt — pentestbevindingen krijgen status ✅ Opgelost.
5. Er geen nieuwe Hoog/Kritieke bevindingen zijn in de **statische analyse** of **dependency scan**.
