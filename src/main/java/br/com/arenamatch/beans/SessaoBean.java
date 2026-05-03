package br.com.arenamatch.beans;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import br.com.arenamatch.client.ChatClient;
import br.com.arenamatch.client.NotificacaoClient;
import br.com.arenamatch.dto.NotificacaoDTO;
import br.com.arenamatch.dto.UsuarioDTO;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
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

    @Getter
    private String tokenJwt;
    
    @Getter @lombok.Setter
    private Long qtdMensagensNaoLidas = 0L;
    
    @Getter @lombok.Setter
    private Integer qtdNotificacoes = 0;
    
    @Inject private ChatClient chatClient;
    @Inject private NotificacaoClient notificacaoClient;

    public boolean isLogado() {
        return usuarioLogado != null;
    }

    public String getPlanoAssinaturaLabel() {
        if (usuarioLogado == null || usuarioLogado.getPlanoAssinatura() == null) {
            return "Básico";
        }

        return switch (usuarioLogado.getPlanoAssinatura()) {
            case TRIAL -> "Trial";
            case PRO -> "Pró";
            case BASICO -> "Básico";
        };
    }

    public String getResumoPlano() {
        if (usuarioLogado == null) {
            return "";
        }

        PlanoAssinatura plano = usuarioLogado.getPlanoAssinatura();
        if (plano == PlanoAssinatura.PRO && usuarioLogado.getStatusPagamento() == StatusPagamento.PAGO) {
            return "Recursos premium ativos";
        }

        if (plano == PlanoAssinatura.BASICO || usuarioLogado.isExpirado()) {
            return "Acesso limitado";
        }

        Long diasRestantes = getDiasRestantesPlano();
        if (diasRestantes == null) {
            return "Plano ativo";
        }

        if (diasRestantes == 0) {
            return "Expira hoje";
        }

        return diasRestantes == 1 ? "1 dia restante" : diasRestantes + " dias restantes";
    }

    public Long getDiasRestantesPlano() {
        if (usuarioLogado == null || usuarioLogado.getDataExpiracao() == null) {
            return null;
        }

        long dias = ChronoUnit.DAYS.between(LocalDate.now(), usuarioLogado.getDataExpiracao().toLocalDate());
        return Math.max(dias, 0);
    }

    public String getPlanoCssClass() {
        if (usuarioLogado == null || usuarioLogado.getPlanoAssinatura() == null) {
            return "basico";
        }

        if (usuarioLogado.getPlanoAssinatura() == PlanoAssinatura.PRO
                && usuarioLogado.getStatusPagamento() == StatusPagamento.PAGO) {
            return "pro";
        }

        if (usuarioLogado.isExpirado()) {
            return "basico";
        }

        return switch (usuarioLogado.getPlanoAssinatura()) {
            case TRIAL -> "trial";
            case PRO -> "pro";
            case BASICO -> "basico";
        };
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

    public void setTokenJwt(String tokenJwt) {
        this.tokenJwt = tokenJwt;

        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.getExternalContext().getSessionMap().put("jwtToken", tokenJwt);
        }
    }
    
    public String logout() {
        this.usuarioLogado = null;
        this.tokenJwt = null;
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
