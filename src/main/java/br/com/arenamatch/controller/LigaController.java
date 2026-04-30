package br.com.arenamatch.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.arenamatch.dto.EnviarConviteLigaDTO;
import br.com.arenamatch.dto.LigaDetalheDTO;
import br.com.arenamatch.dto.LigaExplorarDTO;
import br.com.arenamatch.dto.ResponderConviteLigaDTO;
import br.com.arenamatch.dto.TimeSimplesDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.service.LigaService;

@RestController
@RequestMapping("/api/ligas")
public class LigaController {

    @Autowired
    private LigaService ligaService;

    // --- 1. CRIAR NOVA LIGA ---
    @PostMapping
    public ResponseEntity<LigaDetalheDTO> criarLiga(@RequestBody br.com.arenamatch.dto.NovaLigaDTO dto) {
        
        // O Controller não sabe o que é a entidade Liga. Ele recebe DTO e devolve DTO!
        LigaDetalheDTO ligaCriadaDTO = ligaService.criarLiga(dto.getIdTimeAdmin(), dto.getNome(), dto.getDescricao());
        
        return ResponseEntity.ok(ligaCriadaDTO);
    }
    
    // --- 2. ENVIAR CONVITE PARA A LIGA ---
    @PostMapping("/convites")
    public ResponseEntity<Void> enviarConvite(@RequestBody EnviarConviteLigaDTO dto) {
        ligaService.enviarConvite(dto.getIdLiga(), dto.getIdTimeConvidado(), dto.getMensagem());
        return ResponseEntity.ok().build();
    }

    // --- 3. RESPONDER A UM CONVITE (ACEITAR/RECUSAR) ---
    @PostMapping("/convites/responder")
    public ResponseEntity<Void> responderConvite(@RequestBody ResponderConviteLigaDTO dto) {
        ligaService.responderConvite(dto.getIdConvite(), dto.isAceitar());
        return ResponseEntity.ok().build();
    }


    // --- NOVO: REMOVER MEMBRO ---
    @DeleteMapping("/{idLiga}/membros/{idTime}")
    public ResponseEntity<Void> removerMembro(@PathVariable Long idLiga, @PathVariable Long idTime) {
        ligaService.removerMembro(idLiga, idTime);
        return ResponseEntity.ok().build();
    
    }
    @GetMapping("/{id}")
    public ResponseEntity<LigaDetalheDTO> buscarLigaPorId(@PathVariable Long id) {
        Liga liga = ligaService.buscarLigaPorId(id);
        
        // Conversão Manual (ou via MapStruct/ModelMapper se você usar)
        LigaDetalheDTO dto = new LigaDetalheDTO();
        dto.setId(liga.getId());
        dto.setNome(liga.getNome());
        dto.setDescricao(liga.getDescricao());
        
        dto.setAdmin(new TimeSimplesDTO(liga.getAdmin().getId(), liga.getAdmin().getNome()));
        
        List<TimeSimplesDTO> membrosDTO = liga.getTimes().stream()
                .map(t -> new TimeSimplesDTO(t.getId(), t.getNome()))
                .toList();
        dto.setTimes(membrosDTO);

        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/time/{timeId}")
    public ResponseEntity<List<LigaDetalheDTO>> buscarLigasDoTime(@PathVariable Long timeId) {
        return ResponseEntity.ok(ligaService.buscarLigasDoTime(timeId));
    }

    @GetMapping("/convites/time/{timeId}")
    public ResponseEntity<List<br.com.arenamatch.dto.ConviteLigaDTO>> buscarConvitesPendentes(@PathVariable Long timeId) {
        return ResponseEntity.ok(ligaService.buscarConvitesPendentesDoTime(timeId));
    }
    
    @GetMapping("/convites/agenda/time/{timeId}")
    public ResponseEntity<List<br.com.arenamatch.dto.ConviteLigaDTO>> buscarConvitesParaAgenda(@PathVariable Long timeId) {
        return ResponseEntity.ok(ligaService.buscarConvitesParaAgenda(timeId));
    }
    
 // --- NOVO: ROTA PARA BUSCAR IDS COM CONVITE PENDENTE ---
    @GetMapping("/{idLiga}/convites/pendentes/times")
    public ResponseEntity<List<Long>> buscarIdsTimesComConvitePendente(@PathVariable Long idLiga) {
        return ResponseEntity.ok(ligaService.buscarIdsTimesComConvitePendente(idLiga));
    }
    
    @GetMapping("/explorar/top/{meuTimeId}")
    public ResponseEntity<List<LigaExplorarDTO>> listarLigasEmAlta(@PathVariable Long meuTimeId) {
        return ResponseEntity.ok(ligaService.listarLigasEmAlta(meuTimeId));
    }

    @GetMapping("/explorar/busca/{nomeBusca}/{meuTimeId}")
    public ResponseEntity<List<LigaExplorarDTO>> buscarLigasPorNome(
            @PathVariable String nomeBusca, 
            @PathVariable Long meuTimeId) {
        return ResponseEntity.ok(ligaService.buscarLigasPorNome(nomeBusca, meuTimeId));
    }

    @PostMapping("/{idLiga}/solicitar-entrada/{meuTimeId}")
    public ResponseEntity<Void> solicitarEntradaNaLiga(
            @PathVariable Long idLiga, 
            @PathVariable Long meuTimeId) {
        ligaService.solicitarEntradaNaLiga(idLiga, meuTimeId);
        return ResponseEntity.ok().build();
    }
    
    
}