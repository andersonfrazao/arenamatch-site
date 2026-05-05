package br.com.arenamatch.beans;

import java.io.Serializable;

import org.springframework.web.client.RestClientResponseException;

import br.com.arenamatch.client.AuthClient;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
public class AtivacaoContaBean implements Serializable {

    @Inject
    private AuthClient authClient;

    @Getter @Setter
    private String email;

    @Getter @Setter
    private String codigo;

    @PostConstruct
    public void init() {
        Object emailFlash = FacesContext.getCurrentInstance().getExternalContext().getFlash().get("emailAtivacao");
        if (emailFlash != null) {
            email = emailFlash.toString();
        }
    }

    public String ativarConta() {
        try {
            authClient.ativarConta(email, codigo);
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Conta ativada com sucesso! Faca login para continuar.");
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            return "/login.xhtml?faces-redirect=true";
        } catch (RestClientResponseException e) {
            String mensagem = e.getResponseBodyAsString();
            if (mensagem == null || mensagem.trim().isEmpty()) {
                mensagem = "Nao foi possivel ativar a conta. Verifique o codigo informado.";
            }
            System.err.println("[ERRO AO ATIVAR CONTA] HTTP " + e.getStatusCode() + " - " + mensagem);
            e.printStackTrace();
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", mensagem);
            return null;
        } catch (Exception e) {
            System.err.println("[ERRO AO ATIVAR CONTA]");
            e.printStackTrace();
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Nao foi possivel ativar a conta no momento.");
            return null;
        }
    }

    public void reenviarCodigo() {
        try {
            authClient.reenviarCodigoAtivacao(email);
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Codigo de ativacao reenviado.");
        } catch (RestClientResponseException e) {
            String mensagem = e.getResponseBodyAsString();
            if (mensagem == null || mensagem.trim().isEmpty()) {
                mensagem = "Nao foi possivel reenviar o codigo.";
            }
            System.err.println("[ERRO AO REENVIAR CODIGO DE ATIVACAO] HTTP " + e.getStatusCode() + " - " + mensagem);
            e.printStackTrace();
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", mensagem);
        } catch (Exception e) {
            System.err.println("[ERRO AO REENVIAR CODIGO DE ATIVACAO]");
            e.printStackTrace();
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Nao foi possivel reenviar o codigo.");
        }
    }

    private void adicionarMensagem(FacesMessage.Severity severity, String sumario, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, sumario, detalhe));
    }
}
