# Pipeline Compliance — Appointment Scheduling Module

| | |
|---|---|
| **Norm** | NEN-7510:2024-2 |
| **Module** | openmrs-module-appointmentscheduling v2.0.0 |
| **Datum** | 2026-06-08 |
| **Auteur** | Team ICT2-4AVANS |

---

## Inleiding

Dit verslag toont per NEN-7510:2024-2 control welke pipeline-maatregel actief is in de repository en hoe die maatregel als bewijs dient. Het document sluit aan op de gap-analyse (`01-gap-analyse.md`) en maakt de automatische handhaving van beveiligingseisen aantoonbaar.

---

## Overzichtstabel

| Control | Onderwerp | Pipeline-maatregel | Workflow / bestand | Status |
|---|---|---|---|---|
| A.8.3 | Toegangsbeveiliging | CodeQL-analyse detecteert ontbrekende toegangscontroles in gewijzigde code | `.github/workflows/codeql.yml` | ✅ Actief |
| A.8.5 | Authenticatie | Dependency Review blokkeert kwetsbare authenticatie-gerelateerde libraries | `.github/workflows/dependency-review.yml` | ✅ Actief |
| A.8.15 | Logging en monitoring | SBOM vastlegt alle afhankelijkheden; CodeQL signaleert ontbrekende logging in nieuwe code | `.github/workflows/SBOM.yml` + `codeql.yml` | ✅ Actief |

---

## A.8.3 — Toegangsbeveiliging

### Maatregel

CodeQL voert statische code-analyse uit bij elke push naar `main` en bij elke pull request naar `main`. Nieuwe code die schrijfoperaties op gevoelige objecten uitvoert zonder privilege-check wordt als bevinding gerapporteerd in de GitHub Security-tab.

### Bewijs

**Workflow:** [`.github/workflows/codeql.yml`](../../.github/workflows/codeql.yml)

```yaml
on:
  push:
    branches: [main]
  pull_request:
    branches: [main]
```

- Trigger: elke PR naar `main` en elke push naar `main`
- Taal: Java
- Resultaten zichtbaar onder: **Security → Code scanning**
- Koppeling gap-analyse: de gap in A.8.3 (ontbrekende `@Authorized` op REST-laag) is als CodeQL-bevinding aantoonbaar wanneer nieuwe REST-methoden zonder privilege-check worden ingediend via een PR

### Relatie tot gap

De gap-analyse stelde vast dat de REST-laag geen eigen autorisatiecontroles bevat. CodeQL fungeert als tweede verdedigingslinie: nieuwe code die dezelfde fout introduceert wordt automatisch gesignaleerd vóór merge naar `main`.

---

## A.8.5 — Authenticatie

### Maatregel

De Dependency Review Action controleert bij elke pull request naar `main` of gewijzigde of toegevoegde afhankelijkheden bekende kwetsbaarheden bevatten. De workflow weigert een PR automatisch als een dependency een kwetsbaarheid van ernst `HIGH` of `CRITICAL` bevat.

### Bewijs

**Workflow:** [`.github/workflows/dependency-review.yml`](../../.github/workflows/dependency-review.yml)

```yaml
- uses: actions/dependency-review-action@v4
  with:
    fail-on-severity: high
```

- Trigger: elke PR naar `main`
- Drempelwaarde: `high` — PR wordt geweigerd bij HIGH of CRITICAL CVE in dependencies
- Resultaten zichtbaar in de PR-checks op GitHub

### Relatie tot gap

De gap-analyse stelde vast dat de module authenticatie volledig delegeert aan het OpenMRS-platform. De Dependency Review-maatregel borgt dat authenticatie-gerelateerde platform-libraries (Spring Security, OpenMRS Core) geen bekende kwetsbaarheden bevatten bij elke wijziging.

---

## A.8.15 — Logging en monitoring

### Maatregel 1 — SBOM

De SBOM-workflow genereert bij elke push naar `main` een Software Bill of Materials in CycloneDX JSON-formaat. Dit maakt alle directe afhankelijkheden traceerbaar en biedt een basis voor monitoring van nieuwe CVE's in de supply chain.

**Workflow:** [`.github/workflows/SBOM.yml`](../../.github/workflows/SBOM.yml)

```yaml
- name: Generate SBOM
  uses: anchore/sbom-action@v0
  with:
    format: cyclonedx-json
    output-file: sbom.cyclonedx.json
- name: Upload SBOM
  uses: actions/upload-artifact@v4
  with:
    name: sbom
    path: sbom.cyclonedx.json
```

- Trigger: elke push naar `main`
- Artifact: `sbom` downloadbaar vanuit de Actions-tab
- Formaat: CycloneDX JSON (vereist door opdracht)

### Maatregel 2 — CodeQL

CodeQL-analyse signaleert in nieuwe code ontbrekende logging bij kritieke acties. Gecombineerd met de in de gap-analyse beschreven aanbeveling (audit-logging toevoegen aan `save*`, `cancel*`, `purge*` methoden) bewaakt CodeQL dat bij toekomstige refactoring logging niet per ongeluk wordt weggelaten.

### Relatie tot gap

De gap-analyse stelde vast dat slechts 1 audit-logstatement aanwezig is in de volledige implementatiecode. De SBOM-workflow borgt de supply-chain-transparantie (traceerbaarheid van afhankelijkheden). CodeQL bewaakt dat nieuwe implementaties de logging-eis niet verder verslechteren.

---

## Automatische handhaving samengevat

| Actie | Automatisch geblokkeerd? | Maatregel |
|---|---|---|
| PR met nieuwe HIGH/CRITICAL CVE in dependency | ✅ Ja | Dependency Review (`fail-on-severity: high`) |
| PR met nieuwe HIGH/CRITICAL CodeQL-bevinding | ✅ Ja (via branch protection rule) | CodeQL + branch protection |
| Push naar main zonder nieuwe SBOM | ✅ Nee — SBOM wordt automatisch aangemaakt | SBOM workflow |
| Push naar main zonder code review | ✅ Ja (via branch protection rule) | Branch protection: require PR |
