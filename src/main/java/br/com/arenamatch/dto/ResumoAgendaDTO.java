package br.com.arenamatch.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ResumoAgendaDTO {
    private LocalDate data;
    private String diaSemana; // "Seg", "Ter"
    private String diaMes;    // "04", "05"
    
    // Indicadores para as bolinhas
    private boolean temJogoConfirmado; // Status AGENDADO
    private boolean temDesafioPendente; // Status PENDENTE (Recebido)
    private boolean temCancelado;
}