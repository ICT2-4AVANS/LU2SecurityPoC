# LU2SecurityPoC

Deze repository bevat een PoC-omgeving rond de OpenMRS Appointment Scheduling Module. De opzet is bedoeld om onderhoudbaarheid en security in een gecontroleerde lokale omgeving te onderzoeken, zonder testgegevens of experimentele wijzigingen naar productie door te schuiven.

## Hoe de omgevingen zijn ingericht

Er zijn drie gescheiden omgevingen, elk met een eigen `docker-compose`-bestand en eigen configuratie:

| Omgeving | Bestand | Poort | Secrets |
|---|---|---|---|
| Ontwikkeling | `docker-compose.dev.yml` | 8082 | `DB_PASSWORD` via `.env.dev` (lokaal, nooit gecommit) |
| Test | `docker-compose.test.yml` | 8081 | `DB_PASSWORD` via `.env.test` of GitHub Secrets |
| Productie | `docker-compose.prod.yml` | 80 | `DB_PASSWORD` via GitHub Secrets (environment: production) |

Elke omgeving gebruikt een aparte database (`openmrs_dev`, `openmrs_test`, `openmrs_prod`) en aparte Docker volumes, zodat data tussen omgevingen strikt gescheiden is.

De repository is daarnaast opgesplitst in een documentatielaag en de module zelf:

- `docs/` bevat projectdocumentatie waaronder de gap-analyse en pipeline-compliance.
- `openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/` bevat de broncode van de OpenMRS-module (versie 1.17.0-SNAPSHOT, OpenMRS 1.9.9).
- Binnen de module is een expliciet onderscheid gemaakt tussen productcode en testcode:
  - `api/src/main/` en `omod/src/main/` bevatten de productiecode en resources.
  - `api/src/test/` en `omod/src/test/` bevatten alleen testcode en testdata.

## Hoe wordt voorkomen dat testdata in productie terechtkomt

Testdata staat alleen in testresources, zoals:

- `api/src/test/resources/`
- `omod/src/test/resources/`

Deze datasets worden gebruikt door geautomatiseerde tests en zijn niet bedoeld voor productiegebruik. Maven behandelt `src/test` apart van `src/main`, waardoor testresources niet in de normale runtime-artifacts horen te zitten.

Daarnaast zijn er extra waarborgen:

- productiecode leest uitsluitend uit `src/main` resources en niet uit testdatasets;
- testdatasets zijn herkenbaar aan namen als `*TestDataset.xml`;
- build- en testoutput staat in `target/`, zodat er een duidelijke scheiding is tussen broncode, testdata en gegenereerde artifacts;
- wijzigingen aan testdata worden alleen via tests gevalideerd en niet handmatig in productieruns geladen.

Praktisch betekent dit dat een ontwikkelaar testdata alleen gebruikt in een testomgeving of lokale ontwikkelomgeving. Productie-installaties krijgen alleen de gepackageerde module en de bijbehorende runtime-resources.

## Niet-herleidbare data tussen OTAP-omgevingen

NEN-7510 (controls A.8.31 en A.8.33) eist dat data uit hogere omgevingen — met name productie — niet herleidbaar naar lagere omgevingen mag stromen. In dit project is dat als volgt afgedwongen, expliciet per laag:

| Laag | Maatregel | Hoe afgedwongen |
|---|---|---|
| **Database** | Per omgeving een aparte database (`openmrs_dev`, `openmrs_test`, `openmrs_prod`) op een aparte Docker-volume | `docker-compose.{dev,test,prod}.yml` definieert telkens een eigen `MYSQL_DATABASE` en een eigen `*_data` volume — geen gedeelde volumes |
| **Secrets** | Per omgeving een eigen `DB_PASSWORD`, beheerd via GitHub Environments (`test` en `production`) | GitHub Environment scoping: `production`-secrets zijn alleen leesbaar door jobs met `environment: production` in `deploy.yml`. De test-job kan productie-secrets niet uitlezen — ook niet bij een fout in de workflow |
| **Netwerk** | Verschillende poorten (8082 dev / 8081 test / 80 prod) en gescheiden Docker-netwerken | Geen container in `docker-compose.dev.yml` kan praten met de productie-database, want die draait in een andere Compose-stack |
| **Data-inhoud** | Geen productiedata in dev/test — alle datasets zijn synthetisch of geanonimiseerd | Test-resources (`api/src/test/resources/`) bevatten alleen `*TestDataset.xml`-bestanden met fictieve patiënten. Productie-installaties laden alleen `src/main`-resources |
| **Identifiers** | Patiënt-UUID's en interne ID's uit productie worden niet hergebruikt in test | Maven scope `src/test` is een aparte classpath. Geen build-script kopieert productie-dumps naar lagere omgevingen |
| **Logs** | Logs uit productie worden niet doorgesluisd naar dev/test | Per Docker Compose stack een eigen log-volume; geen syslog-forwarding tussen omgevingen |

**Pseudonimisering is geen waarborg in dit project** — we kiezen bewust voor *volledig synthetische data* in dev/test, omdat correlatie van velden (geboortedatum + postcode + afspraaktype) een patiënt opnieuw herleidbaar kan maken (zie WS02 slide "8.33 Testdata"). Generatie van testdata gebeurt via fixtures in `src/test/resources` — niet via dumps uit productie.

**Restrisico:** een ontwikkelaar met admin-toegang tot zowel productie als de repo zou theoretisch productiedata kunnen exporteren en handmatig in een testdataset plakken. Mitigatie: branch protection + verplichte code review op alle wijzigingen in `src/test/resources` zodat zo'n diff opvalt in PR-review.

## Hoe een nieuwe ontwikkelaar met de omgeving werkt

### Lokale ontwikkelomgeving opstarten

1. Clone de repository.
2. Kopieer `.env.example` naar `.env.dev` en vul een lokaal wachtwoord in:
   ```bash
   cp .env.example .env.dev
   # bewerk .env.dev: zet DB_PASSWORD=<lokaal_wachtwoord>
   ```
3. Start de ontwikkelomgeving:
   ```bash
   export $(cat .env.dev | xargs)
   docker compose -f docker-compose.dev.yml up -d
   ```
   OpenMRS is daarna bereikbaar op `http://localhost:8082`.

### Module bouwen en testen

4. Zorg dat je lokale toolchain overeenkomt met de module:
   - Java 8 (de module compileert met source/target 1.6, Java 8 is compatibel)
   - Maven 3.x
5. Bouw de module en voer alle tests uit:
   ```bash
   cd openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling
   mvn verify
   ```
   Dit genereert de module-artifacts in `omod/target/` én draait alle unit-tests.

### Werken met de testomgeving

6. Start de testomgeving met het DB_PASSWORD uit `.env.test`:
   ```bash
   export $(cat .env.test | xargs)
   docker compose -f docker-compose.test.yml up -d
   ```
   Testomgeving bereikbaar op `http://localhost:8081`.

### Vuistregels

- Wijzig productcode in `src/main`, wijzig testscenario's in `src/test`.
- Commit **nooit** een `.env`-bestand — deze staan in `.gitignore`.
- Draai `mvn verify` voordat je een pull request aanmaakt; de CI doet hetzelfde.

## GitHub Environments en protection rules

De repository gebruikt twee GitHub Environments:

| Environment | Doel | Protection rule |
|---|---|---|
| `test` | Validatie na een merge naar `main` | Automatische deployment, geen goedkeuring vereist |
| `production` | Productie-deployment (vereist een self-hosted runner of externe deploy-target in productie) | Handmatige goedkeuring van minimaal één reviewer vereist |

Secrets worden per environment beheerd via **GitHub → Settings → Environments**. De `test`-secrets zijn alleen beschikbaar in de testomgeving en de `production`-secrets alleen in de productieomgeving. Zo is het onmogelijk dat een testwaarde in productie terechtkomt via de pipeline.

Branch protection op `main`:
- Directe pushes naar `main` zijn geblokkeerd.
- Een pull request en minimaal één goedkeuring zijn vereist.
- De CI-checks (`ci`, `CodeQL`, `Dependency Review`) moeten slagen voordat een PR gemerged mag worden.

## Extra documentatie

- [Modulekeuze](docs/module-keuze.md)
- [Gap-analyse](docs/auditreport/01-gap-analyse.md)
- [Pipeline-compliance](docs/auditreport/02-pipeline-compliance.md)
- [Update-advies dependencies](docs/auditreport/06-update-advies.md)
- [Non-functional requirements](docs/non-functional-requirements.md)
- [Module README](openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/README.md)
