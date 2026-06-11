# Risico-evaluatie CI/CD-proces

## 1. Inleiding

In dit onderdeel wordt het CI/CD-proces van het project beoordeeld op beveiligingsrisico’s. De pipeline draait via GitHub Actions en bevat meerdere controles, zoals build & test, CodeQL, OWASP Dependency-Check, Snyk, SBOM-generatie, Dependency Review, JaCoCo coverage en deployment naar test en productie.

Het doel van deze risico-evaluatie is om te bepalen welke risico’s aanwezig zijn in het CI/CD-proces, hoe groot deze risico’s zijn en welke maatregelen al aanwezig zijn of nog verbeterd kunnen worden.

---

## 2. Korte beschrijving van het CI/CD-proces

De CI/CD-pipeline bestaat uit de volgende onderdelen:

- Code wordt gepusht naar `main` of `dev`.
- Bij een push of pull request worden build- en teststappen uitgevoerd.
- Maven wordt gebruikt om de Java-applicatie te bouwen en te testen.
- CodeQL voert SAST-scans uit op de broncode.
- OWASP Dependency-Check en Snyk controleren dependencies op bekende kwetsbaarheden.
- Er wordt een SBOM gegenereerd met Anchore.
- Grype scant de SBOM op CVE’s.
- Dependency Review controleert nieuwe dependencies in pull requests.
- Dependabot maakt automatisch updatevoorstellen voor Maven packages en GitHub Actions.
- Deployment naar test en productie gebeurt via GitHub Actions.
- Voor productie wordt gebruikgemaakt van een GitHub Environment met handmatige goedkeuring.

---

## 3. Risico-evaluatie

De geselecteerde threats zijn beoordeeld met dezelfde schaal als de CIA/BIV-analyse:

```text
Risico = Kans × Impact
```

| Score | Niveau  | Betekenis                             |
| ----: | ------- | ------------------------------------- |
|   1–4 | Laag    | Acceptabel risico                     |
|   5–9 | Middel  | Monitoren en waar mogelijk verbeteren |
| 10–15 | Hoog    | Maatregel verplicht                   |
| 16–25 | Kritiek | Direct oplossen of mitigeren          |


---

## 4. Risicomatrix

De onderstaande risicomatrix geeft de belangrijkste risico’s binnen het CI/CD-proces weer.

| Risico-ID | Risico | Kans | Impact | Score | Niveau |
|---|---|---:|---:|---:|---|
| CI/CD-R1 | Kwetsbare dependency komt in de applicatie terecht | 3 | 5 | 15 | Hoog |
| CI/CD-R2 | Kwetsbaarheden worden gevonden maar blokkeren niet altijd de pipeline | 4 | 4 | 16 | Kritiek |
| CI/CD-R3 | Foutieve of onveilige deployment naar productie | 3 | 5 | 15 | Hoog |
| CI/CD-R4 | Secrets zoals `DB_PASSWORD` of `SNYK_TOKEN` lekken of worden verkeerd gebruikt | 2 | 5 | 10 | Hoog |
| CI/CD-R5 | Onvoldoende testdekking waardoor fouten in productie komen | 3 | 3 | 9 | Middel |
| CI/CD-R6 | Kwetsbare of misbruikte GitHub Actions worden uitgevoerd | 2 | 5 | 10 | Hoog |
| CI/CD-R7 | SBOM- of scanresultaten worden niet opgevolgd | 3 | 4 | 12 | Hoog |
| CI/CD-R8 | Ongecontroleerde wijzigingen worden naar `main` gemerged | 2 | 5 | 10 | Hoog |

---

## 5. Meest kritieke risico

Het meest kritieke risico in deze CI/CD-pipeline is:

**CI/CD-R2 - Kwetsbaarheden worden gevonden maar blokkeren niet altijd de pipeline**

Dit risico is gekozen omdat sommige security scans in de pipeline wel kwetsbaarheden vinden, maar niet altijd automatisch de pipeline stoppen. In de workflows wordt bijvoorbeeld gebruikgemaakt van `continue-on-error: true` bij Snyk en `fail-build: false` bij de Grype-scan.

Hierdoor kunnen high of critical kwetsbaarheden zichtbaar zijn in rapporten, maar alsnog niet direct worden opgelost. Als deze bevindingen niet goed worden opgevolgd, kan kwetsbare code of een kwetsbare dependency uiteindelijk toch in productie terechtkomen.

Daarom krijgt dit risico een score van **16** en valt het in het niveau **Kritiek**.

---

## 6. Bow-tie analyse voor CI/CD-R2 - Kwetsbaarheden blokkeren niet altijd de pipeline



---

## 7. Conclusie

De CI/CD-pipeline bevat al veel sterke beveiligingsmaatregelen, zoals CodeQL, Snyk, OWASP Dependency-Check, SBOM-generatie, Grype, Dependency Review en Dependabot. Hierdoor worden veel risico’s vroeg in het ontwikkelproces zichtbaar.

Het grootste aandachtspunt is dat sommige security scans niet altijd de pipeline blokkeren. Daardoor kunnen kwetsbaarheden wel worden gevonden, maar alsnog blijven bestaan als er geen duidelijke opvolging plaatsvindt.

De belangrijkste verbetering is om strengere quality gates toe te passen. High en critical findings zouden de pipeline standaard moeten blokkeren, tenzij er bewust een uitzondering wordt goedgekeurd. Daarnaast moet productie-deployment alleen plaatsvinden na review, succesvolle tests en duidelijke approval.