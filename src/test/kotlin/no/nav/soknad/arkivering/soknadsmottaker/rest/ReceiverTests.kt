package no.nav.soknad.arkivering.soknadsmottaker.rest

import com.nhaarman.mockitokotlin2.capture
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.dto.opprettBilInnsendingMedBareSoknadOgKvittering
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import no.nav.soknad.arkivering.soknadsmottaker.service.MESSAGE_ID
import no.nav.soknad.arkivering.soknadsmottaker.supervise.Metrics
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.kafka.core.KafkaTemplate

class ReceiverTests {

	private val topic = "privat-soknadInnsendt-v1-default"

	private val kafkaMock: KafkaTemplate<String, Soknadarkivschema> = mock()
	private val receiver = mockReceiver()

	@Test
	fun `When receiving REST call, message is put on Kafka`() {
		val errorsBefore = Metrics.mottattErrorGet("BIL")
		val sentInBefore = Metrics.mottattSoknadGet("BIL")
		val melding = opprettBilInnsendingMedBareSoknadOgKvittering()

		receiver.receiveMessage(melding)

		val captor = argumentCaptor<ProducerRecord<String, Soknadarkivschema>>()
		verify(kafkaMock, times(1)).send(capture(captor))
		assertEquals(topic, captor.value.topic(), "Should send to the right topic")
		assertEquals(1, captor.value.headers().headers(MESSAGE_ID).count(), "Should have a MESSAGE_ID header")
		assertEquals("BIL", captor.value.value().getArkivtema(), "Should have correct tema")
		assertEquals(errorsBefore + 0.0, Metrics.mottattErrorGet("BIL"), "Should not cause errors")
		assertEquals(sentInBefore + 1.0, Metrics.mottattSoknadGet("BIL"), "Should set counter to 1")
	}

	private fun mockReceiver(): Receiver {
		val kafkaSender = KafkaSender(kafkaMock)
		val orderService = ArchiverService(kafkaSender, AppConfiguration())
		return Receiver(orderService)
	}

	private inline fun <reified T : Any> mock() = mock(T::class.java)!!
	private inline fun <reified T> argumentCaptor(): ArgumentCaptor<T> = ArgumentCaptor.forClass(T::class.java)
}
