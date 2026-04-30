package br.com.arenamatch.client;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import br.com.arenamatch.dto.ConversaInboxDTO;
import br.com.arenamatch.dto.MensagemChatDTO;

@Service
public class ChatClient {

    @Autowired
    private RestClient restClient;

    public List<ConversaInboxDTO> listarInbox(Long meuTimeId) {
        ConversaInboxDTO[] array = restClient.get()
                .uri("/api/chat/inbox/" + meuTimeId)
                .retrieve()
                .body(ConversaInboxDTO[].class);
        return array != null ? Arrays.asList(array) : List.of();
    }

    public List<MensagemChatDTO> buscarHistorico(Long idPartida, Long meuTimeId) {
        MensagemChatDTO[] array = restClient.get()
                .uri("/api/chat/" + idPartida + "/historico/" + meuTimeId)
                .retrieve()
                .body(MensagemChatDTO[].class);
        return array != null ? Arrays.asList(array) : List.of();
    }

    public void enviarMensagem(Long idPartida, Long idRemetente, String texto) {
        restClient.post()
                .uri("/api/chat/" + idPartida + "/enviar/" + idRemetente)
                .body(texto)
                .retrieve()
                .toBodilessEntity();
    }
    
    public Long contarNaoLidasGeral(Long meuTimeId) {
        Long qtd = restClient.get()
                .uri("/api/chat/nao-lidas/" + meuTimeId)
                .retrieve()
                .body(Long.class);
        return qtd != null ? qtd : 0L;
    }

    public void marcarComoLidas(Long idPartida, Long meuTimeId) {
        restClient.put()
                .uri("/api/chat/" + idPartida + "/lidas/" + meuTimeId)
                .retrieve()
                .toBodilessEntity();
    }
    
    // ==========================================
    // MÉTODOS PARA LIGAS (No seu padrão)
    // ==========================================
    
    public List<MensagemChatDTO> buscarHistoricoLiga(Long idLiga, Long meuTimeId) {
        MensagemChatDTO[] array = restClient.get()
                .uri("/api/chat/liga/" + idLiga + "/historico/" + meuTimeId)
                .retrieve()
                .body(MensagemChatDTO[].class);
        return array != null ? Arrays.asList(array) : List.of();
    }

    public void enviarMensagemLiga(Long idLiga, Long idRemetente, String texto) {
        restClient.post()
                .uri("/api/chat/liga/" + idLiga + "/enviar/" + idRemetente)
                .body(texto)
                .retrieve()
                .toBodilessEntity();
    }

    public void marcarComoLidasLiga(Long idLiga, Long meuTimeId) {
        restClient.put()
                .uri("/api/chat/liga/" + idLiga + "/lidas/" + meuTimeId)
                .retrieve()
                .toBodilessEntity();
    }
}