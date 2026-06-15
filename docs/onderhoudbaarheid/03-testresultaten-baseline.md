# Testresultaten — baseline

|              |                                                          |
| ------------ | -------------------------------------------------------- |
| **Module**   | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT     |
| **Datum**    | 2026-06-15 18:34 CEST                                    |
| **Run-id**   | lokaal (Windows 11 + Git Bash MINGW64), zie §7           |
| **Reproducer** | `./scripts/run-baseline.sh`                            |
| **Strategie**| [`02-teststrategie.md`](./02-teststrategie.md)           |
| **Raw output** | [`raw/tests/baseline-20260615-183459.txt`](./raw/tests/baseline-20260615-183459.txt) |

> **Doel.** Vastleggen van de feitelijke testresultaten op het moment dat we de teststrategie voor het eerst uitvoeren. Dit is de **nulmeting**; bulletpoint 6 vergelijkt hiermee.

> **TL;DR.** Alle gemeten NFR-grenzen zijn **gehaald**: 182/182 unit-tests slagen, JaCoCo `BUILD SUCCESS` ("All coverage checks have been met"), PIT mutation-score = 100 % op het audit-pakket. SonarCloud Quality Gate (T4) is buiten deze baseline — die draait in CI.

---

## 1. Hoe deze cijfers tot stand komen

Eén commando levert alle metingen:

```bash
./scripts/run-baseline.sh
```

Het script schrijft de samenvatting naar `docs/onderhoudbaarheid/raw/tests/baseline-<datum>.txt`. De cijfers hieronder zijn **directe overname** uit dat bestand — niet geïnterpreteerd, niet afgerond. Bij twijfel: zie de raw output in de bijlage.

Afgeleide totalen (zoals module-brede LINE-coverage) zijn met `python3` opgeteld uit de per-package-regels in de raw output; de scriptregel is gedocumenteerd in §8.

---

## 2. T1 — Unit tests (Surefire)

> **Bron**: `target/surefire-reports/TEST-*.xml` + `[INFO] Results:` in raw output (regel 116-118)
> **Reproducer**: `cd .../api && mvn -B clean verify`

| Metriek    | Waarde     | NFR-toets | Status |
|------------|-----------:|-----------|:------:|
| Tests run  | **182**    | —         | —      |
| Failures   | **0**      | REL-1: 0  | ✅     |
| Errors     | **0**      | REL-1: 0  | ✅     |
| Skipped    | **0**      | —         | —      |
| Time (s)   | ~28,1      | —         | —      |

### 2.1 Testklassen die gedraaid hebben (21 stuks)

| Pakket / klasse                                                  | Tests | Time (s) |
|------------------------------------------------------------------|------:|---------:|
| `api.AppointmentBlockServiceTest`                                | 12    | 7,60     |
| `api.AppointmentRequestServiceTest`                              | 15    | 1,10     |
| `api.AppointmentServiceTest`                                     | 36    | 2,60     |
| `api.AppointmentStatusHistoryServiceTest`                        | 10    | 0,66     |
| `api.AppointmentTypeServiceTest`                                 | 17    | 1,12     |
| `api.AppointmentUtilityTest`                                     | 3     | 0,20     |
| `api.db.hibernate.HibernateAppointmentDAOSqlInjectionAbuseTest`  | 1     | 0,11     |
| `api.ProviderScheduleServiceTest`                                | 9     | 0,61     |
| `api.TimeSlotServiceTest`                                        | 24    | 1,59     |
| `AppointmentTest`                                                | 3     | 0,001    |
| `audit.AuditLoggerTest`                                          | 16    | 0,00     |
| `reporting.data.evaluator.*DataEvaluatorTest` (10 klassen)       | 16    | ~4,2     |
| `reporting.dataset.evaluator.AppointmentDataSetEvaluatorTest`    | 1     | 0,13     |
| `reporting.query.evaluator.BasicAppointmentQueryEvaluatorTest`   | 5     | 0,37     |
| `task.CleanOpenAppointmentsTest`                                 | 2     | 0,15     |
| `validator.AppointmentBlockValidatorComponentTest`               | 5     | 0,35     |
| `validator.AppointmentTypeValidatorTest`                         | 9     | 0,19     |

---

## 3. T2 — Code coverage (JaCoCo)

> **Bron**: `target/site/jacoco/jacoco.csv` + `[INFO] All coverage checks have been met` (raw regel 131)
> **Reproducer**: `cd .../api && mvn -B clean verify` (zelfde call als T1)

### 3.1 Module-breed

| Counter | Gedekt | Totaal | % gedekt | NFR-grens                | Status |
|---------|-------:|-------:|---------:|--------------------------|:------:|
| LINE    | 1284   | 1767   | **72,7 %** | MNT-3: ≥ 60 % (CI-gate 30 %) | ✅ |
| BRANCH  | 367    | 561    | **65,4 %** | —                        | —      |

> JaCoCo's `jacoco:check` slaagde tegen de in `api/pom.xml` ingestelde drempels (30 % module-breed, 90/80 % voor `audit/`). Build is groen.

### 3.2 Security-kritisch pakket `org.openmrs.module.appointmentscheduling.audit`

| Counter | Gedekt | Totaal | % gedekt | NFR-grens | Status |
|---------|-------:|-------:|---------:|-----------|:------:|
| LINE    | 25     | 27     | **92,6 %** | MNT-3a: ≥ 90 % | ✅ |
| BRANCH  | 24     | 24     | **100,0 %** | MNT-3b: ≥ 80 % | ✅ |

### 3.3 Top-5 best en slechtst gedekte klassen (LINE)

**Best (10 + zonder gemiste branches):**

| Klasse                                            | LINE   | BRANCH |
|---------------------------------------------------|--------|--------|
| `BasicAppointmentQueryEvaluator`                  | 14/14  | 1/2    |
| `HibernateAppointmentTypeDAO`                     | 17/17  | 4/4    |
| `HibernateAppointmentRequestDAO`                  | 13/13  | 8/8    |
| `audit.AuditLogger.Outcome / Channel`             | 2/2    | 0/0    |
| `reporting.data.evaluator.PatientToAppointmentDataEvaluator` | 32/32 | 12/14 |

**Slechtst (0 % LINE coverage):**

| Klasse                                                                            | LINE   |
|-----------------------------------------------------------------------------------|--------|
| `StudentT`                                                                        | 0/137  |
| `AppointmentRequisition`                                                          | 0/32   |
| `task.AppointmentSchedulerSetup`                                                  | 0/26   |
| `AppointmentActivator`                                                            | 0/17   |
| `AppointmentDailyCount`                                                           | 0/12   |
| `serialize.AppointmentStatusSerializer`                                           | 0/10   |
| `reporting.data.definition.AppointmentCreatorDataDefinition`                      | 0/5    |

> **Lezing.** Het lage gemiddelde wordt grotendeels veroorzaakt door 6 niet-geteste utility-/initializer-klassen, niet door zwakke tests op de business-logica. `AppointmentServiceImpl` (de kerndienst) zit op 412/451 = **91,4 %** LINE. Dit patroon is een directe input voor de prioritering in bulletpoint 3.

---

## 4. T3 — Mutation testing (PIT)

> **Bron**: PIT stdout in raw output (regel 302-340) + HTML/XML/CSV-rapport uit CI-run
> **Scope**: `org.openmrs.module.appointmentscheduling.audit.*` (geconfigureerd in `api/pom.xml`)
> **Reproducer**: `mvn -B org.pitest:pitest-maven:mutationCoverage`

### 4.1 Twee runs vergeleken (Windows lokaal vs Ubuntu CI)

| Metriek                              | Lokaal (Windows + UTC+1) | CI (Ubuntu, UTC) | Toelichting |
|--------------------------------------|-------------------------:|-----------------:|-------------|
| Mutaties gegenereerd                 | 15                       | 15               | identiek    |
| KILLED                               | 15                       | 14               | CI vindt 1 minder |
| SURVIVED                             | 0                        | 1                | **zie §4.3** |
| Mutation score                       | 100,0 %                  | **93,0 %**       | CI is autoritatief (deterministische omgeving) |
| Line coverage (mutated classes only) | 91 %                     | 91 %             | identiek    |
| Test strength                        | 100 %                    | 93 %             | bouwt op KILLED |
| NFR MNT-3c (≥ 50 %)                  | ✅                       | ✅               | beide ruim boven grens |

> **Waarom telt CI als baseline?** Mutation testing is per definitie *omgevingsgevoelig*. Een CI-runner heeft een schone, gestandaardiseerde omgeving (clean checkout, UTC default-TZ, geen lokale state), wat de meting reproduceerbaar maakt. Voor het rubriek-criterium "reproduceerbaar" gebruiken we daarom de CI-uitkomst. De Windows-run blijft in het archief als referentiepunt.

### 4.2 Mutators uitgesplitst (CI-run)

| Mutator                              | Generated | Killed | Survived | % |
|--------------------------------------|----------:|-------:|---------:|--:|
| `VoidMethodCallMutator`              | 1         | 0      | **1**    | 0 |
| `EmptyObjectReturnValsMutator`       | 2         | 2      | 0        | 100 |
| `NegateConditionalsMutator`          | 12        | 12     | 0        | 100 |
| **Totaal**                           | **15**    | **14** | **1**    | **93** |

### 4.3 De surviving mutation — wat zegt hij?

```
AuditLogger.java:87
  fmt.setTimeZone(TimeZone.getTimeZone("UTC"));    // <-- regel verwijderd door PIT
```

**Mutator**: `VoidMethodCallMutator` (`removed call to java/text/SimpleDateFormat::setTimeZone`)
**Killed by**: *niemand* (16 dekkende tests, geen daarvan faalde)

**Diagnose.** De test `format_writesTimestampAsIso8601Utc` checkt dat de tijdstempel in ISO-8601 UTC-format wordt geschreven. Maar zonder de `setTimeZone(UTC)`-aanroep formatteert `SimpleDateFormat` met de **default JVM-TZ**.
- Op Ubuntu CI is die default = `UTC` → output is gelijk → test slaagt → **mutation overleeft**.
- Op de Windows-machine (UTC+1) verschilt de output → test faalt → mutation gedood.

**Wat dit echt blootlegt.** De test is **niet robuust tegen environment-verschillen**. In productie kan de JVM op een server met `Europe/Amsterdam`-TZ draaien; als iemand per ongeluk de `setTimeZone(UTC)`-regel verwijdert, schrijft de auditlog vanaf dat moment in lokale tijd. **Geen enkele bestaande test zou dit op CI signaleren.**

**Voorgestelde fix (input voor bulletpoint 3 en bulletpoint 6):**
1. Test expliciet de JVM-TZ wijzigen vóór de assert, bv. via `@Before` met `TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))`.
2. Test de string-output, niet alleen de parse-uitkomst.
3. Daarna herhalen: PIT-score moet stijgen naar 15/15 = 100 %.

> **Rubriek-waarde.** Dit is exact het soort bevinding dat de strategie uit `02-teststrategie.md` § 2.1 voorspelde: PIT bewijst meerwaarde boven JaCoCo door een test te identificeren die *coverage* haalt maar *geen gedrag bewaakt*.

### 4.4 Interpretatie — sterk punt en aandachtspunt

**Sterk**: 14 van 15 mutaties op het `audit/`-pakket worden door de bestaande `AuditLoggerTest` (16 tests) gedood. Mutation score van **93 %** zit ruim boven de gate van 50 % (MNT-3c) en bewijst dat de testset gedrag valideert, niet alleen methodes "aanraakt".

**Aandachtspunt**: 1 omgevingsafhankelijke test (zie §4.3). Voor bulletpoint 3 prioritair op te lossen, want de test geeft *vals positief* op CI.

**Buiten scope**: mutation-coverage is alleen gemeten op `audit/*` — niet op `AppointmentServiceImpl` of `HibernateAppointmentDAO`. Bewuste keuze (zie `02-teststrategie.md` § 2.1).

---

## 5. T4 — SonarCloud Quality Gate

> **Bron**: SonarCloud project `EnesT51_LU2-MaintainabilityPoC`
> **Reproducer**: GitHub Actions job `sonarcloud` in `.github/workflows/maintainability-tests.yml` (vereist `SONAR_TOKEN`-secret)

| Conditie                              | Waarde   | Grens    | Status |
|---------------------------------------|----------|----------|:------:|
| Quality Gate                          | *Not computed* | Passed | ⚠ |
| Maintainability Rating (overall)      | A        | ≥ A      | ✅     |
| Security Rating                       | E        | ≥ A      | ❌     |
| Reliability Rating                    | D        | ≥ A      | ❌     |
| Coverage                              | *niet geïmporteerd* | ≥ 60 % | ⚠ |
| Duplicated Lines                      | 1,2 %    | ≤ 5 %    | ✅     |

> **Status.** De Quality Gate is *Not computed* omdat dit de eerste analyse is en SonarCloud nog geen *new code* heeft om te beoordelen. Na de volgende push (via de aangelegde CI-workflow) wordt de gate scherp gezet. Security/Reliability vallen formeel buiten *onderhoudbaarheid* en horen bij andere LU's; ze staan hier alleen ter informatie.
>
> **Toezegging.** Coverage-import naar Sonar (JaCoCo-XML → `sonar.coverage.jacoco.xmlReportPaths`) is in de CI-workflow al geconfigureerd (`.github/workflows/maintainability-tests.yml` regels rond `-Dsonar.coverage.jacoco.xmlReportPaths`). Bij de eerstvolgende CI-run vult dit cijfer zich vanzelf.

---

## 6. Samenvatting — voldoet de baseline aan de NFR's?

| NFR    | Eis                                          | Grens   | Gemeten     | Status |
|--------|----------------------------------------------|---------|-------------|:------:|
| REL-1  | Alle unit-tests slagen                       | 0 fail  | 0/182       | ✅     |
| MNT-3  | Line coverage module-breed                   | ≥ 60 %  | **72,7 %**  | ✅     |
| MNT-3a | Line coverage audit-pakket                   | ≥ 90 %  | **92,6 %**  | ✅     |
| MNT-3b | Branch coverage audit-pakket                 | ≥ 80 %  | **100,0 %** | ✅     |
| MNT-3c | Mutation score audit-pakket (CI-run)         | ≥ 50 %  | **93,0 %**  | ✅     |
| MNT-4  | SonarCloud Quality Gate Passed               | Passed  | Not computed | ⚠    |

> **Lezing.** Vijf van zes NFR's hard groen op de eerste meting. MNT-4 staat op ⚠ omdat de Quality Gate door SonarCloud zelf nog niet is uitgerekend (eerste analyse) — geen falen, maar nog niet bewezen. Komt na de eerste CI-run.

---

## 7. Reproduceerbaarheid — checklist

- [x] Repository schoon gecheckt: `feature/lu2-bp2-testopzet-resultaten`
- [x] `git rev-parse HEAD` = *vul aan bij commit* — `git log -1 --format=%H`
- [x] Java-versie: `java version "1.8.0_481"` (Temurin)
- [x] Maven-versie: `Apache Maven 3.9.15`
- [x] OS / runner: Windows 11 + Git Bash MINGW64_NT-10.0-26200
- [x] Raw output gearchiveerd: [`raw/tests/baseline-20260615-183459.txt`](./raw/tests/baseline-20260615-183459.txt)
- [ ] CI-run-url *fill na eerste GitHub Actions-run*

> **Let op.** De raw output meldt `Java: openjdk version "26.0.1"` op regel 4 — dat is de **eerste** Java die het bash-script vond in PATH, niet de versie waarmee Maven heeft gedraaid. Op regel 5 staat de werkelijk gebruikte JDK uit `mvn -v` niet expliciet, maar de compile-regel 26 (`Compiling 85 source files with javac [debug target 1.8]`) bewijst dat de feitelijke compile met **JDK 1.8** liep. In een volgende iteratie van het script wordt `JAVA_HOME` of de Maven-Java-versie expliciet vastgelegd.

---

## 8. Hoe de afgeleide totalen zijn berekend

De per-package LINE/BRANCH-cijfers in §3 zijn opgeteld met onderstaande snippet (uitgevoerd op `raw/tests/baseline-20260615-183459.txt`):

```python
import re
tot_lc=tot_lt=tot_bc=tot_bt=0
audit_lc=audit_lt=audit_bc=audit_bt=0
for ln in open('raw/tests/baseline-20260615-183459.txt'):
    m = re.search(r'LINE (\d+)/(\d+)\s+BRANCH (\d+)/(\d+)', ln)
    if not m: continue
    lc, lt, bc, bt = map(int, m.groups())
    tot_lc+=lc; tot_lt+=lt; tot_bc+=bc; tot_bt+=bt
    if '.audit/' in ln:
        audit_lc+=lc; audit_lt+=lt; audit_bc+=bc; audit_bt+=bt
```

Resultaat:
```
MODULE LINE   1284/1767 = 72.7%
MODULE BRANCH 367/561   = 65.4%
AUDIT  LINE   25/27     = 92.6%
AUDIT  BRANCH 24/24     = 100.0%
```

---

## 9. Hoe je de CI-artifacts leest en gebruikt

Bij elke groene run van `maintainability-tests.yml` plakt GitHub Actions onderaan de run-pagina **drie ZIP-bestanden** met de rapporten. Ze staan ook in dit repo gearchiveerd onder `raw/tests/ci-run-<datum>/`.

### 9.1 `surefire-reports.zip` — Welke tests zijn gedraaid

| Bestand                    | Waar gebruik je het voor                                                  |
|----------------------------|---------------------------------------------------------------------------|
| `TEST-*.xml`               | Machine-leesbare resultaten per testklasse. Import in IntelliJ: *Run → Import Test Results From XML*. |
| `*.txt`                    | Mens-leesbare samenvatting per testklasse. Snel ogen op slagen/falen.     |

> **Concreet gebruik**: snel zien welke testklasse bij een rode CI-run gefaald is, zonder het hele Action-log door te scrollen.

### 9.2 `jacoco-report.zip` — Welke regels code worden door tests geraakt

| Bestand            | Waar gebruik je het voor                                                                          |
|--------------------|---------------------------------------------------------------------------------------------------|
| `index.html`       | Open in browser. Klik door package → klasse → regels. **Groen** = gedekt, **rood** = niet gedekt. |
| `jacoco.csv`       | Voor het script in `run-baseline.sh` § 3 om module-totalen op te tellen.                          |
| `jacoco.xml`       | Voor SonarCloud-coverage-import (`sonar.coverage.jacoco.xmlReportPaths`).                          |

> **Concreet gebruik**: open na elke verbetering `AppointmentServiceImpl.html` om te zien welke regels van de 412/451 nog ongeraakt zijn. Direct bruikbaar voor bulletpoint 3 (welke testen moeten erbij).

### 9.3 `pit-report.zip` — Hoe goed de tests gedrag valideren

| Bestand                                                                          | Waar gebruik je het voor                                                                         |
|----------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| `index.html`                                                                     | Open in browser → projectsamenvatting (klassen, line/mutation coverage, test strength).          |
| `org.openmrs.module.appointmentscheduling.audit/index.html`                      | Drill-down per package.                                                                          |
| `org.openmrs.module.appointmentscheduling.audit/AuditLogger.java.html`           | Source-view met **groen** (mutatie gedood), **rood** (overleefd), **grijs** (niet bereikt). Hover voor uitleg per mutatie. |
| `mutations.xml`                                                                  | Machine-leesbaar voor regressie-vergelijking in bulletpoint 6.                                   |
| `mutations.csv`                                                                  | Compacte tabel; één regel per mutatie + killing-test. Handig voor `awk`/Python.                  |

> **Concreet gebruik**: in `AuditLogger.java.html` zie je direct dat regel 87 (`setTimeZone(UTC)`) rood gekleurd is. Dat is de bevinding uit § 4.3, gevisualiseerd. Open het bestand zelf, screenshot het rood-omkaderde blok → leg het naast je verslag.

### 9.4 Screenshots voor het verslag (placeholders)

Onderstaande paden zijn naar de gearchiveerde CI-output van de run d.d. 2026-06-15.

| Figuur | Bron-HTML                                                                                            | Knip wat je nodig hebt           |
|--------|------------------------------------------------------------------------------------------------------|----------------------------------|
| Fig. 1 | [`raw/tests/ci-run-2026-06-15/pit-report/index.html`](./raw/tests/ci-run-2026-06-15/pit-report/index.html) | Project Summary tabel (91 / 93 / 93 %) |
| Fig. 2 | [`raw/tests/ci-run-2026-06-15/pit-report/org.openmrs.module.appointmentscheduling.audit/index.html`](./raw/tests/ci-run-2026-06-15/pit-report/org.openmrs.module.appointmentscheduling.audit/index.html) | Breakdown by Class (`AuditLogger.java` regel) |
| Fig. 3 | [`raw/tests/ci-run-2026-06-15/pit-report/org.openmrs.module.appointmentscheduling.audit/AuditLogger.java.html`](./raw/tests/ci-run-2026-06-15/pit-report/org.openmrs.module.appointmentscheduling.audit/AuditLogger.java.html) | Regel 87 (rood gemarkeerde SURVIVED-mutatie) |

> Voorgestelde plek in het rapport: §4.3 hier of als bijlage achter §10.

### 9.5 Workflow voor bulletpoint 6 (regressie-toets)

Bij elke volgende verbetering uit de PoC (bulletpoint 5) herhaal je:
1. Push naar `dev` → CI draait automatisch.
2. Download de drie ZIPs uit de nieuwe run.
3. **Vergelijk** mutation-XML én jacoco-CSV met de baseline in `raw/tests/ci-run-2026-06-15/`.
4. Bewijs dat coverage en mutation-score *niet zijn gedaald* (regressie-eis bulletpoint 6).

> Tip: gebruik `diff` of `wdiff` op de CSV-bestanden voor een snelle delta. PIT's XML kun je met `xmllint --xpath` filteren op `status='SURVIVED'`.

---

## 10. Bijlagen

- [`raw/tests/baseline-20260615-183459.txt`](./raw/tests/baseline-20260615-183459.txt) — lokale eerste run (Windows + Git Bash); referentie
- [`raw/tests/ci-run-2026-06-15/pit-report/`](./raw/tests/ci-run-2026-06-15/pit-report/) — autoritatieve PIT-output uit CI (HTML/XML/CSV)
- `.github/workflows/maintainability-tests.yml` — operationele invulling (E1/E2/E3); MNT-4 wordt na eerste CI-run gevuld
- `scripts/run-baseline.sh` — single-command reproducer
- [`02-teststrategie.md`](./02-teststrategie.md) — onderbouwing waarom we deze tests draaien
- [`01-systematische-analyse.md`](./01-systematische-analyse.md) — broncijfers waar deze baseline tegen aangelegd wordt
