package br.com.arenamatch.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ConversaInboxDTO implements Serializable {
    
    // --- NOVOS CAMPOS PARA SUPORTAR LIGAS (Chat Híbrido) ---
    private String tipo; // Vai receber "JOGO" ou "LIGA"
    private Long idLiga; // Fica preenchido só quando for Liga
    
    // --- SEUS CAMPOS ORIGINAIS (INTACTOS) ---
    private Long idPartida;
    private Long idAdversario;
    private String nomeAdversario; // Reutilizaremos para o Nome da Liga também
    private LocalDateTime dataJogo;
    private String statusPartida; // PENDENTE, AGENDADO...
    
    // Resumo da última mensagem para aparecer na lista
    private String textoUltimaMensagem;
    private LocalDateTime horaUltimaMensagem;
    private boolean enviadaPorMim; // Para colocar aquele "Você: " antes da mensagem
    private Long qtdNaoLidas;
    private boolean encerrada;
}