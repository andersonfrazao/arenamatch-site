package br.com.arenamatch.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.DashboardDTO;

@Component
public class DashboardClient {

    private final RestClient restClient;

    public DashboardClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public DashboardDTO carregarDadosDashboard(Long idUsuario) {
        return restClient.get()
                .uri("/api/dashboard/{idUsuario}", idUsuario)
                .retrieve()
                .body(DashboardDTO.class);
    }
}