package br.com.arenamatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.arenamatch.entity.Notificacao; // Ajuste o pacote se a sua entidade estiver em outro lugar

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    // Essa linha mágica faz um "SELECT * FROM notificacao WHERE time_id = ? ORDER BY data_criacao DESC"
    List<Notificacao> findByTimeIdOrderByDataCriacaoDesc(Long timeId);
    
    /* * DICA IMPORTANTE: 
     * Se na sua classe Notificacao.java a variável se chamar 'time' (ao invés de 'timeId'), 
     * você deve mudar o nome do método acima para:
     * List<Notificacao> findByTime_IdOrderByDataCriacaoDesc(Long timeId);
     */
    
    void deleteByIdReferenciaAndTipo(Long idReferencia, String tipo);
    
 // NOVO: Comando para apagar a notificação após a ação
    @Modifying
    @Query("DELETE FROM Notificacao n WHERE n.idReferencia = :idReferencia AND n.tipo = :tipo")
    void deletarPorReferenciaETipo(@Param("idReferencia") Long idReferencia, @Param("tipo") String tipo);
    
    
}