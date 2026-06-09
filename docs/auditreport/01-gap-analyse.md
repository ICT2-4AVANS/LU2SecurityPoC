# Gap-analyse — Appointment Scheduling Module

| | |
|---|---|
| **Norm** | NEN-7510:2024-2 |
| **Module** | openmrs-module-appointmentscheduling v2.0.0 |
| **Datum** | 2026-06-03 |
| **Auteur** | Enes |

---

## Inleiding

Een gap-analyse vergelijkt de **huidige staat** van een systeem met een **gewenste norm of standaard**. Het woord "gap" staat voor de kloof tussen wat er is en wat er moet zijn. In de context van informatiebeveiliging wordt per control bepaald of een maatregel:

| Status | Betekenis |
|---|---|
| ✅ Aanwezig | Volledig geïmplementeerd en aantoonbaar in de code |
| ⚠️ Gedeeltelijk | Deels geïmplementeerd, maar met ontbrekende elementen |
| ❌ Afwezig | Niet geïmplementeerd of niet aantoonbaar |

Het resultaat is een prioriteitenlijst van beveiligingsmaatregelen die nog ontbreken of versterkt moeten worden.

---

## Scope

De analyse richt zich op drie NEN-7510:2024-2 controls die direct van toepassing zijn op een module die gevoelige medische gegevens verwerkt:

| Control | Onderwerp | Status |
|---|---|---|
| A.8.3 | Toegangsbeveiliging | ⚠️ Gedeeltelijk |
| A.8.5 | Authenticatie | ❌ Afwezig |
| A.8.15 | Logging en monitoring | ❌ Afwezig |

---

## A.8.3 — Toegangsbeveiliging

### Eis

> Toegang tot informatie en systemen moet worden beperkt op basis van het vastgestelde toegangsbeleid. Gebruikers mogen alleen toegang hebben tot de gegevens en functies die zij voor hun taak nodig hebben *(need-to-know / least privilege)*.

### Bevinding — ⚠️ Gedeeltelijk aanwezig

De module past op de service-laag consequent OpenMRS `@Authorized`-annotaties toe (97 stuks in `AppointmentService.java`). De DWR-laag controleert daarnaast `Context.isAuthenticated()` voordat er service-aanroepen worden gedaan. De basis-autorisatie is dus aanwezig en functioneel.

**De gap zit niet in de service-laag, maar in een onderliggende laag.**

#### Bewijs: aanwezig (service-laag)

**Bestand:** `api/src/main/java/org/openmrs/module/appointmentscheduling/api/AppointmentService.java`

```java
// Regel 60
@Authorized(AppointmentUtils.PRIV_VIEW_APPOINTMENT_TYPES)

// Regel 128
@Authorized(AppointmentUtils.PRIV_MANAGE_APPOINTMENT_TYPES)

// Regel 294
@Authorized(AppointmentUtils.PRIV_VIEW_APPOINTMENTS)
```

In totaal zijn er **97 `@Authorized`-annotaties** in `AppointmentService.java`. De DWR-laag (`DWRAppointmentService.java`) controleert daarnaast `Context.isAuthenticated()` op regel 66, 88 en 138.

#### Bewijs: gap (DAO-laag bevat ongeautoriseerde methode met SQL-injectie)

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

Deze methode:
1. Heeft **geen `@Authorized`-annotatie** — er is geen privilege-check
2. Concateneert input direct in een HQL-query — kwetsbaar voor injectie
3. Zit op de DAO-laag, *onder* de service-laag waar de autorisatiecontroles staan

Als deze methode via een toekomstige aanroep, een Spring-component-injectie of directe Hibernate-call wordt bereikt, kan de toegangsbeveiliging volledig worden omzeild. De aanvaller krijgt dan toegang tot **alle** afspraakgegevens, ongeacht patiënt-privileges.

### Risico

> De architectuur volgt geen *defense-in-depth*-principe. Toegangsbeveiliging zit alleen op de service-laag — onderliggende lagen (DAO/Hibernate) hebben geen eigen autorisatie. De aanwezigheid van `searchAppointmentsByPatientName` toont aan dat methoden op de DAO-laag *kunnen* worden toegevoegd zonder dat dit de privilege-architectuur passeert.

---

## A.8.5 — Authenticatie

### Eis

> Authenticatie-informatie (wachtwoorden, sleutels, tokens) moet veilig worden bewaard en mag niet toegankelijk zijn voor onbevoegden. Authenticatie moet sterk genoeg zijn voor het risiconiveau van de verwerkte gegevens.

### Bevinding — ❌ Afwezig (kritieke schending)

De module bevat **hardcoded credentials in de broncode**. Dit is een directe schending van NEN-7510 en geldt ongeacht of de code runtime wordt uitgevoerd, omdat geheimen permanent in de Git-historie en gecompileerde artefacten zitten.

#### Bewijs: hardcoded credentials

**Bestand:** `api/src/main/java/org/openmrs/module/appointmentscheduling/AppointmentActivator.java`

```java
// Regel 78–82
// HL7 reporting server credentials for appointment data export
private static final String HL7_EXPORT_HOST = "hl7-reports.hospital.internal";
private static final String HL7_EXPORT_USER = "appt_export_svc";
private static final String HL7_EXPORT_PASSWORD = "Appt@Export2021!";
private static final String HL7_DB_URL = "jdbc:mysql://hl7-reports.hospital.internal:3306/appointments?user=appt_export_svc&password=Appt@Export2021!";
```

Dit zijn:
- Een interne hostname van een productieserver (`hl7-reports.hospital.internal`)
- Een service-account naam (`appt_export_svc`)
- Een plain-text wachtwoord (`Appt@Export2021!`)
- Een complete JDBC-URL met credentials

Iedereen met toegang tot de repository of de gecompileerde `.jar` heeft deze credentials. Decompilatie van een JAR is triviaal en de Git-historie bewaart dit permanent.

#### Bewijs: geen vastgestelde authenticatievereisten

De module delegeert authenticatie aan het OpenMRS-platform, maar legt **nergens vereisten vast** voor:

- Minimale OpenMRS-versie voor compliance
- Vereiste sessietime-out
- Wachtwoordbeleid
- Verplichting van meervoudige authenticatie (MFA)

Zoekopdracht uitgevoerd op alle `.java`-bestanden in de module:

```bash
grep -rn "HttpSession|SecurityContext|AuthenticationManager|isAuthenticated|getSession"
```

| Term | Betekenis |
|---|---|
| `HttpSession` | Java-object dat een gebruikerssessie bijhoudt na het inloggen |
| `SecurityContext` | Spring Security-object met de huidige ingelogde gebruiker |
| `AuthenticationManager` | Centrale component dat authenticatie uitvoert |
| `isAuthenticated()` | Methode die controleert of gebruiker is ingelogd |
| `getSession()` | Haalt de huidige HTTP-sessie op |

Er is geen platformconfiguratie-document of `README`-sectie waarin staat welke configuratie het OpenMRS-platform minimaal moet hebben om aan NEN-7510 te voldoen.

### Risico

> Hardcoded credentials in broncode is een **kritieke kwetsbaarheid** onder NEN-7510. Lekken van de repository, een verloren laptop met checkout, of een misconfigured backup leidt direct tot compromittering van de HL7-rapportageserver. Daarnaast kan de module niet aantonen dat het platform aan de norm voldoet, omdat er geen minimumvereisten zijn vastgelegd.

---

## A.8.15 — Logging en monitoring

### Eis

> Gebeurtenissen die relevant zijn voor informatiebeveiliging moeten worden gelogd. Logbestanden moeten minimaal bevatten: **wie** de actie uitvoerde, **welke actie** werd uitgevoerd, en **wanneer**. Toegang tot en wijziging van gevoelige gegevens moet herleidbaar zijn.

### Bevinding — ❌ Afwezig

De module heeft **geen functionele audit-logging**. De enige logregel die op het eerste oog op audit-logging lijkt, is in werkelijkheid een **PII-lek dat in dode code zit**.

#### Bewijs: geen audit-logging op kritieke handelingen

In de gehele `AppointmentServiceImpl.java` is **geen enkele log-aanroep** te vinden bij methoden die afspraken aanmaken, wijzigen of verwijderen. Voorbeelden van methoden zonder audit-log:

| Methode (in AppointmentServiceImpl.java) | Audit-log aanwezig? |
|---|---|
| `saveAppointment()` | ❌ Nee |
| `voidAppointment()` | ❌ Nee |
| `purgeAppointment()` | ❌ Nee |
| `saveAppointmentBlock()` | ❌ Nee |
| `changeAppointmentStatus()` | ❌ Nee |
| Privilege-afwijzingen (via `@Authorized`) | ❌ Nee — geen log bij autorisatiefout |

#### Bewijs: enige "logregel" is een PII-lek

**Bestand:** `api/src/main/java/org/openmrs/module/appointmentscheduling/api/impl/AppointmentServiceImpl.java`

```java
// Regel 1422–1432
/**
 * Returns upcoming appointments for a patient.
 * VULNERABILITY: PII logging - logs patient name, DOB and appointment details to application log
 */
public java.util.List<Appointment> getAppointmentsForPatientWithLogging(Patient patient) {
    log.info("[AUDIT] Fetching appointments for patient: name=" + patient.getPersonName()
            + " dob=" + patient.getBirthdate()
            + " identifier=" + (patient.getPatientIdentifier() != null
                ? patient.getPatientIdentifier().getIdentifier() : "none")
            + " gender=" + patient.getGender());
    return getAppointmentsForPatient(patient);
}
```

Deze regel:
1. Wordt **nergens aangeroepen** in de module (verificatie: `grep -rn "getAppointmentsForPatientWithLogging"` levert alleen de definitie op)
2. Zou bij activering **gevoelige patiëntdata** in plain text in het applicatielog schrijven (naam, geboortedatum, identifier, geslacht)
3. Is dus geen audit-log, maar een **latent datalek**

Bestaande log-statements in de module betreffen uitsluitend technische fouten:

```java
// AppointmentBlockEditor.java, AppointmentEditor.java, TimeSlotEditor.java — Regel 46
log.error("Error setting text: " + text, ex);
```

Dit zijn geen audit-logs maar foutmeldingen voor type-conversie.

### Risico

> Bij een beveiligingsincident of audit is het **onmogelijk te reconstrueren** wie welke afspraken heeft aangemaakt, gewijzigd of geannuleerd. Dit schendt de NEN-7510-eis voor herleidbaarheid. Daarnaast vormt de bestaande "audit"-methode bij activering een direct datalek van persoonsgegevens — wat in plaats van compliance juist een AVG-overtreding zou veroorzaken.

---

## Samenvatting

| Control | Onderwerp | Status | Voornaamste gap |
|---|---|---|---|
| **A.8.3** | Toegangsbeveiliging | ⚠️ Gedeeltelijk | DAO-laag heeft geen autorisatie; aanwezigheid van `searchAppointmentsByPatientName` (zonder `@Authorized`, met SQL-injectie) toont gebrek aan defense-in-depth |
| **A.8.5** | Authenticatie | ❌ Afwezig | Hardcoded HL7-credentials in `AppointmentActivator.java`; geen vastgestelde platformvereisten |
| **A.8.15** | Logging | ❌ Afwezig | Geen audit-logging op aanmaken/wijzigen/verwijderen; enige "audit"-methode is een latent PII-lek |

---

## Aanbevelingen

| Prioriteit | Control | Aanbeveling |
|---|---|---|
| 🔴 Kritiek | A.8.5 | Verwijder hardcoded credentials uit `AppointmentActivator.java` en de Git-historie; verplaats naar OpenMRS Global Properties of externe secret-store |
| 🔴 Kritiek | A.8.15 | Verwijder de PII-loggende methode `getAppointmentsForPatientWithLogging` of vervang door geheugen-veilige audit-logging |
| 🔴 Hoog | A.8.3 | Verwijder of beveilig `searchAppointmentsByPatientName` — vervang HQL-concatenatie door geparametriseerde query en voeg `@Authorized` toe |
| 🟠 Hoog | A.8.15 | Voeg audit-logging toe aan `saveAppointment()`, `voidAppointment()`, `purgeAppointment()` met minimaal: wie (gebruiker-ID), wat (actie), wanneer (timestamp), zonder PII |
| 🟡 Middel | A.8.5 | Documenteer in `README.md` of `docs/` de minimale platformvereisten voor NEN-7510-compliance (sessietime-out, wachtwoordbeleid, MFA) |
| 🟡 Middel | A.8.3 | Documenteer expliciet dat de DAO-laag niet direct aangeroepen mag worden buiten de service-laag om defense-in-depth te waarborgen |
