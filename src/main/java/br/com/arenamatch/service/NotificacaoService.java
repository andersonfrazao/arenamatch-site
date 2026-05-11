package br.com.arenamatch.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.arenamatch.dto.NotificacaoDTO;
import br.com.arenamatch.entity.ConviteLiga;
import br.com.arenamatch.entity.Notificacao;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusConviteLiga;
import br.com.arenamatch.repository.ConviteLigaRepository;
import br.com.arenamatch.repository.NotificacaoRepository;
import br.com.arenamatch.repository.PartidaRepository;

@Service
public class NotificacaoService {

    @Autowired
    private ConviteLigaRepository conviteLigaRepository;

    @Autowired
    private PartidaRepository partidaRepository;
    
    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private PlacarPendenteService placarPendenteService;

    @Autowired
    private HorarioJogoService horarioJogoService;

    // INJEÇÃO DO WEBSOCKET (O "Carteiro" do Spring)
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public List<NotificacaoDTO> buscarNotificacoes(Long idTime) {
        List<NotificacaoDTO> listaConsolidada = new ArrayList<>();

        placarPendenteService.buscarPendencias(idTime);

        // 1. Busca os convites virtuais de LIGA
        listaConsolidada.addAll(buscarConvitesLigaVirtuais(idTime));

        // 2. Busca os convites virtuais de JOGO
        listaConsolidada.addAll(buscarConvitesJogoVirtuais(idTime));

        // 3. NOVA FONTE: Busca os alertas reais (PLACAR) da nova tabela
        listaConsolidada.addAll(buscarNotificacoesFisicas(idTime));

        // 4. Alertas virtuais de jogos realizados sem placar informado
        listaConsolidada.addAll(buscarPlacaresPendentesVirtuais(idTime));

        // Ordena a lista consolidada para mostrar os mais recentes primeiro (Ordem Decrescente)
        listaConsolidada.sort((n1, n2) -> {
            if (n1.getDataCriacao() == null || n2.getDataCriacao() == null) return 0;
            return n2.getDataCriacao().compareTo(n1.getDataCriacao()); 
        });

        return listaConsolidada;
    }

    // --- MÉTODOS AUXILIARES DE BUSCA ---

    private List<NotificacaoDTO> buscarConvitesLigaVirtuais(Long idTime) {
        List<NotificacaoDTO> lista = new ArrayList<>();
        List<ConviteLiga> convitesLiga = conviteLigaRepository.findByTimeConvidadoIdAndStatus(idTime, StatusConviteLiga.PENDENTE);
        
        for (ConviteLiga c : convitesLiga) {
            NotificacaoDTO dto = new NotificacaoDTO();
            dto.setIdReferencia(c.getId());
            dto.setTipo("LIGA");
            dto.setTitulo("Convite de liga");
            dto.setSubtitulo(c.getLiga().getNome() + " — " + c.getMensagem());
            dto.setDataCriacao(c.getDataConvite()); 
            lista.add(dto);
        }
        return lista;
    }

    private List<NotificacaoDTO> buscarConvitesJogoVirtuais(Long idTime) {
        List<NotificacaoDTO> lista = new ArrayList<>();
        List<Partida> partidasPendentes = partidaRepository.buscarConvitesPendentesParaOTime(idTime); 
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

        for(Partida p : partidasPendentes) {
            NotificacaoDTO dto = new NotificacaoDTO();
            dto.setIdReferencia(p.getId());
            dto.setTipo("JOGO");
            dto.setTitulo("Convite de jogo");
            
            boolean fuiEuQueEnviei = p.getDesafiante() != null && p.getDesafiante().getId().equals(idTime);
            dto.setEnviadoPorMim(fuiEuQueEnviei);
            
            boolean souMandante = p.getMandante() != null && p.getMandante().getId().equals(idTime);
            Time adversario = souMandante ? p.getVisitante() : p.getMandante();
            Time donoDoCampo = definirDonoDoCampo(p);
            
            String nomeAdversario = adversario.getNome();
            String prefixo = fuiEuQueEnviei ? "Enviado para: " : "Recebido de: ";
            LocalDateTime dataHoraJogo = horarioJogoService.resolverDataHoraMandante(p);
            String dataHoraFormatada = dataHoraJogo != null ? dataHoraJogo.format(formatador) : "Data a definir";
            
            dto.setSubtitulo(prefixo + nomeAdversario + " — " + dataHoraFormatada);
            dto.setDataCriacao(p.getDataSolicitacao());
            dto.setValorTaxa(donoDoCampo != null ? donoDoCampo.getValorTaxa() : null);
            
            lista.add(dto);
        }
        return lista;
    }

    private Time definirDonoDoCampo(Partida partida) {
        if (partida.getMandante() != null && partida.getMandante().isMandoCampo()) {
            return partida.getMandante();
        }

        if (partida.getVisitante() != null && partida.getVisitante().isMandoCampo()) {
            return partida.getVisitante();
        }

        return partida.getMandante();
    }

    private List<NotificacaoDTO> buscarNotificacoesFisicas(Long idTime) {
        List<NotificacaoDTO> lista = new ArrayList<>();
        // Usa o Repository novo para buscar dados físicos no banco
        List<Notificacao> alertasFisicos = notificacaoRepository.findByTimeIdOrderByDataCriacaoDesc(idTime);
        
        for (Notificacao n : alertasFisicos) {
            NotificacaoDTO dto = new NotificacaoDTO();
            dto.setIdReferencia(n.getIdReferencia());
            dto.setTipo(n.getTipo()); // Vai puxar "PLACAR" do banco
            dto.setTitulo(n.getTitulo());
            dto.setSubtitulo(n.getSubtitulo());
            dto.setDataCriacao(n.getDataCriacao());
            dto.setEnviadoPorMim(false); // Alertas do sistema não são "enviados por mim"
            lista.add(dto);
        }
        return lista;
    }

    private List<NotificacaoDTO> buscarPlacaresPendentesVirtuais(Long idTime) {
        List<NotificacaoDTO> lista = new ArrayList<>();

        List<Partida> partidas = partidaRepository.buscarJogosRealizadosComPlacarPendente(idTime);
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Partida p : partidas) {
            NotificacaoDTO dto = new NotificacaoDTO();
            dto.setIdReferencia(p.getId());
            dto.setTipo("PLACAR_PENDENTE");
            dto.setTitulo("Placar pendente");
            dto.setSubtitulo("Acesse a agenda e informe o placar do jogo do dia "
                    + (p.getDataHora() != null ? p.getDataHora().format(formatador) : "anterior")
                    + " para manter o ranking atualizado.");
            dto.setDataCriacao(p.getDataHora());
            dto.setEnviadoPorMim(false);
            lista.add(dto);
        }

        return lista;
    }

    // --- MÉTODOS DE AÇÃO ---
    
    @Transactional
    public void criarNotificacao(Long idTimeDestino, String tipo, Long idReferencia, String titulo, String subtitulo) {
        
        // 1. Cria a entidade e salva no banco de dados
        Notificacao notificacao = new Notificacao();
        
        notificacao.setTimeId(idTimeDestino); 
        notificacao.setTipo(tipo);
        notificacao.setIdReferencia(idReferencia);
        notificacao.setTitulo(titulo);
        notificacao.setSubtitulo(subtitulo);
        notificacao.setDataCriacao(LocalDateTime.now());

        notificacaoRepository.save(notificacao);

        // 2. Dispara o gatilho em Tempo Real (WebSocket)
        try {
            String canal = "/topic/notificacoes/" + idTimeDestino;
            messagingTemplate.convertAndSend(canal, "CHEGOU_CONVITE");
            
        } catch (Exception e) {
            System.out.println("Erro ao tentar enviar WebSocket para o time " + idTimeDestino + ": " + e.getMessage());
        }
    }
    
    // Opcional: Se desejar limpar a notificação após o Time B confirmar o placar
    @Transactional
    public void deletarNotificacao(Long idPartida, String tipo) {
        // Implemente isso futuramente no NotificacaoRepository se quiser que o alerta suma do banco após lido
    	notificacaoRepository.deletarPorReferenciaETipo(idPartida, "PLACAR");
    }
    
    @Transactional
    public void deletarNotificacaoPlacar(Long idPartida) {
        // Chama o comando que criamos no Repository no passo anterior!
        notificacaoRepository.deletarPorReferenciaETipo(idPartida, "PLACAR");
    }
}
