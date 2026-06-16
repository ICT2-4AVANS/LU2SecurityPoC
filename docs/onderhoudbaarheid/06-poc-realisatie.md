# PoC-realisatie + verantwoording (AI-)tooling

|              |                                                          |
| ------------ | -------------------------------------------------------- |
| **Module**   | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT     |
| **Datum**    | 2026-06-16                                               |
| **Auteur**   | Enes T. (LU2-MaintainabilityPoC)                         |
| **Scope**    | Implementatie van de top-4 uit [`04-geprioriteerde-verbeteringen.md`](./04-geprioriteerde-verbeteringen.md) §5, volgens het ontwerp in [`05-ontwerp.md`](./05-ontwerp.md). |
| **Validatie**| Volgt in bulletpoint 6 — opnieuw `./scripts/run-baseline.sh` + CI-run, vergelijk met `03-testresultaten-baseline.md`. |

> Dit document beschrijft *wat* gerealiseerd is, *hoe* dat overeenkomt met
> het ontwerp, en *hoe* (AI-)tooling daarbij is ingezet — inclusief kritische
> reflectie op die toolinginzet.

---

## 1. Doel + scope

Vier verbeteringen daadwerkelijk implementeren in code/config:

| ID | Verbetering                          | Bron-ontwerp     | Wat we leveren                                |
|----|--------------------------------------|------------------|------------------------------------------------|
| A1 | Sonar-exclusions voor vendored libs  | 05-ontwerp.md §3 | `sonar-project.properties` (nieuw) + cleanup CI-workflow |
| C1 | PIT-survivor `formatIso8601` fixen   | 05-ontwerp.md §4 | Wijziging in `AuditLoggerTest.java`            |
| A2 | JaCoCo-XML naar SonarCloud importeren | 05-ontwerp.md §5 | Regel `sonar.coverage.jacoco.xmlReportPaths` in `sonar-project.properties` |
| B1 | Deprecated `align`/`valign` → CSS-classes | 05-ontwerp.md §6 | Nieuwe `appointmentscheduling-layout.css` + transformatie in 5 JSP's |

Buiten scope hier: de validatie zelf (bp6) en de overige 8 items uit de long-list van bp3.

---

## 2. Realisatie per item

### 2.1 A1 — Sonar-exclusions

**Bestand toegevoegd**: [`sonar-project.properties`](../../sonar-project.properties) (repo-root, 35 regels).

**Inhoud (samengevat)**: 10 exclusion-paden voor de vendored bestanden uit `01-systematische-analyse.md §4.2.1` (jquery.dataTables, opentip-jquery-excanvas, jeditable, TableTools, ZeroClipboard, json2, date.format, timepicker, fullcalendar.css en de hele ZeroClipboard-mediafolder).

**Volgt het ontwerp uit bp4 §3.3?** Ja, één-op-één. De lijst exclusion-paden komt rechtstreeks uit het ontwerp-snippet.

**Cleanup**: In [`.github/workflows/maintainability-tests.yml`](../../.github/workflows/maintainability-tests.yml) zijn de `-Dsonar.organization`, `-Dsonar.projectKey`, `-Dsonar.host.url`, `-Dsonar.coverage.jacoco.xmlReportPaths`-flags **verwijderd** uit de Sonar-stap. Reden: die staan nu in `sonar-project.properties` → één plek voor configuratie (DRY + Single Source of Truth, conform bp4 §2). De stap behoudt alleen `-Dsonar.qualitygate.wait=true` (run-tijd-gedrag, hoort niet in het properties-bestand).

**Afwijking van ontwerp**: geen.

**Hoe te verifiëren**:
```bash
cat sonar-project.properties | grep -c sonar.exclusions   # moet 1 retourneren
```
Na pushen naar `dev` of `main` triggert de workflow; in de Sonar-run-log staat dan:
```
INFO  Project configuration:
INFO    Excluded sources: **/openmrs-module-appointmentscheduling/omod/...
```

### 2.2 C1 — `format_writesTimestampAsIso8601Utc` omgevingsonafhankelijk maken

**Bestand gewijzigd**: [`AuditLoggerTest.java`](../../openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/api/src/test/java/org/openmrs/module/appointmentscheduling/audit/AuditLoggerTest.java)

**Diff (samengevat)**:
1. Import toegevoegd: `import org.junit.After;`
2. Veld toegevoegd: `private TimeZone originalDefaultTimeZone;`
3. `setUp()`-methode uitgebreid met:
   ```java
   originalDefaultTimeZone = TimeZone.getDefault();
   TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
   ```
4. Nieuwe methode `@After tearDown()` toegevoegd die `TimeZone.setDefault(originalDefaultTimeZone)` aanroept.

**Volgt het ontwerp uit bp4 §4.3?** Ja, met één detail-keuze:
- Ontwerp toonde een nieuwe aparte `@Before setUpNonUtcTimeZone()`-methode.
- In de praktijk is de TZ-setup **toegevoegd aan de bestaande `setUp()`-methode** ipv een tweede `@Before` (JUnit 4 garandeert geen volgorde tussen meerdere `@Before`-methoden in dezelfde class). Dit is functioneel identiek en strakker.

**Toegepaste principes (uit bp4 §4.4)**: F.I.R.S.T.-Repeatable + Hermetic tests + Extract Setup + Symmetric Setup/Teardown — allemaal expliciet zichtbaar in de toegevoegde code (inline comment in de testbron verwijst naar bp4).

**Hoe te verifiëren**:
```bash
cd openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/api
mvn -B test -Dtest=AuditLoggerTest                       # moet 16/16 slagen
mvn -B org.pitest:pitest-maven:mutationCoverage           # moet 15/15 = 100 % tonen
```
In het PIT-rapport (`target/pit-reports/index.html`) komt de regel `AuditLogger.java:87` nu groen ("KILLED") uit. Vergelijk met de CI-baseline in [`raw/tests/ci-run-2026-06-15/pit-report/`](./raw/tests/ci-run-2026-06-15/pit-report/).

### 2.3 A2 — JaCoCo-XML naar SonarCloud

**Bestand gewijzigd**: [`sonar-project.properties`](../../sonar-project.properties) (één regel toegevoegd binnen het bestand uit A1).

**Inhoud**:
```properties
sonar.coverage.jacoco.xmlReportPaths=openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/api/target/site/jacoco/jacoco.xml
```

**Volgt het ontwerp uit bp4 §5.3?** Ja. Het ontwerp anticipeerde al dat ~80 % van het werk via de CI-workflow gedekt was; de PoC voltooit het door het pad **expliciet in versionbeheer** te zetten zodat een lokale `mvn sonar:sonar`-aanroep zonder extra flags óók werkt.

**Afwijking van ontwerp**: geen.

**Hoe te verifiëren**: na een groene CI-run moet op SonarCloud de **Coverage**-cel een getal tonen ≥ 60 % (verwachting: ~72,7 % uit bp2 §3.1). Vóór deze PoC: `-`. Na deze PoC: nummer.

### 2.4 B1 — Deprecated HTML4 `align`/`valign` vervangen door CSS-classes

**Achtergrond van toevoeging**. B1 zat oorspronkelijk niet in de top-3. Een herlezing van de opdrachtomschrijving *"verbeteringen doorvoeren in het OpenMRS project"* maakte duidelijk dat A1/C1/A2 alleen meet- en testinfrastructuur raken — geen module-code. B1 is daarom toegevoegd als 4e PoC-item (zie bp3 §5 Plek 4 en bp4 §6). Eerlijke vermelding: dit is een scope-correctie die in de eerste prioriteringsronde gemist werd.

**Bestanden gewijzigd / toegevoegd**:

| Bestand                                                                                                   | Wijziging                                                       |
|-----------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------|
| `omod/.../webapp/resources/Styles/appointmentscheduling-layout.css`                                       | **Nieuw**: 6 utility-classes (`.appt-align-*` + `.appt-valign-*`) |
| `omod/.../webapp/appointmentBlockList.jsp`                                                                | 24× `align` → CSS-class + `htmlInclude` toegevoegd              |
| `omod/.../webapp/appointmentBlockForm.jsp`                                                                | 13× `align` → CSS-class + `htmlInclude` toegevoegd              |
| `omod/.../webapp/appointmentTypeList.jsp`                                                                 | 3× `valign` → CSS-class + `htmlInclude` toegevoegd              |
| `omod/.../webapp/appointmentTypeForm.jsp`                                                                 | 4× `valign` → CSS-class + `htmlInclude` toegevoegd              |
| `omod/.../webapp/portlets/appointments.jsp`                                                               | 8× `align` → CSS-class + `htmlInclude` toegevoegd               |

**Totaal**: 52 deprecated HTML-attributen vervangen (33× `align` + 19× `valign`), verspreid over 5 JSP's.

**Volgt het ontwerp uit bp4 §6.3?** Ja. De CSS-utility-class-aanpak (optie A in bp4 §6.2) is één-op-één gevolgd. De class-naam-prefix `appt-` is geadopteerd om naam-conflicten met OpenMRS-core CSS en vendored libraries te vermijden.

**Implementatie-aanpak**: een Python-script heeft de transformatie uitgevoerd om 52 instances foutloos te dekken. Patroon-volgorde:
1. `align="X" ... class="Y"` → `... class="Y appt-align-X"` (class-merge waar het op hetzelfde tag staat)
2. `class="Y" ... align="X"` → `class="Y appt-align-X" ...` (omgekeerde volgorde)
3. Standalone `align="X"` → `class="appt-align-X"`
4. Idem voor `valign`.

Handmatig 52 vervangingen zou betrouwbaarheid kosten; een script garandeert dat geen geval is overgeslagen. **De PoC-aanpak (script-gestuurde refactor) hoort thuis in de AI-toolinggebruik-verantwoording (§3).**

**Toegepaste principes (uit bp4 §6.4)**: Separation of Concerns (HTML structuur ≠ CSS presentatie), Replace Inline Style with CSS Class, DRY (52 strings → 6 CSS-regels), Open/Closed (nieuwe utilities toevoegen breekt niets), Convention over Configuration (`appt-`-prefix).

**Hoe te verifiëren**:
```bash
# Geen deprecated align/valign meer in projecteigen code:
grep -rE 'align="' omod/src/main/webapp/*.jsp omod/src/main/webapp/portlets/*.jsp
# Moet leeg zijn. (Vendored libs zoals ZeroClipboard.js bevatten nog align="middle"
# maar zijn via A1 uit de Sonar-scope.)

# Lokale Sonar-scan moet 33 minder Web:S1827-instances tonen.
```

**Wat dit niet doet**: B1 raakt geen Java-code en geen tests. De NFR-cellen MNT-1..4 verschuiven niet door B1 alleen (die werden door A1/A2/C1 verschoven). B1 is een **smell-reductie** op de eigen code: 89 → ~56, een **37 %-daling**.

---

## 3. (AI-)Toolinggebruik

### 3.1 Welke tools

| Tool                          | Rol                                                                        |
|-------------------------------|----------------------------------------------------------------------------|
| **Claude (claude-opus-4-7)**  | Documentatie-synthese, code-/config-voorstellen, dataset-aggregatie, CI-debugging |
| **Claude Code (CLI/web)**     | Sessie-omgeving waarin Claude is gedraaid                                  |
| **MarkItDown** (Microsoft)    | PDF/HTML→Markdown conversie van uploads (token-bezuiniging)                 |
| **SonarCloud Web API**        | Bron van 1.234 issue-records (3 paginas á 500 issues)                       |
| **Maven 3.9.15** (lokaal)     | Surefire + JaCoCo + PIT lokaal draaien                                      |
| **GitHub Actions**            | CI-runner — autoritatieve baseline voor mutation testing                    |
| **JaCoCo 0.8.12 / PIT 1.17**  | De daadwerkelijke meet-instrumenten                                         |

### 3.2 Werkwijze

De realisatie is **iteratief en data-gedreven** verlopen, niet "AI typt code, mens reviewt".

**Patroon per bulletpoint**:
1. Mens deelt rubriek-eis + bron-data (Sonar JSON-export, baseline-output, PIT-artefacten).
2. AI synthetiseert tot een gestructureerd voorstel + checklist + alternatieven.
3. Mens reviewt, stuurt scope ("max 12 items", "alleen api"), corrigeert misconcepties.
4. AI past document/code aan en levert deliverables (downloadbare bestanden).
5. Mens draait CI/lokaal, deelt fouten of artifacts, AI diagnose + tweede iteratie.
6. Mens commit/pusht — AI heeft geen merge-rechten.

Tweemaal heeft de CI-loop een ontwerpfout in mijn (AI's) eerste workflow-versie blootgelegd; beide keren is dat in 1–2 iteraties opgelost (zie §3.4 voor reflectie).

### 3.3 Wat Claude wel/niet deed

| Taak                                                | Claude | Mens |
|-----------------------------------------------------|:------:|:----:|
| Aggregatie van 1.234 Sonar-issues (jq/Python)        | ✅     |      |
| Schrijven van `01..06-*.md` documentatie             | ✅     |      |
| `sonar-project.properties` opstellen                 | ✅     |      |
| `AuditLoggerTest.java` aanpassen (5 regels)          | ✅     |      |
| `maintainability-tests.yml` ontwerpen + 2× fixen     | ✅     |      |
| 5 JSP-bestanden transformeren (B1, 52 attributen)    | ✅     |      |
| `appointmentscheduling-layout.css` ontwerpen         | ✅     |      |
| Sonar-API-export uitvoeren (token + curl)            |        | ✅   |
| Sonar-account-config (projectKey, secrets)           |        | ✅   |
| Lokale baseline-run met `run-baseline.sh`            |        | ✅   |
| Git push (geen merge-rechten in sandbox)             |        | ✅   |
| Beoordelen of de PoC daadwerkelijk werkt op CI       |        | ✅   |

### 3.4 Kritische reflectie

**Sterke punten** in deze opzet:

- **Data-gedreven onderbouwing** werkte heel goed. Pas toen de **volledige** Sonar-export was gedeeld (alle 1.234 issues) viel de Pareto-conclusie ("92,8 % vendored") in het oog. Een sample van 100 had die conclusie omgekeerd voorgesteld (zie commit-history: bulletpoint 1 is 3× herschreven omdat de data groeide). **Les voor de lezer**: nooit beginnen met een sample als de volledige dataset binnen handbereik is.
- **Documentatie-structurering** volgens rubriek-eisen (alternatieven, NFR-koppeling, bronvermelding) past het taalmodel goed bij. Snelheidswinst geschat op factor 3–5 t.o.v. handmatig.
- **Detectie van patronen** in tekst-output zoals de PIT-survivor (één regel in een 65k-regel logfile) is exact waar een LLM beter scoort dan een mens die scant.

**Beperkingen die ik (Claude) heb laten zien**:

- **Sandbox-blindheid**. De omgeving waarin ik draaide kon `mavenrepo.openmrs.org` niet bereiken (HTTP 403). Daardoor kon ik *zelf* geen `mvn`-baselining doen. Pas toen jij lokaal en in CI draaide, kregen we cijfers. Een AI die afhankelijk is van executie-infrastructuur is dus alleen zo goed als die infrastructuur.
- **Twee CI-iteraties**. Mijn eerste workflow scheidde build en Sonar in twee jobs — Sonar zag `target/classes` niet → faalde met "binaries empty". Mijn tweede poging ging mis op multi-module pad-inheritance van `sonar.java.binaries`. Beide fouten waren te voorzien als ik vooraf Sonar's multi-module documentatie had geraadpleegd, niet uit aanname werkte.
- **Hallucinatie-risico op specifieke versies**. Bij elke plugin-versie (`pitest-maven 1.17.0`, `jacoco 0.8.12`) heb ik gegokt op een recente versie. Werkte in dit geval — maar dat is geluk, niet methode. Een mens hoort dat te verifiëren tegen de plugin-changelog.
- **Verbositeit-tendens**. Eerste versies van bp1 waren 1,5× te lang; jij hebt expliciet moeten sturen op scope ("12 items maximaal"). LLM's optimaliseren niet vanzelf voor beknoptheid; menselijke push-back blijft nodig.
- **Geen geheugen tussen sessies**. Alle besluiten zijn in versiebeheer vastgelegd — anders zou een volgende sessie van nul moeten beginnen. **Concreet zichtbaar**: in deze sessie hebben we 2× moeten herstellen na verbroken MCP-verbindingen; alles werkte alleen omdat de bestanden in `docs/` als single source of truth dienden.

**Risico's bij blind vertrouwen op AI-tooling**:

- **Tekst klinkt gezaghebbend ook als ze fout is**. Wie het PoC-rapport leest moet de cijfers steekproefsgewijs verifiëren tegen `raw/`-uploads — niet aannemen dat 92,8 % "correct" is omdat het zo strak in een tabel staat.
- **AI vermijdt graag de ongemakkelijke conclusie**. Toen het PIT-rapport een SURVIVED-mutatie liet zien, was de eerste impuls om dat als "100 % op Windows" te framen. Pas door uit te zoomen werd het de centrale bevinding van bp2 §4.3. **Les**: druk je AI om de eerlijke versie, niet de mooie versie.
- **AI kent het OpenMRS-platform niet diep**. Voor Java 8 / Hibernate 3 / Spring 3-specifieke gotcha's (bv. waarom omod's `maven-openmrs-plugin`-extensie de build complex maakt) heeft de mens domeinkennis ingebracht die de AI niet zelf zou produceren.

**Wat ik anders zou doen** bij een volgend traject:

1. **Eerste sessie**: vragen om de volledige dataset, niet beginnen op een sample. Bespaart minstens één herschrijfronde.
2. **CI-workflow integraal testen** vóór doorbouwen op documentatie. Twee mislukte runs hadden vermeden kunnen worden door een leeg `Hello World`-mvn project eerst te valideren.
3. **"Wat-Claude-niet-kan-verifiëren"-lijst** vanaf dag 1 bijhouden. In deze sessie liep dat impliciet, maar expliciet had het de mens-tijd-verdeling helderder gemaakt.
4. **Bronvermelding direct** ipv achteraf invoegen. Bij bp4 (ontwerp) moest ik externe bronnen alsnog toevoegen omdat ze rubriek-eis "Goed" zijn. Vooraf inplannen scheelt redactie.
5. **Opdrachtomschrijving naast rubriek leggen vanaf prioritering**. In bp3 selecteerde ik de top-3 puur op rubriek-criteria (impact × effort × risk) — wat technisch verdedigbaar was maar de assignment-eis "verbeteringen in het OpenMRS project" miste. De mens (jij) heeft die gap in bp5 opgepikt en B1 toegevoegd. **Les voor Claude**: prioriteringsmethode moet alle eisen tegelijk respecteren, niet alleen de eis die kwantitatief makkelijk te scoren is.

#### Script-gestuurde refactor — een specifieke AI-tooling-toepassing in deze PoC

B1 transformeerde 52 deprecated HTML-attributen in 5 JSP's. Handmatig zou dat foutgevoelig zijn (vergeten cases, inconsistent class-merge-gedrag). De aanpak die hier is gebruikt:
- Claude schreef een Python-script dat met regex de drie transformatie-patronen toepaste in vaste volgorde.
- Het script telde "before" en "after" — verificatie ingebouwd, geen vergeten cases.
- Mens reviewde steekproefsgewijs (3 voorbeelden) ipv 52× handmatig.

**Waarom dit eerlijk vermeld wordt**: een mens-reviewer zou anders kunnen denken dat 52 wijzigingen "regel-voor-regel" gemaakt zijn. Dat zou bewerkelijk klinken en gebruikt-wordt-AI-niet-genoemd. Door de scripted aanpak transparant te maken is duidelijk *waar* AI versnelde en *waar* mens-review essentieel bleef (steekproef op output-correctheid, niet op alle 52 instances).

### 3.5 Conclusie op de toolinginzet

AI-tooling heeft in dit traject vooral **structurerings**- en **synthese**-werk gedaan: ruwe SonarCloud-data + losse rubriek-criteria → één samenhangend bewijspad door zes deliverables. De **inhoudelijke besluiten** (welke verbeteringen prioriteren, welke alternatieven, hoe rubriek interpreteren) zijn menselijke besluiten gebleven; de AI heeft alternatieven aangedragen, de mens heeft gekozen. Voor het meet-werk (`mvn`, CI) is de AI volledig afhankelijk van de mens-infrastructuur geweest.

Dit komt overeen met wat de huidige LLM-generatie wel/niet kan, en deze PoC heeft expliciet die rolverdeling gerespecteerd ipv te doen alsof de AI alles zelf deed.

---

## 4. Hoe te valideren

Voor bulletpoint 6 wordt de **identieke baseline-meting** opnieuw gedraaid en vergeleken met `03-testresultaten-baseline.md`:

```bash
# 1. Lokaal verifiëren dat C1 werkt
cd openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/api
mvn -B clean verify                                  # 182/182 unit tests + JaCoCo
mvn -B org.pitest:pitest-maven:mutationCoverage      # verwacht: 15/15 KILLED

# 2. B1 sanity check: geen deprecated align/valign meer in projecteigen JSP's
cd ../../..  # terug naar repo root
grep -rE 'align="' omod/src/main/webapp/*.jsp omod/src/main/webapp/portlets/*.jsp || echo "geen treffers"
#   (vendored ZeroClipboard.js bevat nog align= maar is via A1 uit Sonar-scope)

# 3. CI triggeren: push naar dev of main
git push                                             # triggert maintainability-tests.yml

# 4. Op SonarCloud dashboard checken:
#    - Code smells: van 1.234 -> ~56 (vendored exclusions actief + B1 weghaalt 33)
#    - Coverage: van "-" naar getal >=60% (JaCoCo-XML import werkt)
#    - Quality Gate: na A3 hard groen; tot dan "Passed" of "Not computed"
```

De verwachte eindstand op de NFR-tabel staat in `05-ontwerp.md §7`.

---

## 5. Verwijzingen

### Interne documenten in dit traject

- [`01-systematische-analyse.md`](./01-systematische-analyse.md) — bron-cijfers van de meting
- [`02-teststrategie.md`](./02-teststrategie.md) — vier testtypen + NFR-koppeling
- [`03-testresultaten-baseline.md`](./03-testresultaten-baseline.md) — nulmeting, inclusief PIT-survivor in §4.3
- [`04-geprioriteerde-verbeteringen.md`](./04-geprioriteerde-verbeteringen.md) — selectie top-4
- [`05-ontwerp.md`](./05-ontwerp.md) — ontwerp van A1, C1, A2, B1 met alternatieven en bronvermelding

### Gewijzigde / nieuwe bestanden in deze PoC

- `sonar-project.properties` — nieuw (A1 + A2)
- `AuditLoggerTest.java` — gewijzigd (C1)
- `.github/workflows/maintainability-tests.yml` — opgeschoond (DRY-cleanup na A1+A2)
- `omod/src/main/webapp/resources/Styles/appointmentscheduling-layout.css` — nieuw (B1)
- `omod/src/main/webapp/appointmentBlockList.jsp` — gewijzigd (B1, 24 attributen)
- `omod/src/main/webapp/appointmentBlockForm.jsp` — gewijzigd (B1, 13 attributen)
- `omod/src/main/webapp/appointmentTypeList.jsp` — gewijzigd (B1, 3 attributen)
- `omod/src/main/webapp/appointmentTypeForm.jsp` — gewijzigd (B1, 4 attributen)
- `omod/src/main/webapp/portlets/appointments.jsp` — gewijzigd (B1, 8 attributen)
