package br.com.arenamatch.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventoAgendaDTO {
    private Long idPartida;
    private String titulo;      // Ex: "Contra Tabajara FC"
    private String subtitulo;   // Ex: "14:00 - 16:00 • Quadra ZN"
    private String tipo;        // "GAME" (Confirmado) ou "INVITE" (Pendente)
    private LocalDateTime dataHora;
    private Long idTimeAdversario;
    private String mensagem;      // O recado do desafiante
    private String cidade;        // Cidade do adversário
    private Double distanciaKm;   // Distância calculada
    private Double valorTaxa;     // Se tem custo
    private boolean temCampo;
    private String endereco;
    private String nomeTimeMandante;
    private String nomeTimeVisitante;
    private String posicaoMeuTime;
    private String posicaoAdversario;
    private String statusPlacar;  // "PENDENTE", "AGUARDANDO_CONFIRMACAO", "CONFIRMADO", "EM_DISPUTA"
    private Long idTimeQueInformou; // Para saber se fui eu ou o adversário que preencheu o placar primeiro
    private boolean passouDaHora; // Controle de tela para liberar a edição do placar
    private Integer golsMandante;
    private Integer golsVisitante;
    private boolean euInformeiOPlacar;
    private boolean adversarioInformouOPlacar;
    
}
