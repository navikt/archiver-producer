package no.nav.soknad.arkivering.soknadsmottaker.config

import no.nav.soknad.arkivering.soknadsmottaker.interceptor.MdcInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class ResourceConfig(private val mdcInterceptor: MdcInterceptor) : WebMvcConfigurer {
	override fun addInterceptors(registry: InterceptorRegistry) {
		registry.addInterceptor(mdcInterceptor)
	}
}
