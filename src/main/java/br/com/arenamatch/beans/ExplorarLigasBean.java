package br.com.arenamatch.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import br.com.arenamatch.client.LigaClient;
import br.com.arenamatch.dto.LigaExplorarDTO;

@Named
@ViewScoped
public class ExplorarLigasBean implements Serializable {

    @Autowired
    private LigaClient ligaClient;

    @Autowired
    private SessaoBean sessaoBean;

    @Getter @Setter
    private String termoBusca;

    @Getter @Setter
    private List<LigaExplorarDTO> ligasEncontradas = new ArrayList<>();

    @PostConstruct
    public void init() {
        carregarLigasEmAlta();
    }

    public void carregarLigasEmAlta() {
        try {
            Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
            ligasEncontradas = ligaClient.listarLigasEmAlta(meuTimeId);
        } catch (Exception e) {
            msgErro("Erro ao carregar as ligas em alta.");
        }
    }

    public void pesquisar() {
        try {
            Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
            if (termoBusca == null || termoBusca.trim().isEmpty()) {
                carregarLigasEmAlta(); // Se limpou a busca, volta para o top 15
            } else {
                ligasEncontradas = ligaClient.buscarLigasPorNome(termoBusca, meuTimeId);
            }
        } catch (Exception e) {
            msgErro("Erro ao buscar ligas.");
        }
    }

    public void solicitarParticipacao(LigaExplorarDTO liga) {
        try {
            Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
            ligaClient.solicitarEntradaNaLiga(liga.getId(), meuTimeId);
            
            // Atualiza a flag na tela na hora para o botão mudar para "Aguardando"
            liga.setConvitePendente(true); 
            
            msgInfo("Solicitação enviada! Aguarde a aprovação do administrador.");
        } catch (Exception e) {
            msgErro("Não foi possível enviar a solicitação. " + e.getMessage());
        }
    }

    private void msgInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    private void msgErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }
}