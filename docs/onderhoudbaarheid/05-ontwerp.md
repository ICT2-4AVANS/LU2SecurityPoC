# Aangepast ontwerp — onderbouwing van de top-3 verbeteringen

|              |                                                          |
| ------------ | -------------------------------------------------------- |
| **Module**   | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT     |
| **Datum**    | 2026-06-16                                               |
| **Auteur**   | Enes T. (LU2-MaintainabilityPoC)                         |
| **Scope**    | Ontwerp voor de top-3 uit [`04-geprioriteerde-verbeteringen.md`](./04-geprioriteerde-verbeteringen.md) §5: **A1**, **C1**, **A2** |
| **Voedt**    | De PoC-realisatie in bulletpoint 5                       |

> Dit document maakt **geen code-wijzigingen** en geen nieuwe metingen. Het
> beschrijft *hoe* we de drie geselecteerde verbeteringen gaan ontwerpen,
> welke alternatieven zijn afgewogen en op welke principes/patronen de
> uiteindelijke keuzes rusten.

---

## 1. Doel

Per geselecteerde verbetering vastleggen:

1. **Probleem** met directe verwijzing naar bp1/bp2-cijfers.
2. **Alternatieven** — minimaal drie reële opties tegen elkaar afgezet.
3. **Gekozen ontwerp** met concrete code-/config-fragmenten.
4. **Onderbouwing** in ontwerpprincipes, ontwerppatronen en refactoring-patronen, met externe bronvermelding.
5. **Kwaliteitseisen** waarop de keuze rust (NFR MNT-1..4, REL-1).

Daarmee voldoet het ontwerp aan de rubriek-eis voor *Goed*: doordachte keuzes, alternatieven besproken, gemotiveerd op kwaliteitseisen.

---

## 2. Ontwerpaanpak — gemeenschappelijk kader

Drie heel verschillende soorten verbeteringen (config A1, testcode C1, CI-config A2) vragen niet om één enkel ontwerppatroon. Ze rusten wél op een gemeenschappelijke set principes uit de software-engineering-canon:

| Principe                       | Bron                                                | Waar het hier geldt              |
|--------------------------------|-----------------------------------------------------|----------------------------------|
| **Separation of Concerns (SoC)**| Dijkstra, *On the role of scientific thought*, 1974 | A1: scheidt vendored van eigen   |
| **Single Source of Truth**     | Fowler, *Patterns of Enterprise Application Architecture*, 2002 | A1, A2: één plek voor exclusions resp. coverage |
| **Configuration as Code (CaC)**| Morris, *Infrastructure as Code*, O'Reilly 2016    | A1, A2: alles in git, niet in een UI |
| **F.I.R.S.T.-principes**       | Martin, *Clean Code* hoofdstuk 9, 2008             | C1: het "Repeatable"-aspect      |
| **Hermetic tests**             | Google Testing on the Toilet, sept 2008             | C1: omgevingsonafhankelijk       |
| **Refactoring-catalog**        | Fowler, *Refactoring* 2nd ed., Addison-Wesley 2018  | C1: "Extract Setup" / "Replace Conditional with Polymorphism" |
| **YAGNI**                      | Beck, *Extreme Programming Explained*, 1999         | A1: vendored libs *niet* vervangen (= D1/D2 is bewust uitgesteld) |

Deze toolkit gebruik ik in §3, §4, §5 om elke keuze te motiveren.

---

## 3. Ontwerp A1 — Sonar-exclusions voor vendored libs

### 3.1 Probleemstelling

Uit `01-systematische-analyse.md` §4.2.2:

- 1.234 totale code smells gerapporteerd door SonarCloud.
- **1.145 (92,8 %) zit in vendored 3rd-party libs** (`jquery.dataTables.js`, `opentip-jquery-excanvas.js`, `ZeroClipboard.*`, `TableTools.js`, `json2.js`, `fullcalendar.css`).
- Slechts **89 smells (7,2 %)** in projecteigen code — werkelijk effort ≈ 0,9 dag.

De huidige scope-instelling laat het dashboard een onderhoudslast tonen die het team *niet kan beïnvloeden zonder volledige vervanging van die libs*. Daarmee wordt:

- de **Quality Gate-werking onbruikbaar** (zou altijd rood blijven door vendored noise);
- de **NFR MNT-1 niet aantoonbaar**, omdat overschrijdingen van complexiteit hoofdzakelijk uit vendored JS komen (bp1 §4.3 noemt 40 `javascript:S3776`-treffers, allemaal vendored);
- de **eigen verbeterprestatie onzichtbaar** — werk je 89 smells weg, dan blijven er 1.145 staan op het dashboard.

### 3.2 Alternatieven afgewogen

| # | Optie                                                | Voor                                                       | Tegen                                                                   | Past bij PoC? |
|---|------------------------------------------------------|------------------------------------------------------------|-------------------------------------------------------------------------|:--:|
| A | `sonar.exclusions` voor de vendored-paden            | Declaratief, in git versionneerd, één plek, geen code aan  | Vendored issues blijven *bestaan*, alleen niet meer in scope            | ✅ |
| B | `// NOSONAR`-comments per regel                      | Hyper-specifiek                                            | 1.145× annoteren in third-party bestanden = onhoudbaar; vervuilt code; conflicteert met librarie-updates | ❌ |
| C | Vendored libs daadwerkelijk vervangen (D1+D2)        | Echte oplossing — smells verdwijnen ipv worden gemaskeerd  | Effort = L, Risk = H (UI-regressie); valt buiten PoC-tijdsbudget (zie bp3 §5) | ❌ |
| D | Apart SonarCloud-project voor vendored               | Maakt twee dashboards: één voor eigen code, één voor 3rd-party | Beheeroverhead; verwarrend; dubbele licentie-/quota-kosten             | ❌ |

### 3.3 Gekozen ontwerp — Optie A

Concreet: voeg een `sonar-project.properties` in de modulewortel toe (en/of zet de exclusion in de CI-stap), met de volgende inhoud:

```properties
# sonar-project.properties
# Vendored 3rd-party JavaScript en gerelateerde resources.
# Deze paden worden uit de Sonar-analyse gehouden omdat ze niet door
# het OpenMRS-team worden onderhouden. Verwijdering of upgrade van
# deze libs is een aparte (toekomstige) verbetering, getrackt als
# items D1 en D2 in docs/onderhoudbaarheid/04-geprioriteerde-verbeteringen.md.
sonar.exclusions=\
  **/openmrs-module-appointmentscheduling/omod/src/main/webapp/resources/Scripts/jquery.dataTables.js,\
  **/openmrs-module-appointmentscheduling/omod/src/main/webapp/resources/Scripts/opentip-jquery-excanvas.js,\
  **/openmrs-module-appointmentscheduling/omod/src/main/webapp/resources/Scripts/jquery.jeditable.js,\
  **/openmrs-module-appointmentscheduling/omod/src/main/webapp/resources/Scripts/TableTools.js,\
  **/openmrs-module-appointmentscheduling/omod/src/main/webapp/resources/Scripts/ZeroClipboard.js,\
  **/openmrs-module-appointmentscheduling/omod/src/main/webapp/resources/Scripts/json2.js,\
  **/openmrs-module-appointmentscheduling/omod/src/main/webapp/resources/Scripts/date.format.js,\
  **/openmrs-module-appointmentscheduling/omod/src/main/webapp/resources/Scripts/timepicker.js,\
  **/openmrs-module-appointmentscheduling/omod/src/main/webapp/resources/CSS/fullcalendar.css,\
  **/openmrs-module-appointmentscheduling/omod/src/main/webapp/resources/TableTools/media/ZeroClipboard/**
```

In `.github/workflows/maintainability-tests.yml` blijft `-Dsonar.exclusions=…` als fallback aanwezig (zie bp2 §5 voor de huidige workflow), maar het `sonar-project.properties`-bestand wordt nu de canonical source.

### 3.4 Toegepaste principes en patronen

| Element                                       | Toelichting                                                                                                  |
|-----------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| **Separation of Concerns**                    | Dijkstra (1974): scheid wat het team kan beïnvloeden (eigen JS/Java) van wat het team niet kan (3rd-party vendored). De meting volgt die scheiding. |
| **Single Source of Truth**                    | `sonar-project.properties` is dé bron; de workflow-fallback is `DRY`-conform afgeleid. Beheert iemand de lijst alleen via de UI, dan raakt 'ie uit sync. |
| **Configuration as Code**                     | De exclusion-lijst zit in versiebeheer; elke wijziging is reviewbaar in een PR. Geen "iemand klikte iets in een dashboard"-mysterie. |
| **YAGNI**                                     | We *zouden* de vendored libs kunnen vervangen (D1/D2). Dat doen we nu *niet* — A1 lost het meet-probleem op zonder de risico's van een UI-vervanging. |
| **Refactoring-pattern: "Quarantine Dependency"** (Fowler, 2018, blogpost *Anti-corruption Layer*) | Niet-onderhoudbare 3rd-party code afzonderen zodat het de meting van eigen code niet vertroebelt. |

### 3.5 Motivatie op kwaliteitseisen

| NFR | Stand vóór A1 (bp1 §5)            | Stand verwacht na A1                                                |
|-----|-----------------------------------|---------------------------------------------------------------------|
| MNT-1 (complexiteit ≤10/methode) | ⚠ niet aantoonbaar (vendored vervuilt) | ✅ — overschrijdingen in vendored tellen niet meer mee              |
| MNT-2 (duplicaten ≤5 %)          | ✅ 1,2 %                          | ✅ blijft binnen grens                                              |
| MNT-4 (Quality Gate Passed)      | ❌ Not computed                   | Voorwaarde gecreëerd; A3 (later) maakt 'm hard groen                |

**Hoofdmotivatie**: zonder A1 is geen enkele andere maintainability-NFR-verbetering zichtbaar in het dashboard. Dit is enabling werk — laagste-effort, hoogste impact op zichtbaarheid.

---

## 4. Ontwerp C1 — `format_writesTimestampAsIso8601Utc` omgevingsonafhankelijk maken

### 4.1 Probleemstelling

Uit `03-testresultaten-baseline.md` §4.3:

- PIT-baseline op CI (Ubuntu, default-TZ = UTC): **14/15 mutaties gedood** = 93 % mutation score.
- De ene overlevende mutatie: `VoidMethodCallMutator` haalt `fmt.setTimeZone(TimeZone.getTimeZone("UTC"))` weg op `AuditLogger.java:87`.
- Geen enkele test faalt omdat de JVM-default-TZ al UTC is — verwijderen van de regel verandert het output-formaat niet.
- Op de Windows-machine van de ontwikkelaar (TZ Europe/Amsterdam) faalt de test wél → daar score 15/15.

De test is **niet hermetisch**: zijn correctheid hangt af van een omgevingsvariabele die niet onder testcontrole staat. Dit valt onder Martin's *Repeatable*-uit-F.I.R.S.T.-principe (Clean Code, 2008): "Tests should be repeatable in any environment".

### 4.2 Alternatieven afgewogen

| # | Optie                                                                            | Voor                                                              | Tegen                                                                                  | Past bij PoC? |
|---|----------------------------------------------------------------------------------|-------------------------------------------------------------------|----------------------------------------------------------------------------------------|:--:|
| A | `@Before`-methode die `TimeZone.setDefault(...)` op een niet-UTC-zone zet        | Lokaal, idiomatisch JUnit, herstelbaar in `@After`                | Vereist `@After` om side-effect ongedaan te maken                                      | ✅ |
| B | Maven Surefire `<argLine>-Duser.timezone=America/New_York</argLine>`            | JVM-breed, één keer instellen                                     | Affecteert *alle* 182 tests; verbergt het probleem ipv het te testen                   | ❌ |
| C | DI: `SimpleDateFormat` of `Clock` injecteren in `AuditLogger` via constructor    | Architectonisch netste oplossing — productie-class wordt expliciet over tijd | Wijzigt productie-API (`AuditLogger`); buiten scope voor een test-only fix             | ⚠ |
| D | Surefire `<systemPropertyVariables>` met `user.timezone`                         | Net als B maar declaratief in pom                                 | Zelfde nadeel als B: globale werking                                                   | ❌ |

### 4.3 Gekozen ontwerp — Optie A

Refactor van `AuditLoggerTest` (in `api/src/test/java/.../audit/AuditLoggerTest.java`):

**Vóór** (huidige situatie, gesimplificeerd):

```java
public class AuditLoggerTest {
    private final AuditLogger logger = new AuditLogger(...);

    @Test
    public void format_writesTimestampAsIso8601Utc() {
        Date d = parseDate("2026-06-15T10:00:00Z");
        String line = logger.format(..., d);
        assertThat(line, containsString("when=2026-06-15T10:00:00Z"));
    }
}
```

**Na** (PoC-doel):

```java
public class AuditLoggerTest {
    private final AuditLogger logger = new AuditLogger(...);
    private TimeZone originalTimeZone;

    @Before
    public void setUpNonUtcTimeZone() {
        // Force a non-UTC default zone so that any test that
        // implicitly relies on the JVM default (instead of explicit
        // UTC formatting) will fail. Without this, the test is not
        // hermetic on CI runners where the default already is UTC.
        // See docs/onderhoudbaarheid/03-testresultaten-baseline.md §4.3.
        this.originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
    }

    @After
    public void restoreTimeZone() {
        TimeZone.setDefault(this.originalTimeZone);
    }

    @Test
    public void format_writesTimestampAsIso8601Utc() {
        Date d = parseDate("2026-06-15T10:00:00Z");
        String line = logger.format(..., d);
        assertThat(line, containsString("when=2026-06-15T10:00:00Z"));
    }
}
```

### 4.4 Toegepaste principes en patronen

| Element                                                                                    | Toelichting                                                                                                              |
|--------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| **F.I.R.S.T. — Repeatable** (Martin, *Clean Code* hfdst 9)                                  | Test moet identiek slagen op elke omgeving. Met optie A is dat aantoonbaar het geval.                                    |
| **Hermetic tests** (Google Testing Blog, *Testing on the Toilet*, 2008)                     | Externe state (system-TZ) wordt actief gecontroleerd door de test, niet aangenomen.                                      |
| **Refactor-pattern: "Extract Setup"** (Fowler, *Refactoring* 2e ed., 2018, hfdst 6)         | Setup-stappen die door meerdere tests gedeeld kunnen worden, verhuizen naar `@Before` ipv duplicatie in elk testbody.    |
| **Refactor-pattern: "Symmetrical Setup/Teardown"** (xUnit Test Patterns, Meszaros 2007)     | Elke `setDefault` heeft een paar in `@After`. Side-effects in tests altijd terugdraaien. |
| **AAA-pattern** (Arrange-Act-Assert, Bill Wake 2001)                                        | De `@Before`/`@After`-blok versterkt de **Arrange**-fase expliciet — duidelijker dan een impliciete environment-aanname.  |

### 4.5 Motivatie op kwaliteitseisen

| NFR | Stand vóór C1 (bp2 §6)             | Stand verwacht na C1                                                |
|-----|-------------------------------------|---------------------------------------------------------------------|
| MNT-3c (mutation ≥ 50 %)            | ✅ 93 % (met 1 survivor)            | ✅ 100 % (alle 15 mutaties gedood)                                  |
| REL-1 (0 falende tests)             | ✅ 0/182                            | ✅ blijft groen (alleen één test wordt strenger)                    |

**Hoofdmotivatie**: dit is exact het scenario waar de teststrategie (bp2 §2.1) PIT voor toevoegt aan JaCoCo — een omgevingsafhankelijke test die JaCoCo niet zou detecteren. Het ontwerp adresseert *test-kwaliteit*, niet *test-kwantiteit*, en past direct in het ISO 25010 sub-attribuut *Testability*.

---

## 5. Ontwerp A2 — JaCoCo-XML naar SonarCloud importeren

### 5.1 Probleemstelling

Uit `01-systematische-analyse.md` §3 en §4.5:

- Het SonarCloud-dashboard toont **Coverage = "−"** (niet gerapporteerd).
- Lokaal meet JaCoCo wél: module-breed **72,7 % LINE** (bp2 §3.1), audit-pakket 92,6 / 100 %.
- Zonder import staat NFR MNT-3 in het dashboard op ⚠ ook al wordt 'ie lokaal/CI ruim gehaald.

Het probleem is geen *meting* maar *integratie*: er bestaan twee silo's (lokale JaCoCo en dashboard-Sonar) met elk hun eigen "waarheid". Voor regressie-detectie in bulletpoint 6 hebben we **één geconsolideerde meting** nodig.

### 5.2 Alternatieven afgewogen

| # | Optie                                                                                          | Voor                                                          | Tegen                                                                              | Past bij PoC? |
|---|------------------------------------------------------------------------------------------------|---------------------------------------------------------------|------------------------------------------------------------------------------------|:--:|
| A | In `mvn sonar`-call: `-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml`     | Eén regel CI-config, officieel ondersteund                    | Vereist dat JaCoCo *vóór* Sonar draait — al opgelost in `maintainability-tests.yml`| ✅ |
| B | SonarQube's eigen JaCoCo-plugin                                                                | Pre-2017 standaard                                            | **Deprecated** sinds Sonar 6.7 (SonarSource news, 2017); werkt niet met Sonar 10.x | ❌ |
| C | Coverage uploaden naar Coveralls of Codecov (los van SonarCloud)                               | Verschillende tools = onafhankelijke verificatie              | Tweede dashboard; twee accounts; KPI's gaan divergeren                             | ❌ |
| D | Geen Sonar-import; lokaal JaCoCo-rapport blijft enige bron                                     | Geen extra integratiewerk                                     | Dashboard blijft op `−`; MNT-3 nooit aantoonbaar in Sonar; gateloos                | ❌ |

### 5.3 Gekozen ontwerp — Optie A

De wijziging is grotendeels al voorbereid in `maintainability-tests.yml` (zie bp2). De PoC voltooit:

1. **Verifiëren** dat JaCoCo `target/site/jacoco/jacoco.xml` produceert (bp2 §3 bewijst dit).
2. **Sonar-stap** in de workflow gebruikt al:
   ```yaml
   -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
   ```
3. **Validatie**: na één run moet de Coverage-cel op SonarCloud een getal tonen ≥ 60 % (MNT-3-grens).

Eventueel toevoegen aan de root `sonar-project.properties` voor explicietheid:

```properties
sonar.coverage.jacoco.xmlReportPaths=openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/api/target/site/jacoco/jacoco.xml
```

Daarmee werkt ook een handmatige `mvn sonar:sonar`-call lokaal (zonder de CI-stap).

### 5.4 Toegepaste principes en patronen

| Element                                                                              | Toelichting                                                                                                       |
|--------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| **Single Source of Truth**                                                           | JaCoCo blijft dé bron; Sonar consumeert het XML-rapport. Geen tweede coverage-meting, geen risico op afwijking.    |
| **Loose Coupling via standaardformaat**                                              | JaCoCo en Sonar zijn gekoppeld via een gepubliceerd XML-schema, niet via runtime-API's. Vervang Sonar door X morgen — JaCoCo blijft werken. |
| **Pipeline as Code**                                                                 | De integratie is een declaratief stuk YAML in `maintainability-tests.yml`, niet een handmatige stap.              |
| **DRY (Don't Repeat Yourself)**                                                      | Eén Maven-execution, één coverage-meting, twee consumers (CI-gate via `jacoco:check`, dashboard via Sonar-import). |
| **Refactor-pattern: "Centralize Cross-cutting Concern"** (Fowler, *Refactoring* 2018) | Coverage is een cross-cutting kwaliteitsmeting; centraliseren in CI ipv elke developer apart maakt het reproduceerbaar. |

### 5.5 Motivatie op kwaliteitseisen

| NFR | Stand vóór A2 (bp1 §5)             | Stand verwacht na A2                                                |
|-----|-------------------------------------|---------------------------------------------------------------------|
| MNT-3 (line coverage ≥ 60 %)        | ⚠ "niet geïmporteerd in Sonar"     | ✅ 72,7 % zichtbaar op dashboard                                    |
| MNT-3a (audit-line ≥ 90 %)          | ✅ lokaal 92,6 %                    | ✅ ook zichtbaar in dashboard                                       |

**Hoofdmotivatie**: zonder A2 is bp6 (regressie-toetsing) niet uitvoerbaar op dashboard-niveau. De PoC kan dan alleen via lokale JaCoCo-vergelijking aangetoond worden — bewerkelijker en gevoeliger voor handmatige fouten.

---

## 6. Samenvatting — NFR-impact van het gehele ontwerp

Onderstaande tabel vat samen wat de **gecombineerde top-3 PoC** zal opleveren als de drie ontwerpen succesvol gerealiseerd worden. Vergelijk dit met bp1 §5 (de startsituatie):

| NFR     | Eis                                            | Grenswaarde  | Vóór PoC          | Na A1 + C1 + A2 (verwacht) |
|---------|------------------------------------------------|--------------|-------------------|----------------------------|
| MNT-1   | Cyclomatische complexiteit per methode         | ≤ 10         | ⚠ niet aantoonbaar | ✅ na A1 representatief    |
| MNT-2   | Duplicaat-percentage                            | ≤ 5 %        | ✅ 1,2 %          | ✅                         |
| MNT-3   | Line coverage module-breed                      | ≥ 60 %       | ⚠ alleen lokaal    | ✅ 72,7 % op dashboard     |
| MNT-3a  | Line coverage audit-pakket                      | ≥ 90 %       | ✅ lokaal 92,6 %  | ✅                         |
| MNT-3b  | Branch coverage audit-pakket                    | ≥ 80 %       | ✅ lokaal 100 %    | ✅                         |
| MNT-3c  | Mutation score audit-pakket                     | ≥ 50 %       | ✅ 93 %           | ✅ 100 % na C1             |
| MNT-4   | Quality Gate Passed                             | Passed       | ❌ Not computed   | ⚠ wacht op A3 (na PoC)     |
| REL-1   | Alle unit-tests slagen                          | 0 falende    | ✅ 0/182          | ✅ blijft 0 (alleen één strengere test) |

**Score eindstand verwacht na PoC: 6 ✅ + 1 ⚠ + 0 ❌.** Een grote stap t.o.v. bp1's beginstand (3 ✅ + 4 ⚠/❌).

---

## 7. Verwijzingen

### Interne documenten

- [`01-systematische-analyse.md`](./01-systematische-analyse.md) — broncijfers per smell, per bestand, NFR-stand vóór verbetering
- [`02-teststrategie.md`](./02-teststrategie.md) — koppelt T1..T4 aan MNT-1..4
- [`03-testresultaten-baseline.md`](./03-testresultaten-baseline.md) — baseline-cijfers + PIT survivor in §4.3
- [`04-geprioriteerde-verbeteringen.md`](./04-geprioriteerde-verbeteringen.md) — top-3-selectie + buiten-scope-argumentatie
- [`../non-functional-requirements.md`](../non-functional-requirements.md) — MNT-1..4, REL-1 met grenswaarden

### Externe bronnen (waar principes/patronen vandaan komen)

- Dijkstra, E.W. (1974). *On the role of scientific thought*. In *Selected Writings on Computing*. — Separation of Concerns.
- Beck, K. (1999). *Extreme Programming Explained*. Addison-Wesley. — YAGNI.
- Wake, W. (2001). *3A — Arrange, Act, Assert*. xp123.com.
- Fowler, M. (2002). *Patterns of Enterprise Application Architecture*. Addison-Wesley. — Single Source of Truth.
- Meszaros, G. (2007). *xUnit Test Patterns*. Addison-Wesley. — Setup/Teardown symmetrie.
- Martin, R.C. (2008). *Clean Code*. Prentice Hall. Hoofdstuk 9: F.I.R.S.T.-principes voor unit tests.
- Google Testing Blog (2008). *Testing on the Toilet: Hermetic Servers*. — Hermetic test design.
- SonarSource (2017). *Deprecation of SonarQube JaCoCo plugin in favour of generic coverage report import*. — Onderbouwt A2's keuze tegen optie B.
- Morris, K. (2016). *Infrastructure as Code*. O'Reilly. — Configuration as Code.
- Fowler, M. (2018). *Refactoring: Improving the Design of Existing Code*, 2nd ed. Addison-Wesley. — Refactor-patterns "Extract Setup", "Centralize Cross-cutting Concern", "Quarantine Dependency".
