# Pipeline-compliance — Appointment Scheduling Module

| | |
|---|---|
| **Norm** | NEN-7510:2024-2 |
| **Module** | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT |
| **Datum** | 2026-06-08 |

---

## Inleiding

Dit document beschrijft hoe de CI/CD-pipeline maatregelen implementeert die aansluiten bij de NEN-7510:2024-2 controls die zijn geïdentificeerd in de gap-analyse (`01-gap-analyse.md`). Per control staat de pipeline-maatregel en het aantoonbare bewijs beschreven.

---

## Overzicht

| NEN-7510:2024-2 Control | Onderwerp | Pipeline-maatregel | Workflow | Status |
|---|---|---|---|---|
| A.8.3 | Toegangsbeveiliging | Dependency review blokkeert `HIGH`/`CRITICAL` CVE's | `dependency-review.yml` | ✅ Actief |
| A.8.5 | Authenticatie | CodeQL detecteert authenticatiegerelateerde kwetsbaarheden | `codeql.yml` | ✅ Actief |
| A.8.15 | Logging en monitoring | CodeQL detecteert onveilige loggingpatronen; SBOM identificeert kwetsbare logging-libraries | `codeql.yml`, `SBOM.yml` | ✅ Actief |
| — | Integriteit supply chain | SBOM gegenereerd in CycloneDX-formaat | `SBOM.yml` | ✅ Actief |
| — | Veilig ontwikkelproces | Branch protection + PR-vereiste op `main` | GitHub branch rules | ✅ Actief |
| — | Omgevingsscheiding | Gescheiden GitHub Environments (`test`, `production`) | `deploy.yml` | ✅ Actief |

---

## A.8.3 — Toegangsbeveiliging

**Maatregel:** De `dependency-review` workflow controleert bij elke pull request naar `main` alle nieuwe afhankelijkheden op bekende kwetsbaarheden. Pull requests met een `HIGH` of `CRITICAL` bevinding worden geblokkeerd.

**Workflow:** `.github/workflows/dependency-review.yml`

```yaml
- uses: actions/dependency-review-action@v4
  with:
    fail-on-severity: high
```

**Bewijs:** De workflow is actief op alle pull requests naar `main`. Zolang de `dependency-review` check faalt, kan de PR niet worden gemerged — mits deze check als required status check is ingesteld in de branch protection rules op `main`.

**Relatie met gap-analyse:** De gap-analyse constateerde dat de REST/DWR-laag geen eigen autorisatiecontroles heeft. De dependency review biedt een compenserende maatregel door te voorkomen dat nieuwe kwetsbare libraries worden geïntroduceerd die misbruikt kunnen worden via de REST-laag.

---

## A.8.5 — Authenticatie

**Maatregel:** De `CodeQL`-workflow analyseert de broncode op beveiligingsfouten, waaronder authenticatiegerelateerde kwetsbaarheden zoals:
- `java/hardcoded-credential` — hardcoded wachtwoorden of tokens
- `java/insecure-cookie` — sessie-cookies zonder `HttpOnly`/`Secure`-vlag
- `java/spring-disabled-csrf-protection` — uitgeschakelde CSRF-bescherming

**Workflow:** `.github/workflows/codeql.yml`

```yaml
- uses: github/codeql-action/init@v4
  with:
    languages: java
```

**Bewijs:** De workflow draait automatisch bij elke push naar `main` en bij elke pull request. Bevindingen zijn zichtbaar onder **Security → Code scanning** op GitHub.

**Relatie met gap-analyse:** De gap-analyse constateerde dat de module geen eigen authenticatielogica bevat en volledig afhankelijk is van het OpenMRS-platform. CodeQL detecteert actief gevallen waarbij de module onveilig omgaat met sessies of credentials.

---

## A.8.15 — Logging en monitoring

**Maatregel 1:** CodeQL detecteert onveilige loggingpatronen, zoals het loggen van gevoelige gegevens (persoonsgegevens, credentials). Standaard CodeQL detecteert geen *ontbrekende* audit logs — dat is een handmatige gap die in Sprint 3 wordt aangepakt via logging-tests en code-aanpassingen.

**Maatregel 2:** De SBOM biedt een volledig overzicht van alle afhankelijkheden, inclusief logging-frameworks (`log4j`, `logback`, `slf4j`). Dit maakt het mogelijk om kwetsbare logging-libraries (zoals Log4Shell, CVE-2021-44228) te identificeren.

**Workflow:** `.github/workflows/SBOM.yml` + `.github/workflows/codeql.yml`

**Bewijs:** De SBOM wordt gegenereerd als CI-artifact in CycloneDX JSON-formaat en is downloadbaar vanuit de Actions-tab.

**Relatie met gap-analyse:** De gap-analyse constateerde dat er slechts 1 audit-logstatement aanwezig is. De pipeline detecteert gevallen waarbij gevoelige gegevens worden gelogd en waarschuwt bij kwetsbare logging-libraries. De volledige oplossing van de logging-gap valt buiten de scope van de pipeline en wordt opgepakt in de code zelf.

---

## Omgevingsscheiding

**Maatregel:** Er zijn drie gescheiden omgevingen ingericht:

| Omgeving | Configuratie | Secrets |
|---|---|---|
| `dev` | `docker-compose.dev.yml` — lokale vaste waarden | Geen secrets (lokaal gebruik) |
| `test` | `docker-compose.test.yml` — omgevingsvariabelen | GitHub Secrets (scoped aan `test` environment) |
| `production` | `docker-compose.prod.yml` — omgevingsvariabelen | GitHub Secrets (scoped aan `production` environment) |

De `deploy.yml` workflow deployt eerst naar `test` en pas daarna, na handmatige goedkeuring via de GitHub Environment protection rules, naar `production`.

**Bewijs:** De GitHub Environments `test` en `production` zijn geconfigureerd in de repository-instellingen. De `production` environment vereist handmatige goedkeuring van een reviewer voordat deployment plaatsvindt.

---

## Samenvatting

De pipeline implementeert een gelaagde verdediging die aansluit bij de geïdentificeerde gaps:

| Gap (uit gap-analyse) | Pipeline-compensatie |
|---|---|
| REST/DWR-laag zonder autorisatiecontroles | `dependency-review` blokkeert nieuwe kwetsbare libraries |
| Geen eigen authenticatielogica in de module | `CodeQL` detecteert onveilig gebruik van credentials/sessies |
| Slechts 1 audit-logstatement | `CodeQL` + `SBOM` detecteren kwetsbare logging-libraries |
| Volledige afhankelijkheid van platformconfiguratie | Gescheiden environments met eigen secrets en protection rules |
