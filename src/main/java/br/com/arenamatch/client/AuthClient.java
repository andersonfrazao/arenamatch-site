package br.com.arenamatch.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.dto.LoginResponseDTO;
import br.com.arenamatch.dto.RedefinirSenhaDTO;

@Component
public class AuthClient {

    private final RestClient restClient;

    // O Spring injeta o restClient que configuramos no AppConfig (com a base url)
    public AuthClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public LoginResponseDTO login(LoginDTO loginDTO) {
        return restClient.post()
                .uri("/api/autenticacao/login") // <--- URL de negócio fixa aqui
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginDTO)
                .retrieve()
                .body(LoginResponseDTO.class);
    }
    
    public void solicitarCodigoRecuperacao(String email) {
        restClient.post()
                .uri("/api/autenticacao/recuperar-senha/solicitar")
                .body(email)
                .retrieve()
                .toBodilessEntity();
    }

    public void redefinirSenha(String email, String codigo, String novaSenha) {
        RedefinirSenhaDTO dto = new RedefinirSenhaDTO();
        dto.setEmail(email);
        dto.setCodigo(codigo);
        dto.setNovaSenha(novaSenha);

        restClient.post()
                .uri("/api/autenticacao/recuperar-senha/redefinir")
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }
}
