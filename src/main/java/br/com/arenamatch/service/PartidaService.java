package br.com.arenamatch.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.dto.PartidaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.enums.StatusPlacar;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;

@Service
public class PartidaService {

    @Autowired private PartidaRepository partidaRepository;
    @Autowired private TimeRepository timeRepository;
    @Autowired private NotificacaoService notificacaoService;
    @Autowired private AssinaturaService assinaturaService;
    @Autowired private ParametroSistemaService parametroSistemaService;
    @Autowired private PlacarPendenteService placarPendenteService;
    
    public List<PartidaDTO> listarProximosJogos(Long idTime) {
        Time time = timeRepository.findById(idTime)
                .orElseThrow(() -> new RuntimeException("Time não encontrado com o ID: " + idTime));

        List<Partida> partidas = partidaRepository.buscarPorTime(time);

        return partidas.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    // --- FLUXO 1: SOLICITAR CANCELAMENTO ---
    @Transactional
    public void solicitarCancelamento(Long idPartida, Long idTimeSolicitante, String motivo) {
        Time timeSolicitante = timeRepository.findById(idTimeSolicitante)
                .orElseThrow(() -> new RuntimeException("Time solicitante não encontrado"));

        solicitarCancelamento(idPartida, timeSolicitante, motivo);
    }

    @Transactional
    public void solicitarCancelamento(Long idPartida, Time timeSolicitante, String motivo) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida não encontrada"));

        if (!partida.getMandante().equals(timeSolicitante) && !partida.getVisitante().equals(timeSolicitante)) {
            throw new RuntimeException("Você não participa deste jogo.");
        }

        long diasAteOJogo = ChronoUnit.DAYS.between(LocalDateTime.now(), partida.getDataHora());
        
        if (diasAteOJogo < 3) {
            throw new RuntimeException("Cancelamento não permitido! Faltam menos de 3 dias para o jogo. Combine via Chat.");
        }

        partida.setStatus(StatusPartida.SOLICITACAO_CANCELAMENTO);
        partida.setSolicitanteCancelamento(timeSolicitante);
        partida.setMotivoCancelamento(motivo);
        partida.setDataSolicitacao(LocalDateTime.now());
        
        partidaRepository.save(partida);
    }

    // --- FLUXO 2: RESPONDER SOLICITAÇÃO (ACEITAR/RECUSAR) ---
    @Transactional
    public void responderCancelamento(Long idPartida, Long idTimeRespondente, boolean aceitar) {
        Time timeRespondente = timeRepository.findById(idTimeRespondente)
                .orElseThrow(() -> new RuntimeException("Time respondente não encontrado"));

        responderCancelamento(idPartida, timeRespondente, aceitar);
    }

    @Transactional
    public void responderCancelamento(Long idPartida, Time timeRespondente, boolean aceitar) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida não encontrada"));

        if (partida.getSolicitanteCancelamento().equals(timeRespondente)) {
            throw new RuntimeException("Você não pode responder sua própria solicitação.");
        }

        if (aceitar) {
            partida.setStatus(StatusPartida.CANCELADO);
            // Poderia limpar a notificação de jogo aqui, caso ainda exista
            if (aceitar) {
                partida.setStatus(StatusPartida.CANCELADO);
            }
        } else {
            partida.setStatus(StatusPartida.AGENDADO);
            partida.setSolicitanteCancelamento(null);
            partida.setMotivoCancelamento(null);
        }
        
        partidaRepository.save(partida);
    }
    
    @Transactional
    public void criarDesafio(DesafioDTO dto) {
        java.time.LocalDate dataJogo = dto.getDataHoraPartida().toLocalDate();
        int minDiasAntecedencia = parametroSistemaService.buscarInteiro(
                ParametroSistemaService.MIN_DIAS_ANTECEDENCIA_AGENDAMENTO,
                3
        );
        java.time.LocalDate dataMinimaPermitida = java.time.LocalDate.now().plusDays(minDiasAntecedencia);

        if (dataJogo.isBefore(dataMinimaPermitida)) {
            throw new RuntimeException("O jogo precisa ser marcado com pelo menos "
                    + minDiasAntecedencia + " dias de antecedência.");
        }
        
        boolean ocupado = partidaRepository.isTimeOcupadoNoDia(
            dto.getIdTimeDesafiado(), 
            dataJogo.atStartOfDay(), 
            dataJogo.atTime(23, 59, 59), 
            StatusPartida.AGENDADO
        );
        
        if (ocupado) {
            throw new RuntimeException("Este time já possui um jogo confirmado para esta data!");
        }
        
        boolean temPendente = partidaRepository.isTimeOcupadoNoDia(
                dto.getIdTimeDesafiado(), 
                dataJogo.atStartOfDay(), 
                dataJogo.atTime(23, 59, 59), 
                StatusPartida.PENDENTE); 

        if (temPendente) {
            throw new RuntimeException("Já existe um convite pendente com este time nesta data. Use o chat para negociar!");
        }        
        
        Time desafiante = timeRepository.findById(dto.getIdTimeDesafiante()).orElseThrow();
        Time desafiado = timeRepository.findById(dto.getIdTimeDesafiado()).orElseThrow();

        placarPendenteService.validarSemPlacarPendente(desafiante.getId());
        validarPermissaoParaCriarDesafio(desafiante);

        // ==========================================
        // 🚨 TRAVA DE SEGURANÇA: AGENDA DOS TIMES
        // ==========================================
        DayOfWeek diaEscolhido = dto.getDataHoraPartida().getDayOfWeek();
        String diaBanco = traduzirDia(diaEscolhido);

        // Verifica se O SEU TIME joga neste dia
        boolean desafianteJogaNesseDia = desafiante.getAgendas().stream()
            .anyMatch(a -> a.getDiaSemana().equalsIgnoreCase(diaBanco));
        if (!desafianteJogaNesseDia) {
            throw new RuntimeException("O seu time não possui agenda cadastrada para jogar de " + diaBanco + "!");
        }

        // Verifica se O ADVERSÁRIO joga neste dia
        boolean desafiadoJogaNesseDia = desafiado.getAgendas().stream()
            .anyMatch(a -> a.getDiaSemana().equalsIgnoreCase(diaBanco));
        if (!desafiadoJogaNesseDia) {
            throw new RuntimeException("O time " + desafiado.getNome() + " não joga de " + diaBanco + "!");
        }
        // ==========================================

        Partida partida = new Partida();
        Time mandante = definirMandante(desafiante, desafiado);
        Time visitante = mandante.getId().equals(desafiante.getId()) ? desafiado : desafiante;

        partida.setMandante(mandante);
        partida.setVisitante(visitante);
        partida.setStatus(StatusPartida.PENDENTE);
        partida.setDataHora(dto.getDataHoraPartida()); 
        partida.setDataSolicitacao(LocalDateTime.now()); 
        
        partida.setDesafiante(desafiante);
        partida.setMensagem(dto.getMensagem());
        
        partidaRepository.save(partida);
    }

    private PartidaDTO converterParaDTO(Partida p) {
        PartidaDTO dto = new PartidaDTO();
        dto.setId(p.getId());
        dto.setDataHora(p.getDataHora());
        dto.setStatus(p.getStatus());
        dto.setMotivoCancelamento(p.getMotivoCancelamento());
        dto.setDataSolicitacao(p.getDataSolicitacao());

        if (p.getMandante() != null) {
            dto.setMandante(new TimeResumoDTO(
                p.getMandante().getId(), p.getMandante().getNome(), p.getMandante().getCidade(), 
                p.getMandante().getUf(), p.getMandante().getRegiao(), p.getMandante().isMandoCampo()
            ));
        }
        
        if (p.getVisitante() != null) {
            dto.setVisitante(new TimeResumoDTO(
                p.getVisitante().getId(), p.getVisitante().getNome(), p.getVisitante().getCidade(), 
                p.getVisitante().getUf(), p.getVisitante().getRegiao(), p.getVisitante().isMandoCampo()
            ));
        }
        
        if (p.getSolicitanteCancelamento() != null) {
            dto.setSolicitanteCancelamento(new TimeResumoDTO(
                p.getSolicitanteCancelamento().getId(), p.getSolicitanteCancelamento().getNome(), p.getSolicitanteCancelamento().getCidade(), 
                p.getSolicitanteCancelamento().getUf(), p.getSolicitanteCancelamento().getRegiao(), p.getSolicitanteCancelamento().isMandoCampo()
            ));
        }

        return dto;
    }
    
    private Time definirMandante(Time desafiante, Time desafiado) {
        if (desafiante.isMandoCampo() && !desafiado.isMandoCampo()) {
            return desafiante;
        }

        if (desafiado.isMandoCampo() && !desafiante.isMandoCampo()) {
            return desafiado;
        }

        return desafiado;
    }

    private void validarPermissaoParaCriarDesafio(Time desafiante) {
        if (assinaturaService.temAcessoCompleto(desafiante.getResponsavel())) {
            return;
        }

        if (desafiante.getResponsavel() == null
                || desafiante.getResponsavel().getPlanoAssinatura() != PlanoAssinatura.BASICO) {
            assinaturaService.validarAcessoCompleto(desafiante.getResponsavel());
            return;
        }

        int intervaloDias = parametroSistemaService.buscarDiasIntervaloAgendamentoPlanoBasico();
        List<Partida> partidasAtivas = partidaRepository.buscarPartidasFuturasAtivasPorTime(desafiante.getId());
        if (partidasAtivas.isEmpty()) {
            return;
        }

        Partida ultimaPartidaAtiva = partidasAtivas.get(0);
        LocalDate proximaDataPermitida = ultimaPartidaAtiva.getDataHora().toLocalDate().plusDays(intervaloDias);
        LocalDate hoje = LocalDate.now();

        if (hoje.isBefore(proximaDataPermitida)) {
            long diasRestantes = ChronoUnit.DAYS.between(hoje, proximaDataPermitida);
            String diaTexto = diasRestantes == 1 ? "1 dia" : diasRestantes + " dias";
            throw new RuntimeException("Voce ja tem jogo agendado ou desafio enviado! Seu plano BASICO so permite agendar jogos a cada "
                    + intervaloDias + " dias. Voce podera enviar um novo desafio em " + diaTexto + ".");
        }
    }
    
    @Transactional
    public void excluir(Long idPartida) {
        if (!partidaRepository.existsById(idPartida)) {
            throw new RuntimeException("Partida não encontrada para exclusão.");
        }
        // APAGUE ESTA LINHA:
        // try { notificacaoService.deletarNotificacaoJogo(idPartida); } catch(Exception e) {}
        partidaRepository.deleteById(idPartida);
    }
    
    @Transactional
    public void aceitarDesafio(Long idPartida) {
        Partida p = partidaRepository.findById(idPartida).orElseThrow();

        placarPendenteService.validarSemPlacarPendente(p.getMandante().getId());
        placarPendenteService.validarSemPlacarPendente(p.getVisitante().getId());

        boolean mandanteOcupado = partidaRepository.existsByTimeIdAndDataAndStatusAgendado(p.getMandante().getId(), p.getDataHora().toLocalDate());
        boolean visitanteOcupado = partidaRepository.existsByTimeIdAndDataAndStatusAgendado(p.getVisitante().getId(), p.getDataHora().toLocalDate());

        if (mandanteOcupado || visitanteOcupado) {
            p.setStatus(StatusPartida.CANCELADO); 
            partidaRepository.save(p);
            throw new RuntimeException("Não é mais possível aceitar. Um dos times já possui um jogo agendado para esta data!");
        }
        p.setStatus(StatusPartida.AGENDADO);
        partidaRepository.save(p);
    }
    
    @Transactional
    public void cancelarConvitePorId(Long idPartida) {
        partidaRepository.deleteById(idPartida);
    }

    @Transactional
    public void cancelarConvitePorAdversario(Long meuTimeId, Long adversarioId) {
        partidaRepository.deletarConvitePendente(meuTimeId, adversarioId);
    }
    
    @Transactional
    public void informarPlacar(Long idPartida, Integer golsM, Integer golsV, Long idTimeInformante) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida não encontrada"));

        partida.setGolsMandante(golsM);
        partida.setGolsVisitante(golsV);
        placarPendenteService.registrarInformacaoPlacar(partida, idTimeInformante);
        
        partidaRepository.save(partida);

        Long idAdversario = partida.getMandante().getId().equals(idTimeInformante) ? 
                partida.getVisitante().getId() : partida.getMandante().getId();

        Time timeQueInformou = partida.getMandante().getId().equals(idTimeInformante) ? 
                           partida.getMandante() : partida.getVisitante();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dataDoJogo = partida.getDataHora() != null ? partida.getDataHora().format(formatter) : "Data indefinida";
        
        String tituloNotificacao = "Placar: " + timeQueInformou.getNome();
        String subtituloNotificacao = "Jogo do dia " + dataDoJogo + ". Resultado: " + golsM + " x " + golsV + ". Confirma?";
        
        notificacaoService.criarNotificacao(
            idAdversario,         
            "PLACAR",             
            partida.getId(),      
            tituloNotificacao, 
            subtituloNotificacao 
        );
    }

    @Transactional
    public void confirmarPlacar(Long idPartida) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida não encontrada"));

        if (partida.getStatusPlacar() != StatusPlacar.AGUARDANDO_CONFIRMACAO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido para confirmação");
        }

        placarPendenteService.confirmarPlacar(partida);
        
        // 🚨 LIMPA O SININHO DE PLACAR AQUI
        notificacaoService.deletarNotificacaoPlacar(idPartida);
    }

    @Transactional
    public void contestarPlacar(Long idPartida) {
        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida não encontrada"));

        partida.setStatusPlacar(StatusPlacar.EM_DISPUTA);
        partidaRepository.save(partida);
        
        // 🚨 LIMPA O SININHO DE PLACAR AQUI
        notificacaoService.deletarNotificacaoPlacar(idPartida);
    }

    private void atualizarEstatisticasTime(Time time, Integer golsPro, Integer golsContra) {
        time.setPartidasJogadas(time.getPartidasJogadas() + 1);
        time.setGolsPro(time.getGolsPro() + golsPro);
        time.setGolsContra(time.getGolsContra() + golsContra);

        if (golsPro > golsContra) {
            time.setVitorias(time.getVitorias() + 1);
            time.setPontos(time.getPontos() + 3);
        } else if (golsPro.equals(golsContra)) {
            time.setEmpates(time.getEmpates() + 1);
            time.setPontos(time.getPontos() + 1);
        } else {
            time.setDerrotas(time.getDerrotas() + 1);
        }
        
        timeRepository.save(time);
    }
    
    // Método auxiliar para traduzir o dia da semana
    private String traduzirDia(DayOfWeek dia) {
        switch (dia) {
            case MONDAY: return "Segunda";
            case TUESDAY: return "Terça";
            case WEDNESDAY: return "Quarta";
            case THURSDAY: return "Quinta";
            case FRIDAY: return "Sexta";
            case SATURDAY: return "Sábado";
            case SUNDAY: return "Domingo";
            default: return "";
        }
    }
}
