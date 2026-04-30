package br.com.arenamatch.beans;

import br.com.arenamatch.client.LigaClient;
import br.com.arenamatch.dto.LigaDetalheDTO;
import br.com.arenamatch.dto.TimeSimplesDTO;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Named
@ViewScoped
public class GerenciarLigaBean implements Serializable {

    @Autowired
    private LigaClient ligaClient;

    @Autowired
    private SessaoBean sessaoBean;

    @Getter @Setter
    private Long ligaId;

    @Getter @Setter
    private LigaDetalheDTO ligaAtual;

    @Getter @Setter
    private String termoBusca;
    
    @Getter @Setter
    private TimeSimplesDTO timeSelecionadoParaConvite;
    
    @Getter @Setter
    private String mensagemConvite;
    
    @Getter @Setter
    private List<TimeSimplesDTO> resultadosBusca = new ArrayList<>();

    // --- NOVA LISTA PARA SEGURAR OS IDS ---
    @Getter @Setter
    private List<Long> idsTimesComConvite = new ArrayList<>();

    public void carregarLiga() {
        if (ligaId != null) {
            try {
                ligaAtual = ligaClient.buscarLigaPorId(ligaId);
                log.info("Liga [{}] carregada com sucesso na tela de gestão.", ligaAtual.getNome());
            } catch (Exception e) {
                log.error("Erro fatal ao carregar a liga com ID: {}", ligaId, e);
                msgErro("Erro ao carregar os dados da liga.");
            }
        } else {
            log.warn("Tentativa de carregar a tela de gerenciar liga sem informar o ligaId na URL.");
        }
    }

    public void buscarTimesParaConvidar() {
        if (termoBusca == null || termoBusca.trim().length() < 3) {
            msgErro("Digite pelo menos 3 letras para buscar.");
            return;
        }
        try {
            log.info("Buscando times com o termo: '{}'", termoBusca);
            this.resultadosBusca = ligaClient.buscarTimesPorNome(termoBusca);
            
            // --- POPULA A LISTA DE IDS DE QUEM JÁ FOI CONVIDADO ---
            if (this.ligaAtual != null && this.ligaAtual.getId() != null) {
                this.idsTimesComConvite = ligaClient.buscarIdsTimesComConvitePendente(this.ligaAtual.getId());
            } else {
                this.idsTimesComConvite = new ArrayList<>();
            }

            if (this.resultadosBusca.isEmpty()) {
                msgInfo("Nenhum time encontrado com o nome: " + termoBusca);
            } else {
                msgInfo("Encontramos " + this.resultadosBusca.size() + " time(s)!");
            }
        } catch (Exception e) {
            log.error("Erro ao buscar times para convite com o termo '{}'", termoBusca, e);
            msgErro("Erro ao buscar times.");
        }
    }

    // --- MÉTODOS DE VALIDAÇÃO DE TELA ---
    public boolean jaEhMembro(Long idTimeBuscado) {
        if (ligaAtual != null && ligaAtual.getTimes() != null) {
            return ligaAtual.getTimes().stream()
                    .anyMatch(membro -> membro.getId().equals(idTimeBuscado));
        }
        return false;
    }

    public boolean jaFoiConvidado(Long idTimeBuscado) {
        if (idsTimesComConvite != null) {
            return idsTimesComConvite.contains(idTimeBuscado);
        }
        return false;
    }

    public void removerMembro(TimeSimplesDTO membro) {
        try {
            log.info("Iniciando remoção do time '{}' (ID: {}) da liga ID {}", membro.getNome(), membro.getId(), ligaId);
            ligaClient.removerMembro(ligaId, membro.getId());
            msgInfo(membro.getNome() + " foi removido da liga.");
            carregarLiga(); 
        } catch (Exception e) {
            log.error("Erro ao remover o time ID {} da liga ID {}", membro.getId(), ligaId, e);
            msgErro("Erro ao remover time.");
        }
    }
    
    public void prepararConvite(TimeSimplesDTO time) {
        this.timeSelecionadoParaConvite = time;
        this.mensagemConvite = "Olá! Venha fazer parte da nossa liga: " + ligaAtual.getNome();
    }

    public void cancelarConvite() {
        this.timeSelecionadoParaConvite = null;
    }

    public void enviarConvite() { 
        try {
            if (ligaAtual == null || timeSelecionadoParaConvite == null) {
                msgErro("Erro interno: Dados do convite perdidos.");
                return;
            }

            log.info("Enviando convite com msg personalizada para '{}'", timeSelecionadoParaConvite.getNome());
            
            ligaClient.enviarConvite(ligaId, timeSelecionadoParaConvite.getId(), mensagemConvite);
            msgInfo("Convite enviado para " + timeSelecionadoParaConvite.getNome() + "!");
            
            // ADICIONA O TIME NA LISTA DE CONVIDADOS PARA A TELA ATUALIZAR SOZINHA
            this.idsTimesComConvite.add(timeSelecionadoParaConvite.getId());
            
            // Limpa a seleção e fecha o painel
            this.timeSelecionadoParaConvite = null;
            
        } catch (Exception e) {
            log.error("Erro ao enviar convite", e);
            msgErro("Erro ao enviar convite.");
        }
    }

    private void msgInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    private void msgErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }
}