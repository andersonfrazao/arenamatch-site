package br.com.arenamatch.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.arenamatch.dto.ConversaInboxDTO;
import br.com.arenamatch.dto.MensagemChatDTO;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.MensagemChat;
import br.com.arenamatch.entity.MensagemChatLiga;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.MensagemChatLigaRepository;
import br.com.arenamatch.repository.MensagemChatRepository;
import br.com.arenamatch.repository.PartidaRepository;

@Service
public class ChatService {

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private MensagemChatRepository mensagemRepository;
    
    @Autowired
    private MensagemChatLigaRepository mensagemLigaRepository;
    
    @Autowired
    private LigaRepository ligaRepository;
    
    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate mensageiro;
    

    @Transactional(readOnly = true)
    public List<ConversaInboxDTO> listarConversasAtivas(Long meuTimeId) {
        List<ConversaInboxDTO> todasConversas = new ArrayList<>();

        // ==========================================
        // 1. BUSCA AS CONVERSAS DE JOGOS (Partidas)
        // ==========================================
        List<Partida> partidas = partidaRepository.buscarPartidasParaChat(meuTimeId);
        
        for (Partida p : partidas) {
            ConversaInboxDTO dto = new ConversaInboxDTO();
            dto.setTipo("JOGO");
            dto.setIdPartida(p.getId());
            dto.setStatusPartida(p.getStatus() != null ? p.getStatus().name() : null);
            dto.setDataJogo(p.getDataHora());

            // 🚨 NOVA REGRA DE NEGÓCIO: AS 3 CONDIÇÕES DE BLOQUEIO DE CHAT
            boolean placarConfirmado = p.getStatusPlacar() != null && "CONFIRMADO".equals(p.getStatusPlacar().name());
            boolean jogoCancelado = p.getStatus() != null && "CANCELADO".equals(p.getStatus().name());
            
            // CONVITE EXPIRADO: A data do jogo já passou E o convite nunca foi aceito (continua PENDENTE)
            boolean conviteExpirado = p.getStatus() != null && "PENDENTE".equals(p.getStatus().name()) 
                                      && p.getDataHora() != null && p.getDataHora().isBefore(LocalDateTime.now());
            
            // O chat bloqueia se qualquer uma dessas 3 coisas for verdade!
            dto.setEncerrada(placarConfirmado || jogoCancelado || conviteExpirado);

            Time adversario = p.getMandante().getId().equals(meuTimeId) ? p.getVisitante() : p.getMandante();
            dto.setIdAdversario(adversario.getId());
            dto.setNomeAdversario(adversario.getNome());
            
            dto.setQtdNaoLidas(mensagemRepository.contarNaoLidasPorPartida(p.getId(), meuTimeId));

            MensagemChat ultimaMsg = mensagemRepository.findFirstByPartidaIdOrderByDataHoraDesc(p.getId());
            if (ultimaMsg != null) {
                dto.setTextoUltimaMensagem(ultimaMsg.getTexto());
                dto.setHoraUltimaMensagem(ultimaMsg.getDataHora());
                dto.setEnviadaPorMim(ultimaMsg.getRemetente().getId().equals(meuTimeId)); 
            } else {
                dto.setTextoUltimaMensagem("Inicie uma conversa...");
                dto.setHoraUltimaMensagem(p.getDataSolicitacao()); 
                dto.setEnviadaPorMim(false);
            }
            todasConversas.add(dto);
        }

        // ==========================================
        // 2. BUSCA AS CONVERSAS DE LIGAS
        // ==========================================
        List<Liga> ligas = ligaRepository.buscarLigasDoTime(meuTimeId);
        
        for (Liga l : ligas) {
            ConversaInboxDTO dto = new ConversaInboxDTO();
            dto.setTipo("LIGA");
            dto.setIdLiga(l.getId());
            dto.setNomeAdversario(l.getNome()); 
            
            dto.setEncerrada(false);
            
            dto.setQtdNaoLidas(mensagemLigaRepository.contarNaoLidasPorLiga(l.getId(), meuTimeId));

            MensagemChatLiga ultimaMsgLiga = mensagemLigaRepository.findFirstByLigaIdOrderByDataHoraDesc(l.getId());
            if (ultimaMsgLiga != null) {
                dto.setTextoUltimaMensagem(ultimaMsgLiga.getTexto());
                dto.setHoraUltimaMensagem(ultimaMsgLiga.getDataHora());
                dto.setEnviadaPorMim(ultimaMsgLiga.getRemetente().getId().equals(meuTimeId));
            } else {
                dto.setTextoUltimaMensagem("Bem-vindo à liga!");
                dto.setHoraUltimaMensagem(l.getDataCriacao()); 
                dto.setEnviadaPorMim(false);
            }
            todasConversas.add(dto);
        }

        // ==========================================
        // 3. ORDENA TUDO (Mais recentes no topo)
        // ==========================================
        todasConversas.sort((c1, c2) -> {
            if (c1.getHoraUltimaMensagem() == null) return 1;
            if (c2.getHoraUltimaMensagem() == null) return -1;
            return c2.getHoraUltimaMensagem().compareTo(c1.getHoraUltimaMensagem());
        });

        return todasConversas;
    }
    

    // =======================================================
    // BUSCAR HISTÓRICO DA CONVERSA
    // =======================================================
    @Transactional(readOnly = true)
    public List<MensagemChatDTO> buscarHistoricoPartida(Long idPartida, Long meuTimeId) {
        List<MensagemChat> mensagens = mensagemRepository.findByPartidaIdOrderByDataHoraAsc(idPartida);
        List<MensagemChatDTO> historico = new ArrayList<>();

        for (MensagemChat m : mensagens) {
            MensagemChatDTO dto = new MensagemChatDTO();
            dto.setId(m.getId());
            dto.setIdPartida(idPartida);
            dto.setIdRemetente(m.getRemetente().getId());
            dto.setNomeRemetente(m.getRemetente().getNome());
            dto.setTexto(m.getTexto());
            dto.setDataHora(m.getDataHora());
            dto.setEnviadaPorMim(m.getRemetente().getId().equals(meuTimeId));
            
            historico.add(dto);
        }
        return historico;
    }

    // =======================================================
    // ENVIAR MENSAGEM (SALVA NO BANCO E AVISA NO WEBSOCKET)
    // =======================================================
    @Transactional
    public void enviarMensagem(Long idPartida, Long idRemetente, String texto) {
        Partida partida = partidaRepository.findById(idPartida)
            .orElseThrow(() -> new RuntimeException("Partida não encontrada"));
            
        // 🚨 REPLICANDO A TRAVA DE SEGURANÇA AQUI TAMBÉM
        boolean placarConfirmado = partida.getStatusPlacar() != null && "CONFIRMADO".equals(partida.getStatusPlacar().name());
        boolean jogoCancelado = partida.getStatus() != null && "CANCELADO".equals(partida.getStatus().name());
        boolean conviteExpirado = partida.getStatus() != null && "PENDENTE".equals(partida.getStatus().name()) 
                                  && partida.getDataHora() != null && partida.getDataHora().isBefore(LocalDateTime.now());
        
        if (placarConfirmado || jogoCancelado || conviteExpirado) {
            throw new RuntimeException("Não é possível enviar mensagens. Esta partida já está encerrada ou o convite expirou.");
        }

        Time remetente = new Time();
        remetente.setId(idRemetente);

        MensagemChat novaMsg = new MensagemChat();
        novaMsg.setPartida(partida);
        novaMsg.setRemetente(remetente);
        novaMsg.setTexto(texto);
        novaMsg.setDataHora(LocalDateTime.now());
        
        novaMsg = mensagemRepository.save(novaMsg);

        MensagemChatDTO dto = new MensagemChatDTO();
        dto.setId(novaMsg.getId());
        dto.setIdPartida(idPartida);
        dto.setIdRemetente(idRemetente);
        dto.setTexto(texto);
        dto.setDataHora(novaMsg.getDataHora());

        mensageiro.convertAndSend("/topic/chat/" + idPartida, dto);
    }
    
    @Transactional(readOnly = true)
    public Long contarNaoLidasGeral(Long meuTimeId) {
        return mensagemRepository.contarMensagensNaoLidasGeral(meuTimeId);
    }

    @Transactional
    public void marcarComoLidas(Long idPartida, Long meuTimeId) {
        mensagemRepository.marcarMensagensComoLidas(idPartida, meuTimeId);
    }
    
    // =======================================================
    // LIGAS: BUSCAR HISTÓRICO DA CONVERSA
    // =======================================================
    @Transactional(readOnly = true)
    public List<MensagemChatDTO> buscarHistoricoLiga(Long idLiga, Long meuTimeId) {
        List<MensagemChatLiga> mensagens = mensagemLigaRepository.findByLigaIdOrderByDataHoraAsc(idLiga);
        List<MensagemChatDTO> historico = new ArrayList<>();

        for (MensagemChatLiga m : mensagens) {
            MensagemChatDTO dto = new MensagemChatDTO();
            dto.setId(m.getId());
            dto.setIdPartida(null); 
            dto.setIdRemetente(m.getRemetente().getId());
            dto.setNomeRemetente(m.getRemetente().getNome());
            dto.setTexto(m.getTexto());
            dto.setDataHora(m.getDataHora());
            dto.setEnviadaPorMim(m.getRemetente().getId().equals(meuTimeId));
            
            historico.add(dto);
        }
        return historico;
    }

    // =======================================================
    // LIGAS: ENVIAR MENSAGEM
    // =======================================================
    @Transactional
    public void enviarMensagemLiga(Long idLiga, Long idRemetente, String texto) {
        Liga liga = ligaRepository.findById(idLiga)
            .orElseThrow(() -> new RuntimeException("Liga não encontrada"));
            
        Time remetente = new Time();
        remetente.setId(idRemetente);

        MensagemChatLiga novaMsg = new MensagemChatLiga();
        novaMsg.setLiga(liga);
        novaMsg.setRemetente(remetente);
        novaMsg.setTexto(texto);
        novaMsg.setDataHora(LocalDateTime.now());
        
        novaMsg = mensagemLigaRepository.save(novaMsg);

        MensagemChatDTO dto = new MensagemChatDTO();
        dto.setId(novaMsg.getId());
        dto.setIdPartida(null); 
        dto.setIdRemetente(idRemetente);
        dto.setNomeRemetente(remetente.getNome());
        dto.setTexto(texto);
        dto.setDataHora(novaMsg.getDataHora());

        mensageiro.convertAndSend("/topic/chat/liga/" + idLiga, dto);
    }
    
    @Transactional
    public void marcarComoLidasLiga(Long idLiga, Long meuTimeId) {
        mensagemLigaRepository.marcarMensagensComoLidas(idLiga, meuTimeId);
    }
    
}