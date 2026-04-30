package br.com.arenamatch.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.CadastroDTO;

@Component
public class CadastroClient {
    private final RestClient restClient;

    public CadastroClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public boolean enviarCadastro(CadastroDTO dto) {
        try {
            restClient.post().uri("/api/cadastro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public void salvarTime(CadastroDTO dto) {
        restClient.post()
                .uri("/api/cadastro") // Verifique se sua API usa esta URL
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }
    
    public CadastroDTO buscarDadosParaEdicao(Long idUsuario) {
        return restClient.get()
                .uri("/api/cadastro/" + idUsuario)
                .retrieve()
                .body(CadastroDTO.class);
    }

    public void atualizarConta(Long idUsuario, CadastroDTO dto) {
        restClient.put()
                .uri("/api/cadastro/" + idUsuario)
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }
}
