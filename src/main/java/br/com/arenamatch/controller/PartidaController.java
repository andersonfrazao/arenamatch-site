package br.com.arenamatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.arenamatch.dto.CancelamentoDTO;
import br.com.arenamatch.dto.DesafioDTO;
import br.com.arenamatch.dto.PlacarRequestDTO;
import br.com.arenamatch.dto.RespostaCancelamentoDTO;
import br.com.arenamatch.service.PartidaService;

@RestController
@RequestMapping("/api/partidas")
public class PartidaController {

    @Autowired
    private PartidaService partidaService;

    // --- 1. ENVIAR CONVITE ---
    @PostMapping("/desafiar")
    public ResponseEntity<Void> enviarDesafio(@RequestBody DesafioDTO dto) {
        partidaService.criarDesafio(dto);
        return ResponseEntity.ok().build();
    }
    
    // --- 2. ACEITAR CONVITE ---
    @PutMapping("/{id}/aceitar")
    public ResponseEntity<Void> aceitarDesafio(@PathVariable Long id) {
        partidaService.aceitarDesafio(id);
        return ResponseEntity.ok().build();
    }

    // --- 3. RECUSAR CONVITE ou CANCELAR ENVIO DE CONVITE ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirPartida(@PathVariable Long id) {
        // Este método apaga o convite pendente do banco
        partidaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // --- 4. SOLICITAR CANCELAMENTO DE JOGO MARCADO ---
    @PostMapping("/solicitar-cancelamento")
    public ResponseEntity<Void> solicitarCancelamento(@RequestBody CancelamentoDTO dto) {
        partidaService.solicitarCancelamento(dto.getIdPartida(), dto.getIdTime(), dto.getMotivo());
        return ResponseEntity.ok().build();
    }

    // --- 5. RESPONDER (ACEITAR/RECUSAR) O CANCELAMENTO ---
    @PostMapping("/responder-cancelamento")
    public ResponseEntity<Void> responderCancelamento(@RequestBody RespostaCancelamentoDTO dto) {
        partidaService.responderCancelamento(dto.getIdPartida(), dto.getIdTime(), dto.isAceitar());
        return ResponseEntity.ok().build();
    }
    
 // Rota 1: Usada pela Agenda e pelo Sininho (onde já temos o ID da partida)
    @DeleteMapping("/{idPartida}")
    public ResponseEntity<Void> cancelarConvitePorId(@PathVariable Long idPartida) {
        partidaService.cancelarConvitePorId(idPartida);
        return ResponseEntity.noContent().build();
    }

    // Rota 2: Usada pela Busca (onde sabemos apenas quem somos e quem é o adversário)
    @DeleteMapping("/cancelar/{meuTimeId}/{adversarioId}")
    public ResponseEntity<Void> cancelarConvitePorAdversario(
            @PathVariable Long meuTimeId, 
            @PathVariable Long adversarioId) {
        
        partidaService.cancelarConvitePorAdversario(meuTimeId, adversarioId);
        return ResponseEntity.noContent().build();
    }
    
 // Recebe a requisição do Client para enviar o primeiro palpite de placar
    @PostMapping("/{id}/placar")
    public ResponseEntity<Void> informarPlacar(@PathVariable Long id, @RequestBody PlacarRequestDTO dto) {
        partidaService.informarPlacar(id, dto.getGolsMandante(), dto.getGolsVisitante(), dto.getIdTimeInformante());
        return ResponseEntity.ok().build();
    }

    // Recebe a confirmação do adversário (que clicou no botão verde do sininho)
    @PostMapping("/{id}/confirmar-placar")
    public ResponseEntity<Void> confirmarPlacar(@PathVariable Long id) {
        partidaService.confirmarPlacar(id);
        return ResponseEntity.ok().build();
    }

    // Recebe a contestação do adversário (que clicou no botão vermelho do sininho)
    @PostMapping("/{id}/contestar-placar")
    public ResponseEntity<Void> contestarPlacar(@PathVariable Long id) {
        partidaService.contestarPlacar(id);
        return ResponseEntity.ok().build();
    }
}
