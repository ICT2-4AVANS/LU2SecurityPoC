# Traceability Matrix — OpenMRS Appointment Scheduling Module

## 1. Doel

Deze traceability matrix legt vast hoe de gekozen NEN-7510:2024 controls zijn terug te leiden naar concrete artefacten binnen het LU2 Security PoC-project.

Per control wordt vastgelegd:

- welke pentestbevinding of threat erbij hoort;
- waar de bevinding is beschreven;
- welk fix-document erbij hoort;
- welke Pull Request de wijziging bevat;
- welk test- of bewijsartefact beschikbaar is;
- welke onderdelen nog open, geaccepteerd of niet uitgevoerd zijn.

Hiermee wordt aangetoond dat de securitywerkzaamheden uit Sprint 4 traceerbaar zijn vastgelegd.

## 2. Scope

Project: OpenMRS Appointment Scheduling Module
Repository: `ICT2-4AVANS/LU2SecurityPoC`
Branch: `dev`
Projectperiode: vanaf sprintweek 1 / lesweek 6 tot en met sprintweek 4 / lesweek 8
Auditrapport opgesteld: sprintweek 4 / lesweek 8
Bestand: `docs/auditreport/traceability-matrix.md`

Deze matrix richt zich op de volgende NEN-7510:2024 controls:

| Control | Onderwerp             | Reden van selectie                                                                                                                                  |
| ------- | --------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| A.8.3   | Toegangsbeveiliging   | De module verwerkt patiëntafspraken en moet voorkomen dat gebruikers afspraken of beheerfuncties kunnen benaderen waarvoor zij geen rechten hebben. |
| A.8.5   | Authenticatie         | Credentials, sessies en CSRF-bescherming zijn belangrijk om te voorkomen dat accounts of sessies worden misbruikt.                                  |
| A.8.15  | Logging en monitoring | De module verwerkt patiëntgegevens. Logs moeten beveiligingsrelevante acties vastleggen, maar mogen geen onnodige PII bevatten.                     |

Daarnaast is A.8.28 Veilig coderen aanvullend meegenomen bij kwetsbaarheden waarbij de oorzaak direct in onveilige code of onvoldoende invoerverwerking zat, zoals SQL Injection en trust boundary/inputvalidatie-problemen.

## 3. Traceability matrix

| NEN-7510 control                                      | Bevinding / threat                                                                                     | Eigenaar                   | Analyse-artefact                                                                                                                  | Fix-artefact                                              | PR / code-artefact | Test / bewijs                                                                                                                                                                               | Status                                  | Opmerking / restrisico                                                                                                                                                                                                                                                                      |
| ----------------------------------------------------- | ------------------------------------------------------------------------------------------------------ | -------------------------- | --------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------- | ------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| A.8.3 – Toegangsbeveiliging / A.8.28 – Veilig coderen | B-01 – SQL Injection in zoekfunctie in `HibernateAppointmentDAO.java`                                  | Nick                       | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/security-backlog.md`                                            | `docs/fixes/fix/b01-nick.md`                              | PR #49             | `HibernateAppointmentDAOSqlInjectionAbuseTest`, screenshot vóór fix, screenshot na fix, testuitkomst na fix                                                                                 | Opgelost                                | De kwetsbare HQL-string-concatenatie is vervangen door een geparametriseerde query met `:patientName` en `.setParameter(...)`.                                                                                                                                                              |
| A.8.3 – Toegangsbeveiliging                           | B-03 – IDOR: afspraken van andere patiënten via `appointmentId` in `AppointmentFormController.java`    | Rami                       | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/security-backlog.md`                                            | `docs/fixes/fix/b03-Rami.md`                              | PR #74             | Screenshot kwetsbare code vóór fix, unit test / abuse test vóór fix, screenshot fix, test afspraak zelfde patiënt toegestaan, test afspraak andere patiënt geblokkeerd, testuitkomst na fix | Opgelost                                | Na het ophalen van een afspraak wordt gecontroleerd of de afspraak hoort bij de meegegeven `patientId`. AppointmentId-manipulatie tussen patiënten wordt geblokkeerd met een `APIAuthenticationException`.                                                                                  |
| A.8.3 – Toegangsbeveiliging                           | B-04 – Privilege escalation via directe URL’s / ontbrekende `@Authorized`-annotaties in 13 controllers | Nick                       | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/security-backlog.md`                                            | `docs/fixes/fix/b04-nick.md`                              | PR #52             | `ControllerAuthorizationTest`, screenshot low-privilege gebruiker, screenshot runtime-test, screenshot code vóór/na fix                                                                     | Opgelost                                | Controllers zijn voorzien van expliciete `@Authorized`-annotaties met passende privilege-constanten.                                                                                                                                                                                        |
| A.8.3 – Toegangsbeveiliging / A.8.28 – Veilig coderen | B-10 – Trust boundary violation in `AppointmentBlockListController.java`                               | Nick                       | `docs/auditreport/05-pentest-bevindingen.md` en CodeQL-bevinding                                                                  | `docs/fixes/fix/b10-11-Nick.md`                           | PR #57             | CodeQL-screenshot, screenshot kwetsbare code, screenshot gevalideerde input, `TrustBoundaryValidationTest`                                                                                  | Opgelost                                | Requestparameters worden niet meer direct vertrouwd, maar eerst gevalideerd voordat ze worden gebruikt of opgeslagen in de sessie.                                                                                                                                                          |
| A.8.3 – Toegangsbeveiliging / A.8.28 – Veilig coderen | B-11 – Trust boundary violation in `AppointmentBlockCalendarController.java`                           | Nick                       | `docs/auditreport/05-pentest-bevindingen.md` en CodeQL-bevinding                                                                  | `docs/fixes/fix/b10-11-Nick.md`                           | PR #57             | CodeQL-screenshot, screenshot kwetsbare code, screenshot gevalideerde input, `TrustBoundaryValidationTest`                                                                                  | Opgelost                                | Zelfde patroon als B-10: requestinput wordt eerst gevalideerd voordat deze in controllerlogica of sessieopslag wordt gebruikt.                                                                                                                                                              |
| A.8.5 – Authenticatie                                 | B-02 – Hardcoded credentials in `AppointmentActivator.java`                                            | Amine                      | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/security-backlog.md`                                            | `docs/fixes/fix/b02-amine.md`                             | PR #51             | Screenshot vóór fix, screenshot na fix, testbewijs in fix-document                                                                                                                          | Opgelost, met restrisico                | Credentials zijn uit de broncode verwijderd en vervangen door Global Properties. Restrisico: wachtwoordrotatie en eventuele git-historieschoning moeten nog worden bevestigd.                                                                                                               |
| A.8.5 – Authenticatie                                 | B-07 – Ontbrekende CSRF-beveiliging en sessie-hardening in DWR-laag                                    | Amine                      | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/security-backlog.md`                                            | `docs/fixes/fix/b07-amine.md`                             | PR #55             | Screenshot cookie flags, screenshot kwetsbare DWR-code, CSRF-testbewijs in fix-document                                                                                                     | Opgelost, met restrisico                | CSRF-controle is toegevoegd aan de DWR-service. Cookie flags en bredere session-rotatie blijven deels afhankelijk van OpenMRS core/Tomcat-configuratie.                                                                                                                                     |
| A.8.15 – Logging en monitoring                        | B-05 – PII gelogd in audit log in `AppointmentServiceImpl.java`                                        | Rami                       | `docs/auditreport/05-pentest-bevindingen.md`, `docs/auditreport/02gapanalyselogging.md` en `docs/auditreport/security-backlog.md` | `docs/fixes/fix/b05-Rami.md`                              | PR #75             | Screenshot kwetsbare logregel vóór fix, `git grep`-controle op `getAppointmentsForPatientWithLogging`, screenshot verwijderde methode / code na fix                                         | Opgelost                                | De methode `getAppointmentsForPatientWithLogging` is volledig verwijderd. Hierdoor kunnen patiëntnaam, geboortedatum, patiëntidentifier en geslacht niet meer via deze audit-logregel in `AppointmentServiceImpl.java` terechtkomen.                                                        |
| A.8.15 – Logging en monitoring                        | B-06 – Ontbrekende logging van auth-events                                                             | Team / platformafhankelijk | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/02gapanalyselogging.md`                                         | Geen fix-document                                         | Geen PR            | Analysebewijs in auditdocumentatie                                                                                                                                                          | Doorgeschoven / geaccepteerd restrisico | Deze bevinding is niet technisch opgelost binnen deze sprint. Het risico is lager geprioriteerd dan de kritieke en hoge bevindingen die direct toegang tot patiëntgegevens of autorisatieproblemen raakten. Daarnaast valt volledige auth-event logging deels onder OpenMRS core en Tomcat. |
| A.8.15 – Logging en monitoring                        | B-08 – PII via debug-logging in `AppointmentDataSetEvaluator.java`                                     | Rami                       | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/02gapanalyselogging.md`                                         | Nog toe te voegen, verwacht: `docs/fixes/fix/b08-rami.md` | Nog toe te voegen  | Nog toe te voegen                                                                                                                                                                           | Open / in uitvoering                    | Deze bevinding wordt nog afgerond. De fix moet aantonen dat debug-logging geen patiëntgerelateerde waarden of indirect herleidbare PII meer logt.                                                                                                                                           |
| A.8.28 – Veilig coderen                               | B-09 – XSS via eval-achtige DOM-functie                                                                | Team                       | CodeQL-bevinding en `docs/auditreport/security-backlog.md`                                                                        | Geen fix-document                                         | Geen PR            | Analysebewijs / CodeQL-bevinding                                                                                                                                                            | Niet uitgevoerd / doorgeschoven         | Deze bevinding is niet technisch opgelost binnen deze sprint. B-09 had een lagere prioriteit dan onder andere B-10 en B-11, waarbij requestparameters direct over een trust boundary werden verwerkt. Daarom is B-09 doorgeschoven naar een volgende sprint.                                |

## 4. Niet uitgevoerde of niet volledig afgeronde items

| Item                                               | Control | Status                                  | Onderbouwing                                                                                                                                                                                                                                                                                                                                                                                                    |
| -------------------------------------------------- | ------- | --------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| B-08 – PII via debug-logging                       | A.8.15  | Open / in uitvoering                    | Deze bevinding wordt nog afgerond door Rami. Na afronding moet worden aangetoond dat debug-logs geen patiëntgerelateerde waarden of indirect herleidbare PII meer bevatten. Daarna moeten fix-document, PR-nummer en testbewijs worden toegevoegd.                                                                                                                                                              |
| B-06 – Ontbrekende logging van auth-events         | A.8.15  | Doorgeschoven / geaccepteerd restrisico | B-06 had een lager risico dan de kritieke en hoge bevindingen die binnen deze sprint zijn opgelost. Het risico blijft dat beveiligingsrelevante gebeurtenissen, zoals verdachte toegangspogingen of geweigerde acties, mogelijk nog niet volledig worden vastgelegd. Omdat dit ook deels afhankelijk is van OpenMRS core en Tomcat, is dit vastgelegd als restrisico en doorgeschoven naar een volgende sprint. |
| B-09 – XSS via eval-achtige DOM-functie            | A.8.28  | Niet uitgevoerd / doorgeschoven         | B-09 had een lagere prioriteit dan de bevindingen B-10 en B-11. Bij B-10 en B-11 werden requestparameters direct over een trust boundary verwerkt, waardoor deze binnen de sprint meer prioriteit kregen. Het restrisico is dat de mogelijke XSS-gerelateerde kwetsbaarheid aanwezig blijft totdat de betreffende JavaScript-/DOM-code verder is gecontroleerd en aangepast.                                    |
| Git-historie en wachtwoordrotatie na B-02          | A.8.5   | Nog te bevestigen                       | De hardcoded credentials zijn uit de code verwijderd, maar credentials die eerder in de repository stonden moeten als gecompromitteerd worden beschouwd. Wachtwoordrotatie en eventuele historieschoning moeten nog worden bevestigd.                                                                                                                                                                           |
| Cookie flags en volledige sessie-hardening na B-07 | A.8.5   | Gedeeltelijk restrisico                 | De DWR CSRF-fix is uitgevoerd, maar sommige sessiebeveiligingsmaatregelen zoals cookie flags en session-rotatie zijn afhankelijk van OpenMRS core of Tomcat-configuratie.                                                                                                                                                                                                                                       |

## 5. Samenvatting per NEN-control

### A.8.3 – Toegangsbeveiliging

Deze control is gekoppeld aan bevindingen waarbij onbevoegde toegang tot afspraakgegevens of beheerfunctionaliteit mogelijk was.

| Bevinding                            | Status   |
| ------------------------------------ | -------- |
| B-01 SQL Injection                   | Opgelost |
| B-03 IDOR afspraken andere patiënten | Opgelost |
| B-04 Privilege escalation            | Opgelost |
| B-10 Trust boundary violation        | Opgelost |
| B-11 Trust boundary violation        | Opgelost |

De A.8.3-bevindingen zijn grotendeels opgelost. Voor B-01, B-03, B-04, B-10 en B-11 is fix- en testbewijs aanwezig. Dit verlaagt het risico op ongeautoriseerde toegang tot afspraak- en patiëntgerelateerde gegevens.

### A.8.5 – Authenticatie

Deze control is gekoppeld aan credentials, sessies en CSRF-bescherming.

| Bevinding                    | Status                   |
| ---------------------------- | ------------------------ |
| B-02 Hardcoded credentials   | Opgelost, met restrisico |
| B-07 CSRF / sessie-hardening | Opgelost, met restrisico |

De directe codeproblemen zijn opgelost. Er blijven nog platformafhankelijke of organisatorische restrisico’s over, zoals wachtwoordrotatie, git-historieschoning en volledige session-hardening.

### A.8.15 – Logging en monitoring

Deze control is gekoppeld aan logging, monitoring en het voorkomen van PII in logs.

| Bevinding                                | Status                                  |
| ---------------------------------------- | --------------------------------------- |
| B-05 PII gelogd in audit log             | Opgelost                                |
| B-06 Ontbrekende logging van auth-events | Doorgeschoven / geaccepteerd restrisico |
| B-08 PII via debug-logging               | Open / in uitvoering                    |

Voor A.8.15 is B-05 opgelost door de risicovolle methode met PII-logging volledig te verwijderen. B-08 wordt nog technisch afgerond. B-06 is vastgelegd als restrisico, omdat volledige auth-event logging deels buiten de module valt en een lagere prioriteit had dan de kritieke en hoge bevindingen die deze sprint zijn opgelost.

### A.8.28 – Veilig coderen

Deze control is aanvullend meegenomen bij kwetsbaarheden waarbij onveilige code of onvoldoende invoerverwerking de oorzaak was.

| Bevinding                             | Status                          |
| ------------------------------------- | ------------------------------- |
| B-01 SQL Injection                    | Opgelost                        |
| B-09 XSS via eval-achtige DOM-functie | Niet uitgevoerd / doorgeschoven |
| B-10 Trust boundary violation         | Opgelost                        |
| B-11 Trust boundary violation         | Opgelost                        |

A.8.28 is vooral gebruikt als aanvullende koppeling bij veilig coderen en inputvalidatie. B-09 is niet uitgevoerd, omdat B-10 en B-11 binnen Sprint 4 een hogere prioriteit hadden.

## 6. Artefactenoverzicht

| Artefact              | Pad / verwijzing                             | Doel                                                                                      |
| --------------------- | -------------------------------------------- | ----------------------------------------------------------------------------------------- |
| Pentestbevindingen    | `docs/auditreport/05-pentest-bevindingen.md` | Centrale lijst met bevindingen B-01 t/m B-11, status en NEN-koppeling.                    |
| Security backlog      | `docs/auditreport/security-backlog.md`       | Prioritering van security requirements en koppeling aan threats, bevindingen en controls. |
| Logging gap-analyse   | `docs/auditreport/02gapanalyselogging.md`    | Onderbouwing voor A.8.15 logging en monitoring.                                           |
| Threat model          | `docs/threadmodel/Threat-model.md`           | Onderbouwing van threats en risico’s die aan bevindingen zijn gekoppeld.                  |
| B-01 fixdocument      | `docs/fixes/fix/b01-nick.md`                 | Bewijs voor SQL Injection-fix.                                                            |
| B-02 fixdocument      | `docs/fixes/fix/b02-amine.md`                | Bewijs voor verwijderen hardcoded credentials.                                            |
| B-03 fixdocument      | `docs/fixes/fix/b03-Rami.md`                 | Bewijs voor IDOR-fix in `AppointmentFormController.java`.                                 |
| B-04 fixdocument      | `docs/fixes/fix/b04-nick.md`                 | Bewijs voor autorisatie op controllers.                                                   |
| B-05 fixdocument      | `docs/fixes/fix/b05-Rami.md`                 | Bewijs voor verwijderen van PII-logging uit `AppointmentServiceImpl.java`.                |
| B-07 fixdocument      | `docs/fixes/fix/b07-amine.md`                | Bewijs voor CSRF-fix in DWR-laag.                                                         |
| B-10/B-11 fixdocument | `docs/fixes/fix/b10-11-Nick.md`              | Bewijs voor trust boundary validation in AppointmentBlock controllers.                    |
| PR #49                | GitHub Pull Request #49                      | Code-artefact voor B-01.                                                                  |
| PR #51                | GitHub Pull Request #51                      | Code-artefact voor B-02.                                                                  |
| PR #52                | GitHub Pull Request #52                      | Code-artefact voor B-04.                                                                  |
| PR #55                | GitHub Pull Request #55                      | Code-artefact voor B-07.                                                                  |
| PR #57                | GitHub Pull Request #57                      | Code-artefact voor B-10 en B-11.                                                          |
| PR #74                | GitHub Pull Request #74                      | Code-artefact voor B-03.                                                                  |
| PR #75                | GitHub Pull Request #75                      | Code-artefact voor B-05.                                                                  |

## 7. Conclusie

Deze traceability matrix laat zien hoe de belangrijkste NEN-7510:2024 controls zijn gekoppeld aan concrete analyse-, fix- en testartefacten.

Voor A.8.3 zijn meerdere toegangsbeveiligingsproblemen opgelost, waaronder SQL Injection, IDOR, privilege escalation en trust boundary violations. Voor A.8.5 zijn hardcoded credentials en CSRF-risico’s aangepakt, met enkele restrisico’s die nog bevestigd moeten worden. Voor A.8.15 is B-05 opgelost door de risicovolle PII-loggingmethode te verwijderen. B-08 wordt nog technisch afgerond. B-06 is doorgeschoven en als restrisico vastgelegd, omdat volledige auth-event logging deels buiten de module valt en een lagere prioriteit had dan de kritieke en hoge bevindingen.

B-09 is niet uitgevoerd, omdat deze bevinding een lagere prioriteit had dan de hoger geprioriteerde risico’s die binnen Sprint 4 zijn opgepakt. Vooral B-10 en B-11 kregen prioriteit, omdat daar requestparameters direct over een trust boundary werden verwerkt. Dit is expliciet vastgelegd zodat ook niet-uitgevoerde en doorgeschoven items traceerbaar zijn.
