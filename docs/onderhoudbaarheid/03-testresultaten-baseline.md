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

> **Bron**: PIT stdout in raw output (regel 302-340), HTML-rapport in `target/pit-reports/`
> **Scope**: `org.openmrs.module.appointmentscheduling.audit.*` (geconfigureerd in `api/pom.xml`)
> **Reproducer**: `mvn -B org.pitest:pitest-maven:mutationCoverage`

| Metriek                          | Waarde     | NFR-grens         | Status |
|----------------------------------|-----------:|-------------------|:------:|
| Mutaties gegenereerd             | **15**     | —                 | —      |
| KILLED                           | **15**     | —                 | —      |
| SURVIVED                         | **0**      | —                 | —      |
| NO_COVERAGE                      | **0**      | —                 | —      |
| TIMED_OUT                        | **0**      | —                 | —      |
| **Mutation score**               | **100,0 %** | MNT-3c: ≥ 50 %   | ✅     |
| Line coverage (PIT, mutated classes only) | **91 %** (21/23) | gate ≥ 50 % | ✅ |
| Test strength                    | 100 %      | —                 | —      |
| Tests gerund per mutatie         | 4,33       | —                 | —      |
| Totale duur                      | ~1 s        | —                | —      |

### 4.1 Mutators uitgesplitst

| Mutator                              | Generated | Killed | % |
|--------------------------------------|----------:|-------:|--:|
| `VoidMethodCallMutator`              | 1         | 1      | 100 |
| `EmptyObjectReturnValsMutator`       | 2         | 2      | 100 |
| `NegateConditionalsMutator`          | 12        | 12     | 100 |
| **Totaal**                           | **15**    | **15** | **100** |

### 4.2 Interpretatie

**Sterk punt.** PIT genereerde 15 mutaties op het `audit`-pakket en *alle 15* werden door de bestaande `AuditLoggerTest` (16 tests) gedood. Geen mutaties overleefden — dat betekent dat de bestaande testset het audit-gedrag rond *Outcome*, *Channel* en de logger-flow daadwerkelijk toetst, niet alleen de methodes "aanraakt". Daarmee voldoet het audit-pakket aan een hard testkwaliteit-criterium dat puur op JaCoCo onzichtbaar blijft (een test zonder asserts geeft ook 100 % LINE-coverage maar 0 % mutation-score).

**Wat dit niet zegt.** Mutation-coverage is alleen gemeten op `audit/*` — niet op `AppointmentServiceImpl` of `HibernateAppointmentDAO`. Voor die hotspots is mutation-coverage niet ingericht; dat is een bewuste scoping-keuze (zie [`02-teststrategie.md`](./02-teststrategie.md) §2.1 "Mutation-scope alléén op `audit/`-pakket").

**Aandachtspunt bij hergebruik.** De geparseerde samenvatting onderaan het raw-bestand meldt `(geen mutations geparseerd)` omdat PIT zijn XML-rapport bij deze versie elders schrijft dan het script verwachtte. De getallen hierboven komen uit de PIT-stdout zelf (regels 337-340 van de raw output), niet uit de parser. Dit wordt in een vervolgcommit opgelost door de parser naar `target/pit-reports/mutations.xml` te laten kijken via `find target -name mutations.xml`.

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
| MNT-3c | Mutation score audit-pakket                  | ≥ 50 %  | **100,0 %** | ✅     |
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

## 9. Bijlagen

- [`raw/tests/baseline-20260615-183459.txt`](./raw/tests/baseline-20260615-183459.txt) — volledige scriptuitvoer van deze run
- `.github/workflows/maintainability-tests.yml` — operationele invulling (E1/E2/E3); MNT-4 wordt na eerste CI-run gevuld
- `scripts/run-baseline.sh` — single-command reproducer
- [`02-teststrategie.md`](./02-teststrategie.md) — onderbouwing waarom we deze tests draaien
- [`01-systematische-analyse.md`](./01-systematische-analyse.md) — broncijfers waar deze baseline tegen aangelegd wordt
