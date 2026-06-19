# 2 — Testopzet en testresultaten

| | |
|---|---|
| **Module** | openmrs-module-appointmentscheduling |
| **Run-datum** | 2026-06-17 19:13 |
| **Reproducer** | `./scripts/run-baseline.sh` |
| **Raw output** | [`raw/tests/baseline-20260617-191339.txt`](./raw/tests/baseline-20260617-191339.txt) |
| **PIT-rapport** | [`raw/index.html`](.\raw\index.html) |

---

## 1. Doel

Aantonen dat de module **mechanisch werkt** en **meetbaar getest** is, met meer dan
één testtype, zodat een reviewer de huidige stand van zaken zelf kan reproduceren.

## 2. Testtypen

Vier testtypen, elk met een eigen rol.

| # | Type | Tool | Wat het meet |
|---|---|---|---|
| T1 | Unit tests | Maven Surefire | Functionele correctheid |
| T2 | Code coverage | JaCoCo 0.8.12 | Welke productiecode door T1 wordt geraakt |
| T3 | Mutation testing | PIT 1.17.0 | Kwaliteit van T1 — vangen tests injectie-fouten? |
| T4 | Statische analyse-gate | SonarCloud Quality Gate | Nieuwe smells, complexiteit, duplicaten |

## 3. Reproducer

Eén commando vanaf een schone checkout:

```bash
./scripts/run-baseline.sh
```

Het script draait T1 + T2 + T3 lokaal en schrijft de output naar
`docs/onderhoudbaarheid/raw/tests/baseline-<datum>.txt`. T4 draait in CI op SonarCloud.

## 4. Resultaten

### 4.1 T1 — Unit tests (Surefire)

| Metriek | Waarde |
|---|---|
| Tests run | **182** |
| Failures | **0** |
| Errors | **0** |
| Skipped | **0** |
| Total time | 41,7 s |
| Build status | **BUILD SUCCESS** |

Bron: raw output regel 116-118.

### 4.2 T2 — Coverage (JaCoCo)

| Scope | Coverage | Bron |
|---|---|---|
| Module-breed (LINE) | **70,9 %** | SonarCloud dashboard (bp1) |
| Module-breed (LINE, lokaal JaCoCo) | 72,7 % | Per-package optelling in raw output |
| `audit/`-pakket (LINE) | **92,6 %** | JaCoCo-export |
| `audit/`-pakket (BRANCH) | 100 % | JaCoCo-export |

JaCoCo's `jacoco:check` slaagde tegen de drempels uit `api/pom.xml` →
*"All coverage checks have been met"* (regel 131 van de raw output).

### 4.3 T3 — Mutation testing (PIT)

Scope: `org.openmrs.module.appointmentscheduling.audit.*` (zie `api/pom.xml`).

| Metriek | Waarde |
|---|---|
| Mutaties gegenereerd | **15** |
| KILLED | **15** |
| SURVIVED | **0** |
| Mutation score | **100 %** |
| Test strength | **100 %** |
| Line coverage (mutated classes) | 91 % (21/23) |

Bron: raw output regel 228-231 + `raw/tests/pit-20260617-index.html`.

### 4.4 T4 — SonarCloud Quality Gate

| Metriek | Waarde |
|---|---|
| Quality Gate | **Passed** |
| Maintainability rating | A |
| Coverage | 70,9 % |
| Duplicated lines | 1,6 % |

Bron: SonarCloud dashboard 17/06/2026 18:05 (zie bp1).

## 5. Toetsing aan NFR-grenzen

| NFR | Eis | Grens | Meting | Status |
|---|---|---|---|:--:|
| MNT-2 | Duplicaten | ≤ 5 % | 1,6 % | ✅ |
| MNT-3 | Line coverage | ≥ 60 % | 70,9 % | ✅ |
| MNT-4 | Quality Gate Passed | Passed | Passed | ✅ |
| REL-1 | 0 falende tests | 0 | 0/182 | ✅ |

## 6. Conclusie

Alle gemeten NFR-grenzen worden gehaald. De testset is breder dan alleen
unit-tests (T2 coverage + T3 mutation testing + T4 statische analyse-gate),
wat de testopzet reproduceerbaar én onderbouwd maakt. De raw output is
gearchiveerd, zodat een tweede reviewer met één commando dezelfde cijfers krijgt.

## Bijlagen

- [`raw/tests/baseline-20260617-191339.txt`](./raw/tests/baseline-20260617-191339.txt) — volledige output van `run-baseline.sh`.
- [`raw/tests/index.html`](./raw/index.html) — PIT-projectsamenvatting.
- [`raw/tests/pit-20260617-mutations.csv`](./raw/mutations.csv) — PIT mutation-records.
- [`../non-functional-requirements.md`](../non-functional-requirements.md) — bron van MNT-1..4 en REL-1.
