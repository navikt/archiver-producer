package no.nav.soknad.arkivering.soknadsmottaker.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import no.nav.soknad.arkivering.soknadsmottaker.api.SoknadTestApi
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class TestApi : SoknadTestApi {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val secureLogger = LoggerFactory.getLogger("secureLogger")

	private val seenValues = hashMapOf<String, Soknad>()

	/**
	 * The following annotations are copied from [SoknadTestApi.receiveTest].
	 */
	@Operation(
		summary = "Test endpoint that does nothing",
		operationId = "receiveTest",
		description = "Endpoint used for testing that does nothing.")
	@ApiResponses(
		value = [ApiResponse(responseCode = "200", description = "Successful operation")])
	@RequestMapping(
		method = [RequestMethod.POST],
		value = ["/soknad-test"],
		consumes = ["application/json"]
	)
	override fun receiveTest(soknad: Soknad, xInnsendingId: String?): ResponseEntity<Unit> {
		val key = xInnsendingId ?: soknad.innsendingId
		log(key, soknad)

		updateOrCompareValues(xInnsendingId, key, soknad)

		return ResponseEntity(HttpStatus.OK)
	}

	@Synchronized
	private fun updateOrCompareValues(xInnsendingId: String?, key: String, soknad: Soknad) {
		if (xInnsendingId != null && xInnsendingId.startsWith("henvendelse_")) {
			val id = xInnsendingId.replace("henvendelse_", "")
			if (id in seenValues) {
				val oldSoknad = seenValues[id]
				if (soknad == oldSoknad) {
					logger.info("$key: Soknader are equal")
					seenValues.remove(id)
				} else {
					logger.warn("$key: Soknader differs!\nSS: $oldSoknad\nHV: $soknad")
				}
			} else {
				logger.warn("$xInnsendingId: Never seen Soknad before\n$soknad")
			}
		} else {
			seenValues[key] = soknad
		}
	}

	private fun log(key: String, soknad: Soknad) {
		val fnrMasked = Soknad(
			soknad.innsendingId,
			soknad.erEttersendelse,
			personId = "**fnr can be found in secure logs**",
			soknad.tema,
			soknad.dokumenter
		)
		logger.info("$key: TEST ENDPOINT - Received request '$fnrMasked'")
		secureLogger.info("$key: TEST ENDPOINT - Received request '$soknad'")
	}
}
