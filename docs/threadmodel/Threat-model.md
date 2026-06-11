# Threat Model – OpenMRS Appointment Scheduling Module

## 1. Inleiding

Dit document beschrijft het threat model van de **OpenMRS Appointment Scheduling Module**. De module wordt gebruikt om patiëntafspraken te plannen, bekijken, wijzigen en annuleren binnen OpenMRS.

Voor dit threat model zijn eerst de C4-diagrammen uitgewerkt op drie niveaus:

- **C1 – Contextdiagram**
- **C2 – Containerdiagram**
- **C3 – Componentdiagram**

Daarna is met **Microsoft Threat Modeling Tool 2016** een Level 0 en Level 1 threat model gemaakt. De tool heeft automatisch threats gegenereerd op basis van de gemaakte Data Flow Diagrams. Niet alle gegenereerde threats zijn volledig uitgewerkt. In dit document zijn de **8 belangrijkste threats** geselecteerd, omdat deze het meest relevant zijn voor patiëntafspraken, afspraakgegevens en beschikbaarheid van de module.

---

## 2. C4-model

### 2.1 C1 – Contextdiagram

Het contextdiagram laat zien welke externe actoren en systemen betrokken zijn bij de Appointment Scheduling Module. De module is het systeem-in-scope. Zorgmedewerkers, artsen en beheerders gebruiken de module om afspraken te plannen en te beheren.

![C1 Contextdiagram](images/c1-contextdiagram.png)

---

### 2.2 C2 – Containerdiagram

Het containerdiagram laat zien uit welke technische hoofdonderdelen de module bestaat. De gebruiker werkt via de OpenMRS webinterface. De module gebruikt onder andere de Appointment Scheduling OMOD, REST Resources, Appointment Scheduling API, OpenMRS Core Services en de OpenMRS Database.

![C2 Containerdiagram](images/c2-containerdiagram.png)

---

### 2.3 C3 – Componentdiagram

Het componentdiagram zoomt in op de Appointment Scheduling Module. De belangrijkste onderdelen zijn de webcontrollers, REST-resources, servicelaag, DAO-laag en domeinklassen zoals `Appointment`, `AppointmentType`, `ProviderSchedule`, `AppointmentBlock`, `TimeSlot` en `AppointmentStatusHistory`.

![C3 Componentdiagram](images/c3-componentdiagram.png)

---

## 3. Methode threat modelling

Voor het threat model is gebruikgemaakt van **Microsoft Threat Modeling Tool 2016**. Deze tool genereert threats op basis van een Data Flow Diagram. De gegenereerde threats zijn gebaseerd op de STRIDE-methode.

| STRIDE                 | Betekenis                                  | Voorbeeld binnen deze module                     |
| ---------------------- | ------------------------------------------ | ------------------------------------------------ |
| Spoofing               | Iemand doet zich voor als iemand anders    | Aanvaller doet zich voor als zorgmedewerker      |
| Tampering              | Data wordt ongewenst aangepast             | Afspraakgegevens worden aangepast zonder rechten |
| Repudiation            | Acties zijn niet herleidbaar               | Niet duidelijk wie een afspraak heeft gewijzigd  |
| Information Disclosure | Gegevens worden zichtbaar voor onbevoegden | Patiëntafspraken worden gelekt                   |
| Denial of Service      | Systeem of functie wordt onbeschikbaar     | Afsprakenmodule of database valt uit             |
| Elevation of Privilege | Gebruiker krijgt te veel rechten           | Gewone gebruiker kan beheeracties uitvoeren      |

De gegenereerde threats zijn niet allemaal overgenomen. Er is een selectie gemaakt van de threats die het meest relevant zijn voor de Appointment Scheduling Module en de CIA/BIV-impact van patiëntafspraken.

---

## 4. Microsoft Threat Modeling Tool 2016 – Level 0

### 4.1 Level 0 DFD

Het Level 0 threat model is gebaseerd op het C1-contextdiagram. Hierin wordt vooral gekeken naar externe actoren en de systeemgrens van de Appointment Scheduling Module.

![Level 0 DFD](images/Level0DFD.png)

---

### 4.2 Gegenereerde threats Level 0

De Microsoft Threat Modeling Tool heeft meerdere threats gegenereerd voor het Level 0-diagram. De volledige gegenereerde lijst is als screenshot opgenomen. Daarna zijn de relevante threats geselecteerd.

![Level 0 Gegenereerde Threats](images/Level0Threads.png)

### 4.3 Geselecteerde Level 0 threats

| ID    | STRIDE            | Threat uit Microsoft Threat Modeling Tool                         | Eigen vertaling                                                     | Waarom relevant?                                                                    |
| ----- | ----------------- | ----------------------------------------------------------------- | ------------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| L0-T1 | Spoofing          | Spoofing the Zorgmedewerker / arts / beheerder External Entity    | Een aanvaller doet zich voor als zorgmedewerker, arts of beheerder. | Kan leiden tot onbevoegde toegang tot patiëntafspraken.                             |
| L0-T2 | Repudiation       | Potential Data Repudiation by Appointment Scheduling Module       | Wijzigingen aan afspraken zijn mogelijk niet goed herleidbaar.      | Bij afspraakwijzigingen moet duidelijk zijn wie wat heeft gedaan.                   |
| L0-T3 | Denial of Service | Potential Process Crash or Stop for Appointment Scheduling Module | De Appointment Scheduling Module crasht of stopt.                   | Als de module niet beschikbaar is, kunnen afspraken niet gepland of bekeken worden. |

---

## 5. Microsoft Threat Modeling Tool 2016 – Level 1

### 5.1 Level 1 DFD

Het Level 1 threat model is gebaseerd op het C2-containerdiagram. Hierin wordt gekeken naar de containers, datastromen en trust boundaries tussen de browser, OpenMRS Web UI, OMOD, REST Resources, API, Core Services en database.

![Level 1 DFD](images/Level1DFD.png)

---

### 5.2 Gegenereerde threats Level 1

De Microsoft Threat Modeling Tool heeft voor het Level 1-diagram een langere lijst threats gegenereerd. Veel threats waren dubbel of algemeen. Daarom zijn alleen de threats geselecteerd die direct relevant zijn voor de Appointment Scheduling Module.

![Level 1 Gegenereerde Threats](images/Level1Threads.png)

### 5.3 Geselecteerde Level 1 threats

| ID    | STRIDE                 | Threat uit Microsoft Threat Modeling Tool                                                   | Eigen vertaling                                                   | Waarom relevant?                                                                |
| ----- | ---------------------- | ------------------------------------------------------------------------------------------- | ----------------------------------------------------------------- | ------------------------------------------------------------------------------- |
| L1-T1 | Spoofing               | Spoofing the Browser gebruiker External Entity                                              | De browser of gebruikerssessie kan worden misbruikt.              | Sessie- of browsermisbruik kan leiden tot toegang tot afspraakgegevens.         |
| L1-T2 | Tampering              | Potential SQL Injection Vulnerability for OpenMRS Database                                  | Kwaadaardige input kan richting database worden gestuurd.         | De module leest en schrijft afspraakgegevens naar de OpenMRS Database.          |
| L1-T3 | Denial of Service      | Potential Excessive Resource Consumption for Appointment Scheduling API or OpenMRS Database | De API of database kan overbelast raken.                          | Overbelasting kan de afspraakplanning vertragen of tijdelijk onbruikbaar maken. |
| L1-T4 | Repudiation            | Potential Data Repudiation by OpenMRS Web UI                                                | Acties via de OpenMRS Web UI zijn mogelijk niet goed herleidbaar. | Gebruikersacties via de webinterface moeten controleerbaar zijn.                |
| L1-T5 | Information Disclosure | Weak Credential Transit                                                                     | Inloggegevens worden mogelijk onvoldoende beschermd verzonden.    | Zwakke overdracht van credentials kan leiden tot accountmisbruik.               |

---

## 6. Selectie van de 8 belangrijkste threats

De Microsoft Threat Modeling Tool heeft meer threats gegenereerd dan in dit document volledig worden uitgewerkt. Voor verdere analyse zijn de **8 belangrijkste threats** geselecteerd. Deze threats zijn gekozen omdat ze realistisch kunnen voorkomen binnen de Appointment Scheduling Module en een duidelijke impact hebben op vertrouwelijkheid, integriteit of beschikbaarheid.

| ID  | Level   | STRIDE                 | Threat                                                          | Betrokken onderdeel           | CIA/BIV-impact                  |
| --- | ------- | ---------------------- | --------------------------------------------------------------- | ----------------------------- | ------------------------------- |
| T1  | Level 0 | Spoofing               | Aanvaller doet zich voor als zorgmedewerker, arts of beheerder. | Gebruiker / browser           | Vertrouwelijkheid               |
| T2  | Level 0 | Repudiation            | Wijzigingen aan afspraken zijn niet goed herleidbaar.           | Appointment Scheduling Module | Integriteit                     |
| T3  | Level 0 | Denial of Service      | Appointment Scheduling Module crasht of stopt.                  | Appointment Scheduling Module | Beschikbaarheid                 |
| T4  | Level 1 | Spoofing               | Browser of gebruikerssessie wordt misbruikt.                    | Browser gebruiker             | Vertrouwelijkheid               |
| T5  | Level 1 | Tampering              | SQL Injection richting OpenMRS Database.                        | API → Database                | Vertrouwelijkheid + integriteit |
| T6  | Level 1 | Denial of Service      | API of database raakt overbelast.                               | API / Database                | Beschikbaarheid                 |
| T7  | Level 1 | Repudiation            | Acties via OpenMRS Web UI zijn niet goed herleidbaar.           | OpenMRS Web UI                | Integriteit                     |
| T8  | Level 1 | Information Disclosure | Weak Credential Transit.                                        | Browser → OpenMRS Web UI      | Vertrouwelijkheid               |

---

## 7. Risicobeoordeling

De geselecteerde threats zijn beoordeeld met dezelfde schaal als de CIA/BIV-analyse:

```text
Risico = Kans × Impact
```

| Score | Niveau  | Betekenis                             |
| ----: | ------- | ------------------------------------- |
|   1–4 | Laag    | Acceptabel risico                     |
|   5–9 | Middel  | Monitoren en waar mogelijk verbeteren |
| 10–15 | Hoog    | Maatregel verplicht                   |
| 16–25 | Kritiek | Direct oplossen of mitigeren          |

| ID  | Threat                                                         | Kans | Impact | Score | Niveau |
| --- | -------------------------------------------------------------- | ---: | -----: | ----: | ------ |
| T1  | Aanvaller doet zich voor als zorgmedewerker, arts of beheerder |    2 |      5 |    10 | Hoog   |
| T2  | Wijzigingen aan afspraken zijn niet goed herleidbaar           |    3 |      3 |     9 | Middel |
| T3  | Appointment Scheduling Module crasht of stopt                  |    2 |      4 |     8 | Middel |
| T4  | Browser of gebruikerssessie wordt misbruikt                    |    2 |      5 |    10 | Hoog   |
| T5  | SQL Injection richting OpenMRS Database                        |    3 |      5 |    15 | Hoog   |
| T6  | API of database raakt overbelast                               |    3 |      4 |    12 | Hoog   |
| T7  | Acties via OpenMRS Web UI zijn niet goed herleidbaar           |    3 |      3 |     9 | Middel |
| T8  | Weak Credential Transit                                        |    2 |      5 |    10 | Hoog   |

---

## 8. Belangrijkste threats

De belangrijkste threats zijn:

1. **T5 – SQL Injection richting OpenMRS Database**
2. **T1 – Aanvaller doet zich voor als zorgmedewerker, arts of beheerder**
3. **T4 – Browser of gebruikerssessie wordt misbruikt**
4. **T8 – Weak Credential Transit**
5. **T6 – API of database raakt overbelast**

Deze threats hebben prioriteit omdat ze direct invloed kunnen hebben op patiëntafspraken, afspraakgegevens en de beschikbaarheid van de module.

---

## 9. Attack Surface & Entry Points Analysis (NEW)

### 9.1 Gevonden Entry Points – Inventory

Een gedetailleerde analyse van de attack surface heeft **33 entry points** geïdentificeerd:

- **15 REST API endpoints** (`/rest/v1/appointmentscheduling/*`)
- **18 Web Controller endpoints** (`/module/appointmentscheduling/*`)

**Kritieke Bevinding:** Geen van de REST endpoints of Web controllers hebben `@PreAuthorize` autorisatiecontroles. Ze verlaten zich volledig op backend service-level @Authorized decorators, wat kan worden omzeild.

### 9.2 HIGH RISK Entry Points (Top 10)

Geïdentificeerde kritieke zwaktes per entry point:

| #   | Endpoint                              | Risk        | Threat Coupling                               | Fix                                       |
| --- | ------------------------------------- | ----------- | --------------------------------------------- | ----------------------------------------- |
| 1   | **POST /appointment**                 | 🔴 CRITICAL | Spoofing (T1), Tampering (L1-T2)              | Add @PreAuthorize("hasRole('CLINICIAN')") |
| 2   | **PUT /appointment**                  | 🔴 CRITICAL | Elevation (L1-T1), Repudiation (L0-T2)        | Add authorization check                   |
| 3   | **POST /appointmentSettingsForm**     | 🔴 CRITICAL | Elevation (all admins), DoS (L1-T3)           | Add @PreAuthorize("hasRole('ADMIN')")     |
| 4   | **POST /appointmentblock**            | 🔴 CRITICAL | Tampering (L1-T2), Elevation                  | Add privilege check                       |
| 5   | **POST /appointmentrequest**          | 🔴 CRITICAL | Spoofing (T1), Information Disclosure         | Add patient context check                 |
| 6   | **DELETE /appointment**               | 🔴 CRITICAL | Tampering (L1-T2), Repudiation (L0-T2, L1-T4) | Ensure audit trail                        |
| 7   | **GET /appointment?patient=X**        | 🔴 HOOG     | Information Disclosure (L1-T5, T8)            | Add row-level security                    |
| 8   | **POST /appointmenttype**             | 🟠 HOOG     | Elevation, System misconfiguration            | Add role check                            |
| 9   | **POST /providerschedule**            | 🟠 HOOG     | Tampering, Elevation                          | Add authorization                         |
| 10  | **POST /appointmentallowingoverbook** | 🟠 HOOG     | Denial of Service (L1-T3, L1-T6)              | Add rate limiting                         |

### 9.3 Trust Boundary Violations

**2 Critical Trust Boundaries Broken:**

1. **REST Layer Authorization Missing**
   - Assumption: REST controllers enforce privileges
   - Reality: ❌ NO @PreAuthorize annotations
   - Impact: Any authenticated user can POST/PUT/DELETE appointments
   - Maps to: T1 (Spoofing), L1-T1 (Browser spoofing)

2. **Web Controller Privilege Checks Missing**
   - Assumption: AppointmentSettingsForm checks admin privileges
   - Reality: ❌ Only checks `Context.isAuthenticated()`
   - Impact: Clinicians can modify global module settings
   - Maps to: L1-T1 (Elevation of Privilege)

### 9.4 Input Validation Gaps

- ❌ **No @Valid annotations** on REST RequestBody parameters
- ❌ **AppointmentSettingsForm** relies on client-side validation only
- ⚠️ **Date parameters** - parsed but not range-validated
- ⚠️ **Status enums** - only validated by Java enum check

### 9.5 Audit Logging Deficiency

- ✅ **DELETE operations** - logged via voidAppointment()
- ✅ **Status changes** - tracked in AppointmentStatusHistory
- ❌ **Settings mutations** - **NO AUDIT TRAIL**
- ⚠️ **CREATE/UPDATE operations** - audit level unclear

Maps to: L0-T2, L1-T4 (Repudiation threats)

---

## 9. Attack Surface & Entry Points Analysis (NEW - WS05 Assignment)

### 9.1 Gevonden Entry Points � Inventory

Een gedetailleerde analyse van de attack surface heeft **33 entry points** ge�dentificeerd:

- **15 REST API endpoints** (/rest/v1/appointmentscheduling/\*)
- **18 Web Controller endpoints** (/module/appointmentscheduling/\*)

**Kritieke Bevinding:** Geen van de REST endpoints of Web controllers hebben @PreAuthorize autorisatiecontroles. Ze verlaten zich volledig op backend service-level @Authorized decorators, wat kan worden omzeild.

### 9.2 HIGH RISK Entry Points (Top 10)

Ge�dentificeerde kritieke zwaktes per entry point die rechtstreeks koppelen aan de 8 threats:

| Endpoint                          | Risk        | Threat Coupling                        | Mitigation                                |
| --------------------------------- | ----------- | -------------------------------------- | ----------------------------------------- |
| **POST /appointment**             | ?? CRITICAL | T1 (Spoofing), L1-T2 (Tampering)       | Add @PreAuthorize("hasRole('CLINICIAN')") |
| **PUT /appointment**              | ?? CRITICAL | L1-T1 (Elevation), L0-T2 (Repudiation) | Add authorization check                   |
| **POST /appointmentSettingsForm** | ?? CRITICAL | L1-T1 (Elevation), L1-T3 (DoS)         | Add @PreAuthorize("hasRole('ADMIN')")     |
| **GET /appointment**              | ?? HOOG     | L1-T5 (Information Disclosure)         | Add row-level security                    |
| **DELETE /appointment**           | ?? HOOG     | L0-T2, L1-T4 (Repudiation)             | Ensure audit trail                        |

### 9.3 Correlatie Attack Surface ? 8 Belangrijkste Threats

| Threat                      | Current Status | Enhanced Risk from Attack Surface       | Recommendation                   |
| --------------------------- | -------------- | --------------------------------------- | -------------------------------- |
| **T1** (Spoofing)           | Hoog (10)      | ?? **ELEVATED** � No REST auth          | Add endpoint-level authorization |
| **T2** (Repudiation)        | Middel (9)     | ?? **ELEVATED** � No audit for settings | Add audit logging                |
| **T3** (DoS)                | Middel (8)     | ?? **ELEVATED** � No rate limiting      | Implement rate limits            |
| **T4** (Session misuse)     | Hoog (10)      | ?? **ELEVATED** � All endpoints exposed | Add @PreAuthorize globally       |
| **T5** (SQL Injection)      | Hoog (15)      | ? **MITIGATED** � Hibernate ORM         | Keep as-is                       |
| **T6** (API overload)       | Hoog (12)      | ?? **ELEVATED** � Bulk ops allowed      | Add resource limits              |
| **T7** (Web UI repudiation) | Middel (9)     | ? **MITIGATED** � Spring logs           | Keep as-is                       |
| **T8** (Credential transit) | Hoog (10)      | ?? **DEPENDS** � HTTPS config           | Verify server setup              |

**Result:** 5 out of 8 threats now require ENHANCED PROTECTION based on attack surface findings.

### 9.4 NEN-7510:2024-2 Compliance Mapping

| Control                     | Current    | Gap                   | Priority    |
| --------------------------- | ---------- | --------------------- | ----------- |
| **8.25** (Security in SDLC) | ?? Partial | Authorization missing | ?? CRITICAL |
| **8.28** (Secure coding)    | ?? Weak    | Input validation      | ?? CRITICAL |
| **8.15** (Audit logging)    | ?? Partial | Settings not logged   | ?? HOOG     |
| **8.1** (Confidentiality)   | ? No RLS   | Row-level security    | ?? HOOG     |

---

## 10. Remediation & Next Steps (WS06 Preparation)

1. **Phase 1 (CRITICAL):** Implement @PreAuthorize on 15 REST endpoints
2. **Phase 2 (HOOG):** Add input validation + audit logging
3. **Phase 3 (MEDIUM):** Row-level security + security testing

See: [docs/attack-surface-mapping.md](../attack-surface-mapping.md) for complete entry point inventory and remediation roadmap.

---

**Document Version:** 2.0 (Attack Surface Update for WS05)  
**Last Updated:** June 11, 2026  
**Status:** COMPLETE � Ready for WS05 Submission & WS06 Remediation Planning
