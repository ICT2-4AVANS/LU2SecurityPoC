# LU2SecurityPoC

Deze repository bevat een PoC-omgeving rond de OpenMRS Appointment Scheduling Module. De opzet is bedoeld om onderhoudbaarheid en security in een gecontroleerde lokale omgeving te onderzoeken, zonder testgegevens of experimentele wijzigingen naar productie door te schuiven.

## Hoe de omgevingen zijn ingericht

### OTAP — Docker-omgevingen

De repository bevat drie aparte Docker Compose-bestanden, één per OTAP-laag:

| Omgeving | Bestand | Poort | Database |
|---|---|---|---|
| Ontwikkeling | `docker-compose.dev.yml` | 8080 | `openmrs_dev` |
| Test | `docker-compose.test.yml` | 8081 | `openmrs_test` |
| Productie | `docker-compose.prod.yml` | 80 | `openmrs_prod` |

Elke omgeving heeft eigen configuratie: aparte poorten, aparte databasenamen en aparte secrets. Productie gebruikt een omgevingsvariabele (`${DB_PASSWORD}`) die nooit in de repository staat. Test gebruikt een vaste testwachtwoord dat alleen bedoeld is voor geautomatiseerde pipelines.

**Omgeving starten:**

```bash
# Ontwikkeling
docker compose -f docker-compose.dev.yml up

# Test
docker compose -f docker-compose.test.yml up

# Productie (vereist DB_PASSWORD als omgevingsvariabele)
DB_PASSWORD=<geheim> docker compose -f docker-compose.prod.yml up
```

### GitHub Environments

In GitHub zijn twee Environments geconfigureerd:

- **test** — automatisch gedeployed bij merge naar `main`
- **production** — vereist handmatige goedkeuring (required reviewers) vóór deployment

### Codestructuur

De repository is opgesplitst in een documentatielaag en de module zelf:

- `docs/` bevat de onderbouwing van de modulekeuze en andere projectdocumentatie.
- `openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/` bevat de broncode van de OpenMRS-module.
- Binnen de module is een expliciet onderscheid gemaakt tussen productcode en testcode:
  - `api/src/main/` en `omod/src/main/` bevatten de code en resources die onderdeel zijn van de module.
  - `api/src/test/` en `omod/src/test/` bevatten alleen testcode en testdata.

De module wordt met Maven gebouwd. De belangrijkste output staat in `omod/target/` en `api/target/`. Die mappen zijn build-artifacts en vormen geen bron van waarheid voor de codebase.

## Hoe wordt voorkomen dat testdata in productie terechtkomt

Er zijn twee lagen van scheiding:

**Laag 1 — Docker-omgevingen**

Elke OTAP-laag heeft een eigen database (`openmrs_dev`, `openmrs_test`, `openmrs_prod`). De productieomgeving gebruikt een apart wachtwoord via een omgevingsvariabele die nooit in de repository staat. Het is technisch onmogelijk om de testdatabase te benaderen vanuit de productiecontainer.

**Laag 2 — Maven testresources**

Testdata staat alleen in testresources:

- `api/src/test/resources/`
- `omod/src/test/resources/`

Maven behandelt `src/test` strikt apart van `src/main`. Testresources (herkenbaar aan namen als `*TestDataset.xml`) worden nooit meegenomen in een productie-artifact. Productiecode leest uitsluitend uit `src/main`-resources.

## Hoe een nieuwe ontwikkelaar met de omgeving werkt

### Vereisten

- Docker Desktop geïnstalleerd
- Git
- (optioneel voor lokaal bouwen) Maven + Java 8

### Stappen

1. Clone de repository:

   ```bash
   git clone https://github.com/ICT2-4AVANS/LU2SecurityPoC.git
   cd LU2SecurityPoC
   ```

2. Start de ontwikkelomgeving met Docker:

   ```bash
   docker compose -f docker-compose.dev.yml up
   ```

   OpenMRS is daarna bereikbaar op `http://localhost:8080/openmrs`.

3. Werk in de modulemap `openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/`.

4. Bouw en test de module lokaal (optioneel, vereist Maven + Java 8):

   ```bash
   cd openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling
   mvn package
   ```

5. Maak een nieuwe branch aan voor elke wijziging en open een pull request naar `main`. De CodeQL-analyse en Dependency Review draaien automatisch als check op de PR.

6. Gebruik nooit de productie-omgeving voor ontwikkel- of testwerk. Productie vereist een apart `DB_PASSWORD` dat alleen bekend is bij de beheerder.

**Vuistregel:** wijzig productcode in `src/main`, testscenario's in `src/test`, gebruik `docker-compose.dev.yml` voor lokale ontwikkeling en nooit `docker-compose.prod.yml`.

## Extra documentatie

- [Modulekeuze](docs/module-keuze.md)
- [Kwaliteitseisen](docs/kwaliteitseisen.md)
- [Gap-analyse (NEN-7510)](docs/auditreport/01-gap-analyse.md)
- [Pipeline Compliance](docs/auditreport/02-pipeline-compliance.md)
- [Module README](openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/README.md)
