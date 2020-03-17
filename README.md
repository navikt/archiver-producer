Soknadsmottaker
================
Applikasjonen tilbyr tjeneste for å metadata om søknader til innsendingsapplikasjoner.
Oppretter så meldinger på egen strøm i KAFKA for mottakere av metadata slik at de kan agere på dataen.

This application provides a REST endpoint to which data can be sent. The data will be converted, serialized as an Avro message and put on a Kafka topic.
For a description of the whole archiving system, see [the documentation](https://github.com/navikt/archiving-infrastructure/wiki).

# Dependencies
This component requires the following to work:
* [soknadarkiv-schema](https://github.com/navikt/soknadarkiv-schema) (Avro schema definitions)
* Kafka broker (for providing a Kafka topic to send to)

# Setup
The Kafka topic transports Avro serialized events defined in [soknadarkiv-schema](https://github.com/navikt/soknadarkiv-schema). The Avro schema definitions are made available publicly on the GitHub Package Registry.

Unfortunately, [GitHub doesn't currently allow accessing its package registry without authorization](https://github.community/t5/GitHub-API-Development-and/Download-from-Github-Package-Registry-without-authentication/m-p/35501#M3312).
Until that has been fixed, one must have a [Personal Github access token](https://github.com/settings/tokens) with SSO enabled and `read:packages=true`.

## Building from command line
* In order to build like normal, add the relevant parts of ./.m2/maven-settings.xml to your ~/.m2/settings.xml file and set the environment variables
`GITHUB_USERNAME` and `GITHUB_TOKEN` (e.g. in ~/.bashrc). Then it is possible to build with `mvn clean install` like normal.

* In order to build just this component without changing ~/.m2/settings.xml, one can run the following instead of the step above:<br />
`export GITHUB_USERNAME=<INSERT_GITHUB_USERNAME_HERE> && export GITHUB_TOKEN=<INSERT_GITHUB_ACCESS_TOKEN_HERE> && mvn --settings ./.m2/maven-settings.xml clean install`

## Building from IntelliJ
* Settings -> Build, Execution, Deployment -> Build Tools -> Maven -> Runner<br />
Add this to the Environment Variables:<br />
`GITHUB_TOKEN=<INSERT_GITHUB_ACCESS_TOKEN_HERE>;GITHUB_USERNAME=<INSERT_GITHUB_USERNAME_HERE>`

* If you have not merged ./m2/maven-settings.xml to ~/.m2/settings.xml as per above, you also need to do the following:<br />
Settings -> Build, Execution, Deployment -> Build Tools -> Maven<br />
Override User Settings File and specify ./m2/maven-settings.xml

## Inquiries
Questions regarding the code or the project can be asked to [team-soknad@nav.no](mailto:team-soknad@nav.no)

### For NAV employees
NAV employees can reach the team by Slack in the channel #teamsoknad
