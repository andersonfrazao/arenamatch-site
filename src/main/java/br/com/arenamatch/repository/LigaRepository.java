package br.com.arenamatch.repository;

import br.com.arenamatch.entity.Liga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LigaRepository extends JpaRepository<Liga, Long> {
    
    // Buscar Ligas que o time é dono
    List<Liga> findByAdminId(Long adminId);
    
    // Buscar Ligas que o time faz parte (como membro)
    @Query("SELECT l FROM Liga l JOIN l.times t WHERE t.id = :timeId")
    List<Liga> buscarLigasDoTime(@Param("timeId") Long timeId);
    
 // 1. Busca as "Top Ligas" ordenadas pela quantidade de times (Limita a 10 ou 20 no Service)
    @Query("SELECT l FROM Liga l ORDER BY SIZE(l.times) DESC")
    List<Liga> buscarLigasMaisMovimentadas();

    // 2. Busca ligas digitadas na barra de pesquisa
    @Query("SELECT l FROM Liga l WHERE LOWER(l.nome) LIKE LOWER(CONCAT('%', :nome, '%')) ORDER BY SIZE(l.times) DESC")
    List<Liga> buscarLigasPorNome(@Param("nome") String nome);
    
}