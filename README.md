# LU2SecurityPoC

Deze repository bevat een PoC-omgeving rond de OpenMRS Appointment Scheduling Module. De opzet is bedoeld om onderhoudbaarheid en security in een gecontroleerde lokale omgeving te onderzoeken, zonder testgegevens of experimentele wijzigingen naar productie door te schuiven.

## Hoe de omgevingen zijn ingericht

De repository is opgesplitst in een documentatielaag en de module zelf:

- `docs/` bevat de onderbouwing van de modulekeuze en andere projectdocumentatie.
- `openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/` bevat de broncode van de OpenMRS-module.
- Binnen de module is een expliciet onderscheid gemaakt tussen productcode en testcode:
  - `api/src/main/` en `omod/src/main/` bevatten de code en resources die onderdeel zijn van de module.
  - `api/src/test/` en `omod/src/test/` bevatten alleen testcode en testdata.

De module wordt met Maven gebouwd. De belangrijkste output staat in `omod/target/` en `api/target/`. Die mappen zijn build-artifacts en vormen geen bron van waarheid voor de codebase.

Voor ontwikkeling is de omgeving bedoeld als lokale werkruimte op een eigen machine of in een aparte ontwikkelomgeving. De moduleversie in deze repository is gekoppeld aan OpenMRS 1.9.9 en aan oudere Java- en Maven-compatibiliteit, dus een ontwikkelaar moet de lokale toolchain daarop afstemmen.

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

## Hoe een nieuwe ontwikkelaar met de omgeving werkt

1. Clone de repository en open de workspace in VS Code.
2. Werk in de modulemap `openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/`.
3. Controleer of je lokale toolchain aansluit op de module:
   - Maven voor build en test;
   - een OpenMRS-compatibele Java-versie passend bij deze legacy module.
4. Bouw de module met:

   ```bash
   mvn package
   ```

   Dit genereert de module-artifacts in `omod/target/`.

5. Draai tests voordat je wijzigingen doorvoert naar een gedeelde omgeving. De testdatasets in `api/src/test/resources/` en `omod/src/test/resources/` worden automatisch door de tests gebruikt.
6. Pas alleen broncode aan in `api/src/main/` of `omod/src/main/` tenzij je bewust testscenario's of testdata wilt uitbreiden.
7. Gebruik testdata uitsluitend om gedrag te valideren in de lokale of testomgeving; zet geen testdatasets handmatig om naar productie.

Voor een nieuwe ontwikkelaar is de belangrijkste vuistregel: wijzig productcode in `src/main`, wijzig testscenario's in `src/test`, en vertrouw op Maven om beide strikt te scheiden.

## Extra documentatie

- [Modulekeuze](docs/module-keuze.md)
- [Module README](openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/README.md)
