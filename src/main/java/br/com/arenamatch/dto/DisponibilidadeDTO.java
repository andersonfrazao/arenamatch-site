package br.com.arenamatch.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadeDTO {

    private String diaSemana; // Ex: "Sábado", "Domingo"
    
    // Usamos String para facilitar o inputMask "99:99", 
    // mas no back-end converteremos para LocalTime se precisar salvar no banco
    private String inicio;    
    private String fim;       
    private CategoriaDTO categoria;
    private List<DisponibilidadeDTO> disponibilidades = new ArrayList<>();
    
    // Construtor utilitário para facilitar a criação rápida
    public DisponibilidadeDTO(String diaSemana, String inicio, String fim) {
        this.diaSemana = diaSemana;
        this.inicio = inicio;
        this.fim = fim;
    }
}