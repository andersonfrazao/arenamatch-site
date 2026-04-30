package br.com.arenamatch.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.FiltroBuscaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;

@Component
public class BuscaClient {
    private final RestClient restClient;

    public BuscaClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<TimeResumoDTO> buscarTimes(FiltroBuscaDTO filtro) {
        return restClient.post().uri("/api/busca")
                .contentType(MediaType.APPLICATION_JSON)
                .body(filtro)
                .retrieve()
                .body(new ParameterizedTypeReference<List<TimeResumoDTO>>() {});
    }
    
    
    public List<TimeResumoDTO> filtrarTimes(FiltroBuscaDTO filtro, Long idMeuTime) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/busca/times") // URL do BuscaController
                        .queryParam("data", filtro.getDataJogo().toString()) // Envia yyyy-MM-dd
                        .queryParam("raio", filtro.getRaioKm())
                        .queryParam("cidade", filtro.getCidade())
                        .queryParam("idMeuTime", idMeuTime)
                        .queryParam("categoria", filtro.getCategoria())// Importante para o cálculo de distância
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<TimeResumoDTO>>() {});
    }
}