# Zizmor — Statische audit van GitHub Actions workflows

| | |
|---|---|
| **Tool** | [Zizmor](https://docs.zizmor.sh/) v1.25.2 |
| **Type** | Statische SAST-scanner voor GitHub Actions YAML |
| **Scope** | `.github/workflows/*.yml` — alle 9 workflows |
| **Datum** | 2026-06-17 |
| **Uitgevoerd door** | Amine |
| **NEN-7510** | A.8.28 (veilig coderen) + A.8.3 (toegangsbeveiliging) |
| **CRA-relevantie** | Pipeline-integriteit (artikel 13 — secure development) |

---

## 1. Aanleiding

De docent wees op Zizmor als statische scanner specifiek voor CI/CD-config. Naast onze bestaande security pipeline (CodeQL, Dependabot, Snyk, OWASP Dependency-Check, SBOM) hebben we de **workflow-bestanden zelf** geaudit om aan te tonen dat het CI/CD-proces veilig is ingericht.

### Risico-analyse vóór gebruik

Conform de waarschuwing van de docent ("pas op met random tools") is een korte risico-analyse uitgevoerd:

| Vraag | Antwoord |
|---|---|
| Wie maakt het? | [`woodruffw`](https://github.com/woodruffw/zizmor) — security researcher bij Trail of Bits |
| Open source? | Ja — MIT licentie, broncode publiek |
| Actief onderhouden? | Ja — laatste release 2026, 1000+ stars |
| Wat doet het? | Leest YAML-bestanden, geen netwerk-uitvoer of registratie van data |
| Hoe geïnstalleerd? | Standalone binary van GitHub Releases (geen `pip`/`npm` dependency chain) |
| Risico-niveau | **Laag** — read-only static analysis, geen credentials nodig |

**Conclusie:** veilig voor gebruik in deze audit.

---

## 2. Methodologie

1. Zizmor v1.25.2 gedownload als standalone binary
2. Gedraaid tegen alle 9 workflows in `.github/workflows/`
3. Output gegenereerd in tekst + SARIF formaat
4. Bevindingen gecategoriseerd op ernst en type

```powershell
zizmor.exe .github/workflows/
```

---

## 3. Samenvatting

| Type | Count | Severity | Confidence |
|---|---|---|---|
| `unpinned-uses` | 35 | Error | High |
| `artipacked` | 10 | Warning | Low |
| `excessive-permissions` | 9 | Warning | Medium |
| **Totaal** | **54** | | |

---

## 4. Bevindingen

### Z-01: Unpinned uses (35×) — `error`

**Wat:** Alle gebruikte GitHub Actions zijn pinned op een **versie-tag** (`@v4.2.2`) in plaats van een **commit SHA**. Een aanvaller die de versie-tag kan overschrijven (account compromise van de action-maintainer) kan kwaadaardige code injecteren in onze CI.

**Voorbeeld uit `SBOM.yml`:**
```yaml
- uses: actions/checkout@v4.2.2          # ← tag, niet SHA
- uses: anchore/sbom-action@v0.18.0
```

**Aanbevolen fix:**
```yaml
- uses: actions/checkout@11bd71901bbe5b1630ceea73d27597364c9af683  # v4.2.2
```

**Risicoanalyse:** Voor een schoolproject is tag-pinning acceptabel (alle actions zijn pinned op vaste versies, niet `@main` of `@latest`). Voor productiegebruik in een NEN-7510 omgeving is commit-SHA pinning vereist conform supply chain security best practices (NIST SSDF PW.4.4).

**Status:** ⚠️ Geaccepteerd risico voor LU2 PoC — opgenomen als verbetervoorstel voor productieoplevering.

---

### Z-02: Excessive permissions (9×) — `warning`

**Wat:** 9 van de 9 workflows hebben **geen `permissions:` block** gespecificeerd op job- of workflow-niveau. Hierdoor krijgen ze de **default permissions** van de repository (read/write op contents, packages, etc.) — meer dan nodig voor de taak.

**Voorbeeld uit `SBOM.yml`:**
```yaml
jobs:
  sbom:
    runs-on: ubuntu-24.04
    # ← geen permissions: block
    steps:
      - uses: actions/checkout@v4.2.2
      - ...
```

**Aanbevolen fix:**
```yaml
jobs:
  sbom:
    runs-on: ubuntu-24.04
    permissions:
      contents: read
      security-events: write   # voor SARIF upload
    steps:
      ...
```

**Principe:** Least privilege (NEN-7510 A.8.3). Een gecompromitteerde stap in een workflow kan dan minder schade aanrichten.

**Status:** 🔧 P1 — fixen voor presentatie. Mechanische aanpassing, ~30 minuten werk voor 9 workflows.

---

### Z-03: Artipacked — credential persistence (10×) — `warning`

**Wat:** `actions/checkout` zet standaard `persist-credentials: true`, waardoor de `GITHUB_TOKEN` opgeslagen blijft in de Git config van de runner. Volgende stappen die `git push` doen of artifacts uploaden kunnen onbedoeld credentials lekken.

**Voorbeeld uit `SBOM.yml`:**
```yaml
- uses: actions/checkout@v4.2.2
  # ← geen persist-credentials: false
```

**Aanbevolen fix:**
```yaml
- uses: actions/checkout@v4.2.2
  with:
    persist-credentials: false
```

**Risicoanalyse:** Confidence is **Low** — onze workflows pushen niet terug naar de repo en uploaden geen credentials in artifacts. Het is een defense-in-depth maatregel, geen acute kwetsbaarheid.

**Status:** 🔧 P2 — meenemen tijdens Z-02 fix.

---

## 5. Fix-besluit

| ID | Severity | Besluit | Onderbouwing |
|---|---|---|---|
| Z-01 (unpinned-uses) | Error/High | **Accepteren met documentatie** | Tag-pinning is voldoende voor LU2 PoC scope. Commit-SHA pinning wordt aanbevolen voor productie maar valt buiten de huidige sprintscope. |
| Z-02 (excessive-permissions) | Warning/Medium | **Oplossen** | Eenvoudige aanpassing, expliciet `permissions:` block toevoegen aan elke job. Versterkt A.8.3. |
| Z-03 (artipacked) | Warning/Low | **Oplossen** | Defense-in-depth, opnemen in Z-02 fix-PR. |

---

## 6. NEN-7510 Koppeling

| Control | Hoe Zizmor bijdraagt |
|---|---|
| **A.8.3** Toegangsbeveiliging | Detecteert excessive permissions in workflows (least privilege) |
| **A.8.28** Veilig coderen | Toont onveilige CI-config patronen (unpinned actions, credential persistence) |

---

## 7. Aanbeveling voor structurele inbedding

Toevoegen aan CI als geautomatiseerde check:

```yaml
# .github/workflows/zizmor.yml
name: zizmor workflow audit
on: [pull_request, push]
jobs:
  zizmor:
    runs-on: ubuntu-24.04
    permissions:
      contents: read
      security-events: write
    steps:
      - uses: actions/checkout@v4.2.2
      - run: |
          curl -L https://github.com/woodruffw/zizmor/releases/latest/download/zizmor-x86_64-unknown-linux-gnu.tgz | tar xz
          ./zizmor --format=sarif .github/workflows/ > zizmor.sarif
      - uses: github/codeql-action/upload-sarif@v3.28.18
        with:
          sarif_file: zizmor.sarif
```

Hiermee draait Zizmor automatisch bij elke PR en verschijnen bevindingen in **Security → Code scanning** naast CodeQL.

---

## 8. Retest na fix

Na het toevoegen van `permissions:` blocks en `persist-credentials: false` aan alle 9 workflows is Zizmor opnieuw gedraaid:

| Type | Voor fix | Na fix |
|---|---|---|
| `unpinned-uses` | 35 | 35 (geaccepteerd, zie Z-01) |
| `excessive-permissions` | 9 | **0** ✅ |
| `artipacked` | 10 | **0** ✅ |
| **Totaal actief** | **54** | **35** (alle geaccepteerd) |

Alle actieve bevindingen zijn opgelost. De resterende 35 `unpinned-uses` zijn expliciet geaccepteerd in §4 Z-01 met onderbouwing.

## 9. Conclusie

De Zizmor-scan toont aan dat de basis-pipeline veilig is opgezet:
- Geen gebruik van `@main`/`@latest` tags (vereist door projectbeleid)
- Geen `pull_request_target` zonder review (geen script injection)
- Geen self-hosted runners (geen runner-takeover risico)
- Alle 9 workflows hebben nu expliciete `permissions:` blocks (least privilege)
- Alle `actions/checkout` stappen gebruiken `persist-credentials: false` (credential isolation)

De pipeline voldoet aan defense-in-depth principes conform NEN-7510 A.8.3 en A.8.28.
