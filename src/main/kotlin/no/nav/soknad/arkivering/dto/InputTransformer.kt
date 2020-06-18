package no.nav.soknad.arkivering.dto

class InputTransformer (private val input: SoknadInnsendtDto) {

	fun apply() : SoknadMottattDto = input.toSoknadMottattView()

	private fun SoknadInnsendtDto.toSoknadMottattView() = SoknadMottattDto(
		eksternReferanseId= innsendingsId,
		personId = personId,
		tema = tema,
		innsendtDato = innsendtDato,
		mottatteDokumenter = konverterTilMottatteDokumenterList(innsendteDokumenter)
	)

	private fun InnsendtDokumentDto.toMottattDokumentView() = MottattDokumentDto(
		skjemaNummer = skjemaNummer,
		erHovedSkjema =  erHovedSkjema,
		tittel = tittel,
		varianter =  konverterTilMotattVarianterListe(varianter)
	)

	private fun InnsendtVariantDto.toMottattVariantView() = MottattVariantDto(
		uuid = uuid,
		filNavn = filNavn,
		filtype = filtype,
		variantformat = variantformat
	)

	private fun konverterTilMotattVarianterListe(list: List<InnsendtVariantDto>) = list.map { it.toMottattVariantView() }

	private fun konverterTilMottatteDokumenterList(list: List<InnsendtDokumentDto>) =list.map { it.toMottattDokumentView() }

}