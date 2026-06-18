# AI-tooling verantwoording — LU2 Security & Onderhoudbaarheid

| | |
|---|---|
| **Scope** | Heel het LU2 PoC project — beide sporen (security + onderhoudbaarheid) |
| **Norm** | NEN-7510:2024-2 §8.29 + WS04A (verantwoord AI-gebruik) |
| **Datum** | 2026-06-18 |
| **Teamleden** | Amine, Nick, Enes, Rami |

---

## 1. Doel

Conform de rubric (criterium **Mitigatie & validatie** en **Realisatie PoC**) en NEN-7510 §8.29 verantwoordt dit document hoe AI-tooling is ingezet bij dit project. Door dit centraal vast te leggen blijft het auditspoor traceerbaar zonder dat elke fix-doc dezelfde tekst hoeft te herhalen.

Per teamlid wordt vastgelegd:
- **Welke AI-tool(s)** zijn gebruikt
- **Waar in het project** AI is ingezet
- **Kritische reflectie** — wat ging goed, wat ging mis

---

## 2. Gebruikte AI-tooling (overzicht)

| Tool | Type |
|---|---|
| **GitHub Copilot** | Code-completion in IDE |
| **Claude (Anthropic)** | Conversational AI |
| **CodeQL (GitHub)** | Statische AI-ondersteunde SAST |
| **Snyk** | AI/heuristieke SCA + SAST |
| **Dependabot** | Geautomatiseerde update-bot |

---

## 3. Individuele verantwoording

### 3.1 Amine

**Gebruikte tools:** Claude

**Inzet:**
- **Code-assistent** bij het uitwerken van fixes (B-02 hardcoded credentials, B-07 CSRF/sessie-hardening)
- **Code review** — sparringpartner voor het beoordelen van eigen en andermans code (PR's van Nick, Enes, Rami)
- **Bug fixes** — meedenken bij het lokaliseren van de oorzaak van bugs en het uitwerken van een passende oplossing
- **Security-vraagstukken** — uitleg hoe een specifieke kwetsbaarheid (CSRF, hardcoded creds, trust boundary) correct gemitigeerd kan worden conform NEN-7510 + OWASP
- **Documentatie-structuur** — opzet en formattering van audit-rapporten, fix-docs, traceability matrix, CRA-mapping

**Kritische reflectie:**
- ✅ Werkte goed als sparringpartner voor security-onderbouwing en NEN-7510 mapping
- ✅ Versnelde de markdown-formattering van audit-tabellen
- ⚠️ AI gaf in vroege drafts **verzonnen CVE-ID's** (CVE-2026-40076/75) — deze zijn handmatig gecorrigeerd in `06-update-advies.md` UA-10/UA-11
- ⚠️ Statusvelden in audit-tabellen werden niet automatisch bijgewerkt na een fix; handmatig nalopen blijft nodig
- ✅ Les: AI als concept-generator gebruiken, maar elke CVE-claim, control-nummer en score handmatig verifiëren via NVD

---

### 3.2 Nick

**Gebruikte tools:** *in te vullen door Nick*

**Inzet:**
- *in te vullen door Nick*

**Kritische reflectie:**
- *in te vullen door Nick*

---

### 3.3 Enes

**Gebruikte tools:** *in te vullen door Enes*

**Inzet:**
- *in te vullen door Enes*

**Kritische reflectie:**
- *in te vullen door Enes*

---

### 3.4 Rami

**Gebruikte tools:** M365 Copilot

**Inzet:**
- Ondersteuning bij het begrijpen en analyseren van securitybevindingen.
- Hulp bij het structureren van documentatie en het formuleren van duidelijke markdown-teksten.
- Ondersteuning bij het nadenken over mogelijke oplossingen en mitigaties.
- Hulp bij het controleren van codewijzigingen, testaanpak en Git-commando’s.
- Sparringpartner bij het verbeteren van de onderbouwing van risico’s, fixes en validatie.

**Kritische reflectie:**
- AI hielp goed om technische bevindingen duidelijker te begrijpen en beter te verwoorden.
- AI was nuttig als sparringpartner bij het kiezen tussen mogelijke oplossingen.
- AI versnelde het maken en verbeteren van documentatie.
- Niet alle suggesties waren direct bruikbaar; sommige antwoorden moesten worden gecontroleerd worden in de repo.
- AI gaf soms te uitgebreide oplossingen, waardoor handmatige selectie en controle nodig bleef.
- Alle codewijzigingen, testresultaten, Git-output en securityclaims zijn handmatig gecontroleerd voordat ze zijn gebruikt.
- Les: AI is vooral handig als hulpmiddel voor uitleg, structuur en reflectie, maar de eindverantwoordelijkheid blijft bij mij.

---

## 4. Risico-inschatting AI-gebruik (team-breed)

| Risico | Mitigatie in dit project |
|---|---|
| **AI-hallucinatie** (verzonnen feiten/CVE's) | Elke externe verwijzing handmatig geverifieerd via NVD, CWE-database en officiële docs |
| **Onveilige code-suggesties** | Alle PR's draaien door CodeQL, Snyk, Sonar Quality Gate vóór merge |
| **Plagiaat / IP-inbreuk** | Geen externe code 1-op-1 gekopieerd; alle wijzigingen reviewbaar in PR-diff |
| **Vertrouwelijkheid** | Geen PII of credentials in prompts gevoerd; alleen module-broncode (publiek) |
| **Verlies van leereffect** | Elke fix is door teamleden handmatig uitgevoerd + getest; AI als versneller, niet als vervanger |

---

## 5. Conformiteit NEN-7510 §8.29 + WS04A

- ✅ AI-gebruik gedocumenteerd (dit document)
- ✅ Elk inhoudelijk feit met externe bron geverifieerd
- ✅ Geen vertrouwelijke data in AI-prompts
- ✅ Output door mens beoordeeld vóór merge
- ✅ Reflectie op tekortkomingen opgenomen

Dit voldoet aan de eis dat AI-gebruik in een gereguleerde (zorg)context **transparant, verifieerbaar en gecontroleerd** moet zijn.
