# Non-functional Requirements

| | |
|---|---|
| **Module** | openmrs-module-appointmentscheduling v2.0.0 |
| **Datum** | 2026-06-08 |

---

## Security

| ID | Eis | Meetcriterium | Grenswaarde |
|---|---|---|---|
| SEC-1 | Geen nieuwe `HIGH` of `CRITICAL` CVE's in afhankelijkheden | `dependency-review` in CI | Pipeline faalt bij severity ≥ HIGH |
| SEC-2 | Geen hardcoded credentials of tokens in broncode | CodeQL `java/hardcoded-credential` query | 0 bevindingen op `main` |
| SEC-3 | Audit-logging aanwezig bij alle `save*`, `cancel*` en `purge*` methoden | Handmatige code review + tests | 100% dekking van kritieke mutaties |
| SEC-4 | Gevoelige gegevens (patiëntnamen, -ID's) worden niet in logs opgenomen als plaintext | Statische analyse + logtests | 0 overtredingen |

---

## Onderhoudbaarheid

| ID | Eis | Meetcriterium | Grenswaarde |
|---|---|---|---|
| MNT-1 | Cyclomatische complexiteit per methode | SonarCloud / Qodana | ≤ 10 per methode |
| MNT-2 | Duplicaat-percentage in broncode | SonarCloud / Qodana | ≤ 5% |
| MNT-3 | Testdekking (line coverage) | JaCoCo rapport als CI-artifact | ≥ 60% |
| MNT-4 | Geen kritieke code smells geïntroduceerd bij PR | SonarCloud Quality Gate | "Passed" vereist |

---

## Betrouwbaarheid

| ID | Eis | Meetcriterium | Grenswaarde |
|---|---|---|---|
| REL-1 | Alle unit-tests slagen | Maven Surefire in CI | 0 failing tests op `main` |
| REL-2 | Build slaagt bij elke push naar `main` | `ci.yml` workflow | 0 build failures |
