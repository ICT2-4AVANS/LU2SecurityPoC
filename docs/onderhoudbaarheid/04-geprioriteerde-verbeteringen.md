# Geprioriteerde verbeteringen onderhoudbaarheid

|              |                                                          |
| ------------ | -------------------------------------------------------- |
| **Module**   | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT     |
| **Datum**    | 2026-06-16                                               |
| **Auteur**   | Enes T. (ICT2-4AVANS LU2SecurityPoC)                     |
| **Scope**    | Synthese van bulletpoint 1 (analyse) + bulletpoint 2 (testresultaten) → verbeterplan |
| **Vervolg**  | Top-3 voedt direct het ontwerp (bulletpoint 4) en de PoC-realisatie (bulletpoint 5) |

> Dit document maakt **geen nieuwe metingen**; het synthetiseert de cijfers uit
> [`01-systematische-analyse.md`](./01-systematische-analyse.md) en
> [`03-testresultaten-baseline.md`](./03-testresultaten-baseline.md) tot een
> geprioriteerde verbeterlijst. Elke verwijzing is hard cijfermatig: §X.Y in
> bp1/bp2 of een NFR-ID uit `non-functional-requirements.md`.

---

## 1. Doel + scope

Drie vragen beantwoorden, in deze volgorde:

1. **Waaraan moet gewerkt worden?** Welke verbeteringen vallen logisch uit de analyse en de baseline?
2. **In welke volgorde?** Op basis van expliciete criteria, niet onderbuik.
3. **Wat doen we als eerste (PoC)?** Een top-3 die binnen bulletpoint 5 reëel te realiseren is.

Buiten scope hier: het *hoe* (ontwerp, design patterns, refactoring-patronen). Dat is bulletpoint 4.

---

## 2. Prioriteringsmethode

### 2.1 Criteria

Drie assen, expliciet gescoord. Dit is de "Goed"-rubriek-eis: *expliciete criteria én consistente verwijzing naar analyse/meetgegevens*.

| As         | Schaal      | Wat het meet                                                                          |
|------------|-------------|---------------------------------------------------------------------------------------|
| **Impact** | L / M / H   | Hoeveel verbetert de onderhoudbaarheid? In termen van: smell-reductie, NFR-cel ⚠→✅, mutation-score-stijging. |
| **Effort** | XS / S / M / L | Realistische schatting in mens-tijd. XS = uren, S = ≤ 1 dag, M = 1–3 dagen, L = week+ |
| **Risk**   | L / M / H   | Kans op regressie op bestaande tests of breken van bestaand gedrag                    |

### 2.2 Score-formule

Numeriek omzetten:

| Impact | Effort | Risk |
|--------|--------|------|
| L = 1  | XS = 1 | L = 1 |
| M = 3  | S = 2  | M = 2 |
| H = 5  | M = 4  | H = 4 |
|        | L = 8  |       |

**Score = (Impact × 10) ÷ (Effort × Risk)**

Logica: hoge impact telt zwaar; effort en risico drukken de score evenredig omlaag. Een quick-win (Impact M, Effort XS, Risk L) eindigt op 30. Een hoog-risico-weekklus (Impact H, Effort L, Risk H) zakt naar 1,6.

### 2.3 Bronnen die meetellen

Iedere verbetering verwijst naar minstens één van:

- `01-systematische-analyse.md` §3 (overall metrics), §4.1 (smells per rule), §4.2 (per bestand/categorie), §5 (NFR-toets), §6 (bevindingen O1–O8)
- `03-testresultaten-baseline.md` §3 (JaCoCo per package), §4 (PIT), §6 (NFR-stand)
- `non-functional-requirements.md` MNT-1..4, REL-1

Een verbetering zonder bronverwijzing is geen kandidaat — dan is de onderbouwing per definitie zwak.

---

## 3. Long-list — 12 kandidaat-verbeteringen

Gegroepeerd in vier categorieën die direct uit de analyse vallen. Elke ID is uniek en wordt verderop in §4 gescoord.

### Categorie A — Sonar / CI-hygiëne (meet-correctie)

| ID | Verbetering                                                  | Bron(nen)                       |
|----|--------------------------------------------------------------|---------------------------------|
| A1 | `sonar.exclusions` voor vendored frontend-libs (`jquery.dataTables.js`, `opentip*`, `ZeroClipboard*`, `TableTools`, `json2`, `fullcalendar.css`, …) | bp1 §4.2.2, bp1 §6 A4 |
| A2 | JaCoCo-XML naar SonarCloud uploaden, `sonar.coverage.jacoco.xmlReportPaths` zetten | bp1 §4.5, bp1 §6 A2, bp2 §5     |
| A3 | Quality Gate-profiel "OpenMRS-LU2" definiëren + koppelen aan CI (`-Dsonar.qualitygate.wait=true` staat al) | bp1 §6 A3, bp1 §5 MNT-4, bp2 §5 |

### Categorie B — Modernisering eigen code (de 89 echte smells)

| ID | Verbetering                                                  | Bron(nen)                       |
|----|--------------------------------------------------------------|---------------------------------|
| B1 | Deprecated HTML-attribuut `align` weghalen (33×) — vervangen door CSS-classes | bp1 §4.2.3 rule `Web:S1827`     |
| B2 | A11y-issues fixen: handlers op niet-interactieve elementen + `<a>`-als-knop + ontbrekende `role` (29× in totaal) | bp1 §4.2.3 rules `Web:S6847/6848/6844` |
| B3 | `var` → `let`/`const` in **eigen** JS (13× in `statusButtons.js` e.d., niet vendored) | bp1 §4.2.3 rule `javascript:S3504` |
| B4 | CSS-kleurcontrast (WCAG) — 6× in eigen stylesheets           | bp1 §4.2.3 rule `css:S7924`     |
| B5 | Uitgecommentarieerde code in `pom.xml` opruimen (3×)         | bp1 §4.2.3 rule `xml:S125`      |

### Categorie C — Test-kwaliteit

| ID | Verbetering                                                  | Bron(nen)                       |
|----|--------------------------------------------------------------|---------------------------------|
| C1 | `format_writesTimestampAsIso8601Utc` omgevingsonafhankelijk maken — set JVM-TZ vóór assert, zodat ook `setTimeZone(UTC)`-verwijdering op CI gedood wordt | bp2 §4.3                        |
| C2 | Coverage van 0 %-gedekte klassen optillen: `StudentT` 0/137, `AppointmentRequisition` 0/32, `AppointmentSchedulerSetup` 0/26, `AppointmentActivator` 0/17, `AppointmentDailyCount` 0/12, `AppointmentStatusSerializer` 0/10 | bp2 §3.3 (bottom-5)             |

### Categorie D — Vendored libs (de 1.145 "andere" smells)

| ID | Verbetering                                                  | Bron(nen)                       |
|----|--------------------------------------------------------------|---------------------------------|
| D1 | `jquery.dataTables.js` upgraden naar moderne versie of vervangen door DataTables 2.x — 654 smells | bp1 §4.2.1 (rank 1)             |
| D2 | `ZeroClipboard.*` verwijderen (Flash-relikwie, EOL 2017) — 39 smells + vermindert attack-surface | bp1 §4.2.1 (rank 5+13)          |

---

## 4. Scoring per verbetering

Volledige tabel — alle 12 items met score en bronverwijzing. Sortering: score aflopend.

| Rank | ID | Verbetering (samenvatting)                              | Impact | Effort | Risk | **Score** | Bron                |
|----:|----|---------------------------------------------------------|:------:|:------:|:----:|----------:|---------------------|
|  1  | A1 | Sonar-exclusions vendored libs                          |   H    |   XS   |  L   |     **50** | bp1 §4.2.2, §6 A4   |
|  2  | C1 | PIT-survivor `formatIso8601` fixen                      |   M    |   XS   |  L   |     **30** | bp2 §4.3            |
|  3  | A2 | JaCoCo-XML naar SonarCloud importeren                    |   H    |   S    |  L   |     **25** | bp1 §6 A2, bp2 §5   |
|  3  | A3 | Quality Gate "OpenMRS-LU2" koppelen aan CI              |   H    |   S    |  L   |     **25** | bp1 §6 A3, bp1 §5   |
|  5  | B1 | Deprecated `align`-attribuut vervangen (33×)             |   M    |   S    |  L   |     **15** | bp1 §4.2.3          |
|  6  | B3 | `var` → `let`/`const` in eigen JS (13×)                  |   L    |   XS   |  L   |     **10** | bp1 §4.2.3          |
|  6  | B4 | CSS-kleurcontrast (WCAG, 6×)                             |   L    |   XS   |  L   |     **10** | bp1 §4.2.3          |
|  6  | B5 | Pom.xml commented-out code (3×)                          |   L    |   XS   |  L   |     **10** | bp1 §4.2.3          |
|  9  | C2 | Coverage 0 %-klassen optillen (~234 lines)               |   H    |   L    |  L   |   **6,25** | bp2 §3.3            |
| 10  | B2 | A11y-issues (29×, structureel werk)                      |   M    |   M    |  M   |   **3,75** | bp1 §4.2.3          |
| 10  | D2 | `ZeroClipboard` verwijderen                              |   M    |   M    |  M   |   **3,75** | bp1 §4.2.1          |
| 12  | D1 | `jquery.dataTables` upgraden/vervangen                   |   H    |   L    |  H   |   **1,56** | bp1 §4.2.1          |

### 4.1 Lezen van de tabel

- **Top-4 zijn quick wins** (score ≥ 25). Lage effort, laag risico, hoog impact op de dashboard-cijfers en op de NFR-stand.
- **Top-3 + A3 + B1 + B-cluster** dekt samen alle vier de open NFR-cellen uit `01-systematische-analyse.md §5`. De rest is "nice to have".
- **D1 zakt naar de bodem.** Niet omdat het onbelangrijk is (654 smells!) maar omdat het in een PoC-tijdspanne niet realistisch + risicovol is. A1 maskeert deze 654 al, wat de dashboard-noise effectief oplost zonder de risico's van een upgrade.

---

## 5. Top-3 voor de PoC (bulletpoint 5)

De drie items waar bulletpoint 5 op landt:

### Plek 1 — A1: Sonar-exclusions voor vendored libs

**Waarom #1.** Score 50, XS effort, laag risico. Eén configuratiewijziging haalt 1.145 van de 1.234 smells uit het beeld. Het dashboard gaat van "1.234 problemen" naar "89 echte problemen" — dat is **de juiste meting van wat het team kan beïnvloeden** (bp1 §4.2.2). Geen code-wijziging, geen testimpact, geen regressierisico. Dit is bovendien een voorwaarde voor zinvolle Quality Gate-werking (A3): zonder exclusion zou de gate continu rood blijven door vendored noise.

**Bewijs uit data**: 1.145 vendored smells (92,8 % van het totaal) verdwijnen uit de scope. NFR-rapportage wordt voor het eerst representatief.

### Plek 2 — C1: PIT-survivor `formatIso8601` repareren

**Waarom #2.** Score 30, XS effort. De CI-baseline (bp2 §4.3) liet zien dat één omgevingsafhankelijke test op CI faalt te detecteren — een echte test-kwaliteitsbug die JaCoCo niet zou vinden. Fix is mechanisch (1 regel in `@Before`): `TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))`. Daarna doodt PIT de mutatie en stijgt de mutation-score naar 100 %.

**Bewijs uit data**: bp2 §4.3 — surviving mutation in `AuditLogger.java:87`. PIT-score 14/15 → 15/15. NFR MNT-3c verandert van "ruim gehaald" naar "perfect gehaald".

### Plek 3 — A2: JaCoCo-XML naar SonarCloud importeren

**Waarom #3.** Score 25, S effort. Dit sluit MNT-3 op het SonarCloud-dashboard. Het CI-werk is al voor 80 % gedaan (`maintainability-tests.yml` heeft al `-Dsonar.coverage.jacoco.xmlReportPaths` geconfigureerd). Alleen valideren dat de import werkt + Sonar's coverage-cel niet meer `-` toont.

**Bewijs uit data**: bp1 §3 toont "Coverage: niet gemeten" op het dashboard; bp2 §3.1 toont 72,7 % lokaal — die wil je in het dashboard zien om bp1's NFR-toets (§5) groen te krijgen.

### 5.1 Wat de top-3 samen oplevert

| NFR  | Stand voor PoC                                  | Stand na top-3 PoC (verwacht)                  |
|------|-------------------------------------------------|------------------------------------------------|
| MNT-1| ⚠ deels (geen overschrijdingen in eigen code)    | ✅ — A1 maakt meting representatief; mutation in vendored libs telt niet meer mee |
| MNT-2| ✅ (1,2 % duplicates)                            | ✅                                              |
| MNT-3| ⚠ (lokaal 72,7 %, niet in dashboard)             | ✅ — A2 brengt cijfer naar het dashboard       |
| MNT-3c| ⚠ (93 % met 1 survivor)                         | ✅ — C1 maakt er 100 % van                     |
| MNT-4| ❌ (Quality Gate Not computed)                   | ⚠ → wacht op A3 (volgt in bp5/bp6)             |

Dus na de top-3 PoC: **4 van 5 onderhoudbaarheids-NFR's hard groen**, MNT-4 ligt klaar voor activering in A3.

---

## 6. Bewust buiten scope voor de PoC

Eerlijk: dit doen we niet in bulletpoint 5, en waarom niet.

| Items niet in PoC          | Reden                                                                                       |
|----------------------------|---------------------------------------------------------------------------------------------|
| **A3 Quality Gate-koppeling** | Hoge score (25) maar vereist SonarCloud-UI-werk + token-flow. Past beter in een sprint na de PoC. Documentatief al voorbereid in `maintainability-tests.yml` (`qualitygate.wait=true`). |
| **B1 + B3 + B4 + B5**       | Mechanische refactor-werk; geen onderbouwingsverhaal nodig. Doen we *na* de PoC, niet in de PoC zelf. Levert geen ontwerpprincipes/patronen op (rubriek-eis bp4). |
| **B2 A11y-cluster**         | Hoge waarde, maar het is een *frontend*-PoC die los zou staan van het maintainability-verhaal. Past beter bij een toegankelijkheids-LU. |
| **C2 Coverage 0 %-klassen** | Effort = L (week+). Buiten PoC-budget. `StudentT` (137 niet-gedekte regels) is statistische helper — niet primair gebruikt in business-flow. |
| **D1 jquery.dataTables**    | Score 1,56 — hoogste risico, langste duur. A1 maskeert het in het dashboard al. Niet doen in PoC. |
| **D2 ZeroClipboard**        | Score 3,75. Beter onderdeel van een security-spoor (Flash = dood + attack surface). Niet in maintainability-PoC. |

> **Belangrijk**: dat iets buiten scope valt is geen oordeel over waarde — het is een keuze om de PoC behapbaar te houden. Alle 12 items blijven op de backlog en kunnen na de PoC in volgorde van score worden opgepakt.

---

## 7. Wat hierna komt

| Bulletpoint | Wat                                                                                              |
|-------------|--------------------------------------------------------------------------------------------------|
| **4 — Ontwerp** | Voor de top-3 (A1, C1, A2): welke ontwerpprincipes, patronen, refactoring-patronen sturen de aanpak? Alternatieven afwegen. |
| **5 — PoC**     | A1 + C1 + A2 daadwerkelijk implementeren op een feature-branch. AI-tooling-verantwoording.       |
| **6 — Validatie** | Opnieuw de baseline-meting draaien; aantonen dat de scores ✅ zijn en er geen regressie is.    |

---

## 8. Verwijzingen

- [`01-systematische-analyse.md`](./01-systematische-analyse.md) — broncijfers per smell, per rule, per bestand, en NFR-toets
- [`03-testresultaten-baseline.md`](./03-testresultaten-baseline.md) — JaCoCo + PIT cijfers per package + CI-artifact-analyse
- [`../non-functional-requirements.md`](../non-functional-requirements.md) — MNT-1..4, REL-1 grenswaarden
- [`02-teststrategie.md`](./02-teststrategie.md) — waar de scoring-criteria op aansluiten (T1..T4)
