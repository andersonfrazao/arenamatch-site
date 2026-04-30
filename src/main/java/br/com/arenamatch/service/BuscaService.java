package br.com.arenamatch.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.arenamatch.dto.FiltroBuscaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;

@Service
public class BuscaService {

    @Autowired private TimeRepository timeRepository;
    @Autowired private PartidaRepository partidaRepo;
    @Autowired private UsuarioRepository usuarioRepo; 
    @Autowired private DistanciaService distanciaService;

    public List<TimeResumoDTO> buscar(FiltroBuscaDTO filtro) {
        return new ArrayList<>(); 
    }

    private String capitalizarDia(String dia) {
        if(dia == null) return null;
        String limpo = dia.split("-")[0];
        return limpo.substring(0, 1).toUpperCase() + limpo.substring(1).toLowerCase();
    }
    
    public List<TimeResumoDTO> buscarTimesDisponiveis(LocalDate data, Double raio, String cidade, String nome, Long idMeuTime, Categoria categoria) {
        if (data.isBefore(LocalDate.now())) {
            throw new RuntimeException("A data da busca não pode ser inferior à data atual.");
        }

        try {
            DayOfWeek diaSemanaJava = data.getDayOfWeek();
            String diaSemanaBanco = traduzirDia(diaSemanaJava);

            Time meuTime = timeRepository.findById(idMeuTime)
                .orElseThrow(() -> new RuntimeException("Time logado não encontrado"));

            boolean euJogoNesseDia = meuTime.getAgendas().stream()
                .anyMatch(agenda -> agenda.getDiaSemana().equalsIgnoreCase(diaSemanaBanco));

            if (!euJogoNesseDia) {
                return new ArrayList<>(); 
            }

            // 🚨 PASSANDO A NOVA VARIÁVEL: meuTime.isMandoCampo()
            List<Tuple> resultados = timeRepository.buscarPorLocalizacaoEDia(
                    meuTime.getLatitude(), 
                    meuTime.getLongitude(), 
                    raio, 
                    nome, 
                    meuTime.getId(),
                    data,
                    diaSemanaBanco, 
                    meuTime.isMandoCampo(), // O filtro que traz apenas o "oposto"
                    categoria != null ? categoria.name() : null
            );

            return resultados.stream().map(t -> {
                java.math.BigDecimal taxaBd = t.get("taxa", java.math.BigDecimal.class);
                double valorTaxa = (taxaBd != null) ? taxaBd.doubleValue() : 0.0;
                
                Number distNum = t.get("distancia", Number.class);
                double valorDistancia = (distNum != null) ? distNum.doubleValue() : 0.0;

                Number countConvite = t.get("convitePendente", Number.class);
                boolean isPendente = (countConvite != null && countConvite.intValue() > 0);
                
                String categoriaStr = t.get("categoria", String.class);
                Categoria categoriaEnum = null;
                if (categoriaStr != null && !categoriaStr.trim().isEmpty()) {
                    categoriaEnum = Categoria.valueOf(categoriaStr); 
                }

                Long idAdversario = t.get("id", Long.class);

                TimeResumoDTO dto = new TimeResumoDTO(
                    idAdversario, t.get("nome", String.class), t.get("cidade", String.class),
                    t.get("uf", String.class), t.get("regiao", String.class), valorDistancia,
                    t.get("casa", Boolean.class), valorTaxa, 0L, isPendente, categoriaEnum
                );

                // --- 🚨 FORMATANDO O HORÁRIO PARA A TELA ---
                Object horaInicioObj = t.get("horaInicio");
                Object horaFimObj = t.get("horaFim");
                String horarioAgenda = diaSemanaBanco; // Default

                if (horaInicioObj != null && horaFimObj != null) {
                    // Pega apenas o HH:mm (Ignora os segundos e resolve bugs de drivers SQL diferentes)
                    String hInicio = horaInicioObj.toString().substring(0, 5);
                    String hFim = horaFimObj.toString().substring(0, 5);
                    horarioAgenda = diaSemanaBanco + " das " + hInicio + " às " + hFim;
                }
                
                // Reaproveitamos este campo existente no DTO para mostrar o horário no Card!
                dto.setDiasDaSemanaTexto(horarioAgenda);

                // --- A MÁGICA DO CHAT E DO CONVITE ---
                if (isPendente) {
                    java.time.LocalDateTime inicioDia = data.atStartOfDay();
                    java.time.LocalDateTime fimDia = data.atTime(23, 59, 59);
                    try {
                        Partida partida = partidaRepo.buscarPartidaPendente(meuTime.getId(), idAdversario, inicioDia, fimDia);
                        if (partida != null) {
                            dto.setIdPartidaPendente(partida.getId());
                            if (partida.getDesafiante() != null && !partida.getDesafiante().getId().equals(meuTime.getId())) {
                                dto.setConviteRecebido(true);
                            } else {
                                dto.setConviteRecebido(false);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Aviso: Não foi possível carregar a partida pendente para o time " + idAdversario);
                    }
                }
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar times disponíveis: " + e.getMessage());
        }
    }

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
