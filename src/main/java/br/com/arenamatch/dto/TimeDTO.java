package br.com.arenamatch.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeDTO {
    private Long id;
    private String nome;
    private Integer pontos;
    private Integer partidasJogadas;
    private Integer vitorias;
    private Integer empates;
    private Integer derrotas;
    private Integer golsPro;
    private Integer golsContra;
    
    // Campo calculado para facilitar a vida do JSF
    public Integer getSaldoGols() {
        return (golsPro != null ? golsPro : 0) - (golsContra != null ? golsContra : 0);
    }
}