package br.com.arenamatch.dto;
import lombok.Data;

@Data
public class CancelamentoDTO {
    private Long idPartida;
    private Long idTime;
    private String motivo;
}