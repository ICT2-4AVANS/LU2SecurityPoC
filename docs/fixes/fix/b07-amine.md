# Fix B-07 — CSRF-beveiliging DWRAppointmentService

| | |
|---|---|
| **Bevinding** | B-07 — Ontbrekende CSRF-beveiliging en sessie-hardening |
| **Ernst** | High — CVSS 8.1 |
| **NEN-7510** | A.8.5 – Authenticatie |
| **Opgelost door** | Amine |
| **Datum** | 2026-06-15 |

---

## Probleem

De DWR-laag (`DWRAppointmentService.java`) verwerkte JavaScript-naar-Java-aanroepen zonder te controleren of het request afkomstig was van de eigen applicatie. Een aanvaller kon een ingelogde zorgmedewerker misleiden om een nep-pagina te bezoeken die vervolgens namens hem DWR-aanroepen deed — een klassieke CSRF-aanval.

Alle publieke methoden controleerden alleen `Context.isAuthenticated()` maar niet de herkomst van het request.

**Kwetsbare code (voor fix) — `DWRAppointmentService.java`:**

```java
public PatientData getPatientDescription(Integer patientId) {
    Patient patient = Context.getPatientService().getPatient(patientId);
    // Geen enkele controle op herkomst van het request
    ...
}
```

---

## Abuse bewijs

> **Voeg hier een screenshot toe van:**
> 1. DevTools → Application → Cookies → JSESSIONID zonder HttpOnly/Secure vlag
> 2. DevTools → Network → DWR request zonder CSRF-token in headers

*(Screenshots toe te voegen na browser test op localhost:8082)*

---

## Fix

**Aanpak:** Alle publieke DWR-methoden roepen nu `requireXmlHttpRequest()` aan. Deze methode controleert de `X-Requested-With: XMLHttpRequest` header.

**Waarom werkt dit als CSRF-verdediging:**
- DWR zet automatisch `X-Requested-With: XMLHttpRequest` op elk eigen request
- Een cross-site HTML-form of redirect kan deze header **niet** meesturen (browser blokkeert dit via het same-origin policy)
- Een aanvaller kan deze header dus niet nabootsen vanuit een andere site

**Toegevoegde methode in `DWRAppointmentService.java`:**

```java
private void requireXmlHttpRequest() {
    WebContext webContext = WebContextFactory.get();
    HttpServletRequest request = webContext.getHttpServletRequest();
    if (!"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
        throw new SecurityException("CSRF protection: X-Requested-With header missing or invalid");
    }
}
```

**Aanroep in elke publieke methode:**

```java
public PatientData getPatientDescription(Integer patientId) {
    requireXmlHttpRequest(); // CSRF-check
    Patient patient = Context.getPatientService().getPatient(patientId);
    ...
}
```

Alle 11 publieke methoden zijn voorzien van deze check.

---

## Buiten scope van de module

De volgende onderdelen van B-07 vallen buiten de verantwoordelijkheid van de module:

| Onderdeel | Verantwoordelijkheid |
|---|---|
| `HttpOnly` / `Secure` cookie flags | Tomcat / OpenMRS core (`web.xml`) |
| Session-ID rotatie bij login | OpenMRS core (`SessionFixationProtectionStrategy`) |
| `SameSite=Strict` cookie attribuut | Tomcat configuratie |

Aanbeveling aan de beheerder: stel in `$TOMCAT_HOME/conf/context.xml` de `SameSite` en `HttpOnly` cookie attributen in, en zorg dat HTTPS verplicht is in productie.

---

## Test

**Testbestand:** `omod/src/test/java/org/openmrs/module/appointmentscheduling/DWRAppointmentServiceCsrfTest.java`

**Uitgevoerd commando:**
```powershell
mvn -pl omod -Dtest=DWRAppointmentServiceCsrfTest test
```

**Resultaat:**
```
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

De test verifieert:
- `requireXmlHttpRequest()` methode is aanwezig
- Methode valideert `X-Requested-With` header op waarde `XMLHttpRequest`
- `SecurityException` wordt gegooid bij ontbrekende header
- De check is aanwezig in `getPatientDescription` en `getAppointmentBlocksForCalendar`

---

## NEN-7510 A.8.5 compliance

Voor de fix kon elke geldige sessiecookie gebruikt worden om DWR-aanroepen te doen vanuit een andere herkomst. Na de fix worden alleen requests geaccepteerd die aantoonbaar afkomstig zijn van de eigen DWR-client, conform het authenticatieprincipe in A.8.5.
