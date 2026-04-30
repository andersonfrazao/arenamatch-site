package br.com.arenamatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.arenamatch.entity.MensagemChat;

public interface MensagemChatRepository extends JpaRepository<MensagemChat, Long> {

    // Busca o histórico do chat ordenado cronologicamente
    List<MensagemChat> findByPartidaIdOrderByDataHoraAsc(Long idPartida);
    
 // O Spring Data faz a mágica de pegar só a última (First) ordenando pela data de forma decrescente (Desc)
    MensagemChat findFirstByPartidaIdOrderByDataHoraDesc(Long idPartida);
    
 // Conta quantas mensagens eu recebi (o remetente não sou eu) e que estão com lida = false
    @Query("SELECT COUNT(m) FROM MensagemChat m WHERE (m.partida.mandante.id = :meuTimeId OR m.partida.visitante.id = :meuTimeId) AND m.remetente.id != :meuTimeId AND m.lida = false")
    Long contarMensagensNaoLidasGeral(@org.springframework.data.repository.query.Param("meuTimeId") Long meuTimeId);

    // Atualiza as mensagens de uma conversa específica para lida = true
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE MensagemChat m SET m.lida = true WHERE m.partida.id = :idPartida AND m.remetente.id != :meuTimeId AND m.lida = false")
    void marcarMensagensComoLidas(@org.springframework.data.repository.query.Param("idPartida") Long idPartida, @org.springframework.data.repository.query.Param("meuTimeId") Long meuTimeId);
    
    @Query("SELECT COUNT(m) FROM MensagemChat m WHERE m.partida.id = :idPartida AND m.remetente.id != :meuTimeId AND m.lida = false")
    Long contarNaoLidasPorPartida(@org.springframework.data.repository.query.Param("idPartida") Long idPartida, @org.springframework.data.repository.query.Param("meuTimeId") Long meuTimeId);
    
}