package br.com.arenamatch.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusPartida;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {
    
    // Busca partidas onde o time é mandante OU visitante
    @Query("SELECT p FROM Partida p WHERE p.mandante = :time OR p.visitante = :time ORDER BY p.dataHora ASC")
    List<Partida> buscarPorTime(@Param("time") Time time);
    
 // Verifica se o time tem jogo marcado num intervalo de tempo (no dia específico)
    @Query("SELECT COUNT(p) > 0 FROM Partida p " +
           "WHERE (p.mandante = :time OR p.visitante = :time) " +
           "AND p.dataHora BETWEEN :inicio AND :fim " +
           "AND p.status <> 'CANCELADO'")
    boolean existePartidaNessaData(@Param("time") Time time, 
                                   @Param("inicio") LocalDateTime inicio,
                                   @Param("fim") LocalDateTime fim);
    
    
    @Query("SELECT p FROM Partida p WHERE " +
    	       "(p.mandante.id = :timeId OR p.visitante.id = :timeId) AND " +
    	       "p.dataHora BETWEEN :inicio AND :fim ORDER BY p.dataHora")
    	List<Partida> buscarPartidasPorIntervalo(@Param("timeId") Long timeId, 
    	                                         @Param("inicio") LocalDateTime inicio, 
    	                                         @Param("fim") LocalDateTime fim);
    
    @Query("SELECT COUNT(p) > 0 FROM Partida p WHERE (p.mandante.id = :timeId OR p.visitante.id = :timeId) " +
            "AND p.dataHora >= :inicioDoDia AND p.dataHora <= :fimDoDia AND p.status = :status")
     boolean isTimeOcupadoNoDia(@Param("timeId") Long timeId, 
                                @Param("inicioDoDia") LocalDateTime inicioDoDia,
                                @Param("fimDoDia") LocalDateTime fimDoDia,
                                @Param("status") StatusPartida status);
    
    
 // Busca convites PENDENTES onde o time logado é Mandante (enviou) OU Visitante (recebeu)
    @Query("SELECT p FROM Partida p WHERE (p.visitante.id = :timeId OR p.mandante.id = :timeId) AND p.status = 'PENDENTE'")
    List<Partida> buscarConvitesPendentesParaOTime(@Param("timeId") Long timeId);
    
 // =======================================================================
    // VALIDAÇÃO DE CHOQUE DE AGENDA (DOUBLE BOOKING)
    // Verifica se o time já tem um jogo 'AGENDADO' na mesma data (como mandante ou visitante)
    // =======================================================================
    @Query("""
        SELECT COUNT(p) > 0 FROM Partida p 
        WHERE (p.mandante.id = :timeId OR p.visitante.id = :timeId) 
        AND p.status = 'AGENDADO' 
        AND FUNCTION('DATE', p.dataHora) = :dataBusca
    """)
    boolean existsByTimeIdAndDataAndStatusAgendado(
        @Param("timeId") Long timeId, 
        @Param("dataBusca") java.time.LocalDate dataBusca
    );
    
    @Modifying
    @Query("DELETE FROM Partida p WHERE p.visitante.id = :meuTimeId AND p.mandante.id = :adversarioId AND p.status = 'PENDENTE'")
    void deletarConvitePendente(@Param("meuTimeId") Long meuTimeId, @Param("adversarioId") Long adversarioId);
    
    @Query("""
            SELECT COUNT(p) > 0 FROM Partida p 
            WHERE (p.mandante.id = :timeId OR p.visitante.id = :timeId) 
            AND p.status IN ('PENDENTE', 'AGENDADO') 
            AND p.dataHora > CURRENT_TIMESTAMP
        """)
        boolean existemJogosFuturosPendentesOuAgendados(@Param("timeId") Long timeId);
    
    @Query("SELECT p FROM Partida p WHERE (p.mandante.id = :timeId OR p.visitante.id = :timeId) AND p.status IN ('PENDENTE', 'AGENDADO') ORDER BY p.dataHora ASC")
    List<Partida> findPartidasAtivasPorTime(@Param("timeId") Long timeId);

    @Query("""
            SELECT p FROM Partida p
            WHERE (p.mandante.id = :timeId OR p.visitante.id = :timeId)
            AND p.status IN ('PENDENTE', 'AGENDADO')
            AND p.dataHora > CURRENT_TIMESTAMP
            ORDER BY p.dataHora DESC
        """)
    List<Partida> buscarPartidasFuturasAtivasPorTime(@Param("timeId") Long timeId);

    @Query("""
            SELECT p FROM Partida p
            WHERE (p.mandante.id = :timeId OR p.visitante.id = :timeId)
            AND p.status = 'AGENDADO'
            AND p.statusPlacar = 'PENDENTE'
            AND p.dataHora < CURRENT_TIMESTAMP
            ORDER BY p.dataHora ASC
        """)
    List<Partida> buscarJogosRealizadosComPlacarPendente(@Param("timeId") Long timeId);

    @Query("""
            SELECT p FROM Partida p
            WHERE (p.mandante.id = :timeId OR p.visitante.id = :timeId)
            AND p.status = 'AGENDADO'
            AND p.statusPlacar = 'AGUARDANDO_CONFIRMACAO'
            AND p.idTimeQueInformou <> :timeId
            ORDER BY p.dataInformacaoPlacar ASC
        """)
    List<Partida> buscarPlacaresAguardandoAcaoDoTime(@Param("timeId") Long timeId);

    @Query("""
            SELECT p FROM Partida p
            WHERE p.status = 'AGENDADO'
            AND p.statusPlacar = 'AGUARDANDO_CONFIRMACAO'
            AND p.dataInformacaoPlacar <= :limite
            ORDER BY p.dataInformacaoPlacar ASC
        """)
    List<Partida> buscarPlacaresComConfirmacaoAutomaticaExpirada(@Param("limite") LocalDateTime limite);
    
    @Query("""
            SELECT p.id FROM Partida p 
            WHERE ((p.mandante.id = :meuTimeId AND p.visitante.id = :adversarioId) 
               OR (p.mandante.id = :adversarioId AND p.visitante.id = :meuTimeId)) 
            AND p.dataHora >= :inicioDia AND p.dataHora <= :fimDia 
            AND p.status IN ('PENDENTE', 'AGENDADO')
        """)
        Long buscarIdPartidaPendente(
            @Param("meuTimeId") Long meuTimeId,
            @Param("adversarioId") Long adversarioId,
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("fimDia") LocalDateTime fimDia
        );
    
    @Query("""
            SELECT p FROM Partida p 
            WHERE ((p.mandante.id = :meuTimeId AND p.visitante.id = :adversarioId) 
               OR (p.mandante.id = :adversarioId AND p.visitante.id = :meuTimeId)) 
            AND p.dataHora >= :inicioDia AND p.dataHora <= :fimDia 
            AND p.status IN ('PENDENTE', 'AGENDADO')
        """)
        Partida buscarPartidaPendente(
            @Param("meuTimeId") Long meuTimeId,
            @Param("adversarioId") Long adversarioId,
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("fimDia") LocalDateTime fimDia
        );
    
    @Query("SELECT p FROM Partida p WHERE p.mandante.id = :idTime OR p.visitante.id = :idTime")
    List<Partida> buscarPartidasParaChat(@Param("idTime") Long idTime);
    
}
