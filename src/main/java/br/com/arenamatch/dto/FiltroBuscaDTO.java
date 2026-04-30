package br.com.arenamatch.dto;

import java.time.LocalDate;

import br.com.arenamatch.enums.Categoria;
import lombok.Data;

@Data
public class FiltroBuscaDTO {
    private String cidade;
    private String diaSemana;
    private Categoria categoria;
    private String cepReferencia;
    private String nomeTime; 
    private String nomeLiga;
    private Integer raioKm = 30; // Valor padrão inicial: 30km
    private LocalDate dataJogo;
    private Double latitudeReferencia;
    private Double longitudeReferencia;
    private Long idUsuarioLogado;
}