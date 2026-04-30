package br.com.arenamatch.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.dto.PlacarRequestDTO;

@Component
public class PartidaClient {

    private final RestClient restClient;

    public PartidaClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void enviarDesafio(DesafioDTO dto) {
        restClient.post()
                .uri("/api/partidas/desafiar")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }
    
    public void cancelarConvitePorId(Long idPartida) {
        restClient.delete()
                .uri("/api/partidas/" + idPartida)
                .retrieve()
                .toBodilessEntity(); // toBodilessEntity significa que não esperamos um JSON de volta (é um void)
    }

    public void cancelarConvitePorAdversario(Long meuTimeId, Long adversarioId) {
        restClient.delete()
                .uri("/api/partidas/cancelar/" + meuTimeId + "/" + adversarioId)
                .retrieve()
                .toBodilessEntity();
    }
    
    public void informarPlacar(Long idPartida, Integer golsM, Integer golsV, Long idTimeInformante) {
        restClient.post()
                .uri("/api/partidas/" + idPartida + "/placar")
                .body(new PlacarRequestDTO(golsM, golsV, idTimeInformante))
                .retrieve()
                .toBodilessEntity();
    }

    public void confirmarPlacar(Long idPartida) {
        restClient.post()
                .uri("/api/partidas/" + idPartida + "/confirmar-placar")
                .retrieve()
                .toBodilessEntity();
    }

    public void contestarPlacar(Long idPartida) {
        restClient.post()
                .uri("/api/partidas/" + idPartida + "/contestar-placar")
                .retrieve()
                .toBodilessEntity();
    }
}