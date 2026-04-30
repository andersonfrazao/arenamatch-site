package br.com.arenamatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlacarRequestDTO {
    private Integer golsMandante;
    private Integer golsVisitante;
    private Long idTimeInformante;
}