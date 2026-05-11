package br.com.arenamatch.service;

import br.com.arenamatch.entity.Agenda;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.repository.AgendaRepository;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Service;

@Service
public class HorarioJogoService {

    private final AgendaRepository agendaRepository;

    public HorarioJogoService(AgendaRepository agendaRepository) {
        this.agendaRepository = agendaRepository;
    }

    public LocalDateTime resolverDataHoraMandante(Partida partida) {
        if (partida == null || partida.getDataHora() == null || partida.getMandante() == null) {
            return partida != null ? partida.getDataHora() : null;
        }

        String diaBanco = traduzirDia(partida.getDataHora().getDayOfWeek());
        return agendaRepository.findFirstByTimeIdAndDiaSemanaOrderByHoraInicioAsc(partida.getMandante().getId(), diaBanco)
                .map(Agenda::getHoraInicio)
                .map(LocalTime::parse)
                .map(horaInicio -> partida.getDataHora().toLocalDate().atTime(horaInicio))
                .orElse(partida.getDataHora());
    }

    private String traduzirDia(DayOfWeek dia) {
        switch (dia) {
            case MONDAY: return "Segunda";
            case TUESDAY: return "TerÃ§a";
            case WEDNESDAY: return "Quarta";
            case THURSDAY: return "Quinta";
            case FRIDAY: return "Sexta";
            case SATURDAY: return "SÃ¡bado";
            case SUNDAY: return "Domingo";
            default: return "";
        }
    }
}
