package br.com.arenamatch.beans;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.RestClientResponseException;

import br.com.arenamatch.client.AgendaClient;
import br.com.arenamatch.client.LigaClient;
import br.com.arenamatch.client.PartidaClient;
import br.com.arenamatch.dto.ConviteLigaDTO;
import br.com.arenamatch.dto.EventoAgendaDTO;
import br.com.arenamatch.dto.ResumoAgendaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Named
@ViewScoped
@Slf4j
public class MinhaAgendaBean implements Serializable {

    @Inject
    private AgendaClient agendaClient;
    
    @Inject
    private PartidaClient partidaClient;

    @Inject
    private SessaoBean sessaoBean;
    
    @Inject
    private LigaClient ligaClient;

    // --- VARIÁVEIS DO NOVO LAYOUT (CALENDÁRIO) ---
    
    @Getter
    private List<ResumoAgendaDTO> diasCalendario; 

    @Getter
    private List<EventoAgendaDTO> eventosDoDia; 

    @Getter @Setter
    private ResumoAgendaDTO diaSelecionado; 

    @Getter @Setter
    private Long meuTimeId;
    
    @Getter @Setter
    private EventoAgendaDTO eventoParaCancelar;

    @Getter @Setter
    private String motivoCancelamento;
    
    @Getter @Setter
    private EventoAgendaDTO eventoEmCancelamento;
    
    private List<ConviteLigaDTO> todosConvitesLiga = new ArrayList<>();
    
    @Getter 
    private List<ConviteLigaDTO> convitesLigaDoDia = new ArrayList<>();
    
    @Getter @Setter
    private LocalDate dataBaseCalendario = LocalDate.now();

    // AQUI: Usando o DTO corretamente para o painel do topo
    @Getter @Setter
    private List<ConviteLigaDTO> convitesLigaPendentes = new ArrayList<>();
    
    @Getter @Setter
    private Integer golsMandanteInformado;
    @Getter @Setter
    private Integer golsVisitanteInformado;

    
    @PostConstruct
    public void init() {
        this.diasCalendario = new ArrayList<>();
        this.eventosDoDia = new ArrayList<>();
        
        if (sessaoBean.isLogado()) {
            try {
                Long idUsuario = sessaoBean.getUsuarioLogado().getId();
                TimeResumoDTO meuTime = agendaClient.buscarMeuTime(idUsuario);
                
                if (meuTime != null) {
                    this.meuTimeId = meuTime.getId();
                    carregarCalendario();
                }
            } catch (Exception e) {
                e.printStackTrace();
                msgErro("Erro ao carregar agenda: " + e.getMessage());
            }
        }
    }

    public void carregarCalendario() {
        try {
            log.info("Buscando no banco a partir de: {}", this.dataBaseCalendario);
            
            this.diasCalendario = agendaClient.buscarCalendario(meuTimeId, this.dataBaseCalendario);

            // Busca TODOS os convites de liga (pendentes, aceitos, recusados, etc)
            this.todosConvitesLiga = ligaClient.buscarConvitesParaAgenda(meuTimeId);

            // --- A MÁGICA AQUI: Filtra apenas os pendentes para exibir no topo da tela! ---
            if (this.todosConvitesLiga != null) {
                this.convitesLigaPendentes = this.todosConvitesLiga.stream()
                    .filter(c -> c.getStatus() != null && "PENDENTE".equals(c.getStatus().name()))
                    .toList();
            }

            if (!diasCalendario.isEmpty()) {
                selecionarDia(diasCalendario.get(0));
            }
        } catch (Exception e) {
            log.error("Erro ao buscar calendário para a data {}", this.dataBaseCalendario, e);
            msgErro("Erro ao buscar calendário.");
        }
    }
    
    public void selecionarDia(ResumoAgendaDTO dia) {
        this.diaSelecionado = dia;
        try {
            this.eventosDoDia = agendaClient.buscarDetalhesDia(meuTimeId, dia.getData());
            
            this.convitesLigaDoDia = this.todosConvitesLiga.stream()
                .filter(c -> c.getDataConvite() != null && c.getDataConvite().toLocalDate().equals(dia.getData()))
                .toList();
                
        } catch (Exception e) {
            log.error("Erro ao carregar detalhes da agenda para o dia {}", dia.getData(), e);
            this.eventosDoDia = new ArrayList<>();
            this.convitesLigaDoDia = new ArrayList<>();
            msgErro("Erro ao carregar detalhes do dia.");
        }
    }
    
    // --- AÇÕES DOS BOTÕES (Aceitar/Recusar) ---

    public void aceitarConvite(EventoAgendaDTO evento) {
        try {
            agendaClient.aceitarDesafio(evento.getIdPartida());
            msgInfo("Convite aceito! Jogo confirmado.");
            carregarCalendario(); 
            if(diaSelecionado != null) {
                selecionarDia(diaSelecionado);
            }
            // SINCRONIZA O SININHO
            org.primefaces.PrimeFaces.current().executeScript("atualizarSininhoAjax();");
        } catch (Exception e) {
            e.printStackTrace();
            msgErro("Erro ao aceitar convite: " + e.getMessage());
        }
    }

    public void recusarConvite(EventoAgendaDTO evento) {
        try {
            agendaClient.excluirPartida(evento.getIdPartida());
            msgInfo("Convite recusado e removido.");
            carregarCalendario();
            if(diaSelecionado != null) selecionarDia(diaSelecionado);
            // SINCRONIZA O SININHO
            org.primefaces.PrimeFaces.current().executeScript("atualizarSininhoAjax();");
        } catch (Exception e) {
            msgErro("Erro ao recusar convite.");
            e.printStackTrace();
        }
    }

    public void cancelarEnvio(EventoAgendaDTO evento) {
        try {
            agendaClient.excluirPartida(evento.getIdPartida());
            msgInfo("Desafio cancelado com sucesso.");
            carregarCalendario();
            if(diaSelecionado != null) selecionarDia(diaSelecionado);
            // SINCRONIZA O SININHO
            org.primefaces.PrimeFaces.current().executeScript("atualizarSininhoAjax();");
        } catch (Exception e) {
            msgErro("Erro ao cancelar desafio.");
        }
    }
    
    // --- MÉTODOS DE CANCELAMENTO ---

    public void prepararCancelamento(EventoAgendaDTO evento) {
        this.eventoEmCancelamento = evento;
        this.motivoCancelamento = ""; 
    }
    
    public void abortarCancelamento() {
        this.eventoEmCancelamento = null;
        this.motivoCancelamento = "";
    }    

    public void confirmarSolicitacaoCancelamento() {
        try {
            if (motivoCancelamento == null || motivoCancelamento.trim().isEmpty()) {
                msgErro("Informe o motivo do cancelamento.");
                return;
            }
            agendaClient.solicitarCancelamento(eventoEmCancelamento.getIdPartida(), meuTimeId, motivoCancelamento);
            msgInfo("Solicitação de cancelamento enviada. Aguardando adversário.");
            this.eventoEmCancelamento = null; 
            carregarCalendario();
            if (diaSelecionado != null) selecionarDia(diaSelecionado);
            // SINCRONIZA O SININHO
            org.primefaces.PrimeFaces.current().executeScript("atualizarSininhoAjax();");
        } catch (RestClientResponseException e) {
            msgErro(e.getResponseBodyAsString()); 
        } catch (Exception e) {
            msgErro("Erro inesperado: " + e.getMessage());
        }
    }

    public void aceitarCancelamento(EventoAgendaDTO evento) {
        try {
            agendaClient.responderCancelamento(evento.getIdPartida(), meuTimeId, true);
            msgInfo("Partida cancelada oficialmente.");
            carregarCalendario();
            if (diaSelecionado != null) selecionarDia(diaSelecionado);
            // SINCRONIZA O SININHO
            org.primefaces.PrimeFaces.current().executeScript("atualizarSininhoAjax();");
        } catch (Exception e) {
            msgErro("Erro ao confirmar cancelamento.");
        }
    }

    public void recusarCancelamento(EventoAgendaDTO evento) {
        try {
            agendaClient.responderCancelamento(evento.getIdPartida(), meuTimeId, false);
            msgInfo("Cancelamento recusado. O jogo continua de pé!");
            carregarCalendario();
            if (diaSelecionado != null) selecionarDia(diaSelecionado);
            // SINCRONIZA O SININHO
            org.primefaces.PrimeFaces.current().executeScript("atualizarSininhoAjax();");
        } catch (Exception e) {
            msgErro("Erro ao recusar cancelamento.");
        }
    }
    
    // --- MÉTODOS AUXILIARES PARA A TELA (PINTAR AS CAIXINHAS) ---
    public boolean temConviteLigaPendenteNoDia(LocalDate data) {
        return todosConvitesLiga.stream().anyMatch(c -> 
            c.getDataConvite() != null && 
            c.getDataConvite().toLocalDate().equals(data) && 
            "PENDENTE".equals(c.getStatus().name())); 
    }
    
    public boolean temConviteLigaRecusadoNoDia(LocalDate data) {
        return todosConvitesLiga.stream().anyMatch(c -> 
            c.getDataConvite() != null && 
            c.getDataConvite().toLocalDate().equals(data) && 
            "RECUSADO".equals(c.getStatus().name())); 
    }

    // --- MÉTODOS PARA O TOPO DA TELA (ATALHOS) ---
    public void aceitarConviteLigaTopo(ConviteLigaDTO convite) {
        aceitarConviteLiga(convite); // Reaproveita a lógica de baixo
    }

    public void recusarConviteLigaTopo(ConviteLigaDTO convite) {
        recusarConviteLiga(convite); // Reaproveita a lógica de baixo
    }
    
    // --- MÉTODOS ORIGINAIS DE ACEITAR/RECUSAR LIGA ---
    public void aceitarConviteLiga(ConviteLigaDTO convite) {
        try {
            log.info("Aceitando convite da liga ID {}", convite.getLiga().getId());
            ligaClient.responderConvite(convite.getId(), true);
            msgInfo("Você entrou na liga " + convite.getLiga().getNome() + "!");
            carregarCalendario();
            if(diaSelecionado != null) selecionarDia(diaSelecionado);
            // SINCRONIZA O SININHO
            org.primefaces.PrimeFaces.current().executeScript("atualizarSininhoAjax();");
        } catch (Exception e) {
            log.error("Erro ao aceitar convite da liga ID {}", convite.getId(), e);
            msgErro("Erro ao aceitar o convite da liga.");
        }
    }
    
    public void recusarConviteLiga(ConviteLigaDTO convite) {
        try {
            log.info("Recusando convite da liga ID {}", convite.getLiga().getId());
            ligaClient.responderConvite(convite.getId(), false);
            msgInfo("Convite de liga recusado.");
            carregarCalendario();
            if(diaSelecionado != null) selecionarDia(diaSelecionado);
            // SINCRONIZA O SININHO
            org.primefaces.PrimeFaces.current().executeScript("atualizarSininhoAjax();");
        } catch (Exception e) {
            log.error("Erro ao recusar convite da liga ID {}", convite.getId(), e);
            msgErro("Erro ao recusar o convite.");
        }
    }
    
    // --- MÉTODOS DE NAVEGAÇÃO ---
    public void avancarPeriodo() {
        this.dataBaseCalendario = this.dataBaseCalendario.plusDays(15);
        log.info(">> AVANÇANDO CALENDÁRIO PARA: {}", this.dataBaseCalendario);
        carregarCalendario();
    }

    public void voltarPeriodo() {
        this.dataBaseCalendario = this.dataBaseCalendario.minusDays(15);
        log.info("<< VOLTANDO CALENDÁRIO PARA: {}", this.dataBaseCalendario);
        carregarCalendario();
    }

    public void voltarParaHoje() {
        this.dataBaseCalendario = LocalDate.now();
        log.info("== VOLTANDO PARA HOJE: {}", this.dataBaseCalendario);
        carregarCalendario();
    }

    public boolean isMostrandoHoje() {
        return LocalDate.now().equals(this.dataBaseCalendario);
    }
    
    // --- NOVO: TEXTO DO MÊS/ANO PARA O CABEÇALHO ---
    public String getMesAnoPeriodo() {
        if (diasCalendario == null || diasCalendario.isEmpty()) {
            return "";
        }
        
        LocalDate inicio = diasCalendario.get(0).getData();
        LocalDate fim = diasCalendario.get(diasCalendario.size() - 1).getData();
        
        java.util.Locale ptBR = new java.util.Locale("pt", "BR");
        java.time.format.DateTimeFormatter fmtMes = java.time.format.DateTimeFormatter.ofPattern("MMMM", ptBR);
        
        String mesInicio = inicio.format(fmtMes);
        mesInicio = mesInicio.substring(0, 1).toUpperCase() + mesInicio.substring(1);
        
        if (inicio.getMonthValue() == fim.getMonthValue()) {
            return mesInicio + " " + inicio.getYear();
        } else {
            String mesFim = fim.format(fmtMes);
            mesFim = mesFim.substring(0, 1).toUpperCase() + mesFim.substring(1);
            
            if (inicio.getYear() == fim.getYear()) {
                return mesInicio + " / " + mesFim + " " + inicio.getYear();
            } else {
                return mesInicio + " " + inicio.getYear() + " / " + mesFim + " " + fim.getYear();
            }
        }
    }
    
    public void cancelarConvite(Long idPartida) {
        try {
            partidaClient.cancelarConvitePorId(idPartida);
            msgInfo("Convite cancelado com sucesso.");
            carregarCalendario();
            if(diaSelecionado != null) selecionarDia(diaSelecionado);
            // SINCRONIZA O SININHO
            org.primefaces.PrimeFaces.current().executeScript("atualizarSininhoAjax();");
        } catch (Exception e) {
            msgErro("Não foi possível cancelar o convite.");
            log.error("Não foi possível cancelar o convite", e);
        }
    }
    
    public void salvarPlacarInline(EventoAgendaDTO evento) {
        try {
            if (golsMandanteInformado == null || golsVisitanteInformado == null) {
                msgErro("Por favor, preencha ambos os gols.");
                return;
            }
            
            partidaClient.informarPlacar(evento.getIdPartida(), 
                                         golsMandanteInformado, 
                                         golsVisitanteInformado, 
                                         meuTimeId);
            
            msgInfo("Placar enviado! Aguardando confirmação do adversário.");
            
            // Limpa os campos e atualiza a lista
            golsMandanteInformado = null;
            golsVisitanteInformado = null;
            carregarCalendario();
            selecionarDia(diaSelecionado);
            
            // ==========================================
            // O GOL DE PLACA: AS DUAS LINHAS NOVAS AQUI
            // ==========================================
            // 1. Atualiza a contagem no backend
            sessaoBean.atualizarNotificacoesGlobais();
            
            // 2. O grito global para apagar o sininho da TELA com o ":" 
            //org.primefaces.PrimeFaces.current().ajax().update(":painelNotificacoesGlobais");
         // O seletor @([id$=...]) procura em qualquer lugar da tela sem se importar com formulários!
            org.primefaces.PrimeFaces.current().ajax().update("@([id$=painelNotificacoesGlobais])");
            // ==========================================

            // Sincroniza o sininho do adversário via WebSocket
            org.primefaces.PrimeFaces.current().executeScript("atualizarSininhoAjax();");
            
        } catch (Exception e) {
            msgErro("Erro ao salvar placar: " + e.getMessage());
        }
    }
    
    public void confirmarPlacarAgenda(EventoAgendaDTO evento) {
        try {
            partidaClient.confirmarPlacar(evento.getIdPartida());
         // --- A MÁGICA DA SINCRONIZAÇÃO ---
            // 1. Recalcula os números (faz o buscarNotificacoes() novamente)
            sessaoBean.atualizarNotificacoesGlobais();
            
            // 2. Avisa o PrimeFaces para redesenhar o sininho lá no topo da tela
            //org.primefaces.PrimeFaces.current().ajax().update(":painelNotificacoesGlobais");
         // O seletor @([id$=...]) procura em qualquer lugar da tela sem se importar com formulários!
            org.primefaces.PrimeFaces.current().ajax().update("@([id$=painelNotificacoesGlobais])");
            // ---------------------------------
            
            msgInfo("Placar confirmado com sucesso!");
            carregarCalendario();
            selecionarDia(diaSelecionado);
        } catch (Exception e) {
            msgErro("Erro ao confirmar placar.");
        }
    }

    public void contestarPlacarAgenda(EventoAgendaDTO evento) {
        try {
            partidaClient.contestarPlacar(evento.getIdPartida());
            
         // Repete a mesma lógica aqui para o sininho sumir ao contestar
            sessaoBean.atualizarNotificacoesGlobais();
            org.primefaces.PrimeFaces.current().ajax().update(":painelNotificacoesGlobais");
            
            msgInfo("Placar contestado. O jogo entrou em disputa.");
            carregarCalendario();
            selecionarDia(diaSelecionado);
        } catch (Exception e) {
            msgErro("Erro ao contestar placar.");
        }
    }
    
    // --- MENSAGENS ---
    private void msgInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }
    
    private void msgErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }
}