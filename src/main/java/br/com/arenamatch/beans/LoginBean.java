package br.com.arenamatch.beans;

import br.com.arenamatch.client.AuthClient;
import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.dto.UsuarioDTO;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

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
            UsuarioDTO usuario = authClient.login(loginDTO);

            if (usuario != null) {
                sessaoBean.setUsuarioLogado(usuario);
                return "/minha-agenda?faces-redirect=true"; // Navegação
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Usuário ou senha inválidos."));
        }
        return null; // Fica na mesma tela
    }
}