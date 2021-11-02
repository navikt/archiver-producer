package no.nav.soknad.arkivering.soknadsmottaker.config

import com.natpryce.konfig.*
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import org.springframework.context.annotation.Bean
import java.io.File

private val defaultProperties = ConfigurationMap(
	mapOf(
		"KAFKA_TOPIC" to "privat-soknadInnsendt-v1-teamsoknad",
		"KAFKA_METRICS_TOPIC" to "privat-soknadInnsendt-metrics-v1-teamsoknad",
		"SCHEMA_REGISTRY_URL" to "http://localhost:8081",
		"KAFKA_BOOTSTRAP_SERVERS" to "localhost:29092",
		"SOKNADSMOTTAKER_USERNAME" to "kafkaproducer",
		"SOKNADSMOTTAKER_PASSWORD" to "",
		"KAFKA_SECURITY" to "",
		"KAFKA_SECPROT" to "",
		"KAFKA_SASLMEC" to "",

		"APPLICATION_PROFILE" to "",
		"REST_HENVENDELSE" to "avsender",
		"REST_PASSORD" to "password",

		"RESENDING_LIST" to "",
		"SECONDS_BEFORE_LEADER_CHECK" to "1"
	)
)

val appConfig =
	EnvironmentVariables() overriding
		systemProperties() overriding
		ConfigurationProperties.fromResource(Configuration::class.java, "/application.yml") overriding
		ConfigurationProperties.fromResource(Configuration::class.java, "/local.properties") overriding
		defaultProperties

private fun String.configProperty(): String = appConfig[Key(this, stringType)]

fun readFileAsText(fileName: String, default: String = "") = try { File(fileName).readText(Charsets.UTF_8) } catch (e: Exception ) { default }

data class AppConfiguration(val kafkaConfig: KafkaConfig = KafkaConfig(), val restConfig: RestConfig = RestConfig(), val reSendList: ReSendList = ReSendList()) {
	data class KafkaConfig(
		val profiles: String = "APPLICATION_PROFILE".configProperty(),
		val username: String = readFileAsText("/var/run/secrets/nais.io/serviceuser/username", "SOKNADSMOTTAKER_USERNAME".configProperty()),
		val password: String = readFileAsText("/var/run/secrets/nais.io/serviceuser/password", "SOKNADSMOTTAKER_PASSWORD".configProperty()),
		val servers: String = readFileAsText("/var/run/secrets/nais.io/kv/kafkaBootstrapServers", "KAFKA_BOOTSTRAP_SERVERS".configProperty()),
		val schemaRegistryUrl: String = "SCHEMA_REGISTRY_URL".configProperty(),
		val secure: String = "KAFKA_SECURITY".configProperty(),
		val protocol: String = "KAFKA_SECPROT".configProperty(), // SASL_PLAINTEXT | SASL_SSL
		val salsmec: String = "KAFKA_SASLMEC".configProperty(), // PLAIN
		val topic: String = "KAFKA_TOPIC".configProperty(),
		val metricsTopic: String = "KAFKA_METRICS_TOPIC".configProperty(),
		val saslJaasConfig: String = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";"
	)

	data class RestConfig(
		val user: String = readFileAsText("/var/run/secrets/nais.io/kv/restUser", "REST_HENVENDELSE".configProperty()),
		val password: String = readFileAsText("/var/run/secrets/nais.io/kv/restPassword", "REST_PASSORD".configProperty())
	)

	data class ReSendList(
		val applicationString: String = readFileAsText("/var/run/secrets/nais.io/resend/RESENDING_LIST", "RESENDING_LIST".configProperty()),
		val secondsAfterStartupBeforeStarting: Int = readFileAsText("/var/run/secrets/nais.io/resend/SECONDS_BEFORE_LEADER_CHECK", "SECONDS_BEFORE_LEADER_CHECK".configProperty()).toInt()
	)


	@org.springframework.context.annotation.Configuration
	class ConfigConfig {
		@Bean
		fun appConfiguration() = AppConfiguration()
	}
}
