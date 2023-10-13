package no.nav.soknad.arkivering.soknadsmottaker.supervision

import no.nav.security.token.support.core.api.Unprotected
import no.nav.soknad.arkivering.soknadsmottaker.api.HealthApi
import no.nav.soknad.arkivering.soknadsmottaker.model.ApplicationStatus
import no.nav.soknad.arkivering.soknadsmottaker.model.ApplicationStatusType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class HealthCheck : HealthApi {

	@Unprotected
	override fun isAlive() = ResponseEntity<Unit>(HttpStatus.OK)

	@Unprotected
	override fun ping() = ResponseEntity<Unit>(HttpStatus.OK)

	@Unprotected
	override fun isReady() = ResponseEntity<Unit>(HttpStatus.OK)

	@Unprotected
	override fun getStatus(): ResponseEntity<ApplicationStatus> {
		return ResponseEntity(
			ApplicationStatus(status = ApplicationStatusType.OK, description = "OK"),
			HttpStatus.OK
		)
	}
}
