package br.com.arenamatch.dto;

import java.time.LocalDateTime;

import br.com.arenamatch.enums.Categoria;
import lombok.Data;

@Data
public class DesafioDTO {
    private Long idTimeDesafiante;
    private Long idTimeDesafiado;
    private String mensagem; // Preparando o terreno para o futuro chat/negociação
    private LocalDateTime dataHoraPartida;
    private Categoria categoria;
}
