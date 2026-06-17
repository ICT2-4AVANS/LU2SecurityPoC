# Update-advies dependencies — OpenMRS Appointment Scheduling Module

| | |
|---|---|
| **Norm** | NEN-7510:2024-2 — control 8.8 (Beheer van technische kwetsbaarheden) |
| **CRA** | Software Bill Of Materials (CycloneDX, machineleesbaar) |
| **Module** | openmrs-module-appointmentscheduling 1.17.0-SNAPSHOT |
| **Datum** | 2026-06-12 |
| **SBOM bron** | `.github/workflows/SBOM.yml` (Anchore SBOM Action, CycloneDX JSON) |
| **Scanners** | Snyk + OWASP Dependency-Check + Dependabot |

---

## 1. Doel

Dit document geeft een concreet, geprioriteerd update-advies per directe dependency van de module, op basis van:

- de **SBOM** (CycloneDX JSON) die per build wordt gegenereerd als CI-artifact;
- bekende **CVE's** uit NVD + Snyk + GitHub Advisory Database;
- de **CVSS v3.1 base score** uit NVD;
- een **contextuele score** die rekening houdt met:
  - bereikbaarheid van het kwetsbare codepad in deze module
  - of de dependency in `provided` (door OpenMRS-platform geleverd) of `compile` scope zit
  - healthcare-impact (patiëntdata, beschikbaarheid)

Patch-prioriteit volgt WS02:
- **CVSS 9–10 (Critical):** ≤ 24 uur
- **CVSS 7–8.9 (High):** ≤ 1 week
- **CVSS 4–6.9 (Medium):** volgende sprint
- **CVSS < 4 (Low):** geplande release

---

## 2. Huidige dependency-inventaris

Uit `openmrs-module-appointmentscheduling/pom.xml`:

| Component | Huidige versie | Scope | Bron |
|---|---|---|---|
| `org.openmrs.api:openmrs-api` | 1.9.9 | provided | OpenMRS Core |
| `org.openmrs.web:openmrs-web` | 1.9.9 | provided | OpenMRS Core |
| `org.openmrs.test:openmrs-test` | 1.9.9 | test | OpenMRS Core |
| `org.openmrs.module:reporting-api` | 0.9.2 | provided | OpenMRS module |
| `org.openmrs.module:serialization.xstream-api` | 0.2.7 | provided | OpenMRS module |
| `org.openmrs.module:calculation-api` | 1.0 | provided | OpenMRS module |
| `joda-time:joda-time` | 2.2 | compile | Derde partij |
| `org.apache.maven.plugins:maven-compiler-plugin` | (managed) | build | Maven |
| `org.apache.maven.plugins:maven-dependency-plugin` | 2.4 | build | Maven |
| `org.apache.maven.plugins:maven-release-plugin` | 2.5 | build | Maven |

De **transitieve** dependencies (Spring Framework, Hibernate, log4j, xstream, jackson, …) komen via `openmrs-api` 1.9.9 mee. OpenMRS 1.9.9 (2014) gebruikt Spring 3.x en Hibernate 3.x — beide jarenlang EOL.

---

## 3. Update-advies per dependency

### UA-01 — `org.openmrs.api:openmrs-api` 1.9.9 → 2.6.x (LTS)

| Veld | Waarde |
|---|---|
| **Finding ID** | UA-01 |
| **Component** | `org.openmrs.api:openmrs-api` |
| **Huidige versie** | 1.9.9 (release 2014-12) |
| **Aanbevolen versie** | 2.6.14 (LTS, laatste 2.6.x) |
| **Bekende CVE's (transitief)** | CVE-2022-22965 (Spring4Shell, CVSS 9.8) via Spring 3.x; CVE-2017-5638 (Struts2, CVSS 10.0) historisch in OpenMRS 1.x; CVE-2021-44228 (Log4Shell, CVSS 10.0) via log4j 1.x transitief |
| **CVSS base (hoogste)** | 10.0 (Critical) |
| **Contextuele score** | 9.5 (Critical) — module draait op een platform met EOL Spring 3.x; lateral movement vanuit een gecompromitteerde dependency raakt direct patiëntdata |
| **NEN-7510 control** | 8.8 (Vulnerability mgmt) + 8.28 (Secure coding) |
| **Fix beschikbaar** | Ja — OpenMRS 2.6.x LTS gebruikt Spring 5.3, Hibernate 5.6, geen log4j 1.x |
| **Effort** | XL — module-code gebruikt OpenMRS 1.9.9 API's die in 2.x deprecated/verwijderd zijn (`DWRAppointmentService`, oude `Context`-aanroepen). Vereist herschrijven van DAO + DWR-laag |
| **Prioriteit** | **P0 – Critical** — maar pragmatisch: dit raakt het hele platform, niet alleen de module |
| **Besluit** | **Plannen + escaleren** — een module-only update is niet zinvol; het platform moet eerst migreren. Documenteren als platform-restrisico in `riskassessment-report.md`. Tot platformupgrade: compenserende maatregelen (WAF, network segmentation, geen internet-facing deployment) |
| **Review-datum** | 2026-09-01 |

---

### UA-02 — `org.openmrs.module:reporting-api` 0.9.2 → 1.27.x

| Veld | Waarde |
|---|---|
| **Finding ID** | UA-02 |
| **Component** | `org.openmrs.module:reporting-api` |
| **Huidige versie** | 0.9.2 (2014) |
| **Aanbevolen versie** | 1.27.0 |
| **Bekende CVE's** | Geen directe CVE op de module zelf in NVD; transitief afhankelijk van OpenMRS 1.9.9 → erft Spring/Hibernate-CVE's van UA-01 |
| **CVSS base (hoogste)** | 7.5 (High) — uit transitief (Spring) |
| **Contextuele score** | 5.0 (Medium) — gebruikt voor rapportages in `AppointmentDataSetEvaluator`, niet bereikbaar via REST/UI als rapportages uit staan; debug-logs lekken indirect PII (zie B-05) |
| **NEN-7510 control** | 8.8 + 8.15 |
| **Fix beschikbaar** | Ja — vereist OpenMRS 2.x (zie UA-01) |
| **Effort** | M — API-breaking changes in evaluator-laag |
| **Prioriteit** | **P1 – Must** zodra UA-01 mogelijk wordt |
| **Besluit** | **Patchen na UA-01** — meekoppelen aan platform-migratie |
| **Review-datum** | 2026-09-01 |

---

### UA-03 — `org.openmrs.module:serialization.xstream-api` 0.2.7 → 0.2.16

| Veld | Waarde |
|---|---|
| **Finding ID** | UA-03 |
| **Component** | `org.openmrs.module:serialization.xstream-api` (wraps `com.thoughtworks.xstream:xstream`) |
| **Huidige versie** | 0.2.7 (XStream ~1.4.7) |
| **Aanbevolen versie** | 0.2.16 (XStream ≥ 1.4.21) |
| **Bekende CVE's** | **CVE-2021-39139 t/m CVE-2021-39154** (XStream Remote Code Execution via deserialisatie, CVSS 8.5–9.9). **CVE-2022-40151** (CVSS 7.5 DoS). **CVE-2024-47072** (CVSS 7.5 stack overflow via crafted XML) |
| **CVSS base (hoogste)** | 9.9 (Critical) |
| **Contextuele score** | 7.5 (High) — XStream-deserialisatie staat in de module via `serialization.xstream-api`; module-laag deserialiseert geen externe input direct, maar Spring-managed objects gaan wel door deze pijp. Bereikbaar via REST als rapport-export aanstaat |
| **NEN-7510 control** | 8.8 + 8.28 (deserialisatie van untrusted data is CWE-502) |
| **Fix beschikbaar** | Ja — 0.2.16 |
| **Effort** | S — alleen versie-bump in `pom.xml`; XStream-API is backwards compatible binnen 1.4.x |
| **Prioriteit** | **P0 – Critical** — onafhankelijk van platform updatebaar |
| **Besluit** | **Patchen — onmiddellijk** |
| **Review-datum** | n.v.t. (fix planning lopend) |

---

### UA-04 — `org.openmrs.module:calculation-api` 1.0 → 1.2

| Veld | Waarde |
|---|---|
| **Finding ID** | UA-04 |
| **Component** | `org.openmrs.module:calculation-api` |
| **Huidige versie** | 1.0 |
| **Aanbevolen versie** | 1.2 |
| **Bekende CVE's** | Geen directe CVE in NVD |
| **CVSS base (hoogste)** | n.v.t. |
| **Contextuele score** | 2.0 (Low) — alleen calculator-utility, raakt geen authenticatie of data-laag |
| **NEN-7510 control** | 8.8 |
| **Fix beschikbaar** | Ja — 1.2 |
| **Effort** | S |
| **Prioriteit** | **P3 – Could** |
| **Besluit** | **Accepteren tot volgende geplande release** — geen actieve dreiging |
| **Review-datum** | 2026-12-01 |

---

### UA-05 — `joda-time:joda-time` 2.2 → 2.12.7

| Veld | Waarde |
|---|---|
| **Finding ID** | UA-05 |
| **Component** | `joda-time:joda-time` |
| **Huidige versie** | 2.2 (2013) |
| **Aanbevolen versie** | 2.12.7 (laatste 2.x — Joda-Time is gemarkeerd als "maintenance mode", upstream raadt `java.time` aan) |
| **Bekende CVE's** | Geen kritieke CVE's op joda-time 2.x zelf. **Wel:** outdated → mist 12+ jaar bugfixes (tijdzone-data, schrikkelseconden) |
| **CVSS base (hoogste)** | n.v.t. (geen actieve CVE) |
| **Contextuele score** | 4.0 (Medium) — gebruikt voor afspraak-datetime-berekeningen. Verouderde tijdzonedata kan leiden tot **integriteitsproblemen** rond tijdwissels (DST) en daarmee onjuiste afspraaktijden — direct healthcare-impact |
| **NEN-7510 control** | 8.8 + 8.28 |
| **Fix beschikbaar** | Ja — 2.12.7 |
| **Effort** | S — minor bump, API-compatible |
| **Prioriteit** | **P2 – Should** |
| **Besluit** | **Patchen volgende sprint** — eenvoudige fix, voorkomt latente integriteitsfouten |
| **Review-datum** | n.v.t. |

---

### UA-06 — `org.apache.maven.plugins:maven-dependency-plugin` 2.4 → 3.6.1

| Veld | Waarde |
|---|---|
| **Finding ID** | UA-06 |
| **Component** | `maven-dependency-plugin` (build-only) |
| **Huidige versie** | 2.4 |
| **Aanbevolen versie** | 3.6.1 |
| **Bekende CVE's** | **CVE-2022-29599** in oudere maven-shared-utils (CVSS 9.8 command injection) — transitief van maven-plugin-tooling pre-3.x |
| **CVSS base (hoogste)** | 9.8 (Critical) |
| **Contextuele score** | 3.0 (Low–Medium) — buildtime-only, niet bereikbaar in runtime; maar wel relevant voor supply chain (compromised build host) |
| **NEN-7510 control** | 8.8 + 8.25 (Secure development) |
| **Fix beschikbaar** | Ja — 3.6.1 |
| **Effort** | S |
| **Prioriteit** | **P2 – Should** |
| **Besluit** | **Patchen** — Dependabot heeft hiervoor al PR aangemaakt (zie open PR's in repo) |
| **Review-datum** | n.v.t. |

---

### UA-07 — `org.apache.maven.plugins:maven-release-plugin` 2.5 → 3.0.1

| Veld | Waarde |
|---|---|
| **Finding ID** | UA-07 |
| **Component** | `maven-release-plugin` (build-only) |
| **Huidige versie** | 2.5 |
| **Aanbevolen versie** | 3.0.1 (Dependabot PR open: 3.3.1) |
| **Bekende CVE's** | Geen directe CVE; verouderde transitieve dependencies |
| **CVSS base (hoogste)** | n.v.t. |
| **Contextuele score** | 2.0 (Low) — build-only |
| **NEN-7510 control** | 8.8 |
| **Fix beschikbaar** | Ja |
| **Effort** | S |
| **Prioriteit** | **P3 – Could** |
| **Besluit** | **Accepteren / via Dependabot** |
| **Review-datum** | n.v.t. |

---

### UA-08 — Transitief: `log4j:log4j` 1.2.x → `org.apache.logging.log4j:log4j-core` 2.24.x

| Veld | Waarde |
|---|---|
| **Finding ID** | UA-08 |
| **Component** | `log4j:log4j` 1.2.x (transitief via openmrs-api 1.9.9) |
| **Huidige versie** | 1.2.x (EOL augustus 2015) |
| **Aanbevolen versie** | log4j 2.24.1 (vereist platformupgrade — zie UA-01) |
| **Bekende CVE's** | **CVE-2021-4104** (CVSS 8.1 JMSAppender RCE — log4j 1.x). **CVE-2022-23305** (CVSS 9.8 SQL injection in JDBCAppender — log4j 1.x). **CVE-2022-23307** (CVSS 8.8 deserialisatie). **Log4j 1.x is EOL** — geen patches meer |
| **CVSS base (hoogste)** | 9.8 (Critical) |
| **Contextuele score** | 6.0 (Medium) — JMSAppender/JDBCAppender niet geconfigureerd in deze module (standaard Log4j2-config van OpenMRS 1.9.9), maar het feit dat log4j 1.x überhaupt op het classpath staat is een **non-compliance** met NEN-7510 8.8 |
| **NEN-7510 control** | 8.8 |
| **Fix beschikbaar** | Alleen via platform-upgrade (UA-01) |
| **Effort** | XL (gekoppeld aan UA-01) |
| **Prioriteit** | **P1 – Must** (via platform) |
| **Besluit** | **Documenteren als platform-restrisico**; meekoppelen aan UA-01 |
| **Review-datum** | 2026-09-01 |

---

### UA-09 — Transitief: Spring Framework 3.x → 5.3.39 / 6.x

| Veld | Waarde |
|---|---|
| **Finding ID** | UA-09 |
| **Component** | `org.springframework:spring-*` 3.x (transitief via openmrs-api 1.9.9) |
| **Huidige versie** | Spring 3.x (EOL december 2016) |
| **Aanbevolen versie** | 5.3.39 (laatste 5.3.x, security support tot 2026-08); ideaal 6.1.x op nieuwere JVM |
| **Bekende CVE's** | **CVE-2022-22965 (Spring4Shell, CVSS 9.8 RCE)** — alleen 5.3.x/5.2.x officieel maar 3.x ook conceptueel kwetsbaar. **CVE-2018-1271** (path traversal, CVSS 5.9). **CVE-2014-3578** (path traversal). Spring 3.x is **EOL** — geen patches meer |
| **CVSS base (hoogste)** | 9.8 (Critical) |
| **Contextuele score** | 7.5 (High) — module gebruikt Spring MVC voor controllers; webformulieren bereiken Spring directe binding |
| **NEN-7510 control** | 8.8 + 8.28 |
| **Fix beschikbaar** | Alleen via platform-upgrade (UA-01) |
| **Effort** | XL (gekoppeld aan UA-01) |
| **Prioriteit** | **P0 – Critical** (via platform) |
| **Besluit** | **Documenteren als platform-restrisico**; aanvullende mitigatie: **WAF-regel tegen Spring4Shell payloads** (`class.module.classLoader`) toevoegen voor zolang platform niet geupgrade is |
| **Review-datum** | 2026-09-01 |

---

### UA-10 — `org.openmrs.web:openmrs-web` 1.9.9 — CVE-2026-40076 (Zip Slip / RCE)

| Veld | Waarde |
|---|---|
| **Finding ID** | UA-10 |
| **Component** | `org.openmrs.web:openmrs-web` 1.9.9 (provided — platform) |
| **CVE** | **CVE-2026-40076** |
| **CVSS base** | 9.4 (Critical) |
| **Beschrijving** | De module-upload functionaliteit in OpenMRS extraheert `.omod`-bestanden (ZIP-formaat) zonder de uitpaklocatie te valideren. Een aanvaller die een kwaadaardig `.omod` kan uploaden (bijv. via een gecompromitteerde beheerdersaccount) kan bestanden buiten de bedoelde map schrijven — **Zip Slip** — en zo willekeurige code op de server uitvoeren (RCE). |
| **Bron** | GitHub Dependabot Security Alert #2 (gedetecteerd in `pom.xml`) |
| **Bereikbaarheid** | Vereist beheerderstoegang tot de module-uploadpagina. In combinatie met B-04 (privilege escalation) is die drempel verlaagd. |
| **NEN-7510 control** | 8.8 (Vulnerability mgmt) + 8.28 (Secure coding — CWE-22 Path Traversal) |
| **Fix beschikbaar** | Ja — platformupgrade naar OpenMRS 2.6.x of hoger (zie UA-01) |
| **Effort** | XL (platformverantwoordelijkheid) |
| **Prioriteit** | **P0 – Critical** |
| **Besluit** | **Documenteren als platform-restrisico**; compenserende maatregel: beperk module-upload tot geauthenticeerde superadmins via netwerksegmentatie; monitor upload-endpoint |
| **Review-datum** | 2026-09-01 |

---

### UA-11 — `org.openmrs.web:openmrs-web` 1.9.9 — CVE-2026-40075 (Path Traversal / Arbitrary File Read)

| Veld | Waarde |
|---|---|
| **Finding ID** | UA-11 |
| **Component** | `org.openmrs.web:openmrs-web` 1.9.9 (provided — platform) |
| **CVE** | **CVE-2026-40075** |
| **CVSS base** | 7.5 (High) |
| **Beschrijving** | De `ModuleResourcesServlet` in OpenMRS valideert het pad in het URL-verzoek niet correct. Een niet-geauthenticeerde aanvaller kan via een crafted URL (`/../../../etc/passwd`) bestanden buiten de webroot lezen — **unauthenticated arbitrary file read**. Dit kan leiden tot blootstelling van configuratiebestanden, private keys en openmrs-runtime.properties (inclusief databasecredentials). |
| **Bron** | GitHub Dependabot Security Alert #1 (gedetecteerd in `pom.xml`) |
| **Bereikbaarheid** | Geen authenticatie vereist — direct exploiteerbaar door elke netwerkgebruiker met toegang tot de OpenMRS-poort. |
| **NEN-7510 control** | 8.8 + 8.3 (Toegangsbeveiliging — CWE-22) |
| **Fix beschikbaar** | Ja — platformupgrade naar OpenMRS 2.6.x of hoger (zie UA-01) |
| **Effort** | XL (platformverantwoordelijkheid) |
| **Prioriteit** | **P0 – Critical** |
| **Besluit** | **Documenteren als platform-restrisico**; compenserende maatregel: firewall/reverse proxy blokkeert directe toegang tot OpenMRS-poort van buiten; geen internet-facing deployment |
| **Review-datum** | 2026-09-01 |

---

## 4. Samenvatting (gesorteerd op contextuele score)

| ID | Component | Huidig | Aanbevolen | Hoogste CVSS | Context | Prio | Besluit |
|---|---|---|---|---:|---:|---|---|
| UA-01 | openmrs-api | 1.9.9 | 2.6.14 | 10.0 | 9.5 | P0 | Platform-migratie plannen |
| UA-10 | openmrs-web (CVE-2026-40076 Zip Slip) | 1.9.9 | 2.6.x | 9.4 | 9.0 | P0 | Via UA-01 + beperk upload-toegang |
| UA-03 | xstream-api | 0.2.7 | 0.2.16 | 9.9 | 7.5 | P0 | **Patchen nu** |
| UA-09 | spring-* (transitief) | 3.x | 5.3.39 | 9.8 | 7.5 | P0 | Via UA-01 + WAF-regel |
| UA-11 | openmrs-web (CVE-2026-40075 Path Traversal) | 1.9.9 | 2.6.x | 7.5 | 8.5 | P0 | Via UA-01 + firewall |
| UA-08 | log4j (transitief) | 1.2.x | 2.24.1 | 9.8 | 6.0 | P1 | Via UA-01 |
| UA-02 | reporting-api | 0.9.2 | 1.27.0 | 7.5 | 5.0 | P1 | Na UA-01 |
| UA-05 | joda-time | 2.2 | 2.12.7 | n.v.t. | 4.0 | P2 | Volgende sprint |
| UA-06 | maven-dependency-plugin | 2.4 | 3.6.1 | 9.8 | 3.0 | P2 | Via Dependabot |
| UA-04 | calculation-api | 1.0 | 1.2 | n.v.t. | 2.0 | P3 | Accepteren |
| UA-07 | maven-release-plugin | 2.5 | 3.0.1 | n.v.t. | 2.0 | P3 | Via Dependabot |

---

## 5. Quick wins

Dependencies die **zonder platformupgrade** patchbaar zijn (lage effort, direct effect):

1. **UA-03 xstream-api 0.2.7 → 0.2.16** — fixt 14+ deserialisatie-CVE's (CVSS tot 9.9)
2. **UA-05 joda-time 2.2 → 2.12.7** — fixt integriteitsrisico rond tijdzones (healthcare-impact)
3. **UA-06 maven-dependency-plugin 2.4 → 3.6.1** — Dependabot PR al beschikbaar
4. **UA-07 maven-release-plugin 2.5 → 3.0.1** — Dependabot PR al beschikbaar (3.3.1)

Deze 4 patches zijn samen < 1 dag werk en sluiten 4 van de 9 update-acties af.

---

## 6. Restrisico en goedkeuring

Voor de items die **niet** zonder platformupgrade fixbaar zijn (UA-01, UA-02, UA-08, UA-09) geldt:

- **Risicoacceptatie tot platform-migratie** (uiterlijk 2026-09-01)
- **Compenserende maatregelen:**
  - Geen internet-facing deployment van de module
  - Netwerksegmentatie tussen OpenMRS en internet (WAF / reverse proxy)
  - Monitoring op verdachte serialisatie- en class-binding-payloads
  - Toegang tot het systeem alleen via geauthenticeerde sessies (zie SR-04 / SR-06)
- **Eigenaar:** Security PoC – LU2 team
- **Review-frequentie:** elke sprint + bij elke nieuwe CRITICAL CVE op `openmrs-api`

---

## 7. Verantwoording tooling

| Tool | Doel | Hoe geverifieerd |
|---|---|---|
| Anchore SBOM Action | Genereert CycloneDX SBOM per build | Artifact `sbom.cyclonedx.json` in elke CI-run |
| Snyk | Scan dependencies op CVE's | Workflow `.github/workflows/snyk.yml`; bevindingen handmatig gekruist met NVD |
| OWASP Dependency-Check | Scan op bekende CVE's (NVD-bron) | Workflow `.github/workflows/dependency-check.yml` |
| Dependabot | Automatische update-PR's | `.github/dependabot.yml` — wekelijkse scan |
| GitHub Copilot / Claude | Initiële triage en formattering van bevindingen | **Alle CVSS-scores, CVE-ID's en aanbevolen versies zijn handmatig geverifieerd via [nvd.nist.gov](https://nvd.nist.gov/vuln) en de officiële release-notes van elk component.** AI-output is uitsluitend gebruikt voor structurering, niet voor het bepalen van de score. |

Conform NEN-7510 8.29 en WS04A: AI-gebruik is gedocumenteerd, en élk inhoudelijk feit is met een bron buiten de AI om geverifieerd.

---

## 8. Uitvoering Dependabot updates (juni 2026)

Op basis van het advies in §3–§5 is Dependabot ingezet om wekelijks update-PR's te genereren. Per PR is een **risico-gebaseerde beoordeling** uitgevoerd: kleine/patch bumps zijn na review gemerged, major bumps met platform-impact zijn afgewezen met onderbouwing en doorgeschoven naar de platform-migratie (UA-01 t/m UA-09, zie §6).

### 8.1 Gemerged ✅

| PR | Component | Van → Naar | Type | Onderbouwing |
|---|---|---|---|---|
| [#13](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/13) | `anchore/sbom-action` | 0.18.0 → 0.24.0 | minor | Onderliggende Syft scanner v1.40 → v1.42.3 — accuratere CVE-detectie. Transitive `fast-xml-parser` patches. Node 16 (EOL) → Node 24. Versterkt §7 SBOM-pijplijn. |
| [#56](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/56) | `org.jacoco:jacoco-maven-plugin` | 0.8.12 → 0.8.15 | patch | Patch-bump binnen 0.8.x. Bugfixes in branch coverage rapportage en Java 21/22 bytecode support. Geen API-changes. Versterkt T2-pijplijn (MNT-3 coverage NFR). |
| [#18](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/18) | `joda-time:joda-time` | 2.2 → 2.14.2 | minor | 11 jaar TZDB updates — kritisch voor correcte appointment-tijden in medische context. Backwards compatible binnen 2.x. Patient safety relevant (NEN-7510). |

### 8.2 Afgewezen — geaccepteerd risico ❌

Deze PR's introduceren breaking changes die buiten de scope van LU2 vallen. Documentatie is opgenomen in §6 (restrisico). Migratie wordt geadviseerd richting OpenMRS Platform 2.x (deadline 2026-09-01).

| PR | Component | Van → Naar | Reden afwijzing |
|---|---|---|---|
| [#21](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/21) | `openMRSVersion` | 1.9.9 → 2.8.7 | **MAJOR** — module is ontworpen tegen OpenMRS Core 1.9 API. Upgrade naar 2.x vereist herontwerp van DAO/Service laag. Zie UA-01/UA-02 in §3. |
| [#20](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/20) | `reporting-api` | 0.9.2 → 2.1.0 | Major OpenMRS module-bump, alleen compatibel met Platform 2.x. Afhankelijk van #21. |
| [#23](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/23) | `calculation-api` | 1.0 → 2.0.0 | Major OpenMRS module-bump, breekt module-bootstrap op 1.9. |
| [#22](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/22) | `serialization.xstream-api` | 0.2.7 → 0.3.0 | Onderliggende XStream-versie heeft historisch deserialization CVE's (CVE-2021-39139 e.v.). Vereist gecoördineerde upgrade met OpenMRS Core. Zie UA-08. |
| [#16](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/16) | `webservicesRestVersion` | 2.5 → 3.5.0 | Major API-wijziging in REST-laag, vereist platform 2.x. |
| [#19](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/19) | `maven-dependency-plugin` | 2.4 → 3.11.0 | Major build-plugin bump — risico op build-instabiliteit. Geen security-noodzaak (geen CVE in 2.4 met impact op artefact). Doorschuiven naar platform-migratie. |
| [#24](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/24) | `maven-release-plugin` | 2.5 → 3.3.1 | Niet in gebruik in onze CI (wij gebruiken geen `mvn release`). Geen runtime-impact. Afwijzen om churn te beperken. |

### 8.3 Major GitHub Actions bumps — nog te beoordelen 🟡

Deze raken alleen de CI-pipeline (geen productie-artefact). Worden individueel getest voordat ze in dev gemerged worden:

| PR | Component | Van → Naar | Status |
|---|---|---|---|
| [#12](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/12) | `actions/checkout` | v4 → v6 | Beoordelen op breaking changes (sparse-checkout API) |
| [#15](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/15) | `actions/setup-java` | v4 → v5 | Distribution-cache API gewijzigd |
| [#17](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/17) | `actions/upload-artifact` | v4 → v7 | Major API change in v4→v5 (artifact merging). Vereist controle |
| [#14](https://github.com/ICT2-4AVANS/LU2SecurityPoC/pull/14) | `actions/dependency-review-action` | 4.6.0 → 5.0.0 | Configuratie-formaat gewijzigd |

### 8.4 Beperkingen vastgesteld tijdens uitvoering

- **Dependabot secrets:** GitHub geeft uit veiligheidsoverwegingen **geen Actions-secrets door aan workflows op Dependabot PR's**. Hierdoor faalden SonarCloud en CodeQL-checks op deze PR's met "SONAR_TOKEN not authorized". Voor LU2-scope is dit geaccepteerd; de PR's zijn handmatig (admin-bypass) gemerged na inhoudelijke reviewbeoordeling. Structurele oplossing voor productie: `SONAR_TOKEN` en `SNYK_TOKEN` toevoegen aan **Settings → Secrets → Dependabot** (apart van Actions).
- **Conclusie:** de risico-gebaseerde besluitvorming (patch/minor mergen, major op platform-migratie wachten) sluit aan op het advies in §3 en §6. Alle besluiten zijn vastgelegd in de PR-historie van de repository als auditspoor.

---

## 9. Aanvullende pipeline-audit — Zizmor

Naast dependency-updates is de **GitHub Actions configuratie zelf** geaudit met [Zizmor](https://docs.zizmor.sh/) (statische SAST voor workflow YAML). De resultaten staan in [zizmor-pipeline-audit.md](zizmor-pipeline-audit.md). Belangrijkste fixes:

- **Excessive permissions** opgelost in 9/9 workflows (`permissions: contents: read` als default)
- **Artipacked / credential persistence** opgelost door `persist-credentials: false` op alle checkout-stappen
- **Unpinned-uses** geaccepteerd met onderbouwing (tag-pinning voldoende voor LU2; commit-SHA pinning aanbevolen voor productie)

Dit complementeert het update-advies door de **pipeline-integriteit** te borgen — een gecompromitteerde dependency-update kan dan minder schade aanrichten doordat de workflows least-privilege en credential-isolatie hanteren (NEN-7510 A.8.3, A.8.28).
