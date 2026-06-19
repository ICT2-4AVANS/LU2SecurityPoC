# False Positives Beleid — CI/CD Security Tools

| | |
|---|---|
| **Norm** | NEN-7510:2024-2 |
| **Module** | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT |
| **Datum** | 2026-06-09 |
| **Auteur** | Amine |

---

## Inleiding

De CI/CD-pipeline bevat meerdere security tools (CodeQL, OWASP Dependency-Check, Snyk, Grype) die automatisch kwetsbaarheden rapporteren. Niet elke bevinding is echter een echt risico — sommige zijn **false positives**: meldingen die technisch kloppen maar in onze context geen daadwerkelijk gevaar vormen.

Dit document beschrijft:
1. Wat een false positive is
2. Hoe we bepalen of iets een false positive is
3. Hoe we false positives registreren en onderdrukken
4. De koppeling met NEN-7510:2024-2

---

## Wat is een false positive?

Een false positive is een bevinding waarbij een security tool een kwetsbaarheid rapporteert die in onze specifieke context **niet exploiteerbaar** is. Voorbeelden:

| Type | Voorbeeld |
|---|---|
| Niet-bereikbare code | CVE in een library-klasse die onze module nooit aanroept |
| Test-only dependency | Kwetsbare library die alleen in `scope: test` zit — niet meegeleverd in productie |
| Platformafhankelijkheid | CVE in OpenMRS-core die door het platform zelf wordt gemitigeerd |
| Geen publieke exploit | CVSS hoog, maar geen bekende werkende exploit beschikbaar |

---

## Beslismodel

Bij elke bevinding stellen we de volgende vragen in volgorde:

```
Stap 1: Is de kwetsbare library aanwezig in productie (niet alleen test-scope)?
        NEE → False positive — supprimeren
        JA  → Stap 2

Stap 2: Is de kwetsbare code bereikbaar vanuit onze module?
        NEE → False positive — supprimeren met onderbouwing
        JA  → Stap 3

Stap 3: Bestaat er een publieke exploit of proof-of-concept?
        NEE → Laag risico — accepteren, documenteren
        JA  → Stap 4

Stap 4: Bestaat er een veilige versie van de library?
        JA  → Updaten (PR aanmaken)
        NEE → Mitigatie documenteren en accepteren
```

---

## Drempelwaarden

| CVSS Score | Categorie | Actie |
|---|---|---|
| ≥ 9.0 | CRITICAL | Altijd oplossen of expliciet accepteren met schriftelijke onderbouwing |
| 7.0 – 8.9 | HIGH | Oplossen tenzij aantoonbaar niet exploiteerbaar in onze context |
| 4.0 – 6.9 | MEDIUM | Beoordelen per geval via het beslismodel |
| < 4.0 | LOW | Accepteren, vastleggen in onderstaand overzicht |

---

## Bekende false positives

De onderstaande CVE's zijn beoordeeld via het beslismodel en geclassificeerd als false positive of geaccepteerd risico.

| CVE | CVSS | Tool | Library | Reden | Besluit | Datum |
|---|---|---|---|---|---|---|
| — | — | — | joda-time 2.2 | Wordt ingevuld na eerste CI-scan | 🔄 In behandeling | — |
| — | — | — | openmrs-api 1.9.9 | Wordt ingevuld na eerste CI-scan | 🔄 In behandeling | — |

> **Toelichting:** De tools draaien automatisch via CI/CD. Na de eerste volledige scan worden de gevonden CVE's hier ingevuld en per bevinding beoordeeld via het beslismodel.

---

## Suppression — geplande implementatie

Op het moment van schrijven zijn er nog geen bevestigde false positives. Zodra de eerste scan-resultaten beschikbaar zijn en de eerste false positives via het beslismodel zijn vastgesteld, worden de volgende suppression-mechanismen geactiveerd.

### OWASP Dependency-Check

Het suppression-bestand wordt aangemaakt op `.github/dependency-check-suppression.xml` en gekoppeld aan de workflow `.github/workflows/dependency-check.yml` via een `--suppression` argument. Voorbeeld inhoud:

```xml
<suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
  <suppress>
    <notes>
      CVE-XXXX-XXXXX: De kwetsbare klasse wordt niet aangeroepen vanuit onze module.
      Beoordeeld op YYYY-MM-DD door [naam]. Geen publieke exploit bekend.
    </notes>
    <packageUrl regex="true">^pkg:maven/groupId/artifactId@.*$</packageUrl>
    <cve>CVE-XXXX-XXXXX</cve>
  </suppress>
</suppressions>
```

### Snyk

Voor Snyk wordt een `.snyk` bestand in de projectroot aangemaakt. Voorbeeld inhoud:

```yaml
version: v1.25.0
ignore:
  CVE-XXXX-XXXXX:
    - '*':
        reason: 'Niet exploiteerbaar — kwetsbare klasse niet bereikbaar vanuit module'
        expires: '2027-01-01T00:00:00.000Z'
```

> **Status:** Beide suppression-bestanden bestaan momenteel nog niet en worden alleen aangemaakt zodra er bevestigde false positives zijn.

---

## Koppeling met NEN-7510:2024-2

| Control | Relatie |
|---|---|
| **A.8.8** — Beheer van technische kwetsbaarheden | Dit document is het aantoonbare bewijs van een gestructureerd kwetsbaarhedenbeheerproces. Elke CVE wordt beoordeeld, gedocumenteerd en opgevolgd. |
| **A.8.29** — Beveiligingstesten | Het gebruik van meerdere tools (SAST, SCA, SBOM) en het actief beoordelen van bevindingen toont aan dat security testing structureel is ingebed in het ontwikkelproces. |

Het bijhouden van dit overzicht toont aan dat het team bewust omgaat met kwetsbaarheden en niet blindelings alarmen negeert of accepteert.
