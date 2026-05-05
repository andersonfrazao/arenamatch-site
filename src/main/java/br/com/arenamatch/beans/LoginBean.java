package br.com.arenamatch.beans;

import br.com.arenamatch.client.AuthClient;
import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.dto.LoginResponseDTO;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientResponseException;

@Named
@ViewScoped
public class LoginBean implements Serializable {

    @Getter @Setter
    private String email;

    @Getter @Setter
    private String senha;

    @Getter @Setter
    private String codigoAtivacao;

    @Getter
    private boolean mostrarCodigoAtivacao;

    @Value("${arenamatch.validation.email-activation-enabled:true}")
    private boolean ativacaoEmailHabilitada;

    @Inject
    private AuthClient authClient; // Injeção do Feign Client

    @Inject
    private SessaoBean sessaoBean;

    public String logar() {
        try {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setEmail(email);
            loginDTO.setSenha(senha);
            loginDTO.setCodigoAtivacao(codigoAtivacao);

            // Chama a API via Client
            LoginResponseDTO loginResponse = authClient.login(loginDTO);

            if (loginResponse != null && loginResponse.getUsuario() != null) {
                sessaoBean.setUsuarioLogado(loginResponse.getUsuario());
                sessaoBean.setTokenJwt(loginResponse.getToken());
                return "/minha-agenda?faces-redirect=true"; // Navegação
            }
        } catch (RestClientResponseException e) {
            String mensagem = e.getResponseBodyAsString();
            if (mensagem == null || mensagem.trim().isEmpty()) {
                mensagem = "Usuario ou senha invalidos.";
            }
            if (ativacaoEmailHabilitada && mensagem.toLowerCase().contains("codigo de ativacao")) {
                mostrarCodigoAtivacao = true;
            }
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", mensagem));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Usuário ou senha inválidos."));
        }
        return null; // Fica na mesma tela
    }

    public void reenviarCodigoAtivacao() {
        try {
            authClient.reenviarCodigoAtivacao(email);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Codigo de ativacao reenviado."));
        } catch (RestClientResponseException e) {
            String mensagem = e.getResponseBodyAsString();
            if (mensagem == null || mensagem.trim().isEmpty()) {
                mensagem = "Nao foi possivel reenviar o codigo.";
            }
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", mensagem));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Nao foi possivel reenviar o codigo."));
        }
    }
}
