package br.com.arenamatch.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.arenamatch.dto.CancelamentoDTO;
import br.com.arenamatch.dto.EventoAgendaDTO;
import br.com.arenamatch.dto.PartidaDTO;
import br.com.arenamatch.dto.RespostaCancelamentoDTO;
import br.com.arenamatch.dto.ResumoAgendaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.service.AgendaService;
import br.com.arenamatch.service.PartidaService;
import br.com.arenamatch.service.TimeService;

@RestController
@RequestMapping("/api/agenda")
public class AgendaController {

    @Autowired private PartidaService partidaService;
    @Autowired private AgendaService agendaService;
    @Autowired private TimeService timeService;
    
    @GetMapping("/time/usuario/{idUsuario}")
    public ResponseEntity<TimeResumoDTO> buscarMeuTime(@PathVariable Long idUsuario) {
        return timeService.buscarResumoPorResponsavel(idUsuario)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/partidas/{idTime}")
    public ResponseEntity<List<PartidaDTO>> listarProximosJogos(@PathVariable Long idTime) {
        
        // O Controller apenas repassa o ID para o Service e devolve o resultado!
        List<PartidaDTO> partidas = partidaService.listarProximosJogos(idTime);
        
        return ResponseEntity.ok(partidas);
    }

    @PostMapping("/cancelar")
    public ResponseEntity<Void> solicitarCancelamento(@RequestBody CancelamentoDTO dto) {
        partidaService.solicitarCancelamento(dto.getIdPartida(), dto.getIdTime(), dto.getMotivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/responder-cancelamento")
    public ResponseEntity<Void> responderCancelamento(@RequestBody RespostaCancelamentoDTO dto) {
        partidaService.responderCancelamento(dto.getIdPartida(), dto.getIdTime(), dto.isAceitar());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/calendario")
    public ResponseEntity<List<ResumoAgendaDTO>> getCalendario(
            @RequestParam Long timeId,
            @RequestParam(defaultValue = "7") int dias) { // Padrão 7 dias
        
        return ResponseEntity.ok(agendaService.montarCalendario(timeId, LocalDate.now(), dias));
    }

    @GetMapping("/detalhes")
    public ResponseEntity<List<EventoAgendaDTO>> getDetalhesDia(
            @RequestParam Long timeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        
        return ResponseEntity.ok(agendaService.buscarDetalhesDoDia(timeId, data));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirPartida(@PathVariable Long id) {
        partidaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/calendario/{timeId}")
    public ResponseEntity<List<ResumoAgendaDTO>> buscarCalendario(
            @PathVariable Long timeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataBase) {
        
        // Se a tela não enviar data (ex: ao abrir a página pela 1ª vez), assume hoje
        if (dataBase == null) {
            dataBase = LocalDate.now();
        }

        // Chama o seu Service passando o timeId, a dataBase e mandando buscar 15 dias
        List<ResumoAgendaDTO> calendario = agendaService.montarCalendario(timeId, dataBase, 15);
        return ResponseEntity.ok(calendario);
    }
    
}
