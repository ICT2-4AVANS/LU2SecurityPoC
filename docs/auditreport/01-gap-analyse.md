# Gap-analyse — Appointment Scheduling Module

| | |
|---|---|
| **Norm** | NEN-7510:2024-2 |
| **Module** | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT |
| **Datum** | 2026-06-03 |
| **Auteur** | Enes |

---

## Inleiding

Een gap-analyse vergelijkt de **huidige staat** van een systeem met een **gewenste norm of standaard**. Het woord "gap" staat voor de kloof tussen wat er is en wat er moet zijn.

Voor dit onderzoek zijn drie NEN-7510:2024-2 controls geselecteerd die direct van toepassing zijn op een module die gevoelige medische gegevens verwerkt (patiëntafspraken, zorgverleners, tijdsgegevens). Per control wordt:

1. **De norm-eis** beschreven
2. **De OpenMRS-implementatie op platformniveau** onderzocht via de officiële documentatie en broncode
3. **De module-implementatie** geverifieerd in onze broncode
4. **De compliance-actie** bepaald — wat is nodig om aan de norm te voldoen

---

## Geselecteerde controls

| Control | Onderwerp | Waarom relevant |
|---|---|---|
| A.8.3 | Toegangsbeveiliging | Module gebruikt rollen/privileges; bevat gevoelige patiëntdata |
| A.8.5 | Authenticatie | Module is alleen bereikbaar via geauthenticeerde sessies |
| A.8.15 | Logging en monitoring | Module wijzigt afspraakgegevens — herleidbaarheid vereist |

---

## A.8.3 — Toegangsbeveiliging

### 1. Wat zegt de norm

> Toegang tot informatie en systemen moet worden beperkt op basis van het vastgestelde toegangsbeleid. Gebruikers mogen alleen toegang hebben tot de gegevens en functies die zij voor hun taak nodig hebben *(need-to-know / least privilege)*. Toegangsrechten moeten regelmatig worden beoordeeld.

### 2. Hoe doet OpenMRS dit op platformniveau

Volgens de OpenMRS Wiki (`wiki.openmrs.org` — "Managing Privileges") werkt OpenMRS met een **role-based access control (RBAC)**-systeem:

- **Privileges** zijn de kleinste eenheid: bijv. `View Appointments`, `Manage Appointments`
- **Rollen** bundelen privileges en worden toegekend aan gebruikers
- Op codeniveau wordt dit afgedwongen via de annotatie `@Authorized(...)` op service-methoden
- De OpenMRS Service Context controleert bij elke aanroep of de huidige gebruiker het vereiste privilege heeft

Het framework forceert dit op de **service-laag**. Onderliggende lagen (DAO/Hibernate) worden geacht alleen via de service te worden aangeroepen.

### 3. Hoe is dit in onze module geïmplementeerd

#### Aanwezig — service-laag

**Bestand:** `api/src/main/java/org/openmrs/module/appointmentscheduling/api/AppointmentService.java`

```java
// Regel 60
@Authorized(AppointmentUtils.PRIV_VIEW_APPOINTMENT_TYPES)

// Regel 128
@Authorized(AppointmentUtils.PRIV_MANAGE_APPOINTMENT_TYPES)

// Regel 294
@Authorized(AppointmentUtils.PRIV_VIEW_APPOINTMENTS)
```

In totaal **97 `@Authorized`-annotaties** in `AppointmentService.java`. De DWR-laag (`DWRAppointmentService.java`, regels 66/88/138) controleert bovendien `Context.isAuthenticated()`.

#### Afwezig — DAO-laag

**Bestand:** `api/src/main/java/org/openmrs/module/appointmentscheduling/api/db/hibernate/HibernateAppointmentDAO.java`

```java
// Regel 315–319
public java.util.List<?> searchAppointmentsByPatientName(String patientName) {
    // VULNERABILITY: SQL injection - patientName is concatenated directly into query
    String hql = "from Appointment ap where ap.visit.patient.personName.givenName = '"
                 + patientName + "' or ap.visit.patient.personName.familyName = '"
                 + patientName + "'";
    return super.sessionFactory.getCurrentSession().createQuery(hql).list();
}
```

Deze DAO-methode heeft geen `@Authorized`-annotatie én concateneert input direct in een HQL-query. Als de service-laag wordt omzeild, is er geen autorisatiecheck én is SQL-injectie mogelijk.

**Status:** ⚠️ Gedeeltelijk aanwezig

### 4. Wat moet er gebeuren voor compliance

| Actie | Doel |
|---|---|
| `searchAppointmentsByPatientName` verwijderen of beveiligen | Voorkomt omzeiling van privileges via SQL-injectie |
| HQL-concatenatie vervangen door geparametriseerde query | Voldoet aan het principe van inputvalidatie |
| `@Authorized`-annotatie toevoegen aan DAO-methoden die direct aanroepbaar zijn | Defense-in-depth — niet vertrouwen op één laag |
| Periodieke privilege-review documenteren | Norm vereist regelmatige beoordeling van toegangsrechten |

---

## A.8.5 — Authenticatie

### 1. Wat zegt de norm

> Authenticatie-informatie (wachtwoorden, sleutels, tokens) moet veilig worden bewaard en mag niet toegankelijk zijn voor onbevoegden. Authenticatiemechanismen moeten sterk genoeg zijn voor het risiconiveau van de verwerkte gegevens.

### 2. Hoe doet OpenMRS dit op platformniveau

Volgens de OpenMRS Wiki (`wiki.openmrs.org` — "Authentication"):

- OpenMRS gebruikt een eigen `UserService` voor authenticatie
- Wachtwoorden worden gehasht opgeslagen (SHA-512 met salt in `users`-tabel)
- Sessies worden beheerd via Spring Security
- Account-lockout na herhaalde mislukte pogingen is configureerbaar via Global Properties
- MFA wordt **niet** standaard ondersteund — vereist een aanvullende module

Modules zijn geacht authenticatie **niet zelf te implementeren** en in plaats daarvan de platformcontext te gebruiken (`Context.getAuthenticatedUser()`).

### 3. Hoe is dit in onze module geïmplementeerd

#### Afwezig — geen eigen authenticatielogica (correct ontwerp)

Zoekopdracht uitgevoerd op alle `.java`-bestanden in de module:

```bash
grep -rn "HttpSession|SecurityContext|AuthenticationManager|isAuthenticated|getSession"
```

| Term | Betekenis |
|---|---|
| `HttpSession` | Java-object voor sessiebeheer |
| `SecurityContext` | Spring Security-object met huidige gebruiker |
| `AuthenticationManager` | Centrale authenticatie-component |
| `isAuthenticated()` | Controleert of gebruiker is ingelogd |
| `getSession()` | Haalt huidige HTTP-sessie op |

De module bevat geen eigen authenticatielogica. Dit is conform OpenMRS-ontwerp en op zichzelf geen probleem.

#### Afwezig — kritieke schending: hardcoded credentials

**Bestand:** `api/src/main/java/org/openmrs/module/appointmentscheduling/AppointmentActivator.java`

```java
// Regel 78–82
// HL7 reporting server credentials for appointment data export
private static final String HL7_EXPORT_HOST = "hl7-reports.hospital.internal";
private static final String HL7_EXPORT_USER = "appt_export_svc";
private static final String HL7_EXPORT_PASSWORD = "Appt@Export2021!";
private static final String HL7_DB_URL = "jdbc:mysql://hl7-reports.hospital.internal:3306/appointments?user=appt_export_svc&password=Appt@Export2021!";
```

Een plain-text wachtwoord en complete JDBC-URL staan in de broncode. Dit is een **directe schending** van A.8.5 — credentials moeten beveiligd worden bewaard, niet in version control of in een gecompileerde JAR.

#### Afwezig — geen platformvereisten gedocumenteerd

De module legt nergens vast welke OpenMRS-platformconfiguratie minimaal nodig is voor NEN-7510-compliance (sessietime-out, wachtwoordbeleid, MFA-eis).

**Status:** ❌ Afwezig

### 4. Wat moet er gebeuren voor compliance

| Actie | Doel |
|---|---|
| Hardcoded credentials verwijderen uit `AppointmentActivator.java` | Directe schending van A.8.5 opheffen |
| Credentials verplaatsen naar OpenMRS Global Properties of een externe secret-store | Veilige opslag conform norm |
| Git-historie schonen (credentials staan permanent in oude commits) | Voorkomen van lekken via repo-toegang |
| Platformvereisten documenteren in `README.md` of `docs/` (sessietime-out, MFA-eis, wachtwoordbeleid) | Aantoonbare compliance bij audit |

---

## A.8.15 — Logging en monitoring

### 1. Wat zegt de norm

> Gebeurtenissen die relevant zijn voor informatiebeveiliging moeten worden gelogd. Logbestanden moeten minimaal bevatten: **wie** de actie uitvoerde, **welke actie** werd uitgevoerd, en **wanneer**. Toegang tot en wijziging van gevoelige gegevens moet herleidbaar zijn. Logs zelf moeten worden beveiligd tegen ongeautoriseerde wijziging.

### 2. Hoe doet OpenMRS dit op platformniveau

Volgens de OpenMRS Wiki (`wiki.openmrs.org` — "Auditing Data Changes"):

- Standaard OpenMRS heeft **geen ingebouwde audit-logging** voor data-wijzigingen
- Het platform gebruikt Log4j2 voor algemene logging (errors, info)
- Voor audit-logging is een **aparte module** beschikbaar: `openmrs-module-auditlog`
- Deze module logt automatisch create/update/delete-acties op gemarkeerde entiteiten
- Modules die patiëntdata wijzigen, worden geacht óf de `auditlog`-module te gebruiken óf zelf audit-logging te implementeren

### 3. Hoe is dit in onze module geïmplementeerd

#### Afwezig — geen audit-logging op kritieke handelingen

In `AppointmentServiceImpl.java` is **geen enkele audit-logregel** aanwezig bij methoden die afspraken aanmaken, wijzigen of verwijderen:

| Methode | Audit-log aanwezig? |
|---|---|
| `saveAppointment()` | ❌ Nee |
| `voidAppointment()` | ❌ Nee |
| `purgeAppointment()` | ❌ Nee |
| `saveAppointmentBlock()` | ❌ Nee |
| `changeAppointmentStatus()` | ❌ Nee |

De module gebruikt **niet** de `openmrs-module-auditlog`-module en heeft ook geen eigen alternatief.

#### Afwezig — enige "audit"-statement is een PII-lek

**Bestand:** `api/src/main/java/org/openmrs/module/appointmentscheduling/api/impl/AppointmentServiceImpl.java`

```java
// Regel 1422–1432
/**
 * VULNERABILITY: PII logging - logs patient name, DOB and appointment details to application log
 */
public java.util.List<Appointment> getAppointmentsForPatientWithLogging(Patient patient) {
    log.info("[AUDIT] Fetching appointments for patient: name=" + patient.getPersonName()
            + " dob=" + patient.getBirthdate()
            + " identifier=" + ...
            + " gender=" + patient.getGender());
    return getAppointmentsForPatient(patient);
}
```

Deze methode:
1. Wordt nergens aangeroepen — dode code
2. Zou bij activering patiëntnaam, geboortedatum, identifier en geslacht in plain text naar het log schrijven
3. Is dus geen audit-log maar een **latent datalek**

**Status:** ❌ Afwezig

### 4. Wat moet er gebeuren voor compliance

| Actie | Doel |
|---|---|
| `getAppointmentsForPatientWithLogging` verwijderen | Voorkomt latent PII-lek |
| `openmrs-module-auditlog` toevoegen als dependency, of eigen audit-logging implementeren | Voldoen aan A.8.15 herleidbaarheidseis |
| Audit-log toevoegen aan `saveAppointment`, `voidAppointment`, `purgeAppointment` met **wie** (gebruiker-ID), **wat** (actie), **wanneer** (timestamp), **zonder PII in het logbericht** | Norm-conforme logging |
| Privilege-afwijzingen (autorisatiefouten) loggen | Detectie van ongeautoriseerde toegangspogingen |
| Logbeveiliging documenteren (wie heeft logtoegang, retentie, integriteit) | Norm vereist beveiliging van de logs zelf |

---

## Samenvatting

| Control | Status | OpenMRS-platform | Module-implementatie |
|---|---|---|---|
| **A.8.3** Toegangsbeveiliging | ⚠️ Gedeeltelijk | RBAC via `@Authorized` op service-laag | 97 annotaties op service; DAO-methode zonder check + SQL-injectie |
| **A.8.5** Authenticatie | ❌ Afwezig | Eigen `UserService` met gehashte wachtwoorden | Geen eigen logica (goed) + hardcoded HL7-credentials (kritiek) |
| **A.8.15** Logging | ❌ Afwezig | Vereist `openmrs-module-auditlog` of eigen implementatie | Geen audit-log op CRUD; dode methode lekt PII |

---

## Compliance-routekaart

| Prioriteit | Control | Actie |
|---|---|---|
| 🔴 Kritiek | A.8.5 | Hardcoded credentials verwijderen + Git-historie schonen |
| 🔴 Kritiek | A.8.15 | PII-loggende methode verwijderen |
| 🔴 Hoog | A.8.3 | SQL-injectie in `searchAppointmentsByPatientName` repareren of methode verwijderen |
| 🟠 Hoog | A.8.15 | Audit-logging implementeren via `openmrs-module-auditlog` of eigen oplossing |
| 🟡 Middel | A.8.5 | Platformvereisten documenteren (sessietime-out, MFA, wachtwoordbeleid) |
| 🟡 Middel | A.8.3 | DAO-toegang documenteren als alleen-via-service om defense-in-depth te waarborgen |
