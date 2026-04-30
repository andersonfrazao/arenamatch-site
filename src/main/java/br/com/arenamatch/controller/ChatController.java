package br.com.arenamatch.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.arenamatch.dto.ConversaInboxDTO;
import br.com.arenamatch.dto.MensagemChatDTO;
import br.com.arenamatch.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // ==========================================
    // MÉTODOS PARA PARTIDAS (JOGOS)
    // ==========================================
    
    @GetMapping("/inbox/{idTime}")
    public ResponseEntity<List<ConversaInboxDTO>> listarConversas(@PathVariable Long idTime) {
        return ResponseEntity.ok(chatService.listarConversasAtivas(idTime));
    }

    @GetMapping("/{idPartida}/historico/{idTime}")
    public ResponseEntity<List<MensagemChatDTO>> buscarHistorico(
            @PathVariable Long idPartida, 
            @PathVariable Long idTime) {
        return ResponseEntity.ok(chatService.buscarHistoricoPartida(idPartida, idTime));
    }

    @PostMapping("/{idPartida}/enviar/{idRemetente}")
    public ResponseEntity<Void> enviarMensagem(
            @PathVariable Long idPartida, 
            @PathVariable Long idRemetente, 
            @RequestBody String texto) {
        chatService.enviarMensagem(idPartida, idRemetente, texto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/nao-lidas/{idTime}")
    public ResponseEntity<Long> contarNaoLidasGeral(@PathVariable Long idTime) {
        return ResponseEntity.ok(chatService.contarNaoLidasGeral(idTime));
    }

    @PutMapping("/{idPartida}/lidas/{idTime}")
    public ResponseEntity<Void> marcarComoLidas(
            @PathVariable Long idPartida, 
            @PathVariable Long idTime) {
        chatService.marcarComoLidas(idPartida, idTime);
        return ResponseEntity.ok().build();
    }
    
    // ==========================================
    // NOVOS MÉTODOS PARA LIGAS
    // ==========================================
    
    @GetMapping("/liga/{idLiga}/historico/{idTime}")
    public ResponseEntity<List<MensagemChatDTO>> buscarHistoricoLiga(
            @PathVariable Long idLiga, 
            @PathVariable Long idTime) {
        return ResponseEntity.ok(chatService.buscarHistoricoLiga(idLiga, idTime));
    }

    @PostMapping("/liga/{idLiga}/enviar/{idRemetente}")
    public ResponseEntity<Void> enviarMensagemLiga(
            @PathVariable Long idLiga, 
            @PathVariable Long idRemetente, 
            @RequestBody String texto) {
        chatService.enviarMensagemLiga(idLiga, idRemetente, texto);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/liga/{idLiga}/lidas/{idTime}")
    public ResponseEntity<Void> marcarComoLidasLiga(
            @PathVariable Long idLiga, 
            @PathVariable Long idTime) {
        chatService.marcarComoLidasLiga(idLiga, idTime);
        return ResponseEntity.ok().build();
    }
}