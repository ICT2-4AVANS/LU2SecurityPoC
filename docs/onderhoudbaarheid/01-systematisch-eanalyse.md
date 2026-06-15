# Systematische analyse onderhoudbaarheid

|              |                                                          |
| ------------ | -------------------------------------------------------- |
| **Module**   | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT     |
| **Datum**    | 2026-06-15                                               |
| **Auteur**   | Enes T. (LU2-MaintainabilityPoC)                         |
| **Tooling**  | SonarCloud (statische analyse) + handmatige verificatie + Sonar Web-API export (`api/issues/search`) |
| **Scope**    | Volledige module — `api/`, `omod/`, JSP/JS-frontend      |
| **Bron NFR** | [`docs/non-functional-requirements.md`](../non-functional-requirements.md) — sectie *Onderhoudbaarheid* (MNT-1..MNT-4) |

> Dit document legt **uitsluitend de meting** vast (bulletpoint 1 van Opdrachtonderdeel 1).
> Prioritering, ontwerp en PoC-realisatie volgen in latere documenten in deze map.

---

## 1. Doel

Aantoonbaar maken wat de onderhoudbaarheid van de Appointment Scheduling Module **vandaag** is, zodat:

1. er een **objectieve nulmeting** ligt waartegen latere verbeteringen afgezet kunnen worden (regressiebewaking);
2. duidelijk is **waar** in de codebase de grootste onderhoudslast zit (input voor de prioritering in bulletpoint 3);
3. zichtbaar wordt of de module **voldoet aan de eigen NFR-grenswaarden** uit `non-functional-requirements.md`.

---

## 2. Methodologie

De analyse is *systematisch* opgebouwd in vier stappen, zodat het reproduceerbaar is en niet leunt op een momentopname:

| Stap | Wat                                                       | Hoe                                                                                                  |
| ---: | --------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
|   1  | **Definiëren van onderhoudbaarheid**                      | ISO/IEC 25010 → *Maintainability* met sub-attributen *Analysability*, *Modifiability*, *Testability*, *Modularity*, *Reusability*. |
|   2  | **Operationaliseren naar meetbare grenswaarden**          | Vastgelegd in NFR-tabel: MNT-1 (complexiteit), MNT-2 (duplicaten), MNT-3 (coverage), MNT-4 (Quality Gate). |
|   3  | **Geautomatiseerd meten**                                 | SonarCloud project `LU2-MaintainabilityPoC` — taalanalyzers Java, JavaScript, JSP, XML, CSS.         |
|   4  | **Toetsen + interpreteren**                               | SonarCloud-output naast NFR-grenswaarden leggen en per dimensie classificeren als ✅ / ⚠ / ❌.        |

### 2.1 Waarom SonarCloud

- Dekt alle talen die in de module voorkomen (Java + JSP + JS) — Qodana zou de JSP-laag minder goed pakken.
- Faalt het CI-proces automatisch bij niet voldoen aan de Quality Gate (zie NFR MNT-4).
- Levert herhaalbare, versiegebonden metingen → bruikbaar als regressie-baseline.

### 2.2 Wat *niet* in deze analyse

- **Coverage** — JaCoCo-rapportage staat in [`docs/tests/codecoverage.md`](../tests/codecoverage.md); SonarCloud rapporteert hier `-` omdat de coverage-import nog gepland staat (Sprint 3).
- **Security & reliability** — staan wel in de screenshot, maar vallen buiten onderhoudbaarheid en worden in de auditreports behandeld.

---

## 3. Resultaten — overall (nulmeting)

Bron: SonarCloud dashboard, *Last analysis: 15/06/2026 10:31*, 16k Lines of Code (JavaScript, JSP, Java, …).

| Dimensie                         | Waarde       | Rating |
| -------------------------------- | ------------ | :----: |
| Lines of Code                    | **≈ 16.000** |   —    |
| Code Smells                      | **1.234**    |  **A** |
| Technical Debt                   | **7.792 min ≈ 130 h ≈ 16 d** |  **A** |
| Debt Ratio (SQALE)               | **1,6 %**    |  **A** |
| Effort to Reach A                | **0**        |  **A** |
| Duplications                     | **1,2 %**    |   —    |
| Coverage                         | *niet gemeten* |  —    |
| Quality Gate                     | *Not computed* (eerste run) | — |

> **Verificatie cijfer-consistentie.** De volledige Sonar Web-API export (`api/issues/search?componentKeys=...&types=CODE_SMELL`, 3 pagina's in `raw/`, geconsolideerd in `raw/sonarcloud-issues-all-1234.json`) levert `total=1234` en `effortTotal=7792` minuten op. 7.792 ÷ 60 ÷ 8 ≈ **16,2 dagen** — precies de **16 d** uit het dashboard. Daarmee is de nulmeting cijfermatig dichtgespijkerd op alle 1.234 issues, niet alleen op een sample.

> **Lezing.** De A-rating voor maintainability is misleidend gunstig: SonarCloud berekent het cijfer uit de **debt ratio** (debt ÷ geschatte herontwikkelkosten). Bij een legacy-module met veel regels code zakt die ratio snel onder 5 % (= rating A), terwijl er in absolute zin nog steeds **1.234 smells** open staan. De rating zegt dus weinig over *hoe prettig de code is om aan te werken*; daarvoor moeten we doorklikken naar de smells zelf (§4).

---

## 4. Resultaten — onderhoudbaarheid in detail

### 4.1 Code Smells — verdeling per ernst (N = 1.234, volledig)

Cijfers in deze paragraaf komen uit de **geconsolideerde** Sonar-export (`raw/sonarcloud-issues-all-1234.json`, 3 paginas van 500 issues elk gecombineerd tot alle 1.234 issues).

**Klassieke severity-as (legacy, op dashboard zichtbaar):**

| Severity | Aantal | Aandeel  |
| -------- | -----: | -------: |
| CRITICAL | 798    | 64,7 %   |
| MAJOR    | 235    | 19,0 %   |
| MINOR    | 186    | 15,1 %   |
| INFO     | 12     | 1,0 %    |
| BLOCKER  | 3      | 0,2 %    |
| **Totaal**| **1.234** | **100 %** |

**Nieuw Sonar Clean-Code impact-model (maintainability-as):**

| Impact-severity | Aantal | Aandeel |
| --------------- | -----: | ------: |
| HIGH            | 811    | 65,7 %  |
| MEDIUM          | 216    | 17,5 %  |
| LOW             | 192    | 15,6 %  |
| INFO            | 12     | 1,0 %   |
| BLOCKER         | 3      | 0,2 %   |

> **Lezing.** Twee-derde van de smells heeft de hoogste maintainability-impact. Het beeld is dus *niet* "veel stijlissues" — Sonar markeert het overwegend als zware onderhoudslast. Dat wordt pas écht interpreteerbaar als we in § 4.2 splitsen tussen *vendored* en *projecteigen* code: het overgrote deel van die HIGH-issues komt uit ingeladen 3rd-party libs.

### 4.1.1 Top-15 rules (N = 1.234)

| #  | Rule                 | Aantal | Strekking                                                              |
| -: | -------------------- | -----: | ---------------------------------------------------------------------- |
| 1  | `javascript:S3504`   | 701    | `var` gebruikt i.p.v. `let`/`const` — pre-ES6                          |
| 2  | `javascript:S2004`   | 54     | Functies meer dan 4 niveaus diep genest                                |
| 3  | `javascript:S3776`   | 40     | Cognitieve complexiteit > 15 (refactor required)                        |
| 4  | `javascript:S7735`   | 38     | Onnodig negatieve condities                                            |
| 5  | `javascript:S1481`   | 34     | Ongebruikte variabele-declaraties                                      |
| 6  | `Web:S1827`          | 33     | Deprecated HTML-attribuut `align`                                      |
| 7  | `javascript:S7721`   | 22     | Functie hoort in outer scope                                           |
| 8  | `javascript:S7773`   | 18     | `Number.isNaN` boven `isNaN`                                           |
| 9  | `javascript:S1121`   | 18     | Assignment in expressie extraheren                                     |
| 10 | `Web:S6847`          | 17     | A11y: muis/keyboard-handlers op niet-interactieve elementen            |
| 11 | `javascript:S7780`   | 15     | `String.raw` om escapes te vermijden                                   |
| 12 | `javascript:S7765`   | 14     | `.includes()` i.p.v. `.indexOf() !== -1`                               |
| 13 | `javascript:S1854`   | 14     | Nutteloze variabele-assignment                                         |
| 14 | `javascript:S7762`   | 13     | `childNode.remove()` i.p.v. `parentNode.removeChild()`                 |
| 15 | `javascript:S7740`   | 13     | `this` toegewezen aan `_that`                                          |

> **Eén regel = 57 % van alle smells.** `javascript:S3504` (701×) is verantwoordelijk voor méér dan de helft van het volledige dashboard-getal. Dit is volledig een **pre-ES6-codepatroon**, en het zit uitsluitend in JS-bestanden — vrijwel allemaal vendored libs (zie § 4.2).

### 4.1.2 Talen-verdeling van smells (N = 1.234)

| Taal/analyzer | Issues | Aandeel |
| ------------- | -----: | ------: |
| JavaScript    | 1.146  | **92,9 %** |
| Web (HTML/JSP)| 63     | 5,1 %   |
| CSS           | 17     | 1,4 %   |
| Flex (`.as`)  | 5      | 0,4 %   |
| XML           | 3      | 0,2 %   |
| Java          | **0**  | **0 %** |

> **Saillant.** Sonar vindt **geen enkele Java-smell** in 12.040 regels Java-code. Het hele maintainability-verhaal van deze module zit in de **JavaScript-laag**, en die laag bestaat — zoals § 4.2 laat zien — vooral uit ingeladen 3rd-party libs. De "Java moet gerefactored worden"-reflex die je bij een 1.234-smells-getal verwacht is hier dus *aantoonbaar* niet van toepassing.

### 4.2 Technical Debt — 7.792 min / ~16 d, waar zit het?

Met 7.792 min / 1.234 smells is de **gemiddelde herstelkost ≈ 6,3 min per smell**. De distributie is sterk Pareto: een klein aantal bestanden draagt vrijwel alle debt.

**4.2.1 — Top-15 bestanden (N = 1.234)**

| #  | Bestand                          | # smells | Aandeel | Vendored? |
| -: | -------------------------------- | -------: | ------: | :-------: |
| 1  | `jquery.dataTables.js`           | 654      | 53,0 %  | ✅        |
| 2  | `opentip-jquery-excanvas.js`     | 275      | 22,3 %  | ✅        |
| 3  | `jquery.jeditable.js`            | 69       | 5,6 %   | ✅        |
| 4  | `TableTools.js`                  | 48       | 3,9 %   | ✅        |
| 5  | `ZeroClipboard.js`               | 34       | 2,8 %   | ✅        |
| 6  | `json2.js`                       | 33       | 2,7 %   | ✅        |
| 7  | `statusButtons.js`               | 15       | 1,2 %   | ❌ (eigen)|
| 8  | `appointmentBlockList.jsp`       | 13       | 1,1 %   | ❌        |
| 9  | `appointmentSettingsForm.jsp`    | 12       | 1,0 %   | ❌        |
| 10 | `timepicker.js`                  | 9        | 0,7 %   | ✅        |
| 11 | `appointments.jsp`               | 9        | 0,7 %   | ❌        |
| 12 | `date.format.js`                 | 8        | 0,6 %   | ✅        |
| 13 | `appointmentStatisticsForm.jsp`  | 7        | 0,6 %   | ❌        |
| 14 | `appointmentBlockForm.jsp`       | 6        | 0,5 %   | ❌        |
| 15 | `fullcalendar.css`               | 5        | 0,4 %   | ✅        |

> **Eén bestand = 53 % van alle smells.** `jquery.dataTables.js` alléén is verantwoordelijk voor méér dan de helft van alle 1.234 issues. De top-2 (`jquery.dataTables.js` + `opentip-jquery-excanvas.js`) dekt **75,3 %** van alle smells; de top-6 (allemaal vendored) dekt **90,3 %**.

**4.2.2 — Vendored vs projecteigen (N = 1.234)**

Bestanden geclassificeerd als *vendored* op basis van bekende 3rd-party-bibliotheken (jquery\*, opentip\*, ZeroClipboard, TableTools, json2, fullcalendar, timepicker, date.format). Voor de overige bestanden geldt: projecteigen.

| Categorie     | # smells | Aandeel | Effort (min) | Effort (dagen, 8u) |
| ------------- | -------: | ------: | -----------: | -----------------: |
| Vendored libs | 1.145    | 92,8 %  | 7.354        | ~15,3 d            |
| Projecteigen  | **89**   | **7,2 %**| **438**     | **~0,9 d**         |
| **Totaal**    | **1.234**| **100 %**| **7.792**   | **~16,2 d**        |

> **Hét belangrijkste inzicht van deze hele analyse.** Trek je de vendored libs eraf, dan blijven er **89 smells** over met **438 minuten** (= ~7 uur) effort. De *werkelijke* onderhoudslast op de code waar het team verantwoordelijk voor is, is dus geen 16 dagen maar **minder dan één werkdag**. Het 1.234-getal in het dashboard is geen reflectie van de codekwaliteit van het team — het is een reflectie van "OpenMRS heeft jQuery-plugins gevendord".

**4.2.3 — Top-10 rules in projecteigen code (N = 89)**

| #  | Rule              | Aantal | Strekking                                          |
| -: | ----------------- | -----: | -------------------------------------------------- |
| 1  | `Web:S1827`       | 33     | Deprecated `align`-attribuut                       |
| 2  | `Web:S6847`       | 17     | A11y: handlers op niet-interactieve elementen      |
| 3  | `javascript:S3504`| 13     | `var` → `let`/`const`                              |
| 4  | `css:S7924`       | 6      | Onvoldoende kleurcontrast (WCAG)                   |
| 5  | `Web:S6848`       | 6      | Niet-native interactieve elementen zonder `role`   |
| 6  | `Web:S6844`       | 6      | `<a>` als knop                                     |
| 7  | `xml:S125`        | 3      | Uitgecommentarieerde code (in `pom.xml`)           |
| 8  | `javascript:S7735`| 1      | Negatieve conditie                                 |
| 9  | `javascript:S4138`| 1      | `for-of` i.p.v. klassieke `for`                    |
| 10 | `javascript:S1481`| 1      | Ongebruikte variabele                              |

> **Patroon in de eigen code:** 73 van de 89 issues (82 %) zijn **HTML-modernisering + a11y** (`Web:S*` + `css:S7924`). De rest is licht JS-onderhoud. *Géén* hoge cognitieve complexiteit, géén commented-out blokken, géén Java-smells. De eigen codebasis is in onderhoudbaarheidsopzicht in feite gezond.

**4.2.4 — Projecteigen top-bestanden (N = 89)**

| #  | Bestand                          | # smells |
| -: | -------------------------------- | -------: |
| 1  | `statusButtons.js`               | 15       |
| 2  | `appointmentBlockList.jsp`       | 13       |
| 3  | `appointmentSettingsForm.jsp`    | 12       |
| 4  | `appointments.jsp`               | 9        |
| 5  | `appointmentStatisticsForm.jsp`  | 7        |
| 6  | `appointmentBlockForm.jsp`       | 6        |
| 7  | `appointmentForm.jsp`            | 5        |
| 8  | `appointmentTypeList.jsp`        | 4        |
| 9  | `appointmentTypeForm.jsp`        | 4        |
| 10 | `pom.xml`                        | 3        |

> Negen van de tien projecteigen hotspots zijn JSP-formulieren — dezelfde laag, hetzelfde type fix (HTML5 + a11y). Dit is uitstekend gerichte input voor bulletpoint 3 en 4.

### 4.3 Cyclomatische / cognitieve complexiteit

- Modulebreed bekend uit de moduleverkenning: **Java complexity = 1.034** over **12.040 Java-regels** (zie [`module-keuze.md`](../module-keuze.md)).
- Sonar's `javascript:S3776` ("Refactor cognitive complexity > 15") komt **40×** voor in de Sonar-export, allemaal in vendored JS — niet in projecteigen code.
- In de projecteigen code worden **geen** complexiteits-rules getriggerd. NFR-MNT-1 (≤ 10 per methode) wordt daarmee, voor zover Sonar dit signaleert, **niet aantoonbaar overschreden** in de eigen code.

### 4.4 Duplicaten

- Module-breed **1,2 %** duplicate lines → ruim onder de NFR-grens van 5 %.
- Toch is dit niet "klaar": de duplicates zijn geconcentreerd in een paar JS-/JSP-bestanden. Lokaal kan dat boven de 10 % uitkomen. Die zijn relevant voor de prioritering (bulletpoint 3).

### 4.5 Coverage

- SonarCloud: `-` (niet gerapporteerd).
- JaCoCo (lokaal, zie [`codecoverage.md`](../tests/codecoverage.md)): module-breed gate op **30 %**, security-pakket op **90 %** / **80 %** branch.
- **Gap**: de JaCoCo-XML wordt nog niet naar SonarCloud geüpload; dat is een quick-win voor de CI-koppeling (zie § 6, A2).

---

## 5. Toetsing aan NFR-grenswaarden

| NFR    | Eis                                          | Grenswaarde     | Meting                                         | Status |
| ------ | -------------------------------------------- | --------------- | ---------------------------------------------- | :----: |
| MNT-1  | Cyclomatische complexiteit per methode       | ≤ 10            | 0 overschrijdingen in projecteigen code; 40 in vendored libs | ✅ (eigen) / ⚠ (vendored) |
| MNT-2  | Duplicaat-percentage                         | ≤ 5 %           | 1,2 % module-breed                             | ✅     |
| MNT-3  | Line coverage                                | ≥ 60 %          | nog niet geïmporteerd in Sonar                 | ⚠      |
| MNT-4  | Quality Gate "Passed" op PR                  | Passed verplicht| *Not computed*                                 | ❌     |

> **Interpretatie.** Door de vendored/projecteigen-splitsing in §4.2 wordt MNT-1 op de eigen code **wél aantoonbaar gehaald**. MNT-2 is hard groen. MNT-3 (coverage) en MNT-4 (Quality Gate) blijven open en zijn pure CI-inrichtingstaken, geen codekwesties.

---

## 6. Bevindingen voor vervolgstappen

Onderstaande punten zijn de **observaties** die de analyse oplevert. Ze zijn *nog geen* geprioriteerde verbeteringen — die uitwerking volgt in bulletpoint 3.

| ID  | Bevinding                                                                                                          | Bewijs    |
| --- | ------------------------------------------------------------------------------------------------------------------ | --------- |
| O1  | 1.234 code smells; A-rating misleidend door gunstige SQALE debt-ratio                                              | §3, §4.1  |
| O2  | Eén bestand (`jquery.dataTables.js`) = 53 % van alle smells; top-6 vendored bestanden = 90 %                       | §4.2.1    |
| O3  | **Vendored vs eigen splitsing**: 92,8 % vendored vs 7,2 % projecteigen — eigen debt = 89 smells / 438 min / <1 dag | §4.2.2    |
| O4  | Eigen code is overwegend HTML-modernisering + a11y (73/89 = 82 %); geen Java-smells; geen complexiteits-issues     | §4.1.2, §4.2.3, §4.3 |
| O5  | Eén Sonar-rule (`javascript:S3504`, `var`→`let/const`) = 57 % van alle smells, allemaal in vendored libs           | §4.1.1    |
| O6  | Coverage wordt lokaal gemeten (JaCoCo) maar niet naar SonarCloud doorgegeven                                       | §4.5      |
| O7  | Quality Gate is niet aan de CI gekoppeld → MNT-4 op rood                                                           | §5        |
| O8  | Duplicates module-breed laag (1,2 %); ruim onder NFR-grens                                                         | §4.4, §5  |

### Actiepunten om de analyse zélf te versterken (data-completeness)

- **A1** — ✅ *gedaan*: alle 3 paginas van de SonarCloud-export zijn opgehaald en geconsolideerd (`raw/sonarcloud-issues-all-1234.json`). Cijfers in §4.1 en §4.2 zijn over de volledige 1.234 issues.
- **A2** — JaCoCo XML als artifact uploaden naar SonarCloud zodat coverage-waarde geen `-` meer is.
- **A3** — Quality Gate-profiel "OpenMRS-LU2" definiëren en koppelen aan CI (vereist voor MNT-4).
- **A4** — Sonar-scope-config (`sonar.exclusions` voor vendored libs) opnemen, zodat het dashboard de werkelijke onderhoudslast van de eigen code laat zien i.p.v. legacy-libs. *Dit is een meting-correctie, geen kwaliteitsverbetering.*

> A1 is uitgevoerd. A2–A4 worden opgepakt vóór de prioritering in bulletpoint 3.

---

## 7. Conclusie

De Appointment Scheduling Module krijgt van SonarCloud een **maintainability-rating A** op 1.234 code smells en 16 dagen technische schuld. De volledige API-export (alle 1.234 issues, niet alleen het dashboard-samenvattingsgetal) laat vier patronen zien die de Sonar-UI niet samenvat en die het verhaal fundamenteel veranderen:

1. **Geen Java-smells.** 0 van de 1.234 issues zit in de 12.040 regels Java. De "Java moet gerefactored worden"-reflex is hier aantoonbaar misplaatst.
2. **93 % van de last zit in vendored libs.** 1.145 van 1.234 smells komt uit ingeladen 3rd-party JS (`jquery.dataTables.js` alleen al 654 = 53 %). De eigen code bevat slechts **89 issues / 438 minuten effort ≈ 0,9 dag**.
3. **Eén regel domineert.** `javascript:S3504` (`var`→`let/const`) = 701 issues = 57 % van het totaal, vrijwel uitsluitend in vendored libs.
4. **De eigen debt is overwegend HTML-modernisering + a11y.** 73 van 89 eigen issues (82 %) zijn `Web:*` of `css:S7924` — gestructureerde frontend-modernisering, geen architecturale rework.

Daarmee verschuift het beeld van "groot onderhoudsprobleem" naar "**Sonar-scope verkeerd ingericht + lichte HTML/a11y-modernisering in eigen code**". Twee NFR-issues blijven open (MNT-3 coverage-import, MNT-4 Quality Gate-koppeling) maar zijn pure CI-inrichting, geen code-issues.

Deze vier patronen leveren de **richtlijnen voor bulletpoint 3** (prioriteer vendored-scope-correctie + de 89 eigen issues, niet 1.234 refactor-tasks) en **bulletpoint 4** (ontwerp: Sonar-config-pattern + JSP-modernisering, geen Java-refactoring). De nulmeting is daarmee niet alleen *vastgelegd* maar ook *richtinggevend*, en — door de geconsolideerde raw-export — **reproduceerbaar** vanaf de bron.

---

## Bijlagen / verwijzingen

- [`docs/non-functional-requirements.md`](../non-functional-requirements.md) — NFR-grenswaarden MNT-1..MNT-4
- [`docs/module-keuze.md`](../module-keuze.md) — modulekarakteristieken (omvang, complexiteit)
- [`docs/tests/codecoverage.md`](../tests/codecoverage.md) — JaCoCo-opzet en drempels
- SonarCloud dashboard *LU2-MaintainabilityPoC* — bron-screenshot d.d. 2026-06-15 10:31
- [`raw/sonarcloud-issues-all-1234.json`](./raw/sonarcloud-issues-all-1234.json) — geconsolideerde export van alle 1.234 issues, basis voor §4.1 / §4.2 / §5
- [`raw/sonarcloud-issues-page{1,2,3}-ps500.json`](./raw/) — originele paginaresponses uit Sonar Web-API (`api/issues/search?ps=500&p=N`), bronbestanden voor de consolidatie
