package br.com.arenamatch.client;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ParametroSistemaClient {

    private final RestClient restClient;

    public ParametroSistemaClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void validarDataMinimaAgendamento(LocalDate dataJogo) {
        restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/parametros/validar-data-agendamento")
                        .queryParam("data", dataJogo.toString())
                        .build())
                .retrieve()
                .toBodilessEntity();
    }
}
