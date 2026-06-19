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

**Gebruikte tools:** ChatGPT & GitHub Copilot

**Inzet:**
- Ondersteuning bij het analyseren en uitwerken van securitybevindingen, zoals B-01 SQL Injection, B-04 privilege escalation en B-10/B-11 trust boundary violations
- Code-assistent bij het begrijpen van bestaande Java-code en het bedenken van passende fixes
- Hulp bij het schrijven en verbeteren van fix-documentatie, auditrapportteksten, traceability matrix en CRA-mapping
- Sparringpartner bij het koppelen van bevindingen aan NEN-7510-controls, zoals 8.3, 8.5, 8.15 en aanvullend 8.28
- Ondersteuning bij het formuleren van risico's, impact, mitigaties en restrisico's in duidelijke audit-taal
- Hulp bij het controleren van pull requests en het schrijven van reviewcomments
- Ondersteuning bij het verbeteren van markdown-structuur, tabellen en bewijsverwijzingen in de documentatie

**Kritische reflectie:**
- ChatGPT hielp goed bij het vertalen van technische kwetsbaarheden naar duidelijke auditrapportage
- GitHub Copilot was handig als code-assistent bij het begrijpen van bestaande code en het sneller vinden van mogelijke oplossingsrichtingen
- AI versnelde vooral het structureren van documentatie, zoals fixdocumenten, traceability matrix en auditrapportonderdelen
- Niet alle suggesties waren direct correct of bruikbaar; sommige teksten waren te algemeen of bevatten aannames die ik zelf moest controleren
- Bij securityclaims, NEN-7510-koppelingen, PR-nummers, testresultaten en statusvelden heb ik handmatig gecontroleerd of dit overeenkwam met de repository en de gemaakte fixes
- AI gaf soms te uitgebreide antwoorden, waardoor ik zelf moest kiezen wat relevant was voor ons project en wat niet
- De uiteindelijke codewijzigingen, documentatie, screenshots, testuitkomsten en onderbouwingen zijn door mij zelf gecontroleerd voordat ze zijn gebruikt
- ✅ Les: AI is een handig hulpmiddel voor uitleg, structuur, documentatie en het meedenken over fixes, maar je moet zelf blijven controleren of de oplossing klopt, het bewijs aanwezig is en de conclusie goed onderbouwd is — zeker bij security

---

### 3.3 Enes

**Gebruikte tools:** Claude, VSCode, GitHub, SonarCloud

**Inzet:**
- Gap-analyse opgezet
- JaCoCo coverage ingericht
- PIT mutation testing uitgevoerd
- AuditLogger gemaakt (wie, wat, waar, wanneer, hoe)
- Unit tests geschreven voor AuditLogger
- Surefire exports gegenereerd
- `maintainabilitytests.yml` workflow opgezet
- SonarCloud project ingericht
- bp1 t/m bp6 onderhoudbaarheid documentatie geschreven
- B-06 auth logger opgelost
- Extract Constant refactor uitgevoerd in `HibernateAppointmentDAO.java`
- Baseline en post-PoC metingen vergeleken

**Kritische reflectie:**
- Claude hielp om snel een eerste opzet te maken, maar de antwoorden waren vaak te uitgebreid; regelmatig gevraagd om het eenvoudiger te maken
- De code die Claude voorstelde is nooit direct overgenomen; alles zelf nagekeken, aangepast en gerefactord — daardoor altijd zelf de controle over de uiteindelijke oplossing gehouden
- Soms was niet duidelijk welke wijzigingen precies waren gedaan; daarom altijd zelf de bestanden geopend om alles te controleren vóór een commit
- Tests zoals `mvn verify`, PIT en de Sonar-scan moest ik zelf uitvoeren; Claude kan deze niet zelf draaien en kon de resultaten pas beoordelen nadat ik ze had gedeeld
- Niet alle voorspellingen van Claude kwamen overeen met de uiteindelijke resultaten; alle uitkomsten zelf gecontroleerd en niet alleen vertrouwd op de AI
- ✅ Les: AI is een handig hulpmiddel voor een eerste opzet, uitleg en suggesties, maar je moet AI niet blind vertrouwen. Door de code zelf te refactoren, de resultaten te controleren en alle tests zelf uit te voeren, hield ik de autonomie volledig in eigen handen

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
