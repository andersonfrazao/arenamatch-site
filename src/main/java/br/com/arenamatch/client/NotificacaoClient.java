package br.com.arenamatch.client;

import br.com.arenamatch.dto.NotificacaoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class NotificacaoClient {

    @Autowired
    private RestClient restClient;

    public List<NotificacaoDTO> buscarNotificacoes(Long idTime) {
        NotificacaoDTO[] notificacoes = restClient.get()
                .uri("/api/notificacoes/time/" + idTime)
                .retrieve()
                .body(NotificacaoDTO[].class);
        return notificacoes != null ? new ArrayList<>(Arrays.asList(notificacoes)) : new ArrayList<>();
    }
}