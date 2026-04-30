package br.com.arenamatch.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.FaleConoscoDTO;

@Component
public class EmailClient {

	private final RestClient restClient;

	public EmailClient(RestClient restClient) {
		this.restClient = restClient;
	}
	
	
	public void enviarEmailSuporte(String email, String assunto, String textoEmail, String from) {
		FaleConoscoDTO dto = new FaleConoscoDTO();
		dto.setEmail(email);
		dto.setAssunto(assunto);
		dto.setTextoEmail(textoEmail);
		dto.setFrom(from);
	
	       restClient.post()
	       .uri("/api/email/suporte/enviar")
	       .body(dto)
	       .retrieve()
	       .toBodilessEntity();
	}
	
}
