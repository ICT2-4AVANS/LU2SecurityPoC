# 6 — Validatie + regressie-toets

| | |
|---|---|
| **PoC-item** | E1 — Extract Constant in `HibernateAppointmentDAO.java` |
| **Vergelijking** | bp1/bp2 (vóór E1) ↔ post-E1 metingen |
| **Post-E1 raw** | [`raw/tests/baseline-20260617-220403.txt`](./raw/tests/baseline-20260617-220403.txt) + [`raw/sonarexport20260617.json`](./raw/sonarexport20260617.json) |

---

## 1. Doel

Aantonen met dezelfde meet-instrumenten als bp2 dat E1 de onderhoudbaarheid
**meetbaar verbetert** én dat er **geen regressie** is opgetreden.

## 2. Methode

Identiek aan bp2 — zelfde teststrategie, zelfde commando:

```bash
./scripts/run-baseline.sh
```

Daarna een nieuwe SonarCloud-API-export via dezelfde call als in bp1.

## 3. Verbetering aangetoond

| Metriek | Vóór E1 | Na E1 | Δ | Status |
|---|---|---|---|:--:|
| Sonar smell-count totaal | 426 | **423** | **−3** | ✅ verbeterd |
| Sonar `effortTotal` | 5747 min | **5715 min** | **−32 min** | ✅ verbeterd |
| Sonar `java:S1192` in `HibernateAppointmentDAO.java` | 4 | **1** | **−3** | ✅ verbeterd |

> **Eerlijke noot**: het ontwerp uit bp4 voorspelde −4 hits. In werkelijkheid
> zijn er 3 weg en heeft Sonar 1 nieuwe variant gedetecteerd op regel 199
> (de alias-name `"timeSlot"` als 2e argument van `createAlias`). Sonar
> meldt: *"Use already-defined constant 'TIME_SLOT' instead of duplicating
> its value here"*. Dat patroon was in bp4 §3 bewust buiten scope gelaten
> (alias-name ≠ property-name), maar Sonar herkent het wel als duplicaat.
> Opvolging als losse backlog-item.

**Bronnen**: `raw/SONAR_exportv2.json` (header: `total=423`,
`effortTotal=5715`).

## 4. Regressie-toets

| Dimensie | Vóór E1 | Na E1 | Conclusie |
|---|---|---|---|
| Unit-tests slagen | 182 / 182 ✅ | 184 / 184 ✅ | gelijk — geen test gebroken (2 nieuwe tests toegevoegd in andere PR, niet door E1) |
| BUILD status | SUCCESS | SUCCESS | gelijk |
| Coverage (Sonar) | 70,9 % | 70,9 % | gelijk |
| JaCoCo `jacoco:check` | All checks met | All checks met | gelijk |
| PIT mutation score (audit) | 15 / 15 (100 %) | **15 / 15 (100 %)** | gelijk |
| Quality Gate | Passed | Passed | gelijk |

**Geen enkele cel toont een daling**. Drie cellen blijven gelijk, drie
verbeteren (smell-count, effort, S1192-hits in target file).

## 5. NFR-toets — na E1

| NFR | Grens | Meting | Status |
|---|---|---|:--:|
| MNT-1 — Cyclomatische complexiteit | ≤ 10 | Geen overschrijdingen gemeld | ✅ |
| MNT-2 — Duplicaten | ≤ 5 % | 1,6 % | ✅ |
| MNT-3 — Line coverage | ≥ 60 % | 70,9 % | ✅ |
| MNT-4 — Quality Gate Passed | Passed | Passed | ✅ |
| REL-1 — 0 falende tests | 0 | 0 / 184 | ✅ |

Alle NFR's blijven groen.

## 6. Conclusie

E1 is **aantoonbaar verbeterd** (smell-count −3, effort −32 min,
target-rule-hits in deze file van 4 → 1) **zonder regressie** (alle tests
slagen, coverage en mutation-score ongewijzigd, Quality Gate Passed).
De resultaten zijn met één commando reproduceerbaar; de raw output is
gearchiveerd voor controle.

## Bijlagen

- [`raw/tests/baseline-postE1-20260617-220403.txt`](./raw/tests/baseline-20260617-220403.txt) — post-E1 `run-baseline.sh` output
- [`raw/tests/mutations.csv`](./raw/tests/mutations.csv) — post-E1 PIT mutation-records (15/15 KILLED)
- [`raw/sonar-export-postE1-20260617.json`](./raw/sonarexport20260617.json) — post-E1 SonarCloud API-export (`total=423`)
- [`01-analyse.md`](./01-analyse.md), [`02-tests.md`](./02-tests.md) — vergelijkings-baseline
