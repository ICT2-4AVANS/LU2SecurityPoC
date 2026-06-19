# 3 — Geprioriteerde verbeteringen onderhoudbaarheid

| | |
|---|---|
| **Module** | openmrs-module-appointmentscheduling |
| **Bronnen** | bp1 ([`01-analyse.md`](./01-analyse.md)) + bp2 ([`02-tests.md`](./02-tests.md)) |
| **Sonar-export** | [`raw/SONAR_export.json`](./raw/SONAR_export.json) (426 issues) |

---

## 1. Doel

Een korte, geprioriteerde lijst van verbeter-kandidaten kiezen die direct
voortvloeien uit de Sonar-meting (bp1) en die met de bestaande testset (bp2)
veilig te valideren zijn.

## 2. Bronnen voor de prioritering

| Bron | Wat het levert |
|---|---|
| bp1 §5 | 426 Sonar-issues op `ICT2-4AVANS_LU2SecurityPoC`, Quality Gate Passed |
| bp1 raw-export | Per-issue rule, file, severity, effort-schatting (5747 min totaal) |
| bp2 §4.1 | 182/182 unit-tests slagen → veilig regressie-net voor refactors |
| bp2 §4.3 | PIT 15/15 KILLED → mutation-toets als tweede regressie-net |

## 3. Criteria

Drie assen op 1-5 schaal (zelfde stijl als [`../CIA/CIA-analyse.md`](../CIA/CIA-analyse.md)).

| As | Schaal | Wat het meet |
|---|---|---|
| **Impact** | 1 (Zeer laag) – 5 (Zeer hoog) | Aantal smells dat verdwijnt + Sonar-impact (LOW/MEDIUM/HIGH) |
| **Effort** | 1 (XS, < 1 u) – 5 (L, > 1 dag) | Realistische tijd om de fix uit te voeren |
| **Risk** | 1 (Laag) – 5 (Zeer hoog) | Kans op regressie of breken van bestaand gedrag |

**Score = Impact × 10 ÷ (Effort × Risk)**

Hoge impact telt zwaar; hoge effort of risk drukken de score evenredig omlaag.

## 4. Kandidaten — long-list

Vier kandidaten uit de 426-issues-export, gespreid over verschillende rules en files.

| ID | Verbetering | Sonar-rule | Bron | Impact | Effort | Risk | **Score** |
|---|---|---|---|:--:|:--:|:--:|---:|
| **E1** | Extract Constant — 4 duplicate literals in `HibernateAppointmentDAO.java` | `java:S1192` | bp1 export, file regel 67-261 | 3 | 1 | 1 | **30** |
| **E2** | Verwijder uitgecommentarieerde code in `pom.xml` | `xml:S125` | bp1 export, pom.xml regel 16 | 1 | 1 | 1 | **10** |
| **E3** | Maak 3 niet-serializable velden `transient` in `Appointment.java` | `java:S1948` (HIGH) | bp1 export, Appointment.java regel 113/115/125 | 4 | 2 | 2 | **10** |
| **E4** | Extract Constant — duplicate literals in `HibernateAppointmentStatusHistoryDAO.java` | `java:S1192` | bp1 export, regel 39/49 | 3 | 2 | 1 | **15** |

**Sortering**: E1 (30) → E4 (15) → E2 = E3 (10).

## 5. Keuze top-item — E1

**E1 wordt het PoC-item**, om drie redenen:

1. **Hoogste score (30)** — laag risico, lage effort, meetbare impact (4 Sonar-issues weg).
2. **Pure refactor** — geen logica-wijziging; bytecode-equivalent na compile.
   De 182 unit-tests uit bp2 dekken regressie volledig af.
3. **Volgt het patroon** van rule `java:S1192`, dat ook in andere DAO-files
   voorkomt (zie E4). Een succesvolle E1 valideert de aanpak die later op de
   rest van de backlog herhaald kan worden.

**Verwacht resultaat**: Sonar-totaal **426 → 422** (machineel verifieerbaar via
herhaalde API-export).

## 6. Buiten scope

- **E2** (commented-out pom.xml) — score laag; eerder een opruim-actie dan een
  echte maintainability-verbetering. Op de backlog.
- **E3** (`java:S1948`) — hogere impact maar raakt serialization-gedrag van
  productiecode; risk = 2. Te onzeker voor de PoC, naar volgende sprint.
- **E4** (`java:S1192` elders) — zelfde patroon als E1; pas zinvol nadat E1 het
  patroon heeft bewezen.

## 7. Vervolg

E1 wordt uitgewerkt in:
- bp4 — aangepast ontwerp (ontwerppatroon + alternatieven)
- bp5 — PoC-realisatie in `HibernateAppointmentDAO.java`
- bp6 — validatie (Sonar-rerun + 182/182 tests + PIT 15/15)
