/c/Users/R156312/java/jdk11/bin/java -jar swagger-cli.jar generate -i api-klient/swagger.json --api-package no.nav.soknad.arkivering.soknadsmottaker.klient --model-package no.nav.soknad.arkivering.soknadsmottaker.dto --invoker-package no.nav.soknad.arkivering.soknadsmottaker --group-id no.nav.soknad.arkivering --artifact-id soknadsmottaker --artifact-version 0.0.1-SNAPSHOT -l kotlin --library resttemplate -o src/gen/kotlin/