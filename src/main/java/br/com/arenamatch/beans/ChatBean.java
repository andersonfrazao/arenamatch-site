package br.com.arenamatch.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import br.com.arenamatch.client.ChatClient;
import br.com.arenamatch.dto.ConversaInboxDTO;
import br.com.arenamatch.dto.MensagemChatDTO;
import org.primefaces.PrimeFaces;

@Named
@ViewScoped
public class ChatBean implements Serializable {

    @Inject
    private ChatClient chatClient;

    @Inject
    private SessaoBean sessaoBean;

    @Getter @Setter
    private List<ConversaInboxDTO> inbox = new ArrayList<>();

    @Getter @Setter
    private ConversaInboxDTO conversaSelecionada;

    @Getter @Setter
    private List<MensagemChatDTO> mensagens = new ArrayList<>();

    @Getter @Setter
    private String novaMensagem;

    @PostConstruct
    public void init() {
        carregarInbox();
    }

    public void carregarInbox() {
        try {
            Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
            inbox = chatClient.listarInbox(meuTimeId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void selecionarConversa(ConversaInboxDTO conversa) {
        this.conversaSelecionada = conversa;
        Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
        
        // 1. Marca como lida
        if ("JOGO".equals(conversa.getTipo())) {
            chatClient.marcarComoLidas(conversa.getIdPartida(), meuTimeId);
            // Conecta no WebSocket passando o tipo JOGO
            PrimeFaces.current().executeScript("conectarWebSocketChat('JOGO', " + conversa.getIdPartida() + ");");
        } else {
            chatClient.marcarComoLidasLiga(conversa.getIdLiga(), meuTimeId);
            // Conecta no WebSocket passando o tipo LIGA
            PrimeFaces.current().executeScript("conectarWebSocketChat('LIGA', " + conversa.getIdLiga() + ");");
        }
        
        // 2. Atualiza a bolinha vermelha e recarrega msgs
        sessaoBean.atualizarNotificacoesChat();
        recarregarMensagens();
    }

    public void recarregarMensagens() {
        if (conversaSelecionada != null) {
            Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
            
            if ("JOGO".equals(conversaSelecionada.getTipo())) {
                mensagens = chatClient.buscarHistorico(conversaSelecionada.getIdPartida(), meuTimeId);
            } else {
                mensagens = chatClient.buscarHistoricoLiga(conversaSelecionada.getIdLiga(), meuTimeId);
            }
            
            PrimeFaces.current().executeScript("rolarParaFim();");
        }
    }

    public void enviarMensagem() {
        if (novaMensagem != null && !novaMensagem.trim().isEmpty() && conversaSelecionada != null) {
            Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
            
            if ("JOGO".equals(conversaSelecionada.getTipo())) {
                chatClient.enviarMensagem(conversaSelecionada.getIdPartida(), meuTimeId, novaMensagem);
            } else {
                chatClient.enviarMensagemLiga(conversaSelecionada.getIdLiga(), meuTimeId, novaMensagem);
            }
            
            this.novaMensagem = ""; 
            recarregarMensagens();
            carregarInbox();
        }
    }
    
    public void chegouMensagemPeloWebSocket() {
        recarregarMensagens();
        carregarInbox(); 
    }
}