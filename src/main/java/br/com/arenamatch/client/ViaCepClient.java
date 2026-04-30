package br.com.arenamatch.client;

import br.com.arenamatch.dto.EnderecoDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ViaCepClient {

    private final RestClient restClient;

    public ViaCepClient() {
        // Criamos um client específico para o ViaCEP
        this.restClient = RestClient.create("https://viacep.com.br/ws/");
    }

    public EnderecoDTO buscarEndereco(String cep) {
        try {
            return restClient.get()
                    .uri(cep + "/json/")
                    .retrieve()
                    .body(EnderecoDTO.class);
        } catch (Exception e) {
            return null; // Cep inválido ou erro de conexão
        }
    }

}