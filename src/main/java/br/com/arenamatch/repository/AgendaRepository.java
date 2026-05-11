package br.com.arenamatch.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.arenamatch.entity.Agenda;
import br.com.arenamatch.enums.Categoria;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, Long> {
    // Futuramente usaremos aqui para buscar times por dia da semana!
	
	
	@org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Agenda a WHERE a.time.id = :timeId")
    void deleteByTimeId(@org.springframework.data.repository.query.Param("timeId") Long timeId);
	
	List<Agenda> findByTimeId(Long timeId);

    Optional<Agenda> findFirstByTimeIdAndDiaSemanaAndCategoriaOrderByHoraInicioAsc(Long timeId, String diaSemana, Categoria categoria);

    Optional<Agenda> findFirstByTimeIdAndDiaSemanaOrderByHoraInicioAsc(Long timeId, String diaSemana);
}
