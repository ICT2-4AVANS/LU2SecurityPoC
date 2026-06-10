# Modulekeuze

## Gekozen module

**Naam:** Appointment Scheduling Module
**Repository:** openmrs-module-appointmentscheduling
**Versie:** 1.17.0-SNAPSHOT
**Broncode:** https://github.com/openmrs/openmrs-module-appointmentscheduling

---

# Aanleiding

Voor LU2 moeten we een bestaand OpenMRS-systeem onderzoeken op het gebied van onderhoudbaarheid, security en compliance. Vervolgens moeten we verbeteringen ontwerpen, implementeren en aantonen met behulp van Proof of Concepts (PoC's).

Hiervoor is gekozen voor de **Appointment Scheduling Module** van OpenMRS.

---

# Waarom deze module?

## Voldoende omvang

De opdracht vereist een systeem dat groot genoeg is om betekenisvolle verbeteronderzoeken uit te voeren. De Appointment Scheduling Module voldoet hier ruim aan.

| Metric                    | Waarde        |
| ------------------------- | ------------- |
| Total files               | 248           |
| Code lines                | 46.413        |
| Cyclomatic complexity     | 5.143         |
| Java code lines           | 12.040        |
| Java complexity           | 1.034         |
| Geschatte ontwikkelkosten | $1.519.032    |
| Geschatte ontwikkeltijd   | 16,12 maanden |
| Geschat team              | 8,37 personen |

De module bevat Java-code, JavaScript, XML, JSP en CSS. Hierdoor kunnen zowel backend- als frontendaspecten onderzocht worden.

---

## Legacy-karakter

De Appointment Scheduling Module behoort tot de oudere OpenMRS-modules en bevat code die gedurende meerdere jaren is ontwikkeld en uitgebreid.

Hierdoor is de kans groot dat de module:

- technische schuld bevat;
- verouderde ontwerpkeuzes bevat;
- complexe afhankelijkheden bevat;
- onderhoudbaarheidsproblemen bevat;
- securityproblemen bevat die in oudere software vaker voorkomen.

Dit maakt de module geschikt voor een verbeteronderzoek.

---

## Geschikt voor onderhoudbaarheidsonderzoek

Voor LU2 moet een systematische analyse van onderhoudbaarheid worden uitgevoerd.

De omvang en complexiteit van de module bieden voldoende mogelijkheden om:

- code smells te identificeren;
- hoge cyclomatische complexiteit te analyseren;
- SOLID-principes te beoordelen;
- afhankelijkheden tussen componenten te onderzoeken;
- refactoringmogelijkheden te identificeren;
- ontwerp- en architectuurverbeteringen voor te stellen.

Door de grootte van de codebase kunnen meetbare verbeteringen worden aangetoond met behulp van een herassessment van een PoC.

---

## Geschikt voor securityonderzoek

De module verwerkt gevoelige medische gegevens zoals:

- patiëntinformatie;
- afspraken;
- zorgverleners;
- locaties;
- tijdsgegevens.

Dit maakt security een belangrijk kwaliteitsaspect.

Daarnaast bevat de module:

- REST-endpoints;
- databankinteracties;
- authenticatie- en autorisatiemechanismen;
- externe afhankelijkheden (libraries en frameworks).

Deze onderdelen bieden voldoende mogelijkheden voor:

- security code reviews;
- dependency scanning;
- CVE-analyse;
- SBOM-analyse;
- penetration testing;
- onderzoek naar OWASP-kwetsbaarheden;
- onderzoek naar compliance-eisen zoals NEN 7510 en de Cyber Resilience Act (CRA).

---

## Verwachting van kwetsbaarheden

De opdrachtbeschrijving vermeldt dat oudere OpenMRS-modules bewust beschikbaar zijn gesteld omdat deze waarschijnlijk verschillende issues bevatten.

Gezien:

- de leeftijd van de module;
- de omvang van de codebase;
- het grote aantal afhankelijkheden;
- het gebruik van oudere OpenMRS-componenten;

wordt verwacht dat de module voldoende kwetsbaarheden en verbeterpunten bevat om een volledig security-onderzoek uit te voeren.

Mogelijke onderzoeksonderwerpen zijn onder andere:

- kwetsbare third-party dependencies;
- verouderde libraries;
- ontbrekende inputvalidatie;
- autorisatieproblemen;
- onvoldoende logging;
- beveiligingsproblemen in REST-endpoints;
- onderhoudbaarheidsproblemen die security-risico's veroorzaken.

---

# Verwachte opbrengst voor LU2

Met deze module verwachten wij alle onderdelen van LU2 te kunnen uitvoeren:

## Onderhoudbaarheid

- Software-assessment uitvoeren
- Code smells identificeren
- Refactoringvoorstellen maken
- PoC ontwikkelen
- Herassessment uitvoeren
- Verbeteringen aantonen

## Security

- Security code review uitvoeren
- SBOM opstellen
- CVE-analyse uitvoeren
- Penetration tests uitvoeren
- Kwetsbaarheden prioriteren
- Mitigaties implementeren
- Security PoC ontwikkelen
- Compliance-audit uitvoeren op basis van NEN 7510 en CRA

---

# Conclusie

De Appointment Scheduling Module is gekozen omdat deze voldoende groot, complex en realistisch is voor een uitgebreid onderzoek naar onderhoudbaarheid en security. De module bevat een substantiële hoeveelheid legacy-code, meerdere technologieën en waarschijnlijk diverse onderhoudbaarheids- en securityproblemen. Hierdoor biedt de module voldoende onderzoeksmogelijkheden om alle leerdoelen van LU2 te behalen en de effecten van verbeteringen aantoonbaar te maken.
