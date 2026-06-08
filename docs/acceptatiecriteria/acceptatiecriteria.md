# Acceptatiecriteria — Sprint 1

**Sprint-goal:** "Onze GitHub-omgeving staat klaar en we hebben aantoonbaar gemaakt waar onze module staat ten opzichte van NEN-7510."

---

## Taak 5.1 — Module-keuze vastleggen

**Klaar als:**

- [x] `docs/module-keuze.md` bestaat in de repository
- [x] Het document bevat de naam, versie en een directe link naar de broncode van de gekozen module
- [x] De keuze is gemotiveerd op basis van minimaal drie criteria (bijv. complexiteit, scope, kritieke functionaliteit)
- [x] Het document is leesbaar en bevat geen lege secties

---

## Taak 5.2 — Repository inrichten

**Klaar als:**

- [x] Branch protection is actief op `main` (directe pushes zijn geblokkeerd, pull request vereist)
- [x] Alle teamleden hebben MFA ingeschakeld op hun GitHub-account
- [x] Dependabot alerts zijn ingeschakeld en zichtbaar onder Security → Dependabot
- [x] De CodeQL-workflow is actief en heeft minimaal één succesvolle run voltooid
- [ ] De GitHub Security-tab toont geen onopgeloste configuratiefouten

---

## Taak 5.3 — OTAP-omgeving opzetten

**Klaar als:**

- [x] Er zijn drie aparte `docker-compose`-bestanden aanwezig: `docker-compose.dev.yml`, `docker-compose.test.yml` en `docker-compose.prod.yml`
- [x] Elke omgeving heeft een eigen configuratie (poorten, volumes of omgevingsvariabelen verschillen per omgeving)
- [x] GitHub Environments zijn geconfigureerd met protection rules voor minimaal de productieomgeving

---

## Taak 5.4 — SBOM genereren

**Klaar als:**

- [x] Er is een SBOM-bestand aanwezig in CycloneDX JSON-formaat
- [x] Het SBOM-bestand wordt automatisch gegenereerd als CI-artifact via GitHub Actions
- [x] Het gegenereerde bestand is downloadbaar vanuit de Actions-tab
- [x] Het SBOM-bestand bevat alle directe afhankelijkheden van de module

---

## Taak 5.5 — Gap-analyse uitvoeren

**Klaar als:**

- [x] `docs/auditreport/01-gap-analyse.md` bestaat in de repository
- [x] De analyse dekt minimaal drie NEN-7510:2024-2 controls: A.8.3, A.8.5 en A.8.15
- [x] Elke control heeft een expliciete status: `aanwezig`, `gedeeltelijk` of `afwezig`
- [x] Elke status is onderbouwd met minimaal één bewijs (coderegel met regelnummer of screenshot)
- [x] De analyse bevat een samenvattingstabel met alle controls, statussen en voornaamste gaps
- [x] De analyse bevat aanbevelingen per gevonden gap

---

## Taak 5.6 — Mini-complianceverslag

**Klaar als:**

- [ ] `docs/auditreport/02-pipeline-compliance.md` bestaat in de repository
- [ ] Het document bevat een tabel met per NEN-7510 control: de bijbehorende pipeline-maatregel én het bewijs
- [ ] Elke pipeline-maatregel is aantoonbaar actief in de repository (link naar workflow of screenshot)
- [ ] Het document dekt minimaal dezelfde controls als de gap-analyse (A.8.3, A.8.5, A.8.15)

---

## Non-functional — Kwaliteitseisen vastleggen

**Klaar als:**

- [ ] De kwaliteitseisen voor security en maintainability zijn schriftelijk vastgelegd
- [ ] De eisen zijn concreet en meetbaar (geen vage omschrijvingen)
- [ ] De eisen zijn gedocumenteerd in de repository zodat het hele team ze kan raadplegen

---

## Non-functional — Statische code-analyse

**Klaar als:**

- [ ] De CodeQL-workflow is actief en draait automatisch bij een pull request naar `main`
- [ ] CodeQL-bevindingen zijn zichtbaar onder Security → Code scanning op GitHub
- [ ] Er is vastgelegd dat geen nieuwe `HIGH` of `CRITICAL` CodeQL-bevindingen mogen worden geïntroduceerd bij een pull request naar `main`
- [ ] De statische code-analyse is gekoppeld aan de gap-analyse als aanvullend bewijs voor minimaal één NEN-7510 control

---

## Eindcheck sprint 1

**De sprint is klaar als alle onderstaande punten zijn afgevinkt:**

- [ ] GitHub-repository heeft branch protection en Dependabot actief
- [ ] SBOM-bestand wordt als CI-artifact aangemaakt in Actions
- [ ] Gap-analyse dekt minimaal drie NEN-7510 controls met bewijs
- [ ] Alle teamleden hebben minimaal één commit bijgedragen
- [ ] Er zijn geen openstaande `HIGH` of `CRITICAL` CodeQL-bevindingen op `main`
