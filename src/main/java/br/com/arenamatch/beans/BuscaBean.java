package br.com.arenamatch.beans;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime; // Importante para definir horário padrão
import java.util.ArrayList;
import java.util.List;

import br.com.arenamatch.client.AgendaClient;
import br.com.arenamatch.client.BuscaClient;
import br.com.arenamatch.client.ParametroSistemaClient;
import br.com.arenamatch.client.PartidaClient;
import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.dto.FiltroBuscaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.enums.PlanoAssinatura;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.client.RestClientResponseException;

@Named
@ViewScoped
public class BuscaBean implements Serializable {
    
    @Inject
    private BuscaClient buscaClient;
    
    @Inject
    private SessaoBean sessaoBean;
    
    @Inject
    private PartidaClient partidaClient;
    
    @Inject
    private AgendaClient agendaClient;

    @Inject
    private ParametroSistemaClient parametroSistemaClient;
    
    @Getter @Setter
    private FiltroBuscaDTO filtro = new FiltroBuscaDTO();
    
    @Getter
    private List<TimeResumoDTO> resultados;

    @Getter @Setter
    private TimeResumoDTO timeSelecionadoParaDesafio;

    @Getter @Setter
    private String mensagemDesafio;
    
    // Cache do ID do meu time para não buscar toda hora
    private Long idMeuTimeCache; 
 
    @Getter @Setter
    private Categoria categoriaSelecionada;
    
 

    @PostConstruct
    public void init() {
        this.resultados = new ArrayList<>();
        aplicarLimiteRaioBasicoNaTela();
    }

    public void pesquisar() {
        aplicarLimiteRaioBasicoNaTela();

        // 1. Validação da Data (Obrigatória)
        if (filtro.getDataJogo() == null) {
            msgWarn("Selecione uma data para realizar a busca.");
            return;
        }

        if (filtro.getDataJogo().isBefore(LocalDate.now())) {
            this.resultados = new ArrayList<>();
            msgWarn("A data da busca não pode ser inferior à data atual.");
            return;
        }

        try {
            parametroSistemaClient.validarDataMinimaAgendamento(filtro.getDataJogo());
        } catch (RestClientResponseException e) {
            this.resultados = new ArrayList<>();
            msgWarn(e.getResponseBodyAsString());
            return;
        }

        try {
            // 2. Obtém o ID do time logado (Necessário para o cálculo de distância)
            Long meuId = carregarIdMeuTime();
            
            if (meuId == null) {
                msgErro("Você precisa ter um time cadastrado para buscar adversários.");
                return;
            }

            carregarResultados(meuId);
            
            if (resultados.isEmpty()) {
                msgInfo("Nenhum time encontrado para esta data na sua região.");
            }
            
        } catch (RestClientResponseException e) {
            this.resultados = new ArrayList<>();
            msgErro(e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            msgErro("Erro ao buscar os times");
        }
    }
    
    public void confirmarDesafio() {
        try {
            Long meuId = carregarIdMeuTime();

            if (meuId == null) {
                msgErro("Erro ao identificar seu time.");
                return;
            }

            DesafioDTO dto = new DesafioDTO();
            dto.setIdTimeDesafiante(meuId);
            dto.setIdTimeDesafiado(timeSelecionadoParaDesafio.getId());
            dto.setMensagem(mensagemDesafio);
            dto.setCategoria(timeSelecionadoParaDesafio.getCategoria());
            
            // LÓGICA DA DATA:
            // Pegamos a data do filtro (que é obrigatória) e definimos um horário padrão (ex: 14:00)
            // Ou o horário de início da disponibilidade do time (se tivermos essa info)
            // Aqui fixei 14:00 como referência, mas o texto da mensagem é o que vale.
            if (filtro.getDataJogo() != null) {
                dto.setDataHoraPartida(filtro.getDataJogo().atStartOfDay());
            } else {
                msgErro("A data da pesquisa foi perdida. Pesquise novamente.");
                return;
            }

            partidaClient.enviarDesafio(dto);

            msgInfo("Desafio enviado com sucesso para " + timeSelecionadoParaDesafio.getNome() + "!");

            carregarResultados(meuId);
            
            cancelarDesafio();
            
        } catch (RestClientResponseException e) {
            msgErro("Nao foi possivel enviar o desafio: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            msgErro("Nao foi possivel enviar o desafio: " + e.getMessage());
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void carregarResultados(Long meuId) {
        this.resultados = buscaClient.filtrarTimes(filtro, meuId);
    }

    public boolean isPlanoBasico() {
        return sessaoBean.getUsuarioLogado() != null
                && sessaoBean.getUsuarioLogado().getPlanoAssinatura() == PlanoAssinatura.BASICO;
    }

    public Integer getRaioMaximoPlanoBasicoKm() {
        return 10;
    }

    public void informarBloqueioRaioBasico() {
        aplicarLimiteRaioBasicoNaTela();
        msgWarn("O ajuste de distancia esta bloqueado no plano BASICO. Para buscar acima de "
                + getRaioMaximoPlanoBasicoKm() + " km, mude para o plano PRO.");
    }

    private void aplicarLimiteRaioBasicoNaTela() {
        if (isPlanoBasico()
                && (filtro.getRaioKm() == null || filtro.getRaioKm() > getRaioMaximoPlanoBasicoKm())) {
            filtro.setRaioKm(getRaioMaximoPlanoBasicoKm());
        }
    }

    // Carrega o ID do time apenas uma vez e guarda em cache
    private Long carregarIdMeuTime() {
        if (this.idMeuTimeCache != null) {
            return this.idMeuTimeCache;
        }
        
        try {
            Long idUsuario = sessaoBean.getUsuarioLogado().getId();
            // Assume que agendaClient retorna TimeResumoDTO ou similar
            TimeResumoDTO meuTime = agendaClient.buscarMeuTime(idUsuario);
            
            if (meuTime != null) {
                this.idMeuTimeCache = meuTime.getId();
                return this.idMeuTimeCache;
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar time do usuário: " + e.getMessage());
        }
        return null;
    }

    public void prepararDesafio(TimeResumoDTO timeDesafiado) {
        this.timeSelecionadoParaDesafio = timeDesafiado;
        this.mensagemDesafio = "";
        // Não resetamos a data aqui, pois usaremos a data do filtro!
    }
    
    public void cancelarDesafio() {
        this.timeSelecionadoParaDesafio = null;
        this.mensagemDesafio = "";
    }
    
    public void cancelarConviteEnviado(Long idAdversario) {
        try {
            Long meuTimeId = sessaoBean.getUsuarioLogado().getIdTime();
            
            partidaClient.cancelarConvitePorAdversario(meuTimeId, idAdversario);
            // realizarBusca(); // Atualiza a lista da tela
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Cancelado", "Convite retirado."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao cancelar convite."));
        }
    }
    
    public Categoria[] getCategoriasEnum() {
        return Categoria.values();
    }

    public Categoria[] getCategorias() {
        return Categoria.values();
    }
    
    private void msgWarn(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", msg));
    }
    
    private void msgErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }
    
    private void msgInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }
}
