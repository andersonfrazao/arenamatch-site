package br.com.arenamatch.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MensagemChatDTO implements Serializable {
    private Long id;
    private Long idPartida;
    private Long idRemetente;
    private String nomeRemetente;
    private String texto;
    private LocalDateTime dataHora;
    
    // Flag de UI: true se fui eu que enviei (balão verde na direita), false se foi o adversário (balão cinza na esquerda)
    private boolean enviadaPorMim; 
}