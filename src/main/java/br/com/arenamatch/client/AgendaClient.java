package br.com.arenamatch.client;

import java.time.LocalDate;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.CancelamentoDTO;
import br.com.arenamatch.dto.EventoAgendaDTO;
import br.com.arenamatch.dto.PartidaDTO;
import br.com.arenamatch.dto.RespostaCancelamentoDTO;
import br.com.arenamatch.dto.ResumoAgendaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;

@Component
public class AgendaClient {

    private final RestClient restClient;

    public AgendaClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public TimeResumoDTO buscarMeuTime(Long idUsuario) {
        return restClient.get()
                .uri("/api/agenda/time/usuario/{idUsuario}", idUsuario)
                .retrieve()
                .body(TimeResumoDTO.class);
    }

    public List<PartidaDTO> listarProximosJogos(Long idTime) {
        return restClient.get()
                .uri("/api/agenda/partidas/{idTime}", idTime)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PartidaDTO>>() {});
    }

    public void solicitarCancelamento(CancelamentoDTO dto) {
        restClient.post()
                .uri("/api/agenda/cancelar")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }

    public void responderCancelamento(RespostaCancelamentoDTO dto) {
        restClient.post()
                .uri("/api/agenda/responder-cancelamento")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }
    
    public List<ResumoAgendaDTO> buscarCalendario(Long timeId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/agenda/calendario")
                        .queryParam("timeId", timeId)
                        .queryParam("dias", 15) // Vamos trazer 15 dias fixos
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ResumoAgendaDTO>>() {});
    }

    public List<EventoAgendaDTO> buscarDetalhesDia(Long timeId, LocalDate data) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/agenda/detalhes")
                        .queryParam("timeId", timeId)
                        .queryParam("data", data.toString())
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<EventoAgendaDTO>>() {});
    }
    
    public void excluirPartida(Long idPartida) {
        restClient.delete()
                .uri("/api/partidas/{id}", idPartida) // Ajuste o path conforme seu Controller
                .retrieve()
                .toBodilessEntity();
    }
    
    public void aceitarDesafio(Long idPartida) {
        restClient.put()
                .uri("/api/partidas/{id}/aceitar", idPartida) // Verifique se o caminho base é /api/partidas
                .retrieve()
                .toBodilessEntity();
    }
    
    public void solicitarCancelamento(Long idPartida, Long idMeuTime, String motivo) {
        // Envia o DTO de cancelamento para o backend
        CancelamentoDTO dto = new CancelamentoDTO();
        dto.setIdPartida(idPartida);
        dto.setIdTime(idMeuTime);
        dto.setMotivo(motivo);
        
        restClient.post()
                .uri("/api/partidas/solicitar-cancelamento")
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }

    public void responderCancelamento(Long idPartida, Long idMeuTime, boolean aceitar) {
        RespostaCancelamentoDTO dto = new RespostaCancelamentoDTO();
        dto.setIdPartida(idPartida);
        dto.setIdTime(idMeuTime);
        dto.setAceitar(aceitar);

        restClient.post()
                .uri("/api/partidas/responder-cancelamento")
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }
    
    public List<ResumoAgendaDTO> buscarCalendario(Long timeId, LocalDate dataBase) {
        
        // Formata a URL. Se dataBase for nulo, manda sem o parâmetro para o backend assumir o "Hoje"
        String uri = "/api/agenda/calendario/" + timeId;
        if (dataBase != null) {
            uri += "?dataBase=" + dataBase.toString();
        }

        ResumoAgendaDTO[] dias = restClient.get()
                .uri(uri)
                .retrieve()
                .body(ResumoAgendaDTO[].class);
                
        return dias != null ? java.util.Arrays.asList(dias) : new java.util.ArrayList<>();
    }
}