![b10-b11-voor-codeql-alerts](images/b10-b11-voor-codeql-alerts.png)

![b10-voor-appointmentblocklist-trustboundary](images/b10-voor-appointmentblocklist-trustboundary.png)

![b11-voor-appointmentblockcalendar-trustboundary](images/b11-voor-appointmentblockcalendar-trustboundary.png)

Op onderstaande foto is te zien dat request-parameters zoals `locationId`, `chosenType`, `chosenProvider`, `fromDate` en `toDate` vóór de fix direct werden verwerkt en opgeslagen in de sessie met `httpSession.setAttribute(...)`.
![b10-b11-voor-requestparam-session-short](images/b10-b11-voor-requestparam-session-short.png)

![b10-na-appointmentblocklist-validation](images/b10-na-appointmentblocklist-validation.png)
![b10-na-appointmentblocklist-helpermethods](images/b10-na-appointmentblocklist-helpermethods.png)
