# Attack Surface Mapping – OpenMRS Appointment Scheduling Module

## 1. Inleiding

Dit document beschrijft de attack surface van de **OpenMRS Appointment Scheduling Module**. De attack surface bestaat uit alle punten waarop een gebruiker, API-client of ander systeem input kan leveren aan de module of output kan ophalen uit de module.

De analyse is uitgevoerd als aanvulling op het bestaande threat model. Het doel is om alle ingangen te identificeren, high risk ingangen te markeren en vast te leggen welke onderdelen impliciet worden vertrouwd.

---

## 2. Scope

Binnen scope vallen:

- REST API endpoints van de Appointment Scheduling Module
- Web controllers en formulieren
- Service- en DAO-laag
- Database-interactie
- Module-instellingen
- Dependencies en OMOD deployment op hoofdlijnen

Buiten scope vallen:

- Volledige OpenMRS Core security
- Serverbeheer buiten de module
- Netwerkconfiguratie buiten de OpenMRS-installatie

---

## 3. Methode

De attack surface is in kaart gebracht door te zoeken naar:

- REST resources
- Web controllers
- `@RequestMapping`
- Formulieren en queryparameters
- Service methods
- DAO/database-toegang
- Configuratiebestanden en dependencies

Per ingang is gekeken naar:

- Welke input wordt verwerkt?
- Is er authenticatie?
- Is er autorisatie of privilegecontrole?
- Is server-side validatie aanwezig?
- Worden gevoelige gegevens verwerkt?
- Is de ingang high risk?

---

## 4. Entry Points Inventory

### 4.1 REST API endpoints

Base path:

`/rest/v1/appointmentscheduling/*`

| #   | Endpoint                        | Acties                 | Belangrijke input                               | Vereiste privilege             | Inputvalidatie aanwezig? | Autorisatiecheck aanwezig?  | Risico  |
| --- | ------------------------------- | ---------------------- | ----------------------------------------------- | ------------------------------ | ------------------------ | --------------------------- | ------- |
| 1   | `/appointment`                  | GET, POST, PUT, DELETE | patient, timeSlot, status, reason, cancelReason | View/Schedule Appointments     | Deels / controleren      | Service-layer / controleren | Kritiek |
| 2   | `/appointmentallowingoverbook`  | POST/CRUD              | afspraakdata + overbooking                      | Schedule Appointments          | Deels / controleren      | Service-layer / controleren | Kritiek |
| 3   | `/createappointment`            | POST                   | patient, appointmentType, provider, timeSlot    | Schedule Appointments          | Deels / controleren      | Service-layer / controleren | Kritiek |
| 4   | `/appointmentrequest`           | GET, POST, PUT, DELETE | patient, requestedBy, notes, status             | Request Appointments           | Deels / controleren      | Service-layer / controleren | Kritiek |
| 5   | `/appointmentstatushistory`     | GET                    | appointment/status history                      | View Appointments              | n.v.t. / beperkt         | Service-layer / controleren | Hoog    |
| 6   | `/appointmentblock`             | GET, POST, PUT, DELETE | startDate, endDate, provider, location          | Manage Appointment Blocks      | Deels / controleren      | Service-layer / controleren | Hoog    |
| 7   | `/appointmentblockwithtimeslot` | GET, POST, PUT, DELETE | block + timeslot data                           | Manage Appointment Blocks      | Deels / controleren      | Service-layer / controleren | Hoog    |
| 8   | `/providerschedule`             | GET, POST, PUT, DELETE | provider, location, date/time                   | Manage Provider Schedules      | Deels / controleren      | Service-layer / controleren | Hoog    |
| 9   | `/timeslot`                     | GET, POST, PUT, DELETE | startDate, endDate, appointmentBlock            | View/Manage Appointment Blocks | Deels / controleren      | Service-layer / controleren | Middel  |
| 10  | `/appointmenttype`              | GET, POST, PUT, DELETE | name, duration, description                     | Manage Appointment Types       | Deels / controleren      | Service-layer / controleren | Middel  |
| 11  | `/dailyappointmentcount`        | GET                    | date, provider, location, status                | View Appointments              | Deels / controleren      | Controleren                 | Middel  |
| 12  | `/appointmentstatus`            | GET                    | enum/status                                     | View Appointments              | n.v.t.                   | Controleren                 | Laag    |
| 13  | `/appointmentstatustype`        | GET                    | enum/status type                                | View Appointments              | n.v.t.                   | Controleren                 | Laag    |
| 14  | `/appointmentrequeststatus`     | GET                    | enum/request status                             | Request/View Appointments      | n.v.t.                   | Controleren                 | Laag    |
| 15  | `/timeframeunits`               | GET                    | enum/timeframe units                            | View Appointments              | n.v.t.                   | Controleren                 | Laag    |

### Bevinding REST API

De REST API bevat meerdere high risk endpoints, vooral bij `POST`, `PUT` en `DELETE`. Deze endpoints verwerken patiëntafspraken, afspraakverzoeken, tijdsloten en roosters. Er is geen duidelijke endpoint-level autorisatie zoals `@PreAuthorize` zichtbaar in de REST-laag. De module lijkt vooral te vertrouwen op OpenMRS-authenticatie en service-layer privileges.

Dit is niet automatisch fout, maar moet wel gecontroleerd worden. Vooral bij patiëntgegevens is naast een algemene privilegecheck ook **object-level autorisatie** nodig. Een gebruiker met afspraakrechten mag niet automatisch elke afspraak of elke patiënt bekijken of wijzigen.

---

### 4.2 Web controller endpoints

Base path:

`/module/appointmentscheduling/*`

| #   | Endpoint                               | Methode | Functie                             | Vereiste privilege                  | Inputvalidatie aanwezig? | Autorisatiecheck aanwezig?                | Risico  |
| --- | -------------------------------------- | ------- | ----------------------------------- | ----------------------------------- | ------------------------ | ----------------------------------------- | ------- |
| 1   | `/appointmentForm`                     | GET     | Afspraakformulier laden             | View Appointments                   | Beperkt                  | `Context.isAuthenticated()` / controleren | Middel  |
| 2   | `/appointmentForm`                     | POST    | Afspraak opslaan/wijzigen           | Schedule Appointments               | Deels / controleren      | `Context.isAuthenticated()` / controleren | Kritiek |
| 3   | `/appointmentList`                     | GET     | Afspraken tonen                     | View Appointments                   | Beperkt                  | `Context.isAuthenticated()` / controleren | Hoog    |
| 4   | `/appointmentList`                     | POST    | Bulk-acties op afspraken            | Schedule Appointments               | Deels / controleren      | `Context.isAuthenticated()` / controleren | Hoog    |
| 5   | `/appointmentBlockForm`                | GET     | Blokformulier laden                 | View Appointment Blocks             | Beperkt                  | `Context.isAuthenticated()` / controleren | Middel  |
| 6   | `/appointmentBlockForm`                | POST    | Blok opslaan/wijzigen               | Manage Appointment Blocks           | Deels / controleren      | `Context.isAuthenticated()` / controleren | Hoog    |
| 7   | `/appointmentBlockList`                | GET     | Blokken tonen                       | View Appointment Blocks             | Beperkt                  | `Context.isAuthenticated()` / controleren | Middel  |
| 8   | `/appointmentBlockList`                | POST    | Bulk-acties op blokken              | Manage Appointment Blocks           | Deels / controleren      | `Context.isAuthenticated()` / controleren | Hoog    |
| 9   | `/appointmentBlockCalendar`            | GET     | Kalender tonen                      | View Appointment Blocks             | Beperkt                  | `Context.isAuthenticated()` / controleren | Middel  |
| 10  | `/appointmentBlockCalendar`            | POST    | Kalender/blokken aanpassen          | Manage Appointment Blocks           | Deels / controleren      | `Context.isAuthenticated()` / controleren | Hoog    |
| 11  | `/appointmentTypeForm`                 | GET     | Afspraaktypeformulier laden         | Manage Appointment Types            | Beperkt                  | `Context.isAuthenticated()` / controleren | Laag    |
| 12  | `/appointmentTypeForm`                 | POST    | Afspraaktype opslaan/wijzigen       | Manage Appointment Types            | Deels / controleren      | `Context.isAuthenticated()` / controleren | Hoog    |
| 13  | `/appointmentTypeList`                 | GET     | Afspraaktypes tonen                 | View Appointment Types              | n.v.t. / beperkt         | `Context.isAuthenticated()` / controleren | Laag    |
| 14  | `/appointmentSettingsForm`             | GET     | Instellingen tonen                  | Manage Appointment Settings / Admin | Beperkt                  | `Context.isAuthenticated()` / controleren | Hoog    |
| 15  | `/appointmentSettingsForm`             | POST    | Instellingen wijzigen               | Manage Appointment Settings / Admin | Deels / controleren      | `Context.isAuthenticated()` / controleren | Kritiek |
| 16  | `/appointmentStatisticsForm`           | GET     | Statistieken tonen                  | View Appointment Statistics         | Beperkt                  | `Context.isAuthenticated()` / controleren | Middel  |
| 17  | `/appointmentStatisticsForm`           | POST    | Statistieken filteren               | View Appointment Statistics         | Deels / controleren      | `Context.isAuthenticated()` / controleren | Middel  |
| 18  | `/patientDashboardAppointmentExt.form` | GET     | Afspraken op patiëntdashboard tonen | View Appointments                   | Beperkt                  | `Context.isAuthenticated()` / controleren | Hoog    |

### Bevinding Web UI

De webcontrollers verwerken veel gebruikersinput via formulieren. Vooral POST-acties zijn high risk, omdat hiermee afspraken, blokken, afspraaktypes of instellingen gewijzigd kunnen worden. Bij meerdere controllers lijkt minimaal authenticatie aanwezig te zijn, maar het moet per controller gecontroleerd worden of ook de juiste OpenMRS privilege wordt afgedwongen.

De belangrijkste aandachtspunten zijn:

1. **`POST /appointmentForm`**: afspraak aanmaken of wijzigen.
2. **`POST /appointmentSettingsForm`**: globale module-instellingen wijzigen.
3. **`POST /appointmentBlockForm`**: beschikbaarheid van zorgverleners aanpassen.
4. **`GET /patientDashboardAppointmentExt.form`**: patiëntafspraken tonen.
5. **Bulk-acties** op afspraken of blokken.

Voor deze endpoints is object-level autorisatie nodig. De module moet controleren of de gebruiker toegang heeft tot de specifieke patiënt, afspraak, provider of locatie.

---

## 5. High Risk Entry Points

| #   | Entry point                         | Waarom high risk?                   | Mogelijke threat              |
| --- | ----------------------------------- | ----------------------------------- | ----------------------------- |
| 1   | `POST /appointment`                 | Maakt afspraak aan voor patiënt     | Spoofing / Tampering          |
| 2   | `PUT /appointment`                  | Wijzigt bestaande afspraak          | Tampering                     |
| 3   | `DELETE /appointment`               | Annuleert of void afspraak          | Tampering / Repudiation       |
| 4   | `GET /appointment?patient=X`        | Kan afspraken van patiënt tonen     | Information Disclosure / IDOR |
| 5   | `POST /appointmentrequest`          | Maakt afspraakverzoek aan           | Spoofing / Tampering          |
| 6   | `POST /appointmentallowingoverbook` | Staat overboeking toe               | DoS / Tampering               |
| 7   | `POST /appointmentBlockForm`        | Wijzigt beschikbaarheid             | Tampering / DoS               |
| 8   | `POST /providerschedule`            | Wijzigt providerrooster             | Tampering                     |
| 9   | `POST /appointmentTypeForm`         | Wijzigt afspraaktypes               | Misconfiguration              |
| 10  | `POST /appointmentSettingsForm`     | Wijzigt globale module-instellingen | Elevation of Privilege        |

---

## 6. Trust Boundaries

| Trust boundary              | Wat wordt vertrouwd?                                     | Risico                                         |
| --------------------------- | -------------------------------------------------------- | ---------------------------------------------- |
| Browser → OpenMRS Web UI    | Gebruiker wijzigt request niet handmatig                 | Parameter tampering                            |
| API-client → REST API       | API-client is bevoegd                                    | Onbevoegde API-acties                          |
| Web UI/REST → Service layer | Service layer controleert privileges                     | Autorisatie-bypass als dit niet overal gebeurt |
| Service layer → DAO         | DAO krijgt veilige input                                 | Injection of ongewenste query                  |
| Module → OpenMRS Core       | OpenMRS Core regelt login, sessies en privileges correct | Spoofing / privilege escalation                |
| Module → Database           | Database is alleen intern bereikbaar                     | Datalek                                        |
| Module → Logs               | Logs bevatten geen gevoelige patiëntdata                 | Privacy-lek                                    |
| Module → Dependencies       | Libraries zijn veilig en up-to-date                      | Supply-chain risico                            |

---

## 7. Impliciete trust-aannames

De module vertrouwt impliciet op de volgende aannames:

1. Een ingelogde OpenMRS-gebruiker is correct geauthenticeerd.
2. OpenMRS privileges worden in de service layer correct afgedwongen.
3. Gebruikers kunnen geen verborgen velden of ID’s misbruiken.
4. Een gebruiker met algemene afspraakrechten mag niet automatisch elke patiëntafspraak bekijken of wijzigen.
5. REST-clients sturen alleen toegestane requests.
6. DAO-methodes gebruiken veilige Hibernate/parameterized queries.
7. Logs bevatten geen patiëntnamen, medische inhoud, BSN of sessietokens.
8. De database is niet extern bereikbaar.

De belangrijkste aanname die extra gecontroleerd moet worden is **object-level autorisatie**: de module moet controleren of een gebruiker toegang heeft tot de specifieke patiënt, afspraak, provider of locatie.

---

## 8. Input Validation Gaps

| Onderdeel                                                 | Bevinding                                               | Risico                    | Aanbevolen maatregel                          |
| --------------------------------------------------------- | ------------------------------------------------------- | ------------------------- | --------------------------------------------- |
| Vrije tekstvelden zoals `reason`, `cancelReason`, `notes` | Validatie moet gecontroleerd worden                     | XSS / privacy-lek in logs | Lengte beperken, output encoding, niet loggen |
| Datum- en tijdvelden                                      | Vaak alleen type/format parsing                         | Ongeldige planning of DoS | Rangevalidatie toevoegen                      |
| ID’s zoals `patientId`, `appointmentId`, `providerId`     | Kunnen handmatig aangepast worden                       | IDOR/BOLA                 | Object-level autorisatie                      |
| Statusvelden                                              | Enum-validatie helpt, maar businessregels blijven nodig | Onjuiste statusovergang   | Statusovergangen server-side controleren      |
| Bulk-acties                                               | Kunnen veel data tegelijk wijzigen                      | DoS / massale fout        | Limieten en logging toevoegen                 |

---

## 9. Audit Logging Gaps

Voor NEN-7510 is auditlogging belangrijk, vooral bij acties op patiëntdata.

| Actie                     | Gewenste auditlog                                                   | Status                   |
| ------------------------- | ------------------------------------------------------------------- | ------------------------ |
| Afspraak aanmaken         | User-ID, timestamp, appointment UUID, patient UUID, SUCCESS/FAILURE | Controleren              |
| Afspraak wijzigen         | User-ID, timestamp, gewijzigde afspraak, actie                      | Controleren              |
| Afspraak annuleren/voiden | User-ID, timestamp, reden, appointment UUID                         | Deels aanwezig           |
| Statuswijziging           | Status history met user en tijd                                     | Aanwezig / controleren   |
| Instellingen wijzigen     | User-ID, timestamp, oude/nieuwe waarde                              | Ontbreekt of onduidelijk |
| Bulk-acties               | User-ID, aantal records, actie, uitkomst                            | Ontbreekt of onduidelijk |

Logs mogen geen gevoelige inhoud bevatten zoals medische details, volledige patiëntnamen, BSN, wachtwoorden of sessietokens.

---

## 10. Koppeling met STRIDE

| Entry point                 | STRIDE-risico                                            | Uitleg                                                 |
| --------------------------- | -------------------------------------------------------- | ------------------------------------------------------ |
| Appointment CRUD            | Spoofing, Tampering, Repudiation, Information Disclosure | Verwerkt patiëntafspraken                              |
| Appointment Request         | Spoofing, Tampering, Information Disclosure              | Kan verzoeken voor patiënten bevatten                  |
| Appointment Settings        | Elevation of Privilege, Tampering, DoS                   | Instellingen beïnvloeden werking module                |
| Appointment Blocks          | Tampering, DoS                                           | Beschikbaarheid van afspraken kan worden gemanipuleerd |
| Provider Schedule           | Tampering                                                | Roosters van zorgverleners kunnen worden beïnvloed     |
| Patient Dashboard Extension | Information Disclosure                                   | Toont afspraken bij patiënt                            |
| DAO/Search                  | Injection, Information Disclosure                        | Verwerkt zoek- en filterparameters                     |
| Dependencies/OMOD           | Software Integrity Failure                               | Kwetsbare of aangepaste code kan worden geladen        |

---

## 11. Bijwerking threat model

Door de attack surface mapping zijn de volgende wijzigingen toegevoegd aan het threat model:

| Threat                 | Update door attack surface mapping                            |
| ---------------------- | ------------------------------------------------------------- |
| Spoofing               | Meer aandacht voor sessiemisbruik en REST API-gebruik         |
| Tampering              | Verhoogd risico bij appointment create/update/delete          |
| Repudiation            | Extra aandacht voor auditlogging bij settings en bulk-acties  |
| Information Disclosure | Object-level autorisatie bij patiëntafspraken toegevoegd      |
| Denial of Service      | Overbooking, bulk-acties en planningmanipulatie toegevoegd    |
| Elevation of Privilege | AppointmentSettingsForm als high risk ingang toegevoegd       |
| Injection              | Query/filterinput toegevoegd als aandachtspunt                |
| Software Integrity     | Dependencies en OMOD deployment toegevoegd als attack surface |

---

## 12. Aanbevolen maatregelen

| Prioriteit | Maatregel                                                                     |
| ---------- | ----------------------------------------------------------------------------- |
| Kritiek    | Voeg expliciete privilegechecks toe op high risk POST/PUT/DELETE-acties       |
| Kritiek    | Voeg object-level autorisatie toe voor patiënt, afspraak, provider en locatie |
| Hoog       | Voeg auditlogging toe voor create/update/delete/settings-acties               |
| Hoog       | Valideer alle input server-side                                               |
| Hoog       | Beperk bulk-acties en voeg limieten toe                                       |
| Middel     | Controleer dependencies met SCA en SBOM                                       |
| Middel     | Controleer dat database en adminfuncties niet extern bereikbaar zijn          |
| Middel     | Test high risk entry points met unit tests en security tests                  |

---

## 12. Aanbevolen maatregelen

| Prioriteit | Maatregel                                                                     |
| ---------- | ----------------------------------------------------------------------------- |
| Kritiek    | Voeg expliciete privilegechecks toe op high risk POST/PUT/DELETE-acties       |
| Kritiek    | Voeg object-level autorisatie toe voor patiënt, afspraak, provider en locatie |
| Hoog       | Voeg auditlogging toe voor create/update/delete/settings-acties               |
| Hoog       | Valideer alle input server-side                                               |
| Hoog       | Beperk bulk-acties en voeg limieten toe                                       |
| Middel     | Controleer dependencies met SCA en SBOM                                       |
| Middel     | Controleer dat database en adminfuncties niet extern bereikbaar zijn          |
| Middel     | Test high risk entry points met unit tests en security tests                  |

---

## 13. Conclusie

De grootste attack surface van de Appointment Scheduling Module zit bij de REST API, webformulieren en instellingenpagina’s. Vooral acties waarmee afspraken worden bekeken, aangemaakt, gewijzigd of geannuleerd zijn high risk, omdat ze patiëntgegevens verwerken en direct invloed hebben op het zorgproces.

De belangrijkste risico’s zijn:

1. Onvoldoende object-level autorisatie.
2. Onvoldoende zichtbare endpoint-level privilegecontrole.
3. Onvoldoende auditlogging bij wijzigingen.
4. Onvoldoende server-side inputvalidatie.
5. Misbruik van settings, bulk-acties of overbooking.

Deze bevindingen zijn verwerkt in het bijgewerkte threat model.
