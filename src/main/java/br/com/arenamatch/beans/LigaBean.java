package br.com.arenamatch.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientResponseException;

import br.com.arenamatch.client.LigaClient;
import br.com.arenamatch.dto.ConviteLigaDTO;
import br.com.arenamatch.dto.LigaDetalheDTO;
import br.com.arenamatch.entity.ConviteLiga;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
public class LigaBean implements Serializable {

    @Autowired
    private LigaClient ligaClient;

    // TODO: Substitua pelo ID do time logado na sua sessão!
    @Autowired // Ou @Inject, dependendo de como o seu SessaoBean está anotado
    private SessaoBean sessaoBean; 

    // Controle do formulário Inline
    @Getter @Setter
    private boolean exibindoFormulario = false;
    
    @Getter @Setter
    private String nomeNovaLiga;
    
    @Getter @Setter
    private String descricaoNovaLiga;
    
    @Getter @Setter
    private List<LigaDetalheDTO> minhasLigas = new ArrayList<>();
    
    @Getter @Setter
    private List<ConviteLigaDTO> convitesPendentes = new ArrayList<>();

    @PostConstruct
    public void init() {
        carregarDados();
    }

    public void carregarDados() {
        try {
            minhasLigas = ligaClient.buscarLigasDoTime(getMeuTimeId());
            convitesPendentes = ligaClient.buscarConvitesPendentes(getMeuTimeId());
        } catch (Exception e) {
            msgErro("Erro ao carregar dados das ligas.");
            e.printStackTrace();
        }
    }

    // --- AÇÕES DO FORMULÁRIO INLINE ---
    public void prepararNovaLiga() {
        this.exibindoFormulario = true;
        this.nomeNovaLiga = "";
        this.descricaoNovaLiga = "";
    }

    public void cancelarNovaLiga() {
        this.exibindoFormulario = false;
    }

    public void salvarNovaLiga() {
        try {
            if (nomeNovaLiga == null || nomeNovaLiga.trim().isEmpty()) {
                msgErro("O nome da liga é obrigatório.");
                return;
            }
            
            ligaClient.criarLiga(getMeuTimeId(), nomeNovaLiga, descricaoNovaLiga);
            msgInfo("Liga criada com sucesso!");
            
            this.exibindoFormulario = false; // Fecha o form
            carregarDados(); // Atualiza a lista
            
        } catch (RestClientResponseException e) {
            msgErro(e.getResponseBodyAsString());
        } catch (Exception e) {
            msgErro("Erro interno ao criar liga.");
            e.printStackTrace();
        }
    }

    // --- AÇÕES DE CONVITE ---
    public void aceitarConvite(ConviteLigaDTO convite) {
        try {
            ligaClient.responderConvite(convite.getId(), true);
            msgInfo("Você entrou na liga " + convite.getLiga().getNome() + "!");
            carregarDados();
        } catch (Exception e) {
            msgErro("Erro ao aceitar convite.");
        }
    }

    public void recusarConvite(ConviteLigaDTO convite) {
        try {
            ligaClient.responderConvite(convite.getId(), false);
            msgInfo("Convite recusado.");
            carregarDados();
        } catch (Exception e) {
            msgErro("Erro ao recusar convite.");
        }
    }
    
    private Long getMeuTimeId() {
        // Ajuste o caminho abaixo conforme a sua estrutura de classes (Usuario -> Time -> Id)
        Long id = sessaoBean.getUsuarioLogado().getIdTime();
        if (id == null) {
            throw new RuntimeException("Nenhum time vinculado a este usuário na sessão!");
        }
        
        return id;
    }

    // Utilitário para mensagens
    private void msgInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    private void msgErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }
    
    
}