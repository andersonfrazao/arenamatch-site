package br.com.arenamatch.beans;

import java.io.Serializable;
import java.util.List;
import br.com.arenamatch.client.ChatClient;
import br.com.arenamatch.client.NotificacaoClient;
import br.com.arenamatch.dto.NotificacaoDTO;
import br.com.arenamatch.dto.UsuarioDTO;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;

@Named
@SessionScoped
public class SessaoBean implements Serializable {
    
    @Getter 
    private UsuarioDTO usuarioLogado;
    
    @Getter @lombok.Setter
    private Long qtdMensagensNaoLidas = 0L;
    
    @Getter @lombok.Setter
    private Integer qtdNotificacoes = 0;
    
    @Inject private ChatClient chatClient;
    @Inject private NotificacaoClient notificacaoClient;

    public boolean isLogado() {
        return usuarioLogado != null;
    }

    // SETTER MANUAL PARA GARANTIR A SEGURANÇA
    public void setUsuarioLogado(UsuarioDTO usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        
        if (usuarioLogado != null) {
            // Garante que o atributo de autenticação seja injetado na sessão HTTP real
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                context.getExternalContext().getSessionMap().put("usuarioAutenticado", true);
            }
        }
    }
    
    public String logout() {
        this.usuarioLogado = null;
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.getExternalContext().invalidateSession();
        }
        try {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        } catch (Exception e) {}
        
        return "/login.xhtml?faces-redirect=true";
    }

    public void atualizarNotificacoesChat() {
        if (isLogado()) {
            this.qtdMensagensNaoLidas = chatClient.contarNaoLidasGeral(getUsuarioLogado().getIdTime());
        }
    }
    
    public void atualizarNotificacoesGlobais() {
        try {
            if (this.isLogado()) {
                Long meuTimeId = this.getUsuarioLogado().getIdTime();
                List<NotificacaoDTO> pendentes = notificacaoClient.buscarNotificacoes(meuTimeId);
                this.qtdNotificacoes = pendentes != null ? pendentes.size() : 0;
                //org.primefaces.PrimeFaces.current().ajax().update("painelNotificacoesGlobais");
                org.primefaces.PrimeFaces.current().ajax().update("@([id$=painelNotificacoesGlobais])");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}