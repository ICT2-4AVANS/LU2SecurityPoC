# B-04 — Privilege escalation in controllers

## Gegevens

| Onderdeel | Informatie                             |
| --------- | -------------------------------------- |
| Bevinding | B-04                                   |
| Ernst     | High                                   |
| Titel     | Privilege escalation in 13 controllers |
| Bestanden | `omod/.../web/controller/*.java`       |
| NEN-7510  | A.8.3                                  |
| Branch    | `Fix/b04-Nick-2`                       |

## Probleem

In de Appointment Scheduling Module hadden 13 webcontrollers geen expliciete `@Authorized`-annotatie. Hierdoor werd op controllerniveau niet duidelijk gecontroleerd welk privilege een gebruiker nodig had om een pagina of actie te gebruiken.

In meerdere controllers werd vooral gecontroleerd of een gebruiker ingelogd was met `Context.isAuthenticated()`. Alleen ingelogd zijn is niet genoeg, omdat een gebruiker ook het juiste privilege moet hebben voor bijvoorbeeld afspraken bekijken, afspraken plannen of instellingen aanpassen.

Hierdoor kon privilege escalation ontstaan: een gebruiker met te weinig rechten kon via directe URL’s proberen controller-functionaliteit te bereiken.

## Abuse aantonen

Voor B-04 is een low-privilege gebruiker gebruikt in OpenMRS. Deze gebruiker had geen adminrechten en geen appointment scheduling beheerrechten.

Op onderstaande foto is te zien dat de low-privilege gebruiker geen adminrechten heeft.

![lowprivZonderAdminrechten](images/b04-lowprivZonderAdminrechten.png)

Daarna is geprobeerd om direct naar de appointment settings-pagina te gaan.

![appointmentSettingsForm](images/b04-appointmentSettingsForm.png)

Bij het testen in OpenMRS kreeg de low-privilege gebruiker uiteindelijk een `Insufficient Privileges` melding. Dit laat zien dat de runtime test is uitgevoerd. De browser-abuse werd later in de flow alsnog geblokkeerd door OpenMRS.

![b04-voor-runtime-insufficient-privileges](images/b04-voor-runtime-insufficient-privileges.png)

Omdat de runtime-test door OpenMRS werd geblokkeerd, is aanvullend code-bewijs gebruikt. De kwetsbaarheid zat namelijk op controllerniveau: de controllers hadden vóór de fix geen expliciete `@Authorized`-controle.

Met onderstaande PowerShell-command is gecontroleerd hoeveel `@Authorized`-annotaties aanwezig waren in de controllers:

```powershell
Get-ChildItem $controllerPath -Filter *.java |
ForEach-Object {
    $authorizedCount = (Select-String -Path $_.FullName -Pattern "@Authorized" -ErrorAction SilentlyContinue).Count
    [PSCustomObject]@{
        Controller = $_.Name
        AuthorizedAnnotations = $authorizedCount
    }
} | Format-Table -AutoSize
```

Op onderstaande foto is te zien dat alle 13 controllers vóór de fix `0` `@Authorized`-annotaties hadden.

![b04-voor-controller-authorized-check](images/b04-voor-controller-authorized-check.png)

Daarna is gecontroleerd waar `RequestMapping`, `Context.isAuthenticated()` en `@Authorized` voorkwamen.

```powershell
Select-String -Path "$controllerPath\*.java" -Pattern "Context.isAuthenticated|@Authorized|RequestMapping" |
ForEach-Object {
    [PSCustomObject]@{
        File = Split-Path $_.Path -Leaf
        Line = $_.LineNumber
        Code = $_.Line.Trim()
    }
} | Format-Table -AutoSize -Wrap
```

Op onderstaande foto is te zien dat de controllers wel routes hadden via `RequestMapping`, maar dat er vóór de fix geen `@Authorized`-annotaties aanwezig waren.

![b04-voor-context-isauthenticated](images/b04-voor-context-isauthenticated.png)

## Fix

De fix is om de controllers expliciet te beveiligen met `@Authorized`. Hierdoor wordt per controller of methode gecontroleerd of de gebruiker het juiste privilege heeft.

Voorbeelden van toegevoegde privileges:

| Controller                                 | Toegevoegd privilege                                              |
| ------------------------------------------ | ----------------------------------------------------------------- |
| `AppointmentTypeListController`            | `PRIV_VIEW_APPOINTMENT_TYPES`                                     |
| `AppointmentTypeFormController`            | `PRIV_MANAGE_APPOINTMENT_TYPES`                                   |
| `AppointmentSettingsFormController`        | `PRIV_MANAGE_APPOINTMENTS_SETTINGS`                               |
| `AppointmentFormController`                | `PRIV_SCHEDULE_APPOINTMENTS`                                      |
| `AppointmentListController`                | `PRIV_VIEW_APPOINTMENTS`                                          |
| `AppointmentBlockCalendarController`       | `PRIV_VIEW_APPOINTMENT_BLOCKS` / `PRIV_MANAGE_APPOINTMENT_BLOCKS` |
| `AppointmentBlockFormController`           | `PRIV_MANAGE_APPOINTMENT_BLOCKS`                                  |
| `AppointmentBlockListController`           | `PRIV_VIEW_APPOINTMENT_BLOCKS` / `PRIV_MANAGE_APPOINTMENT_BLOCKS` |
| `AppointmentStatisticsFormController`      | `PRIV_VIEW_APPOINTMENTS_STATISTICS`                               |
| `AppointmentDailyCountController`          | `PRIV_VIEW_APPOINTMENTS_STATISTICS`                               |
| `AppointmentRequisitionController`         | `PRIV_REQUEST_APPOINTMENTS`                                       |
| `AppointmentsPortletController`            | `PRIV_VIEW_APPOINTMENT_HISTORY_TAB`                               |
| `PatientDashboardAppointmentExtController` | `PRIV_VIEW_APPOINTMENT_HISTORY_TAB`                               |

Na de fix is opnieuw gecontroleerd hoeveel `@Authorized`-annotaties aanwezig zijn.

Op onderstaande foto is te zien dat de controllers na de fix niet meer allemaal `0` hebben, maar nu expliciet beveiligd zijn met `@Authorized`.

![b04-na-controller-authorized-check](images/b04-na-controller-authorized-check.png)

## Test na de fix

Na de fix is een unit test toegevoegd:

```text
ControllerAuthorizationTest
```

Deze test controleert dat:

- alle 13 Appointment Scheduling controllers `@Authorized` gebruiken;
- de juiste privilege-constant per controller aanwezig is;
- controllers niet meer zonder expliciete autorisatiecheck blijven.

De test is uitgevoerd met:

```powershell
mvn -f .\openmrs-module-appointmentscheduling\openmrs-module-appointmentscheduling\pom.xml -pl omod -Dtest=ControllerAuthorizationTest test
```

Onderstaande foto laat zien dat de test na de aanpassing succesvol is uitgevoerd.

![b04-na-unit-test-build-succes](images/b04-na-unit-test-build-succes.png)

## Resultaat

B-04 is opgelost. De 13 controllers hebben nu expliciete autorisatie via `@Authorized`. Hierdoor is het niet meer alleen genoeg dat een gebruiker ingelogd is; de gebruiker moet ook het juiste privilege hebben voor de betreffende controller of actie.

Deze fix versterkt de toegangsbeveiliging volgens NEN-7510 A.8.3 en vermindert het risico op privilege escalation binnen de Appointment Scheduling Module.
