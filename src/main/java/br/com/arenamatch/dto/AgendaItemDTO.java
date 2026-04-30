package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Categoria;
import lombok.Data;

@Data
public class AgendaItemDTO {
    private Categoria categoria;
    private String diaSemana;
    private String inicio;
    private String fim;
    
    // Construtor auxiliar
    public AgendaItemDTO(Categoria categoria, String diaSemana, String inicio, String fim) {
        this.categoria = categoria;
        this.diaSemana = diaSemana;
        this.inicio = inicio;
        this.fim = fim;
    }
}