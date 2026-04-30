package br.com.arenamatch.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.arenamatch.dto.DashboardDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;

@Service
public class DashboardService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TimeRepository timeRepository;

    public Optional<DashboardDTO> carregarDadosDashboard(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .map(this::montarDashboard);
    }

    private DashboardDTO montarDashboard(Usuario usuario) {
        DashboardDTO dto = new DashboardDTO();

        if (usuario.getDataExpiracao() != null) {
            long dias = ChronoUnit.DAYS.between(LocalDateTime.now(), usuario.getDataExpiracao());
            dto.setDiasRestantesTrial(Math.max(dias, 0));
        }

        timeRepository.findByResponsavel(usuario)
                .map(this::converterParaResumoDTO)
                .ifPresent(dto::setMeuTime);

        return dto;
    }

    private TimeResumoDTO converterParaResumoDTO(Time time) {
        return new TimeResumoDTO(
                time.getId(),
                time.getNome(),
                time.getCidade(),
                time.getUf(),
                time.getRegiao(),
                time.isMandoCampo()
        );
    }
}
