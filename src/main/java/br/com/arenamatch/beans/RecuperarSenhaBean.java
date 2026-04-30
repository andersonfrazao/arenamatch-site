package br.com.arenamatch.beans;

import java.io.Serializable;

import org.springframework.web.client.HttpClientErrorException;

import br.com.arenamatch.client.AuthClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
public class RecuperarSenhaBean implements Serializable {

    @Inject
    private AuthClient authClient;

    @Getter @Setter
    private String email;
    
    @Getter @Setter
    private String codigo;
    
    @Getter @Setter
    private String novaSenha;
    
    @Getter @Setter
    private String confirmarSenha;

    @Getter
    private boolean etapa2 = false; // Controla qual tela aparece

    public void enviarCodigo() {
        try {
            authClient.solicitarCodigoRecuperacao(email);
            etapa2 = true;
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Código enviado! Verifique seu e-mail.");
            
        } catch (HttpClientErrorException e) {
            // Captura EXATAMENTE o erro 404 (Usuário não existe)
            adicionarMensagem(FacesMessage.SEVERITY_WARN, "Atenção", "Este e-mail não está cadastrado em nosso sistema.");
            
        } catch (Exception e) {
            // Captura erros 500 (Falha no envio do e-mail, banco fora do ar, etc)
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro do Servidor", "Tivemos um problema técnico ao enviar o e-mail. Tente novamente mais tarde.");
            e.printStackTrace(); // Exibe no console o erro real para você debugar
        }
    }

    public void alterarSenha() {
        if (!novaSenha.equals(confirmarSenha)) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "As senhas não conferem.");
            return;
        }

        try {
            authClient.redefinirSenha(email, codigo, novaSenha);
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Senha alterada com sucesso!");
            
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
            
        } catch (HttpClientErrorException.BadRequest e) {
            // Captura EXATAMENTE o erro 400 (Código errado ou expirado)
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "O código digitado é inválido ou expirou.");
            
        } catch (Exception e) {
            // Qualquer outro erro genérico
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro do Servidor", "Não foi possível alterar a senha no momento.");
        }
    }

    public void voltarEtapa1() {
        etapa2 = false;
        codigo = null;
        novaSenha = null;
        confirmarSenha = null;
    }

    private void adicionarMensagem(FacesMessage.Severity severity, String sumario, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, sumario, detalhe));
    }
}