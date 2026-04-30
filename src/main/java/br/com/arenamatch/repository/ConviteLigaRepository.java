package br.com.arenamatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.arenamatch.entity.ConviteLiga;
import br.com.arenamatch.enums.StatusConviteLiga;

public interface ConviteLigaRepository extends JpaRepository<ConviteLiga, Long> {
    
    // Para listar na tela do time os convites que ele recebeu
    List<ConviteLiga> findByTimeConvidadoIdAndStatus(Long timeId, StatusConviteLiga status);
    
    // Trava de segurança: saber se já enviou convite pendente para não gerar spam
    boolean existsByLigaIdAndTimeConvidadoIdAndStatus(Long ligaId, Long timeId, StatusConviteLiga status);
    
    List<ConviteLiga> findByTimeConvidadoIdAndStatusIn(Long timeId, List<StatusConviteLiga> status);
    
    @Query("SELECT c.timeConvidado.id FROM ConviteLiga c WHERE c.liga.id = :ligaId AND c.status = 'PENDENTE'")
    List<Long> findIdsTimesComConvitePendenteNaLiga(@Param("ligaId") Long ligaId);
}