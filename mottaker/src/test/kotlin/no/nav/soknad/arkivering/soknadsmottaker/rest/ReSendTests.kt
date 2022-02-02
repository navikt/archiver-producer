package no.nav.soknad.arkivering.soknadsmottaker.rest

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.prometheus.client.CollectorRegistry
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.dto.InputTransformer
import no.nav.soknad.arkivering.soknadsmottaker.dto.opprettSoknadUtenFilnavnSatt
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import no.nav.soknad.arkivering.soknadsmottaker.service.MESSAGE_ID
import no.nav.soknad.arkivering.soknadsmottaker.service.ReSender
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.SettableListenableFuture
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class ReSendTests {

	private val topic = "privat-soknadInnsendt-v1-teamsoknad"
	private val metricsTopic = "privat-soknadInnsendt-metrics-v1-teamsoknad"

	private val kafkaMock = mockk<KafkaTemplate<String, Soknadarkivschema>>()
	private val metricsKafkaMock = mockk<KafkaTemplate<String, InnsendingMetrics>>()

	private val metrics = InnsendtMetrics(CollectorRegistry(true))

	@Test
	fun `When ReSender starts applications is resent`() {
		val electorPath = getResource("/elector_path.json").toString()
		System.setProperty("ELECTOR_PATH", electorPath)
		System.setProperty("RESENDING_LIST", getBytesFromFile("/resend-applications.json"))

		val errorsBefore = metrics.mottattErrorGet("TSO")
		val sentInBefore = metrics.mottattSoknadGet("TSO")
		val melding = opprettSoknadUtenFilnavnSatt()
		val metricMessage = InnsendingMetrics()

		val record = slot<ProducerRecord<String, Soknadarkivschema>>()
		val metricRecord = slot<ProducerRecord<String, InnsendingMetrics>>()

		every { kafkaMock.send(capture(record)) } returns setFuture(makeSendResult(topic, InputTransformer(melding).apply()))
		every { metricsKafkaMock.send(capture(metricRecord)) } returns setFuture(makeSendResult(metricsTopic, metricMessage))

		val appConfiguration = AppConfiguration()
		startResender(metrics, appConfiguration)

		// Vent til resender har kjørt
		TimeUnit.SECONDS.sleep(2)

		assertTrue(record.isCaptured)
		assertEquals(topic, record.captured.topic(), "Should send to the right topic")
		assertEquals(1, record.captured.headers().headers(MESSAGE_ID).count(), "Should have a MESSAGE_ID header")
		assertEquals("TSO", record.captured.value().arkivtema, "Should have correct tema")
		assertEquals(errorsBefore!! + 0.0, metrics.mottattErrorGet("TSO"), "Should not cause errors")
		assertEquals(sentInBefore!! + 1.0, metrics.mottattSoknadGet("TSO"), "Should increase counter by 1")

		assertEquals(metricsTopic, metricRecord.captured.topic(), "Should send metrics to the right topic")
		assertEquals(1, metricRecord.captured.headers().headers(MESSAGE_ID).count(),
			"Metrics should have a MESSAGE_ID header")
		assertEquals("soknadsmottaker", metricRecord.captured.value().application,
			"Metrics should have correct application name")
		assertEquals("publish to kafka", metricRecord.captured.value().action,
			"Metrics should have correct action")
		assertTrue(metricRecord.captured.value().startTime <= System.currentTimeMillis(),
			"Metrics should have correct startTime")
		assertTrue(metricRecord.captured.value().duration <= metricRecord.captured.value().startTime,
			"Metrics should have a duration")

		metrics.unregister()
	}

	@Test
	fun `When not Leader, ReSender sends no applications`() {
		val electorPath = getResource("/not_leader.json").toString()
		System.setProperty("ELECTOR_PATH", electorPath)
		System.setProperty("RESENDING_LIST", getBytesFromFile("/resend-applications.json"))

		val melding = opprettSoknadUtenFilnavnSatt()
		val metricMessage = InnsendingMetrics()

		val record = slot<ProducerRecord<String, Soknadarkivschema>>()
		val metricRecord = slot<ProducerRecord<String, InnsendingMetrics>>()

		every { kafkaMock.send(capture(record)) } returns setFuture(
			makeSendResult(topic, InputTransformer(melding).apply())
		)
		every { metricsKafkaMock.send(capture(metricRecord)) } returns setFuture(
			makeSendResult(metricsTopic, metricMessage)
		)

		val appConfiguration = AppConfiguration()
		startResender(metrics, appConfiguration)

		// Vent til resender har kjørt
		TimeUnit.SECONDS.sleep(2)

		assertTrue(!record.isCaptured)

		metrics.unregister()
	}

		private fun <T> makeSendResult(topic: String, melding: T): SendResult<String, T> {
		return SendResult(
			ProducerRecord(topic, "123", melding),
			RecordMetadata(TopicPartition(topic, 1), 1L, 1, 1L, 1, 1)
		)
	}

	private fun <T> setFuture(v: SendResult<String, T>) =
		SettableListenableFuture<SendResult<String, T>>().also { it.set(v) }

	private fun startResender(metrics: InnsendtMetrics, appConfiguration: AppConfiguration): ReSender {
		val kafkaSender = KafkaSender(kafkaMock, metricsKafkaMock)
		val orderService = ArchiverService(kafkaSender, AppConfiguration(), metrics)
		return ReSender(orderService, appConfiguration)
	}


	private fun getResource(fileName: String) = this::class.java.getResource(fileName)!!

	@Suppress("SameParameterValue")
	private fun getBytesFromFile(path: String): String {
		val resourceAsStream = this::class.java.getResourceAsStream(path)
		val outputStream = ByteArrayOutputStream()
		resourceAsStream.use { input ->
			outputStream.use { output ->
				input!!.copyTo(output)
			}
		}
		return outputStream.toString(Charset.defaultCharset())
	}
}
