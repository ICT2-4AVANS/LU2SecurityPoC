# Gap-analyse — Logging (NEN-7510:2024-2 control 8.15)

| | |
|---|---|
| **Norm** | NEN-7510:2024-2 — control 8.15 (Logging and monitoring activities) |
| **Module** | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT |
| **Datum** | 2026-06-09 |
| **Auteur** | Enes |

---

## 1. Doelstelling van deze analyse

NEN-7510:2024-2 control **8.15** vereist dat informatieverwerkende systemen gebeurtenissen vastleggen die relevant zijn voor informatiebeveiliging én dat deze gebeurtenissen actief worden gemonitord. Voor een module die persoonsgegevens van patiënten verwerkt geldt dit in het bijzonder voor:

- **Toegang** tot gevoelige gegevens (wie heeft welke patiëntdata bekeken)
- **Mutaties** op gevoelige gegevens (aanmaak, wijziging, verwijdering)
- **Autorisatie-incidenten** (geweigerde toegang, escalatiepogingen)
- **Beheerhandelingen** (configuratiewijzigingen, modulebeheer, scheduled tasks)
- **Beveiliging van de logs zelf** (integriteit, toegang, retentie)
- **Monitoring** (actieve detectie en alerting op basis van logs)

Een logregel moet minimaal de volgende vijf elementen bevatten: **wie** (geauthenticeerde gebruiker), **wat** (handeling + identifier van het object), **waar** (systeem, component of IP-adres), **wanneer** (timestamp uit een betrouwbare tijdbron) en **hoe** (via welk kanaal en met welke uitkomst) — en mag **geen onnodige PII** bevatten. Daarnaast schrijft de norm voor dat **de logs zelf beveiligd zijn tegen ongeautoriseerde wijziging** en dat er een **retentie- en monitoringsbeleid** bestaat.

---

## 2. OpenMRS-platform context

Volgens de OpenMRS Wiki (*"Auditing Data Changes"*):

- Standaard OpenMRS heeft **geen ingebouwde audit-logging** voor data-wijzigingen
- Het platform gebruikt **Log4j2** voor algemene logging (lifecycle, errors, debug)
- Voor audit-logging is een **aparte module** beschikbaar: `openmrs-module-auditlog`, die automatisch create/update/delete-acties logt op gemarkeerde entiteiten
- Modules die patiëntdata wijzigen worden geacht óf `openmrs-module-auditlog` te gebruiken óf zelf audit-logging te implementeren

De Appointment Scheduling Module gebruikt **geen** van beide opties.

---

## 3. Inventarisatie — alle log-statements in de module

Een geautomatiseerde scan op alle `.java`-bestanden levert **16 log-statements** op, verdeeld over **8 bestanden**.

### 3.1 Module-lifecycle (info)

**`AppointmentActivator.java`** — 6 statements

| Regel | Statement | Type |
|---|---|---|
| 33 | `log.info("Refreshing Appointment Module");` | Lifecycle |
| 40 | `log.info("Appointment Module refreshed");` | Lifecycle |
| 47 | `log.info("Starting Appointment Module");` | Lifecycle |
| 54 | `log.info("Appointment Module started");` | Lifecycle |
| 67 | `log.info("Stopping Appointment Module");` | Lifecycle |
| 74 | `log.info("Appointment Module stopped");` | Lifecycle |

### 3.2 Debug-logging voor rapportage (debug)

**`AppointmentDataSetEvaluator.java`** — 5 statements

| Regel | Statement | Type |
|---|---|---|
| 72 | `log.debug("Evaluating column: " + cd.getName());` | Debug |
| 73 | `log.debug("With Data Definition: ...");` | Debug |
| 74 | `log.debug("With Mappings: ...");` | Debug |
| 75 | `log.debug("With Parameters: ...");` | Debug |
| 95 | `log.debug("Added column: " + sw.toString());` | Debug |

⚠️ **Risico:** als de Log4j2-configuratie debug aanzet in productie, kunnen de `Parameters` en `Mappings` indirect patiëntdata bevatten. Dit is een verborgen PII-risico in de bestaande logging.

### 3.3 Technische fouten in property editors (error)

| Bestand | Regel | Statement |
|---|---|---|
| `AppointmentBlockEditor.java` | 46 | `log.error("Error setting text: " + text, ex);` |
| `AppointmentEditor.java` | 46 | `log.error("Error setting text: " + text, ex);` |
| `AppointmentTypeEditor.java` | 46 | `log.error("Error setting text: " + text, ex);` |
| `ProviderEditor.java` | 54 | `log.error("Error setting provider with id or uuid: " + text, ex);` |
| `TimeSlotEditor.java` | 46 | `log.error("Error setting text: " + text, ex);` |

### 3.4 "Audit"-logregel (info — PII-lek)

**`AppointmentServiceImpl.java`** — 1 statement

| Regel | Statement |
|---|---|
| 1427 | `log.info("[AUDIT] Fetching appointments for patient: name=" + patient.getPersonName() + " dob=" + patient.getBirthdate() + ...);` |

Deze methode (`getAppointmentsForPatientWithLogging`) wordt **nergens aangeroepen** — dode code. Bovendien bevat de logregel **PII** (naam, geboortedatum, identifier, geslacht) in plain text en is daarmee geen valide audit-log maar een latent datalek.

---

## 4. Attack surface — wat is bereikbaar van buitenaf

| Laag | Voorbeelden van endpoints | Logging aanwezig? |
|---|---|---|
| **REST API** | `/ws/rest/v1/appointmentscheduling/appointment` (GET/POST/DELETE), `/appointmentblock`, `/timeslot`, `/appointmenttype`, `/appointmentrequest` | ❌ Geen audit-logging |
| **DWR (Direct Web Remoting)** | `DWRAppointmentService.getAppointmentBlocksForCalendar`, `getPatientDescription`, etc. | ❌ Geen audit-logging |
| **JSP-controllers** | Appointment-formulieren (`appointmentForm.form`, etc.) | ❌ Geen audit-logging |
| **Service-laag (intern)** | `AppointmentService.saveAppointment`, `voidAppointment`, `purgeAppointment`, ... | ❌ Geen audit-logging |
| **DAO-laag (intern)** | `HibernateAppointmentDAO.searchAppointmentsByPatientName` | ❌ Geen logging + SQL-injectie |
| **Scheduled tasks** | `CleanOpenAppointmentsTask` (automatische opruiming) | ❌ Geen logging |

---

## 5. Event-matrix — gelogd vs. gewenst

### 5.1 Mutaties op patiëntdata (CRUD)

| Event | Methode (file) | Gelogd? | Gevoelige data | NEN-7510 8.15 compliant? |
|---|---|---|---|---|
| Afspraak aanmaken | `saveAppointment` | ❌ Nee | Patiënt + tijdslot | ❌ Nee |
| Afspraak boeken | `bookAppointment` | ❌ Nee | Patiënt + tijdslot | ❌ Nee |
| Afspraak voiden | `voidAppointment` | ❌ Nee | Patiënt + reden | ❌ Nee |
| Afspraak unvoid | `unvoidAppointment` | ❌ Nee | Patiënt | ❌ Nee |
| Afspraak permanent verwijderen | `purgeAppointment` | ❌ Nee | Patiënt | ❌ Nee |
| Afspraakstatus wijzigen | `changeAppointmentStatus` | ❌ Nee | Patiënt + status | ❌ Nee |
| Afspraakblok aanmaken/wijzigen | `saveAppointmentBlock` | ❌ Nee | Provider + locatie | ❌ Nee |
| Afspraakblok voiden | `voidAppointmentBlock` | ❌ Nee | Provider | ❌ Nee |
| Afspraakblok permanent verwijderen | `purgeAppointmentBlock` | ❌ Nee | Provider | ❌ Nee |
| Tijdslot aanmaken/wijzigen | `saveTimeSlot` | ❌ Nee | Tijdsgegevens | ❌ Nee |
| Tijdslot via providerschema | `createTimeSlotUsingProviderSchedule` | ❌ Nee | Provider + tijdslot | ❌ Nee |
| Tijdslot voiden | `voidTimeSlot` | ❌ Nee | — | ❌ Nee |
| Tijdslot permanent verwijderen | `purgeTimeSlot` | ❌ Nee | — | ❌ Nee |
| Afspraaktype wijzigen | `saveAppointmentType` | ❌ Nee | Configuratie | ❌ Nee |
| Afspraaktype retireren | `retireAppointmentType` | ❌ Nee | Configuratie | ❌ Nee |
| Afspraaktype permanent verwijderen | `purgeAppointmentType` | ❌ Nee | Configuratie | ❌ Nee |
| Afspraakverzoek aanmaken/wijzigen | `saveAppointmentRequest` | ❌ Nee | Patiënt | ❌ Nee |
| Afspraakverzoek voiden | `voidAppointmentRequest` | ❌ Nee | Patiënt | ❌ Nee |
| Afspraakverzoek permanent verwijderen | `purgeAppointmentRequest` | ❌ Nee | Patiënt | ❌ Nee |
| Providerschema aanmaken/wijzigen | `saveProviderSchedule` | ❌ Nee | Provider | ❌ Nee |
| Providerschema voiden | `voidProviderSchedule` | ❌ Nee | Provider | ❌ Nee |
| Providerschema permanent verwijderen | `purgeProviderSchedule` | ❌ Nee | Provider | ❌ Nee |
| Statusgeschiedenis wijzigen | `AppointmentStatusHistory`-mutaties | ❌ Nee | Patiënt + audit-trail | ❌ Nee |

### 5.2 Toegang tot patiëntdata (read)

| Event | Methode | Gelogd? | Gevoelige data | NEN-7510 8.15 compliant? |
|---|---|---|---|---|
| Alle afspraken ophalen | `getAllAppointments` | ❌ Nee | Bulk patiëntdata | ❌ Nee |
| Afspraak ophalen op ID | `getAppointment` | ❌ Nee | Patiënt + tijdslot | ❌ Nee |
| Afspraak ophalen op UUID | `getAppointmentByUuid` | ❌ Nee | Patiënt + tijdslot | ❌ Nee |
| Laatste afspraak van patiënt | `getLastAppointment` | ❌ Nee | Patiënt + tijdslot | ❌ Nee |
| Afspraken per patiënt + PII-lek | `getAppointmentsForPatientWithLogging` | ⚠️ Wél, maar foutief | **PII in plain text** | ❌ Nee (datalek) |
| Patiëntinformatie via DWR | `DWRAppointmentService.getPatientDescription` | ❌ Nee | Naam, telefoon | ❌ Nee |
| Zoeken op patiëntnaam | `HibernateAppointmentDAO.searchAppointmentsByPatientName` | ❌ Nee | Bulk patiëntdata + **SQL-injectie** | ❌ Nee |
| Bulk-statistieken | `getAppointmentTypeDistribution`, `getAverageHistoryDurationByConditionsPerProvider` | ❌ Nee | Geaggregeerde patiëntdata | ❌ Nee |

### 5.3 Authenticatie- en autorisatie-events

| Event | Gelogd? | NEN-7510 8.15 compliant? |
|---|---|---|
| Succesvolle login | ❌ Nee (delegated naar OpenMRS platform) | ❌ Nee — niet op moduleniveau zichtbaar |
| Mislukte login | ❌ Nee (delegated naar OpenMRS platform) | ❌ Nee — niet op moduleniveau zichtbaar |
| Ongeauthenticeerde DWR-toegang (`Context.isAuthenticated() == false` in `DWRAppointmentService.java:66/88/138`) | ❌ Nee — silent return | ❌ Nee — gemiste detectiekans |
| Geweigerde toegang (`@Authorized` faalt) | ❌ Nee | ❌ Nee |
| Logout | ❌ Nee | ❌ Nee |
| Privilege escalatie | ❌ Nee | ❌ Nee |

### 5.4 Beheer-, configuratie- en task-events

| Event | Gelogd? | NEN-7510 8.15 compliant? |
|---|---|---|
| Module start | ✅ Ja (`AppointmentActivator.java:47,54`) | ✅ Ja |
| Module stop | ✅ Ja (`AppointmentActivator.java:67,74`) | ✅ Ja |
| Module refresh | ✅ Ja (`AppointmentActivator.java:33,40`) | ✅ Ja |
| Scheduled task: opruimen open afspraken | `CleanOpenAppointmentsTask` — ❌ geen log | ❌ Nee |
| Wijziging Global Property | ❌ Nee | ❌ Nee |
| Wijziging rollen/privileges (binnen module) | ❌ Nee | ❌ Nee |

### 5.5 Logbeveiliging zelf (norm-vereiste)

| Beveiligingsmaatregel op de logs | Aanwezig? | NEN-7510 8.15 compliant? |
|---|---|---|
| Logintegriteit (append-only / tamper-evident) | ❌ Niet gedocumenteerd | ❌ Nee |
| Toegangscontrole op logbestanden | ❌ Niet gedocumenteerd | ❌ Nee |
| Logretentie (bewaar- en vernietigingstermijn) | ❌ Niet gedocumenteerd | ❌ Nee |
| Logvertrouwelijkheid (encrypted at rest) | ❌ Niet gedocumenteerd | ❌ Nee |
| Betrouwbare tijdbron (NTP voor herleidbare timestamps) | ❌ Niet gedocumenteerd | ❌ Nee |

### 5.6 Monitoring (norm-vereiste, tweede helft van control 8.15)

| Monitoringmaatregel | Aanwezig? | NEN-7510 8.15 compliant? |
|---|---|---|
| Centrale logverwerking (bv. SIEM, ELK) | ❌ Nee | ❌ Nee |
| Alerting op verdachte patronen | ❌ Nee | ❌ Nee |
| Periodieke logreview-procedure | ❌ Niet gedocumenteerd | ❌ Nee |

---

## 6. Samenvatting van de gap

| Categorie | Aanwezig | Ontbrekend | Compliancegraad |
|---|---|---|---|
| Lifecycle / beheer | 6 logs | Global Property-wijziging, scheduled task | ⚠️ Beperkt |
| Debug | 5 logs | n.v.t. (zelfs risicovol) | ➖ |
| Technische fouten | 5 logs | n.v.t. | ➖ |
| **CRUD op patiëntdata** | 0 logs | **23 events** | ❌ 0% |
| **Toegang tot patiëntdata** | 0 valide logs (1 PII-lek) | **8+ events** | ❌ 0% |
| **Autorisatie-events** | 0 logs | **6 events** | ❌ 0% |
| **Scheduled tasks** | 0 logs | 1 event (CleanOpenAppointments) | ❌ 0% |
| **Logbeveiliging zelf** | 0 maatregelen | 5 vereisten | ❌ 0% |
| **Monitoring** | 0 maatregelen | 3 vereisten | ❌ 0% |

**Eindoordeel:** ❌ **Niet compliant met NEN-7510 8.15.**

De aanwezige logging dekt uitsluitend de modulecyclus en technische fouten. Geen enkele beveiligingsrelevante gebeurtenis op het niveau van patiëntdata wordt vastgelegd. De enige logregel die op het eerste oog op audit-logging lijkt (`getAppointmentsForPatientWithLogging`) is een **latent datalek**. Er is geen documentatie over logbeveiliging, retentie of monitoring.

---

## 7. Het gat tussen huidig en gewenst

### 7.1 Huidige staat

- Logging is **uitsluitend technisch**: lifecycle + foutmeldingen + debug
- Geen koppeling met de geauthenticeerde gebruiker (`wie`)
- Geen koppeling met het bewerkte object (`wat`)
- Geen vastlegging van de locatie of het kanaal van de aanroep (`waar`)
- Geen timestamp op beveiligingsrelevante events (`wanneer`)
- Geen vastlegging van het kanaal of de uitkomst (`hoe`)
- Geen registratie van beveiligingsincidenten (`@Authorized`-falen, ongeauthenticeerde DWR-aanroepen)
- Geen logging op scheduled tasks die data muteren
- Geen logbeveiliging gedocumenteerd (integriteit, retentie, toegang, tijdbron)
- Geen monitoring/alerting/SIEM
- Eén logregel bevat PII en is dus eerder een risico dan een controle
- Debug-statements in `AppointmentDataSetEvaluator` kunnen indirect PII lekken als debug in productie aan staat

### 7.2 Gewenste staat (NEN-7510 8.15)

Per beveiligingsrelevante gebeurtenis moeten de volgende vijf elementen worden vastgelegd:

| Veld | Betekenis | Bron in Java | Voorbeeld |
|---|---|---|---|
| **Wie** | Geauthenticeerde gebruiker die de actie uitvoert | `Context.getAuthenticatedUser().getUuid()` | `f2a8c1...` |
| **Wat** | Welke handeling op welk object (géén PII) | Methode-naam + object-ID | `saveAppointment appointmentId=1234` |
| **Waar** | Klasse, laag én IP-adres van de aanroeper | Klasse-naam + `WebContextFactory.getWebContext().getHttpServletRequest().getRemoteAddr()` | `AppointmentServiceImpl`, `192.0.2.10` |
| **Wanneer** | Tijdstip in UTC (ISO-8601), uit een via NTP gesynchroniseerde tijdbron | `Instant.now()` | `2026-06-09T10:14:22Z` |
| **Hoe** | Via welk kanaal (REST/DWR/service) en wat de uitkomst was | Aanroepende laag + resultaatstatus | `REST API`, `success` / `denied` / `failed` |

**Voorbeeldlogregel die aan alle vijf vereisten voldoet (zonder PII):**
```
[AUDIT] who=f2a8c1 what=saveAppointment:1234 where=AppointmentServiceImpl:192.0.2.10 when=2026-06-09T10:14:22Z how=REST/success
```

Daarnaast moeten **de logs zelf** aan de volgende eisen voldoen:

| Eis | Hoe te realiseren |
|---|---|
| Integriteit | Append-only opslag, hashketens of WORM-storage |
| Toegangscontrole | Alleen security-officers en logbeheer kunnen logs lezen; geen schrijftoegang voor applicatiegebruikers |
| Retentie | Documenteer bewaartermijn (richtlijn: minimaal 1 jaar voor medische audit-logs) en vernietigingsprocedure |
| Vertrouwelijkheid | Encrypted at rest, TLS bij transport naar SIEM |
| Tijdbron | NTP-synchronisatie op alle servers die logs produceren |

En **monitoring**:

| Eis | Hoe te realiseren |
|---|---|
| Centrale verzameling | Forward Log4j2-output naar een SIEM (bv. Splunk, ELK) |
| Alerting | Regels op verdachte patronen (bv. veel `@Authorized`-falens, bulk-reads, SQL-fouten) |
| Periodieke review | Procedure waarin een security-officer periodiek de logs beoordeelt |

### 7.3 Concrete actiepunten

| Prioriteit | Actie |
|---|---|
| 🔴 Kritiek | Verwijder de PII-loggende methode `getAppointmentsForPatientWithLogging` of strip de PII uit de logregel |
| 🔴 Kritiek | Voeg audit-logging toe aan alle write-methoden: `saveAppointment`, `voidAppointment`, `purgeAppointment`, `bookAppointment`, `changeAppointmentStatus`, plus de AppointmentRequest- en ProviderSchedule-mutaties |
| 🔴 Kritiek | Schoon de bestaande `log.debug` in `AppointmentDataSetEvaluator` zodat parameter-mappings nooit PII kunnen bevatten |
| 🟠 Hoog | Voeg een AOP-aspect of interceptor toe die `@Authorized`-afwijzingen logt |
| 🟠 Hoog | Log een waarschuwing in `DWRAppointmentService` wanneer `Context.isAuthenticated() == false` |
| 🟠 Hoog | Log lees-acties op patiëntdata-bulkqueries (`getAllAppointments`, `searchAppointmentsByPatientName`) |
| 🟠 Hoog | Voeg audit-logging toe aan `CleanOpenAppointmentsTask` (welke records geraakt, door welke taak, wanneer) |
| 🟡 Middel | Integreer de `openmrs-module-auditlog`-module of een eigen `AuditLog`-tabel voor centrale opslag |
| 🟡 Middel | Documenteer logretentie, logtoegang, logintegriteit en tijdbron (NTP) — norm vereist beveiliging van de logs zelf |
| 🟡 Middel | Documenteer de periodieke logreview-procedure (monitoring-deel van 8.15) |
| 🟢 Laag | Forward logs naar een SIEM en stel alerting-regels in op verdachte patronen |

---

## 8. Re-evaluatie t.o.v. sprint 1

In sprint 1 werd geconcludeerd dat A.8.15 *"gedeeltelijk aanwezig"* was op basis van 1 audit-log. Deze nieuwe analyse corrigeert dat oordeel: die ene logregel is in werkelijkheid een **PII-lek in dode code** en mag niet meetellen als audit-controle. Bovendien is in deze analyse breder gekeken — niet alleen naar audit-events, maar ook naar logbeveiliging, scheduled tasks, monitoring en het risico van bestaande debug-logging. De juiste status is daarom:

> **A.8.15 — Logging and monitoring: ❌ Afwezig**
> 0% van de beveiligingsrelevante events wordt gelogd; geen logbeveiliging gedocumenteerd; geen monitoring ingericht.
