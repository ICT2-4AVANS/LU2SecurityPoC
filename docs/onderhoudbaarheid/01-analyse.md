# 1 — Systematische analyse onderhoudbaarheid

| | |
|---|---|
| **Module** | openmrs-module-appointmentscheduling |
| **Sonar-project** | `ICT2-4AVANS_LU2SecurityPoC` |
| **Snapshot** | 17/06/2026 18:05 |
| **NFR-bron** | [`../non-functional-requirements.md`](../non-functional-requirements.md) |
| **Raw data** | [`raw/sonarexport20260617.json`](./raw/sonarexport20260617.json) |

---

## 1. Doel

De huidige onderhoudbaarheid van de module objectief meten, zodat:

1. er een **nulmeting** ligt om latere verbeteringen tegen af te zetten;
2. duidelijk is of de module **voldoet aan de NFR-grenzen**.

## 2. Scope

Java- en XML-bronbestanden in de module Appointment Scheduling Module API.
Totaal: **4,5k Lines of Code** (zie Sonar-dashboard).

## 3. Tooling

**SonarCloud** met standaard Java-ruleset op het project `ICT2-4AVANS_LU2SecurityPoC`.
Resultaten zijn opgehaald via het web-dashboard en via de API:

```
GET https://sonarcloud.io/api/issues/search
    ?componentKeys=ICT2-4AVANS_LU2SecurityPoC
    &types=CODE_SMELL
    &ps=100&p=1
```

De volledige response staat in `raw/sonarexport20260617.json`.

## 4. NFR-grenzen (uit `non-functional-requirements.md`)

| ID | Eis | Grenswaarde |
|---|---|---|
| MNT-1 | Cyclomatische complexiteit per methode | ≤ 10 |
| MNT-2 | Duplicaat-percentage | ≤ 5 % |
| MNT-3 | Line coverage | ≥ 60 % |
| MNT-4 | Quality Gate | "Passed" |

## 5. Resultaten

**Bron:** SonarCloud dashboard, snapshot 17/06/2026 18:05.

> *(Plek voor screenshot: `raw/sonar-dashboard-20260617.png`)*

| Metriek | Waarde |
|---|---|
| Lines of Code | 4.500 |
| Maintainability rating | A |
| Code smells | 426 |
| Technical debt | 5.747 min ≈ 12 dagen |
| Coverage | 70,9 % |
| Duplicated lines | 1,6 % |
| Reliability rating | E (47 issues) |
| Security rating | A (0 issues) |
| Quality Gate | **Passed** |

> *Reliability en Security staan ter informatie; ze vallen buiten het onderhoudbaarheids-spoor.*

## 6. Toetsing aan de NFR-grenzen

| NFR | Grens | Meting | Status |
|---|---|---|:--:|
| MNT-1 | ≤ 10 | Geen overschrijdingen gemeld door Sonar | ✅ |
| MNT-2 | ≤ 5 % | 1,6 % | ✅ |
| MNT-3 | ≥ 60 % | 70,9 % | ✅ |
| MNT-4 | Passed | Passed | ✅ |

## 7. Conclusie

Alle vier de maintainability-NFR's worden gehaald en de Quality Gate staat op
**Passed**. Er staan wel **426 maintainability-issues** open (≈ 12 dagen schuld);
die vormen de input voor de prioritering in bulletpoint 3.

## Bijlagen

- [`raw/sonar-export-20260617.json`](./raw/sonarexport20260617.json) — volledige API-export (`total: 426`, `effortTotal: 5747`).
- [`../non-functional-requirements.md`](../non-functional-requirements.md) — bron van MNT-1..4.
