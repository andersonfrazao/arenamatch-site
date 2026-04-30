package br.com.arenamatch.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.ConviteLigaDTO;
import br.com.arenamatch.dto.EnviarConviteLigaDTO;
import br.com.arenamatch.dto.LigaExplorarDTO;
import br.com.arenamatch.dto.NovaLigaDTO;
import br.com.arenamatch.dto.ResponderConviteLigaDTO;

@Service
public class LigaClient {

    @Autowired
    private RestClient restClient;

    public void criarLiga(Long idTimeAdmin, String nome, String descricao) {
        NovaLigaDTO dto = new NovaLigaDTO();
        dto.setIdTimeAdmin(idTimeAdmin);
        dto.setNome(nome);
        dto.setDescricao(descricao);

        restClient.post()
                .uri("/api/ligas")
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }

    public List<br.com.arenamatch.dto.LigaDetalheDTO> buscarLigasDoTime(Long timeId) {
        br.com.arenamatch.dto.LigaDetalheDTO[] ligas = restClient.get()
                .uri("/api/ligas/time/" + timeId)
                .retrieve()
                .body(br.com.arenamatch.dto.LigaDetalheDTO[].class);
        return ligas != null ? Arrays.asList(ligas) : new ArrayList<>();
    }

    public List<ConviteLigaDTO> buscarConvitesPendentes(Long timeId) {
        ConviteLigaDTO[] convites = restClient.get()
                .uri("/api/ligas/convites/time/" + timeId)
                .retrieve()
                .body(ConviteLigaDTO[].class);
        return convites != null ? Arrays.asList(convites) : new ArrayList<>();
    }

    public void responderConvite(Long idConvite, boolean aceitar) {
        ResponderConviteLigaDTO dto = new ResponderConviteLigaDTO();
        dto.setIdConvite(idConvite);
        dto.setAceitar(aceitar);

        restClient.post()
                .uri("/api/ligas/convites/responder")
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }
    
 // Atualize o retorno para LigaDetalheDTO
    public br.com.arenamatch.dto.LigaDetalheDTO buscarLigaPorId(Long id) {
        return restClient.get()
                .uri("/api/ligas/" + id)
                .retrieve()
                .body(br.com.arenamatch.dto.LigaDetalheDTO.class);
    }

    // Atualize o retorno para List<TimeSimplesDTO>
    public List<br.com.arenamatch.dto.TimeSimplesDTO> buscarTimesPorNome(String nome) {
        br.com.arenamatch.dto.TimeSimplesDTO[] times = restClient.get()
                .uri("/api/times/buscar-por-nome?nome=" + nome) 
                .retrieve()
                .body(br.com.arenamatch.dto.TimeSimplesDTO[].class);
                
        // AGORA SIM! Criamos um ArrayList de verdade e mutável:
        return times != null ? new ArrayList<>(Arrays.asList(times)) : new ArrayList<>();
    }
    
 // --- MÉTODO PARA ENVIAR CONVITE (Ponto 3) ---
    public void enviarConvite(Long idLiga, Long idTimeConvidado, String mensagem) {
        EnviarConviteLigaDTO dto = new EnviarConviteLigaDTO();
        dto.setIdLiga(idLiga);
        dto.setIdTimeConvidado(idTimeConvidado);
        dto.setMensagem(mensagem);

        restClient.post()
                .uri("/api/ligas/convites")
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }

    // --- MÉTODO PARA REMOVER MEMBRO (Ponto 2) ---
    public void removerMembro(Long idLiga, Long idTime) {
        restClient.delete()
                .uri("/api/ligas/" + idLiga + "/membros/" + idTime)
                .retrieve()
                .toBodilessEntity();
    }
    
    public List<ConviteLigaDTO> buscarConvitesParaAgenda(Long timeId) {
        ConviteLigaDTO[] convites = restClient.get()
                .uri("/api/ligas/convites/agenda/time/" + timeId)
                .retrieve()
                .body(ConviteLigaDTO[].class);
        return convites != null ? new ArrayList<>(Arrays.asList(convites)) : new ArrayList<>();
    }
    
 // --- NOVO: CONSUMIR OS IDS COM CONVITE PENDENTE ---
    public List<Long> buscarIdsTimesComConvitePendente(Long ligaId) {
        Long[] ids = restClient.get()
                .uri("/api/ligas/" + ligaId + "/convites/pendentes/times")
                .retrieve()
                .body(Long[].class);
        return ids != null ? new ArrayList<>(Arrays.asList(ids)) : new ArrayList<>();
    }
    
    public List<LigaExplorarDTO> listarLigasEmAlta(Long meuTimeId) {
        LigaExplorarDTO[] array = restClient.get()
                .uri("/api/ligas/explorar/top/" + meuTimeId)
                .retrieve()
                .body(LigaExplorarDTO[].class);
        return array != null ? Arrays.asList(array) : List.of();
    }

    public List<LigaExplorarDTO> buscarLigasPorNome(String nomeBusca, Long meuTimeId) {
        LigaExplorarDTO[] array = restClient.get()
                .uri("/api/ligas/explorar/busca/" + nomeBusca + "/" + meuTimeId)
                .retrieve()
                .body(LigaExplorarDTO[].class);
        return array != null ? Arrays.asList(array) : List.of();
    }

    public void solicitarEntradaNaLiga(Long idLiga, Long meuTimeId) {
        restClient.post()
                .uri("/api/ligas/" + idLiga + "/solicitar-entrada/" + meuTimeId)
                .retrieve()
                .toBodilessEntity();
    }
    
    
}