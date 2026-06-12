# Threat Model – OpenMRS Appointment Scheduling Module

## 1. Inleiding

Dit document beschrijft het threat model van de **OpenMRS Appointment Scheduling Module**. De module wordt gebruikt om patiëntafspraken te plannen, bekijken, wijzigen en annuleren binnen OpenMRS.

Voor dit threat model zijn eerst C4-diagrammen gemaakt. Daarna is met **Microsoft Threat Modeling Tool 2016** een Level 0 en Level 1 threat model uitgewerkt. Op basis van de attack surface mapping is het threat model bijgewerkt met extra aandacht voor entry points, trust boundaries en high risk ingangen.

---

## 2. C4-model

### 2.1 C1 – Contextdiagram

Het contextdiagram laat zien welke externe actoren en systemen betrokken zijn bij de module. De Appointment Scheduling Module is het systeem-in-scope. Zorgmedewerkers, artsen en beheerders gebruiken de module via OpenMRS.

![C1 Contextdiagram](images/c1-contextdiagram.png)

### 2.2 C2 – Containerdiagram

Het containerdiagram laat de belangrijkste technische onderdelen zien: OpenMRS Web UI, Appointment Scheduling OMOD, REST Resources, Appointment Scheduling API, OpenMRS Core Services en de OpenMRS Database.

![C2 Containerdiagram](images/c2-containerdiagram.png)

### 2.3 C3 – Componentdiagram

Het componentdiagram zoomt in op de module. Belangrijke onderdelen zijn de webcontrollers, REST-resources, servicelaag, DAO-laag en domeinklassen zoals `Appointment`, `AppointmentType`, `ProviderSchedule`, `AppointmentBlock`, `TimeSlot` en `AppointmentStatusHistory`.

![C3 Componentdiagram](images/c3-componentdiagram.png)

---

## 3. Methode threat modelling

Voor het threat model is gebruikgemaakt van **Microsoft Threat Modeling Tool 2016**. De tool genereert threats op basis van STRIDE.

| STRIDE                 | Betekenis                               | Voorbeeld binnen deze module                         |
| ---------------------- | --------------------------------------- | ---------------------------------------------------- |
| Spoofing               | Iemand doet zich voor als iemand anders | Aanvaller gebruikt account/sessie van zorgmedewerker |
| Tampering              | Data wordt ongewenst aangepast          | Afspraak wordt gewijzigd zonder juiste rechten       |
| Repudiation            | Acties zijn niet herleidbaar            | Niet duidelijk wie een afspraak heeft aangepast      |
| Information Disclosure | Gegevens lekken uit                     | Patiëntafspraken zijn zichtbaar voor onbevoegden     |
| Denial of Service      | Systeem wordt onbeschikbaar             | API of database raakt overbelast                     |
| Elevation of Privilege | Gebruiker krijgt te veel rechten        | Niet-admin wijzigt module-instellingen               |

---

## 4. Microsoft Threat Modeling Tool – Level 0

### 4.1 Level 0 DFD

Het Level 0 threat model is gebaseerd op het C1-contextdiagram. Hierin staat vooral de relatie tussen externe gebruikers en de Appointment Scheduling Module centraal.

![Level 0 DFD](images/Level0DFD.png)

### 4.2 Gegenereerde threats Level 0

De Microsoft Threat Modeling Tool heeft meerdere threats gegenereerd. De volledige lijst is als screenshot opgenomen.

![Level 0 Gegenereerde Threats](images/Level0Threads.png)

### 4.3 Relevante Level 0 threats

| ID    | STRIDE            | Threat                                                         | Waarom relevant?                                       |
| ----- | ----------------- | -------------------------------------------------------------- | ------------------------------------------------------ |
| L0-T1 | Spoofing          | Aanvaller doet zich voor als zorgmedewerker, arts of beheerder | Kan leiden tot onbevoegde toegang tot patiëntafspraken |
| L0-T2 | Repudiation       | Wijzigingen aan afspraken zijn niet goed herleidbaar           | Afspraakwijzigingen moeten controleerbaar zijn         |
| L0-T3 | Denial of Service | Appointment Scheduling Module crasht of stopt                  | Afspraken kunnen niet gepland of bekeken worden        |

---

## 5. Microsoft Threat Modeling Tool – Level 1

### 5.1 Level 1 DFD

Het Level 1 threat model is gebaseerd op het C2-containerdiagram. Hierin staan de datastromen tussen browser, Web UI, REST Resources, API, Core Services en database.

![Level 1 DFD](images/Level1DFD.png)

### 5.2 Gegenereerde threats Level 1

De volledige lijst met gegenereerde Level 1 threats is als screenshot opgenomen.

![Level 1 Gegenereerde Threats](images/Level1Threads.png)

### 5.3 Relevante Level 1 threats

| ID    | STRIDE                 | Threat                                                  | Waarom relevant?                            |
| ----- | ---------------------- | ------------------------------------------------------- | ------------------------------------------- |
| L1-T1 | Spoofing               | Browser of gebruikerssessie wordt misbruikt             | Kan toegang geven tot afspraakgegevens      |
| L1-T2 | Tampering              | Kwaadaardige input richting database                    | Afspraakgegevens kunnen worden beïnvloed    |
| L1-T3 | Denial of Service      | API of database raakt overbelast                        | Afspraakplanning kan onbruikbaar worden     |
| L1-T4 | Repudiation            | Web UI-acties zijn niet goed herleidbaar                | Belangrijk voor audit trail                 |
| L1-T5 | Information Disclosure | Gevoelige gegevens worden onveilig verzonden of getoond | Kan leiden tot datalek van patiëntafspraken |

---

## 6. Attack Surface Update

Op basis van de attack surface mapping zijn de bestaande threats aangescherpt. De belangrijkste ingangen tot de module zijn:

- REST API endpoints onder `/rest/v1/appointmentscheduling/*`
- Web controller endpoints onder `/module/appointmentscheduling/*`
- Formulieren voor afspraken, afspraakblokken, afspraaktypes en instellingen
- Service- en DAO-laag richting de OpenMRS Database
- Configuratie en module-instellingen

De belangrijkste bevinding is dat bij meerdere controllers en resources geen duidelijke **endpoint-level autorisatiecheck** zichtbaar is. De module lijkt sterk te vertrouwen op authenticatie via OpenMRS en autorisatie in de servicelaag. Dit moet gecontroleerd worden, omdat vooral object-level autorisatie belangrijk is: een gebruiker mag niet automatisch elke afspraak of elke patiënt bekijken of wijzigen.

### High risk ingangen

| Entry point                                          | Risico                                                     | Gekoppelde threat                 |
| ---------------------------------------------------- | ---------------------------------------------------------- | --------------------------------- |
| Afspraak bekijken via `patientId` of `appointmentId` | Onbevoegde inzage in patiëntafspraken                      | Information Disclosure            |
| Afspraak aanmaken                                    | Afspraak voor verkeerde of onbevoegde patiënt              | Spoofing / Tampering              |
| Afspraak wijzigen                                    | Manipulatie van afspraakstatus, tijdslot of reden          | Tampering                         |
| Afspraak annuleren/verwijderen                       | Zorgproces kan worden verstoord                            | Tampering / Repudiation           |
| Appointment settings wijzigen                        | Niet-admin kan moduleconfiguratie beïnvloeden              | Elevation of Privilege            |
| Appointment blocks en provider schedules wijzigen    | Beschikbaarheid van zorgverleners kan worden gemanipuleerd | Tampering / DoS                   |
| REST API requests                                    | Makkelijk te testen/misbruiken via Postman of Burp         | Spoofing / Information Disclosure |
| DAO/database queries                                 | Onveilige query-afhandeling kan data beïnvloeden           | Tampering / Injection             |

---

## 7. Impliciete trust

De module vertrouwt impliciet op meerdere onderdelen. Deze trust moet expliciet worden meegenomen in het threat model.

| Vertrouwd onderdeel       | Wat wordt vertrouwd?                                               | Risico als dit niet klopt        |
| ------------------------- | ------------------------------------------------------------------ | -------------------------------- |
| OpenMRS authenticatie     | Ingelogde gebruiker is wie hij zegt te zijn                        | Spoofing / sessiemisbruik        |
| OpenMRS privileges        | Privileges worden correct gecontroleerd                            | Privilege escalation             |
| Browser/Web UI            | Gebruiker gebruikt alleen normale formulieren                      | Requests kunnen aangepast worden |
| Hidden form fields / ID’s | `patientId`, `appointmentId` en `providerId` worden niet aangepast | IDOR/BOLA                        |
| REST clients              | API-clients sturen toegestane requests                             | Onbevoegde CRUD-acties           |
| Service layer             | Controllers roepen beveiligde servicemethodes aan                  | Autorisatie-bypass               |
| DAO layer                 | Queries zijn veilig en parameterized                               | Injection / datalek              |
| Databaseverbinding        | Database is alleen intern bereikbaar                               | Datalek of dataverlies           |
| Logging                   | Logs bevatten geen gevoelige patiëntdata                           | Privacy-lek via logbestanden     |

---

## 8. Belangrijkste threats na update

| ID  | STRIDE                   | Threat                                                                    | Betrokken onderdeel     | CIA/BIV-impact                  | Niveau  |
| --- | ------------------------ | ------------------------------------------------------------------------- | ----------------------- | ------------------------------- | ------- |
| T1  | Spoofing                 | Aanvaller gebruikt account of sessie van zorgmedewerker                   | Browser / sessie        | Vertrouwelijkheid               | Hoog    |
| T2  | Information Disclosure   | Gebruiker bekijkt afspraken van onbevoegde patiënt                        | REST / Web UI           | Vertrouwelijkheid               | Hoog    |
| T3  | Tampering                | Afspraak wordt aangemaakt, aangepast of geannuleerd zonder juiste rechten | Appointment endpoints   | Integriteit                     | Kritiek |
| T4  | Elevation of Privilege   | Niet-admin wijzigt module-instellingen                                    | AppointmentSettingsForm | Integriteit / Beschikbaarheid   | Kritiek |
| T5  | Repudiation              | Wijzigingen zijn onvoldoende herleidbaar                                  | Web UI / service layer  | Integriteit                     | Hoog    |
| T6  | Denial of Service        | API, planning of database raakt overbelast                                | API / Database          | Beschikbaarheid                 | Hoog    |
| T7  | Injection                | Onveilige input richting query/filterfunctionaliteit                      | DAO / zoekfuncties      | Vertrouwelijkheid / Integriteit | Middel  |
| T8  | Software/Dependency Risk | Kwetsbare dependencies of module deployment                               | Maven / OMOD            | Vertrouwelijkheid / Integriteit | Middel  |

---

## 9. Risicobeoordeling

Risico wordt bepaald met:

**Risico = Kans × Impact**

| Score | Niveau  | Betekenis                             |
| ----: | ------- | ------------------------------------- |
|   1–4 | Laag    | Acceptabel risico                     |
|   5–9 | Middel  | Monitoren en verbeteren waar mogelijk |
| 10–15 | Hoog    | Maatregel verplicht                   |
| 16–25 | Kritiek | Direct oplossen of mitigeren          |

| ID  | Threat                                     | Kans | Impact | Score | Niveau  |
| --- | ------------------------------------------ | ---: | -----: | ----: | ------- |
| T1  | Account- of sessiemisbruik                 |    3 |      5 |    15 | Hoog    |
| T2  | Onbevoegde inzage in patiëntafspraken      |    4 |      5 |    20 | Kritiek |
| T3  | Onbevoegd wijzigen/annuleren van afspraken |    4 |      5 |    20 | Kritiek |
| T4  | Niet-admin wijzigt module-instellingen     |    3 |      5 |    15 | Hoog    |
| T5  | Onvoldoende audit trail                    |    3 |      4 |    12 | Hoog    |
| T6  | Overbelasting van API/database             |    3 |      4 |    12 | Hoog    |
| T7  | Injection via zoek/filterinput             |    2 |      5 |    10 | Hoog    |
| T8  | Kwetsbare dependency of module deployment  |    2 |      4 |     8 | Middel  |

---

## 10. Conclusie

De grootste risico’s binnen de Appointment Scheduling Module zitten bij de ingangen waarmee afspraken worden bekeken, aangemaakt, gewijzigd of geannuleerd. Deze entry points verwerken patiëntgegevens en beïnvloeden het zorgproces.

De attack surface mapping heeft vooral de risico’s rond **object-level autorisatie**, **endpoint-level privilege checks**, **audit logging** en **inputvalidatie** aangescherpt. De belangrijkste verbeteringen zijn daarom:

1. Controleer privileges per endpoint.
2. Voeg object-level autorisatie toe voor patiënt, afspraak, provider en locatie.
3. Log create/update/delete/settings-acties als audit events.
4. Valideer input server-side.
5. Beperk toegang tot database, instellingen en modulebeheer volgens least privilege.
