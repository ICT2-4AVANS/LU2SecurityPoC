# Teststrategie onderhoudbaarheid

|              |                                                          |
| ------------ | -------------------------------------------------------- |
| **Module**   | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT     |
| **Datum**    | 2026-06-15                                               |
| **Auteur**   | Enes T. (LU2-MaintainabilityPoC)                         |
| **Scope**    | API-laag (`appointmentscheduling-api`); `omod` valt buiten unit-coverage |
| **Bron**     | [`01-systematische-analyse.md`](./01-systematische-analyse.md) — NFR-grenzen, gevonden hotspots |

> Dit document hoort bij bulletpoint 2 van Opdrachtonderdeel 1: het legt **welke tests we uitvoeren, waarom en hoe** vast. De feitelijke testresultaten staan in [`03-testresultaten-baseline.md`](./03-testresultaten-baseline.md).

---

## 1. Doel

Bewijs leveren — reproduceerbaar — dat:

1. de module **mechanisch werkt** zoals verwacht (gedragstesten); dit dient als regressie-anker voor bulletpoint 6;
2. de codebasis de **maintainability-NFR's** uit `non-functional-requirements.md` haalt (MNT-1..MNT-4);
3. de **testset zelf voldoende kwaliteit** heeft (PIT mutation testing) — een coverage-percentage zonder mutation-score is een leeg getal.

Wat hier *niet* in zit: handmatige acceptatietests, security-tests en performance-tests. Die horen bij andere LU's en zijn buiten scope voor onderhoudbaarheid.

---

## 2. Teststrategie — meerdere testtypen, één doel

De strategie hanteert vier complementaire testtypen. Geen ervan dekt onderhoudbaarheid alleen; samen vormen ze de volledige toetsing.

| # | Testtype                       | Tool / plugin             | Wat het meet                                                       | Waar het in CI faalt                           |
|---|--------------------------------|---------------------------|--------------------------------------------------------------------|------------------------------------------------|
| T1| **Unit tests** (gedrag)        | Maven Surefire + JUnit    | Functionele correctheid; regressie-anker                            | bij ≥ 1 failing test                           |
| T2| **Code coverage**              | JaCoCo 0.8.12             | Hoeveel van de productiecode door T1 wordt geraakt                  | < 30 % module-breed of < 90 % audit-pakket     |
| T3| **Mutation testing**           | PIT (`pitest-maven`)      | Kwaliteit van T1: doden tests injectie-fouten?                     | mutation-score < 50 % op audit-pakket          |
| T4| **Statische analyse-gate**     | SonarCloud Quality Gate   | Nieuwe code-smells, complexity, duplicaten, security-issues         | Quality Gate-status ≠ "Passed"                 |

### 2.1 Waarom deze combinatie en niet een andere

**Onderbouwing per keuze (rubriek-Goed: alternatieven afwegen):**

- **T1 Surefire i.p.v. JUnit-direct of TestNG.** OpenMRS-modules zijn historisch Maven-gebaseerd en de bestaande tests gebruiken Surefire-conventies. Wisselen kost meer dan het oplevert.
- **T2 JaCoCo i.p.v. Cobertura/Clover.** JaCoCo is industrie-standaard, actief onderhouden, en koppelt rechtstreeks aan SonarCloud (T4). Cobertura is end-of-life; Clover commercieel.
- **T3 PIT i.p.v. Stryker/Pitest-junit5-plugin alleen.** Stryker is voor JS — niet toepasbaar op deze Java-laag. Mutation-testing toevoegen i.p.v. méér unit-tests stopt het "fake coverage"-probleem (tests zonder asserts). Past direct bij ISO 25010 sub-attribuut *testability*.
- **T4 SonarCloud i.p.v. Qodana of lokale SpotBugs.** Sonar dekt Java + JSP + JS in één meting (zie §4.1 van `01-systematische-analyse.md`); Qodana mist JSP. SpotBugs alleen mist duplicates en cognitive complexity.
- **Mutation-scope alléén op `audit/`-pakket.** Mutation testing op de volledige legacy-codebasis duurt minuten en levert weinig informatie op; het audit-pakket is *nieuwe* code waar testkwaliteit kritisch is (zie security-backlog DoD).

---

## 3. Koppeling NFR → testtype

Iedere NFR-eis moet door minstens één test gedragen worden, anders is de eis niet falsifieerbaar.

| NFR    | Eis                                          | Grens       | Bewijs-test    | Faal-mechanisme                                    |
|--------|----------------------------------------------|-------------|----------------|----------------------------------------------------|
| MNT-1  | Cyclomatische complexiteit per methode       | ≤ 10        | T4 (Sonar)     | Quality Gate fail bij nieuwe overschrijdingen      |
| MNT-2  | Duplicaat-percentage                         | ≤ 5 %       | T4 (Sonar)     | Quality Gate fail boven drempel                    |
| MNT-3  | Line coverage                                | ≥ 60 %      | T2 (JaCoCo)    | `jacoco:check` faalt build < drempel               |
| MNT-3a | Coverage audit-pakket (line)                 | ≥ 90 %      | T2 (JaCoCo)    | `jacoco:check` faalt build < 0.90                  |
| MNT-3b | Coverage audit-pakket (branch)               | ≥ 80 %      | T2 (JaCoCo)    | `jacoco:check` faalt build < 0.80                  |
| MNT-3c | Mutation score audit-pakket                  | ≥ 50 %      | T3 (PIT)       | `pitest:mutationCoverage` faalt < drempel          |
| MNT-4  | Quality Gate "Passed" op PR                  | Passed      | T4 (Sonar)     | GitHub Action `sonar-scan` faalt                   |
| REL-1  | Alle unit-tests slagen                       | 0 failing   | T1 (Surefire)  | `mvn verify` faalt                                 |

> **Lezing.** MNT-3 is uitgesplitst in module-breed (`MNT-3`), audit-line (`MNT-3a`) en audit-branch (`MNT-3b`). MNT-3c (mutation) is een aanvulling t.o.v. het oorspronkelijke NFR-document en wordt in een addendum opgenomen.

---

## 4. Gates / drempels

Drempels zijn bewust **conservatief** voor de eerste run (no-regression-baseline) en moeten met elke sprint omhoog. Schuif ze niet stilzwijgend omlaag — een commit die drempels verlaagt is een audit-trail-event.

### 4.1 JaCoCo (T2)
| Scope                                        | Counter | Drempel | Bron        |
|----------------------------------------------|---------|--------:|-------------|
| Module-breed (`BUNDLE`)                      | LINE    | 30 %    | `api/pom.xml` `jacoco-check` |
| `org.openmrs.module....audit`                | LINE    | 90 %    | idem        |
| `org.openmrs.module....audit`                | BRANCH  | 80 %    | idem        |

### 4.2 PIT (T3)
| Scope                                        | Drempel | Bron                         |
|----------------------------------------------|--------:|------------------------------|
| `org.openmrs.module....audit.*` mutation     | 50 %    | `api/pom.xml` `pitest-maven` |
| `org.openmrs.module....audit.*` coverage     | 50 %    | idem                         |

### 4.3 SonarCloud Quality Gate (T4)
| Conditie                                     | Drempel       |
|----------------------------------------------|---------------|
| New code → Maintainability Rating            | ≥ A           |
| New code → Reliability Rating                | ≥ A           |
| New code → Security Rating                   | ≥ A           |
| New code → Coverage                          | ≥ 60 %        |
| New code → Duplicated lines                  | ≤ 5 %         |

---

## 5. Reproduceerbaarheid

Elke test moet vanaf een schone checkout met **één commando** te draaien zijn. Dat commando is:

```bash
./scripts/run-baseline.sh
```

Dit script (zie `scripts/run-baseline.sh`):

1. doet `mvn -B clean verify` in `api/` (= T1 + T2);
2. doet `mvn -B pitest:mutationCoverage` (= T3);
3. exporteert SonarCloud-issues JSON (= T4, bron voor `raw/`);
4. produceert een samenvatting in `docs/onderhoudbaarheid/raw/tests/baseline-<datum>.txt`.

Same versions, same outputs. Resultaten worden niet handmatig overgetypt; ze worden geknipt-en-geplakt uit de scriptuitvoer in [`03-testresultaten-baseline.md`](./03-testresultaten-baseline.md).

---

## 6. Wat NIET in deze strategie zit (afbakening)

- **Integration-tests** met Spring-context laden — buiten scope vanwege de OpenMRS 1.9.9-platformafhankelijkheid (MySQL 5.7 + Java 8). Aanbevolen voor latere sprints, niet voor deze PoC.
- **End-to-end / Selenium** — niet relevant voor onderhoudbaarheid; valt onder acceptatie-tests.
- **Frontend-tests** (Jest, Cypress) — de eigen frontend-code bestaat vrijwel volledig uit JSP zonder JS-logica die los testbaar is. ESLint via SonarCloud (T4) dekt de statische kant.
- **Performance / load-tests** — niet relevant voor maintainability NFR's.

---

## 7. Verbindingen met andere documenten

| Document                                                                                          | Relatie                                                              |
|---------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| [`01-systematische-analyse.md`](./01-systematische-analyse.md)                                    | Levert NFR-grenzen en hotspot-bestanden waar testfocus op landt      |
| [`03-testresultaten-baseline.md`](./03-testresultaten-baseline.md)                                | Bevat de feitelijke cijfers die deze strategie oplevert              |
| [`../tests/codecoverage.md`](../tests/codecoverage.md)                                            | Originele JaCoCo-rationale (NEN-7510); deze strategie bouwt erop voort |
| [`../non-functional-requirements.md`](../non-functional-requirements.md)                          | Bronspecificatie van MNT-1..MNT-4                                    |
| `.github/workflows/maintainability-tests.yml`                                                     | Operationele invulling van deze strategie (E1/E2/E3)                 |
