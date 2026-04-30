package br.com.arenamatch.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.arenamatch.entity.MensagemChatLiga;

public interface MensagemChatLigaRepository extends JpaRepository<MensagemChatLiga, Long> {

    // Busca o histórico ordenado
    List<MensagemChatLiga> findByLigaIdOrderByDataHoraAsc(Long idLiga);

    // Pega a última mensagem para mostrar na lista de conversas
    MensagemChatLiga findFirstByLigaIdOrderByDataHoraDesc(Long idLiga);

    // Conta não lidas para o administrador da Liga ou para o Time convidado
    @Query("SELECT COUNT(m) FROM MensagemChatLiga m WHERE m.liga.id = :idLiga AND m.remetente.id != :meuTimeId AND m.lida = false")
    Long contarNaoLidasPorLiga(@Param("idLiga") Long idLiga, @Param("meuTimeId") Long meuTimeId);

    // Marca como lidas
    @Modifying
    @Query("UPDATE MensagemChatLiga m SET m.lida = true WHERE m.liga.id = :idLiga AND m.remetente.id != :meuTimeId AND m.lida = false")
    void marcarMensagensComoLidas(@Param("idLiga") Long idLiga, @Param("meuTimeId") Long meuTimeId);
}