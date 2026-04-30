package br.com.arenamatch.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import jakarta.persistence.Tuple;

@Repository
public interface TimeRepository extends JpaRepository<Time, Long> {
    
    Optional<Time> findByResponsavel(Usuario responsavel);
    
    @Query("SELECT DISTINCT t FROM Time t " +
            "LEFT JOIN t.agendas a " +
            "LEFT JOIN t.ligas l " +
            "WHERE " +
            "(:cidade IS NULL OR LOWER(t.cidade) LIKE :cidade) AND " +
            "(:dia IS NULL OR a.diaSemana = :dia) AND " +
            "(:categoria IS NULL OR a.categoria = :categoria) AND " +
            "(:nomeTime IS NULL OR LOWER(t.nome) LIKE :nomeTime) AND " +
            "(:nomeLiga IS NULL OR LOWER(l.nome) LIKE :nomeLiga)")
     List<Time> buscarPorFiltros(
             @Param("cidade") String cidade, 
             @Param("dia") String dia, 
             @Param("categoria") Enum categoria,
             @Param("nomeTime") String nomeTime,
             @Param("nomeLiga") String nomeLiga
     );
    
    // --- QUERY CORRIGIDA E BLINDADA ---
    @Query(value = """
            SELECT 
                t.id AS id, 
                t.nome AS nome, 
                t.cidade AS cidade, 
                t.uf AS uf, 
                t.regiao as regiao,
                t.mando_campo as casa,
                t.valor_taxa as taxa,
                (6371 * acos(cos(radians(:minhaLat)) * cos(radians(t.latitude)) * cos(radians(t.longitude) - radians(:minhaLon)) + sin(radians(:minhaLat)) * sin(radians(t.latitude)))) AS distancia,
                (SELECT count(*) FROM partida p 
                 WHERE ((p.id_mandante = t.id AND p.id_visitante = :meuId) OR (p.id_mandante = :meuId AND p.id_visitante = t.id)) 
                 AND DATE(p.data_hora) = :dataBusca 
                 AND p.status = 'PENDENTE') AS convitePendente,
                 a.dia_semana as diaSemana,
                 a.hora_inicio as horaInicio,
                 a.hora_fim as horaFim,
                 a.categoria as categoria                
            FROM time t
            INNER JOIN agenda a ON a.id_time = t.id
            WHERE t.id != :meuId
            AND a.dia_semana = :diaSemana
            AND t.mando_campo != :minhaCasa
            AND (:nome IS NULL OR LOWER(t.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
            AND (6371 * acos(cos(radians(:minhaLat)) * cos(radians(t.latitude)) * cos(radians(t.longitude) - radians(:minhaLon)) + sin(radians(:minhaLat)) * sin(radians(t.latitude)))) <= :raio
            AND NOT EXISTS (
                SELECT 1 FROM partida p 
                WHERE (p.id_mandante = t.id OR p.id_visitante = t.id)
                AND DATE(p.data_hora) = :dataBusca
                AND p.status = 'AGENDADO' 
            )
            AND (:categoria IS NULL OR a.categoria = :categoria)
        """, nativeQuery = true)
        List<Tuple> buscarPorLocalizacaoEDia(
            @Param("minhaLat") Double minhaLat,
            @Param("minhaLon") Double minhaLon,
            @Param("raio") Double raio,
            @Param("nome") String nome, 
            @Param("meuId") Long meuId,
            @Param("dataBusca") java.time.LocalDate dataBusca,
            @Param("diaSemana") String diaSemana,
            @Param("minhaCasa") Boolean minhaCasa,
            @Param("categoria") String categoria
        );
    
    Optional<Time> findByResponsavelId(Long idResponsavel);
    
    List<Time> findByNomeContainingIgnoreCase(String nome);
    
    @Query("SELECT t FROM Time t ORDER BY t.vitorias DESC, (t.golsPro - t.golsContra) DESC, t.golsPro DESC")
    List<Time> buscarRankingGeral();
}