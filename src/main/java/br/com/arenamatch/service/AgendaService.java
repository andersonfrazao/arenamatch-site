package br.com.arenamatch.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.arenamatch.dto.EventoAgendaDTO;
import br.com.arenamatch.dto.ResumoAgendaDTO;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.enums.StatusPlacar;
import br.com.arenamatch.repository.PartidaRepository;

@Service
public class AgendaService {

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private HorarioJogoService horarioJogoService;

    public List<ResumoAgendaDTO> montarCalendario(Long timeId, LocalDate dataInicio, int dias) {
        List<ResumoAgendaDTO> calendario = new ArrayList<>();
        LocalDate dataFim = dataInicio.plusDays(dias - 1);

        // Busca partidas onde sou Mandante OU Visitante
        List<Partida> partidas = partidaRepository.buscarPartidasPorIntervalo(
                timeId, dataInicio.atStartOfDay(), dataFim.atTime(23, 59, 59));

        Map<LocalDate, List<Partida>> mapaPartidas = partidas.stream()
                .collect(Collectors.groupingBy(p -> p.getDataHora().toLocalDate()));

        for (int i = 0; i < dias; i++) {
            LocalDate dataAtual = dataInicio.plusDays(i);
            ResumoAgendaDTO diaDto = new ResumoAgendaDTO();
            
            diaDto.setData(dataAtual);
            diaDto.setDiaMes(String.format("%02d", dataAtual.getDayOfMonth()));
            
            // Formata dia da semana (Ex: "Seg", "Ter")
            String nomeDia = dataAtual.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"))
                    .replace(".", ""); 
            // Capitaliza a primeira letra (seg -> Seg)
            diaDto.setDiaSemana(nomeDia.substring(0, 1).toUpperCase() + nomeDia.substring(1));

            if (mapaPartidas.containsKey(dataAtual)) {
                List<Partida> doDia = mapaPartidas.get(dataAtual);
                
                // Bolinhas do Calendário
                boolean temConfirmado = doDia.stream().anyMatch(p -> p.getStatus() == StatusPartida.AGENDADO
                        && p.getDataHora() != null
                        && p.getDataHora().isAfter(LocalDateTime.now()));
                boolean temPendente = doDia.stream().anyMatch(p -> p.getStatus() == StatusPartida.PENDENTE);
                boolean temCancelado = doDia.stream().anyMatch(p -> p.getStatus() == StatusPartida.CANCELADO
                        || p.getStatus() == StatusPartida.SOLICITACAO_CANCELAMENTO);
                boolean temPlacarPendente = doDia.stream().anyMatch(p -> p.getStatus() == StatusPartida.AGENDADO
                        && p.getDataHora() != null
                        && p.getDataHora().isBefore(LocalDateTime.now())
                        && (p.getStatusPlacar() == null
                            || p.getStatusPlacar() == StatusPlacar.PENDENTE
                            || p.getStatusPlacar() == StatusPlacar.AGUARDANDO_CONFIRMACAO));
                boolean temJogoRealizado = !temPlacarPendente && doDia.stream().anyMatch(p -> p.getStatus() == StatusPartida.AGENDADO
                        && p.getDataHora() != null
                        && p.getDataHora().isBefore(LocalDateTime.now())
                        && p.getStatusPlacar() == StatusPlacar.CONFIRMADO);

                diaDto.setTemJogoConfirmado(temConfirmado);
                diaDto.setTemDesafioPendente(temPendente);
                diaDto.setTemCancelado(temCancelado); // Define a bolinha vermelha
                diaDto.setTemJogoRealizado(temJogoRealizado);
                diaDto.setTemPlacarPendente(temPlacarPendente);
            }

            calendario.add(diaDto);
        }

        return calendario;
    }

    public List<EventoAgendaDTO> buscarDetalhesDoDia(Long timeId, LocalDate data) {
        List<Partida> partidas = partidaRepository.buscarPartidasPorIntervalo(
                timeId, data.atStartOfDay(), data.atTime(23, 59, 59));
        
        return partidas.stream()
                .map(p -> converterParaEvento(p, timeId))
                .collect(Collectors.toList());
    }

 // ... restante do seu código (montarCalendario) ...

    private EventoAgendaDTO converterParaEvento(Partida p, Long meuTimeId) {
        EventoAgendaDTO dto = new EventoAgendaDTO();
        dto.setIdPartida(p.getId());
        dto.setDataHora(horarioJogoService.resolverDataHoraMandante(p));
        dto.setMensagem(p.getMensagem()); 

        // Identifica adversário
        boolean souMandante = p.getMandante().getId().equals(meuTimeId);
        Time adversario = souMandante ? p.getVisitante() : p.getMandante();
        Time meuTime = souMandante ? p.getMandante() : p.getVisitante(); 
        dto.setPosicaoAdversario(adversario.isMandoCampo() ? "Mandante" : "Visitante");

        Time donoDoCampo = definirDonoDoCampo(p);
                
        dto.setIdTimeAdversario(adversario.getId());
        dto.setCidade(adversario.getCidade());
        dto.setValorTaxa(donoDoCampo != null ? donoDoCampo.getValorTaxa() : null);
        dto.setTemCampo(donoDoCampo != null && donoDoCampo.getId().equals(meuTimeId)); 
        
        // Tratamento do Endereço (Mantido)
        List<String> partes = new ArrayList<>();
        if (donoDoCampo != null && donoDoCampo.getLogradouro() != null && !donoDoCampo.getLogradouro().trim().isEmpty()) {
            partes.add(donoDoCampo.getLogradouro().trim());
        }
        if (donoDoCampo != null && donoDoCampo.getNumero() != null && !donoDoCampo.getNumero().trim().isEmpty()) {
            partes.add(donoDoCampo.getNumero().trim());
        }
        if (donoDoCampo != null && donoDoCampo.getBairro() != null && !donoDoCampo.getBairro().trim().isEmpty()) {
            partes.add(donoDoCampo.getBairro().trim());
        }
        if (donoDoCampo != null && donoDoCampo.getCidade() != null && !donoDoCampo.getCidade().trim().isEmpty()) {
            String cidadeUf = donoDoCampo.getCidade().trim();
            if (donoDoCampo.getUf() != null && !donoDoCampo.getUf().trim().isEmpty()) {
                cidadeUf += "/" + donoDoCampo.getUf().trim();
            }
            partes.add(cidadeUf);
        }
        dto.setEndereco(!partes.isEmpty() ? String.join(", ", partes) : "Endereço não cadastrado");

        // Cálculo de Distância (Mantido)
        if (meuTime.getLatitude() != null && adversario.getLatitude() != null) {
            double dist = calcularDistancia(meuTime.getLatitude(), meuTime.getLongitude(), adversario.getLatitude(), adversario.getLongitude());
            dto.setDistanciaKm(Math.round(dist * 10.0) / 10.0);
        }

        // Títulos e Status
        if (p.getStatus() == StatusPartida.AGENDADO) {
            dto.setTipo("GAME");
            dto.setTitulo("Contra " + adversario.getNome());
        } else if (p.getStatus() == StatusPartida.PENDENTE) {
            boolean fuiEuQueConvidei = p.getDesafiante() != null && p.getDesafiante().getId().equals(meuTimeId);
            dto.setTipo(fuiEuQueConvidei ? "INVITE_SENT" : "INVITE_RECEIVED");
            dto.setTitulo((fuiEuQueConvidei ? "Para " : "De ") + adversario.getNome());
        } else if (p.getStatus() == StatusPartida.CANCELADO) {
            dto.setTipo("CANCELLED");
            dto.setTitulo("Cancelado: " + adversario.getNome());
        } else if (p.getStatus() == StatusPartida.SOLICITACAO_CANCELAMENTO) {
            boolean fuiEuQuePedi = p.getSolicitanteCancelamento() != null && p.getSolicitanteCancelamento().getId().equals(meuTimeId);
            dto.setTipo(fuiEuQuePedi ? "CANCELLATION_SENT" : "CANCELLATION_RECEIVED");
            dto.setTitulo(fuiEuQuePedi ? "Cancelamento Solicitado" : "Atenção: " + adversario.getNome() + " quer cancelar!");
            dto.setMensagem(fuiEuQuePedi ? "Seu motivo: " + p.getMotivoCancelamento() : "Motivo deles: " + p.getMotivoCancelamento());
        }

        // 🏆 LÓGICA DE PLACAR E PÓS-JOGO LIMPA 🏆
        if (p.getMandante() != null) dto.setNomeTimeMandante(p.getMandante().getNome());
        if (p.getVisitante() != null) dto.setNomeTimeVisitante(p.getVisitante().getNome());
        
        dto.setPassouDaHora(p.getDataHora() != null && p.getDataHora().isBefore(LocalDateTime.now()));
        
        dto.setStatusPlacar(p.getStatusPlacar() != null ? p.getStatusPlacar().name() : "PENDENTE");
        dto.setGolsMandante(p.getGolsMandante());
        dto.setGolsVisitante(p.getGolsVisitante());
        dto.setIdTimeQueInformou(p.getIdTimeQueInformou());

        // Lógica de quem informou
        if (p.getIdTimeQueInformou() != null) {
            boolean fuiEu = p.getIdTimeQueInformou().equals(meuTimeId);
            dto.setEuInformeiOPlacar(fuiEu);
            dto.setAdversarioInformouOPlacar(!fuiEu);
        } else {
            dto.setEuInformeiOPlacar(false);
            dto.setAdversarioInformouOPlacar(false);
        }

        return dto;
    }

    // Função auxiliar matemática
    private Time definirDonoDoCampo(Partida partida) {
        if (partida.getMandante() != null && partida.getMandante().isMandoCampo()) {
            return partida.getMandante();
        }

        if (partida.getVisitante() != null && partida.getVisitante().isMandoCampo()) {
            return partida.getVisitante();
        }

        return partida.getMandante();
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raio da Terra em Km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
