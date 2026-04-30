package br.com.arenamatch.dto;
import lombok.Data;

@Data
public class RespostaCancelamentoDTO {
    private Long idPartida;
    private Long idTime;
    private boolean aceitar;
}