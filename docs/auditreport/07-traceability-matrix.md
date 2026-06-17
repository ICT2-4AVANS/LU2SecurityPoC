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
Sprint: Sprint 4 / lesweek 8
Bestand: `docs/auditreport/traceability-matrix.md`

Deze matrix richt zich op de volgende NEN-7510:2024 controls:

| Control | Onderwerp             | Reden van selectie                                                                                                                                  |
| ------- | --------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| A.8.3   | Toegangsbeveiliging   | De module verwerkt patiëntafspraken en moet voorkomen dat gebruikers afspraken of beheerfuncties kunnen benaderen waarvoor zij geen rechten hebben. |
| A.8.5   | Authenticatie         | Credentials, sessies en CSRF-bescherming zijn belangrijk om te voorkomen dat accounts of sessies worden misbruikt.                                  |
| A.8.15  | Logging en monitoring | De module verwerkt patiëntgegevens. Logs moeten beveiligingsrelevante acties vastleggen, maar mogen geen onnodige PII bevatten.                     |

## 3. Traceability matrix

| NEN-7510 control               | Bevinding / threat                                                                                     | Eigenaar                   | Analyse-artefact                                                                                                                  | Fix-artefact                                              | PR / code-artefact | Test / bewijs                                                                                                           | Status                   | Opmerking / restrisico                                                                                                                                                        |
| ------------------------------ | ------------------------------------------------------------------------------------------------------ | -------------------------- | --------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------- | ------------------ | ----------------------------------------------------------------------------------------------------------------------- | ------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| A.8.3 – Toegangsbeveiliging    | B-01 – SQL Injection in zoekfunctie in `HibernateAppointmentDAO.java`                                  | Nick                       | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/security-backlog.md`                                            | `docs/fixes/fix/b01-nick.md`                              | PR #49             | `HibernateAppointmentDAOSqlInjectionAbuseTest`, screenshot vóór fix, screenshot na fix, testuitkomst na fix             | Opgelost                 | De kwetsbare HQL-string-concatenatie is vervangen door een geparametriseerde query met `:patientName` en `.setParameter(...)`.                                                |
| A.8.3 – Toegangsbeveiliging    | B-03 – IDOR: afspraken van andere patiënten via `appointmentId`                                        | Rami                       | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/security-backlog.md`                                            | Nog toe te voegen, verwacht: `docs/fixes/fix/b03-rami.md` | Nog toe te voegen  | Nog toe te voegen                                                                                                       | Open / in uitvoering     | Deze bevinding moet nog worden opgelost. Na afronding moeten fix-document, PR-nummer en testbewijs worden toegevoegd.                                                         |
| A.8.3 – Toegangsbeveiliging    | B-04 – Privilege escalation via directe URL’s / ontbrekende `@Authorized`-annotaties in 13 controllers | Nick                       | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/security-backlog.md`                                            | `docs/fixes/fix/b04-nick.md`                              | PR #52             | `ControllerAuthorizationTest`, screenshot low-privilege gebruiker, screenshot runtime-test, screenshot code vóór/na fix | Opgelost                 | Controllers zijn voorzien van expliciete `@Authorized`-annotaties met passende privilege-constanten.                                                                          |
| A.8.3 – Toegangsbeveiliging    | B-10 – Trust boundary violation in `AppointmentBlockListController.java`                               | Nick                       | `docs/auditreport/05-pentest-bevindingen.md` en CodeQL-bevinding                                                                  | `docs/fixes/fix/b10-11-Nick.md`                           | PR #57             | CodeQL-screenshot, screenshot kwetsbare code, screenshot gevalideerde input, `TrustBoundaryValidationTest`              | Opgelost                 | Requestparameters worden niet meer direct vertrouwd, maar eerst gevalideerd voordat ze worden gebruikt of opgeslagen in de sessie.                                            |
| A.8.3 – Toegangsbeveiliging    | B-11 – Trust boundary violation in `AppointmentBlockCalendarController.java`                           | Nick                       | `docs/auditreport/05-pentest-bevindingen.md` en CodeQL-bevinding                                                                  | `docs/fixes/fix/b10-11-Nick.md`                           | PR #57             | CodeQL-screenshot, screenshot kwetsbare code, screenshot gevalideerde input, `TrustBoundaryValidationTest`              | Opgelost                 | Zelfde patroon als B-10: requestinput wordt eerst gevalideerd voordat deze in controllerlogica of sessieopslag wordt gebruikt.                                                |
| A.8.5 – Authenticatie          | B-02 – Hardcoded credentials in `AppointmentActivator.java`                                            | Amine                      | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/security-backlog.md`                                            | `docs/fixes/fix/b02-amine.md`                             | PR #51             | Screenshot vóór fix, screenshot na fix, testbewijs in fix-document                                                      | Opgelost, met restrisico | Credentials zijn uit de broncode verwijderd en vervangen door Global Properties. Restrisico: wachtwoordrotatie en eventuele git-historieschoning moeten nog worden bevestigd. |
| A.8.5 – Authenticatie          | B-07 – Ontbrekende CSRF-beveiliging en sessie-hardening in DWR-laag                                    | Amine                      | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/security-backlog.md`                                            | `docs/fixes/fix/b07-amine.md`                             | PR #55             | Screenshot cookie flags, screenshot kwetsbare DWR-code, CSRF-testbewijs in fix-document                                 | Opgelost, met restrisico | CSRF-controle is toegevoegd aan de DWR-service. Cookie flags en bredere session-rotatie blijven deels afhankelijk van OpenMRS core/Tomcat-configuratie.                       |
| A.8.15 – Logging en monitoring | B-05 – PII gelogd in audit log in `AppointmentServiceImpl.java`                                        | Rami                       | `docs/auditreport/05-pentest-bevindingen.md`, `docs/auditreport/02gapanalyselogging.md` en `docs/auditreport/security-backlog.md` | Nog toe te voegen, verwacht: `docs/fixes/fix/b05-rami.md` | Nog toe te voegen  | Nog toe te voegen                                                                                                       | Open / in uitvoering     | Deze bevinding moet nog worden opgelost. De fix moet aantonen dat namen, geboortedata, identifiers en geslacht niet meer plain text in logs terechtkomen.                     |
| A.8.15 – Logging en monitoring | B-06 – Ontbrekende logging van auth-events                                                             | Team / platformafhankelijk | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/02gapanalyselogging.md`                                         | Geen fix-document                                         | Geen PR            | Analysebewijs in auditdocumentatie                                                                                      | Geaccepteerd risico      | Auth-events vallen grotendeels onder OpenMRS core en Tomcat. Binnen de module is dit daarom niet volledig opgelost, maar als geaccepteerd risico vastgelegd.                  |
| A.8.15 – Logging en monitoring | B-08 – PII via debug-logging in `AppointmentDataSetEvaluator.java`                                     | Rami                       | `docs/auditreport/05-pentest-bevindingen.md` en `docs/auditreport/02gapanalyselogging.md`                                         | Nog toe te voegen, verwacht: `docs/fixes/fix/b08-rami.md` | Nog toe te voegen  | Nog toe te voegen                                                                                                       | Open / in uitvoering     | Deze bevinding moet nog worden opgelost. De fix moet aantonen dat debug-logging geen patiëntgerelateerde waarden of indirect herleidbare PII meer logt.                       |

## 4. Niet uitgevoerde of niet volledig afgeronde items

| Item                                               | Control | Status                   | Onderbouwing                                                                                                                                                                                                                                                                  |
| -------------------------------------------------- | ------- | ------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| B-09 – XSS via eval-achtige DOM-functie            | A.8.28  | Niet uitgevoerd          | B-09 had een lagere prioriteit dan de hoger geprioriteerde risico’s die in Sprint 4 zijn opgepakt. Binnen de beschikbare sprinttijd is ervoor gekozen om eerst de bevindingen met hogere urgentie en directere impact op patiëntgegevens en toegangsbeveiliging op te lossen. |
| B-03 – IDOR afspraken andere patiënten             | A.8.3   | Nog open / in uitvoering | Deze bevinding staat nog open in het pentestdocument en is toegewezen aan Rami. Na afronding moeten fix-document, PR-nummer en testbewijs worden toegevoegd.                                                                                                                  |
| B-05 – PII gelogd in audit log                     | A.8.15  | Nog open / in uitvoering | Deze bevinding staat nog open in het pentestdocument en is toegewezen aan Rami. Na afronding moet worden aangetoond dat patiëntgegevens niet meer plain text worden gelogd.                                                                                                   |
| B-08 – PII via debug-logging                       | A.8.15  | Nog open / in uitvoering | Deze bevinding staat nog open in het pentestdocument en is toegewezen aan Rami. Na afronding moet worden aangetoond dat debug-logs geen patiëntgerelateerde waarden of indirecte PII meer bevatten.                                                                           |
| B-06 – Ontbrekende logging van auth-events         | A.8.15  | Geaccepteerd risico      | Dit raakt vooral OpenMRS core en Tomcat-authenticatie. Omdat dit niet volledig binnen de module ligt, is het risico geaccepteerd en vastgelegd in het pentestdocument.                                                                                                        |
| Git-historie en wachtwoordrotatie na B-02          | A.8.5   | Nog te bevestigen        | De hardcoded credentials zijn uit de code verwijderd, maar credentials die eerder in de repository stonden moeten als gecompromitteerd worden beschouwd. Wachtwoordrotatie en eventuele historieschoning moeten nog worden bevestigd.                                         |
| Cookie flags en volledige sessie-hardening na B-07 | A.8.5   | Gedeeltelijk restrisico  | De DWR CSRF-fix is uitgevoerd, maar sommige sessiebeveiligingsmaatregelen zoals cookie flags en session-rotatie zijn afhankelijk van OpenMRS core of Tomcat-configuratie.                                                                                                     |

## 5. Samenvatting per NEN-control

### A.8.3 – Toegangsbeveiliging

Deze control is gekoppeld aan bevindingen waarbij onbevoegde toegang tot afspraakgegevens of beheerfunctionaliteit mogelijk was.

| Bevinding                            | Status               |
| ------------------------------------ | -------------------- |
| B-01 SQL Injection                   | Opgelost             |
| B-03 IDOR afspraken andere patiënten | Open / in uitvoering |
| B-04 Privilege escalation            | Opgelost             |
| B-10 Trust boundary violation        | Opgelost             |
| B-11 Trust boundary violation        | Opgelost             |

De meeste A.8.3-bevindingen zijn opgelost. B-03 moet nog worden afgerond en daarna worden toegevoegd met fix-document, PR en testbewijs.

### A.8.5 – Authenticatie

Deze control is gekoppeld aan credentials, sessies en CSRF-bescherming.

| Bevinding                    | Status                   |
| ---------------------------- | ------------------------ |
| B-02 Hardcoded credentials   | Opgelost, met restrisico |
| B-07 CSRF / sessie-hardening | Opgelost, met restrisico |

De directe codeproblemen zijn opgelost. Er blijven nog platformafhankelijke of organisatorische restrisico’s over, zoals wachtwoordrotatie, git-historieschoning en volledige session-hardening.

### A.8.15 – Logging en monitoring

Deze control is gekoppeld aan logging, monitoring en het voorkomen van PII in logs.

| Bevinding                                | Status               |
| ---------------------------------------- | -------------------- |
| B-05 PII gelogd in audit log             | Open / in uitvoering |
| B-06 Ontbrekende logging van auth-events | Geaccepteerd risico  |
| B-08 PII via debug-logging               | Open / in uitvoering |

A.8.15 is inhoudelijk goed onderbouwd via de gap-analyse logging en het pentestdocument, maar de technische fixes voor B-05 en B-08 moeten nog worden afgerond.

## 6. Artefactenoverzicht

| Artefact              | Pad / verwijzing                             | Doel                                                                                      |
| --------------------- | -------------------------------------------- | ----------------------------------------------------------------------------------------- |
| Pentestbevindingen    | `docs/auditreport/05-pentest-bevindingen.md` | Centrale lijst met bevindingen B-01 t/m B-11, status en NEN-koppeling.                    |
| Security backlog      | `docs/auditreport/security-backlog.md`       | Prioritering van security requirements en koppeling aan threats, bevindingen en controls. |
| Logging gap-analyse   | `docs/auditreport/02gapanalyselogging.md`    | Onderbouwing voor A.8.15 logging en monitoring.                                           |
| Threat model          | `docs/threadmodel/Threat-model.md`           | Onderbouwing van threats en risico’s die aan bevindingen zijn gekoppeld.                  |
| B-01 fixdocument      | `docs/fixes/fix/b01-nick.md`                 | Bewijs voor SQL Injection-fix.                                                            |
| B-02 fixdocument      | `docs/fixes/fix/b02-amine.md`                | Bewijs voor verwijderen hardcoded credentials.                                            |
| B-04 fixdocument      | `docs/fixes/fix/b04-nick.md`                 | Bewijs voor autorisatie op controllers.                                                   |
| B-07 fixdocument      | `docs/fixes/fix/b07-amine.md`                | Bewijs voor CSRF-fix in DWR-laag.                                                         |
| B-10/B-11 fixdocument | `docs/fixes/fix/b10-11-Nick.md`              | Bewijs voor trust boundary validation in AppointmentBlock controllers.                    |

## 7. Conclusie

Deze traceability matrix laat zien hoe de belangrijkste NEN-7510:2024 controls zijn gekoppeld aan concrete analyse-, fix- en testartefacten.

Voor A.8.3 zijn meerdere toegangsbeveiligingsproblemen opgelost, waaronder SQL Injection, privilege escalation en trust boundary violations. Voor A.8.5 zijn hardcoded credentials en CSRF-risico’s aangepakt, met enkele restrisico’s die nog bevestigd moeten worden. Voor A.8.15 zijn de loggingrisico’s goed onderbouwd, maar B-05 en B-08 moeten nog technisch worden afgerond.

B-09 is niet uitgevoerd, omdat deze bevinding een lagere prioriteit had dan de hoger geprioriteerde risico’s die binnen Sprint 4 zijn opgepakt. Dit is expliciet vastgelegd zodat ook niet-uitgevoerde items traceerbaar zijn.
