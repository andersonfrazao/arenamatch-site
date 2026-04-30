package br.com.arenamatch.dto;

import br.com.arenamatch.enums.StatusPartida;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartidaDTO {

    private Long id;
    
    // Trocamos a Entidade Time pelo DTO
    private TimeResumoDTO mandante;
    private TimeResumoDTO visitante;
    
    private LocalDateTime dataHora;
    private StatusPartida status;

    // --- Controle de Cancelamento ---
    
    // Trocamos a Entidade Time pelo DTO
    private TimeResumoDTO solicitanteCancelamento;
    
    private String motivoCancelamento;
    private LocalDateTime dataSolicitacao;
}