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
import org.springframework.web.client.RestClientResponseException;

@Named
@ViewScoped
public class LoginBean implements Serializable {

    @Getter @Setter
    private String email;

    @Getter @Setter
    private String senha;

    @Inject
    private AuthClient authClient; // Injeção do Feign Client

    @Inject
    private SessaoBean sessaoBean;

    public String logar() {
        try {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setEmail(email);
            loginDTO.setSenha(senha);

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
            if (mensagem.toLowerCase().contains("pendente de ativacao")) {
                FacesContext.getCurrentInstance().getExternalContext().getFlash().put("emailAtivacao", email);
                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Ativacao pendente", mensagem));
                return "/ativar-conta.xhtml?faces-redirect=true";
            }
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", mensagem));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Usuário ou senha inválidos."));
        }
        return null; // Fica na mesma tela
    }

}
