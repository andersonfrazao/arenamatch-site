package br.com.arenamatch.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.arenamatch.client.AgendaClient;
import br.com.arenamatch.client.LigaClient;
import br.com.arenamatch.client.NotificacaoClient;
import br.com.arenamatch.client.PartidaClient;
import br.com.arenamatch.dto.NotificacaoDTO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Named
@SessionScoped
@Slf4j
public class NotificacaoBean implements Serializable {

    @Inject
    private SessaoBean sessaoBean;

    @Inject
    private NotificacaoClient notificacaoClient;

    @Inject
    private LigaClient ligaClient;

    @Inject
    private AgendaClient agendaClient;

    // INJETADO O CLIENTE DE PARTIDA (Para confirmar/contestar o placar)
    @Inject
    private PartidaClient partidaClient;

    @Getter
    @Setter
    private List<NotificacaoDTO> listaNotificacoes = new ArrayList<>();

    @Getter
    private int totalConvitesJogo = 0;
    
    @Getter
    private int totalConvitesLiga = 0;
    
    @Getter
    private int totalNotificacoes = 0;

    @PostConstruct
    public void init() {
        carregarNotificacoes();
    }

    public void carregarNotificacoes() {
        if (sessaoBean.isLogado()) {
            try {
                Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
                if (meuTimeId != null) {
                    this.listaNotificacoes = notificacaoClient.buscarNotificacoes(meuTimeId);
                    calcularTotais();
                    
                    // Sincroniza o número vermelho da bolinha que fica no layout.xhtml
                    sessaoBean.setQtdNotificacoes(this.totalNotificacoes);
                }
            } catch (Exception e) {
                log.error("Erro ao buscar notificações do topo", e);
            }
        }
    }

    private void calcularTotais() {
        if (listaNotificacoes == null || listaNotificacoes.isEmpty()) {
            this.totalConvitesJogo = 0;
            this.totalConvitesLiga = 0;
            this.totalNotificacoes = 0;
            return;
        }

        // Calcula quantos convites são de jogo (desafios pendentes ou placares aguardando)
        this.totalConvitesJogo = (int) listaNotificacoes.stream()
            .filter(n -> "JOGO".equals(n.getTipo()) || "PLACAR".equals(n.getTipo()) || "PLACAR_PENDENTE".equals(n.getTipo()))
            .count();
            
        // Calcula quantos convites são de liga
        this.totalConvitesLiga = (int) listaNotificacoes.stream()
            .filter(n -> "LIGA".equals(n.getTipo()))
            .count();
            
        this.totalNotificacoes = this.totalConvitesJogo + this.totalConvitesLiga;
    }

    // --- Ações de Convites Normais (Agenda e Ligas) ---

    public void aceitarConvite(NotificacaoDTO notif) {
        try {
            if ("LIGA".equals(notif.getTipo())) {
                ligaClient.responderConvite(notif.getIdReferencia(), true);
                msgInfo("Você entrou na liga!");
            } else if ("JOGO".equals(notif.getTipo())) {
                agendaClient.aceitarDesafio(notif.getIdReferencia());
                msgInfo("Desafio aceito! Jogo confirmado.");
            }
            
            carregarNotificacoes();
            atualizarTelaAgendaSeNecessario();
        } catch (Exception e) {
            msgErro("Erro ao aceitar convite.");
        }
    }

    public void recusarConvite(NotificacaoDTO notif) {
        try {
            if ("LIGA".equals(notif.getTipo())) {
                ligaClient.responderConvite(notif.getIdReferencia(), false);
                msgInfo("Convite de liga recusado.");
            } else if ("JOGO".equals(notif.getTipo())) {
                agendaClient.excluirPartida(notif.getIdReferencia());
                msgInfo("Convite de jogo recusado.");
            }
            
            carregarNotificacoes();
            atualizarTelaAgendaSeNecessario();
        } catch (Exception e) {
            msgErro("Erro ao recusar convite.");
        }
    }

    public void cancelarConvite(NotificacaoDTO notif) {
        try {
            if ("JOGO".equals(notif.getTipo())) {
                agendaClient.excluirPartida(notif.getIdReferencia());
                msgInfo("Desafio cancelado.");
                
                carregarNotificacoes();
                atualizarTelaAgendaSeNecessario();
            }
        } catch (Exception e) {
            msgErro("Erro ao cancelar o convite enviado.");
        }
    }

    // ==========================================
    // 🏆 AÇÕES DO NOVO FLUXO DE PLACAR
    // ==========================================

    public void confirmarResultadoPlacar(NotificacaoDTO notif) {
        try {
            if ("PLACAR".equals(notif.getTipo())) {
                partidaClient.confirmarPlacar(notif.getIdReferencia());
                msgInfo("Placar confirmado! Os pontos foram para o Ranking.");
                
                carregarNotificacoes(); // Tira a notificação da lista
                atualizarTelaAgendaSeNecessario();
            }
        } catch (Exception e) {
            log.error("Erro ao confirmar placar via notificação", e);
            msgErro("Erro ao confirmar o placar.");
        }
    }

    public void contestarResultadoPlacar(NotificacaoDTO notif) {
        try {
            if ("PLACAR".equals(notif.getTipo())) {
                partidaClient.contestarPlacar(notif.getIdReferencia());
                msgInfo("Placar contestado! A partida entrou em disputa.");
                
                carregarNotificacoes(); // Tira a notificação da lista
                atualizarTelaAgendaSeNecessario();
            }
        } catch (Exception e) {
            log.error("Erro ao contestar placar via notificação", e);
            msgErro("Erro ao contestar o placar.");
        }
    }

    // --- Utilitários ---

    private void atualizarTelaAgendaSeNecessario() {
        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        if (viewId != null && viewId.contains("minha-agenda")) {
            // Usa o RemoteCommand que criamos na tela da agenda para forçar o recarregamento dos cards
            org.primefaces.PrimeFaces.current().executeScript("if (typeof atualizarCalendarioAjax === 'function') atualizarCalendarioAjax();");
        }
    }

    private void msgInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    private void msgErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }
}
