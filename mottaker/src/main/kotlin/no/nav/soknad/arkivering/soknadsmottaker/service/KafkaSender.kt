package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.config.KafkaConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class KafkaSender(
	private val kafkaConfig: KafkaConfig,
	private val kafkaTemplate: KafkaTemplate<String, Soknadarkivschema>,
	private val metricKafkaTemplate: KafkaTemplate<String, InnsendingMetrics>,
	private val kafkaBeskjedTemplate: KafkaTemplate<String, String>,
	private val kafkaOppgaveTemplate: KafkaTemplate<String, String>,
	private val kafkaDoneTemplate: KafkaTemplate<String, String>,
	private val kafkaUtkastTemplate: KafkaTemplate<String, String>
) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun publishSoknadarkivschema(key: String, value: Soknadarkivschema) {
		val topic = kafkaConfig.mainTopic
		publish(topic, key, value, kafkaTemplate)
		logger.info("$key: Published to $topic")
	}

	fun publishMetric(key: String, value: InnsendingMetrics) {
		val topic = kafkaConfig.metricsTopic
		publish(topic, key, value, metricKafkaTemplate)
		logger.info("$key: Published to $topic")
	}

	fun publishDoneNotification(key: String, value: String) {
		val topic = kafkaConfig.brukernotifikasjonDoneTopic
		publishBrukernotifikasjon(topic, key, value, kafkaDoneTemplate)
	}

	fun publishUtkastNotification(key: String, value: String) {
		val topic = kafkaConfig.utkastTopic
		logger.info("$key: shall publish Utkast to topic $topic")
		publish(topic, key, value, kafkaUtkastTemplate)
		logger.info("$key: published to topic $topic")
	}

	fun publishBeskjedNotification(key: String, value: String) {
		val topic = kafkaConfig.brukernotifikasjonBeskjedTopic
		publishBrukernotifikasjon(topic, key, value, kafkaBeskjedTemplate)
	}

	fun publishOppgaveNotification(key: String, value: String) {
		val topic = kafkaConfig.brukernotifikasjonOppgaveTopic
		publishBrukernotifikasjon(topic, key, value, kafkaOppgaveTemplate)
	}

	private fun publishBrukernotifikasjon(topic: String, key: String, value: String, kafkaTemplate: KafkaTemplate<String,String>) {
		logger.info("${key}: Shall publish notification to topic $topic")
		publish(topic, key, value, kafkaTemplate)
		logger.info("${key}: Published to $topic")
	}

	private fun <T> publishBrukernotifikasjon(topic: String, key: NokkelInput, value: T, kafkaTemplate: KafkaTemplate<NokkelInput, T>) {
		logger.info("${key.grupperingsId}: Skal publisere notifikasjon med eventId=${key.eventId} på topic $topic")
		publish(topic, key, value, kafkaTemplate)
		logger.info("${key.grupperingsId}: Published to $topic")
	}

	private fun <K, V> publish(topic: String, key: K, value: V, kafkaTemplate: KafkaTemplate<K, V>) {
		val producerRecord = ProducerRecord(topic, key, value)
		val headers = RecordHeaders()
		headers.add(MESSAGE_ID, UUID.randomUUID().toString().toByteArray())
		headers.forEach { h -> producerRecord.headers().add(h) }

		val future = kafkaTemplate.send(producerRecord)
		future.get(10, TimeUnit.SECONDS)
	}
}


const val MESSAGE_ID = "MESSAGE_ID"
