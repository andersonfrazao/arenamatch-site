package br.com.arenamatch.service;

import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusPlacar;
import br.com.arenamatch.repository.NotificacaoRepository;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlacarPendenteService {

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PartidaRepository partidaRepository;
    private final TimeRepository timeRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final ParametroSistemaService parametroSistemaService;

    public PlacarPendenteService(PartidaRepository partidaRepository,
            TimeRepository timeRepository,
            NotificacaoRepository notificacaoRepository,
            ParametroSistemaService parametroSistemaService) {
        this.partidaRepository = partidaRepository;
        this.timeRepository = timeRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.parametroSistemaService = parametroSistemaService;
    }

    @Transactional
    public List<Partida> buscarPendencias(Long timeId) {
        if (timeId == null) {
            return List.of();
        }

        confirmarPlacaresExpirados(timeId);

        List<Partida> pendencias = new ArrayList<>();
        pendencias.addAll(partidaRepository.buscarJogosRealizadosComPlacarPendente(timeId));
        pendencias.addAll(partidaRepository.buscarPlacaresAguardandoAcaoDoTime(timeId));
        return pendencias;
    }

    @Transactional
    public void validarSemPlacarPendente(Long timeId) {
        List<Partida> pendencias = buscarPendencias(timeId);
        if (pendencias.isEmpty()) {
            return;
        }

        Partida partida = pendencias.get(0);
        String dataJogo = partida.getDataHora() != null
                ? partida.getDataHora().format(DATA_FORMATTER)
                : "data anterior";

        if (partida.getStatusPlacar() == StatusPlacar.AGUARDANDO_CONFIRMACAO) {
            int diasConfirmacao = parametroSistemaService.buscarDiasConfirmacaoAutomaticaPlacar();
            throw new RuntimeException("O adversario informou o placar do jogo do dia "
                    + dataJogo + ". Confirme ou conteste o resultado na agenda para continuar usando este recurso. Se voce nao responder em "
                    + diasConfirmacao + " dias, o placar informado sera confirmado automaticamente.");
        }

        throw new RuntimeException("Voce precisa informar o placar do jogo do dia "
                + dataJogo + " antes de continuar usando este recurso. Acesse sua agenda e informe o resultado para manter o ranking atualizado.");
    }

    @Transactional
    public void registrarInformacaoPlacar(Partida partida, Long idTimeInformante) {
        partida.setIdTimeQueInformou(idTimeInformante);
        partida.setDataInformacaoPlacar(LocalDateTime.now());
        partida.setStatusPlacar(StatusPlacar.AGUARDANDO_CONFIRMACAO);
    }

    @Transactional
    public void confirmarPlacar(Partida partida) {
        atualizarEstatisticasTime(partida.getMandante(), partida.getGolsMandante(), partida.getGolsVisitante());
        atualizarEstatisticasTime(partida.getVisitante(), partida.getGolsVisitante(), partida.getGolsMandante());

        partida.setStatusPlacar(StatusPlacar.CONFIRMADO);
        partidaRepository.save(partida);
        notificacaoRepository.deletarPorReferenciaETipo(partida.getId(), "PLACAR");
    }

    private void confirmarPlacaresExpirados(Long timeId) {
        int diasConfirmacao = parametroSistemaService.buscarDiasConfirmacaoAutomaticaPlacar();
        LocalDateTime limite = LocalDateTime.now().minusDays(diasConfirmacao);

        for (Partida partida : partidaRepository.buscarPlacaresAguardandoAcaoDoTime(timeId)) {
            LocalDateTime dataInformacao = partida.getDataInformacaoPlacar();
            if (dataInformacao != null && !dataInformacao.isAfter(limite)) {
                confirmarPlacar(partida);
            }
        }
    }

    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void confirmarPlacaresExpiradosAutomaticamente() {
        int diasConfirmacao = parametroSistemaService.buscarDiasConfirmacaoAutomaticaPlacar();
        LocalDateTime limite = LocalDateTime.now().minusDays(diasConfirmacao);

        for (Partida partida : partidaRepository.buscarPlacaresComConfirmacaoAutomaticaExpirada(limite)) {
            confirmarPlacar(partida);
        }
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
}
